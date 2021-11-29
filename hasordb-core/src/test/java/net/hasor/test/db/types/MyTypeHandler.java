package net.hasor.test.db.types;
import net.hasor.db.types.MappedCross;
import net.hasor.db.types.MappedJavaTypes;
import net.hasor.db.types.MappedJdbcTypes;
import net.hasor.db.types.handler.AbstractTypeHandler;

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
