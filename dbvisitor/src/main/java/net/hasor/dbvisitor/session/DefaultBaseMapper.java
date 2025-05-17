/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.session;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageObject;
import net.hasor.dbvisitor.dialect.PageResult;
import net.hasor.dbvisitor.error.RuntimeSQLException;
import net.hasor.dbvisitor.jdbc.JdbcOperations;
import net.hasor.dbvisitor.lambda.EntityDelete;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.lambda.EntityUpdate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BaseMapper 接口的实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-19
 */
class DefaultBaseMapper implements BaseMapper<Object> {
    private final Class<Object>        entityType;
    private final Session              session;
    private final TableMapping<Object> tabMapping;

    DefaultBaseMapper(TableMapping<Object> tabMapping, Session session) {
        this.entityType = tabMapping.entityType();
        this.session = session;
        this.tabMapping = tabMapping;

        Objects.requireNonNull(this.tabMapping, "entityType '" + entityType + "' undefined.");
    }

    @Override
    public Class<Object> entityType() {
        return this.entityType;
    }

    @Override
    public LambdaTemplate lambda() {
        return this.session.lambda();
    }

    @Override
    public JdbcOperations jdbc() {
        return this.session.jdbc();
    }

    @Override
    public Session session() {
        return this.session;
    }

    private TableMapping<Object> getMapping() {
        return this.tabMapping;
    }

    protected List<ColumnMapping> foundPrimaryKey() {
        return this.getMapping().getProperties().stream().filter(ColumnMapping::isPrimaryKey).collect(Collectors.toList());
    }

    @Override
    public int update(Object entity) throws RuntimeSQLException {
        if (entity == null) {
            throw new NullPointerException("entity is null.");
        }

        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        EntityUpdate<Object> update = this.update();

        for (ColumnMapping pk : pks) {
            update.and().eq(pk.getProperty(), pk.getHandler().get(entity));
        }

        try {
            TableMapping<Object> tableMapping = this.getMapping();
            return update.updateToSample(entity, property -> {
                return !tableMapping.getPropertyByName(property).isPrimaryKey();
            }).doUpdate();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int upsert(Object entity) throws RuntimeSQLException {
        if (entity == null) {
            throw new NullPointerException("entity is null.");
        }

        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        EntityQuery<Object> query = this.query();
        EntityUpdate<Object> update = this.update();

        for (ColumnMapping pk : pks) {
            Object o = pk.getHandler().get(entity);
            query.and().eq(pk.getProperty(), o);
            update.and().eq(pk.getProperty(), o);
        }

        try {
            if (query.queryForCount() == 0) {
                return this.insert().applyEntity(entity).executeSumResult();
            } else {
                TableMapping<Object> tableMapping = this.getMapping();
                return update.updateToSample(entity, property -> {
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

        Map<String, Object> copy = new LinkedHashMap<>(map);
        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        EntityUpdate<Object> update = this.update();

        boolean isPrimaryKeyEmpty = true;
        for (ColumnMapping pk : pks) {
            String key = pk.getProperty();
            update.and().eq(key, copy.get(key));

            copy.remove(key);
            isPrimaryKeyEmpty = false;
        }

        if (isPrimaryKeyEmpty) {
            throw new NullPointerException("primary key is empty.");
        }

        if (copy.isEmpty()) {
            throw new NullPointerException("map is empty.");
        }

        try {
            TableMapping<Object> tableMapping = this.getMapping();
            return update.updateToSampleMap(copy, property -> {
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

        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        EntityDelete<Object> delete = this.delete();

        for (ColumnMapping pk : pks) {
            delete.and().eq(pk.getProperty(), pk.getHandler().get(entity));
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

        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        EntityDelete<Object> delete = this.delete();
        if (pks.size() == 1) {
            delete.and().eq(pks.get(0).getProperty(), id);
        } else {
            for (ColumnMapping pk : pks) {
                delete.and().eq(pk.getProperty(), pk.getHandler().get(id));
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

        List<ColumnMapping> pks = this.foundPrimaryKey();
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
            EntityDelete<Object> delete = this.delete();
            for (Object obj : idList) {
                delete.or(c -> {
                    for (ColumnMapping pk : pks) {
                        c.and().eq(pk.getProperty(), pk.getHandler().get(obj));
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

        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        EntityQuery<Object> query = this.query();
        if (pks.size() == 1) {
            query.and().eq(pks.get(0).getProperty(), id);
        } else {
            for (ColumnMapping pk : pks) {
                query.and().eq(pk.getProperty(), pk.getHandler().get(id));
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

        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new RuntimeSQLException(entityType() + " no primary key is identified");
        }

        if (pks.size() == 1) {
            try {
                return this.query().and().in(pks.get(0).getProperty(), idList).queryForList();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e);
            }
        } else {
            EntityQuery<Object> query = this.query();
            for (Object obj : idList) {
                query.or(c -> {
                    for (ColumnMapping pk : pks) {
                        c.and().eq(pk.getProperty(), pk.getHandler().get(obj));
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

    protected EntityQuery<Object> buildQueryBySample(Object sample) {
        EntityQuery<Object> query = this.query();

        if (sample != null) {
            for (ColumnMapping mapping : this.getMapping().getProperties()) {
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
        if (sample == null) {
            return Collections.emptyList();
        }

        try {
            return this.buildQueryBySample(sample).queryForList();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int countBySample(Object sample) throws RuntimeSQLException {
        if (sample == null) {
            return -1;
        }

        try {
            return this.buildQueryBySample(sample).queryForCount();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int countAll() throws RuntimeSQLException {
        try {
            return this.query().queryForCount();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public PageResult<Object> pageBySample(Object sample, Page page) throws RuntimeSQLException {
        try {
            List<Object> result = this.buildQueryBySample(sample).usePage(page).queryForList();
            return new PageResult<>(page, result);
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public Page initPageBySample(Object sample, int pageSize, int pageNumberOffset) throws RuntimeSQLException {
        int totalCount = this.countBySample(sample);
        PageObject pageObject = new PageObject(pageSize, totalCount);
        pageObject.setPageNumberOffset(pageNumberOffset);
        return pageObject;
    }
}