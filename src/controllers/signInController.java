package controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author javi
 */
public class signInController {

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private TextField emailField;

    @FXML
    private Button signInButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink signUpLink;

    private String email;

    @FXML
    public void initialize() {
        clearError();

        // Sincronizar el texto de la contraseña oculta y la visible
        passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());

        // Listeners para limpiar errores al escribir (sin lambdas)
        emailField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                clearError();
            }
        });

        passwordField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                clearError();
            }
        });

        passwordTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                clearError();
            }
        });

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                emailField.requestFocus();
            }
        });
    }

    @FXML
    public void handleTogglePassword(ActionEvent event) {
        if (passwordField.isVisible()) {
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordTextField.requestFocus();
            passwordTextField.positionCaret(passwordTextField.getText().length());
        } else {
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    @FXML
    public void handleSignIn(ActionEvent event) {
        clearError();
        this.email = emailField.getText();
        String password = passwordField.getText(); // Tiene el mismo texto que passwordTextField

        if (this.email == null || this.email.trim().isEmpty()) {
            showError("Por favor, introduce tu email.");
            emailField.requestFocus();
            return;
        }

        if (!this.email.contains("@") || !this.email.contains(".")) {
            showError("El email no tiene un formato válido.");
            emailField.requestFocus();
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            showError("Por favor, introduce tu contraseña.");
            if (passwordField.isVisible()) {
                passwordField.requestFocus();
            } else {
                passwordTextField.requestFocus();
            }
            return;
        }

        navigateTo("/models/home.fxml");
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recuperar contraseña");
        alert.setHeaderText(null);
        alert.setContentText("Contacta con el administrador para restablecer tu contraseña.");
        alert.initOwner(signInButton.getScene().getWindow());
        alert.showAndWait();
    }

    @FXML
    public void handleSignUp(ActionEvent event) {
        navigateTo("/models/sign_up.fxml");
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                showError("No se encontró el archivo FXML en: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) signInButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al cargar la pantalla: " + fxmlPath);
        }
    }
}