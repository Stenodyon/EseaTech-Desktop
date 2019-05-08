package eseatech;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;

public class Controller {

    @FXML
    private MenuItem COM1;

    @FXML
    protected void handleCOMPort(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Impossible d'initialiser LibUSB", ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    protected void handleMenuClose(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }
}
