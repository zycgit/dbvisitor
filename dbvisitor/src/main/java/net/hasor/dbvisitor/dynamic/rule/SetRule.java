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
 * SET 条件规则实现类，用于生成 UPDATE 语句中的 SET 子句，如果参数不为空，则生成 'column = ?' 或者 ', column = ?' 。
 * 功能特点：
 * 1. 继承自ConditionRule，实现SET条件逻辑
 * 2. 自动处理UPDATE语句中的SET子句
 * 3. 支持两种模式：普通SET模式和IF-SET模式
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public class SetRule extends ConditionRule {
    public static final SqlRule INSTANCE = new SetRule(false);

    /**
     * 构造函数
     * @param usingIf 是否使用IF模式
     */
    public SetRule(boolean usingIf) {
        super(usingIf, new String[] { "set", "," }, "set", "set ", ", ");
    }

    /** 获取规则名称 ifset 或 set */
    @Override
    protected String name() {
        return this.usingIf ? "ifset" : "set";
    }

    /** 是否允许空值，总是返回true，SET 规则允许空值 */
    @Override
    protected boolean allowNullValue() {
        return true;
    }

    /** 是否允许多值，总是返回false，SET规则不允许多值 */
    @Override
    protected boolean allowMultipleValue() {
        return false;
    }
}
