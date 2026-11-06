package controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

import java.util.List;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import SignUI_Front.client.UserRESTClient;
import singui_front.model.User;

public class SignUp {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String ALLOWED_CHARS = "[a-zA-Z0-9._@$*-]+";
    private static final String SPECIAL_CHAR = ".*[._@$*-].*";

    // Datos personales
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;

    // Contraseña
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private ToggleButton passwordToggle;

    // Confirmación de contraseña
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisibleField;
    @FXML private ToggleButton confirmPasswordToggle;

    // Dirección
    @FXML private TextField stateField;
    @FXML private TextField cityField;
    @FXML private TextField streetField;
    @FXML private TextField zipField;

    @FXML private CheckBox termsCheckBox;
    @FXML private Button signUpButton;
    @FXML private Hyperlink signInLink;
    @FXML private Label messageLabel;
    @FXML private Label strengthLabel;

    @FXML
    public void initialize() {
        clearError();

        if (strengthLabel != null) {
            strengthLabel.setText("");
        }

        linkPasswordToggle(passwordField, passwordVisibleField, passwordToggle);
        linkPasswordToggle(confirmPasswordField, confirmPasswordVisibleField, confirmPasswordToggle);

        passwordField.textProperty().addListener(
            (obs, oldValue, newValue) -> updatePasswordStrength(newValue)
        );

        signUpButton.setOnAction(event -> handleSignUp());

        if (strengthLabel != null) {
            strengthLabel.managedProperty().bind(strengthLabel.textProperty().isNotEmpty());
            strengthLabel.visibleProperty().bind(strengthLabel.textProperty().isNotEmpty());
        }
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

    private void handleSignUp() {
        clearError();

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmation = confirmPasswordField.getText();
        String state = stateField.getText().trim();
        String city = cityField.getText().trim();
        String street = streetField.getText().trim();
        String zip = zipField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
            password.isEmpty() || confirmation.isEmpty() || state.isEmpty() ||
            city.isEmpty() || street.isEmpty() || zip.isEmpty()) {
            showError("Todos los campos son obligatorios.");
            return;
        }

        if (!email.matches(EMAIL_REGEX)) {
            showError("El formato del correo electrónico no es válido.");
            return;
        }

        if (!isValidPassword(password)) {
            showError("La contraseña no cumple los requisitos mínimos.");
            return;
        }

        if (!password.equals(confirmation)) {
            showError("Las contraseñas no coinciden.");
            return;
        }

        if (!termsCheckBox.isSelected()) {
            showError("Debes aceptar los Términos y la Política de Privacidad.");
            return;
        }

        if (!zip.matches("\\d{5}")) {
            showError("El código postal debe tener 5 dígitos.");
            return;
        }

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

    /**
     * Comprueba por GET que el email no existe y, si no existe, lo guarda por POST.
     * Se ejecuta fuera del hilo de JavaFX para no congelar la ventana.
     */
    private void registerUser(User user) {
        final String email = user.getEmail();
        signUpButton.setDisable(true);

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                UserRESTClient client = new UserRESTClient();
                try {
                    try {
                        List<User> existing = client.findByEmail_XML(new GenericType<List<User>>() {}, email);
                        if (existing != null && !existing.isEmpty()) {
                            return false;
                        }
                    } catch (jakarta.ws.rs.NotFoundException notFound) {
                        // el email no existe: se puede registrar
                    }
                    client.create_XML(user);
                    return true;
                } finally {
                    client.close();
                }
            }
        };

        task.setOnSucceeded(e -> {
            signUpButton.setDisable(false);
            if (task.getValue()) {
                showMessage("Cuenta creada correctamente.", false);
            } else {
                showError("Ya existe una cuenta con ese correo electrónico.");
            }
        });

        task.setOnFailed(e -> {
            signUpButton.setDisable(false);
            Throwable ex = task.getException();
            ex.printStackTrace();
            if (ex instanceof ProcessingException) {
                showError("No se pudo conectar con el servidor.");
            } else if (ex instanceof WebApplicationException) {
                int status = ((WebApplicationException) ex).getResponse().getStatus();
                showError("El servidor rechazó el registro (HTTP " + status + ").");
            } else {
                showError("Error inesperado: " + ex.getMessage());
            }
        });

        Thread thread = new Thread(task, "signup-request");
        thread.setDaemon(true);
        thread.start();
    }

    /** GET http://localhost:8080/SignUI_Back/webresources/entities.user */
    private List<User> fetchUsers(UserRESTClient client) {
        return client.findAll_XML(new GenericType<List<User>>() {});
    }

    /** Petición GET suelta (útil para probar la conexión). */
    public void cargarUsuarios() {
        UserRESTClient client = new UserRESTClient();
        try {
            List<User> usuarios = fetchUsers(client);
            if (usuarios != null) {
                for (User usuario : usuarios) {
                    System.out.println("Email obtenido: " + usuario.getEmail());
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener los usuarios: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.close();
        }
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        return password.matches(".*[A-Z].*")
            && password.matches(".*[a-z].*")
            && password.matches(".*[0-9].*")
            && password.matches(ALLOWED_CHARS);
    }

    private String getPasswordStrength(String password) {
        if (!isValidPassword(password)) return "Invalid";

        long upper = password.chars().filter(Character::isUpperCase).count();
        long lower = password.chars().filter(Character::isLowerCase).count();
        long digits = password.chars().filter(Character::isDigit).count();

        boolean varied = upper > 1 && lower > 1 && digits > 1;
        boolean hasSpecial = password.matches(SPECIAL_CHAR);

        if (varied && hasSpecial) return "Strong";
        if (varied || hasSpecial) return "Medium";
        return "Weak";
    }

    private void updatePasswordStrength(String password) {
        if (strengthLabel == null) return;

        String strength = getPasswordStrength(password);
        if (strength.equals("Invalid")) {
            strengthLabel.setText(
                password == null || password.isEmpty() ? "" : "La contraseña no cumple los requisitos."
            );
        } else {
            strengthLabel.setText("Strength: " + strength);
        }
    }

    private void showError(String message) {
        showMessage(message, true);
    }

    private void showMessage(String message, boolean isError) {
        if (messageLabel == null) {
            System.out.println(message);
            return;
        }
        messageLabel.getStyleClass().removeAll("auth-error", "auth-success");
        messageLabel.getStyleClass().add(isError ? "auth-error" : "auth-success");
        messageLabel.setText(message);
        messageLabel.setManaged(true);
        messageLabel.setVisible(true);
    }

    private void clearError() {
        if (messageLabel != null) {
            messageLabel.setText("");
            messageLabel.setManaged(false);
            messageLabel.setVisible(false);
        }
    }
}