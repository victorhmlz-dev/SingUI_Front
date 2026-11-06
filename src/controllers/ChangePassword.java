/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Victor
 */
public class ChangePassword {

    private static final String ERROR_STYLE = "auth-error";
    private static final String SUCCESS_STYLE = "auth-success";
    private static final String VALID_CHARACTERS =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
        "abcdefghijklmnopqrstuvwxyz" +
        "0123456789" +
        "áéíóúÁÉÍÓÚñÑ" +
        "._-@#$%&*!?";

    // UI FIELDS, view elements
    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField currentPasswordVisibleField;

    @FXML
    private TextField newPasswordVisibleField;

    @FXML
    private ToggleButton currentPasswordToggle;

    @FXML
    private ToggleButton newPasswordToggle;

    @FXML
    private Label messageLabel;

    @FXML
    private Label strengthLabel;
    
    @FXML
    private Button backButton;

    private String email;

    @FXML
    public void initialize() {
        // Arranca la pantalla change_password
        clearMessage();
        strengthLabel.setText("");
        setupShowPassword();
        
        // Fortaleza del password en tiempo real a medida que se escribe
        newPasswordField.textProperty().addListener((obs, oldV, newV) -> updatePasswordStrength());
        
        // Ocultar el mensaje mientras se edita cualquier campo
        currentPasswordField.textProperty().addListener((obs, o, n) -> clearMessage());
        newPasswordField.textProperty().addListener((obs, o, n) -> clearMessage());
        confirmPasswordField.textProperty().addListener((obs, o, n) -> clearMessage());
    }

    private void setupShowPassword(){
        // Sincroniza password fiel con text field, y alterna la vista al presionar el botón "show" (el ojo)
        currentPasswordField.textProperty().bindBidirectional(currentPasswordVisibleField.textProperty());
        newPasswordField.textProperty().bindBidirectional(newPasswordVisibleField.textProperty());

        currentPasswordToggle.selectedProperty().addListener((obs, o, show) ->
        togglePassword(currentPasswordField, currentPasswordVisibleField, show));

        newPasswordToggle.selectedProperty().addListener((obs, o, show) ->
        togglePassword(newPasswordField, newPasswordVisibleField, show));

    }

    private void togglePassword(PasswordField hidden, TextField visible, boolean show) {
        boolean hadFocus = hidden.isFocused() || visible.isFocused();

        visible.setVisible(show);
        visible.setManaged(show);
        hidden.setVisible(!show);
        hidden.setManaged(!show);
        
        // El foco solo se mueve si el campo lo tenía, cabe destacar que PasswordField Hereda de TextField
        if (hadFocus) { 
            TextField shown = show ? visible : hidden; 
            shown.requestFocus();
            shown.positionCaret(shown.getText().length());
        }
    }

    private boolean isValidPassword(String password) {
    // Valida si hay caracteres no permitidos en el password
        if (password == null || password.isEmpty()) {
            return false;
        }

        for (char character : password.toCharArray()) {
            if (VALID_CHARACTERS.indexOf(character) == -1) {
                return false;
            }
        }

        return true;
    }

    private String getPasswordStrength(String password) {
        int upper = 0, lower = 0, digits = 0;
        for (char c : password.toCharArray()) {
            if (c >= 'A' && c <= 'Z') upper++;
            else if (c >= 'a' && c <= 'z') lower++;
            else if (c >= '0' && c <= '9') digits++;
        }
        boolean extra1 = upper > 1 && lower > 1 && digits > 1;
        boolean extra2 = password.matches(".*[._@$*-].*");

        if (extra1 && extra2) return "Strong";
        if (extra1 || extra2) return "Medium";
        return "Weak";
    }


    private void updatePasswordStrength() {
        String pwd = newPasswordField.getText();
        if (pwd == null || pwd.isEmpty()) {
            strengthLabel.setText("");
        } else if (!isValidPassword(pwd)) {
            strengthLabel.setText("The password does not meet the requirements.");
        } else {
            strengthLabel.setText("Strength: " + getPasswordStrength(pwd));
        }
    }

    @FXML
    private void handleChangePassword() {
        String current = currentPasswordField.getText();
        String newPwd  = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (isBlank(current) || isBlank(newPwd) || isBlank(confirm)) {
            showError("All fields are required.");
            return;
        }
        if (!isValidPassword(newPwd)) {
            showError("The password does not meet the minimum requirements.");
            return;
        }
        if (newPwd.equals(current)) {
            showError("The new password must be different from the current one.");
            return;
        }
        if (!newPwd.equals(confirm)) {
            showError("The new passwords do not match.");
            return;
        }
        showSuccess("Validation confirmed. Change password allowed.");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @FXML
    private void handleCancel() {
        currentPasswordToggle.setSelected(false); // primero: así el listener vuelve a ocultar
        newPasswordToggle.setSelected(false);
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        strengthLabel.setText("");
        clearMessage();
    }
    
    @FXML
    private void handleBack() {
        try {
            // 1. Cargar la vista de Home
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/res/models/home.fxml"));
            Parent root = loader.load();

            // 2. Devolverle el email para que siga mostrando al usuario
            Home homeController = loader.getController();
            homeController.setUserData(email);

            // 3. Reemplazar la escena en la misma ventana
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load the home screen.");
        }
    }

    public void setEmail(String email) { this.email = email; }
    public String getEmail()           { return email; }

    public void setUserData(String email) { setEmail(email); } 

    private void showError(String message)   { showMessage(message, ERROR_STYLE); }
    
    private void showSuccess(String message) { showMessage(message, SUCCESS_STYLE); }

    private void showMessage(String message, String styleClass) {
        messageLabel.getStyleClass().removeAll(ERROR_STYLE, SUCCESS_STYLE); // nunca los dos a la vez
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }
}
