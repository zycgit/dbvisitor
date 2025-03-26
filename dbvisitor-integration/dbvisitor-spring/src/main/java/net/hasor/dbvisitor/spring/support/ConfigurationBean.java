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
package net.hasor.dbvisitor.spring.support;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.RuleRegistry;
import net.hasor.dbvisitor.dynamic.rule.SqlRule;
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.mapper.MapperRegistry;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.springframework.core.io.Resource;

import java.util.Map;
import java.util.Objects;

/**
 * BeanFactory that enables injection of DalRegistry.
 * <p>
 * Sample configuration:
 * <pre class="code">
 * {@code
 *     <bean id="dalRegistry" class="net.hasor.dbvisitor.spring.support.ConfigurationBean">
 *         <property name="mapperResources" value="classpath*:dbvisitor/mapper/*Mapper.xml"/>
 *         ...
 *     </bean>
 * }
 * </pre>
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-29
 * @see Mapper
 */
public class ConfigurationBean extends AbstractSupportBean<Configuration> {
    // - registry`s
    private TypeHandlerRegistry   typeRegistry;
    private Map<Class<?>, Object> javaTypeHandlerMap;
    private Map<Integer, Object>  jdbcTypeHandlerMap;
    private RuleRegistry          ruleRegistry;
    private Map<String, Object>   ruleHandlerMap;
    // options
    private Boolean               autoMapping;
    private Boolean               camelCase;
    private Boolean               caseInsensitive;
    private Boolean               useDelimited;
    private String                dialectName;
    private SqlDialect            dialect;
    private Boolean               ignoreNonExistStatement;
    // mappers
    private Resource[]            mapperResources;
    private Class<?>[]            mapperInterfaces;
    //
    private Configuration         configuration;

