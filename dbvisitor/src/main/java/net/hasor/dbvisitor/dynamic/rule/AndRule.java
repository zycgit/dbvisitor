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
package net.hasor.dbvisitor.dynamic.rule;
/**
 * AND条件规则实现类，如果参数不为空，则生成 'and column = ?' 或者 'column = ?' 。
 * 功能特点：
 * 1. 继承自ConditionRule，实现AND条件逻辑
 * 2. 根据参数是否为空生成不同的SQL片段
 * 3. 支持两种模式：普通AND模式和IF-AND模式
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public class AndRule extends ConditionRule {
    public static final SqlRule INSTANCE = new AndRule(false);

    /**
     * 构造函数
     * @param usingIf 是否使用IF模式
     */
    public AndRule(boolean usingIf) {
        super(usingIf, DEFAULT_TEST_PREFIX, "where", "where ", "and ");
    }

    /**
     * 获取规则名称（ifand 或 and）
     */
    @Override
    protected String name() {
        return this.usingIf ? "ifand" : "and";
    }

    /** 是否允许空值,IF 模式允许空值，普通模式不允许 */
    @Override
    protected boolean allowNullValue() {
        return this.usingIf;
    }
}
