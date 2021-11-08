/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.db.dal.repository;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.XmlUtils;
import net.hasor.cobble.loader.ResourceLoader;
import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
import net.hasor.db.dal.dynamic.DynamicContext;
import net.hasor.db.dal.dynamic.DynamicSql;
import net.hasor.db.dal.dynamic.rule.RuleRegistry;
import net.hasor.db.dal.execute.MapTableReader;
import net.hasor.db.dal.execute.SingleValueTableReader;
import net.hasor.db.dal.repository.config.QuerySqlConfig;
import net.hasor.db.dal.repository.parser.ClassDynamicResolve;
import net.hasor.db.dal.repository.parser.DynamicResolve;
import net.hasor.db.dal.repository.parser.XmlDynamicResolve;
import net.hasor.db.dal.repository.parser.XmlTableMappingResolve;
import net.hasor.db.mapping.TableReader;
import net.hasor.db.mapping.def.TableMapping;
import net.hasor.db.mapping.resolve.ClassTableMappingResolve;
import net.hasor.db.mapping.resolve.MappingOptions;
import net.hasor.db.mapping.resolve.TableMappingResolve;
import net.hasor.db.types.TypeHandlerRegistry;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapper 配置中心
 * @version : 2021-06-05
 * @author 赵永春 (zyc@byshell.org)
 */
public class DalRegistry {
    public static final DalRegistry                               DEFAULT         = new DalRegistry(null, null, null, MappingOptions.buildNew(), null);
    private final       Map<String, Map<String, DynamicSql>>      dynamicMap      = new ConcurrentHashMap<>();
    private final       Map<String, Map<String, TableMapping<?>>> tableMappingMap = new ConcurrentHashMap<>();
    private final       Map<String, TableReader<?>>               typeReaderMap   = new ConcurrentHashMap<>();
    private final       ResourceLoader                            resourceLoader;
    private final       ClassLoader                               classLoader;
    private final       TypeHandlerRegistry                       typeRegistry;
    private final       RuleRegistry                              ruleRegistry;
    private final       MappingOptions                            mappingOptions;

    public DalRegistry() {
        this(null, null, null, null, null);
    }

