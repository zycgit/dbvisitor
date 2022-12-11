/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.keyholder.sequence;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.SeqSqlDialect;
import net.hasor.dbvisitor.keyholder.CreateContext;
import net.hasor.dbvisitor.keyholder.KeySeq;
import net.hasor.dbvisitor.keyholder.KeySeqHolder;
import net.hasor.dbvisitor.keyholder.KeySeqHolderFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.types.TypeHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * 支持 @KeySequence 注解方式
 * @version : 2022-12-01
 * @author 赵永春 (zyc@hasor.net)
 */
public class SeqKeySeqHolderFactory implements KeySeqHolderFactory {
    @Override
    public KeySeqHolder createHolder(CreateContext context) {
        if (context.getOptions() == null || !(context.getOptions().getDefaultDialect() instanceof SeqSqlDialect)) {
            return null;
        }
        Map<String, Object> contextMap = context.getContext();
        if (contextMap == null || !contextMap.containsKey(KeySeq.class.getName())) {
            return null;
        }

        KeySeq keySeqAnno = (KeySeq) contextMap.get(KeySeq.class.getName());
        String seqName = keySeqAnno.value();

        if (StringUtils.isBlank(seqName)) {
            throw new IllegalArgumentException("@KeySeq config failed, no name specified.");
        }

        boolean useDelimited = Boolean.TRUE.equals(context.getOptions().getUseDelimited());
        SeqSqlDialect dialect = (SeqSqlDialect) context.getOptions().getDefaultDialect();
        final String seqQuery = dialect.selectSeq(useDelimited, context.getCatalog(), context.getSchema(), seqName);

        TypeHandler<?> typeHandler = context.getTypeHandler();
        if (typeHandler == null) {
            Class<?> javaType = context.getJavaType();
            Integer jdbcType = context.getJdbcType();
            if (jdbcType != null) {
                typeHandler = context.getTypeRegistry().getTypeHandler(javaType, jdbcType);
            } else {
                typeHandler = context.getTypeRegistry().getTypeHandler(javaType);
            }
        }
        if (typeHandler == null) {
            typeHandler = context.getTypeRegistry().getDefaultTypeHandler();
        }

        TypeHandler<?> finalTypeHandler = typeHandler;
        return new KeySeqHolder() {
            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) throws SQLException {
                Object var = selectSeq(seqQuery, conn, finalTypeHandler);
                mapping.getHandler().set(entity, var);
                return var;
            }
        };
    }

    protected Object selectSeq(String queryStr, Connection conn, TypeHandler<?> typeHandler) throws SQLException {
        try (Statement s = conn.createStatement()) {
            try (ResultSet res = s.executeQuery(queryStr)) {
                if (res.next()) {
                    return typeHandler.getResult(res, 1);
                } else {
                    return null;
                }
            }
        }
    }
}