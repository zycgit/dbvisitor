/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic;
/**
 * 结果参数类型枚举，用于标识存储过程/函数调用的返回结果类型
 * <p>枚举值说明：</p>
 * <ul>
 *   <li>ResultSet - 表示返回结果集</li>
 *   <li>ResultUpdate - 表示返回更新计数</li>
 *   <li>Default - 表示默认返回类型</li>
 * </ul>
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-24
 */
public enum ResultArgType {
    /** 结果集类型 */
    ResultSet,

    /** 更新计数类型 */
    ResultUpdate,

    /** 默认返回类型 */
    Default;
}
