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

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private Device arduino = null;

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
                    updateArduino();
                    return 0;
                });
        if (res != LibUsb.SUCCESS) {
            System.err.printf("Unable to register the hotplug callback: %s\n", LibUsb.errorName(res));
        }
        updateArduino();
    }

    private void updateArduino() {
        arduino = Utils.findArduino();
        Platform.runLater(() -> updateArduinoBanner());
    }

    private void updateArduinoBanner() {
        if (arduino != null) {
            connected_label.setText("Arduino connecté");
            connected_label.setStyle("-fx-background-color: #00FF00;");
        } else {
            connected_label.setText("Arduino déconnecté");
            connected_label.setStyle("-fx-background-color: #FF0000;");
        }
    }
}
