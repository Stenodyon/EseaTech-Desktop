package eseatech;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private static DataProvider dataProvider = null;

    @FXML
    private Menu menu_serial_port;

    @FXML
    protected void handleMenuClose(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        generateSerialMenu();

        Thread dataThread = new Thread(() -> {
            while (true) {
                if (dataProvider != null) {
                    try {
                        Map<String, Float>[] entries = dataProvider.fetchData();
                        System.out.printf("Fetched %d entries\n", entries.length);
                        for (Map<String, Float> entry : entries) {
                            System.out.println(entry.toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(1000);
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
        if (dataProvider != null) {
            if (dataProvider.connectedTo(newSerialPort))
                return;
            dataProvider.close();
        }

        dataProvider = new DataProvider(newSerialPort);
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
}
