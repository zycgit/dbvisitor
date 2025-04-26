/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.mapper;
import net.hasor.cobble.StringUtils;

/**
 * 使用 Statement 方式的枚举类。该类定义了几种不同的 SQL 语句执行方式，
 * 对应 Java 标准库中不同的 Statement 接口实现。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
public enum StatementType {
    /**
     * 使用 java.sql.Statement 来执行 SQL 语句。
     * 这种方式适用于静态 SQL 语句，即 SQL 语句在执行前已经完全确定，不包含参数。
     * "STATEMENT" 是该类型对应的名称。
     */
    Statement("STATEMENT"),
    /**
     * 使用 java.sql.PreparedStatement 来执行 SQL 语句。
     * 这种方式适用于包含参数的 SQL 语句，它可以预编译 SQL 语句，提高执行效率，
     * 同时能有效防止 SQL 注入攻击。"PREPARED" 是该类型对应的名称。
     */
    Prepared("PREPARED"),
    /**
     * 使用 java.sql.CallableStatement 来执行 SQL 语句。
     * 这种方式主要用于调用数据库中的存储过程和函数。
     * "CALLABLE" 是该类型对应的名称。
     */
    Callable("CALLABLE"),
    ;

    private final String typeName;

    StatementType(String typeName) {
        this.typeName = typeName;
    }

    /**
     * 获取该枚举常量对应的类型名称。
     * @return 类型名称。
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * 根据给定的代码字符串查找对应的 StatementType 枚举值。
     * 如果未找到匹配的枚举值，则返回默认的 StatementType。
     * @param code 用于匹配的代码字符串，不区分大小写。
     * @param defaultType 当未找到匹配值时返回的默认 StatementType。
     * @return 匹配的 StatementType 枚举值，若未找到则返回 defaultType。
     */
    public static StatementType valueOfCode(String code, StatementType defaultType) {
        for (StatementType tableType : StatementType.values()) {
            if (StringUtils.equalsIgnoreCase(tableType.typeName, code)) {
                return tableType;
            }
        }
        return defaultType;
    }
}
