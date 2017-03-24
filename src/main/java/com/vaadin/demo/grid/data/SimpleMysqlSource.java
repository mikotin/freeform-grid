package com.vaadin.demo.grid.data;

import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;

import java.net.URLEncoder;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple sql source using free-form sql
 *
 * Is a bit bloated because wanted to support ordering & limiting
 * in order to get full strength of Grid in usage. Those - of course -
 * are not mandatory. Supports only MySQL dialect + has flaws within.
 * (Not really parsing the incoming query to make it "safe")
 */
public class SimpleMysqlSource implements MysqlConnection {

    private Connection conn = null;
    private Statement stmt = null;
    /**
     * The raw query
     */
    private String query = null;
    /**
     * Generated count-query out of query
     */
    private String countQuery = null;
    /**
     * Order-part of query (simple string, like "´name` ASC, `othername` ASC")
     */
    private String order = null;
    /**
     * List of column names
     */
    private List<String> fields;

    public SimpleMysqlSource() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        clear();
    }

    public boolean connectionOpen() {
        try {
            return !(conn == null || conn.isClosed());
        } catch (Exception e) {
            return false;
        }
    }


    @Override
    public void connect(String address, String database, String user, String password) throws Exception {
        if (connectionOpen()) {
            conn.close();
        }
        clear();
        String connectionString = "jdbc:mysql://" + address + "/" + database + "?characterEncoding=utf8" +
                (!user.isEmpty() ? "&user=" + URLEncoder.encode(user, "UTF-8") : "") +
                (!password.isEmpty() ? "&password=" + URLEncoder.encode(password, "UTF-8") : "");
        conn = DriverManager.getConnection(connectionString);
        stmt = conn.createStatement();
    }

    @Override
    public void close() {
        try {
            if (connectionOpen()) {
                if (stmt != null && !stmt.isClosed()) {
                    stmt.close();
                }
                conn.close();
            }
        } catch (SQLException e) {
            // closing might fail if problem in connection, so we can ignore exception
        }
        clear();
    }

    @Override
    public void initialize(String sql) throws Exception {
        //  clear old data
        clear();
        // clear sql
        sql = sql.trim();
        if (!sql.toLowerCase().startsWith("select")) {
            throw new SQLException("Only SELECT query supported");
        }
        this.query = clearSql(sql);
        // get count
        if (this.query.toLowerCase().indexOf("group by") < 0) {
            // make count the easy way if possible
            this.countQuery = "SELECT COUNT(*) AS total_count " + this.query.substring(this.query.toLowerCase().indexOf("from "));
        } else {
            // in case of group use sub-query for counting
            // (of course the group by could be in sub-query, so just string
            //  matching gives false-positives, but it's 'close enough')
            this.countQuery = "SELECT COUNT(*) AS total_count FROM (" + this.query + ") AS sub_query";
        }

        // initialize fields
        ResultSet rs =  stmt.executeQuery(getQueryString(1, 0));
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            fields.add(rs.getMetaData().getColumnLabel(i));
        }
    }

    public String getQuery() {
        return query;
    }

    public List<String> getFields() {
        return fields;
    }

    public List<SqlRowData> fetch(List<QuerySortOrder> orders, int limit, int offset) {
        setSortOrders(orders);
        return fetch(limit, offset);
    }

    public ArrayList<SqlRowData> fetch(int limit, int offset) {
        ArrayList<SqlRowData> rows = new ArrayList<>();
        try {
            ResultSet rs =  stmt.executeQuery(getQueryString(limit, offset));
            while(rs.next()) {
                rows.add(new SqlRowData(rs, fields));
            }
        } catch (SQLException e) {
            //error coming in tested query, so something went bad in connection or so, better close
            close();
        }
        return rows;
    }

    /**
     * Gets the row-count of query
     *
     * @return
     */
    public int count() {
        return totalCount(countQuery);
    }


    public void setSortOrders(List<QuerySortOrder> list) {
        if (list.size() > 0) {
            this.order = " ORDER BY " + list.stream()
                    .map(querySortOrder -> "`" + querySortOrder.getSorted() + "`" + (querySortOrder.getDirection().equals(SortDirection.ASCENDING) ? " ASC" : " DESC"))
                    .collect(Collectors.joining(", "));
        } else {
            this.order = "";
        }
    }

    /**
     * Rather harsh way to clear limit and order by out of the query:
     * trying if they already exist by running test-runs with limit and
     * order. If fail (broken sql), then remove section. This sounds consuming
     * and harsh, but is rather easy and fail-safe for sub-query limits and
     * orders, for those should never be removed.
     *
     * @param sql
     * @return
     * @throws SQLException
     */
    private String clearSql(String sql) throws SQLException {
        // Removing limit if needed
        if (!testSql(sql + " LIMIT 1")) {
            sql = sql.substring(0, sql.toLowerCase().lastIndexOf("limit "));
        }
        ResultSet rs = stmt.executeQuery(sql + " LIMIT 1");
        String col = rs.getMetaData().getColumnLabel(1);
        rs.close();
        // removing order by if needed
        if (!testSql(sql + " ORDER BY `" + col + "` ASC LIMIT 1")) {
            sql = sql.substring(0, sql.toLowerCase().lastIndexOf("order by "));
        }
        return sql;
    }

    /**
     * Simply executes sql to db and returns true if no exceptions were thrown
     *
     * @param sql
     * @return
     */
    private boolean testSql(String sql) {
        try {
            stmt.executeQuery(sql);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }


    /**
     * Gets the full query string with order, limit and offset
     *
     * @param limit
     * @param offset
     * @return
     */
    private String getQueryString(int limit, int offset) {
        if (this.order == null) {
            this.order = "";
        }
        return limit(this.query + " " + this.order, limit, offset);
    }

    /**
     * Adds limit to given sql-select -string
     *
     * @param sql The sql (such as "SELECT * FROM fubar WHERE fubar.snafu < 5.1212")
     * @param limit
     * @param offset
     * @return The sql with concatenated limit & offset, as: "SELECT * FROM fubar WHERE fubar.snafu < 5.1212 LIMIT 100 OFFSET 100")
     */
    private String limit(String sql, int limit, int offset) {
        if (limit > 0) {
            sql += " LIMIT " + limit;
        }
        if (offset > 0) {
            sql += " OFFSET " + offset;
        }
        return sql;
    }


    /**
     * Simple method to execute query and try to get (integer) "total_count" from resultset
     *
     * @param sql Raw sql-query, should be this.countQuery + limit if needed
     * @return
     */
    private int totalCount(String sql) {
        try {
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            return rs.getInt("total_count");
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Clears query-specific data
     */
    private void clear() {
        fields = new ArrayList<>();
        this.order = "";
        this.countQuery = "";
        this.query = "";
    }

}
