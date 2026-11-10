# SingUI_Front
Client project repository 

# ------- STACK

## Preparar el entorno
1. NetBeans 25.
2. Instalar Liberica JDK 25 **Full** (incluye JavaFX).
3. En NetBeans: Tools → Java Platforms → Add Platform → Java Standard Edition,
   seleccionar la carpeta del JDK y llamarla exactamente `JDK_25_JFX25`.
4. No enlazar librerías desde fuera del proyecto: todas van en `lib/`.
5. No subir cambios en `nbproject/project.properties` salvo que se añada una 
librería a propósito.

# ------- CHANGE PASSWORD

# Requisitos minimos de contraseña (Sign up debe tener los mismos criterios):

    - La contraseña debe tener al menos 8 caracteres (Pueden ser alfanúmericos).
    - La contraseña debe tener al menos 1 mayúscula.
    - La contraseña debe tener al menos 1 minúscula.
    - La contraseña debe tener al menos 1 número. 

# Requisitos adicionales:

    - La contraseña tiene más de un número, más de una mayúscula, y 
      más de una minúscula.
    - La contraseña puede tener al menos un carácter especial.
      Carácteres especiales admitidos: ".", "_", "-", "@", "$", "*"
    
# Criterios de fortaleza de contraseña
    
    - Cumpliendo con los requisitos mínimos: (Weak)
    - Cumpliendo los requisitos mínimos, más alguno de los requisitos 
      adicionales: (Medium)
    - Cumpliendo los requisitos mínimos, más ambos de los requisitos 
      adicionales: (Strong)

# VALIDACIÓN:

    - Al momento de escribir
    - Al momento de pulsar el botón fundamental.

# CAMBIOS AL CSS:

    - Se modificaron y añadieron estilos:
        auth-helper
        eye-button
        eye-button.icon

### 1. Autenticación e Interfaz de Inicio de Sesión (`SignIn`)

* **Controlador `SignInController`**: Vinculación estricta en FXML (`fx:controller="controllers.SignInController"`).
* **Toggle de Visibilidad de Contraseña**: Conmutación entre `PasswordField` y `TextField` visible con botón de ojo (`👁`).
* **Validación Defensiva**: Inicialización protegida contra `NullPointerException` ante la falta de nodos en la vista FXML.

### 2. Transferencia de Datos entre Vistas (`SignIn` ➔ `Home`)

* **Propagación del Usuario**: Al autenticarse correctamente, se pasa el identificador/correo del usuario al controlador de `Home`.
* **Personalización del Menú y Avatar**:
  * Actualización dinámica del texto en `userMenu`.
  * Método de extracción de iniciales para el icono del avatar (`avatarInitials`).

### 3. Navegación y Control de Sesión en `Home`

* **Cerrar Sesión (`handleLogout`)**: Carga de la vista `sign_in.fxml` y cambio de escena obteniendo el `Stage` desde el control `userMenu`.
* **Cambiar Contraseña (`handleChangePassword`)**: Carga de la vista `change_password.fxml` enviando opcionalmente la sesión del usuario activo.
