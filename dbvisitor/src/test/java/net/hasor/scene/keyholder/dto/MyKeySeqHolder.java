package net.hasor.scene.keyholder.dto;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;

public class MyKeySeqHolder implements GeneratedKeyHandlerFactory {

    @Override
    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) {
        return new GeneratedKeyHandler() {
            @Override
            public boolean onAfter() {
                return GeneratedKeyHandler.super.onAfter();
            }

            @Override
            public Object afterApply(ResultSet generatedKeys, Object entity, int argsIndex, ColumnMapping mapping) throws SQLException {
                return GeneratedKeyHandler.super.afterApply(generatedKeys, entity, argsIndex, mapping);
            }

            @Override
            public boolean onBefore() {
                return true;
            }

            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) {
                mapping.getHandler().set(entity, 111111);
                return 111111;
            }
        };
    }
}
