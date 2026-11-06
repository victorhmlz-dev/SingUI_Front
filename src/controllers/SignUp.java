package controllers;

import SignUI_Front.client.UserRESTClient;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import java.io.IOException;
import java.util.List;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

public class SignUp {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String ALLOWED_CHARS = "[a-zA-Z0-9._@$*-]+";
    private static final String SPECIAL_CHAR = ".*[._@$*-].*";

    // Campos de la interfaz de usuario
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

    @FXML
    public void initialize() {
        clearError();

        if (strengthLabel != null) {
            strengthLabel.setText("");
        }

        if (passwordVisibleField != null && passwordToggle != null) {
            linkPasswordToggle(passwordField, passwordVisibleField, passwordToggle);
        }
        if (confirmPasswordVisibleField != null && confirmPasswordToggle != null) {
            linkPasswordToggle(confirmPasswordField, confirmPasswordVisibleField, confirmPasswordToggle);
        }

        passwordField.textProperty().addListener(
            (obs, oldValue, newValue) -> updatePasswordStrength(newValue)
        );

        signUpButton.setOnAction(event -> handleSignUp());
        signInLink.setOnAction(event -> handleSignInRedirection());
    }

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

    @FXML
    private void handleSignUp() {
        clearError();

        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String email     = emailField.getText().trim();
        String password  = passwordField.getText();
        String confirmation = confirmPasswordField.getText();
        String state    = stateField.getText().trim();
        String city     = cityField.getText().trim();
        String street   = streetField.getText().trim();
        String zip      = zipField.getText().trim();

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

        Integer zipCode;
        try {
            zipCode = Integer.valueOf(zip);
        } catch (NumberFormatException e) {
            showError("El código postal debe ser un número entero.");
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
        user.setZip(zipCode);

        registrarUsuarioEnBackend(user);
    }

    private void registrarUsuarioEnBackend(User user) {
        signUpButton.setDisable(true);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                UserRESTClient client = new UserRESTClient();
                try {
                    GenericType<List<User>> responseType = new GenericType<List<User>>() {};
                    List<User> usuariosExistentes = client.findAll_XML(responseType);

                    if (usuariosExistentes != null) {
                        boolean existeEmail = usuariosExistentes.stream()
                                .anyMatch(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(user.getEmail()));

                        if (existeEmail) {
                            return false;
                        }
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
            if (Boolean.TRUE.equals(task.getValue())) {
                showMessage("Cuenta registrada correctamente en la base de datos.", false);
                handleSignInRedirection();
            } else {
                showError("El correo electrónico ya está registrado en la base de datos.");
            }
        });

        task.setOnFailed(e -> {
            signUpButton.setDisable(false);
            Throwable ex = task.getException();
            ex.printStackTrace();
            if (ex instanceof ProcessingException) {
                showError("No se pudo conectar con el servidor backend.");
            } else if (ex instanceof WebApplicationException) {
                int status = ((WebApplicationException) ex).getResponse().getStatus();
                showError("El backend rechazó el registro (HTTP " + status + ").");
            } else {
                showError("Error al procesar la solicitud: " + ex.getMessage());
            }
        });

        Thread thread = new Thread(task, "signup-rest-thread");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Redirige a la vista SignIn cargando directamente el FXML sin usar clases auxiliares.
     */
    private void handleSignInRedirection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/res/models/sign_in.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) signInLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sign In");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("No se pudo abrir la pantalla de inicio de sesión.");
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
        if ("Invalid".equals(strength)) {
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
        messageLabel.getStyleClass().removeAll("error", "success");
        messageLabel.getStyleClass().add(isError ? "error" : "success");
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