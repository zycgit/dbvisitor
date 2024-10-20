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
package net.hasor.dbvisitor.mapping.keyseq;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.SeqSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.mapping.KeySeq;
import net.hasor.dbvisitor.mapping.KeySeqHolder;
import net.hasor.dbvisitor.mapping.KeySeqHolderContext;
import net.hasor.dbvisitor.mapping.KeySeqHolderFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.types.TypeHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;

/**
 * 支持 @KeySequence 注解方式
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-12-01
 */
public class SeqKeySeqHolderFactory implements KeySeqHolderFactory {
    @Override
    public KeySeqHolder createHolder(KeySeqHolderContext context) {
        Objects.requireNonNull(context.getSqlDialect(), "sqlDialect is null.");
        Map<String, Object> contextMap = context.getContext();
        if (contextMap == null || !contextMap.containsKey(KeySeq.class.getName())) {
            return null;
        }

        KeySeq keySeqAnno = (KeySeq) contextMap.get(KeySeq.class.getName());
        String seqName = keySeqAnno.value();

        if (StringUtils.isBlank(seqName)) {
            throw new IllegalArgumentException("@KeySeq config failed, no name specified.");
        }

        boolean useDelimited = context.useDelimited();
        SqlDialect dialect = context.getSqlDialect();
        if (!(dialect instanceof SeqSqlDialect)) {
            throw new ClassCastException(dialect.getClass().getName() + " is not SeqSqlDialect.");
        }
        final String seqQuery = ((SeqSqlDialect) dialect).selectSeq(useDelimited, context.getCatalog(), context.getSchema(), seqName);

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
            public boolean onBefore() {
                return true;
            }

            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) throws SQLException {
                Object var = selectSeq(seqQuery, conn, finalTypeHandler);
                mapping.getHandler().set(entity, var);
                return var;
            }

            @Override
            public String toString() {
                return "Sequence@" + this.hashCode();
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