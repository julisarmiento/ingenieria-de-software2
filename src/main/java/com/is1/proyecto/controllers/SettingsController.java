package com.is1.proyecto.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.models.Professor;
import com.is1.proyecto.models.Role;
import com.is1.proyecto.models.Student;
import com.is1.proyecto.models.User;
import com.is1.proyecto.services.StudentService;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class SettingsController {

    public static void init() {

        get("/settings", (req, res) -> {
            String username = req.session().attribute("currentUsername");
            if (username == null) {
                res.redirect("/?error=Debes iniciar sesión primero.");
                return null;
            }
            Role role = req.session().attribute("role");
            Map<String, Object> model = new HashMap<>();
            model.put("isStudent", role == Role.ESTUDIANTE);
            model.put("isProfesor", role == Role.PROFESOR);
            return new ModelAndView(model, "settings.mustache");
        }, new MustacheTemplateEngine());

        post("/settings/delete-account", (req, res) -> {
            String currentUsername = req.session().attribute("currentUsername");
            if (currentUsername == null) {
                res.redirect("/?error=Debes iniciar sesión primero.");
                return "";
            }

            try {
                User user = User.findFirst("name = ?", currentUsername);
                if (user != null) {
                    Role role = req.session().attribute("role");

                    if (role == Role.ESTUDIANTE) {
                        Student student = Student.findById(user.getId());
                        if (student != null)
                            student.delete();
                    } else if (role == Role.PROFESOR) {
                        Professor prof = Professor.findById(user.getId());
                        if (prof != null)
                            prof.delete();
                    }

                    user.delete();
                }
                req.session().invalidate();
                res.redirect("/?message=" + java.net.URLEncoder.encode("Cuenta eliminada correctamente.", "UTF-8"));
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/settings?error=Error al eliminar la cuenta.");
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
            return new ModelAndView(new HashMap<>(), "career_unenroll.mustache");
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
                res.redirect("/dashboard?message=" + URLEncoder.encode(
                        "Te has dado de baja de la carrera exitosamente.", StandardCharsets.UTF_8));
                return null;
            } catch (IllegalArgumentException e) {
                res.redirect("/dashboard?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/dashboard?error=Error inesperado al intentar darte de baja.");
                return null;
            }
        });
    }
}