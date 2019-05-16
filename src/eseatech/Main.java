package eseatech;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.usb4java.LibUsb;

public class Main extends Application {

    private void initializeLibUsb() {
        int result = LibUsb.init(null);
        if (result != LibUsb.SUCCESS) {
            Utils.fail(String.format(
                    "Impossible d'initialiser LibUSB: %s",
                    LibUsb.errorName(result)));
        }

        if (!LibUsb.hasCapability(LibUsb.CAP_HAS_HOTPLUG)) {
            Utils.fail("LibUSB n'a pas la fonctionnalité HotPlug");
        }

        new Thread(() -> {
            try {
                while (true) {
                    LibUsb.handleEvents(null);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initializeLibUsb();

        Parent root = FXMLLoader.load(getClass().getResource("main_view.fxml"));
        primaryStage.setTitle("EseaTech Monitoring");
        primaryStage.setScene(new Scene(root, 640, 480));
        primaryStage.show();
        primaryStage.setMaximized(true);
    }


    public static void main(String[] args) {
        launch(args);
        LibUsb.exit(null);
    }
}
