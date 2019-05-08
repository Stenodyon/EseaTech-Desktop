package eseatech;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;

public class Utils {

    public static final int ArduinoVendorId = 0x2a03;
    public static final int ArduinoProductId = 0x42;

    public static Device findArduino() {
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(null, list);
        if (result < 0)
            fail("LibUSB: impossible d'obtenir la liste des périphériques USB");

        try {
            for (Device device : list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS)
                    continue;

                if (descriptor.idVendor() == ArduinoVendorId && descriptor.idProduct() == ArduinoProductId)
                    return device;
            }
        } finally {
            LibUsb.freeDeviceList(list, true);
        }
        return null;
    }

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
