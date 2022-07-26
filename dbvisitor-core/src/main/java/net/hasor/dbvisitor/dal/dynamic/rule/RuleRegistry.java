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
package net.hasor.dbvisitor.dal.dynamic.rule;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;

import java.util.Map;

/**
 * SqlBuildRule 注册器
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class RuleRegistry {
    public static final RuleRegistry              DEFAULT = new RuleRegistry();
    private final       Map<String, SqlBuildRule> ruleMap = new LinkedCaseInsensitiveMap<>();

    static {
        DEFAULT.register("and", AndRule.INSTANCE);
        DEFAULT.register("or", OrRule.INSTANCE);

        DEFAULT.register("ognl", OgnlRule.INSTANCE);
        DEFAULT.register("md5", MD5Rule.INSTANCE);
        DEFAULT.register("uuid32", UUID32Rule.INSTANCE);
        DEFAULT.register("uuid36", UUID36Rule.INSTANCE);

        DEFAULT.register("include", IncludeRule.INSTANCE);
        DEFAULT.register("text", TextRule.INSTANCE);
        DEFAULT.register("arg", ArgRule.INSTANCE);
    }

    public SqlBuildRule findByName(String ruleName) {
        SqlBuildRule rule = this.ruleMap.get(ruleName);
        if (rule == null && this != DEFAULT) {
            rule = DEFAULT.findByName(ruleName);
        }
        return rule;
    }

    /** 注册 SqlBuildRule */
    public void register(String ruleName, SqlBuildRule sqlBuildRule) {
        this.ruleMap.put(ruleName, sqlBuildRule);
    }
}
