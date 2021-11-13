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
import net.hasor.db.lambda.LambdaOperations.LambdaDelete;
import net.hasor.db.lambda.LambdaOperations.LambdaInsert;
import net.hasor.db.lambda.LambdaOperations.LambdaQuery;
import net.hasor.db.lambda.LambdaOperations.LambdaUpdate;
import net.hasor.db.lambda.QueryCompare;
import net.hasor.db.lambda.core.LambdaTemplate;
import net.hasor.db.page.Page;
import net.hasor.db.page.PageObject;
import net.hasor.db.page.PageResult;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Mapper 继承该接口后，无需编写 mapper.xml 文件，即可获得 CRUD 功能
 * @version : 2021-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public interface BaseMapper<T> extends Mapper {

    public Class<T> entityType();

    public LambdaTemplate template();

    public default LambdaInsert<T> insert() {
        return template().lambdaInsert(entityType());
    }

    public default LambdaDelete<T> delete() {
        return template().lambdaDelete(entityType());
    }

    public default LambdaUpdate<T> update() {
        return template().lambdaUpdate(entityType());
    }

    public default LambdaQuery<T> query() {
        return template().lambdaQuery(entityType());
    }

    /**
     * 插入一条记录，当遇到 duplicateKey 使用 into 策略
     * @param entity 实体对象
     */
    public default int insert(T entity) throws SQLException {
        return insert(entity, DuplicateKeyStrategy.Into);
    }

    /**
     * 插入一条记录
     * @param entity 实体对象
     * @param strategy 当遇到冲的策略
     */
    public int insert(T entity, DuplicateKeyStrategy strategy) throws SQLException;

    /**
     * 插入一组记录，当遇到 duplicateKey 使用 into 策略
     * @param entity 实体对象
     */
    public default int insertBatch(List<T> entity) throws SQLException {
        return insertBatch(entity, DuplicateKeyStrategy.Into);
    }

    /**
     * 插入一组记录，当遇到 duplicateKey 使用 into 策略
     * @param entity 实体对象
     * @param strategy 当遇到冲的策略
     */
    public int insertBatch(List<T> entity, DuplicateKeyStrategy strategy) throws SQLException;

    /**
     * 插入一条记录，当遇到 duplicateKey 使用 into 策略
     * @param columnMap map key 为列名
     */
    public default int insertByColumn(Map<String, Object> columnMap) throws SQLException {
        return insertByColumn(columnMap, DuplicateKeyStrategy.Into);
    }

    /**
     * 插入一条记录
     * @param columnMap map key 为列名
     * @param strategy 当遇到冲的策略
     */
    public int insertByColumn(Map<String, Object> columnMap, DuplicateKeyStrategy strategy) throws SQLException;

    /**
     * 插入一组记录，当遇到 duplicateKey 使用 into 策略
     * @param columnMapList 实体对象，map key 为列名
     */
    public default int insertByColumnBatch(List<Map<String, Object>> columnMapList) throws SQLException {
        return insertByColumnBatch(columnMapList, DuplicateKeyStrategy.Into);
    }

    /**
     * 插入一组记录，当遇到 duplicateKey 使用 into 策略
     * @param columnMapList 实体对象，map key 为列名
     * @param strategy 当遇到冲的策略
     */
    public int insertByColumnBatch(List<Map<String, Object>> columnMapList, DuplicateKeyStrategy strategy) throws SQLException;

    /**
     * 根据 ID 删除
     * @param id 主键ID
     */
    public int deleteById(Serializable id) throws SQLException;

    /**
     * 根据 sample 条件，作为样本 null 将不会被列入条件。
     * 如果想匹配 null 值需要使用 queryByCondition 方法
     * @param sample 实体对象
     */
    public int deleteBySample(T sample) throws SQLException;

    /**
     * 根据 columnMap 条件，删除记录。
     * @param columnMap 列 map 对象
     */
    public int deleteByColumn(Map<String, Object> columnMap) throws SQLException;

    /**
     * 根据 where 查询条件，更新记录
     * @param queryCompare 删除条件（可以为 null 效果同 updateById）
     */
    public int deleteByCondition(Consumer<QueryCompare<T, LambdaDelete<T>>> queryCompare) throws SQLException;

    /**
     * 删除（根据ID 批量删除）
     * @param idList 主键ID列表
     */
    public int deleteBatchIds(List<? extends Serializable> idList) throws SQLException;

    /**
     * 根据 ID 修改
     * @param entity 实体对象
     */
    public int updateById(T entity) throws SQLException;

    /**
     * 根据 ID 修改
     * @param sample 查询条件
     * @param newValue 实体对象
     */
    public int updateBySample(T sample, T newValue) throws SQLException;

    /**
     * 根据 ID 修改
     * @param entity 实体对象
     * @param idList IDs
     */
    public int updateByIds(T entity, List<? extends Serializable> idList) throws SQLException;

    public int updateByCondition(T entity, Consumer<QueryCompare<T, LambdaUpdate<T>>> queryCompare) throws SQLException;

    public int updateByCondition(Map<String, Object> columnMap, Consumer<QueryCompare<T, LambdaUpdate<T>>> queryCompare) throws SQLException;

    /**
     * 根据 ID 查询
     * @param id 主键ID
     */
    public T queryById(Serializable id) throws SQLException;

    /**
     * 根据 entity 条件，作为样本 null 将不会被列入条件。
     * 如果想匹配 null 值需要使用 queryByCondition 方法
     * @param entity 实体对象
     * @return T
     */
    public List<T> queryBySample(T entity) throws SQLException;

    /**
     * 查询（根据ID 批量查询）
     * @param idList 主键ID列表
     */
    public List<T> queryByIds(List<? extends Serializable> idList) throws SQLException;

    /**
     * 根据 where 条件，查询数据
     * @param queryCompare 实体对象封装操作类（可以为 null）
     */
    public List<T> queryByCondition(Consumer<QueryCompare<T, LambdaQuery<T>>> queryCompare) throws SQLException;

    /**
     * 根据 ID 查询
     * @param id 主键ID
     */
    public Map<String, Object> queryMapById(Serializable id) throws SQLException;

    /**
     * 根据 entity 条件，作为样本 null 将不会被列入条件。
     * 如果想匹配 null 值需要使用 queryByCondition 方法
     * @param entity 实体对象
     * @return T
     */
    public List<Map<String, Object>> queryMapBySample(T entity) throws SQLException;

    /**
     * 查询（根据ID 批量查询）
     * @param idList 主键ID列表
     */
    public List<Map<String, Object>> queryMapBatchIds(List<? extends Serializable> idList) throws SQLException;

    /**
     * 根据 where 条件，查询数据
     * @param queryCompare 实体对象封装操作类（可以为 null）
     */
    public List<Map<String, Object>> queryMapByCondition(Consumer<QueryCompare<T, LambdaQuery<T>>> queryCompare) throws SQLException;

    /**
     * 相当于 select count(1) form xxxx
     * @return int
     */
    public int countAll() throws SQLException;

    /**
     * 根据 query 条件，查询总记录数
     * @param sample 样本
     * @return int
     */
    public int countBySample(T sample) throws SQLException;

    /**
     * 根据 query 条件，查询总记录数
     * @param columnMap 查询条件
     * @return int
     */
    public int countByColumn(Map<String, Object> columnMap) throws SQLException;

    /**
     * 根据 query 条件，查询总记录数
     * @param queryCompare 查询条件
     * @return int
     */
    public int countByCondition(Consumer<QueryCompare<T, LambdaQuery<T>>> queryCompare) throws SQLException;

    /** 获取分页信息（无条件下） */
    public default Page pageInfo() throws SQLException {
        return pageInfo(0, 0, null);
    }

    /** 获取分页信息（有条件下） */
    public default Page pageInfo(Consumer<QueryCompare<T, LambdaQuery<T>>> queryCompare) throws SQLException {
        return pageInfo(0, 0, queryCompare);
    }

    /**
     * 获取分页信息（无条件下）
     * @param pageSize 页大小
     * @param pageNumberOffset 页码偏移量
     */
    public default Page pageInfo(int pageSize, int pageNumberOffset) throws SQLException {
        return pageInfo(pageSize, pageNumberOffset, null);
    }

    /**
     * 获取分页信息（有条件下）
     * @param pageSize 页大小
     * @param pageNumberOffset 页码偏移量
     * @param queryCompare 条件
     */
    public default Page pageInfo(int pageSize, int pageNumberOffset, Consumer<QueryCompare<T, LambdaQuery<T>>> queryCompare) throws SQLException {
        int totalCount = queryCompare == null ? countAll() : countByCondition(queryCompare);
        PageObject pageObject = new PageObject(pageSize, totalCount);
        pageObject.setPageNumberOffset(pageNumberOffset);
        return pageObject;
    }

    /** 分页查询 */
    public default PageResult<T> queryByPage(Page page) throws SQLException {
        return queryByPage(page, null);
    }

    /** 分页查询 */
    public default PageResult<T> queryByPage(Page page, Consumer<QueryCompare<T, LambdaQuery<T>>> queryCompare) throws SQLException {
        if (queryCompare == null) {
            List<T> pageData = query().usePage(page).queryForList();
            int totalCount = countAll();
            return new PageResult<>(page, totalCount, pageData);
        } else {
            List<T> pageData = query().and(queryCompare).usePage(page).queryForList();
            int totalCount = countByCondition(queryCompare);
            return new PageResult<>(page, totalCount, pageData);
        }
    }
}
