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
package net.hasor.dbvisitor.template.jdbc.core;
import net.hasor.cobble.ClassUtils;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.RuleRegistry;
import net.hasor.dbvisitor.dynamic.rule.SqlRule;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

public class JdbcQueryContext implements QueryContext {
    private TypeHandlerRegistry typeRegistry  = TypeHandlerRegistry.DEFAULT;
    private MacroRegistry       macroRegistry = MacroRegistry.DEFAULT;
    private RuleRegistry        ruleRegistry  = RuleRegistry.DEFAULT;
    private Options             options       = Options.of();

    public MacroRegistry getMacroRegistry() {
        return this.macroRegistry;
    }

    public void setMacroRegistry(MacroRegistry macroRegistry) {
        this.macroRegistry = macroRegistry;
    }

    public void setTypeRegistry(TypeHandlerRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    public RuleRegistry getRuleRegistry() {
        return this.ruleRegistry;
    }

    public void setRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    public Options getOptions() {
        return this.options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    @Override
    public SqlRule findRule(String ruleName) {
        return this.ruleRegistry.findRule(ruleName);
    }

    @Override
    public DynamicSql findMacro(String dynamicId) {
        return this.macroRegistry.findMacro(dynamicId);
    }

    @Override
    public TypeHandlerRegistry getTypeRegistry() {
        return this.typeRegistry;
    }

    @Override
    public Options options() {
        return this.options;
    }

    public void addMacro(String name, String segment) {
        this.macroRegistry.register(name, segment);
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return ClassUtils.getClass(JdbcQueryContext.class.getClassLoader(), className);
    }

    public Object createObject(Class<?> clazz) {
        return ClassUtils.newInstance(clazz);
    }
}
