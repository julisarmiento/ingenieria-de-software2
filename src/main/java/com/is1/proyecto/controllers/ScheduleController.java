package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.Map;
import org.javalite.activejdbc.LazyList;

import com.is1.proyecto.models.Career;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;
import com.is1.proyecto.models.Role;
import com.is1.proyecto.models.Subject;
import com.is1.proyecto.models.PlanSubject;
import com.is1.proyecto.models.Prerequisite;
import com.is1.proyecto.models.Professor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ScheduleController {

    public static void init() {

        get("/schedule/create", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN && role != Role.PROFESOR) {
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
            
            model.put("subjects", Subject.findAll().toMaps());

            return new ModelAndView(model, "select-subject.mustache");
        }, new MustacheTemplateEngine());

        post("/schedule/select-subject", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN && role != Role.PROFESOR) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }
            
            String materiaId = req.queryParams("identificador_materia");
            Subject materia = Subject.findById(materiaId);
            LazyList <PlanSubject> planesDeMateria = PlanSubject.find("subject_id = ?", materiaId);
            LazyList <Professor> profesores = Professor.findAll();
            LazyList<Career> carreras = Career.findAll();
            PlanSubject planMateria = PlanSubject.findFirst("subject_id = ?", materiaId);
            LazyList<Prerequisite> cursarRegulares = Prerequisite.find("plan_subject_id = ? AND req_type = ?", planMateria.getId(), "CURSAR_REGULAR");
            LazyList<Prerequisite> cursarAprobadas = Prerequisite.find("plan_subject_id = ? AND req_type = ?", planMateria.getId(), "CURSAR_APROBADA");
            LazyList<Prerequisite> rendirAprobadas = Prerequisite.find("plan_subject_id = ? AND req_type = ?", planMateria.getId(), "RENDIR_APROBADA");
            
            try {

                if(materia != null){

                    Map<String, Object> model = new HashMap<>();
                
                    model.put("nombre_materia", materia.getString("name"));
                    model.put("id_materia", materiaId);                
                    model.put("carreras", carreras.toMaps());
                    model.put("planes", planesDeMateria.toMaps());
                    model.put("profesores", profesores.toMaps());
                    model.put("nombre_materia", materia.getString("name"));
                    model.put("id_materia", materiaId);
                    model.put("regimen_materia",planMateria.getString("period"));
                    model.put("ubicacion_plan_de_estudio", planMateria.getString("year"));
                    model.put("cod_regular_cursar", cursarRegulares.toMaps());
                    model.put("cod_aprobada_cursar", cursarAprobadas.toMaps());
                    model.put("cod_aprobada_rendir", rendirAprobadas.toMaps());
                    if(planMateria.getBoolean("is_elective") == false){
                        model.put("caracter_asignatura", "OBLIGATORIA");
                    } else {
                        model.put("caracter_asignatura", "OPTATIVA");
                    }
                    model.put("horas_totales", planMateria.getString("hours"));

                    return new ModelAndView(model, "schedule.mustache");
                
                } else {

                    res.redirect("/schedule/create?error=Materia no encontrada.");
                    return null;
                }

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/schedule/create?error=Error inesperado al guardar.");
                return null;
            }
        }, new MustacheTemplateEngine());

        post("/schedule/create", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN && role != Role.PROFESOR) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            try {

                res.redirect("/dashboard?message="+ URLEncoder.encode("Cronograma guardado con éxito.", StandardCharsets.UTF_8));
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/schedule/create?error=Error inesperado al seleccionar materia.");
                return "";
            }
        });
    }
}