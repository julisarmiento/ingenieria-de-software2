package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

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

                // Gestion de sesión
                req.session(true).attribute("currentUsername", username); // Guarda el nombre de usuario en sesión
                req.session().attribute("userId", ac.getId()); // Guardo el ID de la cuenta en la sesión (útil).
                req.session().attribute("loggedIn", true); // Establece una bandera para indicar que el usuario está
                                                           // logueado.
                req.session().attribute("role", ac.getString("role")); // Modificamos el login para guardar el "role" en
                                                                       // sesion.

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

            // Intenta obtener el nombre de usuario y la bandera de login de la sesión.
            String currentUsername = req.session().attribute("currentUsername");
            Boolean loggedIn = req.session().attribute("loggedIn");
            String role = req.session().attribute("role"); // obtenemos la flag de admin.

            // 1.Verificamos que el usuario ha iniciado sesion.
            // Si no hay un nombre de usuario en la sesión, la flag es falsa o nula,
            // significa que el usuario no está logueado o su sesion expiró.
            if (currentUsername == null || loggedIn == null || !loggedIn) {
                System.out.println("DEBUG: Acceso no autorizado a /dashboard. Redirigiendo a /login.");
                // Redirigimos a /login con un mensaje de error.
                res.redirect("/?error=Debes iniciar sesión para acceder a esta pagina.");
                return null; // Importante retorna null despues de una redirección.
            }

            model.put("username", currentUsername);
            model.put("isAdmin", "admin".equals(role)); // añade la plantilla admin si es que cumple.

            // Renderizamos la plantilla del dashboard con el nombre de usuario.
            return new ModelAndView(model, "dashboard.mustache");
        }, new MustacheTemplateEngine());

        get("/logout", (req, res) -> {
            // Invalida completamente la sesión del usuario.
            // Esto elimina todos los atributos guardados en la sesión y la marca como
            // inválida.
            // La cookie JSESSIONID en el navegador también será gestionada para
            // invalidarse.
            req.session().invalidate();

            System.out.println("DEBUG: Sesión cerrada. Redirigiendo a /login."); //

            // Redirigo al usuario a la pagin login con un mensaje de exito.
            res.redirect("/");
            return null;
        });
    }

}
