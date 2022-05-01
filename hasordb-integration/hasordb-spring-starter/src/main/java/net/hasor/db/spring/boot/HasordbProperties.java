package net.hasor.db.spring.boot;
import net.hasor.db.spring.support.DalMapperBean;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Annotation;

@ConfigurationProperties(prefix = "hasordb")
public class HasordbProperties {
    @NestedConfigurationProperty
    private Configuration configuration;
    private String[]      mapperPackages;
    private String[]      mapperLocations;

    private Class<? extends BeanNameGenerator> mapperNameGenerator;
    private Class<? extends DalMapperBean>     mapperFactoryBean;
    private String                             mapperLazyInit;
    private String                             mapperScope;

    private Class<? extends Annotation> markerAnnotation;
    private Class<?>                    markerInterface;
    private String                      refSessionBean;

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public String[] getMapperPackages() {
        return mapperPackages;
    }

    public void setMapperPackages(String[] mapperPackages) {
        this.mapperPackages = mapperPackages;
    }

    public String[] getMapperLocations() {
        return mapperLocations;
    }

    public void setMapperLocations(String[] mapperLocations) {
        this.mapperLocations = mapperLocations;
    }

    public Class<? extends BeanNameGenerator> getMapperNameGenerator() {
        return mapperNameGenerator;
    }

    public void setMapperNameGenerator(Class<? extends BeanNameGenerator> mapperNameGenerator) {
        this.mapperNameGenerator = mapperNameGenerator;
    }

    public Class<? extends DalMapperBean> getMapperFactoryBean() {
        return mapperFactoryBean;
    }

    public void setMapperFactoryBean(Class<? extends DalMapperBean> mapperFactoryBean) {
        this.mapperFactoryBean = mapperFactoryBean;
    }

    public String getMapperLazyInit() {
        return mapperLazyInit;
    }

    public void setMapperLazyInit(String mapperLazyInit) {
        this.mapperLazyInit = mapperLazyInit;
    }

    public String getMapperScope() {
        return mapperScope;
    }

    public void setMapperScope(String mapperScope) {
        this.mapperScope = mapperScope;
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

    public String getRefSessionBean() {
        return refSessionBean;
    }

    public void setRefSessionBean(String refSessionBean) {
        this.refSessionBean = refSessionBean;
    }

}