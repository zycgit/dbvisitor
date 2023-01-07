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
    private final TableMapping<Object> tableMapping;

    public BaseMapperHandler(String space, Class<?> entityType, DalSession dalSession) {
        this.space = space;
        this.entityType = (Class<Object>) entityType;
        this.dalSession = dalSession;
        this.tableMapping = dalSession.getDalRegistry().findMapping(space, this.entityType);

        Objects.requireNonNull(this.tableMapping, "entityType '" + entityType + "' undefined.");
    }

    @Override
    public Class<Object> entityType() {
        return this.entityType;
    }

    @Override
    public LambdaTemplate template() {
        return this.dalSession.newTemplate(this.space);
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
    public int saveOrUpdate(Object entity) throws SQLException {
        if (entity == null) {
            throw new NullPointerException("entity is null.");
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new SQLException(entityType() + " no primary key is identified");
        }

        EntityQueryOperation<Object> query = query();
        EntityUpdateOperation<Object> update = update();

        for (ColumnMapping pk : pks) {
            Object o = pk.getHandler().get(entity);
            if (o == null) {
                query.and().isNull(pk.getColumn());
                update.and().isNull(pk.getColumn());
            } else {
                query.and().eq(pk.getColumn(), o);
                update.and().eq(pk.getColumn(), o);
            }
        }

        if (query.queryForCount() == 0) {
            return insert().applyEntity(entity).executeSumResult();
        } else {
            return update.updateTo(entity).doUpdate();
        }
    }

    @Override
    public int delete(Object entity) throws SQLException {
        if (entity == null) {
            throw new NullPointerException("entity is null.");
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new SQLException(entityType() + " no primary key is identified");
        }

        EntityDeleteOperation<Object> delete = delete();

        for (ColumnMapping pk : pks) {
            Object o = pk.getHandler().get(entity);
            if (o == null) {
                delete.and().isNull(pk.getColumn());
            } else {
                delete.and().eq(pk.getColumn(), o);
            }
        }
        return delete.doDelete();
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

        EntityDeleteOperation<Object> delete = delete();
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
    public int deleteByIds(List<? extends Serializable> idList) throws SQLException {
        if (idList == null || idList.isEmpty()) {
            return 0;
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new SQLException(entityType() + " no primary key is identified");
        }

        if (pks.size() == 1) {
            EntityDeleteOperation<Object> delete = delete();
            return delete.and().in(pks.get(0).getColumn(), idList).doDelete();
        } else {
            EntityDeleteOperation<Object> delete = delete();
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
    public Object getById(Serializable id) throws SQLException {
        if (id == null) {
            return null;
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new SQLException(entityType() + " no primary key is identified");
        }

        EntityQueryOperation<Object> query = query();
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

        return query.queryForObject();
    }

    @Override
    public List<Object> getByIds(List<? extends Serializable> idList) throws SQLException {
        if (idList == null || idList.isEmpty()) {
            return Collections.emptyList();
        }

        List<ColumnMapping> pks = foundPrimaryKey();
        if (pks.isEmpty()) {
            throw new SQLException(entityType() + " no primary key is identified");
        }

        if (pks.size() == 1) {
            return query().and().in(pks.get(0).getColumn(), idList).queryForList();
        } else {
            EntityQueryOperation<Object> query = query();
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
            return query.queryForList();
        }
    }

    protected EntityQueryOperation<Object> buildQueryBySample(Object sample) {
        EntityQueryOperation<Object> query = query();

        if (sample != null) {
            for (ColumnMapping mapping : getMapping().getProperties()) {
                Object value = mapping.getHandler().get(sample);
                if (value != null) {
                    query.and().eq(mapping.getColumn(), value);
                }
            }
        }

        return query;
    }

    @Override
    public List<Object> listBySample(Object sample) throws SQLException {
        return buildQueryBySample(sample).queryForList();
    }

    @Override
    public int countBySample(Object sample) throws SQLException {
        return buildQueryBySample(sample).queryForCount();
    }

    @Override
    public int countAll() throws SQLException {
        return query().queryForCount();
    }

    @Override
    public PageResult<Object> pageBySample(Object sample, Page page) throws SQLException {
        List<Object> result = buildQueryBySample(sample).usePage(page).queryForList();
        return new PageResult<>(page, page.getTotalCount(), result);
    }

    @Override
    public Page initPageBySample(Object sample, int pageSize, int pageNumberOffset) throws SQLException {
        int totalCount = countBySample(sample);
        PageObject pageObject = new PageObject(pageSize, totalCount);
        pageObject.setPageNumberOffset(pageNumberOffset);
        return pageObject;
    }

}
