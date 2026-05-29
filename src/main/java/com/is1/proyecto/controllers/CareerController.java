package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.is1.proyecto.models.Career;
import com.is1.proyecto.models.Faculty;

import com.is1.proyecto.services.CareerService;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CareerController {

    public static void init() {

        CareerService service = new CareerService();

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
            model.put("faculties", Faculty.findAll().toMaps());

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
            String name = req.queryParams("nombre_carrera");
            String facultyId = req.queryParams("identificador_facultad"); 

            try {
                service.createCareer(name, facultyId);
                String mensajeCodificado = URLEncoder.encode("Carrera " + name + 
                    " creada con exito.", StandardCharsets.UTF_8); //Codificamos el mensaje en 
                                                                   //caso de que el nombre de la 
                                                                   //carrera llegase a tener acentos
                res.redirect("/dashboard?message=" + mensajeCodificado);
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/career/create?error=Error inesperado al guardar.");
                return "";
            }
        });

        get("/career/delete", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
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
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para acceder a esta pagina.");
                return null;
            }

            // Verificar identificador único
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
    }
}