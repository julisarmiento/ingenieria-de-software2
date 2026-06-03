package com.is1.proyecto.controllers;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.javalite.activejdbc.Base;

import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.Career;
import com.is1.proyecto.models.ProgramOfStudy;
import com.is1.proyecto.services.ProgramOfStudyService;

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
            ProgramOfStudyService service = new ProgramOfStudyService();
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }
            Integer careerId = Integer.parseInt(req.queryParams("career_id"));
            Integer mandS = Integer.parseInt(req.queryParams("mandatory_subjects"));
            Integer elecS = Integer.parseInt(req.queryParams("elective_subjects"));
            Integer totalS = mandS + elecS;
            Integer yearV = Integer.parseInt(req.queryParams("year_version"));

            try {
                ProgramOfStudy planNuevo = service.createProgramOfStudyService(careerId, totalS, mandS, elecS, yearV);

                Integer planId = planNuevo.getInteger("id");

                res.redirect("/plan-subject/create?program_id=" + planId);
                return "";

            } catch (ValidationException e) {
                res.redirect("/program-of-study/create?error="
                        + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/program-of-study/delete?error= inesperado al guardar.");
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
            ProgramOfStudyService service = new ProgramOfStudyService();
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            // Capturamos el ID del plan que el usuario eligió en el formulario
            Integer idStr = Integer.parseInt(req.queryParams("plan_id"));

            try {
                // Buscamos ese plan específico
                service.deleteProgramOfStudyService(idStr);

                res.redirect(
                        "/program-of-study/delete?message=El plan de fue eliminado exitosamente.");
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/program-of-study/delete?error="
                        + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";
            }
        });
    }
}