package eseatech;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.usb4java.LibUsb;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        int result = LibUsb.init(null);
        if (result != LibUsb.SUCCESS) {
            Utils.fail("Impossible d'initialiser LibUSB");
        }

        if (!LibUsb.hasCapability(LibUsb.CAP_HAS_HOTPLUG)) {
            Utils.fail("LibUSB n'a pas la fonctionnalité HotPlug");
        }

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
