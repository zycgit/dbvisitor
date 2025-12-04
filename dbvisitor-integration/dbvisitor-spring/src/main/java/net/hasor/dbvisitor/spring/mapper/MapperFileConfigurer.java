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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.spring.annotation.MapperScan;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import net.hasor.cobble.StringUtils;

/**
 * A resource load for {@link MapperScan}.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-29
 */
public class MapperFileConfigurer extends AbstractConfigurer implements InitializingBean {
    private static final Logger                              logger   = LoggerFactory.getLogger(MapperFileConfigurer.class);
    private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private              Session                             session;
    private              String                              sessionRef;
    private              String                              mapperLocations;
    private              boolean                             processPropertyPlaceHolders;

    public void setSession(Session session) {
        this.session = session;
    }

    public void setSessionRef(String sessionRef) {
        this.sessionRef = sessionRef;
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

        if (this.session == null) {
            if (StringUtils.isNotBlank(this.sessionRef)) {
                logger.info("load MapperFile to Session '" + this.sessionRef + "'");
                this.session = (Session) this.applicationContext.getBean(this.sessionRef);
            } else {
                logger.info("load MapperFile to default Session.");
                this.session = this.applicationContext.getBean(Session.class);
            }
        }

        String[] mapperLocationsArrays = Stream.of(Optional.ofNullable(this.mapperLocations).orElse("").split("[,; \t\n]"))
                .filter(StringUtils::isNotBlank).map(String::trim).toArray(String[]::new);
        Resource[] mapperResources = Stream.of(Optional.ofNullable(mapperLocationsArrays).orElse(new String[0]))//
                .flatMap(location -> Stream.of(getResources(location))).toArray(Resource[]::new);
        for (Resource resource : mapperResources) {
            String string = resource.getURI().toString();
            this.session.getConfiguration().loadMapper(string);
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
            this.sessionRef = getPropertyValue("sessionRef", values);
            this.mapperLocations = getPropertyValue("mapperLocations", values);
        }
        this.sessionRef = Optional.ofNullable(this.sessionRef).map(getEnvironment()::resolvePlaceholders).orElse(null);
        this.mapperLocations = Optional.ofNullable(this.mapperLocations).map(getEnvironment()::resolvePlaceholders).orElse(null);
    }
}
