/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package singui_front.util;

import java.io.InputStream;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * @author Victor
 *
 * Aviso temporal "Coming soon" para las funciones que aún no están implementadas.
 * Se muestra centrado en la ventana con una imagen, aparece y desaparece solo.
 *
 * Uso desde cualquier controlador:  ComingSoonPopup.show(algunNodoDeLaVista);
 */
public final class ComingSoonPopup {

    private static final String IMAGE_PATH = "/views/res/images/schinchan.png";
    private static final double IMAGE_WIDTH = 180;

    private static final Duration FADE_IN  = Duration.millis(200);
    private static final Duration VISIBLE  = Duration.seconds(2);
    private static final Duration FADE_OUT = Duration.millis(300);

    /** Imagen cargada una sola vez y reutilizada en cada aviso. */
    private static Image image;

    private ComingSoonPopup() { }   // clase de utilidades: no se instancia

    /** Muestra el aviso con el texto por defecto. */
    public static void show(Node anchor) {
        show(anchor, "Coming soon!");
    }

    /**
     * Muestra el aviso centrado en la ventana que contiene a {@code anchor}.
     *
     * @param anchor  cualquier nodo de la pantalla actual (sirve para saber qué ventana usar)
     * @param message texto bajo la imagen
     */
    public static void show(Node anchor, String message) {
        if (anchor == null || anchor.getScene() == null) {
            return;
        }
        Window window = anchor.getScene().getWindow();

        // ---- Contenido: imagen + texto en una tarjeta oscura ----
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: rgba(20, 20, 35, 0.92);"
              + "-fx-background-radius: 18;"
              + "-fx-padding: 22 28 22 28;"
              + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 30, 0.1, 0, 10);");

        Image img = loadImage();
        if (img != null) {
            ImageView view = new ImageView(img);
            view.setFitWidth(IMAGE_WIDTH);
            view.setPreserveRatio(true);
            view.setSmooth(true);
            card.getChildren().add(view);
        }

        Label label = new Label(message);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        card.getChildren().add(label);

        // ---- Popup: flota sobre la ventana sin bloquearla ----
        Popup popup = new Popup();
        popup.getContent().add(card);
        popup.setAutoHide(true);              // se cierra al hacer clic fuera
        card.setOpacity(0);
        card.setOnMouseClicked(e -> popup.hide());   // o al hacer clic sobre él

        popup.show(window);

        // Centrar en la ventana (el tamaño ya se conoce tras show()).
        popup.setX(window.getX() + (window.getWidth()  - popup.getWidth())  / 2);
        popup.setY(window.getY() + (window.getHeight() - popup.getHeight()) / 2);

        // ---- Animación: aparece, espera, desaparece y se cierra ----
        FadeTransition in = new FadeTransition(FADE_IN, card);
        in.setToValue(1);

        PauseTransition wait = new PauseTransition(VISIBLE);

        FadeTransition out = new FadeTransition(FADE_OUT, card);
        out.setToValue(0);

        SequentialTransition sequence = new SequentialTransition(in, wait, out);
        sequence.setOnFinished(e -> popup.hide());
        sequence.play();
    }

    /** Carga la imagen la primera vez; si el archivo no existe, devuelve null. */
    private static Image loadImage() {
        if (image == null) {
            InputStream stream = ComingSoonPopup.class.getResourceAsStream(IMAGE_PATH);
            if (stream != null) {
                image = new Image(stream);
            }
        }
        return image;
    }
}