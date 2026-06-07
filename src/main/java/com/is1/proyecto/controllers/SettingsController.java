package com.is1.proyecto.controllers;

import com.is1.proyecto.models.Role;
import com.is1.proyecto.models.User;
import com.is1.proyecto.models.Professor;
import com.is1.proyecto.models.Student;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import java.util.HashMap;
import java.util.Map;
import static spark.Spark.get;
import static spark.Spark.post;

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
                        if (student != null) student.delete();
                    } 
                    else if (role == Role.PROFESOR) {
                        Professor prof = Professor.findById(user.getId()); 
                        if (prof != null) prof.delete();
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
    }
}