package com.vaadin.demo.grid;

import com.vaadin.demo.grid.data.*;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

/**
 * UI that uses a MysqlConnection (either SimpleSqlContainer,
 * or SimpleMysqlSource) for connecting to db, running queries
 * and show result in a grid
 */
public class MySqlEditor extends VerticalLayout {

    protected final TextArea query = new TextArea();
    protected Button closeConnection;
    protected Button executeQuery;
    protected MysqlConnector mysqlConnector;

    protected Grid<SqlRowData> grid;
    private com.vaadin.v7.ui.Grid oldGrid;

    private SimpleMysqlSource mysqlSource;
    private SimpleSqlContainer sqlContainer;

    private MysqlConnection connection;


    public MySqlEditor(ConnectionType type) {

        query.setCaption("SQL-query:");
        query.setSizeFull();
        query.setHeight("250px");

        grid = new Grid<>();
        grid.setSizeFull();

        oldGrid = new com.vaadin.v7.ui.Grid();
        oldGrid.setSizeFull();

        sqlContainer = new SimpleSqlContainer();
        Label header = new Label();
        header.addStyleName(ValoTheme.LABEL_H2);

        try {
            mysqlSource = new SimpleMysqlSource();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (type.equals(ConnectionType.SQLCONTAINER)) {
            connection = sqlContainer;
            header.setValue("Free form SqlContainer Grid (V7)");
        } else {
            header.setValue("Free form JDBC Grid (V8)");
            connection = mysqlSource;
        }

        addComponent(header);


        mysqlConnector = new MysqlConnector(connection, this);
        mysqlConnector.setSizeFull();
        addComponent(mysqlConnector);

        closeConnection = new Button("Close connection");
        closeConnection.addClickListener(clickEvent -> {
            connection.close();
            clearData();
            checkConnection();
        });

        addComponent(closeConnection);

        executeQuery = new Button("Execute query");
        executeQuery.setEnabled(false);
        executeQuery.addClickListener( e -> {
            try {
                connection.initialize(query.getValue());

                if (connection instanceof SimpleMysqlSource) {
                    // Vaadin 8, grid from random source
                    removeComponent(grid);
                    grid = new Grid<>();
                    grid.setSizeFull();
                    // initialize fields
                    for (String f : mysqlSource.getFields()) {
                        grid.addColumn(sqlRowData -> sqlRowData.getValue(f)).setId(f).setCaption(f).setSortable(true);
                    }
                    // set fetch & count, support ordering & lazy loading
                    grid.setDataProvider((sortorder, offset, limit) -> mysqlSource.fetch(sortorder, limit, offset).stream(),
                            () -> mysqlSource.count());

                    addComponent(grid);
                } else if (connection instanceof SimpleSqlContainer) {
                    // Vaadin 7, grid from SqlContainer
                    removeComponent(oldGrid);
                    oldGrid = new com.vaadin.v7.ui.Grid();
                    oldGrid.setSizeFull();
                    // use container
                    oldGrid.setContainerDataSource(sqlContainer.getContainer());

                    addComponent(oldGrid);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("Error parsing sql", ex.toString(), Notification.Type.ERROR_MESSAGE);
            }
        });


        addComponents(query, executeQuery, grid, oldGrid);
        checkConnection();
    }

    public void checkConnection() {
        System.out.println();
        setConnectionUI(connection.connectionOpen());

    }

    private void setConnectionUI(boolean connected) {
        executeQuery.setEnabled(connected);
        query.setEnabled(connected);
        grid.setEnabled(connected);
        oldGrid.setEnabled(connected);
        mysqlConnector.setVisible(!connected);
        closeConnection.setVisible(connected);
        grid.setVisible(connection instanceof SimpleMysqlSource);
        oldGrid.setVisible(connection instanceof SimpleSqlContainer);
    }

    /**
     * clear query-texts and grids
     */
    private void clearData() {
        query.setValue("");
        removeComponent(grid);
        removeComponent(oldGrid);

        grid = new Grid<>();
        grid.setSizeFull();
        addComponent(grid);

        oldGrid = new com.vaadin.v7.ui.Grid();
        oldGrid.setSizeFull();
        addComponent(oldGrid);
        checkConnection();
    }

    public enum ConnectionType {
        SQLCONTAINER, PLAINSQL
    }

}
