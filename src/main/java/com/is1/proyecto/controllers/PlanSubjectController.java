package com.is1.proyecto.controllers;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.javalite.activejdbc.Base;

import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.Subject;
import com.is1.proyecto.services.PlanSubjectService;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class PlanSubjectController {
    public static void init() {
        get("/plan-subject/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("program_id", req.queryParams("program_id"));
            model.put("subjects", Subject.findAll().toMaps());
            return new ModelAndView(model, "planSubject.mustache");
        }, new MustacheTemplateEngine());

        post("/plan-subject/create", (req, res) -> {
            PlanSubjectService service = new PlanSubjectService();
            try {
                Base.openTransaction();
                Integer programId = Integer.parseInt(req.queryParams("program_id"));
                Integer subjectId = Integer.parseInt(req.queryParams("subject_id"));
                Integer year = Integer.parseInt(req.queryParams("year"));
                Integer hour = Integer.parseInt(req.queryParams("hour"));
                boolean isElective = req.queryParams("is_elective") != null;
                String[] curseReqs = req.queryParamsValues("curseReqs");
                service.createPlanSubject(programId, subjectId, year, hour, isElective, curseReqs);
                res.redirect("/dashboard?message=Materia cargada con exito");
                Base.commitTransaction();
                return "";
            } catch (ValidationException e) {
                Base.rollbackTransaction();
                res.redirect("/program-of-study/create?error="
                        + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";
            } catch (Exception e) {
                Base.rollbackTransaction();
                e.printStackTrace();
                res.redirect("/program-of-study/create?error=Error al cargar: " + e.getMessage());
                return "";
            }
        });
    }
}