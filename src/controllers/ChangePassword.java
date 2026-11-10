package controllers;

import SignUI_Front.client.UserRESTClient;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import singui_front.model.User;
import singui_front.session.Session;
import singui_front.util.PasswordPolicy;

/**
 * @author Victor
 * 
 * Controlador de la ventana de cambio de contraseña (change_password.fxml).
 *
 * Flujo:
 *   1. Obtiene el usuario de {@link Session}.
 *   2. Valida: campos obligatorios, contraseña actual correcta, política común
 *      ({@link PasswordPolicy}), nueva distinta de la actual y confirmación.
 *   3. Envía el usuario con la nueva contraseña al backend (PUT entities.user/{id}),
 *      en segundo plano.
 *   4. Solo si el servidor confirma, actualiza la contraseña en Session.
 */
public class ChangePassword {

    private static final String HOME_FXML    = "/views/res/models/home.fxml";
    private static final String SIGN_IN_FXML = "/views/res/models/sign_in.fxml";

    private static final String ERROR_STYLE   = "auth-error";
    private static final String SUCCESS_STYLE = "auth-success";

    // ---------- Nodos del FXML ----------

    @FXML private PasswordField currentPasswordField;
    @FXML private TextField currentPasswordVisibleField;
    @FXML private ToggleButton currentPasswordToggle;

    @FXML private PasswordField newPasswordField;
    @FXML private TextField newPasswordVisibleField;
    @FXML private ToggleButton newPasswordToggle;

    @FXML private PasswordField confirmPasswordField;

    @FXML private Label strengthLabel;
    @FXML private Label messageLabel;
    @FXML private Button updatePasswordButton;
    @FXML private Button backButton;

    // ---------- Inicialización ----------

    @FXML
    public void initialize() {
        // Sin sesión no se puede cambiar la contraseña: volver a Sign In.
        if (!Session.isLoggedIn()) {
            Platform.runLater(() -> navigateTo(SIGN_IN_FXML));
            return;
        }

        // Mostrar/ocultar contraseña: mismo modelo que SignIn y SignUp.
        linkPasswordToggle(currentPasswordField, currentPasswordVisibleField, currentPasswordToggle);
        linkPasswordToggle(newPasswordField, newPasswordVisibleField, newPasswordToggle);

        // Fortaleza en vivo de la nueva contraseña.
        newPasswordField.textProperty().addListener((obs, oldValue, newValue) -> updatePasswordStrength());

        // Al escribir en cualquier campo, se oculta el mensaje.
        currentPasswordField.textProperty().addListener((obs, oldValue, newValue) -> clearMessage());
        newPasswordField.textProperty().addListener((obs, oldValue, newValue) -> clearMessage());
        confirmPasswordField.textProperty().addListener((obs, oldValue, newValue) -> clearMessage());

        // La etiqueta de fortaleza solo ocupa espacio cuando tiene texto.
        strengthLabel.managedProperty().bind(strengthLabel.textProperty().isNotEmpty());
        strengthLabel.visibleProperty().bind(strengthLabel.textProperty().isNotEmpty());

        clearMessage();
        strengthLabel.setText("");
        Platform.runLater(() -> currentPasswordField.requestFocus());
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
    private void handleChangePassword() {
        clearMessage();

        User user = Session.getCurrentUser();
        String current = currentPasswordField.getText();
        String newPwd  = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        // 1. Campos obligatorios
        if (isBlank(current) || isBlank(newPwd) || isBlank(confirm)) {
            showError("All fields are required.");
            return;
        }

        // 2. La contraseña actual tiene que ser la del usuario en sesión.
        //    No se aplica la política: las contraseñas antiguas pueden no cumplirla.
        if (!current.equals(user.getPassword())) {
            showError("The current password is incorrect.");
            currentPasswordField.clear();
            focus(currentPasswordField, currentPasswordVisibleField, currentPasswordToggle);
            return;
        }

        // 3. Política común: indica exactamente qué falta.
        List<String> passwordErrors = PasswordPolicy.validate(newPwd, user.getEmail());
        if (!passwordErrors.isEmpty()) {
            showError(String.join(" ", passwordErrors));
            focus(newPasswordField, newPasswordVisibleField, newPasswordToggle);
            return;
        }

        // 4. La nueva tiene que ser distinta de la actual.
        if (newPwd.equals(current)) {
            showError("The new password must be different from the current one.");
            focus(newPasswordField, newPasswordVisibleField, newPasswordToggle);
            return;
        }

        // 5. Confirmación
        if (!newPwd.equals(confirm)) {
            showError("The new passwords do not match.");
            confirmPasswordField.requestFocus();
            return;
        }

        saveNewPassword(user, newPwd);
    }

    /** Limpia el formulario. */
    @FXML
    private void handleCancel() {
        resetForm();
        clearMessage();
    }

    /** Vuelve a Home. Home lee el usuario de Session. */
    @FXML
    private void handleBack() {
        navigateTo(HOME_FXML);
    }

    // ---------- Guardado en el backend ----------

    /**
     * Envía el usuario con la nueva contraseña al servidor (PUT entities.user/{id}).
     * Se manda una copia: el usuario de Session solo se modifica si el servidor confirma,
     * para que nunca quede una contraseña que no está en la base de datos.
     */
    private void saveNewPassword(User user, String newPwd) {
        User updated = copyOf(user);
        updated.setPassword(newPwd);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                UserRESTClient client = new UserRESTClient();
                try {
                    client.edit_XML(updated, String.valueOf(updated.getId()));
                    return null;
                } finally {
                    client.close();
                }
            }
        };

