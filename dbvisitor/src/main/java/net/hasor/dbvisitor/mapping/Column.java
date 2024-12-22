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
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.handler.UnknownTypeHandler;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Types;

/**
 * （可选）标记在字段或者 get/set 方法上表示映射到的列
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /** 列名，为空的话表示采用字段名为列名 see: {@link #name()} */
    String value() default "";

    /** 列名，为空的话表示采用类名为表名 see: {@link #value()} */
    String name() default "";

    /** 指定使用的 jdbcType */
    int jdbcType() default Types.JAVA_OBJECT;

    /** 如果当前属性是一个抽象类型，那么可以通过 specialJavaType 来指定具体的实现类 */
    Class<?> specialJavaType() default Object.class;

    /** 指定使用的 typeHandler（功效和 Mybatis 的 TypeHandler 相同） */
    Class<? extends TypeHandler<?>> typeHandler() default UnknownTypeHandler.class;

    /** (选填) key 生成策略，当列的属性为 null 的时。采用一种生成算法来生成 key 值。通常做用于 自增。 */
    KeyType keyType() default KeyType.None;

    /** (选填) 是否为主键 */
    boolean primary() default false;

    /** (选填) 参与更新（在配置了 @Table 注解时，通过 {@link WrapperAdapter} 接口操作才有效） */
    boolean update() default true;

    /** (选填) 参与新增（在配置了 @Table 注解时，通过 {@link WrapperAdapter} 接口操作才有效） */
    boolean insert() default true;

    /** (选填) 用作 select 语句时 column name 的写法，默认是空 */
    String selectTemplate() default "";

    /** (选填) 用作 insert 语句时 value 的参数写法，默认是 ? */
    String insertTemplate() default "";

    /** (选填) 用作 update 的 set 语句时 column name 的写法，默认是空 */
    String setColTemplate() default "";

    /** (选填) 用作 update set 语句时 value 的参数写法，默认是 ? */
    String setValueTemplate() default "";

    /** (选填) 用作 update/delete 的 where 语句时 column name 的写法，默认是空 */
    String whereColTemplate() default "";

    /** (选填) 用作 update/delete 的 where 语句时 value 的参数写法，默认是 ? */
    String whereValueTemplate() default "";

    /** (选填) 用作 group by 时 column name 的写法，默认是空 */
    String groupByColTemplate() default "";

    /** (选填) 用作 order by 时 column name 的写法，默认是空 */
    String orderByColTemplate() default "";
}
