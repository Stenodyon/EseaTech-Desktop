package eseatech;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private final String[] testPorts = {
            "/dev/ttyUSB0",
            "/dev/ttyACM0",
    };

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
        // TODO: Generate menu entries for USB ports
        generateSerialMenu();
    }

    private void generateSerialMenu() {
        menu_serial_port.getItems().clear();
        for (String port : testPorts) {
            MenuItem menuItem = new MenuItem(port);
            menu_serial_port.getItems().add(menuItem);
        }
    }
}
