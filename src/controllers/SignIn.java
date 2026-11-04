/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;
 
import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import models.User;
 
/**
 *
 * @author javi
 */
public class SignIn {
    @FXML
    private PasswordField passwordField;
    
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
        // Ocultar mensaje de error inicial si existe
        clearError();
 
        // Listener para limpiar error al escribir en el email (clase anónima)
        emailField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                clearError();
            }
        });
 
        // Listener para limpiar error al escribir en la contraseña (clase anónima)
        passwordField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                clearError();
            }
        });
 
        // Dar foco inicial al campo de email
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                emailField.requestFocus();
            }
        });
    }
 
    @FXML
    public void handleSignIn(ActionEvent event) {
        clearError();
        this.email = emailField.getText();
        String password = passwordField.getText();
 
        // Validaciones básicas inline
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
            passwordField.requestFocus();
            return;
        }
 
        // Si la validaciones pasan, navega a la pantalla principal
        // Cambia "/home.fxml" por la ruta de tu vista principal
        navigateTo("/home.fxml");
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
    public void goToSignUp(ActionEvent event) {
        // Cambia "/sign_up.fxml" por la ruta de tu vista de registro
        navigateTo("/views/res/models/sign_up.fxml");
    }
 
    // Muestra el mensaje de error en la interfaz
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }
 
    // Oculta el mensaje de error
    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }
 
    // Cambia de ventana usando métodos propios de JavaFX
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) signInButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al cargar la pantalla: " + fxmlPath);
        }
    }
}