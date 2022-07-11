package com.lvonce.openapi.data.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lvonce.openapi.data.DataRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Slf4j
public class DataRepositoryImpl implements DataRepository {

    private static ObjectMapper jsonMapper = new ObjectMapper();
    private static DataSource dataSource = prepareH2DataSource();
    static {
        prepareTable(dataSource);
    }


    private static DataSource prepareH2DataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:dbc2m;DATABASE_TO_UPPER=false;MODE=MYSQL");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", "256");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return new HikariDataSource(config);
    }

    private static void prepareTable(DataSource dataSource) {
        try {
            Connection connection = dataSource.getConnection();
            executeUpdate(connection, "CREATE TABLE pet(id integer, name varchar(256), primary key(id));");
            executeUpdate(connection, "INSERT INTO  pet(id, name) values(23, 'wang');");

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
        }
    }

    private static boolean executeUpdate(Connection connection, String sql) throws Exception {
        PreparedStatement statement = connection.prepareStatement(sql);
        return statement.execute();
    }
    private static ResultSet executeQuery(Connection connection, String sql) throws Exception {
        PreparedStatement statement = connection.prepareStatement(sql);
        return statement.executeQuery();
    }

    @Override
    public ObjectNode executeQuery(String sourceName, String command) throws DataRepositoryException {
        try {
            Connection connection = dataSource.getConnection();
            ResultSet resultSet = executeQuery(connection, command);
            ObjectNode objectNode = jsonMapper.createObjectNode();
            objectNode.putPOJO("data", resultSet);
            return objectNode;
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new DataRepositoryException("", "");
        }
    }

    @Override
    public ObjectNode executeUpdate(String sourceName, String command) throws DataRepositoryException {
        try {
            Connection connection = dataSource.getConnection();
            executeUpdate(connection, command);
            ObjectNode objectNode = jsonMapper.createObjectNode();
            objectNode.putPOJO("data", true);
            return objectNode;
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new DataRepositoryException("", "");
        }
    }
}
