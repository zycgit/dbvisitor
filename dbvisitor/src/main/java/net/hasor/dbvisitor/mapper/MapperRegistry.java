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
package net.hasor.dbvisitor.mapper;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.EConsumer;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.reflect.resolvable.ResolvableType;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.mapper.def.QueryType;
import net.hasor.dbvisitor.mapper.def.SqlConfig;
import net.hasor.dbvisitor.mapper.resolve.ClassSqlConfigResolve;
import net.hasor.dbvisitor.mapper.resolve.SqlConfigResolve;
import net.hasor.dbvisitor.mapper.resolve.XmlSqlConfigResolve;
import net.hasor.dbvisitor.mapping.MappingHelper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.ResultMap;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.template.jdbc.RowMapper;
import net.hasor.dbvisitor.template.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.template.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.template.jdbc.mapper.SingleColumnRowMapper;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapper 配置中心
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-05
 */
public class MapperRegistry {
    private static final Logger                                 logger    = LoggerFactory.getLogger(MapperRegistry.class);
    public static final  MapperRegistry                         DEFAULT   = new MapperRegistry(MacroRegistry.DEFAULT, MappingRegistry.DEFAULT, TypeHandlerRegistry.DEFAULT);
    private final        Map<String, Map<String, StatementDef>> configMap = new ConcurrentHashMap<>();
    protected final      ClassLoader                            classLoader;
    protected final      MappingRegistry                        mappingRegistry;
    protected final      MacroRegistry                          macroRegistry;
    protected final      TypeHandlerRegistry                    typeRegistry;
    protected final      Set<String>                            loaded;

