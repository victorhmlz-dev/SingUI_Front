package controllers;

import java.io.IOException;
import java.net.URL;
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

/**
 * Controller for home.fxml (dashboard).
 */
public class Home {

    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton navHome;
    @FXML private ComboBox<String> periodCombo;
    @FXML private AreaChart<String, Number> activityChart;
    @FXML private NumberAxis yAxis;

    @FXML private MenuButton userMenu;
    @FXML private Label avatarInitials;

    private String currentUserEmail;

    @FXML
    private void initialize() {
        navGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                oldT.setSelected(true);
            }
        });

        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number n) {
                return n.intValue() == 0 ? "0" : (n.intValue() / 1000) + "k";
            }

            @Override
            public Number fromString(String s) {
                return null;
            }
        });

        loadChart();
    }

    public void setUserData(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return;
        }
        this.currentUserEmail = fullName;

        if (userMenu != null) {
            userMenu.setText(fullName);
        }

        if (avatarInitials != null) {
            String initials = extractInitials(fullName);
            avatarInitials.setText(initials);
        }
    }

    private String extractInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private void loadChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] days = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        int[] values = {950, 1900, 2350, 2450, 1950, 2550, 2950};
        for (int i = 0; i < days.length; i++) {
            series.getData().add(new XYChart.Data<>(days[i], values[i]));
        }
        activityChart.getData().setAll(series);

        XYChart.Data<String, Number> last = series.getData().get(series.getData().size() - 1);
        if (last.getNode() != null) {
            last.getNode().getStyleClass().add("last-point");
        }
    }

    @FXML 
    private void handleChangePassword(ActionEvent event) {
        // Intenta cargar la vista de cambio de contraseña (ajusta el nombre del .fxml si varía en tu proyecto)
        String fxmlPath = "/views/res/models/change_password.fxml";
        URL resource = getClass().getResource(fxmlPath);

        if (resource == null) {
            fxmlPath = "/src/views/res/models/change_password.fxml";
            resource = getClass().getResource(fxmlPath);
        }

        if (resource == null) {
            showErrorAlert("Error de Navegación", "No se encontró el archivo FXML para cambiar la contraseña en /models/.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            // Opcional: Si el controlador de cambio de contraseña requiere recibir el email/usuario actual:
            Object controller = loader.getController();
            if (controller != null && currentUserEmail != null) {
                try {
                    controller.getClass().getMethod("setUserData", String.class).invoke(controller, currentUserEmail);
                } catch (Exception ignored) {
                    // Si el controlador de cambio de contraseña no implementa setUserData, continúa sin error
                }
            }

            Stage stage = (Stage) userMenu.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Ocurrió un error al cargar la vista de cambio de contraseña.");
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (userMenu != null && userMenu.getScene() != null) {
            alert.initOwner(userMenu.getScene().getWindow());
        }
        alert.showAndWait();
    }

    @FXML private void handleNavigation(ActionEvent event) { }
    @FXML private void handleDiscover(ActionEvent event) { }
    @FXML private void handleNotifications(ActionEvent event) { }
    @FXML private void handleProfile(ActionEvent event) { }
    @FXML private void handleLogout(ActionEvent event) { }
    @FXML private void handleDemo(ActionEvent event) { }
    @FXML private void handleLearnMore(ActionEvent event) { }
    @FXML private void handleKnowMore(ActionEvent event) { }
    @FXML private void handleFeatureReservations(MouseEvent event) { }
    @FXML private void handleFeatureOrders(MouseEvent event) { }
    @FXML private void handleFeatureReports(MouseEvent event) { }
    @FXML private void handleFeatureAll(MouseEvent event) { }
}