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
package net.hasor.dbvisitor.wrapper.core;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.wrapper.segment.MergeSqlSegment;
import net.hasor.dbvisitor.wrapper.segment.SqlKeyword;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 提供 lambda update 基础能力。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-27
 */
public abstract class AbstractUpdateWrapper<R, T, P> extends BasicQueryCompare<R, T, P> implements UpdateExecute<R, T, P> {
    protected final Set<String>                allowUpdateKeys;
    protected final Set<String>                primaryKeys;
    protected final Map<String, ColumnMapping> allowUpdateProperties;
    //
    private final   Map<String, SqlArg>        updateValueMap;
    private         boolean                    allowEmptyWhere = false;
    private         boolean                    allowUpdateKey  = false;

    public AbstractUpdateWrapper(Class<?> exampleType, TableMapping<?> tableMapping, MappingRegistry registry, JdbcTemplate jdbc) {
        super(exampleType, tableMapping, registry, jdbc);

        Set<String> allowUpdateKeys = new LinkedHashSet<>();
        Set<String> primaryKeys = new LinkedHashSet<>();
        Map<String, ColumnMapping> allowUpdateProperties = new LinkedHashMap<>();
        for (ColumnMapping mapping : tableMapping.getProperties()) {
            if (mapping.isPrimaryKey()) {
                primaryKeys.add(mapping.getProperty());
            }
            if (mapping.isUpdate()) {
                allowUpdateProperties.put(mapping.getProperty(), mapping);
                allowUpdateKeys.add(mapping.getProperty());
            }
        }

        this.allowUpdateKeys = Collections.unmodifiableSet(allowUpdateKeys);
        this.primaryKeys = Collections.unmodifiableSet(primaryKeys);
        this.allowUpdateProperties = Collections.unmodifiableMap(allowUpdateProperties);
        this.updateValueMap = new LinkedHashMap<>();
    }

    @Override
    public R reset() {
        super.reset();
        this.allowEmptyWhere = false;
        this.allowUpdateKey = false;
        this.updateValueMap.clear();
        return this.getSelf();
    }

    @Override
    public R allowEmptyWhere() {
        this.allowEmptyWhere = true;
        return this.getSelf();
    }

    @Override
    public R allowUpdateKey() {
        this.allowUpdateKey = true;
        return this.getSelf();
    }

    @Override
    public int doUpdate() throws SQLException {
        if (this.updateValueMap.isEmpty()) {
            throw new IllegalStateException("there nothing to update.");
        }

        BoundSql boundSql = getBoundSql();
        String sqlString = boundSql.getSqlString();

        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL statement [" + sqlString + "].");
        }

