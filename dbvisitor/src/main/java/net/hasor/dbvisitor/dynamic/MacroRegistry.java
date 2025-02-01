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
package net.hasor.dbvisitor.dynamic;
import net.hasor.cobble.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 解析动态 SQL 配置
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-05
 */
public class MacroRegistry {
    public static final MacroRegistry           DEFAULT  = new MacroRegistry();
    private final       Map<String, DynamicSql> macroMap = new HashMap<>();

    public DynamicSql findMacro(String dynamicId) {
        return this.macroMap.get(dynamicId);
    }

    public void register(String macroName, String sqlSegment) {
        if (StringUtils.isNotBlank(macroName)) {
            this.macroMap.put(macroName, DynamicParsed.getParsedSql(sqlSegment));
        }
    }

    public void register(String macroName, DynamicSql sqlSegment) {
        if (StringUtils.isNotBlank(macroName)) {
            this.macroMap.put(macroName, sqlSegment);
        }
    }
}