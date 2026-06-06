package com.is1.proyecto.controllers;

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
                service.registerStudent(username, password, name, surname, dni, mail, ageStr, phoneNum);
                res.status(201); // Código de estado HTTP 201 (Created) para una creación exitosa.
                res.redirect("/student/create?message=" + java.net.URLEncoder
                        .encode("Cuenta creada exitosamente para " + name + "!", StandardCharsets.UTF_8));
                return "";

            } catch (ValidationException e) {
                res.redirect(
                        "/student/create?error=" + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
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

        get("/profile", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            
            String currentUsername = req.session().attribute("currentUsername");
            
            if (currentUsername == null) {
                res.redirect("/?error=Debes iniciar sesion primero.");
                return null;
            }

            // Buscamos al Usuario en la base de datos
            com.is1.proyecto.models.User user = com.is1.proyecto.models.User.findFirst("name = ?", currentUsername);
            
            if (user != null) {
                // Buscamos los datos del Estudiante
                Student student = Student.findById(user.getId());
                
                if (student != null) {
                    model.put("nombre", student.getString("name"));
                    model.put("apellido", student.getString("surname"));
                    model.put("dni", student.getString("dni"));
                    model.put("edad", student.getInteger("age"));
                    model.put("correo", student.getString("mail"));
                    model.put("telefono", student.getString("phoneNum"));
                }
            }

            return new ModelAndView(model, "profile.mustache");
        }, new MustacheTemplateEngine());

        get("/settings", (req, res) -> {
            if (req.session().attribute("currentUsername") == null) {
                res.redirect("/?error=Debes iniciar sesion primero.");
                return null;
            }
            return new ModelAndView(new HashMap<>(), "settings.mustache"); 
        }, new MustacheTemplateEngine());

        get("/settings/change-password", (req, res) -> {
            if (req.session().attribute("currentUsername") == null) {
                res.redirect("/?error=Debes iniciar sesion primero.");
                return null;
            }
            Map<String, Object> model = new HashMap<>();
            if (req.queryParams("error") != null) {
                model.put("error", req.queryParams("error"));
            }
            return new ModelAndView(model, "change_password.mustache");
        }, new MustacheTemplateEngine());

        post("/settings/change-password", (req, res) -> {
            String currentUsername = req.session().attribute("currentUsername");
            if (currentUsername == null) {
                res.redirect("/?error=Debes iniciar sesion primero.");
                return "";
            }

            String nuevaPass = req.queryParams("nueva_contrasenia");

            try {
                if (!nuevaPass.matches("^[a-zA-Z0-9]+$")) {
                    throw new Exception("La contraseña solo puede contener letras y números.");
                }

                com.is1.proyecto.models.User user = com.is1.proyecto.models.User.findFirst("name = ?", currentUsername);
                
                if (user != null) {
                    String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(nuevaPass, org.mindrot.jbcrypt.BCrypt.gensalt());
                    user.set("password", hashedPassword);
                    user.saveIt();
                }

                res.redirect("/dashboard?message=" + java.net.URLEncoder.encode("Contraseña actualizada con éxito.", StandardCharsets.UTF_8));
                return "";

            } catch (Exception e) {
                res.redirect("/settings/change-password?error=" + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";
            }
        });

        post("/settings/delete-account", (req, res) -> {
            String currentUsername = req.session().attribute("currentUsername");
            if (currentUsername == null) {
            res.redirect("/?error=Debes iniciar sesion primero.");
            return "";
        }

        try {
            // Buscamos al usuario y estudiante asociado
            com.is1.proyecto.models.User user = com.is1.proyecto.models.User.findFirst("name = ?", currentUsername);

            if (user != null) {
                // Eliminar al estudiante (ajusta según tu lógica de Cascade si existe)
                Student student = Student.findById(user.getId());
                if (student != null) {
                    student.delete();
                }
                // Eliminar al usuario
                user.delete();
            }

            // Invalidar sesión y redirigir al login
            req.session().invalidate();
            res.redirect("/?message=" + java.net.URLEncoder.encode("Cuenta eliminada correctamente.", "UTF-8"));
            return "";
        } catch (Exception e) {
            res.redirect("/settings?error=Error al eliminar la cuenta.");
            return "";
        }
        });
    }
}