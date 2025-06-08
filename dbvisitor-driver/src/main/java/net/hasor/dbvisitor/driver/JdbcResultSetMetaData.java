package net.hasor.dbvisitor.driver;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

class JdbcResultSetMetaData implements ResultSetMetaData {
    private final JdbcStatement    statement;
    private final List<JdbcColumn> columns;

    JdbcResultSetMetaData(JdbcStatement statement, List<JdbcColumn> columns) {
        this.statement = statement;
        this.columns = columns;
    }

    protected Statement getStatement() {
        return this.statement;
    }

    protected ResultSet getResultSet() {
        return null; // TODO
    }

    protected JdbcColumn column(int index) throws SQLException {
        if (index < 0 || index >= columns.size()) {
            throw new SQLException("Invalid column index [" + index + "]");
        } else {
            return this.columns.get(index);
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        this.statement.checkOpen();
        return statement.jdbcConn.adapterConnection().unwrap(iface, this);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        this.statement.checkOpen();
        return statement.jdbcConn.adapterConnection().unwrap(iface, this) != null;
    }

    @Override
    public int getColumnCount() {
        return this.columns.size();
    }

    @Override
    public boolean isAutoIncrement(int column) {
        return false;
    }

    @Override
    public boolean isCaseSensitive(int column) {
        return false;
    }

    @Override
    public boolean isSearchable(int column) {
        return false;
    }

    @Override
    public boolean isCurrency(int column) {
        return false;
    }

    @Override
    public int isNullable(int column) {
        return 0;
    }

    @Override
    public boolean isSigned(int column) {
        return false;
    }

    @Override
    public int getColumnDisplaySize(int column) {
        return 0;
    }

    @Override
    public boolean isReadOnly(int column) {
        return false;
    }

    @Override
    public boolean isWritable(int column) {
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(int column) {
        return false;
    }

    @Override
    public int getPrecision(int column) {
        return 0;
    }

    @Override
    public int getScale(int column) {
        return 0;
    }

    //

    @Override
    public String getCatalogName(int column) throws SQLException {
        return this.column(column).catalog;
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        return this.column(column).schema;
    }

    @Override
    public String getTableName(int column) throws SQLException {
        return this.column(column).table;
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return this.column(column).name;
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return this.column(column).name;
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        return this.statement.jdbcConn.typeSupport().getTypeNumber(this.column(column).type);
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return this.column(column).type;
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return this.statement.jdbcConn.typeSupport().getTypeClassName(this.column(column).type);
    }
}