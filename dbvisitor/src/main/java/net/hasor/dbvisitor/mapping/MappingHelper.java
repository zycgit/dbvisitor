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
package net.hasor.dbvisitor.mapping;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.EFunction;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.cobble.reflect.Annotation;
import net.hasor.cobble.reflect.Annotations;
import net.hasor.cobble.reflect.resolvable.ResolvableType;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TableMappingResolve 的公共方法
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-21
 */
public class MappingHelper {

    private static final Map<String, Class<?>> typeMap        = new HashMap<>();
    private static final Map<Class<?>, String> typeMapReverse = new HashMap<>();

    private static void putTypeMap(String name, Class<?> type) {
        typeMap.put(name.toLowerCase(), type);
        typeMapReverse.put(type, name.toLowerCase());
    }

    static {
        putTypeMap("boolean", boolean.class);
        putTypeMap("bool", boolean.class);
        putTypeMap("bytes", byte[].class);
        putTypeMap("byte", byte.class);
        putTypeMap("short", short.class);
        putTypeMap("int", int.class);
        putTypeMap("long", long.class);
        putTypeMap("float", float.class);
        putTypeMap("double", double.class);
        putTypeMap("bigint", BigInteger.class);
        putTypeMap("decimal", BigDecimal.class);
        putTypeMap("number", Number.class);
        putTypeMap("char", char.class);
        putTypeMap("string", String.class);
        putTypeMap("url", URL.class);
        putTypeMap("uri", URI.class);
        putTypeMap("void", void.class);
        putTypeMap("map", Map.class);
        putTypeMap("hashmap", HashMap.class);
        putTypeMap("linkedmap", LinkedHashMap.class);
        putTypeMap("caseinsensitivemap", LinkedCaseInsensitiveMap.class);
        putTypeMap("date", java.util.Date.class);
        putTypeMap("sqldate", java.sql.Date.class);
        putTypeMap("sqltime", java.sql.Time.class);
        putTypeMap("sqltimestamp", java.sql.Timestamp.class);
        putTypeMap("offsetdatetime", java.time.OffsetDateTime.class);
        putTypeMap("offsettime", java.time.OffsetTime.class);
        putTypeMap("localdate", java.time.LocalDate.class);
        putTypeMap("localtime", java.time.LocalTime.class);
        putTypeMap("localdatetime", java.time.LocalDateTime.class);
        putTypeMap("monthday", java.time.MonthDay.class);
        putTypeMap("month", java.time.Month.class);
        putTypeMap("yearmonth", java.time.YearMonth.class);
        putTypeMap("year", java.time.Year.class);
    }

    public static String typeName(Class<?> type) {
        if (type == null) {
            return null;
        }
        if (typeMapReverse.containsKey(type)) {
            return typeMapReverse.get(type);
        } else {
            return type.getName();
        }
    }

    public static Class<?> typeMappingOr(String typeName, EFunction<String, Class<?>, ClassNotFoundException> defaultType) throws ClassNotFoundException {
        if (typeName == null) {
            return null;
        }
        String testName = typeName.toLowerCase();
        if (typeMap.containsKey(testName)) {
            return typeMap.get(testName);
        } else {
            return defaultType.eApply(typeName);
        }
    }

    public static boolean caseInsensitive(Options global) {
        Boolean caseInsensitive = global == null ? null : global.getCaseInsensitive();
        return caseInsensitive == null || caseInsensitive;
    }

    public static NameInfo findNameInfo(Method method) throws IOException {
        Annotations classAnno = Annotations.ofClass(method.getDeclaringClass());
        Annotations methodAnno = classAnno.getMethod(Annotations.toMethodName(method.getName(), method.getParameterTypes()));
        Annotation annotation = methodAnno.getAnnotation(ResultMap.class);
        return findNameInfo(annotation, null);
    }

    public static NameInfo findNameInfo(Class<?> resultType) throws IOException {
        Annotations classAnno = Annotations.ofClass(resultType);
        Annotation annotation = classAnno.getAnnotation(ResultMap.class);
        return findNameInfo(annotation, resultType.getName());
    }

    private static NameInfo findNameInfo(Annotation annotation, String defaultName) {
        if (annotation != null) {
            String space = annotation.getString("space", "");
            String name = annotation.getString("id", "");
            if (StringUtils.isBlank(name)) {
                name = annotation.getString("value", defaultName);
            }
            return new NameInfo(space, name);
        } else if (StringUtils.isNotBlank(defaultName)) {
            return new NameInfo("", defaultName);
        } else {
            return null;
        }
    }

    public static Class<?> resolveReturnType(Method dalMethod) {
        // resolve required Type
        Class<?> requiredClass = dalMethod.getReturnType();
        if (Collection.class.isAssignableFrom(requiredClass)) {
            Type requiredType = dalMethod.getGenericReturnType();
            ResolvableType type = ResolvableType.forType(requiredType);
            requiredClass = type.getGeneric(0).resolve();
        }

        if (requiredClass != Object.class && requiredClass != Void.class && requiredClass != void.class) {
            return requiredClass;
        } else {
            return null;
        }
    }

    // --------------------------------------------------------------------------------------------
    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();

    public static Document loadXmlRoot(InputStream stream, ClassLoader loader) throws ParserConfigurationException, IOException, SAXException {
        if (stream == null) {
            throw new NullPointerException("stream is null.");
        }
        DocumentBuilder documentBuilder = FACTORY.newDocumentBuilder();
        documentBuilder.setEntityResolver((publicId, systemId) -> {
            boolean mybatisDTD = StringUtils.equalsIgnoreCase("-//mybatis.org//DTD Mapper 3.0//EN", publicId) || StringUtils.containsIgnoreCase(systemId, "mybatis-3-mapper.dtd");
            boolean dbVisitorDTD = StringUtils.equalsIgnoreCase("-//dbvisitor.net//DTD Mapper 1.0//EN", publicId) || StringUtils.containsIgnoreCase(systemId, "dbvisitor-mapper.dtd");
            if (dbVisitorDTD) {
                InputSource source = new InputSource(loader.getResourceAsStream("net/hasor/dbvisitor/dal/repository/parser/dbvisitor-mapper.dtd"));
                source.setPublicId(publicId);
                source.setSystemId(systemId);
                return source;
            } else if (mybatisDTD) {
                InputSource source = new InputSource(loader.getResourceAsStream("net/hasor/dbvisitor/dal/repository/parser/mybatis-3-mapper.dtd"));
                source.setPublicId(publicId);
                source.setSystemId(systemId);
                return source;
            } else {
                return new DefaultHandler().resolveEntity(publicId, systemId);
            }
        });
        return documentBuilder.parse(new InputSource(stream));
    }

    public static String readAttribute(String key, NamedNodeMap nodeAttributes) {
        if (nodeAttributes != null) {
            Node node = nodeAttributes.getNamedItem(key);
            return (node != null) ? node.getNodeValue() : null;
        }
        return null;
    }

    public static class NameInfo {
        private final String space;
        private final String name;

        public NameInfo(String space, String name) {
            this.space = space;
            this.name = name;
        }

        public String getSpace() {
            return this.space;
        }

        public String getName() {
            return this.name;
        }
    }
}
