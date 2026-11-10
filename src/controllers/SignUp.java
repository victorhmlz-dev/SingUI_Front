package controllers;

import SignUI_Front.client.UserRESTClient;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import singui_front.model.User;
import singui_front.util.PasswordPolicy;

/**
 * Controlador de la ventana de registro (sign_up.fxml).
 *
 * Flujo:
 *   1. Valida todos los campos en el cliente (reglas de contraseña en PasswordPolicy).
 *   2. En segundo plano: comprueba que el email no exista y crea el usuario (POST).
 *      El id lo asigna el backend.
 *   3. Si todo va bien, vuelve a Sign In con el email ya escrito.
 *
 * @author teo
 */
public class SignUp {

    /** Mismo formato de email que SignIn. */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[A-Za-z]{2,}$");

    private static final String SIGN_IN_FXML = "/views/res/models/sign_in.fxml";

    // ---------- Nodos del FXML ----------

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;

    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private ToggleButton passwordToggle;

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisibleField;
    @FXML private ToggleButton confirmPasswordToggle;

    @FXML private TextField stateField;
    @FXML private TextField cityField;
    @FXML private TextField streetField;
    @FXML private TextField zipField;

    @FXML private CheckBox termsCheckBox;
    @FXML private Button signUpButton;
    @FXML private Hyperlink signInLink;
    @FXML private Label messageLabel;
    @FXML private Label strengthLabel;

    /** Recuadros (.auth-input) de todos los campos, para quitarles el borde rojo. */
    private final List<Node> inputContainers = new ArrayList<>();

    // ---------- Inicialización ----------

    @FXML
    public void initialize() {
        // Mostrar/ocultar contraseña: mismo modelo que SignIn y ChangePassword.
        linkPasswordToggle(passwordField, passwordVisibleField, passwordToggle);
        linkPasswordToggle(confirmPasswordField, confirmPasswordVisibleField, confirmPasswordToggle);

        // Todos los campos editables: al escribir en cualquiera se limpia el error.
        TextField[] fields = {
            firstNameField, lastNameField, emailField, passwordField, confirmPasswordField,
            stateField, cityField, streetField, zipField
        };
        for (TextField field : fields) {
            inputContainers.add(containerOf(field));
            field.textProperty().addListener((obs, oldValue, newValue) -> clearError());
        }
        termsCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> clearError());

