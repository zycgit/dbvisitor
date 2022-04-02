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
package net.hasor.db.lambda.core;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * 动态拼条件。
 * <p>主动调用or表示紧接着下一个方法不是用and连接!(不调用or则默认为使用and连接)</p>
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public interface QueryCompare<R, P> {
    /** 等于条件 查询，类似：'or ...' */
    R or();

    /** 等于条件 查询，类似：'or ...' */
    R and();

    /** 括号方式嵌套一组查询条件，与现有条件为并且关系。类似：'and ( ...where... )' */
    default R and(Consumer<R> lambda) {
        this.and();
        return this.nested(lambda);
    }

    /** 括号方式嵌套一组查询条件，与现有条件为或关系。类似：'or ( ...where... )' */
    default R or(Consumer<R> lambda) {
        this.or();
        return this.nested(lambda);
    }

    /** 括号方式嵌套一组查询条件 */
    R nested(Consumer<R> lambda);

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
     * 拼接 sql
     * <p>!! 会有 sql 注入风险 !!</p>
     * <p>例1: apply("id = 1")</p>
     * <p>例2: apply("date_format(dateColumn,'%Y-%m-%d') = '2008-08-08'")</p>
     * <p>例3: apply("date_format(dateColumn,'%Y-%m-%d') = {0}", LocalDate.now())</p>
     */
    R apply(String sqlString, Object... args);

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

    //    interface QueryForEntity<T, R> extends QueryCompare<R> {
    //        /** 等于条件 查询，类似：'col = ?' */
    //        R eq(SFunction<T> property, Object value);
    //
    //        /** 不等于条件 查询，类似：'col <> ?' */
    //        R ne(SFunction<T> property, Object value);
    //
    //        /** 大于条件 查询，类似：'col > ?' */
    //        R gt(SFunction<T> property, Object value);
    //
    //        /** 大于等于条件 查询，类似：'col >= ?' */
    //        R ge(SFunction<T> property, Object value);
    //
    //        /** 小于条件 查询，类似：'col < ?' */
    //        R lt(SFunction<T> property, Object value);
    //
    //        /** 小于等于条件 查询，类似：'col <= ?' */
    //        R le(SFunction<T> property, Object value);
    //
    //        /** like 查询，类似：'col like CONCAT('%', ?, '%')' */
    //        R like(SFunction<T> property, Object value);
    //
    //        /** not like 查询，类似：'col not like CONCAT('%', ?, '%')' */
    //        R notLike(SFunction<T> property, Object value);
    //
    //        /** like 查询，类似：'col like CONCAT(?, '%')' */
    //        R likeRight(SFunction<T> property, Object value);
    //
    //        /** not like 查询，类似：'col not like CONCAT(?, '%')' */
    //        R notLikeRight(SFunction<T> property, Object value);
    //
    //        /** like 查询，类似：'col like CONCAT('%', ?)' */
    //        R likeLeft(SFunction<T> property, Object value);
    //
    //        /** not like 查询，类似：'col not like CONCAT('%', ?)' */
    //        R notLikeLeft(SFunction<T> property, Object value);
    //
    //        /** is null 查询，类似：'col is null' */
    //        R isNull(SFunction<T> property);
    //
    //        /** not null 查询，类似：'col is not null' */
    //        R isNotNull(SFunction<T> property);
    //
    //        /** in 查询，类似：'col in (?,?,?)' */
    //        R in(SFunction<T> property, Collection<?> value);
    //
    //        /** not in 查询，类似：'col not in (?,?,?)' */
    //        R notIn(SFunction<T> property, Collection<?> value);
    //
    //        /** between 语句，类似：'col between ? and ?' */
    //        R between(SFunction<T> property, Object value1, Object value2);
    //
    //        /** not between 语句，类似：'col not between ? and ?' */
    //        R notBetween(SFunction<T> property, Object value1, Object value2);
    //    }
}
