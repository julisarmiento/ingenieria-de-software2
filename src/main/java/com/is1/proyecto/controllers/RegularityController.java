package com.is1.proyecto.controllers;

import com.is1.proyecto.models.Role;
import com.is1.proyecto.services.RegularityService;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class RegularityController {

    public static void init() {

        get("/professor/regularities", (req, res) -> {
            int professorId = (int) req.session().attribute("userId");
            RegularityService service = new RegularityService();

            List<Map<String, Object>> subjects = service.getProfessorSubjects(professorId);

            Map<String, Object> model = new HashMap<>();
            model.put("subjects", subjects);

            String success = req.queryParams("message");
            String error = req.queryParams("error");
            if (success != null && !success.isEmpty())
                model.put("successMessage", success);
            if (error != null && !error.isEmpty())
                model.put("errorMessage", error);

            return new ModelAndView(model, "regularities.mustache");
        }, new MustacheTemplateEngine());

        get("/professor/regularities/:subjectId/students", (req, res) -> {
            int professorId = (int) req.session().attribute("userId");
            int subjectId = Integer.parseInt(req.params("subjectId"));
            RegularityService service = new RegularityService();

            List<Map<String, Object>> students = service.getStudentsBySubject(subjectId);

            Map<String, Object> model = new HashMap<>();
            model.put("students", students);
            model.put("subjectId", subjectId);

            String success = req.queryParams("message");
            String error = req.queryParams("error");
            if (success != null && !success.isEmpty())
                model.put("successMessage", success);
            if (error != null && !error.isEmpty())
                model.put("errorMessage", error);

            return new ModelAndView(model, "regularity-students.mustache");
        }, new MustacheTemplateEngine());

        post("/professor/regularities/:enrollmentId/regular", (req, res) -> {
            int professorId = (int) req.session().attribute("userId");
            int enrollmentId = Integer.parseInt(req.params("enrollmentId"));
            int subjectId = Integer.parseInt(req.queryParams("subjectId"));

            RegularityService service = new RegularityService();
            Map<String, Object> result = service.markAsRegular(enrollmentId, professorId);

            if ((boolean) result.get("ok")) {
                res.redirect("/professor/regularities/" + subjectId + "/students?message=Alumno marcado como Regular.");
            } else {
                res.redirect("/professor/regularities/" + subjectId + "/students?error=" + result.get("error"));
            }
            return null;
        });

        post("/professor/regularities/:enrollmentId/libre", (req, res) -> {
            int professorId = (int) req.session().attribute("userId");
            int enrollmentId = Integer.parseInt(req.params("enrollmentId"));
            int subjectId = Integer.parseInt(req.queryParams("subjectId"));

            RegularityService service = new RegularityService();
            Map<String, Object> result = service.markAsLibre(enrollmentId, professorId);

            if ((boolean) result.get("ok")) {
                res.redirect("/professor/regularities/" + subjectId + "/students?message=Alumno marcado como Libre.");
            } else {
                res.redirect("/professor/regularities/" + subjectId + "/students?error=" + result.get("error"));
            }
            return null;
        });
    }
}