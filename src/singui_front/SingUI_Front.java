package singui_front;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SingUI_Front extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        changeView("sign_in");

        primaryStage.setTitle("SingUI");
        primaryStage.show();
    }

    public static void changeView(String view) throws Exception {

        Parent root = FXMLLoader.load(
            SingUI_Front.class.getResource(
                "../views/res/models/" + view + ".fxml"
            )
        );

        primaryStage.setScene(new Scene(root));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
