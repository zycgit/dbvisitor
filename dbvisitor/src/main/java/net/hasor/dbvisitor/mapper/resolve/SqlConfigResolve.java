/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper.resolve;
import net.hasor.dbvisitor.mapper.def.SqlConfig;

/**
 * 解析动态 SQL 配置的接口。
 * 实现该接口的类需要提供具体的逻辑，将特定类型的配置信息解析为 SqlConfig 对象。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public interface SqlConfigResolve<T> {
    /**
     * 解析指定命名空间下的动态 SQL 配置。
     * @param namespace 命名空间，用于区分不同的 SQL 配置组。
     * @param config 待解析的配置对象，具体类型由泛型 T 决定。
     * @return 解析后的 SqlConfig 对象，包含解析完成的 SQL 配置信息。
     */
    SqlConfig parseSqlConfig(String namespace, T config);
}
