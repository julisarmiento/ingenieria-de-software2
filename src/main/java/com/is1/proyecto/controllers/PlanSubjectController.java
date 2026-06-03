package com.is1.proyecto.controllers;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.LazyList;

import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.ProgramOfStudy;
import com.is1.proyecto.models.Subject;
import com.is1.proyecto.services.PlanSubjectService;
import com.is1.proyecto.services.ProgramOfStudyService;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class PlanSubjectController {
    public static void init() {
        get("/plan-subject/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String programIdStrg = req.queryParams("program_id");
            model.put("program_id", programIdStrg);
            ProgramOfStudy program = ProgramOfStudy.findById(programIdStrg);
            if (program != null) {
                Integer carreraId = program.getInteger("career_id");
                LazyList<Subject> materiasFiltradas = Subject.where("career_id = ?", carreraId);
                model.put("subjects", materiasFiltradas.toMaps());
            } else {
                res.redirect("/dashboard?error=Plan de estudio no encontrado");
                return null;
            }
            return new ModelAndView(model, "planSubject.mustache");
        }, new MustacheTemplateEngine());

        post("/plan-subject/create", (req, res) -> {
            PlanSubjectService service = new PlanSubjectService();
            String programIdStr = req.queryParams("program_id");
            try {
                Integer programId = Integer.parseInt(req.queryParams("program_id"));
                Integer subjectId = Integer.parseInt(req.queryParams("subject_id"));
                Integer year = Integer.parseInt(req.queryParams("year"));
                Integer hour = Integer.parseInt(req.queryParams("hours"));
                boolean isElective = req.queryParams("is_elective") != null;
                String[] curseReqs = req.queryParamsValues("curseReqs");
                String[] examReqs = req.queryParamsValues("examReqs");
                if (curseReqs == null)
                    curseReqs = new String[0];
                if (examReqs == null)
                    examReqs = new String[0];
                service.createPlanSubject(programId, subjectId, year, hour, isElective, curseReqs, examReqs);

                ProgramOfStudy program = ProgramOfStudy.findById(programIdStr);
                int limiteTotal = program.getInteger("mandatory_subjects") + program.getInteger("elective_subjects");
                int cantidadActual = com.is1.proyecto.models.PlanSubject.count("programOfStudy_id = ?", programId)
                        .intValue();
                if (cantidadActual >= limiteTotal) {
                    res.redirect("/dashboard?message=" + java.net.URLEncoder
                            .encode("¡Excelente! Plan de estudio completado con éxito", StandardCharsets.UTF_8));
                    return "";
                } else {
                    int faltan = limiteTotal - cantidadActual;
                    res.redirect("/plan-subject/create?program_id=" + programIdStr + "&message=" +
                            java.net.URLEncoder.encode("Materia agregada. Faltan cargar " + faltan + " materias.",
                                    StandardCharsets.UTF_8));
                    return "";
                }
            } catch (ValidationException e) {
                res.redirect("/plan-subject/create?program_id=" + programIdStr + "&error="
                        + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/plan-subject/create?program_id=" + programIdStr + "&error=Error+al+cargar");
                return "";
            }
        });

        get("/program-of-study/cancel", (req, res) -> {
            String idStr = req.queryParams("id");
            if (idStr != null) {
                try {
                    Integer id = Integer.parseInt(idStr);
                    ProgramOfStudyService.deleteProgramOfStudyService(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            res.redirect("/dashboard?error=Creación de plan cancelada. Los datos fueron descartados.");
            return null;
        });
    }
}