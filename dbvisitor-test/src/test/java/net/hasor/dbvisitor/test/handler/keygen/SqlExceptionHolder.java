package net.hasor.dbvisitor.test.handler.keygen;

import java.sql.Connection;
import java.sql.SQLException;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;

/**
 * 抛出 SQLException 的生成器
 */
public class SqlExceptionHolder implements GeneratedKeyHandlerFactory {
    @Override
    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) {
        return new GeneratedKeyHandler() {
            @Override
            public boolean onBefore() {
                return true;
            }

            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) throws SQLException {
                throw new SQLException("Intentional SQLException in key generator");
            }
        };
    }
}
