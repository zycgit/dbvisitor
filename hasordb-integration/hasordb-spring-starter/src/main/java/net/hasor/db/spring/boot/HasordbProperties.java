package net.hasor.db.spring.boot;

import net.hasor.db.spring.support.DalMapperBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.stream.Stream;

import static net.hasor.db.spring.boot.HasordbProperties.HASOR_PREFIX;

@ConfigurationProperties(prefix = HASOR_PREFIX)
public class HasordbProperties {
    public static final  String                  HASOR_PREFIX     = "hasordb";
    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    @NestedConfigurationProperty
    private Configuration                  configuration;
    private String[]                       mapperLocations;
    private String[]                       mapperPackages;
    private Class<? extends DalMapperBean> mapperFactoryBean;
    private String                         mapperScope;
    private String                         mapperLazyInit;

    private Class<? extends Annotation> markerAnnotation;
    private Class<?>                    markerInterface;

    private String[] typeHandlersPackages;
    private String   refSessionBean;

    public Resource[] resolveMapperLocations() {
        return Stream.of(Optional.ofNullable(this.mapperLocations).orElse(new String[0]))//
                .flatMap(location -> Stream.of(getResources(location))).toArray(Resource[]::new);
    }

    private Resource[] getResources(String location) {
        try {
            return resourceResolver.getResources(location);
        } catch (IOException e) {
            return new Resource[0];
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public String[] getMapperLocations() {
        return mapperLocations;
    }

    public void setMapperLocations(String[] mapperLocations) {
        this.mapperLocations = mapperLocations;
    }

    public String[] getMapperPackages() {
        return mapperPackages;
    }

    public void setMapperPackages(String[] mapperPackages) {
        this.mapperPackages = mapperPackages;
    }

    public Class<? extends DalMapperBean> getMapperFactoryBean() {
        return mapperFactoryBean;
    }

    public void setMapperFactoryBean(Class<? extends DalMapperBean> mapperFactoryBean) {
        this.mapperFactoryBean = mapperFactoryBean;
    }

    public String getMapperScope() {
        return mapperScope;
    }

    public void setMapperScope(String mapperScope) {
        this.mapperScope = mapperScope;
    }

    public String getMapperLazyInit() {
        return mapperLazyInit;
    }

    public void setMapperLazyInit(String mapperLazyInit) {
        this.mapperLazyInit = mapperLazyInit;
    }

    public Class<? extends Annotation> getMarkerAnnotation() {
        return markerAnnotation;
    }

    public void setMarkerAnnotation(Class<? extends Annotation> markerAnnotation) {
        this.markerAnnotation = markerAnnotation;
    }

    public Class<?> getMarkerInterface() {
        return markerInterface;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    public String[] getTypeHandlersPackages() {
        return typeHandlersPackages;
    }

    public void setTypeHandlersPackages(String[] typeHandlersPackages) {
        this.typeHandlersPackages = typeHandlersPackages;
    }

    public String getRefSessionBean() {
        return refSessionBean;
    }

    public void setRefSessionBean(String refSessionBean) {
        this.refSessionBean = refSessionBean;
    }
}