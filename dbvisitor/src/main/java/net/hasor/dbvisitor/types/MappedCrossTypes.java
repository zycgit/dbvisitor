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
package net.hasor.dbvisitor.types;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 用于定义JDBC类型与Java类型之间的映射关系注解，该注解可重复使用
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
@Repeatable(MappedCrossTypesGroup.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface MappedCrossTypes {
    /** JDBC 类型代码 */
    int jdbcType();

    /** 对应的Java类型 */
    Class<?> javaType();
}