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