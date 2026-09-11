/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping;
/**
 * 主键生成器工厂接口，用于创建主键生成处理器实例，实现该接口可以提供自定义的主键生成策略
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-01
 */
public interface GeneratedKeyHandlerFactory {
    /**
     * 创建主键生成处理器
     * @param context 主键生成上下文，包含生成主键所需的配置信息
     * @return 主键生成处理器实例
     * @throws ClassNotFoundException 当指定的处理器类无法加载时抛出
     */
    GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) throws ClassNotFoundException;
}
