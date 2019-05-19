package eseatech;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.LibUsb;

import javax.rmi.CORBA.Util;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private MenuItem COM1;

    @FXML
    protected void handleCOMPort(ActionEvent event) {
        updateArduinoBanner();
    }

    @FXML
    protected void handleMenuClose(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private Label connected_label;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        int res = Utils.registerUSBCallback(
                (Context context, Device device, int event, Object userData) -> {
                    System.out.println("Hotplug Callback called!");
                    updateArduino(event);
                    return 0;
                });
        if (res != LibUsb.SUCCESS) {
            System.err.printf("Unable to register the hotplug callback: %s\n", LibUsb.errorName(res));
        }
        updateArduino(0);
    }

    private void updateArduino(int event) {
        switch (event) {
            case LibUsb.HOTPLUG_EVENT_DEVICE_LEFT:
                Utils.closeArduino();
                break;
            default:
                int result = Utils.openArduino();
                if (result != 0) {
                    System.err.printf("Unable to open Arduino USB: %s\n", LibUsb.errorName(result));
                }
                break;
        }
        Platform.runLater(() -> updateArduinoBanner());
    }

    private void updateArduinoBanner() {
        if (Utils.isArduinoOpen()) {
            connected_label.setText("Arduino connecté");
            connected_label.setStyle("-fx-background-color: #00FF00;");
        } else {
            connected_label.setText("Arduino déconnecté");
            connected_label.setStyle("-fx-background-color: #FF0000;");
        }
    }
}
