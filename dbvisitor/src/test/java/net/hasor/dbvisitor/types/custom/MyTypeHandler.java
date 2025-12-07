package net.hasor.dbvisitor.types.custom;
import java.sql.*;
import net.hasor.dbvisitor.types.MappedCrossTypes;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

@MappedCrossTypes(javaType = String.class, jdbcType = Types.DATALINK)
@MappedCrossTypes(javaType = StringBuffer.class, jdbcType = Types.VARCHAR)
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
