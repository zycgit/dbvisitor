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
package net.hasor.dbvisitor.mapper;
import java.lang.annotation.*;

/**
 * 引用 Mapper 配置文件中的 SQL。
 * 此注解可用于类上，通过指定值来引用对应的 Mapper 配置文件里的 SQL 定义。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-19
 */
@MapperDef
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RefMapper {
    /**
     * 获取要引用的 Mapper 配置文件中的 SQL 标识。默认值为空字符串，表示未指定具体的 SQL 标识。
     * @return 要引用的 SQL 标识
     */
    String value() default "";
}