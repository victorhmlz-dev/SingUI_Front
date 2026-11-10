package controllers;

import SignUI_Front.client.UserRESTClient;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.GenericType;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import singui_front.model.User;
import singui_front.session.Session;

/**
 * @author javi
 * 
 * Controlador de la ventana de inicio de sesión (sign_in.fxml).
 *
 * Flujo:
 *   1. Valida email y contraseña en el cliente (SI-05, SI-06, SI-07).
 *   2. Pide el usuario al backend por email, en segundo plano (SI-04).
 *   3. Si existe y la contraseña coincide, lo guarda en {@link Session}
 *      y navega a Home.
 *
 */
public class SignIn {

    /** Formato de email: usuario@dominio.ext (extensión de al menos 2 letras). */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[A-Za-z]{2,}$");

    private static final String HOME_FXML    = "/views/res/models/home.fxml";
    private static final String SIGN_UP_FXML = "/views/res/models/sign_up.fxml";

    // ---------- Nodos del FXML ----------

    @FXML private HBox emailContainer;
    @FXML private TextField emailField;

    @FXML private HBox passwordContainer;
    @FXML private PasswordField passwordField;        // contraseña oculta
    @FXML private TextField passwordVisibleField;     // contraseña visible
    @FXML private ToggleButton passwordToggle;        // botón del ojo

    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Label errorLabel;
    @FXML private Button signInButton;
    @FXML private Hyperlink signUpLink;

    // ---------- Inicialización ----------

    @FXML
    public void initialize() {
        clearError();

        // Mostrar/ocultar contraseña: mismo modelo que SignUp y ChangePassword.
        linkPasswordToggle(passwordField, passwordVisibleField, passwordToggle);

        // Al escribir, se oculta el error y se quitan los bordes rojos (SI-03).
        // Basta con escuchar passwordField: comparte el texto con passwordVisibleField.
        emailField.textProperty().addListener((obs, oldValue, newValue) -> clearError());
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> clearError());

        // Foco inicial en el email (SI-02). runLater: la escena aún no está montada.
        Platform.runLater(() -> emailField.requestFocus());
    }

    /**
     * Sincroniza PasswordField y TextField y alterna cuál se ve según el ToggleButton.
     * Idéntico al de SignUp para que las tres ventanas se comporten igual.
     */
    private void linkPasswordToggle(PasswordField hidden, TextField visible, ToggleButton toggle) {
        // Ambos campos comparten el mismo texto.
        visible.textProperty().bindBidirectional(hidden.textProperty());

        // Solo uno visible a la vez; managed sigue a visible para que no ocupe hueco.
        visible.visibleProperty().bind(toggle.selectedProperty());
        visible.managedProperty().bind(visible.visibleProperty());

        hidden.visibleProperty().bind(toggle.selectedProperty().not());
        hidden.managedProperty().bind(hidden.visibleProperty());

        // Al alternar, el foco y el cursor pasan al campo que queda visible.
        toggle.selectedProperty().addListener((obs, wasShown, isShown) -> {
            TextField active = isShown ? visible : hidden;
            active.requestFocus();
            active.positionCaret(active.getText().length());
        });
    }

    // ---------- Acciones ----------

    /**
     * Valida los campos y autentica contra el backend REST.
     */
    @FXML
    public void handleSignIn(ActionEvent event) {
        clearError();

        final String email = emailField.getText() == null
                ? "" : emailField.getText().trim().toLowerCase();
        final String password = passwordField.getText();

        // 1. Email vacío (SI-05)
        if (email.isEmpty()) {
            failOn(emailContainer, "Please enter your email.");
            emailField.requestFocus();
            return;
        }

        // 2. Formato de email (SI-06)
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            failOn(emailContainer, "The email format is not valid.");
            emailField.requestFocus();
            return;
        }

        // 3. Contraseña vacía (SI-07)
        if (password == null || password.isEmpty()) {
            failOn(passwordContainer, "Please enter your password.");
            focusPassword();
            return;
        }

        // 4. Autenticación en segundo plano para no congelar la ventana (SI-04).
        //    Devuelve el User si las credenciales son correctas, o null si no.
        Task<User> task = new Task<>() {
            @Override
            protected User call() {
                UserRESTClient client = new UserRESTClient();
                try {
                    // El endpoint email/{email} devuelve una lista (<users>).
                    List<User> users = client.findByEmail_XML(
                            new GenericType<List<User>>() { }, email);

                    if (users == null || users.isEmpty()) {
                        return null;                       // el email no existe
                    }
                    User user = users.get(0);
                    return password.equals(user.getPassword()) ? user : null;

                } catch (NotFoundException e) {
                    return null;                           // 404: tratado como credenciales incorrectas
                } finally {
                    client.close();                        // liberar la conexión siempre
                }
            }
        };

        // El botón queda desactivado mientras dura la petición (SI-04).
        signInButton.disableProperty().bind(task.runningProperty());

        task.setOnSucceeded(e -> {
            User user = task.getValue();
            if (user != null) {
                Session.start(user);                       // a partir de aquí, toda la app lo conoce
                navigateToHome();
            } else {
                passwordField.clear();
                showError("Incorrect email or password.");
                focusPassword();
            }
        });

        // Error de red o del servidor (SI-10).
        task.setOnFailed(e -> {
            passwordField.clear();
            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
            showError("An unexpected error occurred. Please try again later.");
        });

        Thread thread = new Thread(task, "rest-auth-thread");
        thread.setDaemon(true);                            // no impide cerrar la aplicación
        thread.start();
    }

    /** Diálogo informativo de recuperación de contraseña (SI-15). */
    @FXML
    public void handleForgotPassword(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forgot password");
        alert.setHeaderText(null);
        alert.setContentText("Contact the administrator to reset your password.");
        alert.initOwner(signInButton.getScene().getWindow());
        alert.showAndWait();
    }

    /** Navega a la pantalla de registro (SI-16). */
    @FXML
    public void handleSignUp(ActionEvent event) {
        navigateTo(SIGN_UP_FXML);
    }

    // ---------- Navegación ----------

    /**
     * Abre Home. El usuario ya está en Session; setUserData se mantiene
     * temporalmente hasta que Home lea los datos de Session directamente.
     */
        /** Abre Home. Los datos del usuario ya están en Session. */
    private void navigateToHome() {
        navigateTo(HOME_FXML);
    }

    private void navigateTo(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                showError("FXML not found: " + fxmlPath);
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) signInButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading screen: " + fxmlPath);
        }
    }

    // ---------- Utilidades de interfaz ----------

    /** Error de validación: borra la contraseña, muestra el mensaje y marca el campo en rojo. */
    private void failOn(HBox container, String message) {
        passwordField.clear();
        showError(message);
        highlight(container);
    }

    /** Da el foco al campo de contraseña que esté visible en ese momento. */
    private void focusPassword() {
        TextField active = passwordToggle.isSelected() ? passwordVisibleField : passwordField;
        active.requestFocus();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        emailContainer.getStyleClass().remove("error");
        passwordContainer.getStyleClass().remove("error");
    }

    private void highlight(HBox container) {
        if (!container.getStyleClass().contains("error")) {
            container.getStyleClass().add("error");
        }
    }
    
     /**
     * Usado por SignUp tras un registro correcto: deja el email escrito
     * y pone el foco en la contraseña.
     */
    public void prefillEmail(String email) {
        emailField.setText(email);
        Platform.runLater(() -> focusPassword()); 
    }
}