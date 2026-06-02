package com.is1.proyecto.controllers;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.javalite.activejdbc.Base;

import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.Career;
import com.is1.proyecto.models.ProgramOfStudy;
import com.is1.proyecto.services.ProgramOfStudyService;
import com.is1.proyecto.models.Role;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class ProgramOfStudyController {

    public static void init() {

        
        get("/program-of-study/create", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) {
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

        post("/program-of-study/create", (req, res) -> {
            ProgramOfStudyService service = new ProgramOfStudyService();
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN)  {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }
            Integer careerId = Integer.parseInt(req.queryParams("career_id"));
            Integer totalH = Integer.parseInt(req.queryParams("total_hours"));
            Integer mandH = Integer.parseInt(req.queryParams("mandatory_hours"));
            Integer elecH = Integer.parseInt(req.queryParams("elective_hours"));

            try {
                ProgramOfStudy planNuevo = service.createProgramOfStudyService(careerId, totalH, mandH, elecH);

                Integer planId = planNuevo.getInteger("id");

                res.redirect("/plan-subject/create?program_id=" + planId);
                return "";

            } catch (ValidationException e) {
                res.redirect("/program-of-study/create?error="
                        + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/plan-subject/create?error=Error inesperado al guardar.");
                return "";
            }
        });

        get("/program-of-study/delete", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN)  {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            // Buscamos todos los planes en la base de datos para mostrarlos en el menú
            // desplegable
            model.put("planes", ProgramOfStudy.findAll().toMaps());

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
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN)  {
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
                Base.commitTransaction();
                return "";
            } catch (Exception e) {
                Base.rollbackTransaction();
                e.printStackTrace();
                res.redirect("/program-of-study/delete?error="
                        + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";
            }
        });
    }
}