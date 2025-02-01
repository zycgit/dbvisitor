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
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.dynamic.rule.*;

import java.util.Map;
import java.util.Objects;

/**
 * SqlBuildRule 注册器
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-05
 */
public class RuleRegistry {
    public static final RuleRegistry         DEFAULT = new RuleRegistry();
    private final       Map<String, SqlRule> ruleMap = new LinkedCaseInsensitiveMap<>();

    static {
        DEFAULT.register("if", IfRule.INSTANCE_IF);
        DEFAULT.register("ifin", new InRule(true));
        DEFAULT.register("iftext", new TextRule(true));
        DEFAULT.register("ifmacro", new MacroRule(true));
        DEFAULT.register("ifand", new AndRule(true));
        DEFAULT.register("ifor", new OrRule(true));
        DEFAULT.register("ifset", new SetRule(true));
        DEFAULT.register("in", InRule.INSTANCE);
        DEFAULT.register("text", TextRule.INSTANCE);
        DEFAULT.register("md5", MD5Rule.INSTANCE);
        DEFAULT.register("uuid32", UUID32Rule.INSTANCE);
        DEFAULT.register("uuid36", UUID36Rule.INSTANCE);
        DEFAULT.register("macro", MacroRule.INSTANCE);
        DEFAULT.register("and", AndRule.INSTANCE);
        DEFAULT.register("or", OrRule.INSTANCE);
        DEFAULT.register("set", SetRule.INSTANCE);

        DEFAULT.register("arg", ArgRule.INSTANCE);
        DEFAULT.register(ResultRule.FUNC_RESULT_SET, ResultRule.INSTANCE_OF_RESULT_SET);
        DEFAULT.register(ResultRule.FUNC_RESULT_UPDATE, ResultRule.INSTANCE_OF_RESULT_UPDATE);
        DEFAULT.register(ResultRule.FUNC_DEFAULT_RESULT, ResultRule.INSTANCE_OF_DEFAULT_RESULT);
    }

    public SqlRule findRule(String ruleName) {
        SqlRule rule = this.ruleMap.get(ruleName);
        if (rule == null && this != DEFAULT) {
            rule = DEFAULT.findRule(ruleName);
        }
        return rule;
    }

    /** 注册 SqlBuildRule */
    public void register(String ruleName, SqlRule rule) {
        this.ruleMap.put(ruleName, Objects.requireNonNull(rule, "rule is null."));
    }
}
