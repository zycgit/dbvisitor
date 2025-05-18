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
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.function.Property;
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
import java.util.concurrent.atomic.AtomicInteger;
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
    public int replace(Object entity) throws RuntimeSQLException {
        if (entity == null) {
            throw new NullPointerException("entity is null.");
        } else if (entity instanceof Map) {
            return this.replaceByMap((Map<String, Object>) entity);
        } else if (!this.entityType.isInstance(entity)) {
            throw new ClassCastException("the type " + entity.getClass().getName() + " cannot be as " + entityType().getName());
        }

        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        }

        EntityUpdate<Object> update = this.update();
        for (ColumnMapping pk : pks) {
            update.and().eq(pk.getProperty(), pk.getHandler().get(entity));
        }

        try {
            TableMapping<Object> tableMapping = this.getMapping();
            return update.updateRow(entity, property -> {
                return !tableMapping.getPropertyByName(property).isPrimaryKey(); // 忽略主键
            }).doUpdate();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int replaceByMap(Map<String, Object> entity) throws RuntimeSQLException {
        if (entity == null) {
            throw new NullPointerException("entityMap is null.");
        }
        Map<String, Object> copy = new LinkedHashMap<>(entity);
        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        }

        EntityUpdate<Object> update = this.update();
        for (ColumnMapping pk : pks) {
            String key = pk.getProperty();
            update.and().eq(key, copy.get(key));
            copy.remove(key);
        }

        if (copy.isEmpty()) {
            return 0;
        }

        try {
            TableMapping<Object> tableMapping = this.getMapping();
            return update.updateRowUsingMap(copy, property -> {
                return !tableMapping.getPropertyByName(property).isPrimaryKey();
            }).doUpdate();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int update(Object sample) throws RuntimeSQLException {
        if (sample == null) {
            throw new NullPointerException("sample is null.");
        } else if (sample instanceof Map) {
            return this.updateByMap((Map<String, Object>) sample);
        } else if (!this.entityType.isInstance(sample)) {
            throw new ClassCastException("the type " + sample.getClass().getName() + " cannot be as " + entityType().getName());
        }

        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        }

        EntityUpdate<Object> update = this.update();
        for (ColumnMapping pk : pks) {
            update.and().eq(pk.getProperty(), pk.getHandler().get(sample));
        }

        try {
            TableMapping<Object> tableMapping = this.getMapping();
            return update.updateToSample(sample, property -> {
                return !tableMapping.getPropertyByName(property).isPrimaryKey(); // 忽略主键
            }).doUpdate();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int updateByMap(Map<String, Object> sample) throws RuntimeSQLException {
        if (sample == null) {
            throw new NullPointerException("sampleMap is null.");
        }
        Map<String, Object> copy = new LinkedHashMap<>();
        sample.forEach((p, v) -> {
            if (v != null) {
                copy.put(p, v);
            }
        });

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        }

        EntityUpdate<Object> update = this.update();
        for (ColumnMapping pk : pks) {
            String key = pk.getProperty();
            update.and().eq(key, copy.get(key));
            copy.remove(key);
        }

        if (copy.isEmpty()) {
            return 0;
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
    public int upsert(Object entity) throws RuntimeSQLException {
        if (entity == null) {
            throw new NullPointerException("entity is null.");
        } else if (entity instanceof Map) {
            return this.upsertByMap((Map<String, Object>) entity);
        } else if (!this.entityType.isInstance(entity)) {
            throw new ClassCastException("the type " + entity.getClass().getName() + " cannot be as " + entityType().getName());
        }

        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        }

        EntityQuery<Object> query = this.query();
        EntityUpdate<Object> update = this.update();
        for (ColumnMapping pk : pks) {
            query.and().eq(pk.getProperty(), pk.getHandler().get(entity));
            update.and().eq(pk.getProperty(), pk.getHandler().get(entity));
        }

        try {
            if (query.queryForCount() == 0) {
                return this.insert().applyEntity(entity).executeSumResult();
            } else {
                TableMapping<Object> tableMapping = this.getMapping();
                return update.updateRow(entity, property -> {
                    return !tableMapping.getPropertyByName(property).isPrimaryKey(); // 忽略主键
                }).doUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int upsertByMap(Map<String, Object> entity) throws RuntimeSQLException {
        if (entity == null) {
            throw new NullPointerException("entityMap is null.");
        }
        Map<String, Object> copy = new LinkedHashMap<>();
        entity.forEach((p, v) -> {
            if (v != null) {
                copy.put(p, v);
            }
        });

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        }

        EntityQuery<Object> query = this.query();
        EntityUpdate<Object> update = this.update();
        for (ColumnMapping pk : pks) {
            String key = pk.getProperty();
            update.and().eq(key, copy.get(key));
            query.and().eq(key, copy.get(key));
            copy.remove(key);
        }

        if (copy.isEmpty()) {
            return 0;
        }

        try {
            if (query.queryForCount() == 0) {
                return this.insert().applyEntity(entity).executeSumResult();
            } else {
                TableMapping<Object> tableMapping = this.getMapping();
                return update.updateRowUsingMap(copy, property -> {
                    return !tableMapping.getPropertyByName(property).isPrimaryKey();
                }).doUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int delete(Object entity) throws RuntimeSQLException {
        if (entity == null) {
            throw new NullPointerException("entity is null.");
        } else if (entity instanceof Map) {
            return this.deleteByMap((Map<String, Object>) entity);
        } else if (!this.entityType.isInstance(entity)) {
            throw new ClassCastException("the type " + entity.getClass().getName() + " cannot be as " + entityType().getName());
        }

        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
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
    public int deleteByMap(Map<String, Object> entity) throws RuntimeSQLException {
        if (entity == null) {
            throw new NullPointerException("entityMap is null.");
        }
        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        }

        EntityDelete<Object> delete = this.delete();
        boolean missingPrimary = false;
        for (ColumnMapping pk : pks) {
            String key = pk.getProperty();
            if (entity.containsKey(key)) {
                delete.and().eq(key, entity.get(key));
            } else {
                missingPrimary = true;
                break;
            }
        }
        if (missingPrimary) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        }

        try {
            return delete.doDelete();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int deleteList(List<Object> entityList) throws RuntimeSQLException {
        if (entityList == null) {
            throw new NullPointerException("entityList is null.");
        }

        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        }

        AtomicInteger counter = new AtomicInteger();
        EntityDelete<Object> delete = this.delete();
        boolean oneColumn = pks.size() == 1;
        for (Object obj : entityList) {
            if (obj == null) {
                continue;
            } else {
                counter.incrementAndGet();
            }

            if (oneColumn) {
                ColumnMapping pkc = pks.get(0);
                delete.or(c -> {
                    if (obj instanceof Map) {
                        c.eq(pkc.getProperty(), ((Map) obj).get(pkc.getProperty()));
                    } else if (this.entityType.isInstance(obj)) {
                        c.eq(pkc.getProperty(), pkc.getHandler().get(obj));
                    } else {
                        throw new ClassCastException("the type " + obj.getClass().getName() + " cannot be as " + entityType().getName());
                    }
                });
            } else {
                delete.or(c -> {
                    if (obj instanceof Map) {
                        for (ColumnMapping pkc : pks) {
                            c.eq(pkc.getProperty(), ((Map) obj).get(pkc.getProperty()));
                        }
                    } else if (this.entityType.isInstance(obj)) {
                        for (ColumnMapping pkc : pks) {
                            c.eq(pkc.getProperty(), pkc.getHandler().get(obj));
                        }
                    } else {
                        throw new ClassCastException("the type " + obj.getClass().getName() + " cannot be as " + entityType().getName());
                    }
                });
            }
        }

        try {
            return counter.get() == 0 ? 0 : delete.doDelete();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int deleteListByMap(List<Map<String, Object>> entityList) throws RuntimeSQLException {
        if (entityList == null) {
            throw new NullPointerException("entityList is null.");
        }

        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        }

        AtomicInteger counter = new AtomicInteger();
        EntityDelete<Object> delete = this.delete();
        for (Object obj : entityList) {
            if (obj == null) {
                continue;
            } else {
                counter.incrementAndGet();
            }

            if (pks.size() == 1) {
                ColumnMapping pkc = pks.get(0);
                delete.or(c -> {
                    c.eq(pkc.getProperty(), ((Map) obj).get(pkc.getProperty()));
                });
            } else {
                delete.or(c -> {
                    for (ColumnMapping pkc : pks) {
                        c.eq(pkc.getProperty(), ((Map) obj).get(pkc.getProperty()));
                    }
                });
            }
        }

        try {
            return counter.get() == 0 ? 0 : delete.doDelete();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int deleteById(Serializable id) throws RuntimeSQLException {
        if (id == null) {
            throw new NullPointerException("id is null.");
        }
        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        } else if (pks.size() > 1) {
            throw new UnsupportedOperationException("does not support composite primary key, please use deleteList/deleteListByMap.");
        }

        try {
            return this.delete().eq(pks.get(0).getProperty(), id).doDelete();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public int deleteByIds(List<? extends Serializable> idList) throws RuntimeSQLException {
        if (idList == null) {
            throw new NullPointerException("idList is null.");
        }
        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        } else if (pks.size() > 1) {
            throw new UnsupportedOperationException("does not support composite primary key, please use deleteList/deleteListByMap.");
        }

        try {
            AtomicInteger counter = new AtomicInteger();
            EntityDelete<Object> delete = this.delete();
            for (Object obj : idList) {
                if (obj != null) {
                    counter.incrementAndGet();
                    delete.eq(pks.get(0).getProperty(), obj).or();
                }
            }

            return counter.get() == 0 ? 0 : delete.doDelete();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public Object selectById(Serializable id) throws RuntimeSQLException {
        if (id == null) {
            throw new NullPointerException("id is null.");
        }
        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        } else if (pks.size() > 1) {
            throw new UnsupportedOperationException("does not support composite primary key, please use deleteList/deleteListByMap.");
        }

        try {
            return this.query().eq(pks.get(0).getProperty(), id).queryForObject();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public List<Object> selectByIds(List<? extends Serializable> idList) throws RuntimeSQLException {
        if (idList == null) {
            throw new NullPointerException("idList is null.");
        }
        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        } else if (pks.size() > 1) {
            throw new UnsupportedOperationException("does not support composite primary key, please use deleteList/deleteListByMap.");
        }

        try {
            AtomicInteger counter = new AtomicInteger();
            EntityQuery<Object> query = this.query();
            for (Object obj : idList) {
                if (obj != null) {
                    counter.incrementAndGet();
                    query.eq(pks.get(0).getProperty(), obj).or();
                }
            }

            return counter.get() == 0 ? Collections.emptyList() : query.queryForList();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public Object loadBy(Object refData) throws RuntimeSQLException {
        if (refData == null) {
            throw new NullPointerException("refData is null.");
        }
        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        }

        EntityQuery<Object> query = this.query();
        if (refData instanceof Map) {
            boolean missingPrimary = false;
            for (ColumnMapping pk : pks) {
                Map<?, ?> refMap = (Map<?, ?>) refData;
                if (refMap.containsKey(pk.getProperty())) {
                    query.eq(pk.getProperty(), ((Map<?, ?>) refData).get(pk.getProperty()));
                } else {
                    missingPrimary = true;
                    break;
                }
            }
            if (missingPrimary) {
                throw new UnsupportedOperationException(entityType() + " missing primary key.");
            }
        } else if (this.entityType.isInstance(refData)) {
            for (ColumnMapping pk : pks) {
                query.eq(pk.getProperty(), pk.getHandler().get(refData));
            }
        } else {
            Map<String, Property> funcMap = BeanUtils.getPropertyFunc(refData.getClass());
            boolean missingPrimary = false;
            for (ColumnMapping pk : pks) {
                if (funcMap.containsKey(pk.getProperty())) {
                    query.eq(pk.getProperty(), funcMap.get(pk.getProperty()).get(refData));
                } else {
                    missingPrimary = true;
                    break;
                }
            }
            if (missingPrimary) {
                throw new UnsupportedOperationException(entityType() + " missing primary key.");
            }
        }

        try {
            return query.queryForObject();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    @Override
    public List<Object> loadListBy(List<?> refList) throws RuntimeSQLException {
        if (refList == null) {
            throw new NullPointerException("refList is null.");
        }
        List<ColumnMapping> pks = this.foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new UnsupportedOperationException(entityType() + " missing primary key.");
        }

        AtomicInteger counter = new AtomicInteger();
        EntityQuery<Object> query = this.query();
        for (Object refData : refList) {
            if (refData == null) {
                continue;
            } else {
                counter.incrementAndGet();
            }

            query.or(c -> {
                if (refData instanceof Map) {
                    Map<?, ?> refMap = (Map<?, ?>) refData;
                    boolean missingPrimary = false;
                    for (ColumnMapping pk : pks) {
                        if (refMap.containsKey(pk.getProperty())) {
                            c.eq(pk.getProperty(), ((Map<?, ?>) refData).get(pk.getProperty()));
                        } else {
                            missingPrimary = true;
                            break;
                        }
                    }

                    if (missingPrimary) {
                        throw new UnsupportedOperationException(refMap.getClass() + " missing primary key.");
                    }
                } else if (this.entityType.isInstance(refData)) {
                    for (ColumnMapping pk : pks) {
                        c.eq(pk.getProperty(), pk.getHandler().get(refData));
                    }
                } else {
                    boolean missingPrimary = false;
                    Map<String, Property> funcMap = BeanUtils.getPropertyFunc(refData.getClass());
                    for (ColumnMapping pk : pks) {
                        if (funcMap.containsKey(pk.getProperty())) {
                            c.eq(pk.getProperty(), funcMap.get(pk.getProperty()).get(refData));
                        } else {
                            missingPrimary = true;
                            break;
                        }
                    }
                    if (missingPrimary) {
                        throw new UnsupportedOperationException(entityType() + " missing primary key.");
                    }
                }
            });
        }

        try {
            return counter.get() == 0 ? Collections.emptyList() : query.queryForList();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
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