package com.is1.proyecto; // Define el paquete de la aplicación, debe coincidir con la estructura de carpetas.

// Importaciones necesarias para la aplicación Spark
import com.is1.proyecto.config.DBConfigSingleton; // Utilidad para serializar/deserializar objetos Java a/desde JSON.
import com.is1.proyecto.config.DBInitializator;
import com.is1.proyecto.config.DatabaseManager; // Importa los métodos estáticos principales de Spark (get, post, before, after, etc.).
import com.is1.proyecto.controllers.AuthController; // Clase central de ActiveJDBC para gestionar la conexión a la base de datos.
import com.is1.proyecto.controllers.CareerController; // Utilidad para hashear y verificar contraseñas de forma segura.
import com.is1.proyecto.controllers.FacultyController; // Representa un modelo de datos y el nombre de la vista a renderizar.
import com.is1.proyecto.controllers.PlanSubjectController;
import com.is1.proyecto.controllers.ProfessorController;
import com.is1.proyecto.controllers.ProgramOfStudyController; // Motor de plantillas Mustache para Spark.
import com.is1.proyecto.controllers.StudentController; // Motor de plantillas Mustache para Spark.
import com.is1.proyecto.controllers.SubjectController;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.halt; // Motor de plantillas Mustache para Spark.
import static spark.Spark.port;

/**
 * Clase principal de la aplicación Spark.
 * Configura las rutas, filtros y el inicio del servidor web.
 */
public class App {

    /**
     * Método principal que se ejecuta al iniciar la aplicación.
     * Aquí se configuran todas las rutas y filtros de Spark.
     */
    public static void main(String[] args) {

        port(8080); // Configura el puerto en el que la aplicación Spark escuchará las peticiones
                    // (por defecto es 4567).

        // Obtener la instancia única del singleton de configuración de la base de
        // datos.
        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        // Forma de crear dinamicamente tablas cada que app se ejecuta, para testear
        // funcionalidades
        try {
            System.out.println("Verificando consistencia de la Base de Datos...");
            dbConfig.openConnection(); // Abrimos un segundo la conexión con el Singleton
            DBInitializator.createTablesIfNotExist(); // Lee scheme.sql si falta algo y siembra el admin hasheado
            dbConfig.closeConnection(); // Cerramos inmediatamente para liberar el archivo dev.db
            System.out.println("Base de Datos lista para operar de manera segura.");
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO al arrancar la inicialización de la DB:");
            e.printStackTrace();
        }

        // --- Filtro 'before' para gestionar la conexión a la base de datos ---
        // Este filtro se ejecuta antes de cada solicitud HTTP.
        before((req, res) -> {
            try {
                // Abre una conexión a la base de datos utilizando las credenciales del
                // singleton.
                DatabaseManager.openConnection(); // Delegamos apertura
                System.out.println("Peticion: " + req.url());

            } catch (Exception e) {
                // Si ocurre un error al abrir la conexión, se registra y se detiene la
                // solicitud
                // con un código de estado 500 (Internal Server Error) y un mensaje JSON.
                System.err.println("Error al abrir conexión con ActiveJDBC: " + e.getMessage());
                halt(500, "{\"error\": \"Error interno del servidor: Fallo al conectar a la base de datos.\"}");
            }
        });

        // --- Filtro 'after' para cerrar la conexión a la base de datos ---
        // Este filtro se ejecuta después de que cada solicitud HTTP ha sido procesada.
        after((req, res) -> {
            try {
                // Cierra la conexión a la base de datos para liberar recursos.
                DatabaseManager.closeConnection(); // Delegamos el cierre
            } catch (Exception e) {
                // Si ocurre un error al cerrar la conexión, se registra.
                System.err.println("Error al cerrar conexion con ActiveJDBC: " + e.getMessage());
            }
        });

        // --- Rutas GET y post para renderizar formularios y páginas HTML de professor
        // ---
        AuthController.init();
        ProfessorController.init();
        StudentController.init();
        CareerController.init();
        FacultyController.init();
        ProgramOfStudyController.init();
        PlanSubjectController.init();
        SubjectController.init();
    }
}
