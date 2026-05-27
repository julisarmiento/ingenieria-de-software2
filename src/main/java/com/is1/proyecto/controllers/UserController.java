package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.is1.proyecto.models.Student;
import com.is1.proyecto.models.User;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class UserController {

    // Instancia estática y final de ObjectMapper para la
    // serialización/deserialización JSON.
    // Se inicializa una sola vez para ser reutilizada en toda la aplicación.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void init() {

        get("/user/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>(); // Crea un mapa para pasar datos a la plantilla.

            // Obtener y añadir mensaje de éxito de los query parameters (ej.
            // ?message=Cuenta creada!)
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }

            // Obtener y añadir mensaje de error de los query parameters (ej. ?error=Campos
            // vacíos)
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            // Renderiza la plantilla 'user_form.mustache' con los datos del modelo.
            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        post("/user/create", (req, res) -> {
            String username = req.queryParams("username"); 
            String password = req.queryParams("password");
            
            String name = req.queryParams("name");
            String surname = req.queryParams("surname");
            String dni = req.queryParams("dni");
            String mail = req.queryParams("mail");
            String ageStr = req.queryParams("age");
            String phoneNum = req.queryParams("phoneNum");


            // Validaciones básicas: campos no pueden ser nulos o vacíos.
            if (username == null || username.isEmpty() || 
                password == null || password.isEmpty() ||
                name == null || name.isEmpty() ||
                surname == null || surname.isEmpty() ||
                dni == null || dni.isEmpty() ||
                mail == null || mail.isEmpty() ||
                ageStr == null || ageStr.isEmpty() ||
                phoneNum == null || phoneNum.isEmpty()) {
                
                res.status(400); // Bad Request
                // Redirige con un mensaje de error general
                res.redirect("/user/create?error=Todos los campos son obligatorios.");
                return ""; // Retorna una cadena vacía ya que la respuesta ya fue redirigida.
            }

            try {
                // Intenta crear y guardar la nueva cuenta en la base de datos.
                User ac = new User(); // Crea una nueva instancia del modelo User.
                // Hashea la contraseña de forma segura antes de guardarla.
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                ac.set("name", username); // Asigna el nombre de usuario.
                ac.set("password", hashedPassword); // Asigna la contraseña hasheada.
                ac.set("role", "estudiante");
                ac.saveIt(); // Guarda el nuevo usuario en la tabla 'users'.

                int userId = ac.getInteger("id");

                //Insercion en estudiante
                Student s = new Student();
                s.set("id", userId); // Lo vinculamos usando el mismo ID
                s.set("name", name);
                s.set("surname", surname);
                s.set("dni", dni);
                s.set("mail", mail);
                s.set("age", Integer.parseInt(ageStr)); // Convertimos la edad a número entero
                s.set("phoneNum", phoneNum); 
                s.set("isFreshman", true);
                s.insert();

                res.status(201); // Código de estado HTTP 201 (Created) para una creación exitosa.
                // Redirige al formulario de creación con un mensaje de éxito.
                res.redirect("/user/create?message=Cuenta creada exitosamente para " + name + "!");
                return ""; // Retorna una cadena vacía.

            } catch (Exception e) {
                // Si ocurre cualquier error durante la operación de DB (ej. nombre de usuario
                // duplicado),
                // se captura aquí y se redirige con un mensaje de error.
                System.err.println("Error al registrar la cuenta: " + e.getMessage());
                e.printStackTrace(); // Imprime el stack trace para depuración.
                res.status(500); // Código de estado HTTP 500 (Internal Server Error).
                res.redirect("/user/create?error=Error interno al crear la cuenta. Intente de nuevo.");
                return ""; // Retorna una cadena vacía.
            }
        });

        // POST: Endpoint para añadir usuarios (API que devuelve JSON, no HTML).
        // Advertencia: Esta ruta tiene un propósito diferente a las de formulario HTML.
        post("/add_users", (req, res) -> {
            res.type("application/json"); // Establece el tipo de contenido de la respuesta a JSON.
            // Obtiene los parámetros 'name' y 'password' de la solicitud.
            String name = req.queryParams("name");
            String password = req.queryParams("password");
            // --- Validaciones básicas ---
            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400); // Bad Request.
                return objectMapper.writeValueAsString(Map.of("error", "Nombre y contrasenia son requeridos."));
            }
            try {
                // --- Creación y guardado del usuario usando el modelo ActiveJDBC ---
                User newUser = new User(); // Crea una nueva instancia de tu modelo User.

                newUser.set("name", name);
                newUser.set("password", BCrypt.hashpw(password, BCrypt.gensalt())); // Hasheada igual que arriba
                newUser.saveIt(); // Guarda el nuevo usuario en la tabla 'users'.
                res.status(201); // Created.
                // Devuelve una respuesta JSON con el mensaje y el ID del nuevo usuario.
                return objectMapper.writeValueAsString(
                        Map.of("message", "Usuario '" + name + "' registrado con exito.", "id", newUser.getId()));
            } catch (Exception e) {
                // Si ocurre cualquier error durante la operación de DB, se captura aquí.
                System.err.println("Error al registrar usuario: " + e.getMessage());
                e.printStackTrace(); // Imprime el stack trace para depuración.
                res.status(500); // Internal Server Error.
                return objectMapper
                        .writeValueAsString(Map.of("error", "Error interno al registrar usuario: " + e.getMessage()));
            }
        });
    }
}
