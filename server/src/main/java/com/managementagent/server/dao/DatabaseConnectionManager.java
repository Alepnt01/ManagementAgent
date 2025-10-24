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
        config.setUsername(ServerSettings.getDatabaseUsername());
        config.setPassword(ServerSettings.getDatabasePassword());
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
