package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.models.Career;
import com.is1.proyecto.models.ProgramOfStudy;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class ProgramOfStudyController {

    public static void init() {

        // 1. Mostrar el formulario
        get("/program-of-study/create", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            model.put("careers", Career.findAll().toMaps());

            return new ModelAndView(model, "program_of_study.mustache");
        }, new MustacheTemplateEngine());

        // 2. Procesar los datos enviados
        post("/program-of-study/create", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }
            String careerId = req.queryParams("career_id");
            String totalH = req.queryParams("total_hours");
            String mandH = req.queryParams("mandatory_hours");
            String elecH = req.queryParams("elective_hours");

            if (careerId == null || totalH == null || mandH == null || elecH == null) {
                res.redirect("/program-of-study/create?error=Faltan campos obligatorios.");
                return null;
            }
            try {
                ProgramOfStudy pos = new ProgramOfStudy();
                pos.set("career_id", Integer.parseInt(req.queryParams("career_id")));
                pos.set("total_hours", Integer.parseInt(req.queryParams("total_hours")));
                pos.set("mandatory_hours", Integer.parseInt(req.queryParams("mandatory_hours")));
                pos.set("elective_hours", Integer.parseInt(req.queryParams("elective_hours")));

                pos.saveIt();

                Integer nuevoId = pos.getInteger("id");

                res.redirect("/plan-subject/create?program_id=" + nuevoId);
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/plan-subject/create?error=Error inesperado al guardar.");
                return "";
            }
        });

        // 3. Mostrar la pantalla para borrar un plan
        get("/program-of-study/delete", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            // Buscamos todos los planes en la base de datos para mostrarlos en el menú
            // desplegable
            model.put("planes", ProgramOfStudy.findAll().toMaps());

            // Manejo de mensajes de éxito y error
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            return new ModelAndView(model, "program_of_study_delete.mustache");
        }, new MustacheTemplateEngine());

        // 4. Procesar la eliminación en la base de datos
        post("/program-of-study/delete", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            // Capturamos el ID del plan que el usuario eligió en el formulario
            String idStr = req.queryParams("plan_id");

            try {
                // Buscamos ese plan específico
                ProgramOfStudy plan = ProgramOfStudy.findFirst("id = ?", idStr);

                if (plan != null) {
                    plan.delete(); // ActiveJDBC lo borra de la tabla
                    res.redirect(
                            "/program-of-study/delete?message=El plan de fue eliminado exitosamente.");
                } else {
                    res.redirect("/program-of-study/delete?error=No se encontro el plan seleccionado.");
                }
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/program-of-study/delete?error=Error inesperado al intentar eliminar el plan.");
                return "";
            }
        });
    }
}