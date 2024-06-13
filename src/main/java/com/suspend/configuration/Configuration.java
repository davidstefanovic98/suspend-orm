package com.suspend.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    private static final String CONFIG_FILE = "application.properties";

    public static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new IOException("Could not find configuration file: " + CONFIG_FILE);
            }
        }
        return properties;
    }
}
