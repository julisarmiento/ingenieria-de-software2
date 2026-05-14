package com.is1.proyecto.config;

import org.javalite.activejdbc.Base;

public class DatabaseManager {

    private static final DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

    public static void openConnection() {
        // Usamos el singleton para abrir la conexión, obtener los datos y evitar rutas
        // fijas
        Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
    }

    public static void closeConnection() {
        Base.close();
    }

}
