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
package net.hasor.dbvisitor.mapping.resolve;
import net.hasor.cobble.*;
import net.hasor.cobble.convert.ConverterUtils;
import net.hasor.cobble.function.Property;
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.keyholder.CreateContext;
import net.hasor.dbvisitor.keyholder.KeySeq;
import net.hasor.dbvisitor.keyholder.KeySeqHolder;
import net.hasor.dbvisitor.keyholder.KeySeqHolderFactory;
import net.hasor.dbvisitor.mapping.KeyTypeEnum;
import net.hasor.dbvisitor.mapping.def.*;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通过 Xml 来解析 TableMapping
 * @version : 2021-06-23
 * @author 赵永春 (zyc@hasor.net)
 */
public class XmlTableMappingResolve extends AbstractTableMappingResolve<Node> {
    private static final Logger                   logger = Logger.getLogger(XmlTableMappingResolve.class);
    private final        ClassTableMappingResolve classTableMappingResolve;

    public XmlTableMappingResolve(MappingOptions global) {
        super(global);
        this.classTableMappingResolve = new ClassTableMappingResolve(this.global);
    }

    protected boolean hasAnyMapping(NodeList childNodes) {
        for (int i = 0, len = childNodes.getLength(); i < len; i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String elementName = node.getNodeName().toLowerCase().trim();
            if ("id".equalsIgnoreCase(elementName) || "result".equalsIgnoreCase(elementName) || "mapping".equalsIgnoreCase(elementName)) {
                return true;
            }
        }
        return false;
    }

    private static String strFromXmlAttribute(NamedNodeMap nodeAttributes, String key) {
        Node node = nodeAttributes.getNamedItem(key);
        return (node != null) ? node.getNodeValue() : null;
    }

    @Override
    public TableDef<?> resolveTableMapping(Node refData, MappingOptions refFile, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) throws ReflectiveOperationException {
        NodeList childNodes = refData.getChildNodes();
        NamedNodeMap nodeAttributes = refData.getAttributes();
        // overwrite data
        Class<?> entityType = classLoader.loadClass(strFromXmlAttribute(nodeAttributes, "type"));
        Map<String, String> overwriteData = new HashMap<>();
        overwriteData.compute("catalog", (key, oldValue) -> strFromXmlAttribute(nodeAttributes, "catalog"));
        overwriteData.compute("schema", (key, oldValue) -> strFromXmlAttribute(nodeAttributes, "schema"));
        overwriteData.compute("table", (key, oldValue) -> strFromXmlAttribute(nodeAttributes, "table"));
        overwriteData.compute("autoMapping", (key, oldValue) -> strFromXmlAttribute(nodeAttributes, "autoMapping"));
        overwriteData.compute("useDelimited", (key, oldValue) -> strFromXmlAttribute(nodeAttributes, "useDelimited"));
        overwriteData.compute("mapUnderscoreToCamelCase", (key, oldValue) -> strFromXmlAttribute(nodeAttributes, "mapUnderscoreToCamelCase"));
        overwriteData.compute("caseInsensitive", (key, oldValue) -> strFromXmlAttribute(nodeAttributes, "caseInsensitive"));
        overwriteData.compute("character-set", (key, oldValue) -> strFromXmlAttribute(nodeAttributes, "character-set"));
        overwriteData.compute("collation", (key, oldValue) -> strFromXmlAttribute(nodeAttributes, "collation"));
        overwriteData.compute("comment", (key, oldValue) -> strFromXmlAttribute(nodeAttributes, "comment"));
        overwriteData.compute("other", (key, oldValue) -> strFromXmlAttribute(nodeAttributes, "other"));
        overwriteData.compute("ddlAuto", (key, oldValue) -> strFromXmlAttribute(nodeAttributes, "ddlAuto"));

        MappingOptions use = refFile == null ? this.global : refFile;
        TableDefaultInfo tableInfo = fetchDefaultInfoByEntity(classLoader, entityType, false, use, overwriteData);

        // passer tableDef
        TableDef<?> tableDef;
        if (hasAnyMapping(childNodes)) {
            // xmlNode 含有配置属性映射，仅解析 Table
            tableDef = this.classTableMappingResolve.resolveTable(tableInfo, entityType);
            loadTableMapping(tableDef, refData, classLoader, typeRegistry);
        } else {
            // xmlNode 没有配置属性映射，完整解析 Table + Column
            tableDef = this.classTableMappingResolve.resolveTableAndColumn(tableInfo, entityType, typeRegistry);
        }

        // passer index
        boolean hasAnyIndex = false;
        for (int i = 0, len = childNodes.getLength(); i < len; i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (!hasAnyIndex) {
                tableDef.getIndexes().clear();// xml overwrite anno
            }
            hasAnyIndex = true;
            String elementName = node.getNodeName().toLowerCase().trim();
            if ("index".equalsIgnoreCase(elementName)) {
                loadTableIndex(tableDef, node);
            }
        }
        return tableDef;
    }

