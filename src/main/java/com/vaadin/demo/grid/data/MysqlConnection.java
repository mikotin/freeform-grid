package com.vaadin.demo.grid.data;

/**
 * Simple interface for MysqlEditor to use
 */
public interface MysqlConnection {

    void connect(String address, String database, String user, String password) throws Exception;

    void close();

    void initialize(String sql) throws Exception;

    boolean connectionOpen();
}
