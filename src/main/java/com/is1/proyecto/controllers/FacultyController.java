package com.is1.proyecto.controllers;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.Faculty;
import com.is1.proyecto.models.Role;
import com.is1.proyecto.services.FacultyService;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class FacultyController {

    public static void init() {

        get("/faculty/create", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN)  {
                res.redirect("/dashboard?error=No tiene permisos de administrador.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("faculties", Faculty.findAll().toMaps());

            String successMessage = req.queryParams("message");
            if (successMessage != null)
                model.put("successMessage", successMessage);
            String errorMessage = req.queryParams("error");
            if (errorMessage != null)
                model.put("errorMessage", errorMessage);

            return new ModelAndView(model, "faculty.mustache");
        }, new MustacheTemplateEngine());

        post("/faculty/create", (req, res) -> {
            FacultyService service = new FacultyService();
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN)  {
                res.redirect("/dashboard?error=No tienes permisos de administrador.");

            }

            String name = req.queryParams("name");
            
            try {
                service.createFaculty(name);
                res.redirect("/dashboard?message=Facultad creada con exito.");
                return "";

            } catch (ValidationException e) {
                res.redirect("/faculty/create?error=" + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/faculty/create?error=Se produjo un error al crear la facultad.");
                return "";
            }
        });

        get("/faculty/delete", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN)  {
                res.redirect("/dashboard?error=No tienes permisos de administrador.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("faculties", Faculty.findAll().toMaps());

            String successMessage = req.queryParams("message");
            if (successMessage != null)
                model.put("successMessage", successMessage);
            String errorMessage = req.queryParams("error");
            if (errorMessage != null)
                model.put("errorMessage", errorMessage);

            return new ModelAndView(model, "faculty_delete.mustache");
        }, new MustacheTemplateEngine());

        post("/faculty/delete", (req, res) -> {
            FacultyService service = new FacultyService();
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN)  {
                res.redirect("/dashboard?error=Accion denegada.");
                return null;
            }

            String id = req.queryParams("faculty_id");

            try {
                service.deleteFaculty(id);
                res.redirect("/faculty/delete?message=Facultad eliminada con exito.");
                return "";
            } catch (ValidationException e) {
                res.redirect("/faculty/delete?error=" + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/faculty/delete?error=Error al intentar eliminar la facultad.");
                return "";
            }
        });
    }
}
