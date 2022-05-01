package net.hasor.db.spring.boot;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.db.dal.repository.DalMapper;
import net.hasor.db.dal.session.DalSession;
import net.hasor.db.spring.mapper.MapperScannerConfigurer;
import net.hasor.db.spring.support.DalMapperBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.List;

@Configuration
@Import({ MapperScannerRegistrarNotFoundConfiguration.AutoConfiguredMapperScannerRegistrar.class })
@ConditionalOnMissingBean({ DalSession.class, MapperScannerConfigurer.class })
public class MapperScannerRegistrarNotFoundConfiguration implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(MapperScannerRegistrarNotFoundConfiguration.class);

    public void afterPropertiesSet() {
        logger.debug("Not found configuration for registering mapper bean using @MapperScan, DalMapperBean and MapperScannerConfigurer.");
    }

    public static class AutoConfiguredMapperScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar {
        private BeanFactory       beanFactory;
        private HasordbProperties hasordbProperties;

        public AutoConfiguredMapperScannerRegistrar(HasordbProperties properties) {
            this.hasordbProperties = properties;
        }

        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        private String getBeanNameForType(Class<?> type, ListableBeanFactory factory) {
            String[] beanNames = factory.getBeanNamesForType(type);
            return (beanNames.length > 0) ? beanNames[0] : null;
        }

        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            if (!AutoConfigurationPackages.has(this.beanFactory)) {
                logger.debug("Could not determine auto-configuration package, automatic mapper scanning disabled.");
                return;
            }
            logger.debug("Searching for mappers annotated with @Mapper");

            List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
            if (logger.isDebugEnabled()) {
                packages.forEach(pkg -> logger.debug("Using auto-configuration base package '" + pkg + "'"));
            }

            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
            builder.addPropertyValue("processPropertyPlaceHolders", true);
            builder.addPropertyValue("basePackage", "${hasordb.mapper-packages:" + StringUtils.collectionToCommaDelimitedString(packages) + "}");
            builder.addPropertyValue("mapperLocations", "${hasordb.mapper-locations:}");
            builder.addPropertyValue("mapperFactoryBeanClass", "${hasordb.mapper-factory-bean:" + DalMapperBean.class.getName() + "}");
            builder.addPropertyValue("defaultScope", "${hasordb.mapper-scope:}");
            builder.addPropertyValue("lazyInitialization", "${hasordb.mapper-lazy-initialization:false}");
            builder.addPropertyValue("nameGenerator", "${hasordb.mapper-name-generator:}");
            builder.addPropertyValue("annotationClass", "${hasordb.marker-annotation:" + DalMapper.class.getName() + "}");
            builder.addPropertyValue("markerInterface", "${hasordb.marker-interface:}");
            builder.addPropertyValue("dalSessionRef", "${hasordb.ref-session-bean:}");

            registry.registerBeanDefinition(MapperScannerConfigurer.class.getName(), builder.getBeanDefinition());
        }

    }
}
