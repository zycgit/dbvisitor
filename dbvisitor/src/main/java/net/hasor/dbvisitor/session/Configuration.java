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
package net.hasor.dbvisitor.session;
import net.hasor.cobble.ClassUtils;
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.RuleRegistry;
import net.hasor.dbvisitor.dynamic.rule.SqlRule;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapper.MapperRegistry;
import net.hasor.dbvisitor.mapper.StatementDef;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 解析动态 SQL 配置
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public class Configuration implements QueryContext {
    private       ClassLoader         classLoader;
    private       Options             options;
    private       TypeHandlerRegistry typeRegistry;
    private       MacroRegistry       macroRegistry;
    private       RuleRegistry        ruleRegistry;
    private       MappingRegistry     mappingRegistry;
    private       MapperRegistry      mapperRegistry;
    private final SessionPrototype    prototype;

    public Configuration() {
        this(Options.of().dialect(new DefaultSqlDialect()));
    }

    public Configuration(Options options) {
        MappingRegistry newMapping = new MappingRegistry(Configuration.class.getClassLoader(), options);
        this.classLoader = newMapping.getClassLoader();
        this.options = options;
        this.typeRegistry = newMapping.getTypeRegistry();
        this.macroRegistry = new MacroRegistry();
        this.ruleRegistry = new RuleRegistry();
        this.mappingRegistry = newMapping;
        this.mapperRegistry = new MapperRegistry(this.mappingRegistry, this.macroRegistry);
        this.prototype = new SessionPrototype(this);
    }

    public Configuration(Options options, ClassLoader classLoader) {
        MappingRegistry newMapping = new MappingRegistry(classLoader, options);
        this.classLoader = newMapping.getClassLoader();
        this.options = options;
        this.typeRegistry = newMapping.getTypeRegistry();
        this.macroRegistry = new MacroRegistry();
        this.ruleRegistry = new RuleRegistry();
        this.mappingRegistry = newMapping;
        this.mapperRegistry = new MapperRegistry(this.mappingRegistry, this.macroRegistry);
        this.prototype = new SessionPrototype(this);
    }

    public Configuration(MappingRegistry mapping, MacroRegistry macroRegistry, RuleRegistry ruleRegistry) {
        this.classLoader = mapping.getClassLoader();
        this.options = mapping.getGlobalOptions();
        this.typeRegistry = mapping.getTypeRegistry();
        this.macroRegistry = macroRegistry;
        this.ruleRegistry = ruleRegistry;
        this.mappingRegistry = mapping;
        this.mapperRegistry = new MapperRegistry(this.mappingRegistry, this.macroRegistry);
        this.prototype = new SessionPrototype(this);
    }

    public Configuration(MapperRegistry mapper, RuleRegistry rule) {
        this.classLoader = mapper.getClassLoader();
        this.options = mapper.getMappingRegistry().getGlobalOptions();
        this.typeRegistry = mapper.getTypeRegistry();
        this.macroRegistry = mapper.getMacroRegistry();
        this.ruleRegistry = rule == null ? new RuleRegistry() : rule;
        this.mappingRegistry = mapper.getMappingRegistry();
        this.mapperRegistry = mapper;
        this.prototype = new SessionPrototype(this);
    }

    /** load mapperType. */
    public void loadMapper(Class<?> mapperType) throws Exception {
        this.mapperRegistry.loadMapper(mapperType);
    }

    /** load mapperFile. */
    public void loadMapper(String resource) throws Exception {
        this.mapperRegistry.loadMapper(resource);
    }

    public <T> TableMapping<T> loadEntityToSpace(Class<T> entityType) {
        return this.mappingRegistry.loadEntityToSpace(entityType);
    }

    public <T> TableMapping<T> loadEntityToSpace(Class<T> entityType, String space) {
        return this.mappingRegistry.loadEntityToSpace(entityType, space);
    }

    public void addMacro(String name, String segment) {
        this.macroRegistry.register(name, segment);
    }

    /** 注册 SqlRule */
    public void addSqlRule(String ruleName, SqlRule rule) {
        this.ruleRegistry.register(ruleName, rule);
    }

    /** create {@link JdbcTemplate} for {@link Connection} */
    public JdbcTemplate newJdbc(Connection conn) {
        return new JdbcTemplate(conn, this.getMappingRegistry(), this);
    }

    /** create {@link JdbcTemplate} for {@link DataSource} */
    public JdbcTemplate newJdbc(DataSource ds) {
        return new JdbcTemplate(ds, this.getMappingRegistry(), this);
    }

    /** create {@link JdbcTemplate} for {@link DynamicConnection} */
    public JdbcTemplate newJdbc(DynamicConnection dc) {
        return new JdbcTemplate(dc, this.getMappingRegistry(), this);
    }

    /** create {@link WrapperAdapter} for {@link Connection} */
    public WrapperAdapter newWrapper(Connection conn) throws SQLException {
        return new WrapperAdapter(conn, this.getMappingRegistry(), this);
    }

    /** create {@link WrapperAdapter} for {@link DataSource} */
    public WrapperAdapter newWrapper(DataSource ds) throws SQLException {
        return new WrapperAdapter(ds, this.getMappingRegistry(), this);
    }

    /** create {@link WrapperAdapter} for {@link DynamicConnection} */
    public WrapperAdapter newWrapper(DynamicConnection dc) throws SQLException {
        return new WrapperAdapter(dc, this.getMappingRegistry(), this);
    }

    /** create {@link Session} for {@link Connection} */
    public Session newSession(Connection conn) throws SQLException {
        return this.prototype.newSession(conn);
    }

    /** create {@link Session} for {@link DataSource} */
    public Session newSession(DataSource ds) throws SQLException {
        return this.prototype.newSession(ds);
    }

    /** create {@link Session} for {@link DynamicConnection} */
    public Session newSession(DynamicConnection dc) throws SQLException {
        return this.prototype.newSession(dc);
    }

    @Override
    public SqlRule findRule(String ruleName) {
        return this.ruleRegistry.findRule(ruleName);
    }

    @Override
    public DynamicSql findMacro(String dynamicId) {
        return this.macroRegistry.findMacro(dynamicId);
    }

    public StatementDef findStatement(Class<?> namespace, String statement) {
        return this.mapperRegistry.findStatement(namespace, statement);
    }

    public TableMapping<?> findByEntity(Class<?> entityType) {
        return this.mappingRegistry.findByEntity(entityType);
    }

    public TableMapping<?> findBySpace(String space, Class<?> entityType) {
        return this.mappingRegistry.findBySpace(space, entityType);
    }

    @Override
    public TypeHandlerRegistry getTypeRegistry() {
        return this.typeRegistry;
    }

    @Override
    public Options options() {
        return this.options;
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return ClassUtils.getClass(this.classLoader, className);
    }

    public Object createObject(Class<?> clazz) {
        return ClassUtils.newInstance(clazz);
    }

    public void setTypeRegistry(TypeHandlerRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    public MacroRegistry getMacroRegistry() {
        return this.macroRegistry;
    }

    public void setMacroRegistry(MacroRegistry macroRegistry) {
        this.macroRegistry = macroRegistry;
    }

    public RuleRegistry getRuleRegistry() {
        return this.ruleRegistry;
    }

    public void setRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    public MapperRegistry getMapperRegistry() {
        return this.mapperRegistry;
    }

    public void setMapperRegistry(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public MappingRegistry getMappingRegistry() {
        return this.mappingRegistry;
    }

    public void setMappingRegistry(MappingRegistry mappingRegistry) {
        this.mappingRegistry = mappingRegistry;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Options getOptions() {
        return this.options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }
}