/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 * Gestión de sesión en memoria para el usuario autenticado (Session.start).
 *
 * @author javi
 */
public class Session {

    private static String currentUserEmail;
    private static boolean loggedIn;

    /**
     * Inicia la sesión en memoria con el email normalizado del usuario.
     */
    public static void start(String email) {
        currentUserEmail = email;
        loggedIn = true;
    }

    /**
     * Cierra la sesión activa.
     */
    public static void close() {
        currentUserEmail = null;
        loggedIn = false;
    }

    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }
}
