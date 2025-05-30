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
package net.hasor.dbvisitor.spring.mapper;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.mapper.MapperDef;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.spring.support.MapperBean;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link ClassPathBeanDefinitionScanner} that registers Mappers by {@code basePackage}, {@code annotationClass}, or
 * {@code markerInterface}. If an {@code annotationClass} and/or {@code markerInterface} is specified, only the
 * specified types will be searched (searching for all interfaces will be disabled).
 * <p>
 * This functionality was previously a private class of {@link MapperScannerConfigurer}, but was broken out in version
 * 1.2.0.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-29
 * @see MapperBean
 */
public class ClassPathMapperScanner extends ClassPathBeanDefinitionScanner {
    private static final Logger                      logger                 = LoggerFactory.getLogger(ClassPathMapperScanner.class);
    private              boolean                     mapperDisabled;
    private              Class<? extends Annotation> annotationClass;
    private              Class<?>                    markerInterface;
    private              Session                     session;
    private              String                      sessionRef;
    private              Class<?>                    mapperFactoryBeanClass = MapperBean.class;
    private              boolean                     lazyInit;
    private              String                      defaultScope;
    private              String                      dependsOn;

    public ClassPathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    public void registerFilters() {
        boolean acceptAllInterfaces = true;

        // if specified, use the given annotation and / or marker interface
        if (this.annotationClass != null) {
            if (this.annotationClass == MapperDef.class) {
                addIncludeFilter(new AnnotationTypeFilter(MapperDef.class, true));
            } else {
                addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
            }
            acceptAllInterfaces = false;
        }

        // override AssignableTypeFilter to ignore matches on the actual marker interface
        if (this.markerInterface != null) {
            addIncludeFilter(new AssignableTypeFilter(this.markerInterface) {
                @Override
                protected boolean matchClassName(String className) {
                    return false;
                }
            });
            acceptAllInterfaces = false;
        }

        if (acceptAllInterfaces) {
            // default include filter that accepts all interfaces
            addIncludeFilter((metadataReader, metadataReaderFactory) -> {
                return metadataReader.getClassMetadata().isInterface();
            });
        }

        // exclude package-info.java
        addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            String className = metadataReader.getClassMetadata().getClassName();
            return className.endsWith("package-info");
        });
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            logger.warn("No dbVisitor mapper was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            BeanDefinitionRegistry registry = getRegistry();
            for (BeanDefinitionHolder beanDef : beanDefinitions) {
                processBeanDefinitions(registry, beanDef);
            }
        }

        return beanDefinitions;
    }

    private void processBeanDefinitions(BeanDefinitionRegistry registry, BeanDefinitionHolder beanDef) {
        if (this.mapperDisabled) {
            return;
        }

        AbstractBeanDefinition definition = (AbstractBeanDefinition) beanDef.getBeanDefinition();
        boolean scopedProxy = false;
        if (ScopedProxyFactoryBean.class.getName().equals(definition.getBeanClassName())) {
            definition = (AbstractBeanDefinition) Optional//
                    .ofNullable(((RootBeanDefinition) definition).getDecoratedDefinition())//
                    .map(BeanDefinitionHolder::getBeanDefinition)//
                    .orElseThrow(() -> new IllegalStateException("The target bean definition of scoped proxy bean not found. Root bean definition[" + beanDef + "]"));
            scopedProxy = true;
        }
        String beanClassName = definition.getBeanClassName();
        logger.debug("Creating MapperBean with name '" + beanDef.getBeanName() + "' and '" + beanClassName + "' mapperInterface");

        // the mapper interface is the original class of the bean but, the actual class of the bean is MapperFactoryBean
        definition.getPropertyValues().add("mapperInterface", beanClassName);
        definition.setBeanClass(this.mapperFactoryBeanClass != null ? this.mapperFactoryBeanClass : MapperBean.class);

        // Attribute for MockitoPostProcessor
        // https://github.com/mybatis/spring-boot-starter/issues/475
        // Copy of FactoryBean#OBJECT_TYPE_ATTRIBUTE which was added in Spring 5.2
        definition.setAttribute("factoryBeanObjectType", beanClassName);

        // configuration
        boolean explicitFactoryUsed = false;
        if (StringUtils.hasText(this.sessionRef)) {
            definition.getPropertyValues().add("session", new RuntimeBeanReference(this.sessionRef));
            explicitFactoryUsed = true;
        } else if (this.session != null) {
            definition.getPropertyValues().add("session", this.session);
            explicitFactoryUsed = true;
        }
        if (!explicitFactoryUsed) {
            logger.debug("Enabling autowire by type for MapperFactoryBean with name '" + beanDef.getBeanName() + "'.");
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }

        // lazy
        definition.setLazyInit(this.lazyInit);

        if (StringUtils.hasText(dependsOn)) {
            definition.setDependsOn(new String[] { dependsOn });
        }

        // scope
        if (scopedProxy) {
            return;
        }
        if (ConfigurableBeanFactory.SCOPE_SINGLETON.equals(definition.getScope()) && this.defaultScope != null) {
            definition.setScope(defaultScope);
        }

        if (!definition.isSingleton()) {
            BeanDefinitionHolder proxyHolder = ScopedProxyUtils.createScopedProxy(beanDef, registry, true);
            if (registry.containsBeanDefinition(proxyHolder.getBeanName())) {
                registry.removeBeanDefinition(proxyHolder.getBeanName());
            }

            registry.registerBeanDefinition(proxyHolder.getBeanName(), proxyHolder.getBeanDefinition());
        }
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) {
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            logger.warn("Skipping MapperFactoryBean with name '" + beanName + "' and '" + beanDefinition.getBeanClassName() + "' mapperInterface" + ". Bean already defined with the same name!");
            return false;
        }
    }

    public void setMapperDisabled(Boolean mapperDisabled) {
        this.mapperDisabled = mapperDisabled;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    public void setSessionRef(String sessionRef) {
        this.sessionRef = sessionRef;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setMapperFactoryBeanClass(Class<?> mapperFactoryBeanClass) {
        this.mapperFactoryBeanClass = mapperFactoryBeanClass;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public void setDefaultScope(String defaultScope) {
        this.defaultScope = defaultScope;
    }

    public void setDependsOn(String dependsOn) {
        this.dependsOn = dependsOn;
    }
}
