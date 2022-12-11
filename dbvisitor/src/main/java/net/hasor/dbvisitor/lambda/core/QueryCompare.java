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
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 动态拼条件。
 * <p>主动调用or表示紧接着下一个方法不是用and连接!(不调用or则默认为使用and连接)</p>
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
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
     * */
    R ifTrue(boolean test, Consumer<QueryCompare<R, T, P>> lambda);

    /**
     * 当 test 条件为 true 的时才执行 nested。相当于如下逻辑：
     * <pre>
     * if (test) {
     *      nested(...)
     * }
     * </pre>
     * */
    R ifTrueNested(boolean test, Consumer<R> lambda);

    /**
     * 当 test 条件为 true 的时才执行 and。相当于如下逻辑：
     * <pre>
     * if (test) {
     *      and(...)
     * }
     * </pre>
     * */
    R ifTrueAnd(boolean test, Consumer<R> lambda);

    /**
     * 当 test 条件为 true 的时才执行 or。相当于如下逻辑：
     * <pre>
     * if (test) {
     *      or(...)
     * }
     * </pre>
     * */
    R ifTrueOr(boolean test, Consumer<R> lambda);

    /** 括号方式嵌套一组查询条件 */
    R nested(Consumer<R> lambda);

    /** 等于条件 查询，类似：'or ...' */
    R or();

    /** 括号方式嵌套一组查询条件，与现有条件为或关系。类似：'or ( ...where... )' */
    default R or(Consumer<R> lambda) {
        this.or();
        return this.nested(lambda);
    }

    /** 等于条件 查询，类似：'or ...' */
    R and();

    /** 括号方式嵌套一组查询条件，与现有条件为并且关系。类似：'and ( ...where... )' */
    default R and(Consumer<R> lambda) {
        this.and();
        return this.nested(lambda);
    }

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
    R eq(P property, Object value);

    /** 不等于条件 查询，类似：'col <> ?' */
    R ne(P property, Object value);

    /** 大于条件 查询，类似：'col > ?' */
    R gt(P property, Object value);

    /** 大于等于条件 查询，类似：'col >= ?' */
    R ge(P property, Object value);

    /** 小于条件 查询，类似：'col < ?' */
    R lt(P property, Object value);

    /** 小于等于条件 查询，类似：'col <= ?' */
    R le(P property, Object value);

    /** like 查询，类似：'col like CONCAT('%', ?, '%')' */
    R like(P property, Object value);

    /** not like 查询，类似：'col not like CONCAT('%', ?, '%')' */
    R notLike(P property, Object value);

    /** like 查询，类似：'col like CONCAT(?, '%')' */
    R likeRight(P property, Object value);

    /** not like 查询，类似：'col not like CONCAT(?, '%')' */
    R notLikeRight(P property, Object value);

    /** like 查询，类似：'col like CONCAT('%', ?)' */
    R likeLeft(P property, Object value);

    /** not like 查询，类似：'col not like CONCAT('%', ?)' */
    R notLikeLeft(P property, Object value);

    /** is null 查询，类似：'col is null' */
    R isNull(P property);

    /** not null 查询，类似：'col is not null' */
    R isNotNull(P property);

    /** in 查询，类似：'col in (?,?,?)' */
    R in(P property, Collection<?> value);

    /** not in 查询，类似：'col not in (?,?,?)' */
    R notIn(P property, Collection<?> value);

    /** between 语句，类似：'col between ? and ?' */
    R between(P property, Object value1, Object value2);

    /** not between 语句，类似：'col not between ? and ?' */
    R notBetween(P property, Object value1, Object value2);

    /** sample 对象中不为空的属性会以 and 方式拼起来，并作为一组条件。类似：('col1 = ?' and 'col2 = ?' and col3 = ?) */
    R eqBySample(T sample);

    /** sample 对象中不为空的属性会以 and 方式拼起来，并作为一组条件。类似：('col1 = ?' and 'col2 = ?' and col3 = ?) */
    R eqBySampleMap(Map<String, Object> sample);
}
