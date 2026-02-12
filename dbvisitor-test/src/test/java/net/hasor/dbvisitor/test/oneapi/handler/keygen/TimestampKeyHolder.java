package net.hasor.dbvisitor.test.oneapi.handler.keygen;
import java.sql.Connection;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;

/**
 * 时间戳主键生成器 - 用于测试
 * 使用当前时间戳的后6位作为主键
 */
public class TimestampKeyHolder implements GeneratedKeyHandlerFactory {
    @Override
    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) {
        return new GeneratedKeyHandler() {
            @Override
            public boolean onBefore() {
                return true;
            }

            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) {
                // 使用时间戳的后6位
                long timestamp = System.currentTimeMillis();
                Integer timestampId = (int) (timestamp % 1000000);

                mapping.getHandler().set(entity, timestampId);
                return timestampId;
            }
        };
    }
}
