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
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageResult;
import net.hasor.dbvisitor.error.RuntimeSQLException;
import net.hasor.dbvisitor.jdbc.JdbcOperations;
import net.hasor.dbvisitor.lambda.*;
import net.hasor.dbvisitor.lambda.Insert;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.session.Session;

/**
 * 基础 Mapper 接口，提供通用的 CRUD 操作方法。
 * 继承该接口的 Mapper 可以获得基本的数据库操作能力。
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
     * 执行 Mapper 配置文件中定义的SQL查询（带分页）
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
     * （替换更新）根据实体对象的主键值更新数据库记录。方法会自动提取实体中的主键字段作为更新条件，非主键字段作为更新内容。
     * 注意：无主键表的更新请使用 {@link #update()} 条件构造器。
     * @param entity 实体对象（支持 Map，效果等同于 replaceByMap）
     */
    int replace(T entity) throws RuntimeSQLException;

    /**
     * （替换更新）允许使用 Map 作为实体数据对象。方法会自动提取实体中的主键字段作为更新条件，非主键字段作为更新内容。
     * 注意：Map 中不存在的列将会按照 null 来处理，无主键表的更新请使用 {@link #update()} 条件构造器。
     * @param entity 更新对象
     */
    int replaceByMap(Map<String, Object> entity) throws RuntimeSQLException;

    /**
     * （局部更新）根据实体对象的主键值更新数据库记录。方法会自动提取实体中的主键字段作为更新条件，非主键的非空字段作为更新内容。
     * 注意：无主键表的更新请使用 {@link #update()} 条件构造器。
     * @param sample 样本
     */
    int update(T sample) throws RuntimeSQLException;

    /**
     * （局部更新）允许使用 Map 作为实体数据对象。方法会自动提取实体中的主键字段作为更新条件，非主键字段作为更新内容。
     * 注意：Map 中不存在的列不参与更新，无主键表的更新请使用 {@link #update()} 条件构造器。
     * @param sample 更新对象
     */
    int updateByMap(Map<String, Object> sample) throws RuntimeSQLException;

    /**
     * （插入或替换更新）在 {@link #replace(Object)} 基础之上，当更新对象不存在时会执行 {@link #insert(Object)}
     * @param entity 实体对象
     */
    int upsert(T entity) throws RuntimeSQLException;

    /**
     * （插入或替换更新）在 {@link #replaceByMap(Map)} 基础之上，当更新对象不存在时会执行 {@link #insert(Object)}
     * @param entity 实体对象
     */
    int upsertByMap(Map<String, Object> entity) throws RuntimeSQLException;

    /**
     * 根据实体对象的主键值删除数据库记录。
     * @param entity 实体对象
     */
    int delete(T entity) throws RuntimeSQLException;

    /**
     * 允许使用 Map 作为实体数据对象。方法会自动提取实体中的主键字段作为删除条件。
     */
    int deleteByMap(Map<String, Object> entity) throws RuntimeSQLException;

    /**
     * 根据实体对象的主键值批量化删除数据库记录。
     * @param entityList 实体对象列表
     */
    int deleteList(List<T> entityList) throws RuntimeSQLException;

    /**
     * 允许使用 Map 作为实体数据对象，批量化删除数据库记录。方法会自动提取实体中的主键字段作为删除条件。
     * @param entityList 实体对象列表
     */
    int deleteListByMap(List<Map<String, Object>> entityList) throws RuntimeSQLException;

    /**
     * 根据 ID 删除。联合主键表的删除需要使用 {@link #delete(Object)} 或 {@link #deleteByMap(Map)} 或 {@link #delete()}
     * @param id 主键ID
     */
    int deleteById(Serializable id) throws RuntimeSQLException;

    /**
     * 根据 ID 批量删除。联合主键表的批量删除需要使用 {@link #deleteList(List)} 或 {@link #deleteListByMap(List)} 或 {@link #delete()}
     * @param idList 主键IDs
     */
    int deleteByIds(List<? extends Serializable> idList) throws RuntimeSQLException;

    /**
     * 根据 ID 查询单条记录。联合主键表的查询需要使用 {@link #loadBy(Object)} 或 {@link #query()}
     * @param id 主键值
     */
    T selectById(Serializable id) throws RuntimeSQLException;

    /**
     * 根据 ID 批量查询。联合主键表的查询需要使用 {@link #loadListBy(List)} 或 {@link #query()}
     * @param idList 主键值列表
     */
    List<T> selectByIds(List<? extends Serializable> idList) throws RuntimeSQLException;

    /**
     * 根据参考对象加载数据库记录，参考对象必须包含主键字段。
     * 与 {@link #selectById(Serializable)} 方法的区别在于，该方法会自动提取参考对象中的主键字段作为查询条件。参考对象可以是 Map、实体对象或其它类型。
     * @param refData 参考对象
     */
    T loadBy(Object refData) throws RuntimeSQLException;

    /**
     * 根据参考对象加载数据库记录，参考对象必须包含主键字段。
     * 与 {@link #selectByIds(List)} 方法的区别在于，该方法会自动提取参考对象中的主键字段作为查询条件。参考对象可以是 Map、实体对象或其它类型。
     * @param refDataList 参考对象
     */
    List<T> loadListBy(List<?> refDataList) throws RuntimeSQLException;

    /**
     * 根据 entity 条件，作为样本 null 将不会被列入条件。
     * 如果想匹配 null 值需要使用 queryByCondition 方法
     * @param entity 实体对象
     * @return T
     */
    List<T> listBySample(T entity) throws RuntimeSQLException;

    /**
     * 根据参考对象查询符合条件的记录数量，参考对象可以是 Map、实体对象或其它类型。
     * 注意：如果想匹配 null 值需要使用 queryByCondition 方法
     * @param sample 参考对象可以是 Map、实体对象或其它类型。
     */
    int countBySample(Object sample) throws RuntimeSQLException;

    /**
     * 相当于 select count(1) form xxxx
     * @return int
     */
    int countAll() throws RuntimeSQLException;

    /**
     * 使用参考对象作为查询条件，进行分页查询。参考对象可以是 Map、实体对象或其它类型。
     * 注意：如果想匹配 null 值需要使用 queryByCondition 方法
     * @param sample 参考对象可以是 Map、实体对象或其它类型。
     * @param page 分页参数对象
     */
    default PageResult<T> pageBySample(Object sample, Page page) throws RuntimeSQLException {
        return this.pageBySample(sample, page, Collections.emptyMap(), Collections.emptyMap());
    }

    /**
     * 使用参考对象作为查询条件，进行分页查询。参考对象可以是 Map、实体对象或其它类型。
     * 注意：如果想匹配 null 值需要使用 queryByCondition 方法
     * @param sample 参考对象可以是 Map、实体对象或其它类型。
     * @param page 分页参数对象
     * @param orderBy 排序字段和排序方式。
     */
    default PageResult<T> pageBySample(Object sample, Page page, Map<String, OrderType> orderBy) throws RuntimeSQLException {
        return this.pageBySample(sample, page, orderBy, Collections.emptyMap());
    }

    /**
     * 使用参考对象作为查询条件，进行分页查询。参考对象可以是 Map、实体对象或其它类型。
     * 注意：如果想匹配 null 值需要使用 queryByCondition 方法
     * @param sample 参考对象可以是 Map、实体对象或其它类型。
     * @param page 分页参数对象
     * @param orderBy 排序字段和排序方式。
     * @param nulls 排序字段的 NULL 值排序方式。
     */
    PageResult<T> pageBySample(Object sample, Page page, Map<String, OrderType> orderBy, Map<String, OrderNullsStrategy> nulls) throws RuntimeSQLException;

    /**
     * 初始化分页查询对象
     * @param sample 查询条件样本对象
     * @param pageSize 每页记录数
     * @return 初始化后的分页对象
     * @throws RuntimeSQLException 数据库操作异常
     */
    default Page pageInitBySample(Object sample, long pageNumber, long pageSize) throws RuntimeSQLException {
        return this.pageInitBySample(sample, pageNumber, pageSize, 0);
    }

    /**
     * 初始化分页查询对象
     * @param sample 查询条件样本对象
     * @param pageSize 每页记录数
     * @param pageNumberOffset 页码偏移量
     * @return 分页对象
     * @throws RuntimeSQLException 数据库操作异常
     */
    Page pageInitBySample(Object sample, long pageNumber, long pageSize, int pageNumberOffset) throws RuntimeSQLException;
}