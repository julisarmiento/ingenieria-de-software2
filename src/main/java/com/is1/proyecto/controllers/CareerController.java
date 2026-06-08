package com.is1.proyecto.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.is1.proyecto.models.Career;
import com.is1.proyecto.models.Faculty;
import com.is1.proyecto.models.Role;
import com.is1.proyecto.models.Student;
import com.is1.proyecto.services.CareerService;
import com.is1.proyecto.services.StudentService;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class CareerController {

    public static void init() {

        CareerService service = new CareerService();

        get("/career/create", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) {
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
            model.put("faculties", Faculty.findAll().toMaps());

            return new ModelAndView(model, "career.mustache");
        }, new MustacheTemplateEngine());

        post("/career/create", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            String name = req.queryParams("nombre_carrera");
            String facultyId = req.queryParams("identificador_facultad");

            try {
                service.createCareer(name, facultyId);
                String mensajeCodificado = URLEncoder.encode("Carrera " + name +
                        " creada con exito.", StandardCharsets.UTF_8);
                res.redirect("/dashboard?message=" + mensajeCodificado);
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/career/create?error=Error inesperado al guardar.");
                return "";
            }
        });

        get("/career/delete", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            model.put("careers", Career.findAll().toMaps());

            List<Map<String, Object>> lista = Career.findAll().toMaps();
            if (!lista.isEmpty()) {
                System.out.println("DEBUG: Claves disponibles en el mapa: " + lista.get(0).keySet());
            }
            model.put("careers", lista);

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            return new ModelAndView(model, "career_delete.mustache");
        }, new MustacheTemplateEngine());

        post("/career/delete", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ADMIN) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }
            String id = req.queryParams("identificador_carrera");

            try {

                service.deleteCareer(id);
                res.redirect("/career/delete?message=Carrera eliminada con exito.");
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/career/delete?error=Error inesperado al eliminar.");
                return "";
            }
        });

        get("/career/select", (req, res) -> {
            Role role = req.session().attribute("role");
            if (role != Role.ESTUDIANTE) {
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

            model.put("ruta_destino", "/career/select");
            model.put("careers", Career.findAll().toMaps());

            return new ModelAndView(model, "career_select.mustache");
        }, new MustacheTemplateEngine());

        post("/career/select", (req, res) -> {
            Boolean loggedIn = req.session().attribute("loggedIn");
            Role role = req.session().attribute("role");
            if (loggedIn == null || !loggedIn || role != Role.ESTUDIANTE) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            int studentId = req.session().attribute("userId");
            int careerId = Integer.parseInt(req.queryParams("career_id"));
            try {
                StudentService stService = new StudentService();
                stService.assignCareer(studentId, careerId);
                String mensaje = URLEncoder.encode("Carrera asignada con exito.", StandardCharsets.UTF_8);
                res.redirect("/dashboard?message=" + mensaje);
                return "";

            } catch (IllegalArgumentException e) {
                String error = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
                res.redirect("/career/select?error=" + error);
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/career/select?error=Error inesperado al asignar la carrera.");
                return "";
            }
        });

        get("/career/unenroll", (req, res) -> {
            Boolean loggedIn = req.session().attribute("loggedIn");
            Role role = req.session().attribute("role");
            if (loggedIn == null || !loggedIn || role != Role.ESTUDIANTE) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            int studentId = req.session().attribute("userId");

            Student student = Student.findById(studentId);
            if (student != null && student.get("career_id") != null) {
                Career career = Career.findById(student.get("career_id"));
                if (career != null) {
                    model.put("career", career.toMap());
                }
            } else {
                model.put("errorMessage", "No estás inscripto en ninguna carrera actualmente.");
            }

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            return new ModelAndView(model, "career_unenroll.mustache");
        }, new MustacheTemplateEngine());

        post("/career/unenroll", (req, res) -> {
            Boolean loggedIn = req.session().attribute("loggedIn");
            Role role = req.session().attribute("role");

            if (loggedIn == null || !loggedIn || role != Role.ESTUDIANTE) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            int studentId = req.session().attribute("userId");
            Student student = Student.findFirst("id = ?", studentId);
            if (student == null || student.get("career_id") == null) {
                res.redirect("/dashboard?error=No tenés una carrera asignada.");
                return null;
            }
            int careerId = student.getInteger("career_id");

            try {
                StudentService stService = new StudentService();
                stService.unenrollCareer(studentId, careerId);

                String mensaje = URLEncoder.encode("Te has dado de baja de la carrera exitosamente.",
                        StandardCharsets.UTF_8);
                res.redirect("/dashboard?message=" + mensaje);
                return "";

            } catch (IllegalArgumentException e) {
                String error = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
                res.redirect("/dashboard?error=" + error);
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/dashboard?error=Error inesperado al intentar darte de baja.");
                return "";
            }
        });
    }
}