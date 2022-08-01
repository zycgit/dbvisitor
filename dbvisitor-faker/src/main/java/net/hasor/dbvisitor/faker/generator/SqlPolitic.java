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
import net.hasor.cobble.StringUtils;

/**
 * SQL 语句生成策略
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public enum SqlPolitic {
    /** 全部随机 */
    RandomCol,
    /** 随机 KEY 列，(update/delete 有效) */
    RandomKeyCol,
    /** 全部列 */
    FullCol,
    /** 仅 KEY 列，(update/delete 有效) */
    KeyCol,
    ;

    public static SqlPolitic valueOfCode(String name, SqlPolitic defaultValue) {
        for (SqlPolitic politic : SqlPolitic.values()) {
            if (StringUtils.equalsIgnoreCase(politic.name(), name)) {
                return politic;
            }
        }
        return defaultValue;
    }
}