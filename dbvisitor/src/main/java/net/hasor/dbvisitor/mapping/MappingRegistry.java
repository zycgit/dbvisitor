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
import net.hasor.dbvisitor.mapping.def.TableDef;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.resolve.ClassTableMappingResolve;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.dbvisitor.mapping.resolve.XmlTableMappingResolve;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TableMappingResolve 的公共方法
 * @version : 2021-06-21
 * @author 赵永春 (zyc@hasor.net)
 */
public class MappingRegistry {
    private final Map<String, Map<String, TableMapping<?>>> tableMappingMap = new ConcurrentHashMap<>();
    private final ClassLoader                               classLoader;
    private final TypeHandlerRegistry                       typeRegistry;
    private final MappingOptions                            options;
    private final XmlTableMappingResolve                    xmlMappingResolve;
    private final ClassTableMappingResolve                  classMappingResolve;

    public MappingRegistry() {
        this(null, null, null);
    }

    public MappingRegistry(ClassLoader classLoader, TypeHandlerRegistry typeRegistry, MappingOptions options) {
        this.classLoader = (classLoader == null) ? Thread.currentThread().getContextClassLoader() : classLoader;
        this.typeRegistry = (typeRegistry == null) ? TypeHandlerRegistry.DEFAULT : typeRegistry;
        this.options = options;
        this.xmlMappingResolve = new XmlTableMappingResolve(options);
        this.classMappingResolve = new ClassTableMappingResolve(options);
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public TypeHandlerRegistry getTypeRegistry() {
        return this.typeRegistry;
    }

    /** 解析并载入 mapper.xml（支持 MyBatis 大部分能力） */
    public void loadMapper(URL resource) throws IOException {
        try (InputStream stream = resource.openStream()) {
            Objects.requireNonNull(stream, "resource '" + resource + "' is not exist.");
            this.loadMapper(stream);
        }
    }

    /** 解析并载入 mapper.xml（支持 MyBatis 大部分能力） */
    public void loadMapper(String resource) throws IOException {
        if (resource.startsWith("/")) {
            resource = resource.substring(1);
        }
        try (InputStream stream = this.classLoader.getResourceAsStream(resource)) {
            Objects.requireNonNull(stream, "resource '" + resource + "' is not exist.");
            this.loadMapper(stream);
        }
    }

    /** 解析并载入 mapper.xml（支持 MyBatis 大部分能力） */
    public void loadMapper(InputStream stream) throws IOException {
        Objects.requireNonNull(stream, "load InputStream is null.");
        try {
            Document document = loadXmlRoot(stream);
            Element root = document.getDocumentElement();
            NamedNodeMap rootAttributes = root.getAttributes();

            String namespace = readAttribute("namespace", rootAttributes);

            this.loadReader(namespace, root);
        } catch (ParserConfigurationException | SAXException | ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    private void loadReader(String space, Element configRoot) throws IOException, ClassNotFoundException {
        NodeList childNodes = configRoot.getChildNodes();
        for (int i = 0, len = childNodes.getLength(); i < len; i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String elementName = node.getNodeName();
            boolean isResultMap = StringUtils.equalsIgnoreCase("resultMap", elementName);
            boolean isEntity = StringUtils.equalsIgnoreCase("entity", elementName);
            if (!isResultMap && !isEntity) {
                continue;
            }

            NamedNodeMap nodeAttributes = node.getAttributes();
            Node idNode = nodeAttributes.getNamedItem("id");
            Node typeNode = nodeAttributes.getNamedItem("type");
            String identify = (idNode != null) ? idNode.getNodeValue() : null;
            String typeString = (typeNode != null) ? typeNode.getNodeValue() : null;
            if (StringUtils.isBlank(identify)) {
                identify = typeString;
            }

            if (StringUtils.isBlank(typeString)) {
                throw new IOException("the <" + (isResultMap ? "resultMap" : "entity") + "> tag, type is null.");
            }

            TableDef<?> tableDef = this.xmlMappingResolve.resolveTableMapping(node, getClassLoader(), getTypeRegistry());
            configNames(true, isEntity, tableDef);

            if (isEntity) {
                saveMapping(true, "", identify, tableDef);
                if (!StringUtils.equals(identify, typeString)) {
                    saveMapping(true, "", typeString, tableDef);
                }
            } else {
                saveMapping(false, space, identify, tableDef);
            }
        }
    }

    /** 解析 entityType 类型并作为实体载入 */
    public void loadEntity(Class<?> entityType) {
        loadEntity(entityType.getName(), entityType);
    }

    /** 解析 entityType 类型并作为实体载入 */
    public void loadEntity(String identify, Class<?> entityType) {
        Objects.requireNonNull(entityType, "entityType is null.");
        String typeString = entityType.getName();
        if (StringUtils.isBlank(identify)) {
            identify = typeString;
        }

        TableDef<?> tableDef = this.classMappingResolve.resolveTableMapping(entityType, getClassLoader(), getTypeRegistry());
        configNames(false, true, tableDef);

        saveMapping(true, "", identify, tableDef);
        if (!StringUtils.equals(identify, typeString)) {
            saveMapping(true, "", typeString, tableDef);
        }
    }

    /** 解析 entityType 类型并作为实体载入 */
    public void loadResultMap(String space, String identify, Class<?> resultType) {
        Objects.requireNonNull(resultType, "resultType is null.");
        String typeString = resultType.getName();
        if (StringUtils.isBlank(identify)) {
            identify = typeString;
        }

        TableDef<?> tableDef = this.classMappingResolve.resolveTableMapping(resultType, getClassLoader(), getTypeRegistry());
        configNames(false, false, tableDef);

        saveMapping(false, space, identify, tableDef);
    }

    private void configNames(boolean isXml, boolean isEntity, TableDef<?> tableDef) {
        if (!isEntity) {
            tableDef.setCatalog("");
            tableDef.setSchema("");
            tableDef.setTable("");
            return;
        }

        if (isXml) {
            return;
        }

        Class<?> entityType = tableDef.entityType();
        if (StringUtils.isBlank(tableDef.getTable())) {
            if (tableDef.isMapUnderscoreToCamelCase()) {
                tableDef.setTable(StringUtils.humpToLine(entityType.getSimpleName()));
            } else {
                tableDef.setTable(entityType.getSimpleName());
            }
        }
    }

    private void saveMapping(boolean isEntity, String space, String identify, TableMapping<?> tableMapping) {
        space = StringUtils.isBlank(space) ? "" : space;

        if (!this.tableMappingMap.containsKey(space)) {
            this.tableMappingMap.put(space, new ConcurrentHashMap<>());
        }

        Map<String, TableMapping<?>> mappingMap = this.tableMappingMap.get(space);
        if (mappingMap.containsKey(identify)) {
            String msg = isEntity ? "repeat entity" : "repeat resultMap";
            throw new IllegalStateException(msg + " '" + identify + "' in " + (StringUtils.isBlank(space) ? "default namespace" : ("'" + space + "' namespace.")));
        }

        if (isEntity && StringUtils.isBlank(tableMapping.getTable())) {
            throw new IllegalStateException("entity '" + identify + "' table is not specified in " + (StringUtils.isBlank(space) ? "default namespace" : ("'" + space + "' namespace.")));
        }

        mappingMap.put(identify, tableMapping);
    }

    public <T> TableMapping<T> findEntity(Class<?> entityType) {
        return findMapping("", entityType.getName());
    }

    public <T> TableMapping<T> findEntity(String idOrType) {
        return findMapping("", idOrType);
    }

    public <T> TableMapping<T> findMapping(String space, String identify) {
        TableMapping<?> tableMapping = null;

        if (StringUtils.isNotBlank(space) && this.tableMappingMap.containsKey(space)) {
            Map<String, TableMapping<?>> mappingMap = this.tableMappingMap.get(space);
            tableMapping = mappingMap.get(identify);
        }

        if (tableMapping == null) {
            Map<String, TableMapping<?>> mappingMap = this.tableMappingMap.get("");
            tableMapping = mappingMap.get(identify);
        }

        return (TableMapping<T>) tableMapping;
    }

    // --------------------------------------------------------------------------------------------
    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();

    protected Document loadXmlRoot(InputStream stream) throws ParserConfigurationException, IOException, SAXException {
        if (stream == null) {
            throw new NullPointerException("stream is null.");
        }
        DocumentBuilder documentBuilder = FACTORY.newDocumentBuilder();
        documentBuilder.setEntityResolver((publicId, systemId) -> {
            boolean mybatisDTD = StringUtils.equalsIgnoreCase("-//mybatis.org//DTD Mapper 3.0//EN", publicId) || StringUtils.containsIgnoreCase(systemId, "mybatis-3-mapper.dtd");
            boolean dbVisitorDTD = StringUtils.equalsIgnoreCase("-//dbvisitor.net//DTD Mapper 1.0//EN", publicId) || StringUtils.containsIgnoreCase(systemId, "dbvisitor-mapper.dtd");
            if (dbVisitorDTD) {
                InputSource source = new InputSource(getClassLoader().getResourceAsStream("net/hasor/dbvisitor/dal/repository/parser/dbvisitor-mapper.dtd"));
                source.setPublicId(publicId);
                source.setSystemId(systemId);
                return source;
            } else if (mybatisDTD) {
                InputSource source = new InputSource(getClassLoader().getResourceAsStream("net/hasor/dbvisitor/dal/repository/parser/mybatis-3-mapper.dtd"));
                source.setPublicId(publicId);
                source.setSystemId(systemId);
                return source;
            } else {
                return new DefaultHandler().resolveEntity(publicId, systemId);
            }
        });
        return documentBuilder.parse(new InputSource(stream));
    }

    private String readAttribute(String attrName, NamedNodeMap rootAttributes) {
        if (rootAttributes != null) {
            Node namespaceNode = rootAttributes.getNamedItem(attrName);
            if (namespaceNode != null) {
                return namespaceNode.getNodeValue();
            }
        }
        return null;
    }
}
