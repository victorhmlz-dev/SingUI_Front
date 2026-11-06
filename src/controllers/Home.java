package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
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

    @FXML
    private void initialize() {
        // Keep one nav item always selected
        navGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                oldT.setSelected(true);
            }
        });

        // Y axis labels: 0, 1k, 2k, 3k
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

    private void loadChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] days = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        int[] values = {950, 1900, 2350, 2450, 1950, 2550, 2950};
        for (int i = 0; i < days.length; i++) {
            series.getData().add(new XYChart.Data<>(days[i], values[i]));
        }
        activityChart.getData().setAll(series);

        // Highlight last point (nodes exist once data is added to the chart)
        XYChart.Data<String, Number> last = series.getData().get(series.getData().size() - 1);
        if (last.getNode() != null) {
            last.getNode().getStyleClass().add("last-point");
        }
    }

    @FXML private void handleNavigation(ActionEvent event) { /* TODO: change view */ }
    @FXML private void handleDiscover(ActionEvent event) { }
    @FXML private void handleNotifications(ActionEvent event) { }
    @FXML private void handleProfile(ActionEvent event) { }
    @FXML private void handleChangePassword(ActionEvent event) { }
    @FXML private void handleLogout(ActionEvent event) { }
    @FXML private void handleDemo(ActionEvent event) { }
    @FXML private void handleLearnMore(ActionEvent event) { }
    @FXML private void handleKnowMore(ActionEvent event) { }
    @FXML private void handleFeatureReservations(MouseEvent event) { }
    @FXML private void handleFeatureOrders(MouseEvent event) { }
    @FXML private void handleFeatureReports(MouseEvent event) { }
    @FXML private void handleFeatureAll(MouseEvent event) { }
}

