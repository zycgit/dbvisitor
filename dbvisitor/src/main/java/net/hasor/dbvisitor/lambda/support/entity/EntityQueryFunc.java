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
package net.hasor.dbvisitor.lambda.support.entity;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;

/**
 * 动态拼条件。
 * <p>主动调用or表示紧接着下一个方法不是用and连接!(不调用or则默认为使用and连接)</p>
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public interface EntityQueryFunc<R> {

    /**
     * 查询一个字段
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    default R select(String property) {
        return this.select(property, (String[]) null);
    }

    /**
     * 查询部分字段
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    default R select(String[] properties) {
        return this.select(null, properties);
    }

    /**
     * 查询部分字段
     * <p>在分组查询下，返回所有分组列而不是所有列</p>
     * <p>selectAll、select、applySelect 三个当同时调用时只有最后一个生效</p>
     */
    R select(String first, String... other);

    /**
     * 追加查询一个字段，不同于 {@link #select(String)} 的是不会清空已有选择条件。
     */
    default R selectAdd(String property) {
        return this.selectAdd(property, (String[]) null);
    }

    /**
     * 追加查询部分字段，不同于 {@link #select(String[])} 的是不会清空已有选择条件。
     */
    default R selectAdd(String[] properties) {
        return this.selectAdd(null, properties);
    }

    /**
     * 追加查询部分字段，不同于 {@link #select(String[])} 的是不会清空已有选择条件。
     */
    R selectAdd(String first, String... other);

    /** 分组条件，类似：group by xxx */
    default R groupBy(String property) {
        return this.groupBy(property, (String[]) null);
    }

    /** 分组条件，类似：group by xxx */
    default R groupBy(String[] properties) {
        return this.groupBy(null, properties);
    }

    /** 分组条件，类似：group by xxx */
    R groupBy(String first, String... other);

    /** 排序条件，类似：order by xxx */
    default R orderBy(String property) {
        return this.orderBy(OrderType.DEFAULT, OrderNullsStrategy.DEFAULT, property, (String[]) null);
    }

    /** 排序条件，类似：order by xxx */
    default R orderBy(String[] properties) {
        return this.orderBy(OrderType.DEFAULT, OrderNullsStrategy.DEFAULT, null, properties);
    }

    /** 分组条件，类似：group by xxx */
    default R orderBy(String first, String... other) {
        return this.orderBy(OrderType.DEFAULT, OrderNullsStrategy.DEFAULT, first, other);
    }

    /** 排序条件，类似：order by xxx */
    default R orderBy(OrderType orderType, OrderNullsStrategy strategy, String property) {
        return this.orderBy(orderType, strategy, property, (String[]) null);
    }

    /** 排序条件，类似：order by xxx */
    default R orderBy(OrderType orderType, OrderNullsStrategy strategy, String[] properties) {
        return this.orderBy(orderType, strategy, null, properties);
    }

    /** 排序条件，类似：order by xxx */
    R orderBy(OrderType orderType, OrderNullsStrategy strategy, String first, String... other);

    /** 排序(升序)，类似：order by xxx asc */
    default R asc(String property) {
        return this.orderBy(OrderType.ASC, OrderNullsStrategy.DEFAULT, property, (String[]) null);
    }

    /** 排序(升序)，类似：order by xxx asc */
    default R asc(String[] properties) {
        return this.orderBy(OrderType.ASC, OrderNullsStrategy.DEFAULT, null, properties);
    }

    /** 排序(升序)，类似：order by xxx asc */
    default R asc(String first, String... other) {
        return this.orderBy(OrderType.ASC, OrderNullsStrategy.DEFAULT, first, other);
    }

    /** 排序(升序)，类似：order by xxx asc */
    default R asc(OrderNullsStrategy strategy, String property) {
        return this.orderBy(OrderType.ASC, strategy, property, (String[]) null);
    }

    /** 排序(升序)，类似：order by xxx asc */
    default R asc(OrderNullsStrategy strategy, String[] properties) {
        return this.orderBy(OrderType.ASC, strategy, null, properties);
    }

    /** 排序(升序)，类似：order by xxx asc */
    default R asc(OrderNullsStrategy strategy, String first, String... other) {
        return this.orderBy(OrderType.ASC, strategy, first, other);
    }

    /** 排序(降序)，类似：order by xxx desc */
    default R desc(String property) {
        return this.orderBy(OrderType.DESC, OrderNullsStrategy.DEFAULT, property, (String[]) null);
    }

    /** 排序(降序)，类似：order by xxx desc */
    default R desc(String[] properties) {
        return this.orderBy(OrderType.DESC, OrderNullsStrategy.DEFAULT, null, properties);
    }

    /** 排序(降序)，类似：order by xxx desc */
    default R desc(OrderNullsStrategy strategy, String property) {
        return this.orderBy(OrderType.DESC, strategy, property, (String[]) null);
    }

    /** 排序(降序)，类似：order by xxx desc */
    default R desc(OrderNullsStrategy strategy, String[] properties) {
        return this.orderBy(OrderType.DESC, strategy, null, properties);
    }

    /** 排序(降序)，类似：order by xxx desc */
    default R desc(OrderNullsStrategy strategy, String first, String... other) {
        return this.orderBy(OrderType.DESC, strategy, first, other);
    }
}