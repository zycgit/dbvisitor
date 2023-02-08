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
import net.hasor.dbvisitor.dal.dynamic.DynamicSql;
import net.hasor.dbvisitor.dal.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.repository.config.QuerySqlConfig;
import net.hasor.dbvisitor.dal.repository.parser.ClassDynamicResolve;
import net.hasor.dbvisitor.dal.repository.parser.DynamicResolve;
import net.hasor.dbvisitor.dal.repository.parser.XmlDynamicResolve;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.TableReader;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.reader.ResultTableReader;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapper 配置中心
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class DalRegistry extends MappingRegistry {
    public static final DalRegistry                              DEFAULT        = new DalRegistry(null, null, null, MappingOptions.buildNew());
    private final       Map<String, Map<String, DynamicSql>>     dynamicMap     = new ConcurrentHashMap<>();
    private final       Map<String, Map<String, TableReader<?>>> readerCacheMap = new ConcurrentHashMap<>();
    private final       RuleRegistry                             ruleRegistry;

    public DalRegistry() {
        this(null, null, null, null);
    }

    public DalRegistry(MappingOptions options) {
        this(null, null, null, options);
    }

    public DalRegistry(ClassLoader classLoader, TypeHandlerRegistry typeRegistry, RuleRegistry ruleRegistry, MappingOptions options) {
        super(classLoader, typeRegistry, options);
        this.ruleRegistry = (ruleRegistry == null) ? RuleRegistry.DEFAULT : ruleRegistry;
        this.initReaderCacheMap();
    }

    private void initReaderCacheMap() {
        Map<String, TableReader<?>> cacheMap = this.readerCacheMap.computeIfAbsent("", s -> new ConcurrentHashMap<>());
        for (String javaType : this.typeRegistry.getHandlerJavaTypes()) {
            TypeHandler<?> typeHandler = this.typeRegistry.getTypeHandler(javaType);
            TableReader<?> tableReader = (columns, rs, rowNum) -> typeHandler.getResult(rs, 1);
            cacheMap.put(javaType, tableReader);
        }

        boolean caseInsensitive = this.options == null || this.options.getCaseInsensitive() == null || Boolean.TRUE.equals(this.options.getCaseInsensitive());
        ResultTableReader mapReader = new ResultTableReader(caseInsensitive, this.typeRegistry);
        cacheMap.put(Map.class.getName(), mapReader);
        cacheMap.put(Map.class.getSimpleName(), mapReader);
        cacheMap.put(StringUtils.firstCharToLowerCase(Map.class.getSimpleName()), mapReader);
    }

    public RuleRegistry getRuleRegistry() {
        return this.ruleRegistry;
    }

    // --------------------------------------------------------------------------------------------

    /** 根据 namespace 和 ID 查找 DynamicSql */
    public DynamicSql findDynamicSql(Class<?> space, String dynamicId) {
        return findDynamicSql(space == null ? "" : space.getName(), dynamicId);
    }

    /** 根据 namespace 和 ID 查找 DynamicSql */
    public DynamicSql findDynamicSql(String space, String dynamicId) {
        space = StringUtils.isBlank(space) ? "" : space;
        Objects.requireNonNull(dynamicId, "'dynamicId' cannot be null.");

        Map<String, DynamicSql> dynamicSqlMap = this.dynamicMap.get(space);
        if (dynamicSqlMap == null) {
            return null;
        } else {
            return dynamicSqlMap.get(dynamicId);
        }
    }

    /** 从类型中解析 TableMapping */
    public <T> TableMapping<T> findMapping(String space, String identify) {
        space = StringUtils.isBlank(space) ? "" : space;
        Objects.requireNonNull(identify, "'identify' cannot be null.");

        TableMapping<T> mapping = super.findMapping(space, identify);
        if (mapping != null) {
            return mapping;
        } else if (StringUtils.isNotBlank(space)) {
            return super.findMapping("", identify);
        } else {
            return (this != DEFAULT) ? DEFAULT.findMapping(space, identify) : null;
        }
    }

    /** 从类型中解析 TableMapping */
    public <T> TableMapping<T> findMapping(String space, Class<?> type) {
        space = StringUtils.isBlank(space) ? "" : space;

        String[] names = new String[] { //
                type.getName(),         //
                type.getSimpleName(),   //
                StringUtils.firstCharToLowerCase(type.getSimpleName())//
        };

        for (String name : names) {
            TableMapping<T> mapping = findMapping(space, name);
            if (mapping != null) {
                return mapping;
            }
        }

        return null;
    }

    /** 从类型中解析 TableReader */
    public <T> TableReader<T> findTableReader(String space, String identify) {
        space = StringUtils.isBlank(space) ? "" : space;
        Objects.requireNonNull(identify, "'identify' cannot be null.");

        // form cache
        Map<String, TableReader<?>> readerMap;
        if (this.readerCacheMap.containsKey(space)) {
            readerMap = this.readerCacheMap.get(space);
            if (readerMap.containsKey(identify)) {
                return (TableReader<T>) readerMap.get(identify);
            }
        }
        if (!StringUtils.equals(space, "")) {
            readerMap = this.readerCacheMap.get("");
            if (readerMap.containsKey(identify)) {
                return (TableReader<T>) readerMap.get(identify);
            }
        }

        // create and cache
        TableMapping<T> mapping = super.findMapping(space, identify);
        if (mapping != null) {
            Map<String, TableReader<?>> map = this.readerCacheMap.computeIfAbsent(space, s -> new ConcurrentHashMap<>());
            TableReader<T> tableReader = mapping.toReader();
            map.put(identify, tableReader);
            return tableReader;
        }
        mapping = super.findMapping("", identify);
        if (mapping != null) {
            Map<String, TableReader<?>> map = this.readerCacheMap.computeIfAbsent("", s -> new ConcurrentHashMap<>());
            TableReader<T> tableReader = mapping.toReader();
            map.put(identify, tableReader);
            return tableReader;
        }

        // default
        return (this != DEFAULT) ? DEFAULT.findTableReader(space, identify) : null;
    }

    /** 解析并载入 mapper.xml（支持 MyBatis 大部分能力） */
    public void loadMapper(Class<?> refRepository) throws IOException, ReflectiveOperationException {
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

            if (StringUtils.isBlank(resource)) {
                resource = refRepository.getName().replace('.', '/') + ".xml";
            }

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

                    this.loadReader(namespace, root);
                    this.loadDynamic(namespace, root);
                } catch (ParserConfigurationException | SAXException e) {
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
                this.asResultMap(namespace, resultType);
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
            if (entityType != null && findEntity(entityType) == null) {
                this.loadEntity(entityType.getName(), entityType);
            }
        }
    }

    /** 解析并载入 mapper.xml（支持 MyBatis 大部分能力） */
    public void loadMapper(InputStream stream) throws IOException, ReflectiveOperationException {
        Objects.requireNonNull(stream, "load InputStream is null.");
        try {
            Document document = loadXmlRoot(stream);
            Element root = document.getDocumentElement();
            NamedNodeMap rootAttributes = root.getAttributes();

            String namespace = readAttribute("namespace", rootAttributes);
            namespace = StringUtils.isBlank(namespace) ? "" : namespace;

            this.loadReader(namespace, root);
            this.loadDynamic(namespace, root);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }
    }
    // --------------------------------------------------------------------------------------------

    private void loadDynamic(String space, Element configRoot) throws IOException, ClassNotFoundException {
        NodeList childNodes = configRoot.getChildNodes();
        DynamicResolve<Node> resolve = getXmlDynamicResolve();

        for (int i = 0, len = childNodes.getLength(); i < len; i++) {
            Node node = childNodes.item(i);
            String elementName = node.getNodeName();
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            boolean isResultMap = StringUtils.equalsIgnoreCase("resultMap", elementName);
            boolean isEntity = StringUtils.equalsIgnoreCase("entity", elementName);
            if (isResultMap || isEntity) {
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
                        if (findMapping(space, mapping) == null) {
                            throw new IOException("loadMapper failed, '" + idString + "', resultMap/entity '" + resultMap + "' is undefined ,resource '" + space + "'");
                        }
                    }
                }

                if (StringUtils.isNotBlank(resultType)) {
                    String[] resultTypes = resultType.split(",");
                    for (String type : resultTypes) {
                        if (findTableReader(space, type) == null) {
                            Class<?> resultClass = ClassUtils.getClass(getClassLoader(), type);
                            asResultMap(space, resultClass);
                        }
                    }
                }
            }

            if (dynamicSql != null) {
                saveDynamic(space, idString, dynamicSql);
            }
        }
    }

    private void asResultMap(String space, Class<?> resultClass) {
        TableReader<?> tableReader;
        String identify = resultClass.getName();

        if (this.typeRegistry.hasTypeHandler(resultClass)) {
            TypeHandler<?> typeHandler = this.typeRegistry.getTypeHandler(resultClass);
            tableReader = (TableReader<Object>) (columns, rs, rowNum) -> typeHandler.getResult(rs, 1);
        } else {
            super.loadResultMap(space, resultClass.getSimpleName(), resultClass);
            TableMapping<?> mapping = super.findMapping(space, resultClass.getSimpleName());
            tableReader = mapping.toReader();
        }

        Map<String, TableReader<?>> map = this.readerCacheMap.computeIfAbsent(space, s -> new ConcurrentHashMap<>());
        map.put(identify, tableReader);
    }

    protected void saveDynamic(String space, String identify, DynamicSql dynamicSql) throws IOException {
        Objects.requireNonNull(identify, "'identify' cannot be null.");
        if (identify.contains(".")) {
            throw new IllegalStateException("identify cannot contain the character '.'");
        }

        space = StringUtils.isBlank(space) ? "" : space;
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

    protected DynamicResolve<Method> getMethodDynamicResolve() {
        return new ClassDynamicResolve();
    }

    protected DynamicResolve<Node> getXmlDynamicResolve() {
        return new XmlDynamicResolve();
    }
}
