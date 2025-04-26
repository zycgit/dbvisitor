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
import net.hasor.cobble.StringUtils;

import java.sql.ResultSet;

/**
 * 该枚举类定义了几种不同的结果集类型，包含 FORWARD_ONLY、SCROLL_SENSITIVE、SCROLL_INSENSITIVE 和 DEFAULT。
 * DEFAULT 等价于 unset，具体类型依赖于数据库驱动的实现。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
public enum ResultSetType {
    /**
     * 结果集游标只能向前移动，不能向后滚动。
     * 这种类型的结果集在处理大量数据时性能较好，因为不需要维护游标位置的历史记录。
     */
    FORWARD_ONLY("FORWARD_ONLY", ResultSet.TYPE_FORWARD_ONLY),
    /**
     * 结果集游标可以前后滚动，并且对其他事务对数据库所做的修改敏感。
     * 即当其他事务修改了数据库中的数据时，当前结果集能反映这些变化。
     */
    SCROLL_SENSITIVE("SCROLL_SENSITIVE", ResultSet.TYPE_SCROLL_SENSITIVE),
    /**
     * 结果集游标可以前后滚动，但对其他事务对数据库所做的修改不敏感。
     * 即当其他事务修改了数据库中的数据时，当前结果集不会反映这些变化。
     */
    SCROLL_INSENSITIVE("SCROLL_INSENSITIVE", ResultSet.TYPE_SCROLL_INSENSITIVE),
    /**
     * 使用默认的结果集类型，具体类型依赖于数据库驱动的实现。
     */
    DEFAULT("DEFAULT", null),
    ;
    private final String  typeName;
    private final Integer resultSetType;

    ResultSetType(String typeName, Integer resultSetType) {
        this.typeName = typeName;
        this.resultSetType = resultSetType;
    }

    /**
     * 获取结果集类型的名称。
     * @return 结果集类型的名称
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * 获取对应的 java.sql.ResultSet 中的结果集类型常量值。
     * @return 结果集类型常量值，如果是 DEFAULT 类型则返回 null
     */
    public Integer getResultSetType() {
        return this.resultSetType;
    }

    /**
     * 根据给定的代码字符串查找对应的 ResultSetType 枚举值。
     * 如果未找到匹配的枚举值，则返回默认的 ResultSetType。
     * @param code 用于匹配的代码字符串，不区分大小写
     * @param defaultType 当未找到匹配值时返回的默认 ResultSetType
     * @return 匹配的 ResultSetType 枚举值，若未找到则返回 defaultType
     */
    public static ResultSetType valueOfCode(String code, ResultSetType defaultType) {
        for (ResultSetType tableType : ResultSetType.values()) {
            if (StringUtils.equalsIgnoreCase(tableType.typeName, code)) {
                return tableType;
            }
        }
        return defaultType;
    }
}
