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
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public interface UpdateExecute<R, T, P> extends BoundSqlBuilder {
    /** 生成 select count() 查询语句并查询总数。*/
    int doUpdate() throws SQLException;

    /** 【危险操作】允许空 Where条件（注意：空 Where 条件会导致更新整个数据库） */
    R allowEmptyWhere();

    /** 【危险操作】允许更新主键列（主键不应具有业务含义，只是唯一标识数据） */
    R allowUpdateKey();

    /** 【危险操作】允许更新整行（容易引发数据误覆盖） */
    R allowReplaceRow();

    /** 参照 sample 局部更新（只更新对象中属性不为空的）*/
    R updateToSample(T sample);

    /** 增强 updateToSample 方法，通过 condition 可以进一步过滤某些列是否参与更新 */
    R updateToSampleCondition(T sample, Predicate<String> condition);

    /** 参照 sample 局部更新（只更新 map 中在的列） */
    R updateToMap(Map<String, Object> sample);

    /** 增强 updateToMap 方法，通过 condition 可以进一步过滤某些列是否参与更新 */
    R updateToMapCondition(Map<String, Object> sample, Predicate<String> condition);

    /** 清空已经设置的所有 set 条件 */
    R resetUpdate();

    /**
     * 整行更新
     * - 注意1：主键会被自动忽略不参与更新，如果想变更主键需要启用 allowUpdateKey（需要依赖 @Column 注解标识出主键列）
     * - 注意2：整行更新是危险操作，需要启用 allowReplaceRow
     */
    R updateTo(T newValue);

    /** 增强 updateTo 方法，通过 condition 可以进一步过滤某些列是否参与更新 */
    R updateToCondition(T newValue, Predicate<String> condition);

    /** 添加一个 update set 字段 */
    default R updateTo(P property, Object value) {
        return this.updateTo(true, property, value);
    }

    /** 当条件为真时，激活更新条件 updateTo，通过反复调用可以添加多个 update set 字段 */
    R updateTo(boolean test, P property, Object value);
}