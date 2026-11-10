package controllers;

import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import singui_front.model.User;
import singui_front.session.Session;
import singui_front.util.ComingSoonPopup;

/**
 * @author Victor
 * 
 * Controlador del panel principal (home.fxml).
 *
 * Los datos del usuario se leen de {@link Session}.
 */
public class Home {

    private static final String SIGN_IN_FXML         = "/views/res/models/sign_in.fxml";
    private static final String CHANGE_PASSWORD_FXML = "/views/res/models/change_password.fxml";

    // ---------- Nodos del FXML ----------

    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton navHome;
    @FXML private ComboBox<String> periodCombo;
    @FXML private AreaChart<String, Number> activityChart;
    @FXML private NumberAxis yAxis;

    @FXML private MenuButton userMenu;
    @FXML private Label avatarInitials;

    // ---------- Inicialización ----------

    @FXML
    private void initialize() {
        // Sin sesión no se puede estar en Home: volver a Sign In.
        // runLater porque durante initialize() la escena todavía no existe.
        if (!Session.isLoggedIn()) {
            Platform.runLater(() -> navigateTo(SIGN_IN_FXML));
            return;
        }

        showUser(Session.getCurrentUser());

        // En la barra lateral siempre hay una opción seleccionada.
        navGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
            }
        });

        // Eje Y en miles: 1000 -> "1k".
        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number n) {
                return n.intValue() == 0 ? "0" : (n.intValue() / 1000) + "k";
            }

            @Override
            public Number fromString(String s) {
                return null;   // el eje no se edita
            }
        });

        loadChart();
    }

    // ---------- Usuario ----------

    /** Pinta nombre completo e iniciales en el menú de usuario. */
    private void showUser(User user) {
        String fullName = fullNameOf(user);
        userMenu.setText(fullName);
        avatarInitials.setText(initialsOf(user, fullName));
    }

    /** "Nombre Apellido"; si faltan los dos, el email. */
    private String fullNameOf(User user) {
        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last  = user.getLastName()  == null ? "" : user.getLastName().trim();
        String full  = (first + " " + last).trim();
        return full.isEmpty() ? user.getEmail() : full;
    }

    /** Iniciales de nombre y apellido ("John Smith" -> "JS"); si no hay, las 2 primeras letras. */
    private String initialsOf(User user, String fallback) {
        String first = user.getFirstName();
        String last  = user.getLastName();
        if (first != null && !first.isBlank() && last != null && !last.isBlank()) {
            return (first.trim().substring(0, 1) + last.trim().substring(0, 1)).toUpperCase();
        }
        return fallback.substring(0, Math.min(2, fallback.length())).toUpperCase();
    }

    // ---------- Gráfica (datos de ejemplo) ----------

    private void loadChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        int[] values  = {950, 1900, 2350, 2450, 1950, 2550, 2950};
        for (int i = 0; i < days.length; i++) {
            series.getData().add(new XYChart.Data<>(days[i], values[i]));
        }
        activityChart.getData().setAll(series);

        // Resaltar el último punto de la serie.
        XYChart.Data<String, Number> last = series.getData().get(series.getData().size() - 1);
        if (last.getNode() != null) {
            last.getNode().getStyleClass().add("last-point");
        }
    }

    // ---------- Menú de usuario ----------

    /** Abre Change password. ChangePassword obtiene el usuario de Session. */
    @FXML
    private void handleChangePassword(ActionEvent event) {
        navigateTo(CHANGE_PASSWORD_FXML);
    }

    /** Cierra la sesión y vuelve a Sign In. */
    @FXML
    private void handleLogout(ActionEvent event) {
        Session.close();
        navigateTo(SIGN_IN_FXML);
    }

    // ---------- Navegación ----------

    /**
     * Sustituye la escena actual por la del FXML indicado.
     * El Stage se obtiene desde userMenu porque un MenuItem no es un Node.
     */
    private void navigateTo(String fxmlPath) {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            showErrorAlert("Navigation error", "View not found: " + fxmlPath);
            return;
        }
        try {
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) userMenu.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not load the view: " + fxmlPath);
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (userMenu.getScene() != null) {
            alert.initOwner(userMenu.getScene().getWindow());
        }
        alert.showAndWait();
    }

    // ---------- Acciones pendientes de implementar(Será SCHINCHAN)----------

    @FXML private void handleNavigation(ActionEvent event)          { comingSoon(); }
    @FXML private void handleDiscover(ActionEvent event)            { comingSoon(); }
    @FXML private void handleNotifications(ActionEvent event)       { comingSoon(); }
    @FXML private void handleProfile(ActionEvent event)             { comingSoon(); }
    @FXML private void handleDemo(ActionEvent event)                { comingSoon(); }
    @FXML private void handleLearnMore(ActionEvent event)           { comingSoon(); }
    @FXML private void handleKnowMore(ActionEvent event)            { comingSoon(); }
    @FXML private void handleFeatureReservations(MouseEvent event)  { comingSoon(); }
    @FXML private void handleFeatureOrders(MouseEvent event)        { comingSoon(); }
    @FXML private void handleFeatureReports(MouseEvent event)       { comingSoon(); }
    @FXML private void handleFeatureAll(MouseEvent event)           { comingSoon(); }

    private void comingSoon() {
        ComingSoonPopup.show(userMenu);
    }
}