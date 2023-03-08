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
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.MappedArg;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.hasor.dbvisitor.lambda.segment.SqlKeyword.*;

/**
 * 提供 lambda update 基础能力。
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractUpdateLambda<R, T, P> extends BasicQueryCompare<R, T, P> implements UpdateExecute<R, T, P> {
    protected final Map<String, ColumnMapping> allowUpdateProperties;
    protected final Map<String, MappedArg>     updateValueMap;
    private         boolean                    allowEmptyWhere = false;
    private         boolean                    allowUpdateKey  = false;
    private         boolean                    allowReplaceRow = false;

    public AbstractUpdateLambda(Class<?> exampleType, TableMapping<?> tableMapping, LambdaTemplate jdbcTemplate) {
        super(exampleType, tableMapping, jdbcTemplate);

        this.allowUpdateProperties = new LinkedHashMap<>();
        for (ColumnMapping mapping : tableMapping.getProperties()) {
            if (mapping.isUpdate()) {
                this.allowUpdateProperties.put(mapping.getProperty(), mapping);
            }
        }

        this.updateValueMap = new HashMap<>();
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
    public R allowReplaceRow() {
        this.allowReplaceRow = true;
        return this.getSelf();
    }

    @Override
    public int doUpdate() throws SQLException {
        if (this.updateValueMap.isEmpty()) {
            throw new IllegalStateException("Nothing to update.");
        }

        BoundSql boundSql = getBoundSql();
        String sqlString = boundSql.getSqlString();

        if (logger.isDebugEnabled()) {
            logger.trace("Executing SQL statement [" + sqlString + "].");
        }

        return this.getJdbcTemplate().executeUpdate(sqlString, boundSql.getArgs());
    }

    @Override
    public R updateBySample(final T newValue) {
        if (newValue == null) {
            throw new NullPointerException("newValue is null.");
        }

        if (exampleIsMap()) {
            return this.updateByMap((Map<String, Object>) newValue);
        }

        Map<String, Object> tempData = new HashMap<>();
        for (Map.Entry<String, ColumnMapping> mappingEntry : this.allowUpdateProperties.entrySet()) {
            Object value = mappingEntry.getValue().getHandler().get(newValue);
            if (value != null) {
                tempData.put(mappingEntry.getKey(), value);
            }
        }

        return this.updateToByCondition(true, tempData::containsKey, tempData::get);
    }

    @Override
    public R updateByMap(Map<String, Object> newValue) {
        if (newValue == null) {
            throw new NullPointerException("newValue is null.");
        }

        Map<String, String> entityKeyMap = extractKeysMap(newValue);
        return this.updateToByCondition(true, entityKeyMap::containsKey, s -> newValue.get(entityKeyMap.get(s)));
    }

    @Override
    public R updateTo(T newValue) {
        if (newValue == null) {
            throw new NullPointerException("newValue is null.");
        }

        if (!this.allowReplaceRow) {
            throw new UnsupportedOperationException("The dangerous UPDATE operation, You must call `allowReplaceRow()` to enable REPLACE row.");
        }

        if (exampleIsMap()) {
            Map<String, Object> newValueMap = (Map<String, Object>) newValue;
            Map<String, String> entityKeyMap = extractKeysMap((Map) newValue);
            return this.updateToByCondition(true, p -> true, s -> newValueMap.get(entityKeyMap.get(s)));
        } else {
            return this.updateToByCondition(true, p -> true, createPropertyReaderFunc(newValue));
        }
    }

    @Override
    public R updateTo(P property, Object value) {
        Map<String, Object> newValue = CollectionUtils.asMap(getPropertyName(property), value);
        Map<String, String> entityKeyMap = extractKeysMap(newValue);
        return this.updateToByCondition(true, entityKeyMap::containsKey, s -> newValue.get(entityKeyMap.get(s)));
    }

    @Override
    public R updateToAdd(P property, Object value) {
        Map<String, Object> newValue = CollectionUtils.asMap(getPropertyName(property), value);
        Map<String, String> entityKeyMap = extractKeysMap(newValue);
        return this.updateToByCondition(false, entityKeyMap::containsKey, s -> newValue.get(entityKeyMap.get(s)));
    }

    private Function<String, Object> createPropertyReaderFunc(T newValue) {
        if (exampleIsMap()) {
            return ((Map) newValue)::get;
        } else {
            final TableMapping<?> tableMapping = this.getTableMapping();
            return property -> {
                ColumnMapping propertyReader = tableMapping.getPropertyByName(property);
                return (propertyReader == null) ? null : propertyReader.getHandler().get(newValue);
            };
        }
    }

    protected R updateToByCondition(boolean doClear, Predicate<String> propertyTester, Function<String, Object> propertyReader) {
        if (doClear) {
            this.updateValueMap.clear();
        }

        Set<String> updateColumns = new HashSet<>();
        for (Map.Entry<String, ColumnMapping> allowFieldEntry : this.allowUpdateProperties.entrySet()) {
            ColumnMapping allowProperty = allowFieldEntry.getValue();
            if (!propertyTester.test(allowProperty.getProperty())) {
                continue;
            }

            String columnName = allowProperty.getColumn();
            String propertyName = allowProperty.getProperty();
            Object propertyValue = propertyReader.apply(allowProperty.getProperty());

            if (updateColumns.contains(columnName)) {
                throw new IllegalStateException("Multiple property mapping to '" + columnName + "' column");
            }

            if (allowProperty.isPrimaryKey() && !this.allowUpdateKey) {
                if (propertyValue != null) {
                    throw new UnsupportedOperationException("The dangerous UPDATE operation, You must call `allowUpdateKey()` to enable UPDATE PrimaryKey.");
                } else {
                    continue; // 主键如果没有值，那么忽略主键更新，否则必须要 allowUpdateKey
                }
            }

            updateColumns.add(columnName);

            MappedArg mappedArg = new MappedArg(propertyValue, allowProperty.getJdbcType(), allowProperty.getTypeHandler());
            this.updateValueMap.put(propertyName, mappedArg);
        }
        return this.getSelf();
    }

    @Override
    protected BoundSql buildBoundSql(SqlDialect dialect) {
        if (this.updateValueMap.isEmpty()) {
            return null;
        }
        // must be clean , The getOriginalBoundSql will reinitialize.
        this.queryParam.clear();
        //
        // update
        MergeSqlSegment updateTemplate = new MergeSqlSegment();
        updateTemplate.addSegment(UPDATE);

        // tableName
        TableMapping<?> tableMapping = this.getTableMapping();
        String catalogName = tableMapping.getCatalog();
        String schemaName = tableMapping.getSchema();
        String tableName = tableMapping.getTable();
        String table = dialect.tableName(isQualifier(), catalogName, schemaName, tableName);
        updateTemplate.addSegment(() -> table);

        // SET
        updateTemplate.addSegment(SET);
        boolean isFirstColumn = true;
        for (String propertyName : updateValueMap.keySet()) {
            if (isFirstColumn) {
                isFirstColumn = false;
            } else {
                updateTemplate.addSegment(() -> ",");
            }

            ColumnMapping mapping = allowUpdateProperties.get(propertyName);

            String specialName = mapping.getSetColTemplate();
            String colName = StringUtils.isNotBlank(specialName) ? specialName : dialect.fmtName(isQualifier(), mapping.getColumn());

            String specialValue = mapping.getSetValueTemplate();
            String colValue = StringUtils.isNotBlank(specialValue) ? specialValue : "?";

            Object columnValue = updateValueMap.get(propertyName);
            updateTemplate.addSegment(() -> colName, EQ, formatSegment(colValue, columnValue));
        }

        // WHERE
        if (!this.queryTemplate.isEmpty()) {
            updateTemplate.addSegment(WHERE);
            updateTemplate.addSegment(this.queryTemplate.sub(1));
        } else if (!this.allowEmptyWhere) {
            throw new UnsupportedOperationException("The dangerous UPDATE operation, You must call `allowEmptyWhere()` to enable UPDATE ALL.");
        }

        String sqlQuery = updateTemplate.getSqlSegment();
        Object[] args = this.queryParam.toArray().clone();
        return new BoundSql.BoundSqlObj(sqlQuery, args);
    }

}
