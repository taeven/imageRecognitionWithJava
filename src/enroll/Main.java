package enroll;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Train the recognizer");
        primaryStage.setScene(new Scene(root, 600, 360));
        primaryStage.show();
        primaryStage.setMaxHeight(360);
        primaryStage.setMaxWidth(600);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
