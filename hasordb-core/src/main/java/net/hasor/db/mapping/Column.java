/*
 * Copyright 2002-2010 the original author or authors.
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
package net.hasor.db.mapping;
import net.hasor.db.types.TypeHandler;
import net.hasor.db.types.UnknownTypeHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Types;

/**
 * （可选）标记在字段或者 get/set 方法上表示映射到的列
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
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

    /** (选填)是否为主键 */
    boolean primary() default false;

    /** (选填)参与更新（在配置了 @Table 注解时，通过 net.hasor.db.lambda.LambdaOperations 接口操作才有效） */
    boolean update() default true;

    /** (选填)参与新增（在配置了 @Table 注解时，通过 net.hasor.db.lambda.LambdaOperations 接口操作才有效） */
    boolean insert() default true;
}