    public DalRegistry(ClassLoader classLoader, TypeHandlerRegistry typeRegistry, RuleRegistry ruleRegistry, MappingOptions mappingOptions, ResourceLoader resourceLoader) {
        this.classLoader = (classLoader == null) ? Thread.currentThread().getContextClassLoader() : classLoader;
        this.typeRegistry = (classLoader == null) ? TypeHandlerRegistry.DEFAULT : typeRegistry;
        this.ruleRegistry = (classLoader == null) ? RuleRegistry.DEFAULT : ruleRegistry;
        this.mappingOptions = new MappingOptions(mappingOptions);
        this.resourceLoader = (resourceLoader == null) ? new ClassPathResourceLoader(this.classLoader) : resourceLoader;

        boolean caseInsensitive = this.mappingOptions.getCaseInsensitive() == null || Boolean.TRUE.equals(this.mappingOptions.getCaseInsensitive());
        this.typeReaderMap.put(byte.class.getName(), new SingleValueTableReader<>(byte.class, this.typeRegistry));
        this.typeReaderMap.put(Byte.class.getName(), new SingleValueTableReader<>(Byte.class, this.typeRegistry));
        this.typeReaderMap.put(short.class.getName(), new SingleValueTableReader<>(short.class, this.typeRegistry));
        this.typeReaderMap.put(Short.class.getName(), new SingleValueTableReader<>(Short.class, this.typeRegistry));
        this.typeReaderMap.put(int.class.getName(), new SingleValueTableReader<>(int.class, this.typeRegistry));
        this.typeReaderMap.put(Integer.class.getName(), new SingleValueTableReader<>(Integer.class, this.typeRegistry));
        this.typeReaderMap.put(long.class.getName(), new SingleValueTableReader<>(long.class, this.typeRegistry));
        this.typeReaderMap.put(Long.class.getName(), new SingleValueTableReader<>(Long.class, this.typeRegistry));
        this.typeReaderMap.put(float.class.getName(), new SingleValueTableReader<>(float.class, this.typeRegistry));
        this.typeReaderMap.put(Float.class.getName(), new SingleValueTableReader<>(Float.class, this.typeRegistry));
        this.typeReaderMap.put(double.class.getName(), new SingleValueTableReader<>(double.class, this.typeRegistry));
        this.typeReaderMap.put(Double.class.getName(), new SingleValueTableReader<>(Double.class, this.typeRegistry));
        this.typeReaderMap.put(char.class.getName(), new SingleValueTableReader<>(char.class, this.typeRegistry));
        this.typeReaderMap.put(Character.class.getName(), new SingleValueTableReader<>(Character.class, this.typeRegistry));

        this.typeReaderMap.put(String.class.getName(), new SingleValueTableReader<>(String.class, this.typeRegistry));
        this.typeReaderMap.put(Map.class.getName(), new MapTableReader(caseInsensitive, this.typeRegistry));
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

    /** 从类型中解析 TableReader */
    public <T> TableReader<T> findTableReader(String entityType) {
        TableReader<?> tableReader = this.typeReaderMap.get(entityType);
        if (tableReader != null) {
            return (TableReader<T>) tableReader;
        } else {
            return (this != DEFAULT) ? DEFAULT.findTableReader(entityType) : null;
        }
    }

    /** 解析并载入 mapper.xml（支持 MyBatis 大部分能力） */
    public void loadMapper(URL resource) throws IOException {
        try (InputStream stream = resource.openStream()) {
            this.loadMapper(stream);
        }
    }

    /** 解析并载入 mapper.xml（支持 MyBatis 大部分能力） */
    public void loadMapper(String resource) throws IOException {
        try (InputStream stream = this.resourceLoader.getResourceAsStream(resource)) {
            this.loadMapper(stream);
        }
    }

    /** 解析并载入 mapper.xml（支持 MyBatis 大部分能力） */
    public void loadMapper(InputStream stream) throws IOException {
        try {
            Element root = loadXmlRoot(stream);
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
        RefMapper refMapper = refRepository.getAnnotation(RefMapper.class);
        SimpleMapper simpleMapper = refRepository.getAnnotation(SimpleMapper.class);

        if (refMapper != null && simpleMapper != null) {
            throw new UnsupportedOperationException("type '" + refRepository.getName() + "' @RefMapper or @SimpleMapper cannot be both used.");
        }
        if (refMapper == null && simpleMapper == null) {
            throw new UnsupportedOperationException("type '" + refRepository.getName() + "' need @RefMapper or @SimpleMapper");
        }

        if (refMapper != null) {
            String resource = refMapper.value();

            if (StringUtils.isBlank(resource) && !ClassDynamicResolve.matchType(refRepository)) {
                return;
            }

            if (StringUtils.isNotBlank(resource)) {
                try (InputStream stream = this.resourceLoader.getResourceAsStream(resource)) {

                    Element root = loadXmlRoot(stream);
                    MappingOptions options = MappingOptions.resolveOptions(root, this.mappingOptions);

                    this.loadReader(namespace, root, options);
                    this.loadDynamic(namespace, root, options);

                } catch (ParserConfigurationException | SAXException | ClassNotFoundException e) {
                    throw new IOException(e);
                }
            }
        }

        if (simpleMapper != null) {
            Method[] dalTypeMethods = refRepository.getMethods();
            DynamicResolve<Method> resolve = getMethodDynamicResolve();

            for (Method method : dalTypeMethods) {
                if (!ClassDynamicResolve.matchMethod(method)) {
                    continue;
                }

                String identify = method.getName();
                DynamicSql dynamicSql = resolve.parseSqlConfig(method);

                if (dynamicSql != null) {
                    saveDynamic(namespace, identify, dynamicSql);
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------

    protected void saveMapping(String space, String identify, TableMapping<?> tableMapping) throws IOException {
        space = StringUtils.isBlank(space) ? "" : space;
        if (!this.tableMappingMap.containsKey(space)) {
            this.tableMappingMap.put(space, new ConcurrentHashMap<>());
        }

        Map<String, TableMapping<?>> mappingMap = this.tableMappingMap.get(space);
        if (mappingMap.containsKey(identify)) {
            throw new IOException("repeat resultMap '" + identify + "' in " + (StringUtils.isBlank(space) ? "default namespace" : ("'" + space + "' namespace.")));
        } else {
            mappingMap.put(identify, tableMapping);
        }
    }

    protected void saveReader(String resultType, TableReader<?> tableReader) throws IOException {
        if (StringUtils.isBlank(resultType) || tableReader == null) {
            return;
        }

        if (this.typeReaderMap.containsKey(resultType)) {
            throw new IOException("repeat resultType '" + resultType + "'");
        } else {
            this.typeReaderMap.put(resultType, tableReader);
        }
    }

    protected void saveDynamic(String space, String identify, DynamicSql dynamicSql) throws IOException {
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

    private void loadReader(final String space, Element configRoot, MappingOptions options) throws IOException, ClassNotFoundException {
        NodeList childNodes = configRoot.getChildNodes();
        TableMappingResolve<Node> resolve = getXmlTableMappingResolve();

        for (int i = 0, len = childNodes.getLength(); i < len; i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            boolean isEntityMap = "entityMap".equalsIgnoreCase(node.getNodeName());
            boolean isResultMap = "resultMap".equalsIgnoreCase(node.getNodeName());
            if (!(isResultMap || isEntityMap)) {
                continue;
            }

            NamedNodeMap nodeAttributes = node.getAttributes();
            Node idNode = nodeAttributes.getNamedItem("id");
            Node typeNode = nodeAttributes.getNamedItem("type");
            Node tableNode = nodeAttributes.getNamedItem("table");
            String idString = (idNode != null) ? idNode.getNodeValue() : null;
            String typeString = (typeNode != null) ? typeNode.getNodeValue() : null;
            String tableName = (tableNode != null) ? tableNode.getNodeValue() : null;
            String mapperSpace = space;

            if (isEntityMap) {
                if (StringUtils.isBlank(tableName)) {
                    throw new IOException("<entityMap> must be include 'table'='xxx'.");
                }
                mapperSpace = "";
                idString = typeString;
            }
            if (isResultMap) {
                if (StringUtils.isBlank(idString) && StringUtils.isBlank(typeString)) {
                    throw new IOException("the <resultMap> tag, id and type require at least one.");
                }
                mapperSpace = StringUtils.isBlank(mapperSpace) ? "" : mapperSpace;
                if (StringUtils.isBlank(idString)) {
                    idString = typeString;
                }
            }

            TableMapping<?> tableMapping = resolve.resolveTableMapping(node, getClassLoader(), getTypeRegistry(), options);
            saveMapping(mapperSpace, idString, tableMapping);
        }
    }

    private void loadDynamic(String scope, Element configRoot, MappingOptions options) throws IOException, ClassNotFoundException {
        NodeList childNodes = configRoot.getChildNodes();
        DynamicResolve<Node> resolve = getXmlDynamicResolve();
        TableMappingResolve<Class<?>> mapResolve = getClassTableMappingResolve();

        for (int i = 0, len = childNodes.getLength(); i < len; i++) {
            Node node = childNodes.item(i);
            String elementName = node.getNodeName();
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if ("entityMap".equalsIgnoreCase(elementName) || "resultMap".equalsIgnoreCase(elementName)) {
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

                    if (findTableReader(resultType) == null) {
                        Class<?> resultTypeClass = ClassUtils.getClass(getClassLoader(), resultType);
                        TableMapping<?> tableMapping = mapResolve.resolveTableMapping(resultTypeClass, getClassLoader(), getTypeRegistry(), options);

                        if (tableMapping != null) {
                            saveReader(resultTypeClass.getName(), tableMapping.toReader());
                        } else {
                            throw new IOException("loadMapper failed, '" + idString + "', resultType '" + resultType + "' is undefined or load failed. ,resource '" + scope + "'");
                        }
                    }
                }
            }

            if (dynamicSql != null) {
                saveDynamic(scope, idString, dynamicSql);
            }
        }
    }

    // --------------------------------------------------------------------------------------------

    protected Element loadXmlRoot(InputStream stream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder documentBuilder = XmlUtils.getDocumentBuilderWithNoValid().newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource(stream));
        return document.getDocumentElement();
    }

    protected TableMappingResolve<Node> getXmlTableMappingResolve() {
        return new XmlTableMappingResolve();
    }

    protected TableMappingResolve<Class<?>> getClassTableMappingResolve() {
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
            return this.dalRegistry.findTableReader(resultType);
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
