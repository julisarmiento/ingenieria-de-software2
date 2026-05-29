package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.models.Subject;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class SubjectController {

    public static void init() {

        //Alta de materias
        get("/materia/create", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            
            // Ya NO buscamos los planes acá. Queda limpito.
            String errorMessage = req.queryParams("error");
            if (errorMessage != null) model.put("errorMessage", errorMessage);

            return new ModelAndView(model, "subject_form.mustache");
        }, new MustacheTemplateEngine());


        post("/materia/create", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) return null;

            String idStr = req.queryParams("id");
            String name = req.queryParams("name");

            if (idStr == null || idStr.isEmpty() || name == null || name.isEmpty()) {
                res.redirect("/materia/create?error=El codigo y el nombre son obligatorios.");
                return null;
            }

            try {
                Subject subject = new Subject();
                subject.set("id", Integer.parseInt(idStr)); // Guardamos el código que tipeó
                subject.set("name", name);
                
                // ¡LA MAGIA ESTÁ ACÁ! Usamos .insert() en vez de .saveIt() 
                // para obligar a ActiveJDBC a crear el registro con nuestro ID forzado.
                subject.insert(); 

                res.redirect("/dashboard?message=Materia base agregada al catalogo exitosamente.");
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/materia/create?error=Error al guardar. Quizas ese codigo de materia ya existe.");
                return "";
            }
        });

        //Baja de materias
        get("/materia/delete", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("materias", Subject.findAll().toMaps());

            String successMessage = req.queryParams("message");
            if (successMessage != null) model.put("successMessage", successMessage);
            String errorMessage = req.queryParams("error");
            if (errorMessage != null) model.put("errorMessage", errorMessage);

            return new ModelAndView(model, "subject_delete.mustache");
        }, new MustacheTemplateEngine());


        post("/materia/delete", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) return null;

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