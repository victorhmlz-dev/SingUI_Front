/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package singui_front.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Victor
 * 
 * Política de contraseñas única para toda la aplicación (SignUp y ChangePassword).
 *
 * Requisitos mínimos:
 *   - Entre 8 y 64 caracteres.
 *   - Al menos 1 mayúscula, 1 minúscula y 1 número.
 *   - Solo letras (A-Z, a-z), números y los especiales: . _ - @ $ *
 *   - No puede contener la parte del email anterior a la @ (si tiene 3+ caracteres).
 *
 * Fortaleza (solo para contraseñas válidas):
 *   - WEAK:   cumple los mínimos.
 *   - MEDIUM: mínimos + (más de 1 mayúscula, minúscula y número) O algún carácter especial.
 *   - STRONG: mínimos + ambas cosas.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 64;
    public static final String SPECIAL_CHARS = "._-@$*";

    /** Nivel de fortaleza de una contraseña válida. */
    public enum Strength {
        WEAK, MEDIUM, STRONG;

        /** "Weak", "Medium" o "Strong", para mostrar en pantalla. */
        public String label() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    private PasswordPolicy() { }   // clase de utilidades: no se instancia

    // ---------- Validación ----------

    /**
     * Devuelve la lista de requisitos que NO se cumplen (vacía = contraseña válida).
     *
     * @param password contraseña a comprobar
     * @param email    email del usuario, para impedir que lo contenga (puede ser null)
     */
    public static List<String> validate(String password, String email) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Password is required.");
            return errors;
        }
        if (password.length() < MIN_LENGTH) {
            errors.add("At least " + MIN_LENGTH + " characters.");
        }
        if (password.length() > MAX_LENGTH) {
            errors.add("At most " + MAX_LENGTH + " characters.");
        }
        if (countUpper(password) == 0) {
            errors.add("At least 1 uppercase letter.");
        }
        if (countLower(password) == 0) {
            errors.add("At least 1 lowercase letter.");
        }
        if (countDigits(password) == 0) {
            errors.add("At least 1 number.");
        }
        if (!hasOnlyAllowedChars(password)) {
            errors.add("Only letters, numbers and " + specialCharsForDisplay() + " are allowed.");
        }

        String emailName = emailLocalPart(email);
        if (emailName != null && emailName.length() >= 3
                && password.toLowerCase().contains(emailName)) {
            errors.add("It must not contain your email name.");
        }
        return errors;
    }

    /** Igual que {@link #validate(String, String)} pero sin la regla del email. */
    public static List<String> validate(String password) {
        return validate(password, null);
    }

    public static boolean isValid(String password, String email) {
        return validate(password, email).isEmpty();
    }

    public static boolean isValid(String password) {
        return isValid(password, null);
    }

    // ---------- Fortaleza ----------

    /**
     * Fortaleza de una contraseña. Llamar solo si es válida;
     * si no lo es, devuelve null.
     */
    public static Strength strength(String password) {
        if (!isValid(password)) {
            return null;
        }
        boolean varied = countUpper(password) > 1
                && countLower(password) > 1
                && countDigits(password) > 1;
        boolean hasSpecial = countSpecial(password) > 0;

        if (varied && hasSpecial) return Strength.STRONG;
        if (varied || hasSpecial) return Strength.MEDIUM;
        return Strength.WEAK;
    }

    /** ". _ - @ $ *" para mostrar al usuario. */
    public static String specialCharsForDisplay() {
        return String.join(" ", SPECIAL_CHARS.split(""));
    }

    // ---------- Auxiliares ----------
    // Solo se admiten caracteres ASCII, así que basta con comparar rangos.

    private static int countUpper(String s) {
        return (int) s.chars().filter(c -> c >= 'A' && c <= 'Z').count();
    }

    private static int countLower(String s) {
        return (int) s.chars().filter(c -> c >= 'a' && c <= 'z').count();
    }

    private static int countDigits(String s) {
        return (int) s.chars().filter(c -> c >= '0' && c <= '9').count();
    }

    private static int countSpecial(String s) {
        return (int) s.chars().filter(c -> SPECIAL_CHARS.indexOf(c) >= 0).count();
    }

    private static boolean hasOnlyAllowedChars(String s) {
        return s.chars().allMatch(c ->
                (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
                || (c >= '0' && c <= '9') || SPECIAL_CHARS.indexOf(c) >= 0);
    }

    /** "victor" para "Victor@mail.com"; null si no hay email válido. */
    private static String emailLocalPart(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at).trim().toLowerCase() : null;
    }
}
