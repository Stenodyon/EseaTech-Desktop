package eseatech;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public static SerialPort currentSerialPort = null;
    private MessageUnpacker unpacker = null;

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

        Thread dataThread = new Thread(() -> {
            while (true) {
                if (unpacker != null) {
                    System.out.print("unpacker is not null!\n");

                    byte[] command = new byte[1];
                    command[0] = 'a';
                    currentSerialPort.writeBytes(command, command.length);
                    try {
                        Map<String, Float> valuemap = unpackMap(unpacker);
                        System.out.printf("%s\n", valuemap.toString());
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

    private void setCurrentSerialPort(SerialPort newSerialPort) {
        if (currentSerialPort != null) {
            unpacker = null;
            currentSerialPort.closePort();
        }
        currentSerialPort = newSerialPort;
        currentSerialPort.openPort();
        currentSerialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        unpacker = MessagePack.newDefaultUnpacker(currentSerialPort.getInputStream());

//        currentSerialPort.addDataListener(new SerialPortDataListener() {
//            @Override
//            public int getListeningEvents() {
//                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
//            }
//
//            @Override
//            public void serialEvent(SerialPortEvent event) {
//                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
//                    return;
//                byte[] data = new byte[currentSerialPort.bytesAvailable()];
//                int numRead = currentSerialPort.readBytes(data, data.length);
//                System.out.printf("Read %d bytes: \"%s\"\n", numRead, data.toString());
//            }
//        });
    }

    private Map<String, Float> unpackMap(MessageUnpacker unpacker) throws IOException {
        int length = unpacker.unpackMapHeader();
        System.out.printf("New map of length %d\n", length);
        Map<String, Float> map = new HashMap<>(length);
        for (int i = 0; i < length; i++) {
            String key = unpacker.unpackString();
            System.out.printf("\tNew key \"%s\"\n", key);
            float value = unpacker.unpackFloat();
            System.out.printf("\twith value %f\n", value);
            map.put(key, value);
        }
        return map;
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
