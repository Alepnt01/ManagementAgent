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

    public static String getDatabaseAuthentication() {
        return PROPERTIES.getProperty("db.authentication", "sql");
    }

    public static String getSqlJdbcAuthLibrary() {
        return PROPERTIES.getProperty("db.sqljdbc.auth.dll");
    }

    public static String getMailHost() {
        return PROPERTIES.getProperty("mail.smtp.host");
    }

    public static int getMailPort() {
        return Integer.parseInt(PROPERTIES.getProperty("mail.smtp.port", "25"));
    }

    public static boolean isMailStartTlsEnabled() {
        return Boolean.parseBoolean(PROPERTIES.getProperty("mail.smtp.starttls", "false"));
    }

    public static String getMailUsername() {
        return PROPERTIES.getProperty("mail.smtp.username");
    }

    public static String getMailPassword() {
        return PROPERTIES.getProperty("mail.smtp.password");
    }

    public static String getMailFromAddress() {
        return PROPERTIES.getProperty("mail.smtp.from", PROPERTIES.getProperty("mail.smtp.username"));
    }
}
