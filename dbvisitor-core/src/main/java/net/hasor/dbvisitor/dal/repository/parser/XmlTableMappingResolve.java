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
package net.hasor.dbvisitor.dal.repository.parser;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.NumberUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.Property;
import net.hasor.dbvisitor.mapping.def.ColumnDef;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableDef;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.resolve.ClassTableMappingResolve;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.dbvisitor.mapping.resolve.TableMappingResolve;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通过 Xml 来解析 TableMapping
 * @version : 2021-06-23
 * @author 赵永春 (zyc@hasor.net)
 */
public class XmlTableMappingResolve implements TableMappingResolve<Node> {
    private final        ClassTableMappingResolve classResolveTableMapping = new ClassTableMappingResolve();
    private static final Map<Class<?>, Class<?>>  CLASS_MAPPING_MAP        = new HashMap<>();

    static {
        CLASS_MAPPING_MAP.put(List.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(Set.class, HashSet.class);
        CLASS_MAPPING_MAP.put(Map.class, HashMap.class);
    }

    @Override
    public TableMapping<?> resolveTableMapping(Node refData, ClassLoader classLoader, TypeHandlerRegistry typeRegistry, MappingOptions options) throws ClassNotFoundException {
        options = MappingOptions.resolveOptions(refData, options);
        NamedNodeMap nodeAttributes = refData.getAttributes();
        Node typeNode = nodeAttributes.getNamedItem("type");
        Node catalogNode = nodeAttributes.getNamedItem("catalog");
        Node schemaNode = nodeAttributes.getNamedItem("schema");
        Node tableNode = nodeAttributes.getNamedItem("table");
        String type = (typeNode != null) ? typeNode.getNodeValue() : null;
        String catalogName = (schemaNode != null) ? catalogNode.getNodeValue() : null;
        String schemaName = (schemaNode != null) ? schemaNode.getNodeValue() : null;
        String tableName = (tableNode != null) ? tableNode.getNodeValue() : null;

        Class<?> entityType = classLoader.loadClass(type);
        if (CLASS_MAPPING_MAP.containsKey(entityType)) {
            entityType = CLASS_MAPPING_MAP.get(entityType);
        }

        if (options.getAutoMapping() != null && options.getAutoMapping()) {
            TableDef<?> tableDef = this.classResolveTableMapping.resolveTableMapping(entityType, classLoader, typeRegistry, options);
            if (StringUtils.isNotBlank(schemaName)) {
                tableDef.setSchema(schemaName);
            }
            if (StringUtils.isNotBlank(tableName)) {
                tableDef.setTable(tableName);
            }

            return tableDef;
        } else {
            boolean caseInsensitive = options.getCaseInsensitive() == null || Boolean.TRUE.equals(options.getCaseInsensitive());
            if (StringUtils.isBlank(tableName)) {
                tableName = humpToLine(entityType.getSimpleName(), options.getMapUnderscoreToCamelCase());
            }

            TableDef<?> tableDef = new TableDef<>(catalogName, schemaName, tableName, entityType, false, false, caseInsensitive, typeRegistry);
            loadTableMapping(tableDef, refData, classLoader, typeRegistry);
            return tableDef;
        }
    }

    private void loadTableMapping(TableDef<?> tableDef, Node refData, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) throws ClassNotFoundException {
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
                columnMapping = this.resolveProperty(true, node, propertyMap, classLoader, typeRegistry);
            } else if ("result".equalsIgnoreCase(elementName) || "mapping".equalsIgnoreCase(elementName)) {
                columnMapping = this.resolveProperty(false, node, propertyMap, classLoader, typeRegistry);
            } else {
                throw new UnsupportedOperationException("tag <" + elementName + "> Unsupported.");
            }

            tableDef.addMapping(columnMapping);
        }
    }

    private ColumnMapping resolveProperty(boolean asPrimaryKey, Node xmlNode, Map<String, Property> propertyMap, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) throws ClassNotFoundException {
        NamedNodeMap nodeAttributes = xmlNode.getAttributes();
        Node columnNode = nodeAttributes.getNamedItem("column");
        Node propertyNode = nodeAttributes.getNamedItem("property");
        Node javaTypeNode = nodeAttributes.getNamedItem("javaType");
        Node jdbcTypeNode = nodeAttributes.getNamedItem("jdbcType");
        Node typeHandlerNode = nodeAttributes.getNamedItem("typeHandler");
        String column = (columnNode != null) ? columnNode.getNodeValue() : null;
        String property = (propertyNode != null) ? propertyNode.getNodeValue() : null;
        String javaType = (javaTypeNode != null) ? javaTypeNode.getNodeValue() : null;
        String jdbcType = (jdbcTypeNode != null) ? jdbcTypeNode.getNodeValue() : null;
        String typeHandler = (typeHandlerNode != null) ? typeHandlerNode.getNodeValue() : null;
        if (!propertyMap.containsKey(property)) {
            throw new IllegalStateException("property '" + property + "' undefined.");
        }

        Property propertyHandler = propertyMap.get(property);
        Class<?> columnJavaType = resolveJavaType(javaType, propertyHandler, classLoader);
        Integer columnJdbcType = resolveJdbcType(jdbcType, columnJavaType, typeRegistry);
        TypeHandler<?> columnTypeHandler = resolveTypeHandler(columnJavaType, columnJdbcType, classLoader, typeHandler, typeRegistry);
        boolean insert = true; // always is true
        boolean update = true; // always is true

        return new ColumnDef(column, property, columnJdbcType, columnJavaType, columnTypeHandler, propertyHandler, insert, update, asPrimaryKey);
    }

    private static Class<?> resolveJavaType(String javaType, Property property, ClassLoader classLoader) throws ClassNotFoundException {
        Class<?> columnJavaType = BeanUtils.getPropertyType(property);

        if (StringUtils.isNotBlank(javaType)) {
            Class<?> configColumnJavaType = ClassUtils.getClass(classLoader, javaType);
            if (configColumnJavaType.isAssignableFrom(columnJavaType)) {
                columnJavaType = configColumnJavaType;
            } else {
                throw new ClassCastException(configColumnJavaType.getName() + " is not a subclass of " + columnJavaType.getName());
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

    private static final Pattern humpPattern = Pattern.compile("[A-Z]");

    private static String humpToLine(String str, Boolean mapUnderscoreToCamelCase) {
        if (StringUtils.isBlank(str) || mapUnderscoreToCamelCase == null || !mapUnderscoreToCamelCase) {
            return str;
        }
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);

        String strString = sb.toString();
        strString = strString.replaceAll("_{2,}", "_");
        if (strString.charAt(0) == '_') {
            strString = strString.substring(1);
        }
        return strString;
    }
}
