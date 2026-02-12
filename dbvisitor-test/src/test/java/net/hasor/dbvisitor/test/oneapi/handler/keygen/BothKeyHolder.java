package net.hasor.dbvisitor.test.oneapi.handler.keygen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;

/**
 * 同时实现 onBefore 和 onAfter 的生成器
 * 用于验证优先级（onBefore 优先）
 */
public class BothKeyHolder implements GeneratedKeyHandlerFactory {
    @Override
    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) {
        return new GeneratedKeyHandler() {
            @Override
            public boolean onBefore() {
                return true; // 返回 true，表示使用 onBefore
            }

            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) {
                mapping.getHandler().set(entity, 888888);
                return 888888;
            }

            @Override
            public boolean onAfter() {
                return true;
            }

            @Override
            public Object afterApply(ResultSet generatedKeys, Object entity, int argsIndex, ColumnMapping mapping) throws SQLException {
                mapping.getHandler().set(entity, 777777);
                return 777777;
            }
        };
    }
}
