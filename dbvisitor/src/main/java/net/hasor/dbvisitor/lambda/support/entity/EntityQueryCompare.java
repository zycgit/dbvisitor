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
import java.util.Collection;

/**
 * 动态拼条件。
 * <p>主动调用or表示紧接着下一个方法不是用and连接!(不调用or则默认为使用and连接)</p>
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
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

    /**
     * 等值条件，类似：'col = ?'，当 value 只为 null 时会自动使用 col is null.
     */
    default R eq(String property, Object value) {
        return this.eq(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用等值条件，类似：'if test then col = ?'，当 value 只为 null 时会自动使用 col is null.
     */
    R eq(boolean test, String property, Object value);

    /**
     * 不等于条件，类似：'col <> ?'，当 value 只为 null 时会自动使用 col is not null.
     */
    default R ne(String property, Object value) {
        return this.ne(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用不等于条件，类似：'if test then col <> ?'，当 value 只为 null 时会自动使用 col is not null.
     */
    R ne(boolean test, String property, Object value);

    /**
     * 大于条件，类似：'col > ?'
     */
    default R gt(String property, Object value) {
        return this.gt(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用大于条件，类似：'if test then col > ?'
     */
    R gt(boolean test, String property, Object value);

    /**
     * 大于等于条件，类似：'col >= ?'
     */
    default R ge(String property, Object value) {
        return this.ge(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用大于等于条件，类似：'if test then col >= ?'
     */
    R ge(boolean test, String property, Object value);

    /**
     * 小于条件，类似：'col < ?'
     */
    default R lt(String property, Object value) {
        return this.lt(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用小于条件，类似：'if test then col < ?'
     */
    R lt(boolean test, String property, Object value);

    /**
     * 小于等于条件，类似：'col <= ?'
     */
    default R le(String property, Object value) {
        return this.le(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用小于等于条件，类似：'if test then col <= ?'
     */
    R le(boolean test, String property, Object value);

    /**
     * like 条件，类似：'col like CONCAT('%', ?, '%')'
     */
    default R like(String property, Object value) {
        return this.like(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 like 条件，类似：'if test then col like CONCAT('%', ?, '%')'
     */
    R like(boolean test, String property, Object value);

    /**
     * not like 条件，类似：'col not like CONCAT('%', ?, '%')'
     */
    default R notLike(String property, Object value) {
        return this.notLike(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 not like 条件，类似：'if test then col not like CONCAT('%', ?, '%')'
     */
    R notLike(boolean test, String property, Object value);

    /**
     * like 条件，类似：'col like CONCAT(?, '%')'
     */
    default R likeRight(String property, Object value) {
        return this.likeRight(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 like 条件，类似：'if test then col like CONCAT(?, '%')'
     */
    R likeRight(boolean test, String property, Object value);

    /**
     * not like 条件，类似：'col not like CONCAT(?, '%')'
     */
    default R notLikeRight(String property, Object value) {
        return this.notLikeRight(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 not like 查询，类似：'if test then col not like CONCAT(?, '%')'
     */
    R notLikeRight(boolean test, String property, Object value);

    /**
     * like 条件，类似：'col like CONCAT('%', ?)'
     */
    default R likeLeft(String property, Object value) {
        return this.likeLeft(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 like 条件，类似：'if test then col like CONCAT('%', ?)'
     */
    R likeLeft(boolean test, String property, Object value);

    /**
     * not like 条件，类似：'col not like CONCAT('%', ?)'
     */
    default R notLikeLeft(String property, Object value) {
        return this.notLikeLeft(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 not like 条件，类似：'if test then col not like CONCAT('%', ?)'
     */
    R notLikeLeft(boolean test, String property, Object value);

    /**
     * is null 条件，类似：'col is null'
     */
    default R isNull(String property) {
        return this.isNull(true, property);
    }

    /**
     * 当 test 条件为真时才使用 is null 条件，类似：'if test then col is null'
     */
    R isNull(boolean test, String property);

    /**
     * not null 条件，类似：'col is not null'
     */
    default R isNotNull(String property) {
        return this.isNotNull(true, property);
    }

    /**
     * 当 test 条件为真时才使用 not null 条件，类似：'if test then col is not null'
     */
    R isNotNull(boolean test, String property);

    /**
     * in 条件，类似：'col in (?,?,?)'
     */
    default R in(String property, Collection<?> value) {
        return this.in(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 in 条件，类似：'if test then col in (?,?,?)'
     */
    R in(boolean test, String property, Collection<?> value);

    /**
     * not in 条件，类似：'col not in (?,?,?)'
     */
    default R notIn(String property, Collection<?> value) {
        return this.notIn(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 not in 条件，类似：'if test then col not in (?,?,?)'
     */
    R notIn(boolean test, String property, Collection<?> value);

    /**
     * between 条件，类似：'col between ? and ?'
     */
    default R between(String property, Object value1, Object value2) {
        return this.between(true, property, value1, value2);
    }

    /**
     * 当 test 条件为真时才使用 between 条件，类似：'if test then col between ? and ?'
     */
    R between(boolean test, String property, Object value1, Object value2);

    /**
     * not between 条件，类似：'col not between ? and ?'
     */
    default R notBetween(String property, Object value1, Object value2) {
        return this.notBetween(true, property, value1, value2);
    }

    /**
     * 当 test 条件为真时才使用 not between 条件，类似：'if test then col not between ? and ?'
     */
    R notBetween(boolean test, String property, Object value1, Object value2);
}