        // Botón desactivado mientras dura la petición.
        updatePasswordButton.disableProperty().bind(task.runningProperty());

        task.setOnSucceeded(e -> {
            user.setPassword(newPwd);          // Session ya refleja la nueva contraseña
            resetForm();
            showSuccess("Your password has been updated.");
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            if (ex instanceof ProcessingException) {
                showError("Could not connect to the server.");
            } else if (ex instanceof WebApplicationException) {
                int status = ((WebApplicationException) ex).getResponse().getStatus();
                showError("The server rejected the change (HTTP " + status + ").");
            } else {
                showError("An unexpected error occurred. Please try again later.");
            }
        });

        Thread thread = new Thread(task, "change-password-request");
        thread.setDaemon(true);
        thread.start();
    }

    /** Copia de todos los campos del usuario (el PUT sustituye el registro completo). */
    private User copyOf(User source) {
        User copy = new User(source.getId());
        copy.setFirstName(source.getFirstName());
        copy.setLastName(source.getLastName());
        copy.setMiddleInitial(source.getMiddleInitial());
        copy.setEmail(source.getEmail());
        copy.setPassword(source.getPassword());
        copy.setStreet(source.getStreet());
        copy.setCity(source.getCity());
        copy.setState(source.getState());
        copy.setZip(source.getZip());
        copy.setPhone(source.getPhone());
        return copy;
    }

    // ---------- Indicador de fortaleza ----------

    /** Muestra qué falta o, si la nueva contraseña es válida, su fortaleza. */
    private void updatePasswordStrength() {
        String pwd = newPasswordField.getText();
        if (pwd == null || pwd.isEmpty()) {
            strengthLabel.setText("");
            return;
        }
        List<String> errors = PasswordPolicy.validate(pwd, Session.getCurrentUserEmail());
        strengthLabel.setText(errors.isEmpty()
                ? "Strength: " + PasswordPolicy.strength(pwd).label()
                : String.join(" ", errors));
    }

    // ---------- Navegación ----------

    private void navigateTo(String fxmlPath) {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            showError("View not found: " + fxmlPath);
            return;
        }
        try {
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load the view: " + fxmlPath);
        }
    }

    // ---------- Utilidades de interfaz ----------

    /** Vacía los campos y vuelve a ocultar las contraseñas. */
    private void resetForm() {
        currentPasswordToggle.setSelected(false);
        newPasswordToggle.setSelected(false);
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        strengthLabel.setText("");
    }

    /** Da el foco al campo de contraseña que esté visible. */
    private void focus(PasswordField hidden, TextField visible, ToggleButton toggle) {
        (toggle.isSelected() ? visible : hidden).requestFocus();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void showError(String message)   { showMessage(message, ERROR_STYLE); }

    private void showSuccess(String message) { showMessage(message, SUCCESS_STYLE); }

    private void showMessage(String message, String styleClass) {
        messageLabel.getStyleClass().removeAll(ERROR_STYLE, SUCCESS_STYLE);
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