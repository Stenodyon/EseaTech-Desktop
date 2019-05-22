package eseatech;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Menu;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import org.msgpack.core.MessageTypeException;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

public class Controller implements Initializable {

    private static Semaphore dataProviderMutex = null;
    private static DataProvider dataProvider = null;

    private XYChart.Series<Float, Float> humidityData = null;
    private XYChart.Series<Float, Float> temperatureData = null;
    private XYChart.Series<Float, Float> gpsData = null;

    @FXML
    private Menu menu_serial_port;

    @FXML
    private ProgressBar battery_indicator;

    @FXML
    private ProgressBar power_indicator;

    @FXML
    private LineChart<Float, Float> humidity_indicator;

    @FXML
    private LineChart<Float, Float> temperature_indicator;

    @FXML
    private ScatterChart<Float, Float> gps_chart;

    @FXML
    protected void handleMenuClose(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dataProviderMutex = new Semaphore(1);

        generateSerialMenu();

        Thread dataThread = new Thread(() -> {
            while (true) {
                try {
                    dataProviderMutex.acquire();

                    if (dataProvider != null) {
                        try {
                            Map<String, Float>[] entries = dataProvider.fetchData();
                            System.out.printf("Fetched %d entries\n", entries.length);
                            for (Map<String, Float> entry : entries) {
                                System.out.println(entry.toString());
                                distributeData(entry);
                            }
                        } catch (MessageTypeException e) {
                            dataProvider.clearBytes();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    dataProviderMutex.release();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        dataThread.setDaemon(true);
        dataThread.start();

        humidityData = new XYChart.Series<>();
        humidity_indicator.getData().add(humidityData);
        temperatureData = new XYChart.Series<>();
        temperature_indicator.getData().add(temperatureData);
        gpsData = new XYChart.Series<>();
        gps_chart.getData().add(gpsData);
    }

    public static DataProvider getDataProvider() {
        return dataProvider;
    }

    private void setCurrentSerialPort(SerialPort newSerialPort) {
        try {
            dataProviderMutex.acquire();

            if (dataProvider != null) {
                if (dataProvider.connectedTo(newSerialPort))
                    return;
                dataProvider.close();
            }

            dataProvider = newSerialPort != null ? new DataProvider(newSerialPort) : null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            dataProviderMutex.release();
        }
    }

    private void generateSerialMenu() {
        ToggleGroup toggleGroup = new ToggleGroup();

        menu_serial_port.getItems().clear();
        for (SerialPort port : SerialPort.getCommPorts())
        {
            String label = String.format("%s (%s)", port.getSystemPortName(), port.getPortDescription());

            RadioMenuItem menuItem = new RadioMenuItem(label);
            menuItem.setToggleGroup(toggleGroup);
            menuItem.setOnAction(event -> {
                setCurrentSerialPort(menuItem.isSelected() ? port : null);
            });

            menu_serial_port.getItems().add(menuItem);
        }
    }

    private float startTimestamp = -1;
    private void distributeData(Map<String, Float> dataEntry) {
        Float battery_level = dataEntry.get("battery");
        if (battery_level != null)
            updateBatteryLevel(battery_level);

        Float power_level = dataEntry.get("power");
        if (power_level != null)
            updateBatteryPower(power_level);

        Float gps_x = dataEntry.get("gps_x");
        Float gps_y = dataEntry.get("gps_y");
        if (gps_x != null && gps_y != null)
            updateGPSCoordinates(gps_x, gps_y);

        Float timestamp = dataEntry.get("timestamp");
        if (timestamp == null)
            return;
        if (startTimestamp < 0)
            startTimestamp = timestamp;
        timestamp -= startTimestamp;

        Float humidity = dataEntry.get("humidity");
        if (humidity != null)
            updateHumidityLevel(timestamp, humidity);

        Float temperature = dataEntry.get("temperature");
        if (temperature != null)
            updateTemperatureLevel(timestamp, temperature);
    }

    private void updateBatteryLevel(float newValue) {
        if (newValue < 0)
            newValue = 0;
        else if (newValue > 1)
            newValue = 1;

        battery_indicator.setProgress(newValue);
    }

    private float maxBatteryPower = 0;
    private void updateBatteryPower(float newValue) {
        if (newValue > maxBatteryPower)
            maxBatteryPower = newValue;

        power_indicator.setProgress(newValue / maxBatteryPower);
    }

    private void updateHumidityLevel(float timestamp, float newValue) {
        Platform.runLater(() -> {
            humidityData.getData().add(new XYChart.Data<>(timestamp, newValue));
            humidity_indicator.setTitle(String.format("Humidité (%f)", newValue));
        });
    }

    private void updateTemperatureLevel(float timestamp, float newValue) {
        Platform.runLater(() -> {
            temperatureData.getData().add(new XYChart.Data<>(timestamp, newValue));
            temperature_indicator.setTitle(String.format("Température (%f)", newValue));
        });
    }

    private void updateGPSCoordinates(float gps_x, float gps_y) {
        Platform.runLater(() ->
                gpsData.getData().add(new XYChart.Data<>(gps_x, gps_y)));
    }
}
