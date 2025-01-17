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
import net.hasor.cobble.reflect.Annotation;
import net.hasor.cobble.reflect.Annotations;
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

/**
 * TableMappingResolve 的公共方法
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-21
 */
public class MappingHelper {

    public static boolean caseInsensitive(MappingOptions global) {
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

    //    public static TableInfo findTableInfo(Class<?> tableType, MappingOptions usingOpt) {
    //        return null;
    //    }

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

    //    public static class TableInfo {
    //        private final String catalog;
    //        private final String schema;
    //        private final String table;
    //
    //        public TableInfo(String catalog, String schema, String table) {
    //            this.catalog = catalog;
    //            this.schema = schema;
    //            this.table = table;
    //        }
    //
    //        public String getCatalog() {
    //            return this.catalog;
    //        }
    //
    //        public String getSchema() {
    //            return this.schema;
    //        }
    //
    //        public String getTable() {
    //            return this.table;
    //        }
    //    }
}
