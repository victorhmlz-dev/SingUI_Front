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

