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
package net.hasor.dbvisitor.dal.session;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.lambda.EntityDeleteOperation;
import net.hasor.dbvisitor.lambda.EntityQueryOperation;
import net.hasor.dbvisitor.lambda.EntityUpdateOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * BaseMapper 接口的实现类。
 * @version : 2021-05-19
 * @author 赵永春 (zyc@hasor.net)
 */
class BaseMapperHandler implements BaseMapper<Object> {
    private final String               space;
    private final Class<Object>        entityType;
    private final DalSession           dalSession;
    private final LambdaTemplate       template;
    private final TableMapping<Object> tableMapping;

    public BaseMapperHandler(String space, Class<?> entityType, DalSession dalSession) {
        this.space = space;
        this.entityType = (Class<Object>) entityType;
        this.dalSession = dalSession;
        this.template = dalSession.newTemplate(this.space);
        this.tableMapping = dalSession.getDalRegistry().findMapping(space, this.entityType);

        Objects.requireNonNull(this.tableMapping, "entityType '" + entityType + "' undefined.");
    }

    @Override
    public Class<Object> entityType() {
        return this.entityType;
    }

    @Override
    public LambdaTemplate template() {
        return this.template;
    }

    @Override
    public DalSession getSession() {
        return this.dalSession;
    }

    private TableMapping<Object> getMapping() {
        return this.tableMapping;
    }

    protected List<ColumnMapping> foundPrimaryKey() {
        TableMapping<Object> tableMapping = getMapping();
        return tableMapping.getProperties().stream().filter(ColumnMapping::isPrimaryKey).collect(Collectors.toList());
    }

