package net.hasor.dbvisitor.test.oneapi.handler.keygen;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;

/**
 * 计数器主键生成器 - 用于测试 GeneratedKeyHandler.onBefore
 */
public class CounterKeyHolder implements GeneratedKeyHandlerFactory {
    private final AtomicInteger counter = new AtomicInteger(800000);

    @Override
    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) {
        return new GeneratedKeyHandler() {
            @Override
            public boolean onBefore() {
                return true;
            }

            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) {
                Integer nextId = counter.incrementAndGet();
                mapping.getHandler().set(entity, nextId);
                return nextId;
            }
        };
    }
}
