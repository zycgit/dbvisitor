package net.hasor.dbvisitor.test.oneapi.handler.keygen;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;

/**
 * 插入后获取主键生成器 - 用于测试
 * 从数据库返回的 ResultSet 中获取自增主键
 */
public class AfterInsertKeyHolder implements GeneratedKeyHandlerFactory {

    @Override
    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) {
        return new GeneratedKeyHandler() {
            @Override
            public boolean onAfter() {
                return true;
            }

            @Override
            public boolean useGeneratedKeys() {
                return true;
            }

            @Override
            public Object afterApply(ResultSet generatedKeys, Object entity, int argsIndex, ColumnMapping mapping) throws SQLException {
                // 框架在 processKeySeqHolderAfter 中已调用 rs.next()，此处不需重复调用
                if (generatedKeys != null) {
                    Object keyValue = mapping.getTypeHandler().getResult(generatedKeys, argsIndex + 1);
                    mapping.getHandler().set(entity, keyValue);
                    return keyValue;
                }
                return null;
            }
        };
    }
}
