package net.hasor.dbvisitor.test.oneapi.handler.keygen;

import java.sql.Connection;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import static org.junit.Assert.assertNotNull;

/**
 * 验证上下文访问的生成器
 */
public class ContextAwareKeyHolder implements GeneratedKeyHandlerFactory {
    @Override
    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) {
        // 可以访问上下文信息
        assertNotNull("Context should not be null", context);
        assertNotNull("Column should be available", context.getColumn());
        assertNotNull("Table should be available", context.getTable());

        return new GeneratedKeyHandler() {
            @Override
            public boolean onBefore() {
                return true;
            }

            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) {
                // 使用上下文信息生成主键
                String columnName = mapping.getColumn();
                Integer keyValue = columnName.hashCode() % 100000 + 700000;
                mapping.getHandler().set(entity, keyValue);
                return keyValue;
            }
        };
    }
}
