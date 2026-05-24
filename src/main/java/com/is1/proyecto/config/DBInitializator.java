package com.is1.proyecto.config;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

import org.javalite.activejdbc.Base;
import org.mindrot.jbcrypt.BCrypt;

import com.is1.proyecto.models.User;

public class DBInitializator {
    public static void createTablesIfNotExist() {
        try {
            Connection conn = Base.connection();
            var meta = conn.getMetaData();
            var rs = meta.getTables(null, null, "users", null);

            // 1. Lee el scheme.sql si no existe la tabla
            if (!rs.next()) {
                System.out.println("BD vacía. Creando tablas con scheme.sql...");
                String sqlScript = new String(Files.readAllBytes(Paths.get("src/main/resources/scheme.sql")));
                String[] queries = sqlScript.split(";");

                try (Statement stmt = conn.createStatement()) {
                    for (String query : queries) {
                        if (!query.trim().isEmpty()) {
                            stmt.execute(query);
                        }
                    }
                }
                System.out.println("Tablas creadas con éxito.");
            }

            // 2. Sembrado del Admin con contraseña HASHEADA
            if (User.findFirst("name = ?", "admin") == null) {
                System.out.println("Creando usuario admin...");

                // Hasheamos la contraseña "admin123" antes de guardarla
                String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt());

                User admin = new User();
                admin.set("name", "admin");
                admin.set("password", hashedPassword); // Guardamos el hash, NO el texto plano
                admin.set("role", "admin");
                admin.saveIt();

                System.out.println("Admin creado y hasheado correctamente.");
            }

        } catch (Exception e) {
            System.err.println("Error en DBInitializer:");
            e.printStackTrace();
        }
    }
}
