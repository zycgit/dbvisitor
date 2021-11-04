/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.db.dal.session;
import net.hasor.db.lambda.DuplicateKeyStrategy;
import net.hasor.db.lambda.LambdaOperations;
import net.hasor.db.lambda.LambdaOperations.LambdaDelete;
import net.hasor.db.lambda.LambdaOperations.LambdaQuery;
import net.hasor.db.lambda.LambdaOperations.LambdaUpdate;
import net.hasor.db.lambda.QueryCompare;
import net.hasor.db.lambda.core.LambdaTemplate;
import net.hasor.db.mapping.def.ColumnMapping;
import net.hasor.db.mapping.def.TableMapping;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * BaseMapper 接口的实现类。
 * @version : 2021-05-19
 * @author 赵永春 (zyc@hasor.net)
 */
class BaseMapperHandler implements BaseMapper<Object> {
    private final Class<Object>        entityType;
    private final DalSession           dalSession;
    private final TableMapping<Object> tableMapping;

    public BaseMapperHandler(String space, Class<?> entityType, DalSession dalSession) {
        this.entityType = (Class<Object>) entityType;
        this.dalSession = dalSession;
        this.tableMapping = dalSession.getDalRegistry().findTableMapping(space, this.entityType);
    }

    @Override
    public Class<Object> entityType() {
        return this.entityType;
    }

    @Override
    public LambdaTemplate lambdaTemplate() {
        return this.dalSession.lambdaTemplate();
    }

    private TableMapping<Object> getMapping() {
        return this.tableMapping;
    }

    protected List<ColumnMapping> foundPrimaryKey() {
        TableMapping<Object> tableMapping = getMapping();
        return tableMapping.getProperties().stream().filter(ColumnMapping::isPrimaryKey).collect(Collectors.toList());
    }

    @Override
    public int insert(Object entity, DuplicateKeyStrategy strategy) throws SQLException {
        if (entity == null) {
            return 0;
        }

        strategy = (strategy == null) ? DuplicateKeyStrategy.Into : strategy;
        return insert().onDuplicateStrategy(strategy).applyEntity(entity).executeSumResult();
    }

    @Override
    public int insertBatch(List<Object> entity, DuplicateKeyStrategy strategy) throws SQLException {
        if (entity == null || entity.isEmpty()) {
            return 0;
        }

        strategy = (strategy == null) ? DuplicateKeyStrategy.Into : strategy;
        return insert().onDuplicateStrategy(strategy).applyEntity(entity).executeSumResult();
    }

    @Override
    public int insertByColumn(Map<String, Object> columnMap, DuplicateKeyStrategy strategy) throws SQLException {
        if (columnMap == null || columnMap.isEmpty()) {
            return 0;
        }

        strategy = (strategy == null) ? DuplicateKeyStrategy.Into : strategy;
        return insert().onDuplicateStrategy(strategy).applyMap(columnMap).executeSumResult();
    }

    @Override
    public int insertByColumnBatch(List<Map<String, Object>> columnMapList, DuplicateKeyStrategy strategy) throws SQLException {
        if (columnMapList == null || columnMapList.isEmpty()) {
            return 0;
        }

        strategy = (strategy == null) ? DuplicateKeyStrategy.Into : strategy;
        return insert().onDuplicateStrategy(strategy).applyMap(columnMapList).executeSumResult();
    }

    @Override
    public int deleteById(Serializable id) throws SQLException {
        if (id == null) {
            return 0;
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new SQLException(entityType() + " no primary key is identified");
        }

        LambdaDelete<Object> delete = delete();
        if (pks.size() == 1) {
            delete.and().eq(pks.get(0).getColumn(), id);
        } else {
            for (ColumnMapping pk : pks) {
                Object o = pk.getHandler().get(id);
                if (o == null) {
                    delete.and().isNull(pk.getColumn());
                } else {
                    delete.and().eq(pk.getColumn(), o);
                }
            }
        }

        return delete.doDelete();
    }

    @Override
    public int deleteBySample(Object sample) throws SQLException {
        if (sample == null) {
            return 0;
        }

        LambdaDelete<Object> delete = delete();
        for (ColumnMapping mapping : getMapping().getProperties()) {
            Object value = mapping.getHandler().get(sample);
            if (value != null) {
                delete.and().eq(mapping.getColumn(), value);
            }
        }

        return delete.doDelete();
    }

