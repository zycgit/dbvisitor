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
