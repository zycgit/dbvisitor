/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping.keyseq;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;

/**
 * 使用 jdbc 接受来自数据库的自增回填值
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-01
 */
public class AutoKeySeqHolderFactory implements GeneratedKeyHandlerFactory {
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
                Object value = mapping.getTypeHandler().getResult(generatedKeys, argsIndex + 1);
                mapping.getHandler().set(entity, value);
                return value;
            }

            @Override
            public String toString() {
                return "Auto@" + this.hashCode();
            }
        };
    }
}
