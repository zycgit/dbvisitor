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
package net.hasor.dbvisitor.lambda.core;

import java.sql.SQLException;
import java.util.Map;
import java.util.function.Predicate;

/**
 * lambda Update 执行器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface UpdateExecute<R, T, P> extends BasicFunc<R>, ConditionFunc<R>, BoundSqlBuilder {
    /** 生成 select count() 查询语句并查询总数。 */
    int doUpdate() throws SQLException;

    /** 【危险操作】允许更新主键列（主键不应具有业务含义，只是唯一标识数据） */
    R allowUpdateKey();

    /** 参照 sample 局部更新（只处理不为空的属性） */
    R updateToSample(T sample);

    /** 参照 sample 局部更新（只处理不为空的属性），通过 condition 可以进一步过滤某些列是否参与更新 */
    R updateToSample(T sample, Predicate<String> condition);

    /** 参照 sample 局部更新（只处理不为空的属性） */
    R updateToSampleMap(Map<String, Object> sample);

    /** 参照 sample 局部更新（只处理不为空的属性），通过 condition 可以进一步过滤某些列是否参与更新 */
    R updateToSampleMap(Map<String, Object> sample, Predicate<String> condition);

    /**
     * 整行更新，会触发主键的更新，通常需要配合启用 allowUpdateKey 使用（需要依赖 @Column 注解标识出主键列）
     */
    R updateRow(T newValue);

    /** 增强 updateRow 方法，通过 condition 可以进一步过滤某些列是否参与更新 */
    R updateRow(T newValue, Predicate<String> condition);

    R updateRowUsingMap(Map<String, Object> newValue);

    R updateRowUsingMap(Map<String, Object> newValue, Predicate<String> condition);

    /** 添加一个 update set 字段 */
    default R updateTo(P property, Object value) {
        return this.updateTo(true, property, value);
    }

    /** 当条件为真时，激活更新条件 updateTo，通过反复调用可以添加多个 update set 字段 */
    R updateTo(boolean test, P property, Object value);

    /** 添加一个 update set 字段 */
    default R updateToUsingStr(String property, Object value) {
        return this.updateToUsingStr(true, property, value);
    }

    /** 当条件为真时，激活更新条件 updateTo，通过反复调用可以添加多个 update set 字段 */
    R updateToUsingStr(boolean test, String property, Object value);
}