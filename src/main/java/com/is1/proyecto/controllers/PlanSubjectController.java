package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.Map;
import com.is1.proyecto.models.PlanSubject;
import com.is1.proyecto.models.Prerequisite;
import com.is1.proyecto.models.Subject;
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
            PlanSubject ps = new PlanSubject();
            ps.set("programOfStudy_id", Integer.parseInt(req.queryParams("program_id")));
            ps.set("subject_id", Integer.parseInt(req.queryParams("subject_id")));
            ps.set("year", Integer.parseInt(req.queryParams("year")));
            ps.set("hours", Integer.parseInt(req.queryParams("hours")));
            ps.set("is_elective", req.queryParams("is_elective") != null ? 1 : 0);
            ps.saveIt();

            String[] curseReqs = req.queryParamsValues("curseReqs");
            if (curseReqs != null) {
                for (String subId : curseReqs) {
                    Prerequisite p = new Prerequisite();
                    p.set("plan_subject_id", ps.getId());
                    p.set("required_subject_id", Integer.parseInt(subId));
                    p.set("req_type", "COURSE");
                    p.saveIt();
                }
            }
            res.redirect("/dashboard?message=Materia cargada con exito");
            return "";
        });
    }
}