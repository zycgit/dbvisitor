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
package net.hasor.db.lambda.core;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.db.dialect.BoundSql;
import net.hasor.db.lambda.LambdaOperations.LambdaUpdate;
import net.hasor.db.lambda.UpdateExecute;
import net.hasor.db.lambda.segment.MergeSqlSegment;
import net.hasor.db.mapping.TableReader;
import net.hasor.db.mapping.def.ColumnMapping;
import net.hasor.db.mapping.def.TableMapping;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.hasor.db.lambda.segment.SqlKeyword.*;

/**
 * 提供 lambda update 能力，是 LambdaUpdate 接口的实现类。
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public class LambdaUpdateWrapper<T> extends AbstractQueryCompare<T, LambdaUpdate<T>> implements LambdaUpdate<T> {
    protected final Map<String, ColumnMapping> allowUpdateProperties;
    protected final Map<String, Object>        updateValueMap;
    private         boolean                    allowEmptyWhere = false;

    public LambdaUpdateWrapper(TableReader<T> tableReader, LambdaTemplate jdbcTemplate) {
        super(tableReader, jdbcTemplate);

        this.allowUpdateProperties = new LinkedHashMap<>();
        TableMapping<T> tableMapping = super.getTableMapping();
        for (ColumnMapping mapping : tableMapping.getProperties()) {
            if (mapping.isUpdate()) {
                this.allowUpdateProperties.put(mapping.getProperty(), mapping);
            }
        }

        this.updateValueMap = new HashMap<>();
    }

    @Override
    protected boolean supportPage() {
        return false;// update is disable Page;
    }

    @Override
    protected LambdaUpdate<T> getSelf() {
        return this;
    }

    @Override
    public LambdaUpdate<T> useQualifier() {
        this.enableQualifier();
        return this;
    }

    @Override
    public UpdateExecute<T> allowEmptyWhere() {
        this.allowEmptyWhere = true;
        return this;
    }

    @Override
    public UpdateExecute<T> updateTo(Map<String, Object> newValue) {
        Predicate<ColumnMapping> tester = m -> true;
        Function<ColumnMapping, Object> reader = m -> newValue.get(m.getProperty());

        return this.updateTo(tester, reader);
    }

    @Override
    public UpdateExecute<T> updateTo(Map<String, Object> newValue, SFunction<T>... properties) {
        List<String> collect = Arrays.stream(properties).map(BeanUtils::toProperty).collect(Collectors.toList());

        Predicate<ColumnMapping> tester = m -> collect.contains(m.getProperty());
        Function<ColumnMapping, Object> reader = m -> newValue.get(m.getProperty());

        return this.updateTo(tester, reader);
    }

    @Override
    public UpdateExecute<T> updateTo(T newValue) {
        if (newValue == null) {
            throw new NullPointerException("newValue is null.");
        }

        Predicate<ColumnMapping> tester = m -> true;
        Function<ColumnMapping, Object> reader = m -> m.getHandler().get(newValue);

        return this.updateTo(tester, reader);
    }

    @Override
    public UpdateExecute<T> updateTo(T newValue, SFunction<T>... properties) {
        if (newValue == null) {
            throw new NullPointerException("newValue is null.");
        }
        if (properties == null || properties.length == 0) {
            throw new NullPointerException("properties is empty.");
        }

        List<String> collect = Arrays.stream(properties).map(BeanUtils::toProperty).collect(Collectors.toList());

        Predicate<ColumnMapping> tester = m -> collect.contains(m.getProperty());
        Function<ColumnMapping, Object> reader = m -> m.getHandler().get(newValue);

        return this.updateTo(tester, reader);
    }

    protected UpdateExecute<T> updateTo(Predicate<ColumnMapping> tester, Function<ColumnMapping, Object> propertyReader) {
        if (tester == null) {
            throw new NullPointerException("tester is null.");
        }

        this.updateValueMap.clear();
        Set<String> updateColumns = new HashSet<>();
        for (Map.Entry<String, ColumnMapping> allowFieldEntry : this.allowUpdateProperties.entrySet()) {
            ColumnMapping allowProperty = allowFieldEntry.getValue();
            if (!tester.test(allowProperty)) {
                continue;
            }
            String columnName = allowProperty.getColumn();
            String propertyName = allowProperty.getProperty();
            if (updateColumns.contains(columnName)) {
                throw new IllegalStateException("Multiple property mapping to '" + columnName + "' column");
            } else {
                updateColumns.add(columnName);
                Object propertyValue = propertyReader.apply(allowProperty);
                this.updateValueMap.put(propertyName, propertyValue);
            }
        }
        return this;
    }

    @Override
    public BoundSql getOriginalBoundSql() {
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
        TableMapping<T> tableMapping = this.getTableMapping();
        String schemaName = tableMapping.getSchema();
        String tableName = tableMapping.getTable();

        String table = dialect().tableName(isQualifier(), schemaName, tableName);
        updateTemplate.addSegment(() -> table);
        //
        updateTemplate.addSegment(SET);
        boolean isFirstColumn = true;
        for (String propertyName : updateValueMap.keySet()) {
            if (isFirstColumn) {
                isFirstColumn = false;
            } else {
                updateTemplate.addSegment(() -> ",");
            }
            //
            ColumnMapping mapping = allowUpdateProperties.get(propertyName);
            String columnName = dialect().columnName(isQualifier(), schemaName, tableName, mapping.getColumn());
            Object columnValue = updateValueMap.get(propertyName);
            updateTemplate.addSegment(() -> columnName, EQ, formatSegment(columnValue));
        }
        //
        if (!this.queryTemplate.isEmpty()) {
            updateTemplate.addSegment(WHERE);
            updateTemplate.addSegment(this.queryTemplate.sub(1));
        } else if (!this.allowEmptyWhere) {
            throw new UnsupportedOperationException("The dangerous UPDATE operation, You must call `allowEmptyWhere()` to enable UPDATE ALL.");
        }
        //
        String sqlQuery = updateTemplate.getSqlSegment();
        Object[] args = this.queryParam.toArray().clone();
        return new BoundSql.BoundSqlObj(sqlQuery, args);
    }

    @Override
    public int doUpdate() throws SQLException {
        if (this.updateValueMap.isEmpty()) {
            throw new IllegalStateException("Nothing to update.");
        }
        BoundSql boundSql = getBoundSql();
        String sqlString = boundSql.getSqlString();
        return this.getJdbcTemplate().executeUpdate(sqlString, boundSql.getArgs());
    }
}
