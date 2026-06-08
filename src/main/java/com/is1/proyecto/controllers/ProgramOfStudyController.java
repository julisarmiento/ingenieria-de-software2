package com.is1.proyecto.controllers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.javalite.activejdbc.Base;

import org.javalite.activejdbc.LazyList;

import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.Career;
import com.is1.proyecto.models.PlanSubject;
import com.is1.proyecto.models.ProgramOfStudy;
import com.is1.proyecto.models.Role;
import com.is1.proyecto.models.Subject;
import com.is1.proyecto.models.Prerequisite;
import com.is1.proyecto.services.ProgramOfStudyService;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class ProgramOfStudyController {

    public static void init() {

        get("/program-of-study/create", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            String errorMessage = req.queryParams("errorMessage");
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = req.queryParams("error");
            }
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            model.put("careers", Career.findAll().toMaps());

            return new ModelAndView(model, "program_of_study.mustache");
        }, new MustacheTemplateEngine());

        post("/program-of-study/create", (req, res) -> {
            ProgramOfStudyService service = new ProgramOfStudyService();
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }
            Integer careerId = Integer.parseInt(req.queryParams("career_id"));
            Integer mandS = Integer.parseInt(req.queryParams("mandatory_subjects"));
            Integer elecS = Integer.parseInt(req.queryParams("elective_subjects"));
            Integer totalS = mandS + elecS;
            Integer yearV = Integer.parseInt(req.queryParams("year_version"));

            try {
                ProgramOfStudy planNuevo = service.createProgramOfStudyService(careerId, totalS, mandS, elecS, yearV);

                Integer planId = planNuevo.getInteger("id");

                res.redirect("/plan-subject/create?program_id=" + planId);
                return "";

            } catch (ValidationException e) {
                res.redirect("/program-of-study/create?errorMessage="
                        + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/program-of-study/delete?error= inesperado al guardar.");
                return "";
            }
        });

        get("/program-of-study/delete", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            List<ProgramOfStudy> allPlanes = ProgramOfStudy.findAll();
            List<Map<String, Object>> planConCarrera = new ArrayList<>();

            for (ProgramOfStudy plan : allPlanes) {
                Map<String, Object> datosPlan = new HashMap<>();

                datosPlan.put("id", plan.getId());
                datosPlan.put("year_version", plan.get("year_version"));
                datosPlan.put("status", plan.get("status"));

                Integer careerId = plan.getInteger("career_id");
                Career carrera = Career.findById(careerId);

                if (carrera != null) {
                    System.out.println("DATOS DE LA CARRERA ENCONTRADA: " + carrera.toMap());
                    datosPlan.put("career_name", carrera.getString("name"));
                } else {
                    datosPlan.put("career_name", "Carrera Desconocida");
                }

                planConCarrera.add(datosPlan);
            }
            model.put("planes", planConCarrera);

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

        post("/program-of-study/delete", (req, res) -> {
            ProgramOfStudyService service = new ProgramOfStudyService();
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            Integer idStr = Integer.parseInt(req.queryParams("plan_id"));

            try {
                service.deleteProgramOfStudyService(idStr);

                res.redirect(
                        "/program-of-study/delete?message=El plan de fue eliminado exitosamente.");
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/program-of-study/delete?error="
                        + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";
            }
        });

        get("/program-of-study/view", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
        
            // Atrapamos los parámetros
            String carreraIdParam = req.queryParams("carrera_id");
            String anioPlanParam = req.queryParams("anio_plan");
            String accion = req.queryParams("accion");
        
            boolean mostrarCorrelativas = "si".equals(req.queryParams("mostrar_correlativas"));
            model.put("mostrar_correlativas", mostrarCorrelativas);

            LazyList<Career> carrerasDb = Career.findAll();
            List<Map<String, Object>> listaCarreras = new ArrayList<>();
            
            for (Career c : carrerasDb) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("name", c.getString("name"));
                
                if (carreraIdParam != null && c.getId().toString().equals(carreraIdParam)) {
                    map.put("selected", true);
                }
                listaCarreras.add(map);
            }
            model.put("carreras", listaCarreras);
        
            if (carreraIdParam != null && !carreraIdParam.isEmpty()) {
                int carreraId = Integer.parseInt(carreraIdParam);
            
                LazyList<ProgramOfStudy> planesDeLaCarrera = ProgramOfStudy.where("career_id = ?", carreraId);
                List<Map<String, Object>> listaAnios = new ArrayList<>();
            
                for (ProgramOfStudy plan : planesDeLaCarrera) {
                    Map<String, Object> anioMap = new HashMap<>();
                    Integer anio = plan.getInteger("year_version");
                    anioMap.put("year_version", anio);
                    
                    // Si el usuario ya eligió un año, lo dejamos seleccionado
                    if (anioPlanParam != null && anio.toString().equals(anioPlanParam)) {
                        anioMap.put("selected", true);
                    }
                    listaAnios.add(anioMap);
                }
                model.put("anios_disponibles", listaAnios);
            
                if ("buscar".equals(accion) && anioPlanParam != null && !anioPlanParam.isEmpty()) {
                    int anioPlan = Integer.parseInt(anioPlanParam);
                
                    ProgramOfStudy planSeleccionado = ProgramOfStudy.findFirst("career_id = ? AND year_version = ?", carreraId, anioPlan);
                
                    if (planSeleccionado != null) {
                        model.put("planSeleccionado", true);
                    
                        Career carrera = Career.findById(carreraId);
                        model.put("carrera_id", carrera.getId());
                        model.put("carrera_nombre", carrera.getString("name"));
                        model.put("anio_plan", planSeleccionado.getInteger("year_version"));
                        model.put("estado", planSeleccionado.getString("status"));
                        model.put("version", planSeleccionado.getString("year_version"));
                        model.put("tipo_plan", "ORDINARIO");
                        model.put("facultad_nombre", "EXACTAS FCO. QCAS. Y NAT.");
                    
                        LazyList<PlanSubject> materiasDelPlan = PlanSubject.where("programOfStudy_id = ?", planSeleccionado.getId());
                        List<Map<String, Object>> listaMaterias = new ArrayList<>();
                        int sumatoriaHorasTotales = 0;
                    
                        for (PlanSubject planMateria : materiasDelPlan) {
                            Subject materiaReal = Subject.findById(planMateria.get("subject_id"));
                        
                            if (materiaReal != null) {
                                Map<String, Object> fila = new HashMap<>();
                                fila.put("codigo", materiaReal.getId());
                                fila.put("nombre", materiaReal.getString("name"));
                                fila.put("periodo", planMateria.getString("period"));
                                fila.put("anio", planMateria.getInteger("year"));
                            
                                boolean esOptativa = planMateria.getBoolean("is_elective");
                                fila.put("tipo", esOptativa ? "OPT" : "OB");
                            
                                int horasMateria = planMateria.getInteger("hours");
                                fila.put("horas", horasMateria);
                                sumatoriaHorasTotales += horasMateria;
                            
                                fila.put("disponibilidad", "Si");
                                
                                if (mostrarCorrelativas) {
                                    LazyList<Prerequisite> preReqs = Prerequisite.where("plan_subject_id = ?", planMateria.getId());
                                                
                                    List<String> paraCursar = new ArrayList<>();
                                    List<String> paraRendir = new ArrayList<>();
                                                
                                    for (Prerequisite reqPre : preReqs) {
                                        String tipoReq = reqPre.getString("req_type");
                                        Integer reqSubjectId = reqPre.getInteger("required_subject_id");
                                    
                                        if ("CURSAR_REGULAR".equals(tipoReq)) {
                                            paraCursar.add("[" + reqSubjectId + "] R");
                                        } else if ("CURSAR_APROBADA".equals(tipoReq)) {
                                            paraCursar.add("[" + reqSubjectId + "] A");
                                        } else if ("RENDIR_REGULAR".equals(tipoReq)) {
                                            paraRendir.add("[" + reqSubjectId + "] R");
                                        } else if ("RENDIR_APROBADA".equals(tipoReq)) {
                                            paraRendir.add("[" + reqSubjectId + "] A");
                                        }
                                    }
                                
                                    fila.put("correlativas_cursar", paraCursar.isEmpty() ? "---" : String.join("<br>", paraCursar));
                                    fila.put("correlativas_rendir", paraRendir.isEmpty() ? "---" : String.join("<br>", paraRendir));
                                }
                                
                                listaMaterias.add(fila);
                            }
                        }
                    
                        model.put("materias", listaMaterias);
                        model.put("horas_totales", sumatoriaHorasTotales);
                    
                    } else {
                        model.put("errorMessage", "No se encontró un plan de estudios para la carrera y año seleccionados.");
                    }
                }
            }
        
            return new ModelAndView(model, "plan_study.mustache");
        }, new MustacheTemplateEngine());

        
        post("/program-of-study/view", (req, res) -> {
            ProgramOfStudyService service = new ProgramOfStudyService();
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            Integer idStr = Integer.parseInt(req.queryParams("plan_id"));

            try {
            
                service.deleteProgramOfStudyService(idStr);

                res.redirect(
                        "/program-of-study/view?message=El plan de fue eliminado exitosamente.");
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/program-of-study/view?error="
                        + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";
            }
        });
    }
}