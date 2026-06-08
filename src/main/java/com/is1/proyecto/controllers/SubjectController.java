package com.is1.proyecto.controllers;
import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.models.Career;
import com.is1.proyecto.models.Subject;
import com.is1.proyecto.models.Role; 
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class SubjectController {

    public static void init() {

        
        get("/materia/create", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("careers", Career.findAll().toMaps());

            
            String errorMessage = req.queryParams("error");
            if (errorMessage != null)
                model.put("errorMessage", errorMessage);

            return new ModelAndView(model, "subject_form.mustache");
        }, new MustacheTemplateEngine());

        post("/materia/create", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) return null;

            String idStr = req.queryParams("id");
            String name = req.queryParams("name");
            Integer careerId = Integer.parseInt(req.queryParams("career_id"));

            if (idStr == null || idStr.isEmpty() || name == null || name.isEmpty() || careerId == null) {
                res.redirect("/materia/create?error=El codigo y el nombre son obligatorios.");
                return null;
            }

            try {
                Subject subject = new Subject();
                subject.set("id", Integer.parseInt(idStr));
                subject.set("name", name);
                subject.set("career_id", careerId);
                subject.insert();

                res.redirect("/dashboard?message=Materia agregada exitosamente.");
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/materia/create?error=Error al cargar la materia.");
                return "";
            }
        });

        get("/materia/delete", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN)  {
                res.redirect("/?error=No tienes permiso.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("materias", Subject.findAll().toMaps());

            String successMessage = req.queryParams("message");
            if (successMessage != null)
                model.put("successMessage", successMessage);
            String errorMessage = req.queryParams("error");
            if (errorMessage != null)
                model.put("errorMessage", errorMessage);

            return new ModelAndView(model, "subject_delete.mustache");
        }, new MustacheTemplateEngine());

        post("/materia/delete", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN)  return null;

            String idStr = req.queryParams("subject_id");

            try {
                Subject subject = Subject.findFirst("id = ?", idStr);
                if (subject != null) {
                    String nombre = subject.getString("name");
                    subject.delete();
                    res.redirect("/materia/delete?message=Materia '" + nombre + "' eliminada con exito.");
                } else {
                    res.redirect("/materia/delete?error=Materia no encontrada.");
                }
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/materia/delete?error=Error al eliminar.");
                return "";
            }
        });
    }
}