    public MapperRegistry() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.mappingRegistry = new MappingRegistry(this.classLoader);
        this.typeRegistry = this.mappingRegistry.getTypeRegistry();
        this.macroRegistry = MacroRegistry.DEFAULT;
        this.loaded = new HashSet<>();
    }

    public MapperRegistry(MacroRegistry macroRegistry, MappingRegistry mappingRegistry, TypeHandlerRegistry typeRegistry) {
        this.classLoader = mappingRegistry.getClassLoader();
        this.mappingRegistry = mappingRegistry;
        this.typeRegistry = typeRegistry;
        this.macroRegistry = macroRegistry;
        this.loaded = new HashSet<>();
    }

    public StatementDef findStatement(Class<?> namespace, String statement) {
        return this.findStatement(namespace.getName(), statement);
    }

    public StatementDef findStatement(Class<?> namespace, Method statement) {
        return this.findStatement(namespace.getName(), statement.getName());
    }

    public StatementDef findStatement(String namespace, String statement) {
        if (this.configMap.containsKey(namespace)) {
            return this.configMap.get(namespace).get(statement);
        } else {
            return null;
        }
    }

    public void loadMapper(Class<?> mapperType) throws IOException, ReflectiveOperationException {
        testMapper(mapperType);

        // check duplicated
        tryLoaded(mapperType, mt -> {
            // load resource.
            if (mapperType.isAnnotationPresent(RefMapper.class)) {
                this.tryLoadRefMapperFile(mapperType, mapperType.getAnnotation(RefMapper.class));
            }

            // load entity.
            if (BaseMapper.class.isAssignableFrom(mapperType)) {
                ResolvableType type = ResolvableType.forClass(mapperType).as(BaseMapper.class);
                Class<?>[] generics = type.resolveGenerics(Object.class);
                Class<?> entityType = (generics[0] == Object.class) ? null : generics[0];
                if (entityType != null) {
                    if (this.mappingRegistry.findByEntity(entityType) == null) {
                        this.mappingRegistry.loadEntityToSpace(entityType);
                    }
                }
            }

            // load method.
            this.tryLoadMethod(mapperType, mapperType.getMethods());
        });
    }

    // --------------------------------------------------------------------------------------------

    private void tryLoadRefMapperFile(Class<?> mapperType, RefMapper refMapper) throws IOException {
        String resource = refMapper.value();
        if (StringUtils.isBlank(resource)) {
            resource = mapperType.getName().replace('.', '/') + ".xml";
        }
        if (resource.startsWith("/")) {
            resource = resource.substring(1);
        }

        if (StringUtils.isBlank(resource) && !ClassSqlConfigResolve.matchType(mapperType)) {
            return;
        }

        if (StringUtils.isNotBlank(resource)) {
            this.tryLoaded(resource, r -> {
                // try load mapping
                this.mappingRegistry.loadMapper(r);

                // try load mapper
                try (InputStream stream = this.classLoader.getResourceAsStream(r)) {
                    if (stream == null) {
                        throw new FileNotFoundException("not found mapper file '" + r + "'"); // Don't block the app from launching
                    }

                    Document document = MappingHelper.loadXmlRoot(stream, this.mappingRegistry.getClassLoader());
                    this.tryLoadNode(r, document.getDocumentElement());
                } catch (ParserConfigurationException | SAXException | ClassNotFoundException e) {
                    throw new IOException(e);
                }
            });
        }
    }

    private void tryLoadNode(String resource, Element configRoot) throws ClassNotFoundException, IOException {
        NamedNodeMap rootAttributes = configRoot.getAttributes();
        String configSpace = MappingHelper.readAttribute("namespace", rootAttributes);
        configSpace = StringUtils.isBlank(configSpace) ? "" : configSpace;

        SqlConfigResolve<Node> resolve = getXmlDynamicResolve();
        NodeList childNodes = configRoot.getChildNodes();
        for (int i = 0, len = childNodes.getLength(); i < len; i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            NamedNodeMap nodeAttributes = node.getAttributes();
            String configId = MappingHelper.readAttribute("id", nodeAttributes);
            if (StringUtils.isBlank(configId)) {
                throw new IllegalStateException("the <" + node.getNodeName() + "> tag, id is null.");
            }

            SqlConfig sqlConfig = resolve.parseSqlConfig(configSpace, node);
            if (sqlConfig != null) {
                StatementDef def = new StatementDef(configSpace, sqlConfig);
                this.configMap.computeIfAbsent(configSpace, s -> new ConcurrentHashMap<>()).put(configId, def);

                if (sqlConfig.getType() == QueryType.Segment) {
                    String macroId = StringUtils.isBlank(configSpace) ? configId : (configSpace + "." + configId);
                    this.macroRegistry.addMacro(macroId, sqlConfig);
                } else if (sqlConfig.getType() == QueryType.Select) {
                    String resultMapStr = MappingHelper.readAttribute("resultMap", nodeAttributes);
                    String resultTypeStr = MappingHelper.readAttribute("resultType", nodeAttributes);
                    if (StringUtils.isNotBlank(resultMapStr) && StringUtils.isNotBlank(resultTypeStr)) {
                        throw new IllegalStateException("the '" + configId + "', resultMap,resultType cannot be used at the same time. location at '" + resource + "'");
                    }

                    if (StringUtils.isNotBlank(resultMapStr)) {
                        TableMapping<?> tabMapping = this.mappingRegistry.findBySpace(configSpace, resultMapStr);
                        if (tabMapping == null) {
                            String fullname = StringUtils.isBlank(configSpace) ? resultMapStr : (configSpace + "." + resultMapStr);
                            throw new IllegalStateException("the resultMap '" + fullname + "' cannot be found.");
                        } else {
                            def.setRowMapper(new BeanMappingRowMapper<>(tabMapping));
                        }
                    } else if (StringUtils.isNotBlank(resultTypeStr)) {
                        Class<?> requiredType = this.classLoader.loadClass(resultTypeStr);
                        def.setRowMapper(this.mapperFromType(configSpace, requiredType));
                    } else {
                        boolean caseInsensitive = MappingHelper.caseInsensitive(this.mappingRegistry.getGlobalOptions());
                        def.setRowMapper(new ColumnMapRowMapper(caseInsensitive, this.typeRegistry));
                    }
                }
            }
        }
    }

    private void tryLoadMethod(Class<?> mapperType, Method[] methods) throws IOException {
        SqlConfigResolve<Method> resolve = this.getMethodDynamicResolve();
        for (Method m : methods) {
            this.tryLoaded(m, method -> {
                // skip.
                if (method.isDefault()) {
                    return;
                }

                // check conflict
                String configSpace = mapperType.getName();
                String configId = method.getName();
                boolean hasConf = this.configMap.containsKey(configSpace) && this.configMap.containsKey(configId);
                boolean hasAnnoConf = ClassSqlConfigResolve.matchMethod(method);
                if (hasAnnoConf && hasConf) {
                    throw new IllegalStateException("Annotations and mapperFile conflicts with " + configSpace + "." + configId);
                }

                if (hasConf) {
                    return;
                }

                if (hasAnnoConf) {
                    StatementDef def = new StatementDef(configSpace, resolve.parseSqlConfig(configSpace, method));
                    this.configMap.computeIfAbsent(configSpace, s -> new ConcurrentHashMap<>()).put(configId, def);

                    //
                    Class<?> requiredClass = method.getReturnType();
                    if (Collection.class.isAssignableFrom(requiredClass)) {
                        Type requiredType = method.getGenericReturnType();
                        ResolvableType type = ResolvableType.forType(requiredType);
                        requiredClass = type.getGeneric(0).resolve();
                        def.setMappingType(requiredClass);
                    } else if (requiredClass.isArray()) {
                        requiredClass = requiredClass.getComponentType();
                        def.setMappingType(requiredClass);
                    }

                    RowMapper<?> rowMapper = this.mapperFromMethodReturning(method);
                    if (rowMapper != null) {
                        def.setRowMapper(rowMapper);
                    } else {
                        def.setRowMapper(this.mapperFromType(configSpace, requiredClass));
                    }
                }
            });
        }
    }

    private RowMapper<?> mapperFromMethodReturning(Method method) throws IOException {
        // 1.determine the mapping type.
        Class<?> resultType = method.getReturnType();
        if (resultType == Object.class || resultType == Void.class) {
            boolean caseInsensitive = MappingHelper.caseInsensitive(this.mappingRegistry.getGlobalOptions());
            return new ColumnMapRowMapper(caseInsensitive, this.typeRegistry);
        }

        // 2. method @ResultMap
        if (method.isAnnotationPresent(ResultMap.class)) {
            MappingHelper.NameInfo nameInfo = MappingHelper.findNameInfo(method);
            if (nameInfo == null) {
                throw new IllegalArgumentException("the @ResultMap annotation on the method(" + method + ") is incorrectly configured");
            }
            String space = nameInfo.getSpace();
            String name = nameInfo.getName();
            TableMapping<?> mapping = this.mappingRegistry.findBySpace(space, name);
            if (mapping == null) {
                String fullname = StringUtils.isBlank(space) ? name : (space + "." + name);
                throw new IllegalStateException("the resultMap '" + fullname + "' cannot be found.");
            }
            return new BeanMappingRowMapper<>(mapping);
        }

        return null;
    }

    private RowMapper<?> mapperFromType(String namespace, Class<?> requiredType) throws IOException {
        // as TypeHandler
        if (this.typeRegistry.hasTypeHandler(requiredType) || requiredType.isEnum()) {
            return new SingleColumnRowMapper<>(requiredType, this.typeRegistry);
        }

        // @Table or @ResultMap
        if (requiredType.isAnnotationPresent(Table.class) || requiredType.isAnnotationPresent(ResultMap.class)) {
            if (requiredType.isAnnotationPresent(Table.class)) {
                // Try to find in space using the typeFullName
                TableMapping<?> mapping = this.mappingRegistry.findBySpace(namespace, requiredType.getName());
                if (mapping == null) {
                    mapping = this.mappingRegistry.findByEntity(requiredType);
                }
                if (mapping == null) {
                    mapping = this.mappingRegistry.loadEntityToSpace(requiredType);
                }
                return new BeanMappingRowMapper<>(mapping);
            } else {
                TableMapping<?> mapping;
                if (requiredType.isAnnotationPresent(ResultMap.class)) {
                    MappingHelper.NameInfo nameInfo = MappingHelper.findNameInfo(requiredType);
                    mapping = this.mappingRegistry.findBySpace(nameInfo.getSpace(), nameInfo.getName());
                    if (mapping == null) {
                        mapping = this.mappingRegistry.loadResultMapToSpace(requiredType);
                    }
                } else {
                    mapping = this.mappingRegistry.findBySpace(namespace, requiredType.getName());
                    if (mapping == null) {
                        mapping = this.mappingRegistry.loadResultMapToSpace(requiredType, namespace, requiredType.getName());
                    }
                }
                return new BeanMappingRowMapper<>(mapping);
            }
        }

        // as Bean
        return new BeanMappingRowMapper<>(requiredType, this.mappingRegistry);
    }

    // --------------------------------------------------------------------------------------------

    private void tryLoaded(String target, EConsumer<String, IOException> call) throws IOException {
        String cacheKey = "RES::" + target;
        if (!loaded.contains(cacheKey)) {
            logger.info("loadMapper '" + target + "'");
            call.eAccept(target);
            this.loaded.add(cacheKey);
        }
    }

    private void tryLoaded(Class<?> target, EConsumer<Class<?>, IOException> call) throws IOException {
        String cacheKey = "TYPE::" + target;
        if (!loaded.contains(cacheKey)) {
            logger.info("loadMapper '" + target.getName() + "'");
            call.eAccept(target);
            this.loaded.add(cacheKey);
        }
    }

    private void tryLoaded(Method target, EConsumer<Method, IOException> call) throws IOException {
        String cacheKey = "METHOD::" + target;
        if (!loaded.contains(cacheKey)) {
            call.eAccept(target);
            this.loaded.add(cacheKey);
        }
    }

    protected static void testMapper(Class<?> mapperType) {
        if (!mapperType.isInterface()) {
            throw new UnsupportedOperationException("the '" + mapperType.getName() + "' must interface.");
        }

        boolean testMapper = false;
        java.lang.annotation.Annotation[] annotations = mapperType.getDeclaredAnnotations();
        for (java.lang.annotation.Annotation annotation : annotations) {
            if (annotation instanceof DalMapper || annotation.annotationType().getAnnotation(DalMapper.class) != null) {
                testMapper = true;
                break;
            }
        }

        if (!testMapper) {
            throw new UnsupportedOperationException("type '" + mapperType.getName() + "' need @RefMapper or @SimpleMapper or @DalMapper");
        }
    }

    protected SqlConfigResolve<Method> getMethodDynamicResolve() {
        return new ClassSqlConfigResolve();
    }

    protected SqlConfigResolve<Node> getXmlDynamicResolve() {
        return new XmlSqlConfigResolve();
    }
}
