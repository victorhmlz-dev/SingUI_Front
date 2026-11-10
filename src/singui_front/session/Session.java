/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package singui_front.session;

import singui_front.model.User;

/**
 * @author Victor
 * Sesión en memoria del usuario autenticado. Accesible desde cualquier controlador.
 */

/**
 *
 * @author Victor
 */
public final class Session {

    private static User currentUser;

    private Session() { }

    /** Se llama en SignIn tras un login correcto. */
    public static void start(User user) {
        currentUser = user;
    }

    /** Se llama al cerrar sesión (Home → Logout). */
    public static void close() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
