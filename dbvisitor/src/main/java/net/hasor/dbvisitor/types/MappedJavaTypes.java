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
 * 用于指定类型处理器 {@link TypeHandler} 可以处理的 Java 类型
 * 该注解通常标注在自定义类型处理器上，声明该处理器能够处理的Java类型。
 * @author 赵永春 (zyc@hasor.net)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MappedJavaTypes {
    /** 该类型处理器支持的Java类型数组 */
    Class<?>[] value();
}