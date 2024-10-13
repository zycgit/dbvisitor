/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.spring.support;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.dynamic.rule.SqlBuildRule;
import net.hasor.dbvisitor.dal.mapper.Mapper;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * BeanFactory that enables injection of DalRegistry.
 * <p>
 * Sample configuration:
 *
 * <pre class="code">
 * {@code
 *     <bean id="dalRegistry" class="net.hasor.dbvisitor.spring.support.DalRegistryBean">
 *         <property name="mapperResources" value="classpath*:dbvisitor/mapper/*Mapper.xml"/>
 *         ...
 *     </bean>
 * }
 * </pre>
 *
 * @version : 2022-04-29
 * @author 赵永春 (zyc@hasor.net)
 * @see Mapper
 */
public class DalRegistryBean extends AbstractSupportBean<DalRegistry> {
    // - dalTypeRegistry
    private TypeHandlerRegistry   typeRegistry;
    private Map<Class<?>, Object> javaTypeHandlerMap;
    private Map<Integer, Object>  jdbcTypeHandlerMap;
    // - dalRuleRegistry
    private RuleRegistry          ruleRegistry;
    private Map<String, Object>   ruleHandlerMap;
    // - dalRegistry
    private DalRegistry           dalRegistry;
    private Boolean               autoMapping;
    private Boolean               camelCase;
    private Boolean               caseInsensitive;
    private Boolean               useDelimited;
    private String                dialectName;
    private SqlDialect            dialect;
    // mappers
    private Resource[]            mapperResources;
    private Class<?>[]            mapperInterfaces;

    private void initDialect() throws Exception {
        if (this.dialect == null && StringUtils.isNotBlank(this.dialectName)) {
            Class<?> dialectClass = this.classLoader.loadClass(dialectName);
            if (this.applicationContext != null) {
                this.dialect = (PageSqlDialect) createBeanByType(dialectClass, this.applicationContext);
            } else {
                this.dialect = (PageSqlDialect) dialectClass.newInstance();
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        TypeHandlerRegistry typeRegistry = this.typeRegistry != null ? this.typeRegistry : new TypeHandlerRegistry();
        RuleRegistry ruleRegistry = this.ruleRegistry != null ? this.ruleRegistry : new RuleRegistry();
        ClassLoader classLoader = this.classLoader != null ? this.classLoader : Thread.currentThread().getContextClassLoader();
        initDialect();

        MappingOptions options = MappingOptions.buildNew();
        options.setAutoMapping(this.autoMapping);
        options.setMapUnderscoreToCamelCase(this.camelCase);
        options.setCaseInsensitive(this.caseInsensitive);
        options.setUseDelimited(this.useDelimited);
        if (this.dialect != null) {
            options.setDefaultDialect(this.dialect);
        } else if (StringUtils.isNotBlank(this.dialectName)) {
            options.setDefaultDialect(SqlDialectRegister.findOrCreate(this.dialectName, this.classLoader));
        }

        this.dalRegistry = new DalRegistry(classLoader, typeRegistry, ruleRegistry, options);

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
        if (this.mapperResources != null && this.mapperResources.length > 0) {
            for (Resource resource : this.mapperResources) {
                try (InputStream ins = resource.getInputStream()) {
                    this.dalRegistry.loadMapper(ins);
                }
            }
        }

        // mapperInterfaces
        if (this.mapperInterfaces != null && this.mapperInterfaces.length > 0) {
            for (Class<?> mapperInterface : this.mapperInterfaces) {
                this.dalRegistry.loadMapper(mapperInterface);
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

    private SqlBuildRule castToRuleHandler(Object handlerObject) throws Exception {
        SqlBuildRule handler = null;
        if (handlerObject instanceof SqlBuildRule) {
            handler = (SqlBuildRule) handlerObject;
        } else if (handlerObject instanceof Class) {
            Class<?> handlerClass = (Class) handlerObject;
            if (SqlBuildRule.class.isAssignableFrom(handlerClass)) {
                if (this.applicationContext != null) {
                    handler = (SqlBuildRule) createBeanByType(handlerClass, this.applicationContext);
                } else {
                    handler = (SqlBuildRule) handlerClass.newInstance();
                }
            }
        } else if (handlerObject instanceof String) {
            Class<?> handlerClass = this.classLoader.loadClass(handlerObject.toString());
            if (this.applicationContext != null) {
                handler = (SqlBuildRule) createBeanByType(handlerClass, this.applicationContext);
            } else {
                handler = (SqlBuildRule) handlerClass.newInstance();
            }
        }

        if (handler == null) {
            throw new IllegalArgumentException("'" + handlerObject + "' can't be as SqlBuildRule.");
        } else {
            return handler;
        }
    }

    @Override
    public DalRegistry getObject() {
        return Objects.requireNonNull(this.dalRegistry, "dalRegistry not init.");
    }

    @Override
    public Class<?> getObjectType() {
        return DalRegistry.class;
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
