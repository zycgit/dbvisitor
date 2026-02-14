package net.hasor.dbvisitor.test.handler.keygen;
import java.sql.Connection;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;

/**
 * 固定值主键生成器 - 用于测试
 * 始终返回固定值 999999
 */
public class FixedValueKeyHolder implements GeneratedKeyHandlerFactory {

    @Override
    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) {
        return new GeneratedKeyHandler() {
            @Override
            public boolean onBefore() {
                return true;
            }

            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) {
                Integer fixedId = 999999;
                mapping.getHandler().set(entity, fixedId);
                return fixedId;
            }
        };
    }
}
