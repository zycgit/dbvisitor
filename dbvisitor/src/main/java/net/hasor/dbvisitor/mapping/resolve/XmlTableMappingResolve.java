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
import net.hasor.cobble.reflect.Annotation;
import net.hasor.cobble.reflect.Annotations;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.mapping.*;
import net.hasor.dbvisitor.mapping.def.*;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通过 Xml 来解析 TableMapping
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-23
 */
public class XmlTableMappingResolve extends AbstractTableMappingResolve<Node> {
    private final ClassTableMappingResolve classTableMappingResolve;

    public XmlTableMappingResolve() {
        this.classTableMappingResolve = new ClassTableMappingResolve();
    }

    protected boolean hasAnyMapping(NodeList childNodes) {
        for (int i = 0, len = childNodes.getLength(); i < len; i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String elementName = node.getNodeName().toLowerCase().trim();
            if ("id".equalsIgnoreCase(elementName) || "result".equalsIgnoreCase(elementName) || "mapping".equalsIgnoreCase(elementName) || "index".equalsIgnoreCase(elementName)) {
                return true;
            }
        }
        return false;
    }

    protected TableDef<?> resolveTableInfo(boolean isEntity, NamedNodeMap xmlAttr, MappingOptions usingOpt, ClassLoader classLoader) throws ClassNotFoundException, IOException {
        String autoMapping = strFromXmlAttribute(xmlAttr, "autoMapping");
        String useDelimited = strFromXmlAttribute(xmlAttr, "useDelimited");
        String mapUnderscoreToCamelCase = strFromXmlAttribute(xmlAttr, "mapUnderscoreToCamelCase");
        String caseInsensitive = strFromXmlAttribute(xmlAttr, "caseInsensitive");

        Class<?> entityType = ClassUtils.getClass(classLoader, strFromXmlAttribute(xmlAttr, "type"), false);
        Annotations classAnno = Annotations.ofClass(entityType);
        boolean usingAutoProperty = StringUtils.isBlank(autoMapping) ? (usingOpt.getAutoMapping() == null || usingOpt.getAutoMapping()) : Boolean.parseBoolean(autoMapping);
        boolean usingUseDelimited = StringUtils.isBlank(useDelimited) ? Boolean.TRUE.equals(usingOpt.getUseDelimited()) : Boolean.parseBoolean(useDelimited);
        boolean usingMapUnderscoreToCamelCase = StringUtils.isBlank(mapUnderscoreToCamelCase) ? Boolean.TRUE.equals(usingOpt.getMapUnderscoreToCamelCase()) : Boolean.parseBoolean(mapUnderscoreToCamelCase);
        boolean usingCaseInsensitive = StringUtils.isBlank(caseInsensitive) ? (usingOpt.getCaseInsensitive() == null || usingOpt.getCaseInsensitive()) : Boolean.parseBoolean(caseInsensitive);
        SqlDialect dialect = usingOpt.getDefaultDialect();

        TableDef<?> def = new TableDef<>("", "", "", entityType, dialect,//
                usingAutoProperty, usingUseDelimited, usingCaseInsensitive, usingMapUnderscoreToCamelCase);
        def.setAnnotations(classAnno);

        if (isEntity) {
            String catalog = strFromXmlAttribute(xmlAttr, "catalog");
            String schema = strFromXmlAttribute(xmlAttr, "schema");
            String table = strFromXmlAttribute(xmlAttr, "table");
            def.setCatalog(StringUtils.isBlank(catalog) ? usingOpt.getCatalog() : catalog);
            def.setSchema(StringUtils.isBlank(schema) ? usingOpt.getSchema() : schema);
            def.setTable(table);

            TableDescDef tableDesc = new TableDescDef();
            tableDesc.setCharacterSet(strFromXmlAttribute(xmlAttr, "character-set"));
            tableDesc.setCollation(strFromXmlAttribute(xmlAttr, "collation"));
            tableDesc.setComment(strFromXmlAttribute(xmlAttr, "comment"));
            tableDesc.setOther(strFromXmlAttribute(xmlAttr, "other"));
            tableDesc.setDdlAuto(DdlAuto.valueOfCode(strFromXmlAttribute(xmlAttr, "ddlAuto")));
            def.setDescription(tableDesc);
        }

        return def;
    }