    @Override
    public int deleteByColumn(Map<String, Object> columnMap) throws SQLException {
        if (columnMap == null || columnMap.isEmpty()) {
            return 0;
        }

        LambdaDelete<Object> delete = delete();
        for (String columnKey : columnMap.keySet()) {
            Object val = columnMap.get(columnKey);
            if (val == null) {
                delete.and().isNull(columnKey);
            } else {
                delete.and().eq(columnKey, val);
            }
        }

        return delete.doDelete();
    }

    @Override
    public int deleteByCondition(Consumer<QueryCompare<Object, LambdaDelete<Object>>> queryCompare) throws SQLException {
        if (queryCompare == null) {
            throw new NullPointerException("queryCompare is null or empty.");
        }

        return delete().and(queryCompare).doDelete();
    }

    @Override
    public int deleteBatchIds(List<? extends Serializable> idList) throws SQLException {
        if (idList == null || idList.isEmpty()) {
            return 0;
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new SQLException(entityType() + " no primary key is identified");
        }

        if (pks.size() == 1) {
            LambdaDelete<Object> delete = delete();
            return delete.and().in(pks.get(0).getColumn(), idList).doDelete();
        } else {
            LambdaDelete<Object> delete = delete();
            for (Object obj : idList) {
                delete.or(queryCompare -> {
                    for (ColumnMapping pkColumn : pks) {
                        Object keyValue = pkColumn.getHandler().get(obj);
                        if (keyValue == null) {
                            queryCompare.and().isNull(pkColumn.getColumn());
                        } else {
                            queryCompare.and().eq(pkColumn.getColumn(), keyValue);
                        }
                    }
                });
            }
            return delete.doDelete();
        }
    }

    @Override
    public int updateById(Object entity) throws SQLException {
        if (entity == null) {
            throw new NullPointerException("entity is null.");
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new SQLException(entityType() + " no primary key is identified");
        }

        LambdaUpdate<Object> update = update();
        for (ColumnMapping pk : pks) {
            Object o = pk.getHandler().get(entity);
            if (o == null) {
                update.and().isNull(pk.getColumn());
            } else {
                update.and().eq(pk.getColumn(), o);
            }
        }
        return update.updateTo(entity).doUpdate();
    }

    @Override
    public int updateBySample(Object sample, Object entity) throws SQLException {
        if (sample == null || entity == null) {
            throw new NullPointerException("sample or entity is null.");
        }

        LambdaUpdate<Object> update = update();
        for (ColumnMapping mapping : getMapping().getProperties()) {
            Object value = mapping.getHandler().get(sample);
            if (value != null) {
                update.and().eq(mapping.getColumn(), value);
            }
        }

        return update.updateTo(entity).doUpdate();
    }

    @Override
    public int updateByIds(Object entity, List<? extends Serializable> idList) throws SQLException {
        if (idList == null || idList.isEmpty()) {
            return 0;
        }

        if (entity == null) {
            throw new NullPointerException("entity is null.");
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new SQLException(entityType() + " no primary key is identified");
        }

        if (pks.size() == 1) {
            LambdaUpdate<Object> update = update();
            return update.and().in(pks.get(0).getColumn(), idList).doUpdate();
        } else {
            LambdaUpdate<Object> update = update();
            for (Object obj : idList) {
                update.or(queryCompare -> {
                    for (ColumnMapping pkColumn : pks) {
                        Object val = pkColumn.getHandler().get(obj);
                        if (val == null) {
                            queryCompare.and().isNull(pkColumn.getColumn());
                        } else {
                            queryCompare.and().eq(pkColumn.getColumn(), val);
                        }
                    }
                });
            }
            return update.updateTo(entity).doUpdate();
        }
    }

    @Override
    public int updateByCondition(Object entity, Consumer<QueryCompare<Object, LambdaUpdate<Object>>> queryCompare) throws SQLException {
        if (queryCompare == null || entity == null) {
            throw new NullPointerException("queryCompare or entity is null.");
        }
        return update().and(queryCompare).updateTo(entity).doUpdate();
    }

    @Override
    public int updateByCondition(Map<String, Object> columnMap, Consumer<QueryCompare<Object, LambdaUpdate<Object>>> queryCompare) throws SQLException {
        if (columnMap == null || columnMap.isEmpty()) {
            return 0;
        }

        LambdaUpdate<Object> update = update();
        for (String columnKey : columnMap.keySet()) {
            Object val = columnMap.get(columnKey);
            if (val == null) {
                update.and().isNull(columnKey);
            } else {
                update.and().eq(columnKey, val);
            }
        }

        return update.doUpdate();
    }

    protected LambdaQuery<Object> buildQueryById(Serializable id) throws SQLException {
        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new SQLException(entityType() + " no primary key is identified");
        }

