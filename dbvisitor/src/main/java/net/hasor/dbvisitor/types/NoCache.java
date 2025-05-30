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
import java.lang.annotation.*;

/**
 * 用于标记类型处理器(TypeHandler)不启用缓存的注解，
 *
 * 被该注解标注的类型处理器在执行时将不会使用缓存机制，
 * 每次都会创建新的处理器实例。适用于有状态的类型处理器。
 *
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NoCache {
}