package com.vaadin.demo.grid;

import com.vaadin.demo.grid.data.MysqlConnection;
import com.vaadin.ui.*;

/**
 * Form for reading MySQL -connection parameters and
 * passing those to MysqlConnection
 */
public class MysqlConnector extends FormLayout {
    private MysqlConnection source;
    private MySqlEditor parent;
    private TextField address;
    private TextField database;
    private TextField username;
    private PasswordField password;
    private Button connectDb;


    public MysqlConnector(MysqlConnection source, MySqlEditor parent) {
        this.parent = parent;
        this.source = source;
        address = new TextField("Server address (like \"locahost\")");
        database = new TextField("Database name (like \"mydb\")");
        username = new TextField("Username (like \"readonlyuser\")");
        password = new PasswordField("Password (like \"password123\", but please have something else :D)");

        connectDb = new Button("Connect to MySQL db");
        connectDb.setEnabled(false);
        connectDb.addClickListener(clickEvent -> {
                    try {
                        this.source.connect(address.getValue(), database.getValue(), username.getValue(), password.getValue());
                        Notification.show("Connected to database");
                    } catch (Exception ex) {
                        Notification.show("Error connecting to db", ex.toString(), Notification.Type.ERROR_MESSAGE);
                        ex.printStackTrace();
                    } finally {
                        this.parent.checkConnection();
                    }
                }
        );

        address.addValueChangeListener(valueChangeEvent -> checkEnabled());
        database.addValueChangeListener(valueChangeEvent -> checkEnabled());
        username.addValueChangeListener(valueChangeEvent -> checkEnabled());
        password.addValueChangeListener(valueChangeEvent -> checkEnabled());

        addComponents(address,database,username,password,connectDb);

    }

    protected void checkEnabled() {
        connectDb.setEnabled(!address.isEmpty() && !database.isEmpty() && !username.isEmpty() && !password.isEmpty());
    }
}
