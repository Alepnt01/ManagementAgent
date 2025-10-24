package com.managementagent.server.dao;

import com.managementagent.server.ServerSettings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Singleton responsabile di fornire il {@link DataSource} verso SQL Server.
 * Rende esplicita l'adozione del design pattern Singleton nel progetto.
 */
public final class DatabaseConnectionManager {

    private static final DatabaseConnectionManager INSTANCE = new DatabaseConnectionManager();
    private final HikariDataSource dataSource;

    private DatabaseConnectionManager() {
        // Configurazione di HikariCP con i parametri definiti nelle impostazioni del server.
        HikariConfig config = new HikariConfig();
        // URL JDBC di destinazione.
        config.setJdbcUrl(ServerSettings.getDatabaseUrl());
        String authentication = ServerSettings.getDatabaseAuthentication();
        if ("windows".equalsIgnoreCase(authentication)) {
            // Abilito l'autenticazione integrata di Windows per SQL Server.
            config.addDataSourceProperty("integratedSecurity", "true");
            String dllPath = ServerSettings.getSqlJdbcAuthLibrary();
            if (dllPath != null && !dllPath.isBlank()) {
                // Specifico la libreria nativa necessaria al driver JDBC per l'autenticazione Windows.
                System.setProperty("java.library.path", dllPath);
            }
        } else {
            String username = ServerSettings.getDatabaseUsername();
            String password = ServerSettings.getDatabasePassword();
            if (username != null && !username.isBlank()) {
                // Configuro l'utente SQL.
                config.setUsername(username);
            }
            if (password != null && !password.isBlank()) {
                // Configuro la password SQL.
                config.setPassword(password);
            }
        }
        // Seleziono esplicitamente il driver JDBC Microsoft.
        config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        // Limito il numero massimo di connessioni per evitare saturazione del database.
        config.setMaximumPoolSize(10);
        // Assegno un nome leggibile al pool per il debugging.
        config.setPoolName("ManagementAgentPool");
        // Creo l'istanza condivisa di DataSource.
        this.dataSource = new HikariDataSource(config);
    }

    public static DatabaseConnectionManager getInstance() {
        // Restituisce l'unica istanza disponibile (pattern Singleton).
        return INSTANCE;
    }

    public DataSource getDataSource() {
        // Espone il DataSource configurato verso gli altri componenti DAO.
        return dataSource;
    }
}