    private void initDialect() {
        if (this.dialect == null && StringUtils.isNotBlank(this.dialectName)) {
            this.dialect = SqlDialectRegister.findOrCreate(this.dialectName, this.classLoader);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        TypeHandlerRegistry typeRegistry = this.typeRegistry != null ? this.typeRegistry : new TypeHandlerRegistry();
        RuleRegistry ruleRegistry = this.ruleRegistry != null ? this.ruleRegistry : new RuleRegistry();
        ClassLoader classLoader = this.classLoader != null ? this.classLoader : Thread.currentThread().getContextClassLoader();
        initDialect();

        // create config
        Options options = Options.of();
        options.setAutoMapping(this.autoMapping);
        options.setMapUnderscoreToCamelCase(this.camelCase);
        options.setCaseInsensitive(this.caseInsensitive);
        options.setUseDelimited(this.useDelimited);
        options.setIgnoreNonExistStatement(this.ignoreNonExistStatement);
        if (this.dialect != null) {
            options.setDialect(this.dialect);
        } else if (StringUtils.isNotBlank(this.dialectName)) {
            options.setDialect(SqlDialectRegister.findOrCreate(this.dialectName, this.classLoader));
        }

        MappingRegistry mappingRegistry = new MappingRegistry(classLoader, typeRegistry, options);
        MacroRegistry macroRegistry = new MacroRegistry();
        MapperRegistry mapperRegistry = new MapperRegistry(mappingRegistry, macroRegistry);
        this.configuration = new Configuration(mapperRegistry, ruleRegistry);

        // typeHandler
        if (this.javaTypeHandlerMap != null) {
            for (Map.Entry<Class<?>, Object> entry : this.javaTypeHandlerMap.entrySet()) {
                Class<?> type = Objects.requireNonNull(entry.getKey(), "javaTypeHandler type is null.");
                typeRegistry.register(type, castToTypeHandler(entry.getValue()));
            }
        }
        if (this.jdbcTypeHandlerMap != null) {
            for (Map.Entry<Integer, Object> entry : this.jdbcTypeHandlerMap.entrySet()) {
                Integer jdbc = Objects.requireNonNull(entry.getKey(), "jdbcTypeHandlerMap type is null.");
                typeRegistry.register(jdbc, castToTypeHandler(entry.getValue()));
            }
        }

        // ruleHandler
        if (this.ruleHandlerMap != null) {
            for (Map.Entry<String, Object> entry : this.ruleHandlerMap.entrySet()) {
                if (StringUtils.isBlank(entry.getKey())) {
                    throw new IllegalArgumentException("ruleName is blank.");
                }
                ruleRegistry.register(entry.getKey(), castToRuleHandler(entry.getValue()));
            }
        }

        // mapperResources
        if (this.mapperResources != null) {
            for (Resource resource : this.mapperResources) {
                String string = resource.getURI().toString();
                this.configuration.loadMapper(string);
            }
        }

        // mapperInterfaces
        if (this.mapperInterfaces != null) {
            for (Class<?> mapperInterface : this.mapperInterfaces) {
                this.configuration.loadMapper(mapperInterface);
            }
        }
    }

    private TypeHandler<?> castToTypeHandler(Object handlerObject) throws Exception {
        TypeHandler<?> handler = null;
        if (handlerObject instanceof TypeHandler) {
            handler = (TypeHandler<?>) handlerObject;
        } else if (handlerObject instanceof Class) {
            Class<?> handlerClass = (Class) handlerObject;
            if (TypeHandler.class.isAssignableFrom(handlerClass)) {
                if (this.applicationContext != null) {
                    handler = (TypeHandler<?>) createBeanByType(handlerClass, this.applicationContext);
                } else {
                    handler = (TypeHandler<?>) handlerClass.newInstance();
                }
            }
        } else if (handlerObject instanceof String) {
            Class<?> handlerClass = this.classLoader.loadClass(handlerObject.toString());
            if (this.applicationContext != null) {
                handler = (TypeHandler<?>) createBeanByType(handlerClass, this.applicationContext);
            } else {
                handler = (TypeHandler<?>) handlerClass.newInstance();
            }
        }

        if (handler == null) {
            throw new IllegalArgumentException("'" + handlerObject + "' can't be as TypeHandler.");
        } else {
            return handler;
        }
    }

    private SqlRule castToRuleHandler(Object handlerObject) throws Exception {
        SqlRule handler = null;
        if (handlerObject instanceof SqlRule) {
            handler = (SqlRule) handlerObject;
        } else if (handlerObject instanceof Class) {
            Class<?> handlerClass = (Class) handlerObject;
            if (SqlRule.class.isAssignableFrom(handlerClass)) {
                if (this.applicationContext != null) {
                    handler = (SqlRule) createBeanByType(handlerClass, this.applicationContext);
                } else {
                    handler = (SqlRule) handlerClass.newInstance();
                }
            }
        } else if (handlerObject instanceof String) {
            Class<?> handlerClass = this.classLoader.loadClass(handlerObject.toString());
            if (this.applicationContext != null) {
                handler = (SqlRule) createBeanByType(handlerClass, this.applicationContext);
            } else {
                handler = (SqlRule) handlerClass.newInstance();
            }
        }

        if (handler == null) {
            throw new IllegalArgumentException("'" + handlerObject + "' can't be as SqlBuildRule.");
        } else {
            return handler;
        }
    }

    @Override
    public Configuration getObject() {
        return Objects.requireNonNull(this.configuration, "Configuration not init.");
    }

    @Override
    public Class<?> getObjectType() {
        return Configuration.class;
    }

    public void setTypeRegistry(TypeHandlerRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    public void setJavaTypeHandlerMap(Map<Class<?>, Object> javaTypeHandlerMap) {
        this.javaTypeHandlerMap = javaTypeHandlerMap;
    }

    public void setJdbcTypeHandlerMap(Map<Integer, Object> jdbcTypeHandlerMap) {
        this.jdbcTypeHandlerMap = jdbcTypeHandlerMap;
    }

    public void setRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    public void setRuleHandlerMap(Map<String, Object> ruleHandlerMap) {
        this.ruleHandlerMap = ruleHandlerMap;
    }

    public void setAutoMapping(Boolean autoMapping) {
        this.autoMapping = autoMapping;
    }

    public void setCamelCase(Boolean camelCase) {
        this.camelCase = camelCase;
    }

    public void setCaseInsensitive(Boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public void setUseDelimited(Boolean useDelimited) {
        this.useDelimited = useDelimited;
    }

    public void setDialectName(String dialectName) {
        this.dialectName = dialectName;
    }

    public void setDialect(SqlDialect dialect) {
        this.dialect = dialect;
    }

    public void setIgnoreNonExistStatement(Boolean ignoreNonExistStatement) {
        this.ignoreNonExistStatement = ignoreNonExistStatement;
    }

    /**
     * location of dbVisitor mapper files e.g. "classpath*:sqlmap/*-mapper.xml".
     * @param mapperResources location of dbVisitor mapper files
     */
    public void setMapperResources(Resource[] mapperResources) {
        this.mapperResources = mapperResources;
    }

    public void setMapperInterfaces(Class<?>[] mapperInterfaces) {
        this.mapperInterfaces = mapperInterfaces;
    }
}
