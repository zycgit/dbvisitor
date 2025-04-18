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
package net.hasor.dbvisitor.spring.annotation;
import net.hasor.dbvisitor.spring.mapper.ClassPathMapperScanner;
import net.hasor.dbvisitor.spring.mapper.MapperFileConfigurer;
import net.hasor.dbvisitor.spring.mapper.MapperScannerConfigurer;
import net.hasor.dbvisitor.spring.support.MapperBean;
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
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-29
 * @see ClassPathMapperScanner
 * @since 1.2.0
 */
public class ScannerRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes mapperScanAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(MapperScan.class.getName()));
        if (mapperScanAttrs == null) {
            return;
        }

        boolean mapperDisabled = mapperScanAttrs.getBoolean("mapperDisabled");

        // add MapperFileConfigurer bean
        String scanMapperFileBean = generateBaseBeanName(importingClassMetadata, "resource", 0);
        registerMapperFileScannerBean(importingClassMetadata, mapperScanAttrs, registry, scanMapperFileBean);

        // add MapperScannerConfigurer bean
        if (!mapperDisabled) {
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

        String sessionRef = annoAttrs.getString("sessionRef");
        if (StringUtils.hasText(sessionRef)) {
            builder.addPropertyValue("session", new RuntimeBeanReference(sessionRef));
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

        // attr - sessionRef
        String configurationRef = annoAttrs.getString("sessionRef");
        if (StringUtils.hasText(configurationRef)) {
            builder.addPropertyValue("sessionRef", annoAttrs.getString("sessionRef"));
        }

        // attr - factoryBean
        Class<? extends MapperBean> mapperFactoryBeanClass = annoAttrs.getClass("factoryBean");
        if (!MapperBean.class.equals(mapperFactoryBeanClass)) {
            builder.addPropertyValue("mapperFactoryBeanClass", mapperFactoryBeanClass);
        }

        // attr - lazyInit
        String lazyInit = annoAttrs.getString("lazyInit");
        if (StringUtils.hasText(lazyInit)) {
            builder.addPropertyValue("lazyInit", lazyInit);
        }

        // attr - defaultScope
        String defaultScope = annoAttrs.getString("defaultScope");
        if (!AbstractBeanDefinition.SCOPE_DEFAULT.equals(defaultScope)) {
            builder.addPropertyValue("defaultScope", defaultScope);
        }

        // attr - mapperDisabled
        String mapperDisabled = annoAttrs.getString("mapperDisabled");
        if (!AbstractBeanDefinition.SCOPE_DEFAULT.equals(mapperDisabled)) {
            builder.addPropertyValue("mapperDisabled", mapperDisabled);
        }

        builder.addPropertyValue("dependsOn", dependsOn);
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }

    private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata, String subType, int index) {
        return importingClassMetadata.getClassName() + "#" + ScannerRegistrar.class.getSimpleName() + "#" + subType + "_" + index;
    }

    /** A {@link ScannerRegistrar} for {@link MapperScans} */
    static class RepeatingRegistrar extends ScannerRegistrar {
        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            AnnotationAttributes mapperScansAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(MapperScans.class.getName()));
            if (mapperScansAttrs == null) {
                return;
            }

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
