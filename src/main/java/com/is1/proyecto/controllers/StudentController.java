package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.is1.proyecto.models.Student;
import com.is1.proyecto.models.User;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class StudentController {

    public static void init() {


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