/*
 * Copyright 2010-2021 the original author or authors.
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
package net.hasor.db.spring.mapper;
import net.hasor.db.dal.session.DalSession;
import net.hasor.db.spring.support.DalMapperBean;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;

import static org.springframework.util.Assert.notNull;

/**
 * @version 2022-04-29
 * @author 赵永春 (zyc@hasor.net)
 * @see DalMapperBean
 * @see ClassPathMapperScanner
 */
public class MapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {
    private ApplicationContext             applicationContext;
    private String                         basePackage;
    private BeanNameGenerator              nameGenerator;
    private Class<? extends Annotation>    annotationClass;
    private Class<?>                       markerInterface;
    private String                         dalSessionRef;
    private DalSession                     dalSession;
    private Class<? extends DalMapperBean> mapperFactoryBeanClass;
    private String                         lazyInitialization;
    private String                         defaultScope;
    private boolean                        processPropertyPlaceHolders;
    private String                         dependsOn;
    private String                         beanName;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() {
        notNull(this.basePackage, "Property 'basePackage' is required");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // left intentionally blank
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        if (this.processPropertyPlaceHolders) {
            processPropertyPlaceHolders();
        }

        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
        scanner.setAnnotationClass(this.annotationClass);
        scanner.setMarkerInterface(this.markerInterface);
        scanner.setDalSessionRef(this.dalSessionRef);
        scanner.setDalSession(this.dalSession);
        scanner.setResourceLoader(this.applicationContext);
        scanner.setBeanNameGenerator(this.nameGenerator);
        scanner.setMapperFactoryBeanClass(this.mapperFactoryBeanClass);
        scanner.setDependsOn(this.dependsOn);
        if (StringUtils.hasText(this.lazyInitialization)) {
            scanner.setLazyInitialization(Boolean.parseBoolean(this.lazyInitialization));
        }
        if (StringUtils.hasText(this.defaultScope)) {
            scanner.setDefaultScope(this.defaultScope);
        }

        scanner.registerFilters();
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    /*
     * BeanDefinitionRegistries are called early in application startup, before BeanFactoryPostProcessors.
     * This means that PropertyResourceConfigurers will not have been loaded and any property substitution of this class' properties will fail.
     * To avoid this, find any PropertyResourceConfigurers defined in the context and run them on this class' bean definition. Then update the values.
     */
    private void processPropertyPlaceHolders() {
        Map<String, PropertyResourceConfigurer> prcs = this.applicationContext.getBeansOfType(PropertyResourceConfigurer.class, false, false);

        if (!prcs.isEmpty() && this.applicationContext instanceof ConfigurableApplicationContext) {
            BeanDefinition mapperScannerBean = ((ConfigurableApplicationContext) this.applicationContext).getBeanFactory().getBeanDefinition(beanName);

            // PropertyResourceConfigurer does not expose any methods to explicitly perform
            // property placeholder substitution. Instead, create a BeanFactory that just
            // contains this mapper scanner and post process the factory.
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            factory.registerBeanDefinition(this.beanName, mapperScannerBean);

            for (PropertyResourceConfigurer prc : prcs.values()) {
                prc.postProcessBeanFactory(factory);
            }

            PropertyValues values = mapperScannerBean.getPropertyValues();

            this.basePackage = getPropertyValue("basePackage", values);
            this.dalSessionRef = getPropertyValue("dalSessionRef", values);
            this.lazyInitialization = getPropertyValue("lazyInitialization", values);
            this.defaultScope = getPropertyValue("defaultScope", values);
        }
        this.basePackage = Optional.ofNullable(this.basePackage).map(getEnvironment()::resolvePlaceholders).orElse(null);
        this.dalSessionRef = Optional.ofNullable(this.dalSessionRef).map(getEnvironment()::resolvePlaceholders).orElse(null);
        this.lazyInitialization = Optional.ofNullable(this.lazyInitialization).map(getEnvironment()::resolvePlaceholders).orElse(null);
        this.defaultScope = Optional.ofNullable(this.defaultScope).map(getEnvironment()::resolvePlaceholders).orElse(null);
    }

    private Environment getEnvironment() {
        return this.applicationContext.getEnvironment();
    }

    private String getPropertyValue(String propertyName, PropertyValues values) {
        PropertyValue property = values.getPropertyValue(propertyName);

        if (property == null) {
            return null;
        }

        Object value = property.getValue();

        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return value.toString();
        } else if (value instanceof TypedStringValue) {
            return ((TypedStringValue) value).getValue();
        } else {
            return null;
        }
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public void setNameGenerator(BeanNameGenerator nameGenerator) {
        this.nameGenerator = nameGenerator;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    public void setDalSessionRef(String dalSessionRef) {
        this.dalSessionRef = dalSessionRef;
    }

    public void setDalSession(DalSession dalSession) {
        this.dalSession = dalSession;
    }

    public void setMapperFactoryBeanClass(Class<? extends DalMapperBean> mapperFactoryBeanClass) {
        this.mapperFactoryBeanClass = mapperFactoryBeanClass;
    }

    public void setLazyInitialization(String lazyInitialization) {
        this.lazyInitialization = lazyInitialization;
    }

    public void setDefaultScope(String defaultScope) {
        this.defaultScope = defaultScope;
    }

    public void setProcessPropertyPlaceHolders(boolean processPropertyPlaceHolders) {
        this.processPropertyPlaceHolders = processPropertyPlaceHolders;
    }

    public void setDependsOn(String dependsOn) {
        this.dependsOn = dependsOn;
    }
}
