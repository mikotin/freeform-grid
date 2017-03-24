package com.vaadin.demo.grid.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * Simple data-row encapsulating key-value -pairs from resultset
 * (of given fields)
 */
public class SqlRowData {

    private HashMap<String, String> data;

    public SqlRowData(ResultSet rs, List<String> fields) throws SQLException {
        data = new HashMap<>();
        for (String f  : fields) {
            data.put(f, rs.getString(f));
        }
    }

    public String getValue(String field) {
        return data.get(field);
    }

}
