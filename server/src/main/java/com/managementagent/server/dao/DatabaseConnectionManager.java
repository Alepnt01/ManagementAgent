package com.managementagent.server.dao;

import com.managementagent.server.ServerSettings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Singleton responsible for providing the SQL Server {@link DataSource}.
 * Demonstrates the Singleton design pattern explicitly within the project.
 */
public final class DatabaseConnectionManager {

    private static final DatabaseConnectionManager INSTANCE = new DatabaseConnectionManager();
    private final HikariDataSource dataSource;

    private DatabaseConnectionManager() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(ServerSettings.getDatabaseUrl());
        String authentication = ServerSettings.getDatabaseAuthentication();
        if ("windows".equalsIgnoreCase(authentication)) {
            config.addDataSourceProperty("integratedSecurity", "true");
            String dllPath = ServerSettings.getSqlJdbcAuthLibrary();
            if (dllPath != null && !dllPath.isBlank()) {
                System.setProperty("java.library.path", dllPath);
            }
        } else {
            String username = ServerSettings.getDatabaseUsername();
            String password = ServerSettings.getDatabasePassword();
            if (username != null && !username.isBlank()) {
                config.setUsername(username);
            }
            if (password != null && !password.isBlank()) {
                config.setPassword(password);
            }
        }
        config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        config.setMaximumPoolSize(10);
        config.setPoolName("ManagementAgentPool");
        this.dataSource = new HikariDataSource(config);
    }

    public static DatabaseConnectionManager getInstance() {
        return INSTANCE;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
