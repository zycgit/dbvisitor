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
package net.hasor.dbvisitor.faker;
import net.hasor.cobble.StringUtils;

/**
 * sql 语句生成策略
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public enum SqlPolitic {
    /** 随机列 */
    RandomCol,
    /** KEY 列中进行随机(update/delete 有效) */
    RandomKeyCol,
    /** 含有全部列 */
    FullCol,
    /** KEY 列中进行随机(update/delete 有效) */
    KeyCol,
    ;

    public static SqlPolitic valueOf(String name, SqlPolitic defaultValue) {
        for (SqlPolitic politic : SqlPolitic.values()) {
            if (StringUtils.equalsIgnoreCase(politic.name(), name)) {
                return politic;
            }
        }

        return defaultValue;
    }
}