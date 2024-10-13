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
package net.hasor.dbvisitor.spring.boot;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.dal.mapper.Mapper;
import net.hasor.dbvisitor.dal.repository.DalMapper;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.dbvisitor.spring.mapper.MapperFileConfigurer;
import net.hasor.dbvisitor.spring.mapper.MapperScannerConfigurer;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;

import static net.hasor.dbvisitor.spring.boot.DbVisitorProperties.PREFIX;

@Configuration
@ConditionalOnClass({ DalSession.class, DalRegistry.class })
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties(DbVisitorProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class DbVisitorAutoConfiguration implements BeanClassLoaderAware, ApplicationContextAware, InitializingBean {
    private static final Logger              logger = LoggerFactory.getLogger(DbVisitorAutoConfiguration.class);
    private              ApplicationContext  applicationContext;
    private              ClassLoader         classLoader;
    private final        DbVisitorProperties properties;
    //private final Interceptor[]     interceptors;

    public DbVisitorAutoConfiguration(DbVisitorProperties properties) {
        this.properties = properties;
        // this.interceptors = (Interceptor[]) interceptorsProvider.getIfAvailable();
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() {
        //
    }

    @Bean
    @ConditionalOnMissingBean
    public DalRegistry dalRegistry(ObjectProvider<TypeHandlerRegistry> typeHandlersProvider, ObjectProvider<RuleRegistry> ruleRegistryProvider) {
        TypeHandlerRegistry typeHandlerRegistry = typeHandlersProvider.getIfAvailable();
        RuleRegistry ruleRegistry = ruleRegistryProvider.getIfAvailable();

        MappingOptions options = MappingOptions.buildNew();
        options.setAutoMapping(this.properties.getAutoMapping());
        options.setMapUnderscoreToCamelCase(this.properties.getCamelCase());
        options.setCaseInsensitive(this.properties.getCaseInsensitive());
        options.setUseDelimited(this.properties.getUseDelimited());
        if (StringUtils.hasText(this.properties.getDialect())) {
            options.setDefaultDialect(SqlDialectRegister.findOrCreate(this.properties.getDialect(), this.classLoader));
        }
        return new DalRegistry(this.classLoader, typeHandlerRegistry, ruleRegistry, options);
    }

    @Bean
    @ConditionalOnMissingBean
    public DalSession dalSession(DataSource dataSource, DalRegistry dalRegistry) throws Exception {
        String refSessionBean = this.properties.getRefSessionBean();
        if (StringUtils.hasText(refSessionBean)) {
            return (DalSession) this.applicationContext.getBean(refSessionBean);
        } else {
            return new DalSession(dataSource, dalRegistry);
        }
    }

    //  jdbcTemplate name of the conflict

    @Bean
    @ConditionalOnMissingBean
    public LambdaTemplate lambdaTemplate(DataSource dataSource, ObjectProvider<TypeHandlerRegistry> typeHandlersProvider) {
        TypeHandlerRegistry typeHandlerRegistry = typeHandlersProvider.getIfAvailable();
        if (typeHandlerRegistry == null) {
            return new LambdaTemplate(dataSource);
        } else {
            return new LambdaTemplate(dataSource, typeHandlerRegistry);
        }
    }

    /**
     * If mapper registering configuration or mapper scanning configuration not present,
     * this configuration allow to scan mappers based on the same component-scanning path as Spring Boot itself.
     */
    @Configuration
    @Import(AutoConfiguredMapperScannerRegistrar.class)
    @ConditionalOnMissingBean({ DalSession.class, MapperScannerConfigurer.class })
    public static class MapperScannerRegistrarNotFoundConfiguration implements InitializingBean {
        public void afterPropertiesSet() {
            logger.debug("Not found configuration for registering mapper bean using @MapperScan, DalSessionBean and MapperScannerConfigurer.");
        }
    }

    public static class AutoConfiguredMapperScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar {
        private BeanFactory beanFactory;

        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            if (!AutoConfigurationPackages.has(this.beanFactory)) {
                logger.debug("Could not determine auto-configuration package, automatic mapper scanning disabled.");
                return;
            }

            logger.debug("Searching for mappers annotated with @DalMapper");
            List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
            if (logger.isDebugEnabled()) {
                packages.forEach(pkg -> logger.debug("Using auto-configuration base package '" + pkg + "'"));
            }

            // load mapper xml
            String fileBeanName = MapperFileConfigurer.class.getName();
            BeanDefinitionBuilder fileBuilder = BeanDefinitionBuilder.genericBeanDefinition(MapperFileConfigurer.class);
            fileBuilder.addPropertyValue("processPropertyPlaceHolders", true);
            fileBuilder.addPropertyValue("mapperLocations", "${" + PREFIX + ".mapper-locations:classpath*:/dbvisitor/mapper/**/*.xml}");
            fileBuilder.addPropertyValue("dalSessionRef", "${" + PREFIX + ".ref-session-bean:}");
            registry.registerBeanDefinition(fileBeanName, fileBuilder.getBeanDefinition());

            // load mapper interface
            String mapperBeanName = MapperScannerConfigurer.class.getName();
            BeanDefinitionBuilder mapperBuilder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
            mapperBuilder.addPropertyValue("mapperDisabled", "${" + PREFIX + ".mapper-disabled:false}");
            mapperBuilder.addPropertyValue("processPropertyPlaceHolders", true);
            mapperBuilder.addPropertyValue("basePackage", "${" + PREFIX + ".mapper-packages:" + StringUtils.collectionToCommaDelimitedString(packages) + "}");
            mapperBuilder.addPropertyValue("mapperFactoryBeanClassName", "${" + PREFIX + ".mapper-factory-bean:}");
            mapperBuilder.addPropertyValue("defaultScope", "${" + PREFIX + ".mapper-scope:" + AbstractBeanDefinition.SCOPE_DEFAULT + "}");
            mapperBuilder.addPropertyValue("lazyInitialization", "${" + PREFIX + ".mapper-lazy-initialization:false}");
            mapperBuilder.addPropertyValue("nameGeneratorName", "${" + PREFIX + ".mapper-name-generator:}");
            mapperBuilder.addPropertyValue("annotationClassName", "${" + PREFIX + ".marker-annotation:" + DalMapper.class.getName() + "}");
            mapperBuilder.addPropertyValue("markerInterfaceName", "${" + PREFIX + ".marker-interface:" + Mapper.class.getName() + "}");
            mapperBuilder.addPropertyValue("dalSessionRef", "${" + PREFIX + ".ref-session-bean:}");
            mapperBuilder.addPropertyValue("dependsOn", fileBeanName);

            registry.registerBeanDefinition(mapperBeanName, mapperBuilder.getBeanDefinition());
        }
    }
}