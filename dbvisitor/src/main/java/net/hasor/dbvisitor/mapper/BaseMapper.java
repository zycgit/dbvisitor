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
package net.hasor.dbvisitor.mapper;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageResult;
import net.hasor.dbvisitor.error.RuntimeSQLException;
import net.hasor.dbvisitor.jdbc.JdbcOperations;
import net.hasor.dbvisitor.lambda.*;
import net.hasor.dbvisitor.lambda.Insert;
import net.hasor.dbvisitor.session.Session;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 基础Mapper接口，提供通用的CRUD操作方法。
 * 继承该接口的Mapper可以获得基本的数据库操作能力。
 *
 * @param <T> 实体类型，对应数据库表结构
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-10-31
 */
public interface BaseMapper<T> extends Mapper {

    /**
     * 获取当前Mapper管理的实体类型
     * @return 实体类Class对象
     */
    Class<T> entityType();

    /**
     * 获取Lambda表达式操作模板
     * @return LambdaTemplate实例
     */
    LambdaTemplate lambda();

    /**
     * 获取底层JDBC操作接口
     * @return JdbcOperations实例
     */
    JdbcOperations jdbc();

    /**
     * 获取数据库会话
     * @return Session实例
     */
    Session session();

    /**
     * 创建Lambda风格的插入操作构造器
     * @return Insert操作构造器
     */
    default Insert<T> insert() {
        return lambda().insert(entityType());
    }

    /**
     * 创建Lambda风格的更新操作构造器
     * @return Update操作构造器
     */
    default EntityUpdate<T> update() {
        return lambda().update(entityType());
    }

    /**
     * 创建Lambda风格的删除操作构造器
     * @return Delete操作构造器
     */
    default EntityDelete<T> delete() {
        return lambda().delete(entityType());
    }

    /**
     * 创建Lambda风格的查询操作构造器
     * @return Query操作构造器
     */
    default EntityQuery<T> query() {
        return lambda().query(entityType());
    }

    /**
     * 执行Mapper配置文件中定义的SQL语句
     * @param stId SQL语句ID
     * @param parameter 参数对象
     * @return 执行结果
     * @throws RuntimeSQLException 数据库操作异常
     */
    default Object executeStatement(String stId, Object parameter) throws RuntimeSQLException {
        try {
            return this.session().executeStatement(stId, parameter);
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    /**
     * 执行Mapper配置文件中定义的SQL查询
     * @param stId SQL语句ID
     * @param parameter 参数对象
     * @param <E> 返回结果类型
     * @return 查询结果列表
     * @throws RuntimeSQLException 数据库操作异常
     */
    default <E> List<E> queryStatement(String stId, Object parameter) throws RuntimeSQLException {
        try {
            return this.session().queryStatement(stId, parameter, null);
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    /**
     * 执行Mapper配置文件中定义的SQL查询（带分页）
     * @param stId SQL语句ID
     * @param parameter 参数对象
     * @param page 分页参数
     * @param <E> 返回结果类型
     * @return 查询结果列表
     * @throws RuntimeSQLException 数据库操作异常
     */
    default <E> List<E> queryStatement(String stId, Object parameter, Page page) throws RuntimeSQLException {
        try {
            return this.session().queryStatement(stId, parameter, page);
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
     * 修改，使用时需注意数据被替换的问题
     * @param entity 实体对象
     */
    int update(T entity) throws RuntimeSQLException;

    /**
     * 保存或修改
     * @param entity 实体对象
     */
    int upsert(T entity) throws RuntimeSQLException;

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
     * 根据 Map 删除
     */
    int deleteByMap(Map<String, Object> map) throws RuntimeSQLException;

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
     * 根据ID查询单条记录
     * @param id 主键值
     * @return 实体对象，未找到返回null
     * @throws RuntimeSQLException 数据库操作异常
     */
    T selectById(Serializable id) throws RuntimeSQLException;

    /**
     * 批量ID查询
     * @param idList 主键值列表
     * @return 实体对象列表，未找到返回空列表
     * @throws RuntimeSQLException 数据库操作异常
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

    /**
     * 分页查询
     * @param sample 查询条件样本对象
     * @param page 分页参数对象
     * @return 包含分页信息的查询结果
     * @throws RuntimeSQLException 数据库操作异常
     */
    PageResult<T> pageBySample(Object sample, Page page) throws RuntimeSQLException;

    /**
     * 初始化分页查询对象
     * @param sample 查询条件样本对象
     * @param pageSize 每页记录数
     * @return 初始化后的分页对象
     * @throws RuntimeSQLException 数据库操作异常
     */
    default Page initPageBySample(Object sample, int pageSize) throws RuntimeSQLException {
        return this.initPageBySample(sample, pageSize, 0);
    }

    /**
     * 初始化分页查询对象
     * @param sample 查询条件样本对象
     * @param pageSize 每页记录数
     * @param pageNumberOffset 页码偏移量
     * @return 分页对象
     * @throws RuntimeSQLException 数据库操作异常
     */
    Page initPageBySample(Object sample, int pageSize, int pageNumberOffset) throws RuntimeSQLException;
}