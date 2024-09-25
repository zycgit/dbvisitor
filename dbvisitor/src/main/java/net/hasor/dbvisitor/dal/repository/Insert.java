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
package net.hasor.dbvisitor.dal.repository;

import java.lang.annotation.*;

/**
 * insert 语句
 * @version : 2021-05-19
 * @author 赵永春 (zyc@hasor.net)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Insert {
    String[] value();

    boolean xml() default false;

    StatementType statementType() default StatementType.Prepared;

    int timeout() default -1;

    /** 是否使用自增属性。
     * - 如果同时配置了 SelectKey 标签，那么该配置将会失效。 */
    boolean useGeneratedKeys() default false;

    /** 回填自增属性值的 Bean 属性名。
     * - 如果同时配置了 SelectKey 标签，那么该配置将会失效。*/
    String keyProperty() default "";

    /** 参数类型，可以用于回填自增属性。
     * - 如果同时配置了 SelectKey 标签 或者 useGeneratedKeys 属性，那么该配置将会失效。*/
    Class<?> parameterType() default Object.class;
}
