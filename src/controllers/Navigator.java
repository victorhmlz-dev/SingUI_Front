package controllers;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

public final class Navigator {

    // Carpeta de recursos donde están los .fxml (ajústala a tu proyecto)
    private static final String VIEWS_PATH = "/views/";

    private Navigator() {}

    /** Carga la vista, la pone en la escena actual y devuelve su controlador. */
    public static <T> T goTo(Node source, String fxmlName) throws IOException {
        URL url = Navigator.class.getResource(VIEWS_PATH + fxmlName);
        if (url == null) {
            throw new IOException("No se encuentra la vista: " + VIEWS_PATH + fxmlName);
        }

        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        source.getScene().setRoot(root);
        return loader.getController();
    }
}