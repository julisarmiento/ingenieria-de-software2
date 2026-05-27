package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.Map;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.is1.proyecto.exceptions.UserAlreadyExistsException;
import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.services.UserService;

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
            UserService service = new UserService();

            try {
               service.registerUser(name, password);
                res.status(201); // Código de estado HTTP 201 (Created) para una creación exitosa.
                res.redirect("/user/create?message=" + java.net.URLEncoder.encode("Cuenta creada exitosamente para " + name + "!", java.nio.charset.StandardCharsets.UTF_8));
                return ""; 

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
            } catch (ValidationException e) {
                res.redirect("/user/create?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
                return ""; 

            }catch (UserAlreadyExistsException e) {
                res.redirect("/user/create?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
                return ""; 

            } catch (Exception e) {
                res.status(500);
                res.redirect("/user/create?error=Error interno del servidor");
                return "";
            }

        });
    }
}
