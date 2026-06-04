package com.is1.proyecto.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.models.Professor;
import com.is1.proyecto.services.ResetPasswordService;

import java.time.LocalDateTime;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class ResetPasswordController {
    
    public static void init(){

        get("/password/reset", (req, res) -> {

            String id = req.queryParams("id");
            String token = req.queryParams("token");

            try {

                Professor prof = Professor.findFirst("id = ?", id);

                if (token == null || token.isEmpty()) {
                    throw new Exception("El token es invalido");
                }

                if(prof.getExpireDateToken().isBefore(LocalDateTime.now())){
                    throw new Exception("El token ya EXPIRO");
                }

                Map<String, Object> model = new HashMap<>();
                model.put("id", id);
                model.put("token", token); 
                return new ModelAndView(model, "resetPassword.mustache");

            } catch (IllegalArgumentException e) {
                res.redirect("/login?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return null;
            }
        }, new MustacheTemplateEngine());

        post("/password/reset", (req, res) -> {
            ResetPasswordService service = new ResetPasswordService();

            String id = req.queryParams("id_profesor");
            String token = req.queryParams("token");
            String newPass = req.queryParams("contrasenia_nueva");

            try {

                service.createPassword(id, newPass);

                res.redirect("/?message=" + URLEncoder.encode("Contraseña actualizada con éxito. Ya puedes iniciar sesión.", StandardCharsets.UTF_8));
                return "";
            } catch (Exception e) {
                res.redirect("/password/reset?token=" + token + "&error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                return "";
            }
        });
    }
    
}
