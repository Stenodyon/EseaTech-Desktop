package eseatech;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Utils {

    public static final int ArduinoVendorId = 0x2a03;
    public static final int ArduinoProductId = 0x42;

    public static void fail(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        Platform.runLater(() -> {
            alert.getDialogPane().getScene().getWindow().sizeToScene();
        });
        alert.showAndWait();

        Platform.exit();
        System.exit(0);
    }
}
