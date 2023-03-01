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
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.NumberUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.convert.ConverterUtils;
import net.hasor.cobble.function.Property;
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.keyholder.CreateContext;
import net.hasor.dbvisitor.keyholder.KeySeq;
import net.hasor.dbvisitor.keyholder.KeySeqHolder;
import net.hasor.dbvisitor.keyholder.KeySeqHolderFactory;
import net.hasor.dbvisitor.mapping.KeyTypeEnum;
import net.hasor.dbvisitor.mapping.def.ColumnDef;
import net.hasor.dbvisitor.mapping.def.ColumnDescDef;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableDef;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 通过 Xml 来解析 TableMapping
 * @version : 2021-06-23
 * @author 赵永春 (zyc@hasor.net)
 */
public class XmlTableMappingResolve extends AbstractTableMappingResolve<Node> {
    private static final Logger                   logger = Logger.getLogger(XmlTableMappingResolve.class);
    private final        ClassTableMappingResolve classTableMappingResolve;

    public XmlTableMappingResolve(MappingOptions options) {
        super(options);
        this.classTableMappingResolve = new ClassTableMappingResolve(this.options);
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

    @Override
    public TableDef<?> resolveTableMapping(Node refData, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) throws ReflectiveOperationException {
        NamedNodeMap nodeAttributes = refData.getAttributes();
        Node typeNode = nodeAttributes.getNamedItem("type");
        Node catalogNode = nodeAttributes.getNamedItem("catalog");
        Node schemaNode = nodeAttributes.getNamedItem("schema");
        Node tableNode = nodeAttributes.getNamedItem("table");
        Node caseInsensitiveNode = nodeAttributes.getNamedItem("caseInsensitive");
        Node mapUnderscoreToCamelCaseNode = nodeAttributes.getNamedItem("mapUnderscoreToCamelCase");
        Node autoMappingNode = nodeAttributes.getNamedItem("autoMapping");
        Node commentNode = nodeAttributes.getNamedItem("comment");
        Node otherNode = nodeAttributes.getNamedItem("other");

        String type = (typeNode != null) ? typeNode.getNodeValue() : null;
        String catalogName = (schemaNode != null) ? catalogNode.getNodeValue() : null;
        String schemaName = (schemaNode != null) ? schemaNode.getNodeValue() : null;
        String tableName = (tableNode != null) ? tableNode.getNodeValue() : null;
        String caseInsensitive = (caseInsensitiveNode != null) ? caseInsensitiveNode.getNodeValue() : null;
        String mapUnderscoreToCamelCase = (mapUnderscoreToCamelCaseNode != null) ? mapUnderscoreToCamelCaseNode.getNodeValue() : null;
        String autoMapping = (autoMappingNode != null) ? autoMappingNode.getNodeValue() : null;
        String comment = (commentNode != null) ? commentNode.getNodeValue() : null;
        String other = (otherNode != null) ? otherNode.getNodeValue() : null;

        // overwrite data
        Class<?> entityType = classLoader.loadClass(type);
        Map<String, String> overwriteData = new HashMap<>();
        if (catalogName != null) {
            overwriteData.put("catalog", catalogName);
        }
        if (schemaName != null) {
            overwriteData.put("schema", schemaName);
        }
        if (tableName != null) {
            overwriteData.put("table", tableName);
        }
        if (caseInsensitive != null) {
            overwriteData.put("caseInsensitive", caseInsensitive);
        }
        if (mapUnderscoreToCamelCase != null) {
            overwriteData.put("mapUnderscoreToCamelCase", mapUnderscoreToCamelCase);
        }
        if (autoMapping != null) {
            overwriteData.put("autoMapping", autoMapping);
        }
        if (comment != null) {
            overwriteData.put("comment", comment);
        }
        if (other != null) {
            overwriteData.put("other", other);
        }

        TableDefaultInfo tableInfo = fetchDefaultInfoByEntity(classLoader, entityType, this.options, overwriteData);

        // passer tableDef
        TableDef<?> tableDef;
        if (hasAnyMapping(refData.getChildNodes())) {
            // xmlNode 含有配置属性映射，仅解析 Table
            tableDef = this.classTableMappingResolve.resolveTable(tableInfo, entityType);
            loadTableMapping(tableDef, refData, classLoader, typeRegistry);
        } else {
            // xmlNode 没有配置属性映射，完整解析 Table + Column
            tableDef = this.classTableMappingResolve.resolveTableAndColumn(tableInfo, entityType, typeRegistry);
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
            } else {
                throw new UnsupportedOperationException("tag <" + elementName + "> Unsupported.");
            }

            tableDef.addMapping(columnMapping);
        }
    }

