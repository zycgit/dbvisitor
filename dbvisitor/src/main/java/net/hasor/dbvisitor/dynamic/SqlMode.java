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
