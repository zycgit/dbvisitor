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
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.mapper.MapperRegistry;
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/**
 * 解析动态 SQL 配置
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-05
 */
public class RegistryManager {
    public static final RegistryManager     DEFAULT = new RegistryManager(MappingRegistry.DEFAULT, RuleRegistry.DEFAULT, MacroRegistry.DEFAULT);
    private             PageSqlDialect      dialect;
    private final       MapperRegistry      mapperRegistry;
    private final       MappingRegistry     mappingRegistry;
    private final       TypeHandlerRegistry typeRegistry;
    private final       RuleRegistry        ruleRegistry;
    private final       MacroRegistry       macroRegistry;

    public RegistryManager() {
        this.typeRegistry = new TypeHandlerRegistry();
        this.ruleRegistry = new RuleRegistry();
        this.macroRegistry = new MacroRegistry();
        this.mappingRegistry = new MappingRegistry(null, this.typeRegistry, MappingOptions.buildNew());
        this.mapperRegistry = new MapperRegistry(this.macroRegistry, this.mappingRegistry, this.typeRegistry);
        this.dialect = new DefaultSqlDialect();
    }

    public RegistryManager(MappingRegistry mapping, RuleRegistry rule, MacroRegistry macro) {
        MappingRegistry usingMapping = mapping != null ? mapping : MappingRegistry.DEFAULT;

        this.typeRegistry = usingMapping.getTypeRegistry();
        this.ruleRegistry = rule != null ? rule : RuleRegistry.DEFAULT;
        this.macroRegistry = macro != null ? macro : MacroRegistry.DEFAULT;
        this.mappingRegistry = usingMapping;
        this.mapperRegistry = new MapperRegistry(this.macroRegistry, this.mappingRegistry, this.typeRegistry);
        this.dialect = new DefaultSqlDialect();
    }

    public RegistryManager(TypeHandlerRegistry type, RuleRegistry rule, MacroRegistry macro) {
        TypeHandlerRegistry usingTypeRegistry = type != null ? type : TypeHandlerRegistry.DEFAULT;
        MappingRegistry usingMapping = new MappingRegistry(null, usingTypeRegistry);

        this.typeRegistry = usingMapping.getTypeRegistry();
        this.ruleRegistry = rule != null ? rule : RuleRegistry.DEFAULT;
        this.macroRegistry = macro != null ? macro : MacroRegistry.DEFAULT;
        this.mappingRegistry = usingMapping;
        this.mapperRegistry = new MapperRegistry(this.macroRegistry, this.mappingRegistry, this.typeRegistry);
        this.dialect = new DefaultSqlDialect();
    }

    @Deprecated
    public DynamicSql findMacro(String dynamicId) {
        return this.macroRegistry.findMacro(dynamicId);
    }

    public PageSqlDialect getDialect() {
        return this.dialect;
    }

    public void setDialect(PageSqlDialect dialect) {
        this.dialect = dialect;
    }

    public MapperRegistry getMapperRegistry() {
        return this.mapperRegistry;
    }

    public MacroRegistry getMacroRegistry() {
        return this.macroRegistry;
    }

    public MappingRegistry getMappingRegistry() {
        return this.mappingRegistry;
    }

    public TypeHandlerRegistry getTypeRegistry() {
        return this.typeRegistry;
    }

    public RuleRegistry getRuleRegistry() {
        return this.ruleRegistry;
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