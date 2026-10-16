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

/**
 *
 * @author Victor
 */
public class ChangePassword {

    // UI FIELDS
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

    private String email;

    @FXML
    public void initialize() {
        clearError();
        strengthLabel.setText("");

        // Validar mientras el usuario escribe.
        newPasswordField.textProperty().addListener(
            (observable, oldValue, newValue) -> {
                updatePasswordStrength(newValue);
            }
        );

        // Botón del ojo para mostrar/ocultar la contraseña.
        setupShowPassword(currentPasswordField,
                currentPasswordVisibleField, currentPasswordToggle);
        setupShowPassword(newPasswordField,
                newPasswordVisibleField, newPasswordToggle);
    }

    /**
     * Sincroniza el PasswordField con un TextField visible y alterna
     * entre ambos cuando se pulsa el botón del ojo.
     */
    private void setupShowPassword(PasswordField hiddenField,
            TextField visibleField, ToggleButton toggle) {

        visibleField.textProperty()
                .bindBidirectional(hiddenField.textProperty());

        toggle.selectedProperty().addListener(
            (observable, oldValue, show) -> {
                TextField from = show ? hiddenField : visibleField;
                TextField to = show ? visibleField : hiddenField;

                visibleField.setVisible(show);
                visibleField.setManaged(show);
                hiddenField.setVisible(!show);
                hiddenField.setManaged(!show);

                if (from.isFocused()) {
                    to.requestFocus();
                    to.positionCaret(to.getText().length());
                }
            }
        );
    }

    /**
     * Comprueba los requisitos mínimos de contraseña
     * y los caracteres permitidos.
     */
    private boolean isValidPassword(String password) {

        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*[0-9].*");

        // Solo se permiten letras, números y estos caracteres especiales.
        boolean allowedCharacters =
                password.matches("[a-zA-Z0-9._@$*-]+");

        return hasUppercase
                && hasLowercase
                && hasNumber
                && allowedCharacters;
    }

    /**
     * Calcula la fortaleza: Weak, Medium o Strong.
     */
    private String getPasswordStrength(String password) {

        if (!isValidPassword(password)) {
            return "Invalid";
        }

        long uppercaseCount = password.chars()
                .filter(Character::isUpperCase)
                .count();

        long lowercaseCount = password.chars()
                .filter(Character::isLowerCase)
                .count();

        long numberCount = password.chars()
                .filter(Character::isDigit)
                .count();

        boolean additionalRequirement1 =
                uppercaseCount > 1
                && lowercaseCount > 1
                && numberCount > 1;

        boolean additionalRequirement2 =
                password.matches(".*[._@$-].*");

        if (additionalRequirement1 && additionalRequirement2) {
            return "Strong";
        }

        if (additionalRequirement1 || additionalRequirement2) {
            return "Medium";
        }

        return "Weak";
    }

    /**
     * Actualiza la fortaleza en tiempo real.
     */
    private void updatePasswordStrength(String password) {

        String strength = getPasswordStrength(password);

        switch (strength) {
            case "Weak" ->
                strengthLabel.setText("Strength: Weak");

            case "Medium" ->
                strengthLabel.setText("Strength: Medium");

            case "Strong" ->
                strengthLabel.setText("Strength: Strong");

            default ->
                strengthLabel.setText(
                    password == null || password.isEmpty()
                        ? ""
                        : "La contraseña no cumple los requisitos."
                );
        }
    }

    /**
     * Acción del botón Change Password.
     */
    @FXML
    private void handleChangePassword() {

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmation = confirmPasswordField.getText();

        if (currentPassword.isBlank()
                || newPassword.isBlank()
                || confirmation.isBlank()) {

            showError("Todos los campos son obligatorios.");
            return;
        }

        if (!isValidPassword(newPassword)) {
            showError("La contraseña no cumple los requisitos mínimos.");
            return;
        }

        if (currentPassword.equals(newPassword)) {
            showError("La nueva contraseña debe ser diferente de la actual.");
            return;
        }

        if (!newPassword.equals(confirmation)) {
            showError("Las contraseñas nuevas no coinciden.");
            return;
        }

        // Pendiente: comprobar la contraseña actual en el backend
        // y guardar el hash de la nueva contraseña.

        showError("Validación correcta. Ya puedes actualizar la contraseña.");
    }

    @FXML
    private void handleCancel() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        clearError();
        strengthLabel.setText("");
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
    
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setManaged(true);
        messageLabel.setVisible(true);
    }

    private void clearError() {
        messageLabel.setText("");
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
    }
}