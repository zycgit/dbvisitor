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
package net.hasor.dbvisitor.lambda.support.entity;
import java.util.Collection;

/**
 * 动态拼条件。
 * <p>主动调用or表示紧接着下一个方法不是用and连接!(不调用or则默认为使用and连接)</p>
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public interface EntityQueryCompare<R> {

    //    /** in 子查询，类似：'col in (LambdaQuery)' */
    //     <V> R andInLambda(SFunction<T> property, CompareBuilder<V> subLambda);
    //    /** in 子查询，类似：'or col in (LambdaQuery)' */
    //     <V> R orInLambda(SFunction<T> property, CompareBuilder<V> subLambda);
    //    /** not in 子查询，类似：'col not in (LambdaQuery)' */
    //     <V> R andNotInLambda(SFunction<T> property, CompareBuilder<V> subLambda);
    //    /** not in 子查询，类似：'or col not in (LambdaQuery)' */
    //     <V> R orNotInLambda(SFunction<T> property, CompareBuilder<V> subLambda);
    //    /** in SQL 子查询，类似：'col in (subQuery)' */
    //     R andInSql(SFunction<T> property, String subQuery, Object... subArgs);
    //    /** in SQL 子查询，类似：'or col in (subQuery)' */
    //     R orInSql(SFunction<T> property, String subQuery, Object... subArgs);
    //    /** not in SQL 子查询，类似：'col not in (subQuery)' */
    //     R andNotInSql(SFunction<T> property, String subQuery, Object... subArgs);
    //    /** not in SQL 子查询，类似：'or col not in (subQuery)' */
    //     R orNotInSql(SFunction<T> property, String subQuery, Object... subArgs);

    /** 等于条件 查询，类似：'col = ?' */
    R eq(String property, Object value);

    /** 不等于条件 查询，类似：'col <> ?' */
    R ne(String property, Object value);

    /** 大于条件 查询，类似：'col > ?' */
    R gt(String property, Object value);

    /** 大于等于条件 查询，类似：'col >= ?' */
    R ge(String property, Object value);

    /** 小于条件 查询，类似：'col < ?' */
    R lt(String property, Object value);

    /** 小于等于条件 查询，类似：'col <= ?' */
    R le(String property, Object value);

    /** like 查询，类似：'col like CONCAT('%', ?, '%')' */
    R like(String property, Object value);

    /** not like 查询，类似：'col not like CONCAT('%', ?, '%')' */
    R notLike(String property, Object value);

    /** like 查询，类似：'col like CONCAT(?, '%')' */
    R likeRight(String property, Object value);

    /** not like 查询，类似：'col not like CONCAT(?, '%')' */
    R notLikeRight(String property, Object value);

    /** like 查询，类似：'col like CONCAT('%', ?)' */
    R likeLeft(String property, Object value);

    /** not like 查询，类似：'col not like CONCAT('%', ?)' */
    R notLikeLeft(String property, Object value);

    /** is null 查询，类似：'col is null' */
    R isNull(String property);

    /** not null 查询，类似：'col is not null' */
    R isNotNull(String property);

    /** in 查询，类似：'col in (?,?,?)' */
    R in(String property, Collection<?> value);

    /** not in 查询，类似：'col not in (?,?,?)' */
    R notIn(String property, Collection<?> value);

    /** between 语句，类似：'col between ? and ?' */
    R between(String property, Object value1, Object value2);

    /** not between 语句，类似：'col not between ? and ?' */
    R notBetween(String property, Object value1, Object value2);

}
