package eseatech;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

    @FXML
    private Menu menu_serial_port;

    @FXML
    private ProgressBar battery_indicator;

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
                        } catch (IOException | MessageTypeException e) {
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

            dataProvider = new DataProvider(newSerialPort);
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
                setCurrentSerialPort(port);
            });

            menu_serial_port.getItems().add(menuItem);
        }
    }

    private void distributeData(Map<String, Float> dataEntry) {
        Float battery_level = dataEntry.get("battery");
        if (battery_level != null)
            updateBatteryLevel(battery_level);
    }

    private void updateBatteryLevel(float newValue) {
        if (newValue < 0)
            newValue = 0;
        else if (newValue > 1)
            newValue = 1;

        battery_indicator.setProgress(newValue);
    }
}
