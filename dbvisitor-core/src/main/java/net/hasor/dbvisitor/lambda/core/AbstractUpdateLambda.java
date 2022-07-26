/*
 * Copyright 2002-2010 the original author or authors.
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
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.segment.MergeSqlSegment;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;

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
public abstract class AbstractUpdateLambda<R, T, P> extends BasicQueryCompare<R, T, P> implements UpdateExecute<R, T> {
    protected final Map<String, ColumnMapping> allowUpdateProperties;
    protected final Map<String, Object>        updateValueMap;
    private         boolean                    allowEmptyWhere = false;

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
    public int doUpdate() throws SQLException {
        if (this.updateValueMap.isEmpty()) {
            throw new IllegalStateException("Nothing to update.");
        }

        BoundSql boundSql = getBoundSql();
        return this.getJdbcTemplate().executeUpdate(boundSql.getSqlString(), boundSql.getArgs());
    }

    @Override
    public R updateBySample(final T newValue) {
        if (newValue == null) {
            throw new NullPointerException("newValue is null.");
        }

        Map<String, Object> tempData = new HashMap<>();
        for (Map.Entry<String, ColumnMapping> mappingEntry : this.allowUpdateProperties.entrySet()) {
            Object value = mappingEntry.getValue().getHandler().get(newValue);
            if (value != null) {
                tempData.put(mappingEntry.getKey(), value);
            }
        }

        return this.updateToByCondition(tempData::containsKey, tempData::get);
    }

    @Override
    public R updateByMap(Map<String, Object> newValue) {
        if (newValue == null) {
            throw new NullPointerException("newValue is null.");
        }

        return this.updateToByCondition(newValue::containsKey, newValue::get);
    }

    @Override
    public R updateTo(T newValue) {
        if (newValue == null) {
            throw new NullPointerException("newValue is null.");
        }

        return this.updateToByCondition(p -> true, createPropertyReaderFunc(newValue));
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

    protected R updateToByCondition(Predicate<String> propertyTester, Function<String, Object> propertyReader) {
        this.updateValueMap.clear();
        Set<String> updateColumns = new HashSet<>();
        for (Map.Entry<String, ColumnMapping> allowFieldEntry : this.allowUpdateProperties.entrySet()) {
            ColumnMapping allowProperty = allowFieldEntry.getValue();
            if (!propertyTester.test(allowProperty.getProperty())) {
                continue;
            }

            String columnName = allowProperty.getColumn();
            String propertyName = allowProperty.getProperty();
            if (updateColumns.contains(columnName)) {
                throw new IllegalStateException("Multiple property mapping to '" + columnName + "' column");
            }

            updateColumns.add(columnName);
            Object propertyValue = propertyReader.apply(allowProperty.getProperty());
            this.updateValueMap.put(propertyName, propertyValue);
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
            String columnName = dialect.columnName(isQualifier(), catalogName, schemaName, tableName, mapping.getColumn());
            Object columnValue = updateValueMap.get(propertyName);
            updateTemplate.addSegment(() -> columnName, EQ, formatSegment(columnValue));
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
