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
package net.hasor.dbvisitor.wrapper.core;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 动态拼条件。
 * <p>主动调用or表示紧接着下一个方法不是用and连接!(不调用or则默认为使用and连接)</p>
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
public interface QueryCompare<R, T, P> {

    /**
     * 拼接 sql
     * <p>!! 会有 sql 注入风险 !!</p>
     * <p>例1: apply("id = 1")</p>
     * <p>例2: apply("date_format(dateColumn,'%Y-%m-%d') = '2008-08-08'")</p>
     * <p>例3: apply("date_format(dateColumn,'%Y-%m-%d') = {0}", LocalDate.now())</p>
     */
    R apply(String sqlString, Object... args);

    /**
     * ifTrue(Consumer) 当 test 条件为 true 的时才执行。相当于如下逻辑：
     * <pre>
     * if (test) {
     *      ...
     * }
     * </pre>
     */
    R ifTrue(boolean test, Consumer<QueryCompare<R, T, P>> lambda);

    /** 括号方式嵌套一组查询条件 */
    R nested(Consumer<R> lambda);

    /**
     * 当 test 条件为 true 的时才执行 nested。相当于如下逻辑：
     * <pre>
     * if (test) {
     *      nested(...)
     * }
     * </pre>
     */
    R nested(boolean test, Consumer<R> lambda);

    /**
     * 下一个查询条件使用或关系，类似：'or ...'
     */
    R or();

    /**
     * 下一个查询条件组使用或关系。类似：'or (...)'
     */
    default R or(Consumer<R> lambda) {
        this.or();
        return this.nested(lambda);
    }

    /**
     * 当 test 条件为真时才使用下一个查询条件组，条件组使用或关系。类似：'or (...)'
     */
    R or(boolean test, Consumer<R> lambda);

    /**
     * 下一个查询条件使用与关系，类似：'and ...'
     */
    R and();

    /**
     * 下一个查询条件组使用与关系。类似：'and (...)'
     */
    default R and(Consumer<R> lambda) {
        this.and();
        return this.nested(lambda);
    }

    /**
     * 当 test 条件为真时才使用下一个查询条件组，条件组使用与关系。类似：'and (...)'
     */
    R and(boolean test, Consumer<R> lambda);

    /**
     * 下一个查询条件使用与关系，类似：'not ...'
     */
    R not();

    /**
     * 下一个查询条件组使用与关系。类似：'not (...)'
     */
    default R not(Consumer<R> lambda) {
        this.not();
        return this.nested(lambda);
    }

