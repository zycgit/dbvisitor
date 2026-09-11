/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic;
/**
 * SQL参数模式枚举，用于标识存储过程/函数参数的方向
 * <p>枚举值说明：</p>
 * <ul>
 *   <li>In - 输入参数</li>
 *   <li>Out - 输出参数</li>
 *   <li>Cursor - 游标类型输出参数</li>
 *   <li>InOut - 输入输出参数</li>
 * </ul>
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-24
 */
public enum SqlMode {
    /** 输入参数 */
    In(true, false),

    /** 输出参数 */
    Out(false, true),

    /** 游标类型输出参数 */
    Cursor(false, true),

    /** 输入输出参数 */
    InOut(true, true);

    private final boolean out;
    private final boolean in;

    SqlMode(boolean in, boolean out) {
        this.in = in;
        this.out = out;
    }

    /** 是否为输入参数 */
    public boolean isIn() {
        return this.in;
    }

    /** 是否为输出参数 */
    public boolean isOut() {
        return this.out;
    }
}
