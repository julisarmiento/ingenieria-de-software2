package com.is1.proyecto.controllers;

import java.util.HashMap;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import com.is1.proyecto.models.Professor;
import com.is1.proyecto.models.User;

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
            // Solo administradores pueden crear profesores
            String role = req.session().attribute("role");
            if (role == null || !role.equals("admin")) {
                res.redirect("/?error=No tienes permiso para realizar esta accion.");
                return null;
            }

            // Campos del formulario
            String nombre = req.queryParams("nombre");
            String apellido = req.queryParams("apellido");
            String correo = req.queryParams("correo");
            String dni = req.queryParams("dni");

            // Validaciones básicas
            if (nombre == null || nombre.isEmpty() ||
                    apellido == null || apellido.isEmpty() ||
                    correo == null || correo.isEmpty() ||
                    dni == null || dni.isEmpty()) {

                res.redirect("/professor/create?error=Faltan campos obligatorios.");
                return null;
            }

            if (!correo.contains("@") || !correo.contains(".")) {
                res.redirect("/professor/create?error=Correo no valido.");
                return null;
            }

            try {
                // Verificar DNI único
                if (Professor.findFirst("dni = ?", dni) != null) {
                    res.redirect("/professor/create?error=El DNI ya esta registrado.");
                    return null;
                }

                // Verificar correo único
                if (Professor.findFirst("correo = ?", correo) != null) {
                    res.redirect("/professor/create?error=El correo ya esta registrado.");
                    return null;
                }

                // nombre de usuario = inicial nombre + apellido
                String username = nombre.substring(0, 1).toUpperCase() +
                        apellido;

                // Contraseña = últimos 4 dígitos del DNI
                String last4 = dni.substring(dni.length() - 4);
                String hashedPassword = BCrypt.hashpw(last4, BCrypt.gensalt());

                // insercion para user
                User newUser = new User();
                newUser.set("name", username);
                newUser.set("password", hashedPassword);
                newUser.set("role", "professor");
                newUser.saveIt();

                int userId = newUser.getInteger("id");

                // insercion para profesor
                Professor prof = new Professor();
                prof.set("id", userId);
                prof.set("nombre", nombre);
                prof.set("apellido", apellido);
                prof.set("correo", correo);
                prof.set("dni", dni);
                prof.insert();

                res.redirect("/dashboard?message=Profesor creado. Usuario: "
                        + username + " Contrasenia: " + last4);
                return "";

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/professor/create?error=Error inesperado.");
                return "";
            }
        });

    }
}