    private void loadTableMapping(TableDef<?> tableDef, Node refData, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) throws ReflectiveOperationException {
        Map<String, Property> propertyMap = BeanUtils.getPropertyFunc(tableDef.entityType());

        NodeList childNodes = refData.getChildNodes();
        for (int i = 0, len = childNodes.getLength(); i < len; i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String elementName = node.getNodeName().toLowerCase().trim();
            if (StringUtils.isBlank(elementName)) {
                throw new UnsupportedOperationException("tag name is Empty.");
            }

            ColumnMapping columnMapping = null;
            if ("id".equalsIgnoreCase(elementName)) {
                columnMapping = this.resolveProperty(tableDef, true, node, propertyMap, classLoader, typeRegistry);
            } else if ("result".equalsIgnoreCase(elementName) || "mapping".equalsIgnoreCase(elementName)) {
                columnMapping = this.resolveProperty(tableDef, false, node, propertyMap, classLoader, typeRegistry);
            } else if ("index".equalsIgnoreCase(elementName)) {
                continue; // ignore
            } else {
                throw new UnsupportedOperationException("tag <" + elementName + "> Unsupported.");
            }

            tableDef.addMapping(columnMapping);
        }
    }

    private void loadTableIndex(TableDef<?> tableDef, Node refData) {
        NamedNodeMap nodeAttributes = refData.getAttributes();
        String idxName = strFromXmlAttribute(nodeAttributes, "name");
        String columns = strFromXmlAttribute(nodeAttributes, "columns");
        String idxUnique = strFromXmlAttribute(nodeAttributes, "unique");
        String idxComment = strFromXmlAttribute(nodeAttributes, "comment");
        String idxOther = strFromXmlAttribute(nodeAttributes, "other");

        if (StringUtils.isBlank(idxName)) {
            throw new IllegalArgumentException("entityType " + tableDef.getTable() + " missing index name.");
        }

        List<String> columnList = null;
        if (StringUtils.isNotBlank(columns)) {
            columnList = Arrays.stream(columns.split(",")).filter(StringUtils::isNotBlank).map(String::trim).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(columnList)) {
            columnList = new ArrayList<>();
            NodeList columnNodes = refData.getChildNodes();
            for (int i = 0, len = columnNodes.getLength(); i < len; i++) {
                Node colNode = columnNodes.item(i);
                if (colNode.getNodeType() != Node.ELEMENT_NODE || !StringUtils.equalsIgnoreCase(colNode.getNodeName(), "column")) {
                    continue;
                }
                String columnName = colNode.getTextContent().trim();
                if (StringUtils.isNotBlank(columnName)) {
                    columnList.add(columnName);
                }
            }
            if (CollectionUtils.isEmpty(columnList)) {
                throw new IllegalArgumentException("entityType " + tableDef.getTable() + " columns is empty.");
            }
        }

        IndexDef idxDef = new IndexDef();
        idxDef.setName(idxName);
        idxDef.setColumns(columnList);
        idxDef.setUnique((Boolean) ConverterUtils.convert(idxUnique, Boolean.TYPE));
        idxDef.setComment(StringUtils.isBlank(idxComment) ? null : idxComment);
        idxDef.setOther(StringUtils.isBlank(idxOther) ? null : idxOther);
        tableDef.addIndexDescription(idxDef);
    }

