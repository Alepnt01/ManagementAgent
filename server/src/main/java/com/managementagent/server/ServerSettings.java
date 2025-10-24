package com.managementagent.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utilità per leggere e centralizzare i valori di configurazione del server.
 */
public final class ServerSettings {

    private static final Properties PROPERTIES = new Properties();

    static {
        // Carica il file application.properties presente nel classpath.
        try (InputStream inputStream = ServerSettings.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                // Popola la mappa Properties con tutte le chiavi configurate.
                PROPERTIES.load(inputStream);
            }
        } catch (IOException e) {
            // In caso di errore interrompo il bootstrap perché le impostazioni sono fondamentali.
            throw new IllegalStateException("Unable to load application.properties", e);
        }
    }

    private ServerSettings() {
    }

    public static int getPort() {
        // Porta HTTP del server con valore di default 7070.
        return Integer.parseInt(PROPERTIES.getProperty("server.port", "7070"));
    }

    public static String getDatabaseUrl() {
        // Stringa di connessione JDBC verso SQL Server.
        return PROPERTIES.getProperty("db.url");
    }

    public static String getDatabaseUsername() {
        // Nome utente da utilizzare con l'autenticazione SQL.
        return PROPERTIES.getProperty("db.username");
    }

    public static String getDatabasePassword() {
        // Password da utilizzare con l'autenticazione SQL.
        return PROPERTIES.getProperty("db.password");
    }

    public static String getDatabaseAuthentication() {
        // Modalità di autenticazione (sql o windows) letta dalla configurazione.
        return PROPERTIES.getProperty("db.authentication", "sql");
    }

    public static String getSqlJdbcAuthLibrary() {
        // Percorso della libreria nativa necessaria per l'autenticazione integrata di Windows.
        return PROPERTIES.getProperty("db.sqljdbc.auth.dll");
    }

    public static String getMailHost() {
        // Host SMTP configurato per l'invio delle email.
        return PROPERTIES.getProperty("mail.smtp.host");
    }

    public static int getMailPort() {
        // Porta SMTP, con fallback al valore standard 25.
        return Integer.parseInt(PROPERTIES.getProperty("mail.smtp.port", "25"));
    }

    public static boolean isMailStartTlsEnabled() {
        // Indica se il client SMTP deve utilizzare STARTTLS.
        return Boolean.parseBoolean(PROPERTIES.getProperty("mail.smtp.starttls", "false"));
    }

    public static String getMailUsername() {
        // Utente autenticato presso il server SMTP.
        return PROPERTIES.getProperty("mail.smtp.username");
    }

    public static String getMailPassword() {
        // Password utilizzata per autenticarsi con il server SMTP.
        return PROPERTIES.getProperty("mail.smtp.password");
    }

    public static String getMailFromAddress() {
        // Indirizzo mittente predefinito per le email in uscita.
        return PROPERTIES.getProperty("mail.smtp.from", PROPERTIES.getProperty("mail.smtp.username"));
    }
}
