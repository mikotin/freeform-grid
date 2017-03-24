package com.vaadin.demo.grid.data;

import com.vaadin.v7.data.util.sqlcontainer.SQLContainer;
import com.vaadin.v7.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.v7.data.util.sqlcontainer.query.FreeformQuery;

/**
 * Simple implementation of MysqlConnection for SQLContainer
 *
 */
public class SimpleSqlContainer implements MysqlConnection {
    protected SQLContainer container;
    protected SimpleJDBCConnectionPool connectionPool;


    @Override
    public void connect(String address, String database, String user, String password) throws Exception {
        if (connectionOpen()) {
            close();
        }
        connectionPool = new SimpleJDBCConnectionPool(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://" + address + "/" + database, user, password, 2, 5);
    }

    @Override
    public void close() {
        if (connectionPool != null) {
            connectionPool.destroy();
        }
        connectionPool = null;
        container = null;
    }

    @Override
    public void initialize(String sql) throws Exception {
        container = new SQLContainer(new FreeformQuery(sql,
                connectionPool));
    }

    @Override
    public boolean connectionOpen() {
        return connectionPool != null;
    }

    public SQLContainer getContainer() {
        return container;
    }
}
