package eseatech;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import org.usb4java.Device;

public class Controller {

    @FXML
    private MenuItem COM1;

    @FXML
    protected void handleCOMPort(ActionEvent event) {
        Device arduino = Utils.findArduino();

        if (arduino != null) {
            connected_label.setText("Arduino connecté");
            connected_label.setStyle("-fx-background-color: #00FF00;");
        } else {
            connected_label.setText("Arduino déconnecté");
            connected_label.setStyle("-fx-background-color: #FF0000;");
        }
    }

    @FXML
    protected void handleMenuClose(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private Label connected_label;
}
