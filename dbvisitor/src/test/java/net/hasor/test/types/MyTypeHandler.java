package net.hasor.test.types;
import net.hasor.dbvisitor.types.MappedCross;
import net.hasor.dbvisitor.types.MappedJavaTypes;
import net.hasor.dbvisitor.types.MappedJdbcTypes;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

import java.sql.*;

@MappedCross(javaTypes = @MappedJavaTypes(String.class), jdbcType = @MappedJdbcTypes(Types.DATALINK))
@MappedCross(javaTypes = @MappedJavaTypes(StringBuffer.class), jdbcType = @MappedJdbcTypes(Types.VARCHAR))
public class MyTypeHandler extends AbstractTypeHandler<String> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, Integer jdbcType) throws SQLException {

    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return null;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }
}
