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
import net.hasor.dbvisitor.spring.support.DalMapperBean;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.annotation.Annotation;

import static net.hasor.dbvisitor.spring.boot.DbVisitorProperties.PREFIX;

@ConfigurationProperties(prefix = PREFIX)
public class DbVisitorProperties {
    public final static String                             PREFIX = "dbvisitor";
    private             String[]                           mapperPackages;
    private             String[]                           mapperLocations;
    private             Boolean                            mapperDisabled;
    private             Class<? extends BeanNameGenerator> mapperNameGenerator;
    private             Class<? extends DalMapperBean>     mapperFactoryBean;
    private             Boolean                            mapperLazyInit;
    private             String                             mapperScope;
    private             Class<? extends Annotation>        markerAnnotation;
    private             Class<?>                           markerInterface;
    private             String                             refSessionBean;
    // opt
    private             Boolean                            autoMapping;
    private             Boolean                            camelCase;
    private             Boolean                            caseInsensitive;
    private             Boolean                            useDelimited;
    private             String                             dialect;

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

    public Boolean getMapperDisabled() {
        return this.mapperDisabled;
    }

    public void setMapperDisabled(Boolean mapperDisabled) {
        this.mapperDisabled = mapperDisabled;
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

    public Boolean getMapperLazyInit() {
        return mapperLazyInit;
    }

    public void setMapperLazyInit(Boolean mapperLazyInit) {
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

    public Boolean getAutoMapping() {
        return autoMapping;
    }

    public void setAutoMapping(Boolean autoMapping) {
        this.autoMapping = autoMapping;
    }

    public Boolean getCamelCase() {
        return camelCase;
    }

    public void setCamelCase(Boolean camelCase) {
        this.camelCase = camelCase;
    }

    public Boolean getCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(Boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public Boolean getUseDelimited() {
        return useDelimited;
    }

    public void setUseDelimited(Boolean useDelimited) {
        this.useDelimited = useDelimited;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }
}