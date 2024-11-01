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
    protected static final Logger          logger = LoggerFactory.getLogger(BasicLambda.class);
    private final          Class<?>        exampleType;
    private final          boolean         exampleIsMap;
    private final          TableMapping<?> tableMapping;
    //
    protected final        RegistryManager registry;
    protected final        JdbcTemplate    jdbc;

    public BasicLambda(Class<?> exampleType, TableMapping<?> tableMapping, RegistryManager registry, JdbcTemplate jdbc) {
        this.exampleType = Objects.requireNonNull(exampleType, "exampleType is null.");
        this.exampleIsMap = Map.class == exampleType || Map.class.isAssignableFrom(this.exampleType());
        this.tableMapping = Objects.requireNonNull(tableMapping, "tableMapping is null.");

        this.registry = Objects.requireNonNull(registry, "registry is null.");
        this.jdbc = jdbc;
    }

    public final Class<?> exampleType() {
        return this.exampleType;
    }

    protected final TableMapping<?> getTableMapping() {
        return this.tableMapping;
    }

    protected boolean isQualifier() {
        return this.tableMapping.useDelimited();
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

    protected Segment buildGroupOrderByProperty(String propertyName) {
        return buildGroupOrderByProperty(false, false, propertyName);
    }

    private Segment buildGroupOrderByProperty(boolean isWhere, boolean isSelect, String propertyName) {
        ColumnMapping property = this.findPropertyByName(propertyName);

        if (!isSelect && isWhere) {
            String specialWhereCol = property.getWhereColTemplate();
            if (StringUtils.isNotBlank(specialWhereCol)) {
                return d -> specialWhereCol;
            }
        } else if (isSelect && !isWhere) {
            String specialSelectCol = property.getSelectTemplate();
            if (StringUtils.isNotBlank(specialSelectCol)) {
                return d -> specialSelectCol;
            }
        }

        String columnName = property.getColumn();
        return d -> d.fmtName(isQualifier(), columnName);
    }

    protected ColumnMapping findPropertyByName(String propertyName) {
        ColumnMapping propertyInfo = this.tableMapping.getPropertyByName(propertyName);

        if (propertyInfo == null) {
            propertyInfo = this.whenPropertyNotExist(propertyName);
            if (propertyInfo == null) {
                String catalogName = tableMapping.getCatalog();
                String schemaName = this.tableMapping.getSchema();
                String tableName = this.tableMapping.getTable();
                StringBuilder strBuilder = new StringBuilder();
                strBuilder.insert(0, tableName);
                if (StringUtils.isNotBlank(schemaName)) {
                    strBuilder.insert(0, schemaName + ".");
                }
                if (StringUtils.isNotBlank(catalogName)) {
                    strBuilder.insert(0, catalogName + ".");
                }
                throw new NoSuchElementException("tableMapping '" + strBuilder + "', property '" + propertyName + "' is not exist.");
            }
        }
        return propertyInfo;
    }

    protected ColumnMapping whenPropertyNotExist(String propertyName) {
        return null;
    }

    protected boolean isFreedom() {
        return false;
    }

    protected Map<String, String> extractKeysMap(Map entity) {
        TableMapping<?> tableMapping = getTableMapping();
        Collection<ColumnMapping> properties = tableMapping.getProperties();
        if (properties.isEmpty()) {
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
            for (ColumnMapping mapping : properties) {
                propertySet.put(mapping.getProperty(), mapping.getColumn());
            }
            return propertySet;
        }
    }

    protected final SqlDialect dialect() {
        SqlDialect dialect = this.tableMapping.getDialect();
        if (dialect != null) {
            return dialect;
        } else {
            return DefaultSqlDialect.DEFAULT;
        }
    }

    public final BoundSql getBoundSql() {
        return buildBoundSql(dialect());
    }

    protected abstract BoundSql buildBoundSql(SqlDialect dialect);

    protected abstract R getSelf();

    protected R reset() {
        return this.getSelf();
    }
}