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
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.EConsumer;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.reflect.resolvable.ResolvableType;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.SingleColumnRowMapper;
import net.hasor.dbvisitor.mapper.def.DqlConfig;
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
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapper 配置中心，负责加载和管理 Mapper 相关的配置信息，
 * 包括从类、XML 文件加载 Mapper 配置，查找 SQL 语句定义等功能。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public class MapperRegistry {
    private static final Logger                                 logger    = LoggerFactory.getLogger(MapperRegistry.class);
    private final        Map<String, Map<String, StatementDef>> configMap = new ConcurrentHashMap<>();
    protected final      ClassLoader                            classLoader;
    protected final      MappingRegistry                        mappingRegistry;
    protected final      MacroRegistry                          macroRegistry;
    protected final      TypeHandlerRegistry                    typeRegistry;
    protected final      Set<String>                            loaded;

    /**
     * 默认构造函数，使用当前线程的上下文类加载器初始化 MapperRegistry。
     */
    public MapperRegistry() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.mappingRegistry = new MappingRegistry(this.classLoader);
        this.typeRegistry = this.mappingRegistry.getTypeRegistry();
        this.macroRegistry = MacroRegistry.DEFAULT;
        this.loaded = new HashSet<>();
    }

    /**
     * 带参数的构造函数，允许用户指定 MappingRegistry 和 MacroRegistry。
     * @param mapping 映射注册表，用于管理实体类和数据库表之间的映射关系。
     * @param macro 宏注册表，用于管理 SQL 宏定义。
     */
    public MapperRegistry(MappingRegistry mapping, MacroRegistry macro) {
        this.classLoader = mapping.getClassLoader();
        this.mappingRegistry = mapping;
        this.typeRegistry = mapping.getTypeRegistry();
        this.macroRegistry = (macro == null) ? MacroRegistry.DEFAULT : macro;
        this.loaded = new HashSet<>();
    }

    /**
     * 获取当前使用的类加载器。
     * @return 类加载器对象。
     */
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * 获取映射注册表。
     * @return 映射注册表对象。
     */
    public MappingRegistry getMappingRegistry() {
        return this.mappingRegistry;
    }

    /**
     * 获取宏注册表。
     * @return 宏注册表对象。
     */
    public MacroRegistry getMacroRegistry() {
        return this.macroRegistry;
    }

    /**
     * 获取类型处理器注册表。
     * @return 类型处理器注册表对象。
     */
    public TypeHandlerRegistry getTypeRegistry() {
        return this.typeRegistry;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * 根据命名空间类和 SQL 语句 ID 查找对应的 StatementDef 对象。
     * @param namespace 命名空间类，使用类的全限定名作为命名空间。
     * @param statement SQL 语句的 ID。
     * @return 对应的 StatementDef 对象，如果未找到则返回 null。
     */
    public StatementDef findStatement(Class<?> namespace, String statement) {
        return this.findStatement(namespace.getName(), statement);
    }

    /**
     * 根据命名空间类和方法查找对应的 StatementDef 对象，使用方法名作为 SQL 语句 ID。
     * @param namespace 命名空间类，使用类的全限定名作为命名空间。
     * @param statement 方法对象，使用方法名作为 SQL 语句的 ID。
     * @return 对应的 StatementDef 对象，如果未找到则返回 null。
     */
    public StatementDef findStatement(Class<?> namespace, Method statement) {
        return this.findStatement(namespace.getName(), statement.getName());
    }

    /**
     * 根据命名空间和 SQL 语句 ID 查找对应的 StatementDef 对象。
     * @param namespace 命名空间字符串。
     * @param statement SQL 语句的 ID。
     * @return 对应的 StatementDef 对象，如果未找到则返回 null。
     */
    public StatementDef findStatement(String namespace, String statement) {
        if (this.configMap.containsKey(namespace)) {
            return this.configMap.get(namespace).get(statement);
        } else {
            return null;
        }
    }

    /**
     * 加载指定的 mapper 类型，包括其关联的 XML 资源、实体映射信息以及方法级别的 SQL 配置。
     * 该方法会检查 mapper 类型是否已经加载过，避免重复加载。
     * 如果 mapper 类型上存在 {@link RefMapper} 注解，会尝试加载对应的 XML 文件；
     * 如果 mapper 类型是 {@link BaseMapper} 的实现类，会加载其泛型对应的实体映射信息；
     * 最后会加载 mapper 类型中所有方法的 SQL 配置。
     *
     * @param mapperType 要加载的 mapper 类型，必须是一个接口，并且需要有 @RefMapper 或 @SimpleMapper 或 @DalMapper 注解。
     */
    public void loadMapper(Class<?> mapperType) throws Exception {
        testMapper(mapperType);

        // check duplicated
        tryLoaded(mapperType, mt -> {
            // load resource.
            boolean refXml = mapperType.isAnnotationPresent(RefMapper.class);
            if (refXml) {
                RefMapper refMapper = mapperType.getAnnotation(RefMapper.class);
                logger.info("mapper '" + mapperType.getName() + "' using '" + refMapper.value() + "'");

                String resource = refMapper.value();
                if (StringUtils.isBlank(resource)) {
                    resource = mapperType.getName().replace('.', '/') + ".xml";
                }
                if (resource.startsWith("/")) {
                    resource = resource.substring(1);
                }

                if (StringUtils.isBlank(resource) && !matchType(mapperType)) {
                    return;
                }

                this.loadMapper(resource);
            } else {
                logger.info("mapper '" + mapperType.getName() + "' using default.");
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
            SqlConfigResolve<Method> resolve = this.getMethodDynamicResolve();
            for (Method m : mapperType.getMethods()) {
                this.tryLoadMethod(resolve, mapperType, m, refXml);
            }
        });
    }

    /**
     * 加载指定资源路径的 mapper 文件。
     * 如果资源路径为空，会抛出 FileNotFoundException。
     * @param mapperResource 要加载的 mapper 文件的资源路径
     */
    public void loadMapper(String mapperResource) throws Exception {
        if (StringUtils.isBlank(mapperResource)) {
            throw new FileNotFoundException("mapper file ios empty.");
        }

        try (InputStream stream = ResourcesUtils.getResourceAsStream(this.classLoader, mapperResource)) {
            if (stream == null) {
                throw new FileNotFoundException("not found mapper file '" + mapperResource + "'"); // Don't block the app from launching
            }

            this.loadMapper(mapperResource, stream);
        }
    }

    /**
     * 从输入流加载 Mapper 配置
     * @param streamId 流标识符，用于日志记录和缓存键。如果为空，方法将直接返回不执行任何操作
     * @param stream 包含要加载为 Mapper 的 XML 内容的输入流，不能为 null
     * @throws IOException 如果发生 I/O 错误或流无法解析为有效的 XML 文档
     */
    public void loadMapper(String streamId, InputStream stream) throws Exception {
        Objects.requireNonNull(stream, "resource '" + streamId + "' stream is null.");
        if (StringUtils.isBlank(streamId)) {
            return;
        }

        Document document = MappingHelper.loadXmlRoot(stream, this.mappingRegistry.getClassLoader());
        this.loadMapper(streamId, document);
    }

    /** load stream */
    public void loadMapper(final String documentId, Document document) throws Exception {
        Objects.requireNonNull(document, "resource '" + documentId + "' document is null.");
        if (StringUtils.isBlank(documentId)) {
            return;
        }

        tryLoaded(documentId, s -> {
            // try load mapping
            this.mappingRegistry.loadMapping(s, document);

            // try load mapper
            try {
                this.tryLoadNode(document.getDocumentElement());
            } catch (ParserConfigurationException | SAXException | ClassNotFoundException e) {
                throw new IOException(e);
            }
        });
    }
    // --------------------------------------------------------------------------------------------

    private void tryLoadNode(Element configRoot) throws Exception {
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

            QueryType queryType = QueryType.valueOfTag(node.getNodeName().toLowerCase().trim());
            if (queryType == null) {
                continue;
            }

            NamedNodeMap nodeAttributes = node.getAttributes();
            String configId = MappingHelper.readAttribute("id", nodeAttributes);
            if (StringUtils.isBlank(configId)) {
                throw new IllegalStateException("the <" + node.getNodeName() + "> tag, id is null.");
            }

            SqlConfig sqlConfig = resolve.parseSqlConfig(configSpace, node);
            if (sqlConfig.getType() == QueryType.Segment) {
                String macroId = StringUtils.isBlank(configSpace) ? configId : (configSpace + "." + configId);
                this.macroRegistry.register(macroId, sqlConfig);
            } else if (sqlConfig.getType() == QueryType.Select) {
                StatementDef def = new StatementDef(configSpace, configId, sqlConfig);
                this.applyResultConfig(def);
                this.configMap.computeIfAbsent(configSpace, s -> new ConcurrentHashMap<>()).put(configId, def);
            } else {
                StatementDef def = new StatementDef(configSpace, configId, sqlConfig);
                this.configMap.computeIfAbsent(configSpace, s -> new ConcurrentHashMap<>()).put(configId, def);
            }
        }
    }

    private void tryLoadMethod(SqlConfigResolve<Method> resolve, Class<?> mapperType, Method method, boolean refXml) throws Exception {
        this.tryLoaded(method, m -> {
            // skip.
            if (m.isDefault()) {
                return;
            }
            if (BaseMapper.class.isAssignableFrom(mapperType)) {
                if (m.getDeclaringClass() == BaseMapper.class || m.getDeclaringClass() == Mapper.class) {
                    return;
                }
            }

            // check conflict
            String configSpace = mapperType.getName();
            String configId = m.getName();
            boolean hasXmlConf = refXml && this.configMap.containsKey(configSpace) && this.configMap.get(configSpace).containsKey(configId);
            boolean hasAnnoConf = matchMethod(m);

            if (hasAnnoConf && hasXmlConf) {
                throw new IllegalStateException("Annotations and mapperFile conflicts with " + configSpace + "." + configId);
            }

            Map<String, StatementDef> defMap = this.configMap.computeIfAbsent(configSpace, s -> new ConcurrentHashMap<>());
            if (hasAnnoConf) {
                StatementDef def = new StatementDef(configSpace, configId, resolve.parseSqlConfig(configSpace, m));

                if (def.getConfig().getType() == QueryType.Segment) {
                    String macroId = configId;//TODO StringUtils.isBlank(configSpace) ? configId : (configSpace + "." + configId);
                    this.macroRegistry.register(macroId, def.getConfig());
                    return;
                }

                def.setUsingCollection(Collection.class.isAssignableFrom(m.getReturnType()));
                this.applyResultConfig(def);// for DqlConfig
                defMap.put(configId, def);
            }

            // supplement (only load from xml try supplement mappingType)
            Class<?> requiredClass = MappingHelper.resolveReturnType(m);
            if (hasXmlConf) {
                StatementDef def = defMap.get(configId);
                if (def.getResultType() == null && def.getResultExtractor() == null && def.getResultRowCallback() == null && def.getResultRowMapper() == null) {
                    if (requiredClass != null) {
                        def.setResultType(requiredClass);
                        def.setResultRowMapper(this.mapperFromType(configSpace, requiredClass));
                    }
                }
            }

            // some check option.
            StatementDef def = defMap.get(configId);
            if (def != null && def.getResultType() != null && requiredClass != null) {
                Class<?> resultType = def.getResultType();
                if (requiredClass == String.class) {
                    // string compatibility is very strong
                } else if (requiredClass.isPrimitive() && !resultType.isPrimitive()) {
                    throw new ClassCastException("the wrapper type '" + resultType.getName() + "' is returned as the primitive type '" + requiredClass.getName() + "' at " + configSpace + "." + configId);
                } else if (requiredClass.isPrimitive() || resultType.isPrimitive()) {
                    Class<?> a = ClassUtils.primitiveToWrapper(requiredClass);
                    Class<?> b = ClassUtils.primitiveToWrapper(resultType);
                    if (a != b) {
                        throw new ClassCastException("the type '" + resultType.getName() + "' cannot be as '" + requiredClass.getName() + "' at " + configSpace + "." + configId);
                    }
                } else if (requiredClass != resultType && !requiredClass.isAssignableFrom(resultType)) {
                    throw new ClassCastException("the type '" + resultType.getName() + "' cannot be as '" + requiredClass.getName() + "' at " + configSpace + "." + configId);
                }
            }
        });
    }

    // config 'resultType/ResultSetExtractor/RowCallbackHandler/RowMapper'
    private void applyResultConfig(StatementDef def) throws Exception {
        if (!(def.getConfig() instanceof DqlConfig)) {
            return;
        }

        String configSpace = ((DqlConfig) def.getConfig()).getResultMapSpace();
        String configId = ((DqlConfig) def.getConfig()).getResultMapId();
        String resultType = ((DqlConfig) def.getConfig()).getResultType();
        String resultSetExtractor = ((DqlConfig) def.getConfig()).getResultSetExtractor();
        String resultRowCallback = ((DqlConfig) def.getConfig()).getResultRowCallback();
        String resultRowMapper = ((DqlConfig) def.getConfig()).getResultRowMapper();
        int hasResultCnt = 0;
        hasResultCnt += (StringUtils.isNotBlank(configId) ? 1 : 0);
        hasResultCnt += (StringUtils.isNotBlank(resultType) ? 1 : 0);
        hasResultCnt += (StringUtils.isNotBlank(resultSetExtractor) ? 1 : 0);
        hasResultCnt += (StringUtils.isNotBlank(resultRowCallback) ? 1 : 0);
        hasResultCnt += (StringUtils.isNotBlank(resultRowMapper) ? 1 : 0);

        if (hasResultCnt > 1) {
            throw new IllegalArgumentException("only one of the options can be selected. e.g., resultMap/resultType/resultSetExtractor/resultRowCallback/resultRowMapper, at mapperId '" + def.toConfigId() + "'.");
        }

        // for ResultSetExtractor
        if (StringUtils.isNotBlank(resultSetExtractor)) {
            Class<?> loadType = MappingHelper.typeMappingOr(resultSetExtractor, this.classLoader::loadClass);
            if (!ResultSetExtractor.class.isAssignableFrom(loadType)) {
                throw new ClassCastException("the type '" + loadType.getName() + "' cannot be as ResultSetExtractor, at mapperId '" + def.toConfigId() + "'.");
            } else {
                def.setResultExtractor(ClassUtils.newInstance(loadType));
            }
        }

        // for RowCallbackHandler
        if (StringUtils.isNotBlank(resultRowCallback)) {
            Class<?> loadType = MappingHelper.typeMappingOr(resultRowCallback, this.classLoader::loadClass);
            if (!RowCallbackHandler.class.isAssignableFrom(loadType)) {
                throw new ClassCastException("the type '" + loadType.getName() + "' cannot be as RowCallbackHandler, at mapperId '" + def.toConfigId() + "'.");
            } else {
                def.setResultRowCallback(ClassUtils.newInstance(loadType));
            }
        }

        // for RowMapper
        if (StringUtils.isNotBlank(resultRowMapper)) {
            Class<?> loadType = MappingHelper.typeMappingOr(resultRowMapper, this.classLoader::loadClass);
            if (!RowMapper.class.isAssignableFrom(loadType)) {
                throw new ClassCastException("the type '" + loadType.getName() + "' cannot be as RowMapper, at mapperId '" + def.toConfigId() + "'.");
            } else {
                def.setResultRowMapper(ClassUtils.newInstance(loadType));
            }
        }

        // for resultType
        if (StringUtils.isNotBlank(resultType)) {
            def.setResultType(MappingHelper.typeMappingOr(resultType, this.classLoader::loadClass));
        }

        if (StringUtils.isNotBlank(configId)) {
            // for resultMap
            TableMapping<?> tabMapping = this.mappingRegistry.findBySpace(configSpace, configId);
            if (tabMapping == null) {
                String fullname = StringUtils.isBlank(configSpace) ? configId : (configSpace + "." + configId);
                throw new IllegalArgumentException("the resultMap '" + fullname + "' cannot be found, at mapperId '" + def.toConfigId() + "'.");
            } else {
                if (def.getResultRowMapper() == null) {
                    def.setResultRowMapper(new BeanMappingRowMapper<>(tabMapping));
                    if (def.getResultType() == null) {
                        def.setResultType(tabMapping.entityType());
                    }
                } else {
                    String fullname = StringUtils.isBlank(configSpace) ? configId : (configSpace + "." + configId);
                    String mapperType = def.getResultRowMapper().getClass().getName();
                    logger.warn("ignore resultMap '" + fullname + "' and use ResultRowMapper '" + mapperType + "', at mapperId '" + def.toConfigId() + "'.");
                }
            }
        } else if (StringUtils.isNotBlank(resultType)) {
            // for resultType
            Class<?> requiredType = MappingHelper.typeMappingOr(resultType, this.classLoader::loadClass);
            if (def.getResultRowMapper() == null) {
                def.setResultRowMapper(this.mapperFromType(configSpace, requiredType));
            } else {
                String mapperType = def.getResultRowMapper().getClass().getName();
                logger.warn("ignore resultType '" + resultType + "' and use ResultRowMapper '" + mapperType + "', at mapperId '" + def.toConfigId() + "'.");
            }
        }
    }

    private RowMapper<?> mapperFromType(String namespace, Class<?> requiredType) throws Exception {
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

                // check class cast
                if (mapping.entityType() == requiredType || mapping.entityType().isAssignableFrom(requiredType)) {
                    return new BeanMappingRowMapper<>(mapping);
                } else {
                    throw new ClassCastException("the type '" + mapping.entityType().getName() + "' cannot be as '" + requiredType.getName() + "'.");
                }
            }
        }

        // as Bean
        if (Map.class.isAssignableFrom(requiredType)) {
            boolean caseInsensitive = MappingHelper.caseInsensitive(this.mappingRegistry.getGlobalOptions());
            return new ColumnMapRowMapper(caseInsensitive, this.typeRegistry) {
                @Override
                protected Map<String, Object> createColumnMap(int columnCount) {
                    if (requiredType == Map.class) {
                        return super.createColumnMap(columnCount);
                    } else {
                        return ClassUtils.newInstance(requiredType);
                    }
                }
            };
        } else {
            return new BeanMappingRowMapper<>(requiredType, this.mappingRegistry);
        }
    }

    // --------------------------------------------------------------------------------------------

    private void tryLoaded(String target, EConsumer<String, Exception> call) throws Exception {
        String fmtTarget = ResourcesUtils.formatResource(target);

        String cacheKey = "RES::" + fmtTarget;
        if (!loaded.contains(cacheKey)) {
            logger.info("loadMapper '" + fmtTarget + "'");
            call.eAccept(fmtTarget);
            this.loaded.add(cacheKey);
        }
    }

    private void tryLoaded(Class<?> target, EConsumer<Class<?>, Exception> call) throws Exception {
        String cacheKey = "TYPE::" + target;
        if (!loaded.contains(cacheKey)) {
            logger.info("loadMapper '" + target.getName() + "'");
            call.eAccept(target);
            this.loaded.add(cacheKey);
        }
    }

    private void tryLoaded(Method target, EConsumer<Method, Exception> call) throws Exception {
        String cacheKey = "METHOD::" + target;
        if (!loaded.contains(cacheKey)) {
            call.eAccept(target);
            this.loaded.add(cacheKey);
        }
    }

    private static void testMapper(Class<?> mapperType) {
        if (!mapperType.isInterface()) {
            throw new UnsupportedOperationException("the '" + mapperType.getName() + "' must interface.");
        }

        boolean testMapper = false;
        java.lang.annotation.Annotation[] annotations = mapperType.getDeclaredAnnotations();
        for (java.lang.annotation.Annotation annotation : annotations) {
            if (annotation instanceof MapperDef || annotation.annotationType().getAnnotation(MapperDef.class) != null) {
                testMapper = true;
                break;
            }
        }
        if (!testMapper) {
            testMapper = Mapper.class.isAssignableFrom(mapperType);
        }

        if (!testMapper) {
            throw new UnsupportedOperationException("type '" + mapperType.getName() + "' need @RefMapper or @SimpleMapper or @DalMapper");
        }
    }

    private static boolean matchType(Class<?> dalType) {
        if (!dalType.isInterface()) {
            return false;
        }
        Method[] dalTypeMethods = dalType.getMethods();
        for (Method method : dalTypeMethods) {
            if (matchMethod(method)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchMethod(Method dalMethod) {
        if (dalMethod.getDeclaringClass() == Object.class) {
            return false;
        }
        for (Annotation anno : dalMethod.getAnnotations()) {
            if (ClassSqlConfigResolve.matchAnnotation(anno)) {
                return true;
            }
        }
        return false;
    }

    private SqlConfigResolve<Method> getMethodDynamicResolve() {
        return new ClassSqlConfigResolve();
    }

    private SqlConfigResolve<Node> getXmlDynamicResolve() {
        return new XmlSqlConfigResolve();
    }
}
