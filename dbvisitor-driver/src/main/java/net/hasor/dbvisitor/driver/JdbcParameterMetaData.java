package net.hasor.dbvisitor.driver;

import net.hasor.dbvisitor.dynamic.SqlMode;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

class JdbcParameterMetaData implements ParameterMetaData {
    private final JdbcPreparedStatement statement;
    private final Map<String, JdbcArg>  parameters;

    JdbcParameterMetaData(JdbcPreparedStatement statement, Map<String, JdbcArg> parameters) {
        this.statement = statement;
        this.parameters = parameters == null ? Collections.emptyMap() : parameters;
    }

    protected PreparedStatement getStatement() {
        return this.statement;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        this.statement.checkOpen();
        return this.statement.jdbcConn.adapterConnection().unwrap(iface, this);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        this.statement.checkOpen();
        return this.statement.jdbcConn.adapterConnection().unwrap(iface, this) != null;
    }

    @Override
    public int getParameterCount() {
        return this.parameters.size();
    }

    private JdbcArg checkAndGet(String paramName) throws SQLException {
        JdbcArg jdbcArg = this.parameters.get(paramName);
        if (jdbcArg == null) {
            throw new SQLException("invalid parameter.");
        } else {
            return jdbcArg;
        }
    }

    @Override
    public int getParameterMode(int param) throws SQLException {
        SqlMode mode = this.checkAndGet("arg" + param).getMode();
        switch (mode) {
            case In:
                return parameterModeIn;
            case InOut:
                return parameterModeInOut;
            case Out:
                return parameterModeOut;
            default:
                return parameterModeUnknown;
        }
    }

    @Override
    public String getParameterTypeName(int param) throws SQLException {
        return this.checkAndGet("arg" + param).getType();
    }

    @Override
    public int getParameterType(int param) throws SQLException {
        String typeName = this.checkAndGet("arg" + param).getType();
        return this.statement.jdbcConn.typeSupport().getTypeNumber(typeName);
    }

    @Override
    public String getParameterClassName(int param) throws SQLException {
        String typeName = this.checkAndGet("arg" + param).getType();
        return this.statement.jdbcConn.typeSupport().getTypeClassName(typeName);
    }

    @Override
    public int isNullable(int param) {
        return parameterNullableUnknown;
    }

    @Override
    public boolean isSigned(int param) {
        return false;
    }

    @Override
    public int getPrecision(int param) {
        return 0;
    }

    @Override
    public int getScale(int param) {
        return 0;
    }
}