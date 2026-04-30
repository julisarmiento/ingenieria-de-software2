package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.Map;
import com.is1.proyecto.services.ProfessorService;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class ProfessorController {

    public static void init() {

        get("/professor/create", (req, res) -> {
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
            return new ModelAndView(model, "professor.mustache");
        }, new MustacheTemplateEngine());

        post("/professor/create", (req, res) -> {
            ProfessorService service = new ProfessorService();
            // Solo administradores pueden crear profesores
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            String nombre = req.queryParams("nombre");
            String apellido = req.queryParams("apellido");
            String correo = req.queryParams("correo");
            String dni = req.queryParams("dni");


            try {
                    service.createProfessor(nombre, apellido, correo, dni);
                     res.redirect("/dashboard?message=Profesor creado");
                     return null;

            } catch (IllegalArgumentException e) {
                res.redirect("/professor/create?error=" + e.getMessage());
                return "";
            }
        });

    }
}
