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
package net.hasor.dbvisitor.lambda.support.map;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.dialect.BatchBoundSql.BatchBoundSqlObj;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.BoundSql.BoundSqlObj;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.core.AbstractInsertLambda;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.dbvisitor.types.MappedArg;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供 lambda insert 能力。是 InsertOperation 接口的实现类。
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public class InsertLambdaForMap extends AbstractInsertLambda<InsertOperation<Map<String, Object>>, Map<String, Object>, String> //
        implements InsertOperation<Map<String, Object>> {
    private final boolean toCamelCase;

    public InsertLambdaForMap(TableMapping<?> tableMapping, MappingOptions opt, LambdaTemplate jdbcTemplate) {
        super(Map.class, tableMapping, opt, jdbcTemplate);
        this.toCamelCase = getTableMapping().isToCamelCase();
    }

    @Override
    protected InsertLambdaForMap getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(String property) {
        return property;
    }

    @Override
    protected BoundSql buildBoundSql(SqlDialect dialect) {
        if (this.insertValuesCount.get() != 1) {
            throw new IllegalStateException("require single record.");
        }

        InsertEntity entity = this.insertValues.get(0);
        BoundSqlObj boundSqlObj = this.buildBoundSql(dialect(), (Map) entity.objList.get(0));

        return new BatchBoundSqlObj(boundSqlObj.getSqlString(), new MappedArg[][] { (MappedArg[]) boundSqlObj.getArgs() });
    }

    @Override
    public int[] executeGetResult() throws SQLException {
        return this.getJdbcTemplate().execute((ConnectionCallback<int[]>) con -> {
            final TypeHandlerRegistry typeRegistry = this.getJdbcTemplate().getTypeRegistry();
            int[] result = new int[this.insertValuesCount.get()];

            int i = 0;
            for (InsertEntity entity : this.insertValues) {
                for (Object obj : entity.objList) {
                    result[i] = executeOne(con, (Map) obj, typeRegistry);
                    i++;
                }
            }
            return result;
        });
    }

    private int executeOne(Connection con, Map ent, TypeHandlerRegistry typeRegistry) throws SQLException {
        BoundSqlObj boundSqlObj = this.buildBoundSql(dialect(), ent);
        String sqlString = boundSqlObj.getSqlString();

        try (PreparedStatement ps = createPrepareStatement(con, sqlString)) {
            applyPreparedStatement(ps, boundSqlObj.getArgs(), typeRegistry);
            return ps.executeUpdate();
        }
    }

    protected BoundSqlObj buildBoundSql(SqlDialect dialect, Map entity) {
        Map<String, String> entityKeyMap = this.extractKeysMap(entity);
        List<String> insertProperties = new ArrayList<>();
        List<String> insertColumns = new ArrayList<>();
        entityKeyMap.forEach((p, c) -> {
            insertProperties.add(p);
            insertColumns.add(c);
        });

        String insertSql = buildInsert(dialect, this.primaryKeys, insertColumns, this.insertColumnTerms);
        MappedArg[] args = new MappedArg[entityKeyMap.size()];

        for (int i = 0; i < insertProperties.size(); i++) {
            Object arg = entity.get(insertProperties.get(i));
            Integer jdbcType = arg == null ? null : TypeHandlerRegistry.toSqlType(arg.getClass());
            args[i] = (arg == null) ? null : new MappedArg(arg, jdbcType, null);
        }

        return new BoundSqlObj(insertSql, args);
    }

    protected Map<String, String> extractKeysMap(Map entity) {
        Map<String, String> propertySet = getTableMapping().isCaseInsensitive() ? new LinkedCaseInsensitiveMap<>() : new HashMap<>();
        for (Object key : entity.keySet()) {
            String keyStr = key.toString();
            if (this.toCamelCase) {
                propertySet.put(keyStr, StringUtils.humpToLine(keyStr));
            } else {
                propertySet.put(keyStr, keyStr);
            }
        }
        return propertySet;
    }
}
