/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping.keyseq;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;

/**
 * 使用 32 长度 UUID 作为默认 Key 值
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-01
 */
public class Uuid32KeySeqHolderFactory implements GeneratedKeyHandlerFactory {
    @Override
    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) {
        return new GeneratedKeyHandler() {
            @Override
            public boolean onBefore() {
                return true;
            }

            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) throws java.sql.SQLException {
                Class<?> javaType = mapping.getJavaType();
                if (!String.class.equals(javaType)) {
                    throw new SQLException("UUID32 key holder requires String target property, but was " + (javaType == null ? "null" : javaType.getName()));
                }

                String genUUID = UUID.randomUUID().toString().replace("-", "");
                mapping.getHandler().set(entity, genUUID);
                return genUUID;
            }

            @Override
            public String toString() {
                return "UUID32@" + this.hashCode();
            }
        };
    }
}