    public MappingOptions fromXmlNode(NamedNodeMap attributes, MappingOptions parent) {
        String caseInsensitive = strFromXmlAttribute(attributes, "caseInsensitive");
        String mapUnderscoreToCamelCase = strFromXmlAttribute(attributes, "mapUnderscoreToCamelCase");
        String autoMapping = strFromXmlAttribute(attributes, "autoMapping");
        String useDelimited = strFromXmlAttribute(attributes, "useDelimited");

        MappingOptions fileScope = MappingOptions.buildNew(parent);
        fileScope.setCaseInsensitive(StringUtils.isBlank(caseInsensitive) ? null : Boolean.parseBoolean(caseInsensitive));
        fileScope.setMapUnderscoreToCamelCase(StringUtils.isBlank(mapUnderscoreToCamelCase) ? null : Boolean.parseBoolean(mapUnderscoreToCamelCase));
        fileScope.setAutoMapping(StringUtils.isBlank(autoMapping) ? null : Boolean.parseBoolean(autoMapping));
        fileScope.setUseDelimited(StringUtils.isBlank(useDelimited) ? null : Boolean.parseBoolean(useDelimited));
        return fileScope;
    }

    @Override
    public TableDef<?> resolveTableMapping(Node refData, MappingOptions usingOpt, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) throws ReflectiveOperationException, IOException {
        NodeList childNodes = refData.getChildNodes();
        NamedNodeMap xmlAttr = refData.getAttributes();

        boolean isEntity = StringUtils.equalsIgnoreCase("entity", refData.getNodeName());
        TableDef<?> tableDef = this.resolveTableInfo(isEntity, xmlAttr, usingOpt, classLoader);

        if (hasAnyMapping(childNodes)) {
            this.loadTableMapping(isEntity, tableDef, refData, classLoader, typeRegistry);
            if (isEntity) {
                loadTableIndexes(tableDef, childNodes);
            }
        } else {
            Annotations classAnno = Annotations.ofClass(tableDef.entityType());
            this.classTableMappingResolve.resolveTableAndColumn(isEntity, classAnno, tableDef, classLoader, typeRegistry);
            if (isEntity) {
                this.classTableMappingResolve.loadTableIndex(classAnno, tableDef);
            }
        }

        return tableDef;
    }

    private void loadTableMapping(boolean isEntity, TableDef<?> tableDef, Node refData, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) throws ReflectiveOperationException {
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
                columnMapping = this.resolveProperty(isEntity, tableDef, true, node, propertyMap, classLoader, typeRegistry);
            } else if ("result".equalsIgnoreCase(elementName) || "mapping".equalsIgnoreCase(elementName)) {
                columnMapping = this.resolveProperty(isEntity, tableDef, false, node, propertyMap, classLoader, typeRegistry);
            } else if ("index".equalsIgnoreCase(elementName)) {
                continue; // ignore
            } else {
                throw new UnsupportedOperationException("tag <" + elementName + "> Unsupported.");
            }

