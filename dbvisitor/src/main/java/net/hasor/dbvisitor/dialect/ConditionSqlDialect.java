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
package net.hasor.dbvisitor.dialect;
import net.hasor.cobble.StringUtils;

/**
 * 条件 SQL 方言接口，扩展 {@link SqlDialect} 以支持条件查询
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface ConditionSqlDialect extends SqlDialect {
    /**
     * 生成 LIKE 条件 SQL 片段
     * @param likeType LIKE 模式枚举值
     * @param value 匹配值
     * @return 生成的 LIKE 条件 SQL 片段
     */
    default String like(SqlLike likeType, Object value, String valueTerm) {
        valueTerm = StringUtils.isBlank(valueTerm) ? "?" : valueTerm.trim();
        switch (likeType) {
            case LEFT:
                return "CONCAT('%', " + valueTerm + " )";
            case RIGHT:
                return "CONCAT( " + valueTerm + " ,'%')";
            default:
                return "CONCAT('%', " + valueTerm + " ,'%')";
        }
    }

    /** like 查询相关的选项 */
    enum SqlLike {
        /** %值 */
        LEFT,
        /** 值% */
        RIGHT,
        /** %值% */
        DEFAULT
    }
}
