package com.is1.proyecto.config;

import org.javalite.activejdbc.Base;

public class DatabaseManager {

    private static final DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

    public static void openConnection() {
        if(!Base.hasConnection()){
            Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
        }
    }

    public static void closeConnection() {
        if(Base.hasConnection()){
            Base.close();
        }
    }

}
