package com.managementagent.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility for reading server-level configuration values.
 */
public final class ServerSettings {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream inputStream = ServerSettings.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                PROPERTIES.load(inputStream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load application.properties", e);
        }
    }

    private ServerSettings() {
    }

    public static int getPort() {
        return Integer.parseInt(PROPERTIES.getProperty("server.port", "7070"));
    }

    public static String getDatabaseUrl() {
        return PROPERTIES.getProperty("db.url");
    }

    public static String getDatabaseUsername() {
        return PROPERTIES.getProperty("db.username");
    }

    public static String getDatabasePassword() {
        return PROPERTIES.getProperty("db.password");
    }
}
