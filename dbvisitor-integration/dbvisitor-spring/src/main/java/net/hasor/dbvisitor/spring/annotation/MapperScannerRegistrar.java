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
package net.hasor.dbvisitor.spring.annotation;
import net.hasor.dbvisitor.spring.mapper.ClassPathMapperScanner;
import net.hasor.dbvisitor.spring.mapper.MapperFileConfigurer;
import net.hasor.dbvisitor.spring.mapper.MapperScannerConfigurer;
import net.hasor.dbvisitor.spring.support.DalMapperBean;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A {@link ImportBeanDefinitionRegistrar} to allow annotation configuration of dbVisitor mapper scanning. Using
 * an @Enable annotation allows beans to be registered via @Component configuration, whereas implementing
 * {@code BeanDefinitionRegistryPostProcessor} will work for XML configuration.
 *
 * @version 2022-04-29
 * @author 赵永春 (zyc@hasor.net)
 * @see ClassPathMapperScanner
 * @since 1.2.0
 */
public class MapperScannerRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes mappingScanAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(MappingScan.class.getName()));
        AnnotationAttributes mapperScanAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(MapperScan.class.getName()));
        if (mappingScanAttrs != null && mapperScanAttrs != null) {
            throw new IllegalStateException("@MappingScan and @MapperScan can only use one of them");
        }
        if (mappingScanAttrs == null && mapperScanAttrs == null) {
            return;
        }

        // add MapperFileConfigurer bean
        String scanMapperFileBean = generateBaseBeanName(importingClassMetadata, "resource", 0);
        AnnotationAttributes mappingAttrs = mappingScanAttrs != null ? mappingScanAttrs : mapperScanAttrs;
        registerMapperFileScannerBean(importingClassMetadata, mappingAttrs, registry, scanMapperFileBean);

        // add MapperScannerConfigurer bean
        if (mapperScanAttrs != null) {
            String scanMapperBean = generateBaseBeanName(importingClassMetadata, "mapper", 0);
            registerMapperScannerBean(importingClassMetadata, mapperScanAttrs, registry, scanMapperBean, scanMapperFileBean);
        }
    }

    void registerMapperFileScannerBean(AnnotationMetadata annoMeta, AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry, String beanName) {
        List<String> mapperFiles = Arrays.stream(annoAttrs.getStringArray("mapperLocations")).filter(StringUtils::hasText).collect(Collectors.toList());
        Set<String> mapperLocations = new LinkedHashSet<>();
        if (!mapperFiles.isEmpty()) {
            mapperLocations.addAll(mapperFiles);
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperFileConfigurer.class);
        builder.addPropertyValue("mapperLocations", StringUtils.collectionToCommaDelimitedString(mapperLocations));

        String dalSessionRef = annoAttrs.getString("dalSessionRef");
        if (StringUtils.hasText(dalSessionRef)) {
            builder.addPropertyValue("dalSession", new RuntimeBeanReference(dalSessionRef));
        }

        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }

    void registerMapperScannerBean(AnnotationMetadata annoMeta, AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry, String beanName, String dependsOn) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);

        // attr - basePackage
        List<String> basePackages = new ArrayList<>();
        basePackages.addAll(Arrays.stream(annoAttrs.getStringArray("value")).filter(StringUtils::hasText).collect(Collectors.toList()));
        basePackages.addAll(Arrays.stream(annoAttrs.getStringArray("basePackages")).filter(StringUtils::hasText).collect(Collectors.toList()));
        basePackages.addAll(Arrays.stream(annoAttrs.getClassArray("basePackageClasses")).map(ClassUtils::getPackageName).collect(Collectors.toList()));
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(annoMeta.getClassName()));
        }
        builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(basePackages));

        // attr - nameGenerator
        Class<? extends BeanNameGenerator> generatorClass = annoAttrs.getClass("nameGenerator");
        if (!BeanNameGenerator.class.equals(generatorClass)) {
            builder.addPropertyValue("nameGenerator", BeanUtils.instantiateClass(generatorClass));
        }

        // attr - annotationClass
        Class<? extends Annotation> annotationClass = annoAttrs.getClass("annotationClass");
        if (!Annotation.class.equals(annotationClass)) {
            builder.addPropertyValue("annotationClass", annotationClass);
        }

        // attr - markerInterface
        Class<?> markerInterface = annoAttrs.getClass("markerInterface");
        if (!Class.class.equals(markerInterface)) {
            builder.addPropertyValue("markerInterface", markerInterface);
        }

        // attr - dalSessionRef
        String dalSessionRef = annoAttrs.getString("dalSessionRef");
        if (StringUtils.hasText(dalSessionRef)) {
            builder.addPropertyValue("dalSessionRef", annoAttrs.getString("dalSessionRef"));
        }

        // attr - factoryBean
        Class<? extends DalMapperBean> mapperFactoryBeanClass = annoAttrs.getClass("factoryBean");
        if (!DalMapperBean.class.equals(mapperFactoryBeanClass)) {
            builder.addPropertyValue("mapperFactoryBeanClass", mapperFactoryBeanClass);
        }

        // attr - lazyInitialization
        String lazyInitialization = annoAttrs.getString("lazyInitialization");
        if (StringUtils.hasText(lazyInitialization)) {
            builder.addPropertyValue("lazyInitialization", lazyInitialization);
        }

        // attr - defaultScope
        String defaultScope = annoAttrs.getString("defaultScope");
        if (!AbstractBeanDefinition.SCOPE_DEFAULT.equals(defaultScope)) {
            builder.addPropertyValue("defaultScope", defaultScope);
        }

        // attr - defaultScope
        builder.addPropertyValue("dependsOn", dependsOn);

        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }

    private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata, String subType, int index) {
        return importingClassMetadata.getClassName() + "#" + MapperScannerRegistrar.class.getSimpleName() + "#" + subType + "_" + index;
    }

    /** A {@link MapperScannerRegistrar} for {@link MapperScans} */
    static class RepeatingRegistrar extends MapperScannerRegistrar {
        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            AnnotationAttributes mappingScansAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(MappingScans.class.getName()));
            AnnotationAttributes mapperScansAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(MapperScans.class.getName()));
            if (mappingScansAttrs != null && mapperScansAttrs != null) {
                throw new IllegalStateException("@MappingScans and @MapperScans can only use one of them");
            }
            if (mappingScansAttrs == null && mapperScansAttrs == null) {
                return;
            }

            if (mappingScansAttrs != null) {
                AnnotationAttributes[] annotations = mappingScansAttrs.getAnnotationArray("value");
                for (int i = 0; i < annotations.length; i++) {
                    String scanMapperFileBean = generateBaseBeanName(importingClassMetadata, "resource", i);
                    registerMapperFileScannerBean(importingClassMetadata, annotations[i], registry, scanMapperFileBean);
                }
            }

            if (mapperScansAttrs != null) {
                AnnotationAttributes[] annotations = mapperScansAttrs.getAnnotationArray("value");
                for (int i = 0; i < annotations.length; i++) {
                    String scanMapperFileBean = generateBaseBeanName(importingClassMetadata, "resource", i);
                    registerMapperFileScannerBean(importingClassMetadata, annotations[i], registry, scanMapperFileBean);

                    String scanMapperBean = generateBaseBeanName(importingClassMetadata, "mapper", i);
                    registerMapperScannerBean(importingClassMetadata, annotations[i], registry, scanMapperBean, scanMapperFileBean);
                }
            }
        }
    }
}