        Objects.requireNonNull(this.jdbc, "Connection unavailable, JdbcTemplate is required.");
        return this.jdbc.executeUpdate(sqlString, boundSql.getArgs());
    }

    @Override
    public R updateToSample(final T newValue) {
        return updateToSample(newValue, t -> true);
    }

    @Override
    public R updateToSample(T newValue, Predicate<String> condition) {
        if (newValue == null) {
            throw new NullPointerException("newValue is null.");
        }

        if (exampleIsMap()) {
            return this.updateToSampleMap((Map<String, Object>) newValue, condition);
        }

        Map<String, Object> tempData = new HashMap<>();
        for (Map.Entry<String, ColumnMapping> mappingEntry : this.allowUpdateProperties.entrySet()) {
            Object value = mappingEntry.getValue().getHandler().get(newValue);
            if (value != null) {
                tempData.put(mappingEntry.getKey(), value);
            }
        }

        Function<String, String> colName = s -> this.allowUpdateProperties.get(s).getColumn();

        Set<String> keys = new LinkedHashSet<>(this.allowUpdateKeys);
        if (!this.allowUpdateKey) {
            keys.removeAll(this.primaryKeys);
        }

        return this.updateToByCondition(keys, true, s -> {
            return tempData.containsKey(s) && condition.test(s);
        }, colName, tempData::get);
    }

    @Override
    public R updateToSampleMap(Map<String, Object> newValue) {
        return this.updateToSampleMap(newValue, t -> true);
    }

    @Override
    public R updateToSampleMap(Map<String, Object> newValue, Predicate<String> condition) {
        if (newValue == null) {
            throw new NullPointerException("newValue is null.");
        }

        Map<String, String> entityKeyMap = extractKeysMap(newValue);
        Set<String> keys;
        Map<String, Object> tempData;
        if (this.isFreedom()) {
            keys = entityKeyMap.keySet();
            tempData = newValue;
        } else {
            keys = new LinkedHashSet<>();
            tempData = new HashMap<>();
            for (String key : this.allowUpdateKeys) {
                Object value = newValue.get(key);
                if (value != null) {
                    tempData.put(key, value);
                    keys.add(key);
                }
            }
            if (tempData.isEmpty()) {
                throw new IllegalStateException("there nothing to update.");
            }
        }

        return this.updateToByCondition(keys, true, s -> {
            return condition.test(s) && keys.contains(s);
        }, entityKeyMap::get, tempData::get);
    }

    @Override
    public R updateRow(T newValue) {
        return this.updateRow(newValue, t -> true);
    }

    @Override
    public R updateRow(T newValue, Predicate<String> condition) {
        if (newValue == null) {
            throw new NullPointerException("newValue is null.");
        }

        if (exampleIsMap()) {
            return this.updateRowUsingMap((Map<String, Object>) newValue, condition);
        }

        Set<String> keys = new LinkedHashSet<>(this.allowUpdateKeys);
        if (!this.allowUpdateKey) {
            keys.removeAll(this.primaryKeys);
        }

        Function<String, String> colName = s -> this.allowUpdateProperties.get(s).getColumn();
        return this.updateToByCondition(keys, true, condition, colName, createPropertyReaderFunc(newValue));
    }

    private Function<String, Object> createPropertyReaderFunc(T newValue) {
        if (exampleIsMap()) {
            return ((Map) newValue)::get;
        } else {
            return property -> {
                ColumnMapping reader = this.findPropertyByName(property);
                return reader.getHandler().get(newValue);
            };
        }
    }

    @Override
    public R updateRowUsingMap(Map<String, Object> newValue) {
        return this.updateRowUsingMap(newValue, t -> true);
    }

    @Override
    public R updateRowUsingMap(Map<String, Object> newValue, Predicate<String> condition) {
        if (newValue == null) {
            throw new NullPointerException("newValue is null.");
        }

        Map<String, String> entityKeyMap = extractKeysMap(newValue);
        Set<String> keys = this.isFreedom() ? entityKeyMap.keySet() : this.allowUpdateKeys;

        return this.updateToByCondition(keys, true, condition, entityKeyMap::get, newValue::get);
    }

    @Override
    public R updateTo(boolean test, P property, Object value) {
        if (test) {
            return this.updateToUsingStr(true, getPropertyName(property), value);
        } else {
            return this.getSelf();
        }
    }

    @Override
    public R updateToUsingStr(boolean test, String property, Object value) {
        if (test) {
            Set<String> keys = new HashSet<>(Collections.singletonList(property));

            if (isFreedom()) {
                String colName = this.getTableMapping().isToCamelCase() ? StringUtils.humpToLine(property) : property;

                Map<String, Object> valMap = CollectionUtils.asMap(property, value);
                Map<String, String> colMap = CollectionUtils.asMap(property, colName);
                return this.updateToByCondition(keys, false, keys::contains, colMap::get, valMap::get);
            }

            if (!this.allowUpdateKeys.contains(property)) {
                throw new NoSuchElementException("No such property: " + property);
            }

            ColumnMapping colMapping = this.allowUpdateProperties.get(property);
            Map<String, Object> valMap = CollectionUtils.asMap(property, value);
            Map<String, String> colMap = CollectionUtils.asMap(property, colMapping.getColumn());
            return this.updateToByCondition(keys, false, keys::contains, colMap::get, valMap::get);
        } else {
            return this.getSelf();
        }
    }

    protected R updateToByCondition(Set<String> foreach, boolean doClear, Predicate<String> tester, Function<String, String> colConvert, Function<String, Object> reader) {
        if (doClear) {
            this.updateValueMap.clear();
        }

        Set<String> updateColumns = new LinkedHashSet<>();
        for (String propertyName : foreach) {
            if (!tester.test(propertyName)) {
                continue;
            }

            String columnName = colConvert.apply(propertyName);
            if (updateColumns.contains(columnName)) {
                throw new IllegalStateException("Multiple property mapping to '" + columnName + "' column");
            } else {
                updateColumns.add(columnName);
            }

            ColumnMapping mapping = this.allowUpdateProperties.get(propertyName);
            Object propertyValue = reader.apply(propertyName);
            if (mapping != null && mapping.isPrimaryKey() && !this.allowUpdateKey) {
                if (propertyValue != null) {
                    throw new UnsupportedOperationException("The dangerous UPDATE operation, You must call `allowUpdateKey()` to enable UPDATE PrimaryKey.");
                } else {
                    continue; // 主键如果没有值，那么忽略主键更新，否则必须要 allowUpdateKey
                }
            }

            if (propertyValue == null) {
                this.updateValueMap.put(propertyName, null);
            } else if (mapping != null) {
                SqlArg mappedArg = new SqlArg(propertyValue, mapping.getJdbcType(), exampleIsMap() ? null : mapping.getTypeHandler());
                this.updateValueMap.put(propertyName, mappedArg);
            } else {
                int sqlType = TypeHandlerRegistry.toSqlType(propertyValue.getClass());
                TypeHandler<?> typeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(propertyValue.getClass());
                SqlArg mappedArg = new SqlArg(propertyValue, sqlType, typeHandler);
                this.updateValueMap.put(propertyName, mappedArg);
            }
        }
        return this.getSelf();
    }

    @Override
    protected BoundSql buildBoundSql(SqlDialect dialect) {
        if (this.updateValueMap.isEmpty()) {
            throw new IllegalStateException("there nothing to update.");
        }
        // must be clean , The getOriginalBoundSql will reinitialize.
        this.queryParam.clear();
        //
        // update
        MergeSqlSegment updateTemplate = new MergeSqlSegment();
        updateTemplate.addSegment(SqlKeyword.UPDATE);

        // tableName
        updateTemplate.addSegment(d -> {
            TableMapping<?> tableMapping = this.getTableMapping();
            String catalogName = tableMapping.getCatalog();
            String schemaName = tableMapping.getSchema();
            String tableName = tableMapping.getTable();
            return d.tableName(isQualifier(), catalogName, schemaName, tableName);
        });

        // SET
        updateTemplate.addSegment(SqlKeyword.SET);
        boolean isFirstColumn = true;
        for (String propertyName : this.updateValueMap.keySet()) {
            if (isFirstColumn) {
                isFirstColumn = false;
            } else {
                updateTemplate.addSegment(d -> ",");
            }

            String colName;
            String colValue;
            if (this.isFreedom()) {
                String col = this.getTableMapping().isToCamelCase() ? StringUtils.humpToLine(propertyName) : propertyName;
                colName = dialect.fmtName(isQualifier(), col);
                colValue = "?";
            } else {
                ColumnMapping mapping = this.allowUpdateProperties.get(propertyName);
                String specialName = mapping.getSetColTemplate();
                String specialValue = mapping.getSetValueTemplate();

                colName = StringUtils.isNotBlank(specialName) ? specialName : dialect.fmtName(isQualifier(), mapping.getColumn());
                colValue = StringUtils.isNotBlank(specialValue) ? specialValue : "?";
            }

            Object columnValue = this.updateValueMap.get(propertyName);
            updateTemplate.addSegment(d -> colName, SqlKeyword.EQ, formatSegment(colValue, columnValue));
        }

        // WHERE
        if (!this.queryTemplate.isEmpty()) {
            updateTemplate.addSegment(SqlKeyword.WHERE);
            updateTemplate.addSegment(this.queryTemplate.sub(1));
        } else if (!this.allowEmptyWhere) {
            throw new UnsupportedOperationException("The dangerous UPDATE operation, You must call `allowEmptyWhere()` to enable UPDATE ALL.");
        }

        String sqlQuery = updateTemplate.getSqlSegment(dialect);
        Object[] args = this.queryParam.toArray().clone();
        return new BoundSql.BoundSqlObj(sqlQuery, args);
    }

}
