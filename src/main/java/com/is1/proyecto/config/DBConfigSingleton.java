package com.is1.proyecto.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DBConfigSingleton {

    private static DBConfigSingleton instance;

    private final String dbUrl;
    private final String user;
    private final String pass;
    private final String driver;

    private DBConfigSingleton() {
        Properties props = new Properties();

        // Carga el archivo desde el classpath
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException("No se encontró db.properties en el classpath");
            }
            props.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Error al cargar db.properties", e);
        }

        // Lee cada valor — sin ningún fallback hardcodeado
        this.driver = getRequiredProperty(props, "db.driver");
        this.dbUrl   = getRequiredProperty(props, "db.url");
        this.user    = props.getProperty("db.user", "");  // opcional: SQLite no lo usa
        this.pass    = props.getProperty("db.pass", "");  // opcional: SQLite no lo usa
    }

    /** Lanza excepción clara si falta una propiedad obligatoria */
    private String getRequiredProperty(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Falta la propiedad obligatoria: " + key);
        }
        return value.trim();
    }

    public static synchronized DBConfigSingleton getInstance() {
        if (instance == null) {
            instance = new DBConfigSingleton();
        }
        return instance;
    }

    public String getDbUrl()   { return dbUrl; }
    public String getUser()    { return user; }
    public String getPass()    { return pass; }
    public String getDriver()  { return driver; }
}