/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;

/**
 * 主键生成处理器接口，用于自定义数据库主键生成策略
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-01
 */
public interface GeneratedKeyHandler {
    /**
     * 是否需要在插入操作前执行主键生成
     * @return 默认返回 false（不执行前置生成）
     */
    default boolean onBefore() {
        return false;
    }

    /**
     * 前置主键生成逻辑（在INSERT语句执行前调用）
     * @param conn 数据库连接
     * @param entity 实体对象
     * @param mapping 列映射信息
     * @return 生成的主键值
     */
    default Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) throws SQLException {
        return null;
    }

    /**
     * 是否需要在插入操作后执行主键获取
     * @return 默认返回false（不执行后置获取）
     */
    default boolean onAfter() {
        return false;
    }

    /**
     * 是否需要使用 {@link java.sql.Statement#RETURN_GENERATED_KEYS} 来获取数据库生成的主键。
     * <p>当返回 true 时，框架在创建 PreparedStatement 时会使用
     * {@code conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)}，
     * 使得插入后可以通过 {@code PreparedStatement.getGeneratedKeys()} 获取数据库自动生成的键值。</p>
     * @return 默认返回 false（不请求数据库返回生成的主键）
     */
    default boolean useGeneratedKeys() {
        return false;
    }

    /**
     * 后置主键获取逻辑（在INSERT语句执行后调用）
     * @param generatedKeys 数据库返回的生成键结果集
     * @param entity 实体对象
     * @param argsIndex 参数索引位置
     * @param mapping 列映射信息
     * @return 获取到的主键值
     */
    default Object afterApply(ResultSet generatedKeys, Object entity, int argsIndex, ColumnMapping mapping) throws SQLException {
        return null;
    }
}
