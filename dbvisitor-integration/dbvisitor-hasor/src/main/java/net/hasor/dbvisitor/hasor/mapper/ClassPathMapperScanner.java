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
import java.util.HashSet;
import java.util.Set;
import net.hasor.cobble.loader.ClassMatcher;
import net.hasor.cobble.loader.CobbleClassScanner;

/** Finds mapper interfaces from bounded packages. */
public class ClassPathMapperScanner {
    private final ClassLoader                classLoader;
    private       Class<? extends Annotation> annotationClass;
    private       Class<?>                    markerInterface;

    public ClassPathMapperScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Set<Class<?>> scan(String... basePackages) {
        CobbleClassScanner scanner = new CobbleClassScanner(this.classLoader);
        Set<Class<?>> mapperTypes = new HashSet<>();
        this.collect(scanner, basePackages, this.annotationClass, mapperTypes);
        this.collect(scanner, basePackages, this.markerInterface, mapperTypes);
        return mapperTypes;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    private void collect(CobbleClassScanner scanner, String[] basePackages, Class<?> markerType, Set<Class<?>> mapperTypes) {
        if (markerType == null) {
            return;
        }
        Set<Class<?>> matches = scanner.getClassSet(basePackages, context -> this.matches(context, markerType));
        matches.remove(markerType);
        mapperTypes.addAll(matches);
    }

    private boolean matches(ClassMatcher.ClassMatcherContext context, Class<?> markerType) {
        ClassMatcher.ClassInfo classInfo = context.getClassInfo();
        String markerName = markerType.getName();
        if (classInfo.className.equals(markerName) || classInfo.superName.equals(markerName)) {
            return true;
        }
        for (String type : classInfo.castType) {
            if (type.equals(markerName)) {
                return true;
            }
        }
        for (String annotation : classInfo.annos) {
            if (annotation.equals(markerName)) {
                return true;
            }
        }
        return false;
    }
}
