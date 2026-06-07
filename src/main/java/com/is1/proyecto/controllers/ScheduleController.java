package com.is1.proyecto.controllers;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.javalite.activejdbc.LazyList;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;
import com.is1.proyecto.models.*;

import com.is1.proyecto.services.ScheduleService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ScheduleController {

    public static void init() {

        get("/schedule/create", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.PROFESOR) {
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

            LazyList<Subject> materias = Subject.findAll();
            List<Map<String, Object>> lista = new ArrayList<>();
            
            for(Subject m : materias){
                Map<String, Object> aux = new HashMap<>();
                aux.put("id", m.getId());
                aux.put("name", m.getString("name") + " (Cod: " + m.getId() +")");

                lista.add(aux);
            }

            model.put("titulo","Crear un Cronograma");
            model.put("ruta_destino","/schedule/build");
            model.put("subjects", lista);

            return new ModelAndView(model, "select-subject.mustache");
        }, new MustacheTemplateEngine());

        post("/schedule/build", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.PROFESOR) {
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
                    model.put("horas_totales", planMateria.getString("hours"));

                    if(planMateria.getBoolean("is_elective") == false){
                        model.put("caracter_asignatura", "OBLIGATORIA");
                    } else {
                        model.put("caracter_asignatura", "OPTATIVA");
                    }

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
            if (role != Role.PROFESOR) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

                ScheduleService service = new ScheduleService();

                String materiaId = req.queryParams("identificador_materia");
                int anioActual = Integer.parseInt(req.queryParams("anio_actual"));

                String[] carrerasElegidas = req.queryParamsValues("carreras");
                String[] profesoresElegidos = req.queryParamsValues("docentes_seleccionados");

            try {

                service.createSchedule(materiaId, anioActual, carrerasElegidas, profesoresElegidos);

                res.redirect("/dashboard?message="+ URLEncoder.encode("Cronograma guardado con éxito.", StandardCharsets.UTF_8));
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/schedule/create?error=Error inesperado al crear el cronograma.");
                return "";
            }
        });

        get("/schedule/delete", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.PROFESOR) {
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
            LazyList<Schedule> cronogramas = Schedule.findAll();
            List<Map<String, Object>> lista = new ArrayList<>();
            
            for(Schedule cronograma : cronogramas){
                Subject materia = Subject.findById(cronograma.get("subject_id"));
                if(materia != null){
                    Map<String, Object> aux = new HashMap<>();
                    aux.put("id", cronograma.getId());
                    aux.put("name", materia.getString("name") + " (" + materia.getId() +
                                 ") - Año " + cronograma.getString("current_year"));
                
                    lista.add(aux);
                }
            }
            
            model.put("titulo","Elimina un Cronograma");
            model.put("ruta_destino","/schedule/delete");
            model.put("subjects", lista);

            return new ModelAndView(model, "select-subject.mustache");
        }, new MustacheTemplateEngine());

        post("/schedule/delete", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.PROFESOR){
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            ScheduleService service = new ScheduleService();
            String cronogramaId = req.queryParams("identificador_materia");

            try {
                
                service.deleteSchedule(cronogramaId);

                res.redirect("/dashboard?message=El Cronograma fue eliminado exitosamente.");
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/schedule/delete?error=Error al eliminar el cronograma.");
                return "";
            }
        });
    }
}