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
package net.hasor.dbvisitor.hasor.mapper;

import java.lang.annotation.Annotation;
import java.util.Set;
import net.hasor.cobble.StringUtils;
import net.hasor.core.ApiBinder;
import net.hasor.core.BindInfo;
import net.hasor.core.HasorUtils;
import net.hasor.dbvisitor.session.Session;

/** Configures mapper scanning and registers mapper proxies in Hasor. */
public class MapperScannerConfigurer implements net.hasor.core.Module {
    private String                      sourceName;
    private String                      basePackage;
    private Boolean                     mapperDisabled;
    private Class<? extends Annotation> annotationClass;
    private Class<?>                    markerInterface;

    public MapperScannerConfigurer() {
    }

    @Override
    public void loadModule(ApiBinder apiBinder) throws Exception {
        BindInfo<Session> session = apiBinder.findBindingRegister(this.bindingName(), Session.class);
        if (session == null) {
            throw new IllegalStateException("dbVisitor Session '" + this.bindingName() + "' is not registered.");
        }

        this.applySettings(apiBinder);
        if (Boolean.TRUE.equals(this.mapperDisabled) || StringUtils.isBlank(this.basePackage)) {
            return;
        }

        ClassPathMapperScanner scanner = new ClassPathMapperScanner(apiBinder.getClassLoader());
        scanner.setAnnotationClass(this.annotationClass);
        scanner.setMarkerInterface(this.markerInterface);
        Set<Class<?>> mapperTypes = scanner.scan(this.basePackage.split(","));
        for (Class<?> mapperType : mapperTypes) {
            this.registerMapper(apiBinder, session, mapperType);
        }
    }

    private void applySettings(ApiBinder apiBinder) throws ClassNotFoundException {
        MapperSettings settings = new MapperSettings(apiBinder.getSettings(), this.sourceName);
        if (this.mapperDisabled == null) {
            this.mapperDisabled = Boolean.parseBoolean(settings.getMapperDisabled());
        }
        if (StringUtils.isBlank(this.basePackage)) {
            this.basePackage = settings.getMapperPackages();
        }
        if (this.annotationClass == null) {
            String annotationName = settings.getMarkerAnnotation();
            if (StringUtils.isNotBlank(annotationName)) {
                this.annotationClass = apiBinder.getClassLoader().loadClass(annotationName).asSubclass(Annotation.class);
            }
        }
        if (this.markerInterface == null) {
            String interfaceName = settings.getMarkerInterface();
            if (StringUtils.isNotBlank(interfaceName)) {
                this.markerInterface = apiBinder.getClassLoader().loadClass(interfaceName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void registerMapper(ApiBinder apiBinder, BindInfo<Session> session, Class<?> mapperType) {
        MapperSupplier supplier = new MapperSupplier(mapperType, session);
        HasorUtils.pushStartListener(apiBinder.getEventContext(), supplier);
        if (StringUtils.isBlank(this.sourceName)) {
            apiBinder.bindType((Class<Object>) mapperType).toProvider(supplier).asEagerSingleton();
        } else {
            apiBinder.bindType((Class<Object>) mapperType).nameWith(this.sourceName).toProvider(supplier).asEagerSingleton();
        }
    }

    private String bindingName() {
        return this.sourceName == null ? "" : this.sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
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
}
