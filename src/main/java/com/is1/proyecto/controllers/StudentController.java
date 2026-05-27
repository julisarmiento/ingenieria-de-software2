package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.models.Person;
import com.is1.proyecto.models.Student;
import com.is1.proyecto.models.User;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

import com.is1.proyecto.models.Person;

public class StudentController {

    public static void init() {

        get("/student/create", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            // model.put("faculties", Faculty.findAll());

            return new ModelAndView(model, "student_create.mustache");
        }, new MustacheTemplateEngine());

        post("/student/create", (req, res) -> {
            // Solo administradores pueden crear carreras
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            // Campos del formulario
            String username = req.queryParams("username"); // Para la tabla users
            String password = req.queryParams("password"); // Para la tabla users
            String name = req.queryParams("name");
            String surname = req.queryParams("surname");
            String dni = req.queryParams("dni");
            String mail = req.queryParams("mail");
            String ageStr = req.queryParams("age");
            String phoneNum = req.queryParams("phoneNum");
            
            boolean isFreshman = req.queryParams("isFreshman") != null;

            // Validaciones básicas
            if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                name == null || name.trim().isEmpty() ||
                surname == null || surname.trim().isEmpty() ||
                dni == null || dni.trim().isEmpty() ||
                mail == null || mail.trim().isEmpty() ||
                ageStr == null || ageStr.trim().isEmpty() ||
                phoneNum == null || phoneNum.trim().isEmpty()) {
                
                res.redirect("/student/create?error=Faltan campos obligatorios.");
                return null;
            }

            // String facultyId = req.queryParams("nombre_de_identificador_Facultad"); //
            // Captura el ID seleccionado

            try {
                Person p = new Person();
                p.set("name", name);
                p.set("surname", surname);
                p.set("dni", dni);
                p.set("age", Integer.parseInt(ageStr));
                p.set("phoneNum", phoneNum);
                p.insert();

                User u = new User();
                u.set("id", p.getId()); // Le pasamos el ID 15
                u.set("name", username); // Ojo: en tu BD el username de la cuenta se guarda en la columna "name"
                u.set("password", password);
                u.set("role", "user");
                u.insert();

                Student s = new Student();
                s.set("id", p.getId()); // Le pasamos el mismo ID 15
                s.set("dni", dni);
                s.set("name", name);
                s.set("surname", surname);
                s.set("age", Integer.parseInt(ageStr));
                s.set("phoneNum", phoneNum);
                s.set("mail", mail);
                s.set("isFreshman", isFreshman);
                s.insert();

                res.redirect("/student/create?message=Estudiante " + name + " " + surname + " registrado con exito.");
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/student/create?error=Error al guardar. Revisa que el DNI, Correo o Usuario no existan ya.");
                return "";
            }
        });

        get("/student/delete", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
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
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            // Verificar identificador único
            String id = req.queryParams("identificador_estudiante");

            try {

                Student s = Student.findFirst("id = ?", id);

                User u = User.findFirst("id = ?", id);

                if (s != null && u != null) {
                    String nombreCompleto = s.getString("name") + " " + s.getString("surname");

                    u.delete(); 

                    res.redirect("/student/delete?message=Estudiante " + nombreCompleto + " eliminado con exito.");
                }
                else{
                    res.redirect("/student/delete?error=Estudiante no encontrado.");
                }
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/student/delete?error=Error inesperado al eliminar.");
                return "";
            }
        });
    }
}