            tableDef.addMapping(columnMapping);
        }
    }

    private ColumnMapping resolveProperty(boolean isEntity, TableDef<?> tableDef, boolean asPrimaryKey, Node xmlNode, Map<String, Property> propertyMap, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) throws ReflectiveOperationException {
        NamedNodeMap nodeAttributes = xmlNode.getAttributes();
        String column = strFromXmlAttribute(nodeAttributes, "column");
        String property = strFromXmlAttribute(nodeAttributes, "property");
        String javaType = strFromXmlAttribute(nodeAttributes, "javaType");
        String jdbcType = strFromXmlAttribute(nodeAttributes, "jdbcType");
        String typeHandler = strFromXmlAttribute(nodeAttributes, "typeHandler");
        String keyType = strFromXmlAttribute(nodeAttributes, "keyType");

        if (!propertyMap.containsKey(property)) {
            throw new NoSuchFieldException("property '" + property + "' undefined. location= " + logMessage(xmlNode));
        }

        Property propertyHandler = propertyMap.get(property);
        Class<?> columnJavaType = resolveJavaType(xmlNode, javaType, propertyHandler, classLoader);
        Integer columnJdbcType = resolveJdbcType(jdbcType, columnJavaType, typeRegistry);
        TypeHandler<?> columnTypeHandler = resolveTypeHandler(columnJavaType, columnJdbcType, classLoader, typeHandler, typeRegistry);
        ColumnDef colDef = new ColumnDef(column, property, columnJdbcType, columnJavaType, columnTypeHandler, propertyHandler);

        if (isEntity) {
            String insertStr = strFromXmlAttribute(nodeAttributes, "insert");
            String updateStr = strFromXmlAttribute(nodeAttributes, "update");
            String selectTemplate = strFromXmlAttribute(nodeAttributes, "selectTemplate");
            String insertTemplate = strFromXmlAttribute(nodeAttributes, "insertTemplate");
            String setColTemplate = strFromXmlAttribute(nodeAttributes, "setColTemplate");
            String setValueTemplate = strFromXmlAttribute(nodeAttributes, "setValueTemplate");
            String whereColTemplate = strFromXmlAttribute(nodeAttributes, "whereColTemplate");
            String whereValueTemplate = strFromXmlAttribute(nodeAttributes, "whereValueTemplate");
            colDef.setPrimaryKey(asPrimaryKey);
            colDef.setInsert(StringUtils.isBlank(insertStr) || Boolean.parseBoolean(insertStr));
            colDef.setUpdate(StringUtils.isBlank(updateStr) || Boolean.parseBoolean(updateStr));
            colDef.setSelectTemplate(StringUtils.isNotBlank(selectTemplate) ? selectTemplate : null);
            colDef.setInsertTemplate(StringUtils.isNotBlank(insertTemplate) ? insertTemplate : null);
            colDef.setSetColTemplate(StringUtils.isNotBlank(setColTemplate) ? setColTemplate : null);
            colDef.setSetValueTemplate(StringUtils.isNotBlank(setValueTemplate) ? setValueTemplate : null);
            colDef.setWhereColTemplate(StringUtils.isNotBlank(whereColTemplate) ? whereColTemplate : null);
            colDef.setWhereValueTemplate(StringUtils.isNotBlank(whereValueTemplate) ? whereValueTemplate : null);

            // for Description
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
            boolean nullable = StringUtils.isBlank(nullableStr) || Boolean.parseBoolean(updateStr);
            nullable = !asPrimaryKey && nullable;
            colDef.setDescription(new ColumnDescDef(sqlType, length, precision, scale, characterSet, collation, nullable, defaultValue, comment, other));
        }

        // init KeySeqHolder
        colDef.setKeySeqHolder(resolveKeyType(tableDef, colDef, keyType, classLoader, typeRegistry));
        return colDef;
    }

    private void loadTableIndexes(TableDef<?> tableDef, NodeList childNodes) {
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
                    return keyTypeEnum.createHolder(new KeySeqHolderContext(typeRegistry, tableDef, colDef, classLoader, Annotations.empty()));
                case None:
                case Holder:
                case Sequence:
                default:
                    return null;
            }
        } else if (StringUtils.startsWithIgnoreCase(keyType, "KeySeq::")) {
            Annotation mockKeySeq = Annotation.create();
            mockKeySeq.putData("value", keyType.substring("KeySeq::".length()));
            Annotations mockAnno = Annotations.create();
            mockAnno.putTypeData(KeySeq.class.getName(), mockKeySeq);
            return KeyTypeEnum.Sequence.createHolder(new KeySeqHolderContext(typeRegistry, tableDef, colDef, classLoader, mockAnno));
        } else {
            Class<?> aClass = classLoader.loadClass(keyType);
            KeySeqHolderFactory holderFactory = (KeySeqHolderFactory) aClass.newInstance();
            return holderFactory.createHolder(new KeySeqHolderContext(typeRegistry, tableDef, colDef, classLoader, Annotations.empty()));
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
            return typeRegistry.createTypeHandler(ClassUtils.getClass(classLoader, typeHandler), javaType);
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

    private static String strFromXmlAttribute(NamedNodeMap nodeAttributes, String key) {
        if (nodeAttributes != null) {
            Node node = nodeAttributes.getNamedItem(key);
            return (node != null) ? node.getNodeValue() : null;
        }
        return null;
    }
}
