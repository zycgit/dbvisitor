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
package net.hasor.dbvisitor.lambda.core;
/**
 * 排序策略。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-11-02
 */
public enum OrderType {
    /** 默认排序方式，行为由数据库决定。 */
    DEFAULT,
    /** 升序排序(ASC) */
    ASC,
    /** 降序排序(DESC) */
    DESC
}
