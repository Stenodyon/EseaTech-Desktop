package eseatech;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public static SerialPort currentSerialPort = null;

    @FXML
    private Menu menu_serial_port;

    @FXML
    protected void handleCOMPort(ActionEvent event) {
    }

    @FXML
    protected void handleMenuClose(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        generateSerialMenu();
    }

    private void setCurrentSerialPort(SerialPort newSerialPort) {
        if (currentSerialPort != null) {
            currentSerialPort.closePort();
        }
        currentSerialPort = newSerialPort;
        currentSerialPort.openPort();
        currentSerialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;
                byte[] data = new byte[currentSerialPort.bytesAvailable()];
                int numRead = currentSerialPort.readBytes(data, data.length);
                System.out.printf("Read %d bytes: \"%s\"\n", numRead, data.toString());
            }
        });
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
