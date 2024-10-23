package com.suspend.connection;

import com.suspend.configuration.Configuration;
import com.suspend.core.exception.SuspendException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionManager {

    private static ConnectionManager instance = null;
    private static final String DRIVER_CLASS_PROP = "suspend.datasource.driverClassName";
    private static final String URL_PROP = "suspend.datasource.url";
    private static final String USERNAME_PROP = "suspend.datasource.username";
    private static final String PASSWORD_PROP = "suspend.datasource.password";
    private static final String DIALECT_PROP = "suspend.datasource.dialect";

    private final Properties properties;
    private Connection connection;

    public ConnectionManager() {
        properties = Configuration.getInstance().getProperties();
    }

    public static synchronized ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null) {
                String driverClassName = properties.getProperty(DRIVER_CLASS_PROP);
                String url = properties.getProperty(URL_PROP);
                String username = properties.getProperty(USERNAME_PROP);
                String password = properties.getProperty(PASSWORD_PROP);

                Class.forName(driverClassName);
                connection = DriverManager.getConnection(url, username, password);
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new SuspendException("Could not connect to database");
        }
        return connection;
    }

    public String getDialect() {
        return properties.getProperty(DIALECT_PROP);
    }
}