    private ColumnMapping resolveProperty(TableDef<?> tableDef, boolean asPrimaryKey, Node xmlNode, Map<String, Property> propertyMap, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) throws ReflectiveOperationException {
        NamedNodeMap nodeAttributes = xmlNode.getAttributes();
        String column = strFromXmlAttribute(nodeAttributes, "column");
        String property = strFromXmlAttribute(nodeAttributes, "property");
        String javaType = strFromXmlAttribute(nodeAttributes, "javaType");
        String jdbcType = strFromXmlAttribute(nodeAttributes, "jdbcType");
        String typeHandler = strFromXmlAttribute(nodeAttributes, "typeHandler");
        String keyType = strFromXmlAttribute(nodeAttributes, "keyType");
        String insertStr = strFromXmlAttribute(nodeAttributes, "insert");
        String updateStr = strFromXmlAttribute(nodeAttributes, "update");
        String selectTemplate = strFromXmlAttribute(nodeAttributes, "selectTemplate");
        String insertTemplate = strFromXmlAttribute(nodeAttributes, "insertTemplate");
        String setColTemplate = strFromXmlAttribute(nodeAttributes, "setColTemplate");
        String setValueTemplate = strFromXmlAttribute(nodeAttributes, "setValueTemplate");
        String whereColTemplate = strFromXmlAttribute(nodeAttributes, "whereColTemplate");
        String whereValueTemplate = strFromXmlAttribute(nodeAttributes, "whereValueTemplate");
        String sqlType = strFromXmlAttribute(nodeAttributes, "sqlType");
        String length = strFromXmlAttribute(nodeAttributes, "length");
        String precision = strFromXmlAttribute(nodeAttributes, "precision");
        String scale = strFromXmlAttribute(nodeAttributes, "scale");
        String characterSet = strFromXmlAttribute(nodeAttributes, "character-set");
        String collation = strFromXmlAttribute(nodeAttributes, "collation");
        String nullableStr = strFromXmlAttribute(nodeAttributes, "nullable");
        String defaultValue = strFromXmlAttribute(nodeAttributes, "default");
        String comment = strFromXmlAttribute(nodeAttributes, "comment");
        String other = strFromXmlAttribute(nodeAttributes, "other");

        if (!propertyMap.containsKey(property)) {
            throw new NoSuchFieldException("property '" + property + "' undefined. location= " + logMessage(xmlNode));
        }

        Property propertyHandler = propertyMap.get(property);
        Class<?> columnJavaType = resolveJavaType(xmlNode, javaType, propertyHandler, classLoader);
        Integer columnJdbcType = resolveJdbcType(jdbcType, columnJavaType, typeRegistry);
        TypeHandler<?> columnTypeHandler = resolveTypeHandler(columnJavaType, columnJdbcType, classLoader, typeHandler, typeRegistry);
        boolean insert = StringUtils.isBlank(insertStr) || Boolean.parseBoolean(insertStr);
        boolean update = StringUtils.isBlank(updateStr) || Boolean.parseBoolean(updateStr);
        boolean nullable = StringUtils.isBlank(nullableStr) || Boolean.parseBoolean(updateStr);

        ColumnDef colDef = new ColumnDef(column, property, columnJdbcType, columnJavaType, columnTypeHandler, propertyHandler, insert, update, asPrimaryKey,//
                selectTemplate, insertTemplate, setColTemplate, setValueTemplate, whereColTemplate, whereValueTemplate);

        if (sqlType == null && length == null && precision == null && scale == null && characterSet == null && collation == null && defaultValue == null && comment == null && other == null) {
            colDef.setDescription(null);
        } else {
            nullable = !asPrimaryKey && nullable;
            colDef.setDescription(new ColumnDescDef(sqlType, length, precision, scale, characterSet, collation, nullable, defaultValue, comment, other));
        }

        // init KeySeqHolder
        colDef.setKeySeqHolder(resolveKeyType(tableDef, colDef, keyType, classLoader, typeRegistry));
        return colDef;
    }

    private KeySeqHolder resolveKeyType(TableDef<?> tableDef, ColumnDef colDef, String keyType, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) throws ReflectiveOperationException {
        if (StringUtils.isBlank(keyType)) {
            return null;
        }

        KeyTypeEnum keyTypeEnum = KeyTypeEnum.valueOfCode(keyType);
        if (keyTypeEnum != null) {
            switch (keyTypeEnum) {
                case Auto:
                case UUID32:
                case UUID36:
                    return keyTypeEnum.createHolder(new CreateContext(this.global, typeRegistry, tableDef, colDef, Collections.emptyMap()));
                case None:
                case Holder:
                case Sequence:
                default:
                    return null;
            }
        } else if (StringUtils.startsWithIgnoreCase(keyType, "KeySeq::")) {
            keyType = keyType.substring("KeySeq::".length());
            Map<String, Object> context = new HashMap<>();
            context.put(KeySeq.class.getName(), new KeySeqImpl(keyType));
            return KeyTypeEnum.Sequence.createHolder(new CreateContext(this.global, typeRegistry, tableDef, colDef, context));
        } else {
            Class<?> aClass = classLoader.loadClass(keyType);
            KeySeqHolderFactory holderFactory = (KeySeqHolderFactory) aClass.newInstance();
            return holderFactory.createHolder(new CreateContext(this.global, typeRegistry, tableDef, colDef, Collections.emptyMap()));
        }
    }

