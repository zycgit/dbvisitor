/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
/**
 * 动态SQL处理包
 * <p>提供dynamicSQL机制，可以动态生成SQL语句及其相关参数信息。</p>
 * <p>主要功能包括：</p>
 * <ul>
 *   <li>支持动态SQL片段构建</li>
 *   <li>支持多种参数绑定方式（命名参数、位置参数等）</li>
 *   <li>支持条件判断和规则处理</li>
 *   <li>支持SQL注入表达式处理</li>
 * </ul>
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-25
 */
package net.hasor.dbvisitor.dynamic;
