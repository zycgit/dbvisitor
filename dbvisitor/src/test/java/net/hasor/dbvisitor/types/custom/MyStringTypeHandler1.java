package net.hasor.dbvisitor.types.custom;

import net.hasor.dbvisitor.types.MappedJavaTypes;
import net.hasor.dbvisitor.types.handler.string.StringTypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJavaTypes(String.class)
public class MyStringTypeHandler1 extends StringTypeHandler {
    private boolean writeMark;
    private boolean readMark;

    public boolean isWriteMark() {
        return writeMark;
    }

    public void setWriteMark(boolean writeMark) {
        this.writeMark = writeMark;
    }

    public boolean isReadMark() {
        return readMark;
    }

    public void setReadMark(boolean readMark) {
        this.readMark = readMark;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, Integer jdbcType) throws SQLException {
        this.writeMark = true;
        super.setNonNullParameter(ps, i, parameter, jdbcType);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        this.readMark = true;
        return super.getNullableResult(rs, columnName);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        this.readMark = true;
        return super.getNullableResult(rs, columnIndex);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        this.readMark = true;
        return super.getNullableResult(cs, columnIndex);
    }
}