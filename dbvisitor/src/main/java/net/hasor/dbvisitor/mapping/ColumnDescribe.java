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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * （可选）标记在字段或者 get/set 方法上表示列信息，用于生成 DDL 语句
 * @version : 2023-01-07
 * @author 赵永春 (zyc@hasor.net)
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnDescribe {
    /** 列备注 */
    String comment() default "";

    /** 列数据类型 */
    String dbType();

    /** 长度 */
    int length() default 0;

    /** 列上具有的默认值 */
    String defaultValue() default "";

    /** 表示列是否允许为空 */
    boolean nullable() default true;

    /** 这个列属于哪些索引（如果某个索引含有多个列，那么这些列的 belongIndex 属性都会含有这个索引的名字） */
    String[] belongIndex() default {};

    /** 这个列属于哪些唯一索引（如果某个索引含有多个列，那么这些列的 belongIndex 属性都会含有这个索引的名字） */
    String[] belongUnique() default {};
}
