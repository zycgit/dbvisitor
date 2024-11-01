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
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.MapInsertOperation;
import net.hasor.dbvisitor.lambda.core.AbstractInsertLambda;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供 lambda insert 能力。是 InsertOperation 接口的实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-04-02
 */
public class InsertLambdaForMap extends AbstractInsertLambda<InsertOperation<Map<String, Object>>, Map<String, Object>, String> //
        implements MapInsertOperation {

    public InsertLambdaForMap(TableMapping<?> tableMapping, RegistryManager registry, JdbcTemplate jdbc) {
        super(Map.class, tableMapping, registry, jdbc);
    }

    @Override
    public MapInsertOperation asMap() {
        return this;
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
        if (this.insertValuesCount.get() == 0) {
            return null;
        }

        InsertEntity entity = this.insertValues.get(0);
        BoundSqlObj boundSqlObj = this.buildBoundSql(dialect, (Map) entity.objList.get(0));

        return new BatchBoundSqlObj(boundSqlObj.getSqlString(), new SqlArg[][] { (SqlArg[]) boundSqlObj.getArgs() });
    }

    @Override
    public int[] executeGetResult() throws SQLException {
        try {
            return this.jdbc.execute((ConnectionCallback<int[]>) con -> {
                final TypeHandlerRegistry typeRegistry = this.registry.getTypeRegistry();
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
        } finally {
            this.reset();
        }
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

        String insertSql = buildInsert(dialect, this.forBuildPrimaryKeys, insertColumns, this.forBuildInsertColumnTerms);
        SqlArg[] args = new SqlArg[entityKeyMap.size()];

        for (int i = 0; i < insertProperties.size(); i++) {
            Object arg = entity.get(insertProperties.get(i));
            Integer jdbcType = arg == null ? null : TypeHandlerRegistry.toSqlType(arg.getClass());
            args[i] = (arg == null) ? null : new SqlArg(arg, jdbcType, null);
        }

        return new BoundSqlObj(insertSql, args);
    }

    protected Map<String, String> extractKeysMap(Map entity) {
        if (this.insertProperties.isEmpty()) {
            TableMapping<?> tableMapping = getTableMapping();
            Map<String, String> propertySet = tableMapping.isCaseInsensitive() ? new LinkedCaseInsensitiveMap<>() : new LinkedHashMap<>();
            for (Object key : entity.keySet()) {
                String keyStr = key.toString();
                if (tableMapping.isToCamelCase()) {
                    propertySet.put(keyStr, StringUtils.humpToLine(keyStr));
                } else {
                    propertySet.put(keyStr, keyStr);
                }
            }
            return propertySet;
        } else {
            Map<String, String> propertySet = new LinkedHashMap<>();
            for (ColumnMapping mapping : this.insertProperties) {
                if (entity.containsKey(mapping.getProperty())) {
                    propertySet.put(mapping.getProperty(), mapping.getColumn());
                }
            }
            return propertySet;
        }
    }
}
