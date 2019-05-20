package eseatech;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main_view.fxml"));
        primaryStage.setTitle("EseaTech Monitoring");
        primaryStage.setScene(new Scene(root, 640, 480));
        primaryStage.show();
        primaryStage.setMaximized(true);
    }

    public static void main(String[] args) {
        launch(args);

        if (Controller.currentSerialPort != null) {
            Controller.currentSerialPort.closePort();
        }
    }
}
