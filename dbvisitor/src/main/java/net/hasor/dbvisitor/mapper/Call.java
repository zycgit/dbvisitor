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
 * 用于标注存储过程调用方法的注解。
 * 该注解通常用在Mapper接口的方法上，用于执行数据库存储过程调用。
 *
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Call {
    /**
     * 存储过程调用语句，支持多条语句
     * @return 存储过程调用SQL语句数组
     */
    String[] value();

    /**
     * 执行超时时间（秒）
     * @return 超时时间，默认-1表示不设置超时
     */
    int timeout() default -1;

    /**
     * 输出参数绑定
     * @return 输出参数名称数组
     */
    String[] bindOut() default {};
}