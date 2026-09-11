/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic;
import net.hasor.dbvisitor.dynamic.rule.SqlRule;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/**
 * 动态 SQL 查询上下文接口，提供动态 SQL 处理过程中所需的上下文信息和方法
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-02-01
 */
public interface QueryContext {
    /**
     * 根据规则名称查找 SQL 处理规则
     * @param ruleName 规则名称
     * @return 对应的 SQL 处理规则，找不到返回 null
     */
    SqlRule findRule(String ruleName);

    /**
     * 根据名称查找宏定义
     * @param name 宏名称
     * @return 对应的宏定义，找不到返回 null
     */
    DynamicSql findMacro(String name);

    /**
     * 加载指定类
     * @param typeName 类全限定名
     * @return 加载的类对象
     */
    Class<?> loadClass(String typeName) throws ClassNotFoundException;

    /**
     * 创建指定类的实例
     * @param clazz 要实例化的类
     * @return 创建的实例
     */
    Object createObject(Class<?> clazz);

    /** 获取类型处理器注册表 */
    TypeHandlerRegistry getTypeRegistry();

    /** 获取配置选项 */
    Options options();
}
