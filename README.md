# Free form sql to grid

A playground for running free-form sql and get result into a grid.
Both old SqlContainer + Grid and new Grid<> with simple JDBC string query

## Usage

Launch UI, navigate to localhost:8080 (or so), connect with db-connection parameters, write your select -query and see results in a grid (SimpleSqlContainer doesn't support lazy-load or sorting - the SimpleMysqlSource does).

### Running

Template for this is from vaadin-archetype-application so just run

```
mvn jetty:run
```

to get UI up

### The beef

MysqlEditor implements both SimpleMysqlSource (basic jdbc -query with very little intelligence to support grid sort + limit) and SimpleSqlContainer (wrapper for SqlContainer). Bot implementations are very simple, check from MysqlEditor.java line (about) 80 =>

### Note

Please remember that queries are not really checked to be safe, so you can inject bad stuff within.