    private ColumnMapping resolveProperty(TableDef<?> tableDef, boolean asPrimaryKey, Node xmlNode, Map<String, Property> propertyMap, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) throws ReflectiveOperationException {
        NamedNodeMap nodeAttributes = xmlNode.getAttributes();
        Node columnNode = nodeAttributes.getNamedItem("column");
        Node propertyNode = nodeAttributes.getNamedItem("property");
        Node javaTypeNode = nodeAttributes.getNamedItem("javaType");
        Node jdbcTypeNode = nodeAttributes.getNamedItem("jdbcType");
        Node typeHandlerNode = nodeAttributes.getNamedItem("typeHandler");
        Node keyTypeNode = nodeAttributes.getNamedItem("keyType");
        Node insertNode = nodeAttributes.getNamedItem("insert");
        Node updateNode = nodeAttributes.getNamedItem("update");
        Node selectTemplateNode = nodeAttributes.getNamedItem("selectTemplate");
        Node insertTemplateNode = nodeAttributes.getNamedItem("insertTemplate");
        Node setColTemplateNode = nodeAttributes.getNamedItem("setColTemplate");
        Node setValueTemplateNode = nodeAttributes.getNamedItem("setValueTemplate");
        Node whereColTemplateNode = nodeAttributes.getNamedItem("whereColTemplate");
        Node whereValueTemplateNode = nodeAttributes.getNamedItem("whereValueTemplate");
        Node commentNode = nodeAttributes.getNamedItem("comment");
        Node dbTypeNode = nodeAttributes.getNamedItem("dbType");
        Node lengthNode = nodeAttributes.getNamedItem("length");
        Node precisionNode = nodeAttributes.getNamedItem("precision");
        Node scaleNode = nodeAttributes.getNamedItem("scale");
        Node defaultValueNode = nodeAttributes.getNamedItem("defaultValue");
        Node nullableNode = nodeAttributes.getNamedItem("nullable");
        Node otherNode = nodeAttributes.getNamedItem("other");

        String column = (columnNode != null) ? columnNode.getNodeValue() : null;
        String property = (propertyNode != null) ? propertyNode.getNodeValue() : null;
        String javaType = (javaTypeNode != null) ? javaTypeNode.getNodeValue() : null;
        String jdbcType = (jdbcTypeNode != null) ? jdbcTypeNode.getNodeValue() : null;
        String typeHandler = (typeHandlerNode != null) ? typeHandlerNode.getNodeValue() : null;
        String keyType = (keyTypeNode != null) ? keyTypeNode.getNodeValue() : null;
        if (!propertyMap.containsKey(property)) {
            throw new NoSuchFieldException("property '" + property + "' undefined. location= " + logMessage(xmlNode));
        }

        Property propertyHandler = propertyMap.get(property);
        Class<?> columnJavaType = resolveJavaType(xmlNode, javaType, propertyHandler, classLoader);
        Integer columnJdbcType = resolveJdbcType(jdbcType, columnJavaType, typeRegistry);
        TypeHandler<?> columnTypeHandler = resolveTypeHandler(columnJavaType, columnJdbcType, classLoader, typeHandler, typeRegistry);
        boolean insert = insertNode == null || StringUtils.isBlank(insertNode.getNodeValue()) || Boolean.parseBoolean(insertNode.getNodeValue());
        boolean update = updateNode == null || StringUtils.isBlank(updateNode.getNodeValue()) || Boolean.parseBoolean(updateNode.getNodeValue());
        String selectTemplate = (selectTemplateNode != null) ? selectTemplateNode.getNodeValue() : null;
        String insertTemplate = (insertTemplateNode != null) ? insertTemplateNode.getNodeValue() : null;
        String setColTemplate = (setColTemplateNode != null) ? setColTemplateNode.getNodeValue() : null;
        String setValueTemplate = (setValueTemplateNode != null) ? setValueTemplateNode.getNodeValue() : null;
        String whereColTemplate = (whereColTemplateNode != null) ? whereColTemplateNode.getNodeValue() : null;
        String whereValueTemplate = (whereValueTemplateNode != null) ? whereValueTemplateNode.getNodeValue() : null;
        String comment = (commentNode != null) ? commentNode.getNodeValue() : null;
        String dbType = (dbTypeNode != null) ? dbTypeNode.getNodeValue() : null;
        String length = (lengthNode != null) ? lengthNode.getNodeValue() : null;
        String precision = (precisionNode != null) ? precisionNode.getNodeValue() : null;
        String scale = (scaleNode != null) ? scaleNode.getNodeValue() : null;
        String defaultValue = (defaultValueNode != null) ? defaultValueNode.getNodeValue() : null;
        Boolean nullable = (nullableNode != null) ? (Boolean) ConverterUtils.convert(nullableNode.getNodeValue(), Boolean.TYPE) : null;
        String other = (otherNode != null) ? otherNode.getNodeValue() : null;

        ColumnDef colDef = new ColumnDef(column, property, columnJdbcType, columnJavaType, columnTypeHandler, propertyHandler, insert, update, asPrimaryKey,//
                selectTemplate, insertTemplate, setColTemplate, setValueTemplate, whereColTemplate, whereValueTemplate);

        if (comment == null && dbType == null && length == null && precision == null && scale == null && defaultValue == null && nullable == null && other == null) {
            colDef.setDescription(null);
        } else {
            colDef.setDescription(new ColumnDescDef(comment, dbType, length, precision, scale, defaultValue, nullable, other));
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
                    return keyTypeEnum.createHolder(new CreateContext(this.options, typeRegistry, tableDef, colDef, Collections.emptyMap()));
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
            return KeyTypeEnum.Sequence.createHolder(new CreateContext(this.options, typeRegistry, tableDef, colDef, context));
        } else {
            Class<?> aClass = classLoader.loadClass(keyType);
            KeySeqHolderFactory holderFactory = (KeySeqHolderFactory) aClass.newInstance();
            return holderFactory.createHolder(new CreateContext(this.options, typeRegistry, tableDef, colDef, Collections.emptyMap()));
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
                    return ClassUtils.newInstance(configTypeHandlerType);
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
