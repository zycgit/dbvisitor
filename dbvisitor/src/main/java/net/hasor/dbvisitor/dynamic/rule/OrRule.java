/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic.rule;
/**
 * OR条件规则实现类，用于生成OR条件的SQL片段。如果参数不为空，则生成 'or column = ?' 或者 'column = ?' 。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public class OrRule extends ConditionRule {
    public static final SqlRule INSTANCE = new OrRule(false);

    /**
     * 构造函数
     * @param usingIf 是否使用IF模式
     */
    public OrRule(boolean usingIf) {
        super(usingIf, DEFAULT_TEST_PREFIX, "where", "where ", "or ");
    }

    /** 获取规则名称，规则名称（ifor或or） */
    @Override
    protected String name() {
        return this.usingIf ? "ifor" : "or";
    }

    /**
     * 是否允许空值，IF 模式允许空值，普通模式不允许
     */
    @Override
    protected boolean allowNullValue() {
        return this.usingIf;
    }
}
