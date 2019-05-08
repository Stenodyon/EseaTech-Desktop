package eseatech;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import org.usb4java.Device;

public class Controller {

    @FXML
    private MenuItem COM1;

    @FXML
    protected void handleCOMPort(ActionEvent event) {
        Device arduino = Utils.findArduino();

        if (arduino != null)
            System.out.println("Found arduino!");
        else
            System.out.println("Arduino not found!");
    }

    @FXML
    protected void handleMenuClose(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }
}
