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
package net.hasor.dbvisitor.dialect.builder;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect.SqlLike;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;

/**
 * 命令构建器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-06
 */
public interface CommandBuilder {

    /** 设置操作的表 */
    void setTable(String catalog, String schema, String table);

    /** 添加查询条件 */
    void addCondition(ConditionLogic logic, String col, String colTerm, ConditionType type, Object value, String valueTerm, SqlLike forLikeType);

    /** 添加查询条件 */
    void addConditionForBetween(ConditionLogic logic, String col, String colTerm, ConditionType type, Object value1, String value1Term, Object value2, String value2Term);

    /** 添加查询条件 */
    void addConditionForIn(ConditionLogic logic, String col, String colTerm, ConditionType type, Object[] values, String valueTerm);

    /** 添加嵌套条件组 */
    void addConditionGroup(ConditionLogic logic, Consumer<CommandBuilder> group);

    /** 添加查询列 */
    void addSelect(String col, String colTerm);

    /** 添加查询列 */
    void addSelect(String col, String custom, Object[] args);

    /** 添加查询列 */
    boolean hasSelect(String col);

    /** 添加查询列 */
    boolean hasSelectAll();

    //

    /** 添加分组 */
    void addGroupBy(String col) throws SQLException;

    /** 添加排序 */
    void addOrderBy(String col, String colTerm, OrderType type, OrderNullsStrategy nullsStrategy);

    //

    /** 添加更新列和值 */
    void addUpdateSet(String col, Object value, String valueTerm);

    /** 添加插入列和值 */
    void addInsert(String col, Object value, String valueTerm);

    //

    /** 构建 Select */
    BoundSql buildSelect(SqlDialect dialect, boolean delimited) throws SQLException;

    /** 构建 Update */
    BoundSql buildUpdate(SqlDialect dialect, boolean delimited, boolean allowEmptyWhere) throws SQLException;

    /** 构建 Delete */
    BoundSql buildDelete(SqlDialect dialect, boolean delimited, boolean allowEmptyWhere) throws SQLException;

    /** 构建 Insert */
    BoundSql buildInsert(SqlDialect dialect, boolean delimited, List<String> primaryKey, DuplicateKeyStrategy strategy) throws SQLException;
}
