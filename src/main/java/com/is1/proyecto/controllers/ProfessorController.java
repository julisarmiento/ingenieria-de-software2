package com.is1.proyecto.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.models.Professor;
import com.is1.proyecto.models.User;
import com.is1.proyecto.services.ProfessorService;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class ProfessorController {

    public static void init() {

        get("/professor/create", (req, res) -> {
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
            return new ModelAndView(model, "professor.mustache");
        }, new MustacheTemplateEngine());

        post("/professor/create", (req, res) -> {
            ProfessorService service = new ProfessorService();
            // Solo administradores pueden crear profesores
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            String nombre = req.queryParams("name");
            String apellido = req.queryParams("surname");
            String correo = req.queryParams("mail");
            String dni = req.queryParams("dni");

            try {
                service.createProfessor(nombre, apellido, correo, dni);
                res.redirect(
                        "/dashboard?message=" + URLEncoder.encode("Profesor creado con éxito", StandardCharsets.UTF_8));
                return null;

            } catch (Exception e) {

                String mensajeError = (e.getMessage() != null) ? e.getMessage()
                        : "Error desconocido en la base de datos";

                // Usamos encode porque si el error tiene espacios, la redirección falla
                String errorEncoded = URLEncoder.encode(mensajeError, StandardCharsets.UTF_8);

                res.redirect("/professor/create?error=" + errorEncoded);
                return null;
            }
        });

        // Formulario de baja
        get("/professor/delete", (req, res) -> {
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/dashboard?error=No tienes permisos de administrador.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            // Mandamos los profesores para llenar el select del HTML
            model.put("professors", Professor.findAll().toMaps());

            // Mensajes de feedback dinámicos
            String successMessage = req.queryParams("message");
            if (successMessage != null){
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null){
                model.put("errorMessage", errorMessage);
            }
            return new ModelAndView(model, "professor_delete.mustache");
        }, new MustacheTemplateEngine());

        post("/professor/delete", (req, res) -> {
            ProfessorService service = new ProfessorService();
            String role = req.session().attribute("role");

            if (role == null || !role.equals("admin")) {
                res.redirect("/professor/delete?error=Accion denegada");
                return null;
            }

            String id = req.queryParams("professor_id");

            if (id == null || id.isEmpty()) {
                res.redirect("/professor/delete?error=ID invalido");
                return null;
            }

            try {
                String name = service.deleteProfessor(id);//Si la eliminacion fue exitosa retorna el nombre del profe eliminado
                res.redirect("/professor/delete?message=Profesor " + name + " eliminado con exito");
                return null;

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/professor/delete?error=Error al intentar eliminar el profesor");
                return null;
            }
        });
    }
}
