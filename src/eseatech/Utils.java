package eseatech;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.usb4java.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Utils {

    public static final int ArduinoVendorId = 0x2a03;
    public static final int ArduinoProductId = 0x42;
    private static DeviceHandle arduinoHandle = null;

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

    public static int openArduino() {
        if (arduinoHandle != null)
            return 0;
        Device arduino = findArduino();
        if (arduino == null)
            return 0;
        arduinoHandle = new DeviceHandle();
        int result = LibUsb.open(arduino, arduinoHandle);
        if (result != 0)
            arduinoHandle = null;
        return result;
    }

    public static boolean isArduinoOpen() {
        return arduinoHandle != null;
    }

    public static void closeArduino() {
        if (arduinoHandle == null)
            return;
        LibUsb.close(arduinoHandle);
        arduinoHandle = null;
    }

    public static int registerUSBCallback(HotplugCallback callback) {
        HotplugCallbackHandle handle = new HotplugCallbackHandle();
        return LibUsb.hotplugRegisterCallback(
                null,
                LibUsb.HOTPLUG_EVENT_DEVICE_ARRIVED | LibUsb.HOTPLUG_EVENT_DEVICE_LEFT,
                LibUsb.HOTPLUG_NO_FLAGS,
                ArduinoVendorId,
                ArduinoProductId,
                LibUsb.HOTPLUG_MATCH_ANY,
                callback,
                null,
                handle
        );
    }

    // Assumes arduino is not null
    public static float USBReadFloat() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        IntBuffer transfered = IntBuffer.allocate(1);
        int result = LibUsb.bulkTransfer(arduinoHandle, (byte)0x82, buffer, transfered, 0);
        if (result != 0) {
            System.err.printf("Unable to read from Arduino: %s\n", LibUsb.strError(result));
        }
        if (transfered.get(0) != 4) {
            System.err.printf("Did not read enough bytes: %d\n", transfered.get(0));
        }
        return buffer.getFloat();
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
