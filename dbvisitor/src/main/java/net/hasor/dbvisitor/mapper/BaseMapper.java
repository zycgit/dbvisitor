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
package net.hasor.dbvisitor.mapper;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageResult;
import net.hasor.dbvisitor.error.RuntimeSQLException;
import net.hasor.dbvisitor.session.dal.session.DalSession;
import net.hasor.dbvisitor.wrapper.*;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Mapper 继承该接口后可以获得 CRUD 功能
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-10-31
 */
public interface BaseMapper<T> extends Mapper {

    Class<T> entityType();

    WrapperAdapter template();

    DalSession getSession();

    /** return LambdaInsert for insert */
    default InsertWrapper<T> insert() {
        return template().insertByEntity(entityType());
    }

    /** return LambdaUpdate for update */
    default EntityUpdateWrapper<T> update() {
        return template().updateByEntity(entityType());
    }

    /** return LambdaDelete for delete */
    default EntityDeleteWrapper<T> delete() {
        return template().deleteByEntity(entityType());
    }

    /** return LambdaQuery for query */
    default EntityQueryWrapper<T> query() {
        return template().queryByEntity(entityType());
    }

    /** 执行 Mapper 配置文件中的 SQL */
    default int executeStatement(String stId, Object parameter) throws RuntimeSQLException {
        try {
            return this.getSession().executeStatement(stId, parameter);
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    /** 执行 Mapper 配置文件中的 SQL */
    default <E> List<E> queryStatement(String stId, Object parameter) throws RuntimeSQLException {
        return this.queryStatement(stId, parameter, null);
    }

    /** 执行 Mapper 配置文件中的 SQL */
    default <E> List<E> queryStatement(String stId, Object parameter, Page page) throws RuntimeSQLException {
        try {
            return this.getSession().queryStatement(stId, parameter, page);
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    /**
     * 插入一条记录
     * @param entity 实体对象
     */
    default int insert(T entity) throws RuntimeSQLException {
        try {
            return insert().applyEntity(entity).executeSumResult();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    /**
     * 插入一组记录
     * @param entity 实体对象列表
     */
    default int insert(List<T> entity) throws RuntimeSQLException {
        try {
            return insert().applyEntity(entity).executeSumResult();
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    /**
     * 修改
     * @param entity 实体对象
     */
    int updateById(T entity) throws RuntimeSQLException;

    /**
     * 保存或修改
     * @param entity 实体对象
     */
    int upsertById(T entity) throws RuntimeSQLException;

    /**
     * 局部修改
     * @param map 局部更新对象
     */
    int updateByMap(Map<String, Object> map) throws RuntimeSQLException;

    /**
     * 删除
     * @param entity 实体对象
     */
    int delete(T entity) throws RuntimeSQLException;

    /**
     * 根据 ID 删除
     * @param id 主键ID
     */
    int deleteById(Serializable id) throws RuntimeSQLException;

    /**
     * 根据 ID 删除
     * @param idList 主键ID
     */
    int deleteByIds(List<? extends Serializable> idList) throws RuntimeSQLException;

    /**
     * 根据 ID 查询
     * @param id 主键ID
     */
    T selectById(Serializable id) throws RuntimeSQLException;

    /**
     * 查询（根据ID 批量查询）
     * @param idList 主键ID列表
     */
    List<T> selectByIds(List<? extends Serializable> idList) throws RuntimeSQLException;

    /**
     * 根据 entity 条件，作为样本 null 将不会被列入条件。
     * 如果想匹配 null 值需要使用 queryByCondition 方法
     * @param entity 实体对象
     * @return T
     */
    List<T> listBySample(T entity) throws RuntimeSQLException;

    /**
     * 根据 entity 条件，作为样本 null 将不会被列入条件。
     * 如果想匹配 null 值需要使用 queryByCondition 方法
     * @param entity 实体对象
     * @return T
     */
    int countBySample(T entity) throws RuntimeSQLException;

    /**
     * 相当于 select count(1) form xxxx
     * @return int
     */
    int countAll() throws RuntimeSQLException;

    /** 分页查询 */
    PageResult<T> pageBySample(Object sample, Page page) throws RuntimeSQLException;

    /** 初始化分页对象 */
    default Page initPageBySample(Object sample, int pageSize) throws RuntimeSQLException {
        return this.initPageBySample(sample, pageSize, 0);
    }

    /** 初始化分页对象 */
    Page initPageBySample(Object sample, int pageSize, int pageNumberOffset) throws RuntimeSQLException;
}
