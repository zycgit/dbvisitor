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
package net.hasor.dbvisitor.dal.repository;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.reflect.resolvable.ResolvableType;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.dal.dynamic.DynamicSql;
import net.hasor.dbvisitor.dal.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.repository.config.QuerySqlConfig;
import net.hasor.dbvisitor.dal.repository.parser.ClassDynamicResolve;
import net.hasor.dbvisitor.dal.repository.parser.DynamicResolve;
import net.hasor.dbvisitor.dal.repository.parser.XmlDynamicResolve;
import net.hasor.dbvisitor.dal.repository.parser.XmlTableMappingResolve;
import net.hasor.dbvisitor.mapping.TableReader;
import net.hasor.dbvisitor.mapping.def.TableDef;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.reader.DynamicTableReader;
import net.hasor.dbvisitor.mapping.resolve.ClassTableMappingResolve;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.dbvisitor.mapping.resolve.TableMappingResolve;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Mapper 配置中心
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class DalRegistry {
    public static final DalRegistry                               DEFAULT          = new DalRegistry(null, null, null, MappingOptions.buildNew());
    private final       Map<String, Map<String, DynamicSql>>      dynamicMap       = new ConcurrentHashMap<>();
    private final       Map<String, Map<String, TableMapping<?>>> tableMappingMap  = new ConcurrentHashMap<>();
    private final       Map<String, TableReader<?>>               typeHandlerCache = new ConcurrentHashMap<>();

    private final ClassLoader         classLoader;
    private final TypeHandlerRegistry typeRegistry;
    private final RuleRegistry        ruleRegistry;
    private final MappingOptions      mappingOptions;

    public DalRegistry() {
        this(null, null, null, null);
    }

    public DalRegistry(ClassLoader classLoader, TypeHandlerRegistry typeRegistry, RuleRegistry ruleRegistry, MappingOptions mappingOptions) {
        this.classLoader = (classLoader == null) ? Thread.currentThread().getContextClassLoader() : classLoader;
        this.typeRegistry = (typeRegistry == null) ? TypeHandlerRegistry.DEFAULT : typeRegistry;
        this.ruleRegistry = (ruleRegistry == null) ? RuleRegistry.DEFAULT : ruleRegistry;
        this.mappingOptions = new MappingOptions(mappingOptions);

        for (String javaType : this.typeRegistry.getHandlerJavaTypes()) {
            TypeHandler<?> typeHandler = this.typeRegistry.getTypeHandler(javaType);
            TableReader<Object> tableReader = (columns, rs, rowNum) -> typeHandler.getResult(rs, 1);
            this.typeHandlerCache.put(javaType, tableReader);
        }
        boolean caseInsensitive = this.mappingOptions.getCaseInsensitive() == null || Boolean.TRUE.equals(this.mappingOptions.getCaseInsensitive());
        this.typeHandlerCache.put(Map.class.getName(), new DynamicTableReader(caseInsensitive, this.typeRegistry));

    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public TypeHandlerRegistry getTypeRegistry() {
        return this.typeRegistry;
    }

    public RuleRegistry getRuleRegistry() {
        return this.ruleRegistry;
    }

    public DynamicContext createContext(String space) {
        return new DalContext(space, this);
    }

    public MappingOptions cloneOptions() {
        return MappingOptions.buildNew(this.mappingOptions);
    }
    // --------------------------------------------------------------------------------------------

    /** 根据 namespace 和 ID 查找 DynamicSql */
    public DynamicSql findDynamicSql(Class<?> space, String dynamicId) {
        return findDynamicSql(space == null ? null : space.getName(), dynamicId);
    }

    /** 根据 namespace 和 ID 查找 DynamicSql */
    public DynamicSql findDynamicSql(String space, String dynamicId) {
        Map<String, DynamicSql> dynamicSqlMap = this.dynamicMap.get(space);
        if (dynamicSqlMap == null) {
            return null;
        } else {
            return dynamicSqlMap.get(dynamicId);
        }
    }

    /** 从类型中解析 TableMapping */
    public <T> TableMapping<T> findTableMapping(String space, String mapName) {
        space = StringUtils.isBlank(space) ? "" : space;
        Map<String, TableMapping<?>> resultMap = this.tableMappingMap.get(space);
        if (resultMap != null && resultMap.containsKey(mapName)) {
            return (TableMapping<T>) resultMap.get(mapName);
        } else if (StringUtils.isNotBlank(space)) {
            return findTableMapping("", mapName);
        } else {
            return (this != DEFAULT) ? DEFAULT.findTableMapping(space, mapName) : null;
        }
    }

    /** 从类型中解析 TableMapping */
    public <T> TableMapping<T> findTableMapping(String space, Class<?> mapType) {
        space = StringUtils.isBlank(space) ? "" : space;
        String[] names = new String[] {     //
                mapType.getName(),          //
                mapType.getSimpleName(),    //
                StringUtils.firstCharToLowerCase(mapType.getSimpleName())//
        };

        for (String name : names) {
            TableMapping<T> mapping = findTableMapping(space, name);
            if (mapping != null) {
                return mapping;
            }
        }

        Map<String, TableMapping<?>> resultMap = this.tableMappingMap.get(space);
        if (resultMap == null) {
            return null;
        }
        List<TableMapping<?>> mappings = resultMap.values().stream().filter(tableMapping -> {
            return mapType.isAssignableFrom(tableMapping.entityType());
        }).collect(Collectors.toList());

        if (mappings.size() == 1) {
            return (TableMapping<T>) mappings.get(0);
        } else if (mappings.size() > 1) {
            throw new NoSuchElementException("type '" + mapType.getName() + "' automatic choose failure, there are multiple matches.");
        } else {
            return null;
        }
    }

    /** 从类型中解析 TableReader */
    protected <T> TableReader<T> findTableReader(String scope, String entityType) {
        TableMapping<T> tableMapping = this.findTableMapping(scope, entityType);
        if (tableMapping != null) {
            return tableMapping.toReader();
        } else if (this.typeHandlerCache.containsKey(entityType)) {
            return (TableReader<T>) this.typeHandlerCache.get(entityType);
        } else {
            return (this != DEFAULT) ? DEFAULT.findTableReader(scope, entityType) : null;
        }
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

            String namespace = "";
            if (rootAttributes != null) {
                Node namespaceNode = rootAttributes.getNamedItem("namespace");
                if (namespaceNode != null && StringUtils.isBlank(namespace)) {
                    namespace = namespaceNode.getNodeValue();
                }
            }

            MappingOptions options = MappingOptions.resolveOptions(root, this.mappingOptions);

            this.loadReader(namespace, root, options);
            this.loadDynamic(namespace, root, options);

        } catch (ParserConfigurationException | SAXException | ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    /** 解析并载入 mapper.xml（支持 MyBatis 大部分能力） */
    public void loadMapper(Class<?> refRepository) throws IOException {
        if (!refRepository.isInterface()) {
            throw new UnsupportedOperationException("the '" + refRepository.getName() + "' must interface.");
        }
        String namespace = refRepository.getName();
        boolean simpleMapper = false;

        Annotation[] annotations = refRepository.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof DalMapper || annotation.annotationType().getAnnotation(DalMapper.class) != null) {
                simpleMapper = true;
                break;
            }
        }

        if (!simpleMapper) {
            throw new UnsupportedOperationException("type '" + refRepository.getName() + "' need @RefMapper or @SimpleMapper or @DalMapper");
        }

        RefMapper refMapper = refRepository.getAnnotation(RefMapper.class);
        if (refMapper != null) {
            String resource = refMapper.value();
            if (resource.startsWith("/")) {
                resource = resource.substring(1);
            }

            if (StringUtils.isBlank(resource) && !ClassDynamicResolve.matchType(refRepository)) {
                return;
            }

            if (StringUtils.isNotBlank(resource)) {
                try (InputStream stream = this.classLoader.getResourceAsStream(resource)) {
                    if (stream == null) {
                        throw new FileNotFoundException("not found mapper file '" + resource + "'");
                    }

                    Document document = loadXmlRoot(stream);
                    Element root = document.getDocumentElement();

                    MappingOptions options = MappingOptions.resolveOptions(root, this.mappingOptions);

                    this.loadReader(namespace, root, options);
                    this.loadDynamic(namespace, root, options);

                } catch (ParserConfigurationException | SAXException | ClassNotFoundException e) {
                    throw new IOException(e);
                }
            }
        }

        Method[] dalTypeMethods = refRepository.getMethods();
        DynamicResolve<Method> resolve = getMethodDynamicResolve();
        for (Method method : dalTypeMethods) {
            if (!ClassDynamicResolve.matchMethod(method)) {
                continue;
            }

            Class<?> resultType = null;
            for (Annotation anno : method.getAnnotations()) {
                if (anno instanceof Query) {
                    resultType = ((Query) anno).resultType();
                    break;
                }
            }
            resultType = (resultType == Object.class) ? null : resultType;
            if (resultType != null && findTableReader(namespace, resultType.getName()) == null) {
                this.loadAsMapping(namespace, resultType);
            }

            String identify = method.getName();
            DynamicSql dynamicSql = resolve.parseSqlConfig(method);

            if (dynamicSql != null) {
                saveDynamic(namespace, identify, dynamicSql);
            }
        }

        if (BaseMapper.class.isAssignableFrom(refRepository)) {
            ResolvableType type = ResolvableType.forClass(refRepository).as(BaseMapper.class);
            Class<?>[] generics = type.resolveGenerics(Object.class);
            Class<?> entityType = generics[0];
            entityType = (entityType == Object.class) ? null : entityType;
            if (entityType != null && findTableReader(namespace, entityType.getName()) == null) {
                this.loadAsMapping(namespace, entityType);
            }
        }
    }

    public <T> TableMapping<T> loadAsMapping(String space, Class<T> entityType) {
        if (entityType.isInterface() || entityType.isArray() || entityType.isEnum() || entityType.isPrimitive()) {
            throw new UnsupportedOperationException("entityType " + entityType.getName() + " must is pojo.");
        }

        TableDef<?> tableDef = getClassTableMappingResolve().resolveTableMapping(entityType, entityType.getClassLoader(), getTypeRegistry(), this.mappingOptions);
        saveMapping(space, entityType.getName(), tableDef);
        return (TableMapping<T>) tableDef;
    }
    // --------------------------------------------------------------------------------------------

    private void loadReader(final String space, Element configRoot, MappingOptions options) throws IOException, ClassNotFoundException {
        NodeList childNodes = configRoot.getChildNodes();
        TableMappingResolve<Node> resolve = getXmlTableMappingResolve();

        for (int i = 0, len = childNodes.getLength(); i < len; i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            boolean isResultMap = "resultMap".equalsIgnoreCase(node.getNodeName());
            if (!isResultMap) {
                continue;
            }

            NamedNodeMap nodeAttributes = node.getAttributes();
            Node idNode = nodeAttributes.getNamedItem("id");
            Node typeNode = nodeAttributes.getNamedItem("type");
            String idString = (idNode != null) ? idNode.getNodeValue() : null;
            String typeString = (typeNode != null) ? typeNode.getNodeValue() : null;
            String mapperSpace = space;

            if (StringUtils.isBlank(typeString)) {
                throw new IOException("the <resultMap> tag, type is null.");
            }
            mapperSpace = StringUtils.isBlank(mapperSpace) ? "" : mapperSpace;
            if (StringUtils.isBlank(idString)) {
                idString = typeString;
            }

            TableMapping<?> tableMapping = resolve.resolveTableMapping(node, getClassLoader(), getTypeRegistry(), options);
            saveMapping(mapperSpace, idString, tableMapping);
        }
    }

    private void loadDynamic(String scope, Element configRoot, MappingOptions options) throws IOException, ClassNotFoundException {
        NodeList childNodes = configRoot.getChildNodes();
        DynamicResolve<Node> resolve = getXmlDynamicResolve();

        for (int i = 0, len = childNodes.getLength(); i < len; i++) {
            Node node = childNodes.item(i);
            String elementName = node.getNodeName();
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if ("resultMap".equalsIgnoreCase(elementName)) {
                continue;
            }

            NamedNodeMap nodeAttributes = node.getAttributes();
            Node idNode = nodeAttributes.getNamedItem("id");
            String idString = (idNode != null) ? idNode.getNodeValue() : null;
            if (StringUtils.isBlank(idString)) {
                throw new IOException("the <" + node.getNodeName() + "> tag is missing an ID.");
            }

            DynamicSql dynamicSql = resolve.parseSqlConfig(node);
            if (dynamicSql instanceof QuerySqlConfig) {
                String resultMap = ((QuerySqlConfig) dynamicSql).getResultMap();
                String resultType = ((QuerySqlConfig) dynamicSql).getResultType();

                if (StringUtils.isNotBlank(resultMap)) {

                    String[] tableMappings = resultMap.split(",");
                    for (String mapping : tableMappings) {
                        if (findTableMapping(scope, mapping) == null) {
                            throw new IOException("loadMapper failed, '" + idString + "', resultMap '" + resultMap + "' is undefined ,resource '" + scope + "'");
                        }
                    }
                }

                if (StringUtils.isNotBlank(resultType)) {
                    String[] resultTypes = resultType.split(",");
                    for (String type : resultTypes) {
                        if (findTableReader(scope, type) == null) {
                            Class<?> resultClass = ClassUtils.getClass(getClassLoader(), type);
                            loadReaderByType(resultClass, options);
                        }
                    }
                }
            }

            if (dynamicSql != null) {
                saveDynamic(scope, idString, dynamicSql);
            }
        }
    }

    protected TableReader<?> loadReaderByType(Class<?> resultClass, MappingOptions options) throws IOException {
        if (this.typeHandlerCache.containsKey(resultClass.getName())) {
            return this.typeHandlerCache.get(resultClass.getName());
        }

        TableReader<?> tableReader = null;

        if (this.typeRegistry.hasTypeHandler(resultClass)) {
            TypeHandler<?> typeHandler = this.typeRegistry.getTypeHandler(resultClass);
            tableReader = (TableReader<Object>) (columns, rs, rowNum) -> typeHandler.getResult(rs, 1);
        } else {
            tableReader = getClassTableMappingResolve().resolveTableMapping(resultClass, resultClass.getClassLoader(), getTypeRegistry(), options).toReader();
        }

        if (tableReader == null) {
            throw new IOException("loadReaderByType failed, entityType '" + resultClass.getName() + "' can not resolve.");
        } else {
            this.typeHandlerCache.put(resultClass.getName(), tableReader);
            return tableReader;
        }
    }

    protected void saveMapping(String space, String identify, TableMapping<?> tableMapping) {
        space = StringUtils.isBlank(space) ? "" : space;
        if (!this.tableMappingMap.containsKey(space)) {
            this.tableMappingMap.put(space, new ConcurrentHashMap<>());
        }

        Map<String, TableMapping<?>> mappingMap = this.tableMappingMap.get(space);
        if (mappingMap.containsKey(identify)) {
            throw new IllegalStateException("repeat resultMap '" + identify + "' in " + (StringUtils.isBlank(space) ? "default namespace" : ("'" + space + "' namespace.")));
        } else {
            mappingMap.put(identify, tableMapping);
        }
    }

    protected void saveDynamic(String space, String identify, DynamicSql dynamicSql) throws IOException {
        if (identify.contains(".")) {
            throw new IllegalStateException("identify cannot contain the character '.'");
        }

        if (!this.dynamicMap.containsKey(space)) {
            this.dynamicMap.put(space, new ConcurrentHashMap<>());
        }

        Map<String, DynamicSql> sqlMap = this.dynamicMap.get(space);
        if (sqlMap.containsKey(identify)) {
            throw new IOException("repeat '" + identify + "' in " + (StringUtils.isBlank(space) ? "default namespace" : ("'" + space + "' namespace.")));
        } else {
            sqlMap.put(identify, dynamicSql);
        }
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

    protected XmlTableMappingResolve getXmlTableMappingResolve() {
        return new XmlTableMappingResolve();
    }

    protected ClassTableMappingResolve getClassTableMappingResolve() {
        return new ClassTableMappingResolve();
    }

    protected DynamicResolve<Method> getMethodDynamicResolve() {
        return new ClassDynamicResolve();
    }

    protected DynamicResolve<Node> getXmlDynamicResolve() {
        return new XmlDynamicResolve();
    }

    /** 生成动态 SQL 的 Build 环境 */
    private static class DalContext extends DynamicContext {
        private final String      space;
        private final DalRegistry dalRegistry;

        public DalContext(String space, DalRegistry dalRegistry) {
            this.space = space;
            this.dalRegistry = dalRegistry;
        }

        public DynamicSql findDynamic(String dynamicId) {
            return this.dalRegistry.findDynamicSql(this.space, dynamicId);
        }

        public TableMapping<?> findTableMapping(String resultMap) {
            return this.dalRegistry.findTableMapping(this.space, resultMap);
        }

        public TableReader<?> findTableReader(String resultType) {
            return this.dalRegistry.findTableReader(this.space, resultType);
        }

        public TypeHandlerRegistry getTypeRegistry() {
            return this.dalRegistry.getTypeRegistry();
        }

        public RuleRegistry getRuleRegistry() {
            return this.dalRegistry.getRuleRegistry();
        }

        public ClassLoader getClassLoader() {
            return this.dalRegistry.getClassLoader();
        }
    }
}
