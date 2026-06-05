package com.is1.proyecto.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import com.is1.proyecto.models.Role; 
import com.is1.proyecto.exceptions.AlreadyExistsException;
import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.Student;
import com.is1.proyecto.services.StudentService;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class StudentController {

    public static void init() {

        get("/student/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>(); // Crea un mapa para pasar datos a la plantilla.

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }

            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            // Renderiza la plantilla 'user_form.mustache' con los datos del modelo.
            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        post("/student/create", (req, res) -> {
            StudentService service = new StudentService();

            String username = req.queryParams("username");
            String password = req.queryParams("password");
            String name = req.queryParams("name");
            String surname = req.queryParams("surname");
            String dni = req.queryParams("dni");
            String mail = req.queryParams("mail");
            String ageStr = req.queryParams("age");
            String phoneNum = req.queryParams("phoneNum");

            try {
                int newStudentId = service.registerStudent(username, password, name, surname, dni, mail, ageStr, phoneNum);

                req.session(true).attribute("currentUsername", username);
                req.session().attribute("userId", newStudentId);
                req.session().attribute("loggedIn", true);
                req.session().attribute("role", Role.ESTUDIANTE);

                String mensaje = URLEncoder.encode(
                    "Cuenta creada exitosamente para " + name + "! Ahora elige tu carrera.",
                    StandardCharsets.UTF_8
                );
                res.redirect("/career/select?message=" + mensaje);
                return "";
            } catch (AlreadyExistsException e) {
                res.redirect(
                        "/student/create?error=" + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";

            } catch (Exception e) {
                // Si ocurre cualquier error durante la operación de DB (ej. nombre de usuario
                // duplicado),
                // se captura aquí y se redirige con un mensaje de error.
                System.err.println("Error al registrar la cuenta: " + e.getMessage());
                e.printStackTrace(); // Imprime el stack trace para depuración.
                res.status(500); // Código de estado HTTP 500 (Internal Server Error).
                res.redirect("/student/create?error=Error interno al crear la cuenta. Intente de nuevo.");
                return "";
            }
        });

        get("/student/delete", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            model.put("students", Student.findAll().toMaps());

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            return new ModelAndView(model, "student_delete.mustache");
        }, new MustacheTemplateEngine());

        post("/student/delete", (req, res) -> {
            StudentService service = new StudentService();
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            // Verificar identificador único
            String id = req.queryParams("identificador_estudiante");

            try {
                String name = service.deleteStudent(id);
                if (name != null) {
                    res.redirect("/student/delete?message=Estudiante " + name + " eliminado con exito.");
                    return "";
                } else {
                    res.redirect("/student/delete?error=Estudiante no encontrado.");
                    return "";
                }

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/student/delete?error=Error inesperado al eliminar.");
                return "";
            }
        });
    }
}