package singui_front;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class SingUI_Front extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        changeView("sign_in");

        primaryStage.setTitle("Misenplace - Smart Restaurant");
        primaryStage.getIcons().add(
                new Image(SingUI_Front.class.getResourceAsStream("/views/res/images/icon.png")));
        primaryStage.show();
    }

    public static void changeView(String view) throws Exception {
        //FIXME: Location is wrong: bad path
        Parent root = FXMLLoader.load(
            SingUI_Front.class.getResource(
                "/views/res/models/" + view + ".fxml"
            )
        );

        primaryStage.setScene(new Scene(root));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
