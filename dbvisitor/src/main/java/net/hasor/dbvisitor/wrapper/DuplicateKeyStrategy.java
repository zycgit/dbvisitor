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
package net.hasor.dbvisitor.wrapper;
/**
 * 遇到重复 Key 的策略
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-10-25
 */
public enum DuplicateKeyStrategy {
    /** 使用标准 insert into */
    Into,
    /** 新值替代旧值 例如 mysql 的 replace into ,以及 upsert */
    Update,
    /** 忽略插入 例如 mysql 的 insert ignore */
    Ignore,
}
