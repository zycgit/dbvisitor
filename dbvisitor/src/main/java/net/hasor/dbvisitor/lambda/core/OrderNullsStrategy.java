/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.core;
import net.hasor.cobble.StringUtils;

/**
 * 排序中 null 值的排序策略。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-11-10
 */
public enum OrderNullsStrategy {
    /** 默认行为 */
    DEFAULT,
    /** 排序中 null 值位于前面 */
    FIRST,
    /** 排序中 null 值位于后面 */
    LAST;

    public static OrderNullsStrategy valueOfCode(String code) {
        for (OrderNullsStrategy s : OrderNullsStrategy.values()) {
            if (StringUtils.equalsIgnoreCase(s.name(), code)) {
                return s;
            }
        }
        return null;
    }
}
