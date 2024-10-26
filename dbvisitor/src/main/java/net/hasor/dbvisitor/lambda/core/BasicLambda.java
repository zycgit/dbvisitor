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
package net.hasor.dbvisitor.lambda.core;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.segment.Segment;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import java.util.*;

/**
 * 所有 SQL 执行器必要的公共属性
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-27
 */
public abstract class BasicLambda<R, T, P> {
    protected static final Logger              logger = LoggerFactory.getLogger(BasicLambda.class);
    private                boolean             qualifier;
    private final          Class<?>            exampleType;
    private final          boolean             exampleIsMap;
    private final          TableMapping<?>     tableMapping;
    private final          List<ColumnMapping> primaryKey;
    //
    protected final        RegistryManager     registry;
    protected final        JdbcTemplate        jdbc;
    protected final        SqlDialect          dialect;

    public BasicLambda(Class<?> exampleType, TableMapping<?> tableMapping, RegistryManager registry, JdbcTemplate jdbc) {
        this.exampleType = Objects.requireNonNull(exampleType, "exampleType is null.");
        this.exampleIsMap = Map.class == exampleType || Map.class.isAssignableFrom(this.exampleType());
        this.tableMapping = Objects.requireNonNull(tableMapping, "tableMapping is null.");
        this.primaryKey = getPrimaryKeyColumns(this.tableMapping);

        this.registry = Objects.requireNonNull(registry, "registry is null.");
        this.jdbc = jdbc;

        SqlDialect tempDialect = tableMapping.getDialect();
        this.dialect = (tempDialect == null) ? DefaultSqlDialect.DEFAULT : tempDialect;
        this.qualifier = tableMapping.useDelimited();
    }

    public final Class<?> exampleType() {
        return this.exampleType;
    }

    public R useQualifier() {
        this.qualifier = true;
        return this.getSelf();
    }

    protected final JdbcTemplate getJdbc() {
        return this.jdbc;
    }

    protected final TableMapping<?> getTableMapping() {
        return this.tableMapping;
    }

    protected final SqlDialect dialect() {
        return this.dialect;
    }

    protected boolean isQualifier() {
        return this.qualifier;
    }

    protected boolean exampleIsMap() {
        return this.exampleIsMap;
    }

    protected abstract String getPropertyName(P property);

    protected Segment buildSelectByProperty(String propertyName) {
        return buildGroupOrderByProperty(false, true, propertyName);
    }

    protected Segment buildConditionByProperty(String propertyName) {
        return buildGroupOrderByProperty(true, false, propertyName);
    }

    protected Segment buildConditionByColumn(String columnName) {
        return () -> dialect().fmtName(isQualifier(), columnName);
    }

    protected Segment buildGroupOrderByProperty(String propertyName) {
        return buildGroupOrderByProperty(false, false, propertyName);
    }

    private Segment buildGroupOrderByProperty(boolean isWhere, boolean isSelect, String propertyName) {
        TableMapping<?> tableMapping = this.getTableMapping();
        String catalogName = tableMapping.getCatalog();
        String schemaName = tableMapping.getSchema();
        String tableName = tableMapping.getTable();
        ColumnMapping propertyInfo = tableMapping.getPropertyByName(propertyName);

        if (propertyInfo == null) {
            String tab = this.dialect.tableName(isQualifier(), catalogName, schemaName, tableName);
            throw new NullPointerException("tableMapping '" + tab + "', property '" + propertyName + "' is not exist.");
        }

        if (!isSelect && isWhere) {
            String specialWhereCol = propertyInfo.getWhereColTemplate();
            if (StringUtils.isNotBlank(specialWhereCol)) {
                return () -> specialWhereCol;
            }
        } else if (isSelect && !isWhere) {
            String specialSelectCol = propertyInfo.getSelectTemplate();
            if (StringUtils.isNotBlank(specialSelectCol)) {
                return () -> specialSelectCol;
            }
        }

        String columnName = propertyInfo.getColumn();
        return () -> dialect().fmtName(isQualifier(), columnName);
    }

    protected Map<String, String> extractKeysMap(Map entity) {
        Map<String, String> propertySet = getTableMapping().isCaseInsensitive() ? new LinkedCaseInsensitiveMap<>() : new HashMap<>();
        for (Object key : entity.keySet()) {
            String keyStr = key.toString();
            propertySet.put(keyStr, keyStr);
        }
        return propertySet;
    }

    public final BoundSql getBoundSql() {
        return getBoundSql(dialect());
    }

    public final BoundSql getBoundSql(SqlDialect dialect) {
        if (dialect == null) {
            throw new IllegalStateException("dialect is null.");
        } else {
            return buildBoundSql(dialect);
        }
    }

    protected abstract BoundSql buildBoundSql(SqlDialect dialect);

    protected abstract R getSelf();

    protected List<ColumnMapping> getPrimaryKey() {
        return this.primaryKey;
    }

    private List<ColumnMapping> getPrimaryKeyColumns(TableMapping<?> tableMapping) {
        List<ColumnMapping> properties = new ArrayList<>();
        Set<String> pkc = new HashSet<>();
        for (ColumnMapping mapping : tableMapping.getProperties()) {
            String columnName = mapping.getColumn();
            if (!mapping.isPrimaryKey()) {
                continue;
            }

            if (pkc.contains(columnName)) {
                throw new IllegalStateException("Multiple property mapping to '" + columnName + "' column");
            } else {
                pkc.add(columnName);
                properties.add(mapping);
            }
        }
        return properties;
    }
}