    /**
     * 当 test 条件为真时才使用下一个查询条件组，条件组使用与关系。类似：'not (...)'
     */
    R not(boolean test, Consumer<R> lambda);

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
     * 等值条件，类似：'col = ?'
     */
    default R eq(P property, Object value) {
        return this.eq(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用等值条件，类似：'if test then col = ?'
     */
    R eq(boolean test, P property, Object value);

    /**
     * 不等于条件，类似：'col <> ?'
     */
    default R ne(P property, Object value) {
        return this.ne(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用不等于条件，类似：'if test then col <> ?'
     */
    R ne(boolean test, P property, Object value);

    /**
     * 大于条件，类似：'col > ?'
     */
    default R gt(P property, Object value) {
        return this.gt(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用大于条件，类似：'if test then col > ?'
     */
    R gt(boolean test, P property, Object value);

    /**
     * 大于等于条件，类似：'col >= ?'
     */
    default R ge(P property, Object value) {
        return this.ge(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用大于等于条件，类似：'if test then col >= ?'
     */
    R ge(boolean test, P property, Object value);

    /**
     * 小于条件，类似：'col < ?'
     */
    default R lt(P property, Object value) {
        return this.lt(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用小于条件，类似：'if test then col < ?'
     */
    R lt(boolean test, P property, Object value);

    /**
     * 小于等于条件，类似：'col <= ?'
     */
    default R le(P property, Object value) {
        return this.le(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用小于等于条件，类似：'if test then col <= ?'
     */
    R le(boolean test, P property, Object value);

    /**
     * like 条件，类似：'col like CONCAT('%', ?, '%')'
     */
    default R like(P property, Object value) {
        return this.like(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 like 条件，类似：'if test then col like CONCAT('%', ?, '%')'
     */
    R like(boolean test, P property, Object value);

    /**
     * not like 条件，类似：'col not like CONCAT('%', ?, '%')'
     */
    default R notLike(P property, Object value) {
        return this.notLike(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 not like 条件，类似：'if test then col not like CONCAT('%', ?, '%')'
     */
    R notLike(boolean test, P property, Object value);

    /**
     * like 条件，类似：'col like CONCAT(?, '%')'
     */
    default R likeRight(P property, Object value) {
        return this.likeRight(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 like 条件，类似：'if test then col like CONCAT(?, '%')'
     */
    R likeRight(boolean test, P property, Object value);

    /**
     * not like 条件，类似：'col not like CONCAT(?, '%')'
     */
    default R notLikeRight(P property, Object value) {
        return this.notLikeRight(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 not like 查询，类似：'if test then col not like CONCAT(?, '%')'
     */
    R notLikeRight(boolean test, P property, Object value);

    /**
     * like 条件，类似：'col like CONCAT('%', ?)'
     */
    default R likeLeft(P property, Object value) {
        return this.likeLeft(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 like 条件，类似：'if test then col like CONCAT('%', ?)'
     */
    R likeLeft(boolean test, P property, Object value);

    /**
     * not like 条件，类似：'col not like CONCAT('%', ?)'
     */
    default R notLikeLeft(P property, Object value) {
        return this.notLikeLeft(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 not like 条件，类似：'if test then col not like CONCAT('%', ?)'
     */
    R notLikeLeft(boolean test, P property, Object value);

    /**
     * is null 条件，类似：'col is null'
     */
    default R isNull(P property) {
        return this.isNull(true, property);
    }

    /**
     * 当 test 条件为真时才使用 is null 条件，类似：'if test then col is null'
     */
    R isNull(boolean test, P property);

    /**
     * not null 条件，类似：'col is not null'
     */
    default R isNotNull(P property) {
        return this.isNotNull(true, property);
    }

    /**
     * 当 test 条件为真时才使用 not null 条件，类似：'if test then col is not null'
     */
    R isNotNull(boolean test, P property);

    /**
     * in 条件，类似：'col in (?,?,?)'
     */
    default R in(P property, Collection<?> value) {
        return this.in(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 in 条件，类似：'if test then col in (?,?,?)'
     */
    R in(boolean test, P property, Collection<?> value);

    /**
     * not in 条件，类似：'col not in (?,?,?)'
     */
    default R notIn(P property, Collection<?> value) {
        return this.notIn(true, property, value);
    }

    /**
     * 当 test 条件为真时才使用 not in 条件，类似：'if test then col not in (?,?,?)'
     */
    R notIn(boolean test, P property, Collection<?> value);

    /**
     * between 条件，类似：'col between ? and ?'
     */
    default R rangeBetween(P property, Object value1, Object value2) {
        return this.rangeBetween(true, property, value1, value2);
    }

    /**
     * 当 test 条件为真时才使用 between 条件，类似：'if test then col between ? and ?'
     */
    R rangeBetween(boolean test, P property, Object value1, Object value2);

    /**
     * not between 条件，类似：'col not between ? and ?'
     */
    default R rangeNotBetween(P property, Object value1, Object value2) {
        return this.rangeNotBetween(true, property, value1, value2);
    }

    /**
     * 当 test 条件为真时才使用 not between 条件，类似：'if test then col not between ? and ?'
     */
    R rangeNotBetween(boolean test, P property, Object value1, Object value2);

    /**
     * 类似：'xx < col < xx'
     */
    default R rangeOpenOpen(P property, Object value1, Object value2) {
        return this.rangeOpenOpen(true, property, value1, value2);
    }

    /**
     * 当 test 条件为真时才使用 rangeOpenOpen 条件，类似：'if test then xx < col < xx'
     */
    R rangeOpenOpen(boolean test, P property, Object value1, Object value2);

    /**
     * 类似：'not (xx < col < xx)'
     */
    default R rangeNotOpenOpen(P property, Object value1, Object value2) {
        return this.rangeNotOpenOpen(true, property, value1, value2);
    }

    /**
     * 当 test 条件为真时才使用 rangeNotOpenOpen 条件，类似：'if test then not (xx < col < xx)'
     */
    R rangeNotOpenOpen(boolean test, P property, Object value1, Object value2);

    /**
     * 类似：'xx < col <= xx'
     */
    default R rangeOpenClosed(P property, Object value1, Object value2) {
        return this.rangeOpenClosed(true, property, value1, value2);
    }

    /**
     * 当 test 条件为真时才使用 rangeOpenClosed 条件，类似：'if test then xx < col <= xx'
     */
    R rangeOpenClosed(boolean test, P property, Object value1, Object value2);

    /**
     * 类似：'not (xx < col <= xx)'
     */
    default R rangeNotOpenClosed(P property, Object value1, Object value2) {
        return this.rangeNotOpenClosed(true, property, value1, value2);
    }

    /**
     * 当 test 条件为真时才使用 rangeNotOpenClosed 条件，类似：'if test then not (xx < col <= xx)'
     */
    R rangeNotOpenClosed(boolean test, P property, Object value1, Object value2);

    /**
     * 类似：'xx <= col < xx'
     */
    default R rangeClosedOpen(P property, Object value1, Object value2) {
        return this.rangeClosedOpen(true, property, value1, value2);
    }

    /**
     * 当 test 条件为真时才使用 rangeClosedOpen 条件，类似：'if test then xx <= col < xx'
     */
    R rangeClosedOpen(boolean test, P property, Object value1, Object value2);

    /**
     * 类似：'not (xx <= col < xx)'
     */
    default R rangeNotClosedOpen(P property, Object value1, Object value2) {
        return this.rangeNotClosedOpen(true, property, value1, value2);
    }

    /**
     * 当 test 条件为真时才使用 rangeNotClosedOpen 条件，类似：'if test then not (xx <= col < xx)'
     */
    R rangeNotClosedOpen(boolean test, P property, Object value1, Object value2);

    /**
     * 类似：'xx <= col <= xx'
     */
    default R rangeClosedClosed(P property, Object value1, Object value2) {
        return this.rangeClosedClosed(true, property, value1, value2);
    }

    /**
     * 当 test 条件为真时才使用 rangeClosedClosed 条件，类似：'if test then xx <= col <= xx'
     */
    R rangeClosedClosed(boolean test, P property, Object value1, Object value2);

    /**
     * 类似：'not (xx <= col <= xx)'
     */
    default R rangeNotClosedClosed(P property, Object value1, Object value2) {
        return this.rangeNotClosedClosed(true, property, value1, value2);
    }

    /**
     * 当 test 条件为真时才使用 rangeNotClosedClosed 条件，类似：'if test then not (xx <= col <= xx)'
     */
    R rangeNotClosedClosed(boolean test, P property, Object value1, Object value2);

    /** sample 对象中不为空的属性会以 and 方式拼起来，并作为一组条件。类似：('col1 = ?' and 'col2 = ?' and col3 = ?) */
    R eqBySample(T sample);

    /** sample 对象中不为空的属性会以 and 方式拼起来，并作为一组条件。类似：('col1 = ?' and 'col2 = ?' and col3 = ?) */
    R eqBySampleMap(Map<String, Object> sample);
}