        // Indicador de fortaleza en vivo. También depende del email (regla del nombre).
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> updatePasswordStrength());
        emailField.textProperty().addListener((obs, oldValue, newValue) -> updatePasswordStrength());

        // La etiqueta de fortaleza solo ocupa espacio cuando tiene texto.
        strengthLabel.managedProperty().bind(strengthLabel.textProperty().isNotEmpty());
        strengthLabel.visibleProperty().bind(strengthLabel.textProperty().isNotEmpty());

        clearError();
        strengthLabel.setText("");
        Platform.runLater(() -> firstNameField.requestFocus());
    }

    /** Sincroniza PasswordField y TextField y alterna cuál se ve según el ToggleButton. */
    private void linkPasswordToggle(PasswordField hidden, TextField visible, ToggleButton toggle) {
        visible.textProperty().bindBidirectional(hidden.textProperty());

        visible.visibleProperty().bind(toggle.selectedProperty());
        visible.managedProperty().bind(visible.visibleProperty());

        hidden.visibleProperty().bind(toggle.selectedProperty().not());
        hidden.managedProperty().bind(hidden.visibleProperty());

        toggle.selectedProperty().addListener((obs, wasShown, isShown) -> {
            TextField active = isShown ? visible : hidden;
            active.requestFocus();
            active.positionCaret(active.getText().length());
        });
    }

    // ---------- Acciones ----------

    @FXML
    public void handleSignUp(ActionEvent event) {
        clearError();

        // 1. Campos obligatorios: se marca el primero que esté vacío.
        TextField[] required = {
            firstNameField, lastNameField, emailField, passwordField, confirmPasswordField,
            stateField, cityField, streetField, zipField
        };
        for (TextField field : required) {
            if (field.getText() == null || field.getText().trim().isEmpty()) {
                failOn(field, "All fields are required.");
                return;
            }
        }

        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String email     = emailField.getText().trim().toLowerCase();   // siempre en minúsculas
        String password  = passwordField.getText();
        String confirm   = confirmPasswordField.getText();
        String state     = stateField.getText().trim();
        String city      = cityField.getText().trim();
        String street    = streetField.getText().trim();
        String zip       = zipField.getText().trim();

        // 2. Email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            failOn(emailField, "The email format is not valid.");
            return;
        }

        // 3. Contraseña: se indica exactamente qué requisitos faltan.
        List<String> passwordErrors = PasswordPolicy.validate(password, email);
        if (!passwordErrors.isEmpty()) {
            failOn(passwordField, String.join(" ", passwordErrors));
            return;
        }

        // 4. Confirmación
        if (!password.equals(confirm)) {
            failOn(confirmPasswordField, "Passwords do not match.");
            return;
        }

        // 5. Código postal (en la base de datos es un int)
        if (!zip.matches("\\d{5}")) {
            failOn(zipField, "The zip code must have 5 digits.");
            return;
        }

        // 6. Términos, al final porque no es un campo de texto.
        if (!termsCheckBox.isSelected()) {
            showMessage("You must accept the Terms and Privacy Policy.", true);
            termsCheckBox.requestFocus();
            return;
        }

        // Sin id: lo asigna el backend al crear el usuario.
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);
        user.setState(state);
        user.setCity(city);
        user.setStreet(street);
        user.setZip(Integer.valueOf(zip));

        registerUser(user);
    }

    /** Vuelve a la pantalla de inicio de sesión. */
    @FXML
    public void handleSignIn(ActionEvent event) {
        navigateToSignIn(null);
    }

    // ---------- Registro en el backend ----------

    /**
     * Comprueba que el email no exista (GET) y crea el usuario (POST),
     * fuera del hilo de JavaFX para no congelar la ventana.
     * Devuelve true si se creó y false si el email ya existía.
     */
    private void registerUser(User user) {
        final String email = user.getEmail();

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                UserRESTClient client = new UserRESTClient();
                try {
                    try {
                        List<User> existing = client.findByEmail_XML(
                                new GenericType<List<User>>() { }, email);
                        if (existing != null && !existing.isEmpty()) {
                            return false;                  // email ya registrado
                        }
                    } catch (NotFoundException notFound) {
                        // 404: el email no existe, se puede registrar
                    }
                    client.create_XML(user);
                    return true;
                } finally {
                    client.close();
                }
            }
        };

        // Botón desactivado mientras dura la petición.
        signUpButton.disableProperty().bind(task.runningProperty());

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                navigateToSignIn(email); // registro correcto
            } else {
                failOn(emailField, "An account with this email already exists.");
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            if (ex instanceof ProcessingException) {
                showMessage("Could not connect to the server.", true);
            } else if (ex instanceof WebApplicationException) {
                int status = ((WebApplicationException) ex).getResponse().getStatus();
                showMessage("The server rejected the registration (HTTP " + status + ").", true);
            } else {
                showMessage("An unexpected error occurred. Please try again later.", true);
            }
        });

        Thread thread = new Thread(task, "signup-request");
        thread.setDaemon(true);
        thread.start();
    }

    // ---------- Navegación ----------

    /**
     * Abre Sign In. Si se pasa un email (registro correcto), lo deja escrito
     * y pone el foco en la contraseña.
     */
    private void navigateToSignIn(String email) {
        try {
            URL resource = getClass().getResource(SIGN_IN_FXML);
            if (resource == null) {
                showMessage("FXML not found: " + SIGN_IN_FXML, true);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            if (email != null) {
                SignIn signIn = loader.getController();
                signIn.prefillEmail(email);
            }

            Stage stage = (Stage) signUpButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Error loading the sign in screen.", true);
        }
    }

    // ---------- Indicador de fortaleza ----------

    /** Muestra qué falta o, si la contraseña es válida, su fortaleza. */
    private void updatePasswordStrength() {
        String password = passwordField.getText();
        if (password == null || password.isEmpty()) {
            strengthLabel.setText("");
            return;
        }
        List<String> errors = PasswordPolicy.validate(password, emailField.getText());
        strengthLabel.setText(errors.isEmpty()
                ? "Strength: " + PasswordPolicy.strength(password).label()
                : String.join(" ", errors));
    }

    // ---------- Utilidades de interfaz ----------

    /** Muestra el error, marca el campo en rojo y le da el foco. */
    private void failOn(TextField field, String message) {
        showMessage(message, true);
        Node container = containerOf(field);
        if (container != null && !container.getStyleClass().contains("error")) {
            container.getStyleClass().add("error");
        }
        focus(field);
    }

    /** Da el foco al campo; si es una contraseña mostrada, al campo visible. */
    private void focus(TextField field) {
        if (field == passwordField && passwordToggle.isSelected()) {
            passwordVisibleField.requestFocus();
        } else if (field == confirmPasswordField && confirmPasswordToggle.isSelected()) {
            confirmPasswordVisibleField.requestFocus();
        } else {
            field.requestFocus();
        }
    }

    /** Sube por la jerarquía hasta el HBox con estilo "auth-input" que envuelve el campo. */
    private Node containerOf(Node node) {
        Node current = node;
        while (current != null && !current.getStyleClass().contains("auth-input")) {
            current = current.getParent();
        }
        return current;
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.getStyleClass().removeAll("auth-error", "auth-success");
        messageLabel.getStyleClass().add(isError ? "auth-error" : "auth-success");
        messageLabel.setText(message);
        messageLabel.setManaged(true);
        messageLabel.setVisible(true);
    }

    private void clearError() {
        messageLabel.setText("");
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
        for (Node container : inputContainers) {
            if (container != null) {
                container.getStyleClass().remove("error");
            }
        }
    }
}