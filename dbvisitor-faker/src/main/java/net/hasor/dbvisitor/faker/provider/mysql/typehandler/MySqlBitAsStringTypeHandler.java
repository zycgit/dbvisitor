package net.hasor.dbvisitor.faker.provider.mysql.typehandler;
import net.hasor.dbvisitor.types.handler.StringTypeHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySqlBitAsStringTypeHandler extends StringTypeHandler {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, Integer jdbcType) throws SQLException {
        ps.setInt(i, Integer.parseInt(parameter, 2));
    }
}
