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
package net.hasor.dbvisitor.faker.generator;
/**
 * 值的用途
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public enum UseFor {
    /** 被用作 Insert */
    Insert,
    /** 被用作 update 语句中 set 部分 */
    UpdateSet,
    /** 被用作 update 语句中的 where 部分 */
    UpdateWhere,
    /** 被用作 delete 语句的 where 部分 */
    DeleteWhere,
}