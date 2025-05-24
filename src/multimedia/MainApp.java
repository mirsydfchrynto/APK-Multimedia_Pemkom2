package multimedia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/multimedia/Player.fxml"));
        Scene scene = new Scene(root, 900, 600);

        // Optional: tambah stylesheet jika ada
        scene.getStylesheets().add(getClass().getResource("/multimedia/styles.css").toExternalForm());

        stage.setTitle("Aesthetic Media Player");
        stage.setScene(scene);
        stage.setMaximized(true); // fullscreen-like maximize window
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
