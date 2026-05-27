package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.models.Faculty;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class FacultyController {

    public static void init() {

        get("/faculty/create", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
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

        post("faculty/create", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            String name = req.queryParams("name");

            if (name == null || name.isEmpty()) {
                res.redirect("/faculty/create?error=Faltan campos obligatorios.");
                return null;
            }

            try {
                if (Faculty.findFirst("name = ?", name) != null) {
                    res.redirect("/faculty/create?error=La facultad ya está registrada.");
                    return null;
                }

                Faculty newFaculty = new Faculty();
                newFaculty.set("name", name);
                newFaculty.saveIt();

                res.redirect("/dashboard?message=Facultad creada con exito.");
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/faculty/create?error=Se produjo un error al crear la facultad.");
                return "";
            }
        });

        // Mostramos el formulario de baja
        get("/faculty/delete", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/dashboard?error=No tienes permisos de administrador.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            // Mandamos las facultades para llenar el select del HTML
            model.put("faculties", Faculty.findAll().toMaps());

            // Mensajes de feedback dinámicos
            String successMessage = req.queryParams("message");
            if (successMessage != null)
                model.put("successMessage", successMessage);
            String errorMessage = req.queryParams("error");
            if (errorMessage != null)
                model.put("errorMessage", errorMessage);

            return new ModelAndView(model, "faculty_delete.mustache");
        }, new MustacheTemplateEngine());

        post("/faculty/delete", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/dashboard?error = Accion denegada.");
                return null;
            }

            String id = req.queryParams("faculty_id");

            try {
                Faculty f = Faculty.findFirst("id = ?", id);

                if (f != null) {
                    String name = f.getString("name");
                    // ON DELETE CASCADE de la DB limpia carreras automáticamente
                    f.delete();
                    res.redirect("/faculty/delete?message=Facultad " + name + " eliminada con exito.");
                } else {
                    res.redirect("/faculty/delete?error=La facultad no existe.");
                }
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/faculty/delete?error=Error al intentar eliminar la facultad.");
                return "";
            }
        });
    }
}
