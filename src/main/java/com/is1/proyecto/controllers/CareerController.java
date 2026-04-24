package com.is1.proyecto.controllers;

import static spark.Spark.get;
import static spark.Spark.post;

import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.models.Career;
//import com.is1.proyecto.models.Faculty;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

public class CareerController {
   

    public static void init() {
    
        get("/career/create", (req, res) -> {
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
            //model.put("faculties", Faculty.findAll());

            return new ModelAndView(model, "career.mustache");
        }, new MustacheTemplateEngine());

        post("/career/create", (req, res) -> {
            // Solo administradores pueden crear carreras
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            // Campos del formulario
            String name = req.queryParams("name");

            // Validaciones básicas
            if (name == null || name.isEmpty()) {
                res.redirect("/career/create?error=Faltan campos obligatorios.");
                return null;
            }

            //String facultyId = req.queryParams("nombre_de_identificador_Facultad"); // Captura el ID seleccionado

            try {
                // Verificar nombre único
                if (Career.findFirst("name = ?", name) != null) {
                    res.redirect("/career/create?error=La carrera ya esta registrado.");
                    return null;
                }

                Career newCareer = new Career();
                newCareer.set("name", name);
                //newCareer.set("faculty_id", facultyId);
                newCareer.insert();

                res.redirect("/dashboard?message=Carrera " + name + " creada con exito.");
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/career/create?error=Error inesperado al guardar.");
                return "";
            }
        });
    }
}