    @Override
    public int updateById(Object entity) throws RuntimeSQLException {
        if (entity == null) {
            throw new NullPointerException("entity is null.");
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        EntityUpdateOperation<Object> update = update();

        for (ColumnMapping pk : pks) {
            Object o = pk.getHandler().get(entity);
            if (o == null) {
                update.and().isNull(pk.getProperty());
            } else {
                update.and().eq(pk.getProperty(), o);
            }
        }

        try {
            TableMapping<Object> tableMapping = getMapping();
            return update.updateToSampleCondition(entity, property -> {
                return !tableMapping.getPropertyByName(property).isPrimaryKey();
            }).doUpdate();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int upsertById(Object entity) throws RuntimeSQLException {
        if (entity == null) {
            throw new NullPointerException("entity is null.");
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        EntityQueryOperation<Object> query = query();
        EntityUpdateOperation<Object> update = update();

        for (ColumnMapping pk : pks) {
            Object o = pk.getHandler().get(entity);
            if (o == null) {
                query.and().isNull(pk.getProperty());
                update.and().isNull(pk.getProperty());
            } else {
                query.and().eq(pk.getProperty(), o);
                update.and().eq(pk.getProperty(), o);
            }
        }

        try {
            if (query.queryForCount() == 0) {
                return insert().applyEntity(entity).executeSumResult();
            } else {
                TableMapping<Object> tableMapping = getMapping();
                return update.updateToSampleCondition(entity, property -> {
                    return !tableMapping.getPropertyByName(property).isPrimaryKey();
                }).doUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int updateByMap(Map<String, Object> map) throws RuntimeSQLException {
        if (map == null) {
            throw new NullPointerException("map is null.");
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        EntityUpdateOperation<Object> update = update();

        boolean isPrimaryKeyEmpty = true;
        for (ColumnMapping pk : pks) {
            String key = pk.getProperty();
            Object o = map.get(key);

            if (o == null) {
                update.and().isNull(key);
            } else {
                update.and().eq(key, o);
            }

            map.remove(key);
            isPrimaryKeyEmpty = false;
        }

        if (isPrimaryKeyEmpty) {
            throw new NullPointerException("primary key is empty.");
        }

        if (map.size() == 0) {
            throw new NullPointerException("map is empty.");
        }

        try {
            TableMapping<Object> tableMapping = getMapping();
            return update.updateToMapCondition(map, property -> {
                return !tableMapping.getPropertyByName(property).isPrimaryKey();
            }).doUpdate();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int delete(Object entity) throws RuntimeSQLException {
        if (entity == null) {
            throw new NullPointerException("entity is null.");
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        EntityDeleteOperation<Object> delete = delete();

        for (ColumnMapping pk : pks) {
            Object o = pk.getHandler().get(entity);
            if (o == null) {
                delete.and().isNull(pk.getProperty());
            } else {
                delete.and().eq(pk.getProperty(), o);
            }
        }

        try {
            return delete.doDelete();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int deleteById(Serializable id) throws RuntimeSQLException {
        if (id == null) {
            return 0;
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        EntityDeleteOperation<Object> delete = delete();
        if (pks.size() == 1) {
            delete.and().eq(pks.get(0).getProperty(), id);
        } else {
            for (ColumnMapping pk : pks) {
                Object o = pk.getHandler().get(id);
                if (o == null) {
                    delete.and().isNull(pk.getProperty());
                } else {
                    delete.and().eq(pk.getProperty(), o);
                }
            }
        }

        try {
            return delete.doDelete();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int deleteByIds(List<? extends Serializable> idList) throws RuntimeSQLException {
        if (idList == null || idList.isEmpty()) {
            return 0;
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        if (pks.size() == 1) {
            try {
                return delete().and().in(pks.get(0).getProperty(), idList).doDelete();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e);
            }
        } else {
            EntityDeleteOperation<Object> delete = delete();
            for (Object obj : idList) {
                delete.or(queryCompare -> {
                    for (ColumnMapping pkColumn : pks) {
                        Object keyValue = pkColumn.getHandler().get(obj);
                        if (keyValue == null) {
                            queryCompare.and().isNull(pkColumn.getProperty());
                        } else {
                            queryCompare.and().eq(pkColumn.getProperty(), keyValue);
                        }
                    }
                });
            }

            try {
                return delete.doDelete();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e);
            }
        }
    }

    @Override
    public Object selectById(Serializable id) throws RuntimeSQLException {
        if (id == null) {
            return null;
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        EntityQueryOperation<Object> query = query();
        if (pks.size() == 1) {
            query.and().eq(pks.get(0).getProperty(), id);
        } else {
            for (ColumnMapping pk : pks) {
                Object o = pk.getHandler().get(id);
                if (o == null) {
                    query.and().isNull(pk.getProperty());
                } else {
                    query.and().eq(pk.getProperty(), o);
                }
            }
        }

        try {
            return query.queryForObject();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public List<Object> selectByIds(List<? extends Serializable> idList) throws RuntimeSQLException {
        if (idList == null || idList.isEmpty()) {
            return Collections.emptyList();
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        if (pks.size() == 1) {
            try {
                return query().and().in(pks.get(0).getProperty(), idList).queryForList();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e);
            }
        } else {
            EntityQueryOperation<Object> query = query();
            for (Object obj : idList) {
                query.or(queryCompare -> {
                    for (ColumnMapping pkColumn : pks) {
                        Object keyValue = pkColumn.getHandler().get(obj);
                        if (keyValue == null) {
                            queryCompare.and().isNull(pkColumn.getProperty());
                        } else {
                            queryCompare.and().eq(pkColumn.getProperty(), keyValue);
                        }
                    }
                });
            }

            try {
                return query.queryForList();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e);
            }
        }
    }

    protected EntityQueryOperation<Object> buildQueryBySample(Object sample) {
        EntityQueryOperation<Object> query = query();

        if (sample != null) {
            for (ColumnMapping mapping : getMapping().getProperties()) {
                Object value = mapping.getHandler().get(sample);
                if (value != null) {
                    query.and().eq(mapping.getProperty(), value);
                }
            }
        }

        return query;
    }

    @Override
    public List<Object> listBySample(Object sample) throws RuntimeSQLException {
        try {
            return buildQueryBySample(sample).queryForList();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int countBySample(Object sample) throws RuntimeSQLException {
        try {
            return buildQueryBySample(sample).queryForCount();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int countAll() throws RuntimeSQLException {
        try {
            return query().queryForCount();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public PageResult<Object> pageBySample(Object sample, Page page) throws RuntimeSQLException {
        try {
            List<Object> result = buildQueryBySample(sample).usePage(page).queryForList();
            return new PageResult<>(page, result);
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public Page initPageBySample(Object sample, int pageSize, int pageNumberOffset) throws RuntimeSQLException {
        int totalCount = countBySample(sample);
        PageObject pageObject = new PageObject(pageSize, totalCount);
        pageObject.setPageNumberOffset(pageNumberOffset);
        return pageObject;
    }

}
