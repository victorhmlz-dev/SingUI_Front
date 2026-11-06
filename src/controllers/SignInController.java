package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;
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
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


/**
 *
 * @author javi
 */
public class SignInController {

    // Patrón para validar el formato del email (usuario@dominio.extension de al menos 2 letras)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[A-Za-z]{2,}$");

    @FXML
    private HBox emailContainer;

    @FXML
    private TextField emailField;

    @FXML
    private HBox passwordContainer;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Label errorLabel;

    @FXML
    private Button signInButton;

    @FXML
    private Hyperlink signUpLink;

    @FXML
    public void initialize() {
        clearError();

        // Sincronizar el texto entre la contraseña oculta y la visible
        passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());

        // Listeners para ocultar el mensaje de error y quitar los bordes rojos al modificar texto (SI-03)
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

        // Foco inicial en el campo email (SI-02)
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                emailField.requestFocus();
            }
        });
    }

    /**
     * Alterna la visibilidad entre el PasswordField y el TextField normal.
     */
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

    /**
     * Valida las entradas e inicia sesión conectándose al backend REST.
     */
    @FXML
    public void handleSignIn(ActionEvent event) {
        clearError();
        final String rawEmail = emailField.getText();
        final String password = passwordField.getText();

        // 1. Validar Email vacío (SI-05)
        if (rawEmail == null || rawEmail.trim().isEmpty()) {
            passwordField.clear();
            showError("Please enter your email.");
            highlightContainer(emailContainer);
            emailField.requestFocus();
            return;
        }

        // 2. Validar formato de Email (SI-06)
        if (!EMAIL_PATTERN.matcher(rawEmail.trim()).matches()) {
            passwordField.clear();
            showError("The email format is not valid.");
            highlightContainer(emailContainer);
            emailField.requestFocus();
            return;
        }

        // 3. Validar Contraseña vacía (SI-07)
        if (password == null || password.isEmpty()) {
            passwordField.clear();
            showError("Please enter your password.");
            highlightContainer(passwordContainer);
            requestFocusOnVisiblePassword();
            return;
        }

        // 4. Ejecución en segundo plano para autenticar contra la API REST (SI-04)
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                String normalizedEmail = rawEmail.trim().toLowerCase();

                try {
                    // Instancia del cliente REST generado por NetBeans
                    SignUI_Front.client.UserRESTClient restClient = new SignUI_Front.client.UserRESTClient();

                    // Consultar usuario en el backend
                    String responseJson = restClient.findByEmail_JSON(String.class, normalizedEmail);

                    if (responseJson == null || responseJson.trim().isEmpty() || responseJson.equalsIgnoreCase("null")) {
                        return false;
                    }

                    // Verifica si la respuesta contiene el email y la clave introducida
                    return responseJson.contains(normalizedEmail) && responseJson.contains(password);

                } catch (Exception e) {
                    // Si el servidor devuelve 404 Not Found (usuario no existe), lo tratamos como credenciales incorrectas
                    String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                    if (msg.contains("404") || msg.contains("not found")) {
                        return false;
                    }
                    throw e; // Lanzar hacia setOnFailed para errores genéricos de red/servidor
                }
            }
        };

        // Bloquear el botón durante la petición (SI-04)
        signInButton.disableProperty().bind(task.runningProperty());

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Boolean success = task.getValue();
                if (Boolean.TRUE.equals(success)) {
                    // Credenciales correctas -> Navegar a la pantalla principal (SI-09)
                    navigateTo("/views/res/models/home.fxml");
                } else {
                    // Credenciales incorrectas (SI-08): No resalta bordes en rojo
                    passwordField.clear();
                    showError("Incorrect email or password.");
                    requestFocusOnVisiblePassword();
                }
            }
        });

        task.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                // Error inesperado (SI-10)
                passwordField.clear();
                if (task.getException() != null) {
                    task.getException().printStackTrace();
                }
                showError("An unexpected error occurred. Please try again later.");
            }
        });

        Thread thread = new Thread(task, "rest-auth-thread");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Muestra diálogo informativo de recuperación (SI-15).
     */
    @FXML
    public void handleForgotPassword(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recuperar contraseña");
        alert.setHeaderText(null);
        alert.setContentText("Contact the administrator to reset your password.");
        alert.initOwner(signInButton.getScene().getWindow());
        alert.showAndWait();
    }

    /**
     * Navega a la pantalla de registro (SI-16).
     */
    @FXML
    public void handleSignUp(ActionEvent event) {
        navigateTo("/views/res/models/sign_up.fxml");
    }

    private void requestFocusOnVisiblePassword() {
        if (passwordField.isVisible()) {
            passwordField.requestFocus();
        } else {
            passwordTextField.requestFocus();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setManaged(true);
            errorLabel.setVisible(true);
        }
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setManaged(false);
            errorLabel.setVisible(false);
        }
        if (emailContainer != null) {
            emailContainer.getStyleClass().remove("error");
        }
        if (passwordContainer != null) {
            passwordContainer.getStyleClass().remove("error");
        }
    }

    private void highlightContainer(HBox container) {
        if (container != null && !container.getStyleClass().contains("error")) {
            container.getStyleClass().add("error");
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
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