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
package net.hasor.dbvisitor.dynamic.segment;
/**
 * SQL修饰符工具类，提供对SQL语句中不同参数类型的标识和检查功能
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-28
 */
public class SqlModifier {
    /**
     * the position arg.
     */
    public static final int POSITION  = 1;
    /**
     * the named arg.
     */
    public static final int NAMED     = 2;
    /**
     * the sql injection.
     */
    public static final int INJECTION = 4;
    /**
     * include rule
     */
    public static final int RULE      = 8;

    /**
     * 检查是否包含位置参数
     * @param modifier 修饰符值
     * @return 如果包含位置参数返回 true，否则返回 false
     */
    public static boolean hasPosition(int modifier) {
        return (POSITION & modifier) != 0;
    }

    /**
     * 检查是否包含命名参数
     * @param modifier 修饰符值
     * @return 如果包含命名参数返回 true，否则返回 false
     */
    public static boolean hasNamed(int modifier) {
        return (NAMED & modifier) != 0;
    }

    /**
     * 检查是否包含SQL注入参数
     * @param modifier 修饰符值
     * @return 如果包含SQL注入参数返回 true，否则返回 false
     */
    public static boolean hasInjection(int modifier) {
        return (INJECTION & modifier) != 0;
    }

    /**
     * 检查是否包含规则参数
     * @param modifier 修饰符值
     * @return 如果包含规则参数返回 true，否则返回 false
     */
    public static boolean hasRule(int modifier) {
        return (RULE & modifier) != 0;
    }
}
