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
import net.hasor.cobble.ClassUtils;
import net.hasor.dbvisitor.dynamic.rule.SqlRule;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/**
 * 多个 SQL 节点组合成一个 SqlNode
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-24
 */
public class TestQueryContext implements QueryContext {
    private final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
    private final RuleRegistry        ruleRegistry        = new RuleRegistry();
    private final MacroRegistry       macroRegistry       = new MacroRegistry();

    public void addMacro(String macroName, String sqlSegment) {
        this.macroRegistry.register(macroName, sqlSegment);
    }

    @Override
    public SqlRule findRule(String ruleName) {
        return this.ruleRegistry.findRule(ruleName);
    }

    @Override
    public DynamicSql findMacro(String name) {
        return macroRegistry.findMacro(name);
    }

    @Override
    public TypeHandlerRegistry getTypeRegistry() {
        return this.typeHandlerRegistry;
    }

    @Override
    public Options options() {
        return Options.of();
    }

    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return ClassUtils.getClass(this.getClassLoader(), className);
    }

    public Object createObject(Class<?> clazz) {
        return ClassUtils.newInstance(clazz);
    }

}