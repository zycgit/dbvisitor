/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic.args;
/**
 * 用于关闭 SQL 参数的资源分配，例如： Lob 类型参数。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-14
 */
public interface SqlArgDisposer {
    /** 关闭参数分配的可回收资源，例如：Lob 类型参数。 */
    void cleanupParameters();
}
