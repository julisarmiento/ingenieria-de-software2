package com.is1.proyecto.controllers;
import com.is1.proyecto.models.Role;
import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.models.User;
import com.is1.proyecto.services.AuthService;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class AuthController {

    public static void init() {

        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            return new ModelAndView(model, "login.mustache");
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        post("/login", (req, res) -> {
            AuthService service = new AuthService();
            Map<String, Object> model = new HashMap<>();
            String username = req.queryParams("username");
            String plainTextPassword = req.queryParams("password");

           try{
                User ac = service.authenticate(username, plainTextPassword);
                res.status(200);

                req.session(true).attribute("currentUsername", username); // Guarda el nombre de usuario en sesión
                req.session().attribute("userId", ac.getId()); // Guardo el ID de la cuenta en la sesión (útil).
                req.session().attribute("loggedIn", true); 
                req.session().attribute("role", ac.getRole());
                System.out.println("DEBUG: Login exitoso para la cuenta: " + username);
                System.out.println("DEBUG: ID de sesión: " + req.session().id()); //

                res.redirect("/dashboard");
                return null;

            } catch (IllegalArgumentException e){
                res.status(401); 
                System.out.println("DEBUG: Intento de login fallido para la cuenta: " + username);
                model.put("errorMessage", e.getMessage());
                return new ModelAndView(model, "login.mustache");
            }
        }, new MustacheTemplateEngine()); 

        get("/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>(); // Modelo para la plantillo del dashboard

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }

            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            String currentUsername = req.session().attribute("currentUsername");
            Boolean loggedIn = req.session().attribute("loggedIn");
            Role role = req.session().attribute("role");

            if (currentUsername == null || loggedIn == null || !loggedIn) {
                System.out.println("DEBUG: Acceso no autorizado a /dashboard. Redirigiendo a /login.");
                res.redirect("/?error=Debes iniciar sesión para acceder a esta pagina.");
                return null; 
            }

            model.put("username", currentUsername);
            model.put("isAdmin", role == Role.ADMIN);
            model.put("isStudent", role == Role.ESTUDIANTE);
            model.put("isProfesor", role == Role.PROFESOR);
            return new ModelAndView(model, "dashboard.mustache");
        }, new MustacheTemplateEngine());

        get("/logout", (req, res) -> {
            req.session().invalidate();
            System.out.println("DEBUG: Sesión cerrada. Redirigiendo a /login."); //
            res.redirect("/");
            return null;
        });
    }

}
