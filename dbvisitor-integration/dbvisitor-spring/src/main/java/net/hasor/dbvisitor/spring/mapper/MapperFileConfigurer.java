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
package net.hasor.dbvisitor.spring.mapper;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.spring.annotation.MapperScan;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/** A resource load for {@link MapperScan}. */
public class MapperFileConfigurer extends AbstractConfigurer implements InitializingBean {
    private static final Logger                              logger   = LoggerFactory.getLogger(MapperFileConfigurer.class);
    private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private              DalSession                          dalSession;
    private              String                              dalSessionRef;
    private              String                              mapperLocations;
    private              boolean                             processPropertyPlaceHolders;

    public void setDalSession(DalSession dalSession) {
        this.dalSession = dalSession;
    }

    public void setDalSessionRef(String dalSessionRef) {
        this.dalSessionRef = dalSessionRef;
    }

    public void setMapperLocations(String mapperLocations) {
        this.mapperLocations = mapperLocations;
    }

    public void setProcessPropertyPlaceHolders(boolean processPropertyPlaceHolders) {
        this.processPropertyPlaceHolders = processPropertyPlaceHolders;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.processPropertyPlaceHolders) {
            processPropertyPlaceHolders();
        }

        if (this.dalSession == null) {
            if (StringUtils.hasText(this.dalSessionRef)) {
                logger.info("load MapperFile to DalSession '" + this.dalSessionRef + "'");
                this.dalSession = (DalSession) this.applicationContext.getBean(this.dalSessionRef);
            } else {
                logger.info("load MapperFile to default DalSession.");
                this.dalSession = this.applicationContext.getBean(DalSession.class);
            }
        }

        String[] mapperLocationsArrays = StringUtils.tokenizeToStringArray(this.mapperLocations, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
        Resource[] mapperResources = Stream.of(Optional.ofNullable(mapperLocationsArrays).orElse(new String[0]))//
                .flatMap(location -> Stream.of(getResources(location))).toArray(Resource[]::new);
        for (Resource resource : mapperResources) {
            try (InputStream ins = resource.getInputStream()) {
                if (ins != null) {
                    logger.info("loadMapper '" + resource + "'");
                    this.dalSession.getDalRegistry().loadMapper(ins);
                }
            }
        }
    }

    private static Resource[] getResources(String location) {
        try {
            return resolver.getResources(location);
        } catch (Exception e) {
            return new Resource[0];
        }
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
            this.dalSessionRef = getPropertyValue("dalSessionRef", values);
            this.mapperLocations = getPropertyValue("mapperLocations", values);
        }
        this.dalSessionRef = Optional.ofNullable(this.dalSessionRef).map(getEnvironment()::resolvePlaceholders).orElse(null);
        this.mapperLocations = Optional.ofNullable(this.mapperLocations).map(getEnvironment()::resolvePlaceholders).orElse(null);
    }
}
