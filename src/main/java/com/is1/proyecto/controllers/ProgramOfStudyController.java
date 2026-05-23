package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.models.ProgramOfStudy;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class ProgramOfStudyController {

    public static void init() {

        // 1. Mostrar el formulario
        get("/plan-estudio/create", (req, res) -> {
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

            return new ModelAndView(model, "program_of_study.mustache");
        }, new MustacheTemplateEngine());


        // 2. Procesar los datos enviados
        post("/plan-estudio/create", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            // Capturar campos del formulario
            String subjectName = req.queryParams("subjectName");
            String subjectType = req.queryParams("subjectType");
            String yearStr = req.queryParams("year");
            String hoursStr = req.queryParams("hours");
            String curseReq = req.queryParams("curseReq");
            String examReq = req.queryParams("examReq");
            String facultyIdStr = req.queryParams("faculty_id");

            // Validación básica
            if (subjectName == null || subjectName.isEmpty() || yearStr == null || yearStr.isEmpty() || facultyIdStr == null || facultyIdStr.isEmpty()) {
                res.redirect("/plan-estudio/create?error=Faltan campos obligatorios.");
                return null;
            }

            try {
                ProgramOfStudy plan = new ProgramOfStudy();
                plan.set("subjectName", subjectName);
                plan.set("subjectType", subjectType);
                plan.set("year", Integer.parseInt(yearStr));
                
                // Las horas pueden estar vacías según tu tabla
                if (hoursStr != null && !hoursStr.isEmpty()) {
                    plan.set("hours", Integer.parseInt(hoursStr));
                }
                
                plan.set("curseReq", curseReq);
                plan.set("examReq", examReq);
                plan.set("faculty_id", Integer.parseInt(facultyIdStr));
                
                plan.saveIt();

                res.redirect("/dashboard?message=Plan de estudio creado con exito.");
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/plan-estudio/create?error=Error inesperado al guardar.");
                return "";
            }
        });

        // 3. Mostrar la pantalla para borrar un plan
        get("/plan-estudio/delete", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            // Buscamos todos los planes en la base de datos para mostrarlos en el menú desplegable
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
        post("/plan-estudio/delete", (req, res) -> {
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
                    String nombre = plan.getString("subjectName");
                    plan.delete(); // ActiveJDBC lo borra de la tabla
                    res.redirect("/plan-estudio/delete?message=El plan de " + nombre + " fue eliminado exitosamente.");
                } else {
                    res.redirect("/plan-estudio/delete?error=No se encontro el plan seleccionado.");
                }
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/plan-estudio/delete?error=Error inesperado al intentar eliminar el plan.");
                return "";
            }
        });
    }
}