        LambdaQuery<Object> query = query();
        if (pks.size() == 1) {
            query.and().eq(pks.get(0).getColumn(), id);
        } else {
            for (ColumnMapping pk : pks) {
                Object o = pk.getHandler().get(id);
                if (o == null) {
                    query.and().isNull(pk.getColumn());
                } else {
                    query.and().eq(pk.getColumn(), o);
                }
            }
        }

        return query;
    }

    protected LambdaQuery<Object> buildQueryBySample(Object sample) {
        if (sample == null) {
            throw new NullPointerException("sample is null.");
        }

        LambdaQuery<Object> query = query();
        for (ColumnMapping mapping : getMapping().getProperties()) {
            Object value = mapping.getHandler().get(sample);
            if (value != null) {
                query.and().eq(mapping.getColumn(), value);
            }
        }

        return query;
    }

    protected LambdaQuery<Object> buildQueryByColumn(Map<String, Object> columnMap) {

        LambdaQuery<Object> query = query();
        for (String columnKey : columnMap.keySet()) {
            Object val = columnMap.get(columnKey);
            if (val != null) {
                query.and().isNull(columnKey);
            } else {
                query.and().eq(columnKey, val);
            }
        }

        return query;
    }

    protected LambdaQuery<Object> buildQueryByIds(List<? extends Serializable> idList) throws SQLException {
        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new SQLException(entityType() + " no primary key is identified");
        }

        if (pks.size() == 1) {
            LambdaQuery<Object> query = query();
            return query.and().in(pks.get(0).getColumn(), idList);
        } else {
            LambdaQuery<Object> query = query();
            for (Object obj : idList) {
                query.or(queryCompare -> {
                    for (ColumnMapping pkColumn : pks) {
                        Object keyValue = pkColumn.getHandler().get(obj);
                        if (keyValue == null) {
                            queryCompare.and().isNull(pkColumn.getColumn());
                        } else {
                            queryCompare.and().eq(pkColumn.getColumn(), keyValue);
                        }
                    }
                });
            }
            return query;
        }
    }

    @Override
    public Object queryById(Serializable id) throws SQLException {
        if (id == null) {
            return null;
        } else {
            return buildQueryById(id).queryForObject();
        }
    }

    @Override
    public List<Object> queryBySample(Object sample) throws SQLException {
        return buildQueryBySample(sample).queryForList();
    }

    @Override
    public List<Object> queryByIds(List<? extends Serializable> idList) throws SQLException {
        if (idList == null || idList.isEmpty()) {
            return Collections.emptyList();
        }

        return buildQueryByIds(idList).queryForList();
    }

    @Override
    public List<Object> queryByCondition(Consumer<QueryCompare<Object, LambdaQuery<Object>>> queryCompare) throws SQLException {
        if (queryCompare == null) {
            throw new NullPointerException("queryCompare or entity is null.");
        } else {
            return query().and(queryCompare).queryForList();
        }
    }

    @Override
    public Map<String, Object> queryMapById(Serializable id) throws SQLException {
        if (id == null) {
            return null;
        } else {
            return buildQueryById(id).queryForMap();
        }
    }

    @Override
    public List<Map<String, Object>> queryMapBySample(Object sample) throws SQLException {
        return buildQueryBySample(sample).queryForMapList();
    }

    @Override
    public List<Map<String, Object>> queryMapBatchIds(List<? extends Serializable> idList) throws SQLException {
        if (idList == null || idList.isEmpty()) {
            return Collections.emptyList();
        }

        return buildQueryByIds(idList).queryForMapList();
    }

    @Override
    public List<Map<String, Object>> queryMapByCondition(Consumer<QueryCompare<Object, LambdaOperations.LambdaQuery<Object>>> queryCompare) throws SQLException {
        if (queryCompare == null) {
            throw new NullPointerException("queryCompare or entity is null.");
        } else {
            return query().and(queryCompare).queryForMapList();
        }
    }

    @Override
    public int countAll() throws SQLException {
        return query().queryForCount();
    }

    @Override
    public int countBySample(Object sample) throws SQLException {
        return buildQueryBySample(sample).queryForCount();
    }

    @Override
    public int countByColumn(Map<String, Object> columnMap) throws SQLException {
        return buildQueryByColumn(columnMap).queryForCount();
    }

    @Override
    public int countByCondition(Consumer<QueryCompare<Object, LambdaOperations.LambdaQuery<Object>>> queryCompare) throws SQLException {
        if (queryCompare == null) {
            throw new NullPointerException("queryCompare or entity is null.");
        } else {
            return query().and(queryCompare).queryForCount();
        }
    }
}
