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
package net.hasor.db.dal.repository.manager;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.db.dal.dynamic.DynamicContext;
import net.hasor.db.dal.dynamic.DynamicSql;
import net.hasor.db.dal.dynamic.rule.RuleRegistry;
import net.hasor.db.dal.repository.RefMapper;
import net.hasor.db.dal.repository.SimpleMapper;
import net.hasor.db.dal.repository.config.QuerySqlConfig;
import net.hasor.db.dal.repository.parser.ClassDynamicResolve;
import net.hasor.db.dal.repository.parser.DynamicResolve;
import net.hasor.db.dal.repository.parser.XmlDynamicResolve;
import net.hasor.db.dal.repository.parser.XmlTableMappingResolve;
import net.hasor.db.mapping.def.TableMapping;
import net.hasor.db.mapping.resolve.ClassTableMappingResolve;
import net.hasor.db.mapping.resolve.MappingOptions;
import net.hasor.db.mapping.resolve.TableMappingResolve;
import net.hasor.db.types.TypeHandlerRegistry;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapper 配置中心
 * @version : 2021-06-05
 * @author 赵永春 (zyc@byshell.org)
 */
public class DalRegistry {
    public static final DalRegistry                               DEFAULT    = new DalRegistry(null, null, null, MappingOptions.buildNew());
    private final       Map<String, Map<String, DynamicSql>>      dynamicMap = new ConcurrentHashMap<>();
    private final       Map<String, Map<String, TableMapping<?>>> mappingMap = new ConcurrentHashMap<>();
    private final       ClassLoader                               classLoader;
    private final       TypeHandlerRegistry                       typeRegistry;
    private final       RuleRegistry                              ruleRegistry;
    private final       MappingOptions                            mappingOptions;

    public DalRegistry() {
        this(null, null, null, null);
    }

    public DalRegistry(ClassLoader classLoader, TypeHandlerRegistry typeRegistry, RuleRegistry ruleRegistry, MappingOptions mappingOptions) {
        this.classLoader = (classLoader == null) ? Thread.currentThread().getContextClassLoader() : classLoader;
        this.typeRegistry = (classLoader == null) ? TypeHandlerRegistry.DEFAULT : typeRegistry;
        this.ruleRegistry = (classLoader == null) ? RuleRegistry.DEFAULT : ruleRegistry;
        this.mappingOptions = new MappingOptions(mappingOptions);
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

    public DynamicContext createContext(String space, Object context) {
        return new DalDynamicContext(space, context, this);
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
    public <T> TableMapping<T> findTableMapping(String space, Class<?> entityType) {
        return findTableMapping(space, entityType.getName());
    }

    /** 从类型中解析 TableMapping */
    public <T> TableMapping<T> findTableMapping(String space, String mapName) {
        Map<String, TableMapping<?>> resultMap = this.mappingMap.get(space);
        if (resultMap == null) {
            return null;
        } else {
            return (TableMapping<T>) resultMap.get(mapName);
        }
    }

    /** 解析并载入 mapper.xml（支持 MyBatis 大部分能力） */
    public void loadResource(String resource) throws IOException {
        try (InputStream stream = getResourceAsStream(resource)) {
            Element root = loadXmlRoot(resource, stream);

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
        String scope = refRepository.getName();
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
                try (InputStream stream = getResourceAsStream(resource)) {

                    Element root = loadXmlRoot(resource, stream);
                    MappingOptions options = MappingOptions.resolveOptions(root, this.mappingOptions);

                    this.loadDynamic(scope, root, options);

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
                    saveDynamic(scope, identify, dynamicSql);
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------

    protected void saveMapping(String space, String identify, TableMapping<?> tableMapping) throws IOException {
        if (!this.mappingMap.containsKey(space)) {
            this.mappingMap.put(space, new ConcurrentHashMap<>());
        }

        Map<String, TableMapping<?>> mappingMap = this.mappingMap.get(space);
        if (mappingMap.containsKey(identify)) {
            throw new IOException("repeat '" + identify + "' in " + (StringUtils.isBlank(space) ? "default namespace" : ("'" + space + "' namespace.")));
        } else {
            mappingMap.put(identify, tableMapping);
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

    private void loadReader(String scope, Element configRoot, MappingOptions options) throws IOException, ClassNotFoundException {
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

            if (StringUtils.isBlank(idString) && StringUtils.isBlank(typeString)) {
                throw new IOException("the <" + node.getNodeName() + "> tag, id and type require at least one.");
            }
            if (StringUtils.isBlank(idString)) {
                idString = typeString;
            }

            TableMapping<?> tableMapping = resolve.resolveTableMapping(node, getClassLoader(), getTypeRegistry(), options);
            saveMapping(scope, idString, tableMapping);
        }
    }

    private void loadDynamic(String scope, Element configRoot, MappingOptions options) throws IOException, ClassNotFoundException {
        NodeList childNodes = configRoot.getChildNodes();
        DynamicResolve<Node> resolve = getXmlDynamicResolve();
        TableMappingResolve<Class<?>> mapResolve = getClassTableMappingResolve();

        for (int i = 0, len = childNodes.getLength(); i < len; i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            NamedNodeMap nodeAttributes = node.getAttributes();
            Node idNode = nodeAttributes.getNamedItem("id");
            String idString = (idNode != null) ? idNode.getNodeValue() : null;
            if (StringUtils.isBlank(idString)) {
                throw new IOException("the <" + configRoot.getNodeName() + "> tag is missing an ID.");
            }

            DynamicSql dynamicSql = resolve.parseSqlConfig(node);
            if (dynamicSql instanceof QuerySqlConfig) {
                String resultMap = ((QuerySqlConfig) dynamicSql).getResultMap();
                String resultType = ((QuerySqlConfig) dynamicSql).getResultType();

                if (StringUtils.isNotBlank(resultMap)) {
                    TableMapping<?> tableMapping = null;

                    Map<String, TableMapping<?>> readerMap = this.mappingMap.get(scope);
                    if (readerMap != null) {
                        tableMapping = readerMap.get(resultMap);
                    }

                    Objects.requireNonNull(tableMapping, "loadMapper failed, '" + idString + "', resultMap '" + resultMap + "' is missing ,resource '" + scope + "'");
                }

                if (StringUtils.isNotBlank(resultType)) {

                    Class<?> mapType = ClassUtils.getClass(getClassLoader(), resultType);
                    TableMapping<?> tableMapping = mapResolve.resolveTableMapping(mapType, getClassLoader(), getTypeRegistry(), options);
                    saveMapping(scope, idString, tableMapping);
                }
            }

            if (dynamicSql != null) {
                saveDynamic(scope, idString, dynamicSql);
            }
        }
    }

    // --------------------------------------------------------------------------------------------

    protected InputStream getResourceAsStream(String resource) throws IOException {
        return ResourcesUtils.getResourceAsStream(resource);
    }

    protected Element loadXmlRoot(String resource, InputStream stream) throws ParserConfigurationException, IOException, SAXException {
        if (stream == null) {
            throw new IOException("mapper resource '" + resource + "' not exist.");
        }
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
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
}