    private static Class<?> resolveJavaType(Node xmlNode, String javaType, Property property, ClassLoader classLoader) throws ReflectiveOperationException {
        Class<?> columnJavaType = BeanUtils.getPropertyType(property);

        if (StringUtils.isNotBlank(javaType)) {
            try {
                Class<?> configColumnJavaType = ClassUtils.getClass(classLoader, javaType);
                if (configColumnJavaType.isAssignableFrom(columnJavaType)) {
                    columnJavaType = configColumnJavaType;
                } else {
                    String errorMessage = configColumnJavaType.getName() + " is not a subclass of " + columnJavaType.getName() + ", location= " + logMessage(xmlNode);
                    throw new ClassCastException(errorMessage);
                }
            } catch (ClassNotFoundException e) {
                String errorMessage = javaType + ", location " + logMessage(xmlNode);
                throw new ClassNotFoundException(errorMessage);
            }
        }

        return columnJavaType;
    }

    private static Integer resolveJdbcType(String jdbcType, Class<?> javaType, TypeHandlerRegistry typeRegistry) {
        if (NumberUtils.isNumber(jdbcType)) {
            return NumberUtils.createInteger(jdbcType);
        } else {
            return TypeHandlerRegistry.toSqlType(javaType);
        }
    }

    private static TypeHandler<?> resolveTypeHandler(Class<?> javaType, Integer jdbcType, ClassLoader classLoader, String typeHandler, TypeHandlerRegistry typeRegistry) throws ClassNotFoundException {
        if (StringUtils.isNotBlank(typeHandler)) {
            Class<?> configTypeHandlerType = ClassUtils.getClass(classLoader, typeHandler);
            if (typeRegistry.hasTypeHandler(configTypeHandlerType)) {
                return typeRegistry.getTypeHandler(configTypeHandlerType);
            } else {
                if (TypeHandler.class.isAssignableFrom(configTypeHandlerType)) {
                    return createTypeHandler(configTypeHandlerType, javaType);
                } else {
                    throw new ClassCastException(configTypeHandlerType.getName() + " is not a subclass of " + TypeHandler.class.getName());
                }
            }
        }

        if (typeRegistry.hasTypeHandler(javaType, jdbcType)) {
            return typeRegistry.getTypeHandler(javaType, jdbcType);
        }

        if (typeRegistry.hasTypeHandler(javaType)) {
            return typeRegistry.getTypeHandler(javaType);
        }

        if (typeRegistry.hasTypeHandler(jdbcType)) {
            return typeRegistry.getTypeHandler(jdbcType);
        }

        return typeRegistry.getDefaultTypeHandler();
    }

    private static String logMessage(Node xmlNode) {
        Node xpath = xmlNode;
        StringBuilder xpathString = new StringBuilder();

        do {
            if (xpathString.length() > 0) {
                xpathString.insert(0, "/");
            }
            xpathString.insert(0, xpath.getNodeName());
            xpath = xpath.getParentNode();
        } while ((xpath.getParentNode() != null));

        Element documentElement = xmlNode.getOwnerDocument().getDocumentElement();
        NamedNodeMap docAttr = documentElement.getAttributes();
        Node spaceNode = docAttr.getNamedItem("namespace");
        xpathString.insert(0, "namespace=" + ((spaceNode != null) ? spaceNode.getNodeValue() : null) + ", ");

        NamedNodeMap mappingNodeAttr = xmlNode.getParentNode().getAttributes();
        Node idNode = mappingNodeAttr.getNamedItem("id");
        Node typeNode = mappingNodeAttr.getNamedItem("type");
        xpathString.append("[");
        xpathString.append("@id=" + ((idNode != null) ? idNode.getNodeValue() : null) + ", ");
        xpathString.append("@type=" + ((typeNode != null) ? typeNode.getNodeValue() : null));
        xpathString.append("]");

        return xpathString.toString();
    }

    private static class KeySeqImpl implements KeySeq {
        private final String keyType;

        public KeySeqImpl(String keyType) {
            this.keyType = keyType;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return KeySeq.class;
        }

        @Override
        public String value() {
            return this.keyType;
        }
    }
}
