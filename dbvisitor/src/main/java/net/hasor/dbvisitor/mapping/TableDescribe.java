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
 * （可选）标记在类型上表示列表的信息，用于生成 DDL 语句
 * @version : 2023-01-07
 * @author 赵永春 (zyc@hasor.net)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableDescribe {
    /** 表备注 */
    String comment() default "";

    /** 在生成建表语句的时候用于拼接的其它信息，开发者可以随意指定。会在 'create table' / 'alter table' 语句生成时自动追加在最后 */
    String other() default "";
}
