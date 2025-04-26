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
package net.hasor.dbvisitor.mapping;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.EConsumer;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.mapping.def.TableDef;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.resolve.ClassTableMappingResolve;
import net.hasor.dbvisitor.mapping.resolve.XmlTableMappingResolve;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TableMappingResolve 的公共方法
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-21
 */
public class MappingRegistry {
    private static final Logger                                                              logger      = LoggerFactory.getLogger(MappingRegistry.class);
    public static final  MappingRegistry                                                     DEFAULT     = new MappingRegistry(null);
    private final        Map<String, Map<String, Map<String, Map<String, TableMapping<?>>>>> mapForLevel = new ConcurrentHashMap<>();
    private final        Map<String, Map<String, TableMapping<?>>>                           mapForSpace = new ConcurrentHashMap<>();
    protected final      ClassLoader                                                         classLoader;
    protected final      TypeHandlerRegistry                                                 typeRegistry;
    protected final      Options                                                             global;
    private final        XmlTableMappingResolve                                              xmlMappingResolve;
    private final        ClassTableMappingResolve                                            entityClassResolve;
    protected final      Set<String>                                                         loaded;

    /**
     * 默认构造方法，使用系统类加载器、默认类型处理器注册表和空配置选项
     */
    public MappingRegistry() {
        this(null, TypeHandlerRegistry.DEFAULT, Options.of());
    }

    /**
     * 使用指定类加载器构造，使用默认类型处理器注册表和空配置选项
     * @param classLoader 自定义类加载器，如果为null则使用系统类加载器
     */
    public MappingRegistry(ClassLoader classLoader) {
        this(classLoader, TypeHandlerRegistry.DEFAULT, Options.of());
    }

    /**
     * 使用指定类加载器和全局配置构造，使用默认类型处理器注册表
     * @param classLoader 自定义类加载器，如果为null则使用系统类加载器
     * @param global 全局配置选项
     */
    public MappingRegistry(ClassLoader classLoader, Options global) {
        this(classLoader, TypeHandlerRegistry.DEFAULT, global);
    }

    /**
     * 使用指定类加载器、类型处理器注册表和全局配置
     * @param classLoader 自定义类加载器，如果为null则使用系统类加载器
     * @param typeRegistry 类型处理器注册表，如果为null则使用默认注册表
     * @param global 全局配置选项，如果为null则使用空配置
     */
    public MappingRegistry(ClassLoader classLoader, TypeHandlerRegistry typeRegistry, Options global) {
        this.classLoader = classLoader != null ? classLoader : MappingRegistry.class.getClassLoader();
        this.typeRegistry = (typeRegistry == null) ? TypeHandlerRegistry.DEFAULT : typeRegistry;
        this.global = (global == null) ? Options.of() : global;
        this.xmlMappingResolve = new XmlTableMappingResolve();
        this.entityClassResolve = new ClassTableMappingResolve();
        this.loaded = new HashSet<>();
    }

    /**
     * 检查指定类是否是实体类（是否标记了@Table注解）
     * @param testClass 要检查的类
     * @param <T> 类类型
     * @return 如果是实体类返回true，否则返回false
     */
    public static <T> boolean isEntity(Class<T> testClass) {
        return testClass.isAnnotationPresent(Table.class);
    }

    /** 获取当前使用的类加载器 */
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /** 获取当前使用的类型处理器注册表 */
    public TypeHandlerRegistry getTypeRegistry() {
        return this.typeRegistry;
    }

    /** 获取全局配置选项 */
    public Options getGlobalOptions() {
        return this.global;
    }

    /** load `mapper.xml` and escape decoding is not used */
    public void loadMapping(final String resource) throws IOException {
        this.loadMapping(resource, false);
    }

    /** load `mapper.xml` */
    public void loadMapping(final String resource, boolean escape) throws IOException {
        if (StringUtils.isBlank(resource)) {
            return;
        }

        String name = escape ? StringUtils.escapeDecode(resource) : resource;
        if (name.startsWith("/")) {
            name = name.substring(1);
        }

        tryLoaded(name, s -> {
            try (InputStream stream = ResourcesUtils.getResourceAsStream(this.classLoader, s)) {
                Objects.requireNonNull(stream, "resource '" + s + "' is not exist.");
                try {
                    Document document = MappingHelper.loadXmlRoot(stream, getClassLoader());
                    Element root = document.getDocumentElement();
                    NamedNodeMap rootAttributes = root.getAttributes();

                    String namespace = MappingHelper.readAttribute("namespace", rootAttributes);
                    this.loadMapping(namespace, root);
                } catch (ParserConfigurationException | SAXException | ReflectiveOperationException e) {
                    throw new IOException(e);
                }
            }
        });
    }

    protected void loadMapping(String space, Element configRoot) throws IOException, ReflectiveOperationException {
        Options optInfile = this.xmlMappingResolve.fromXmlNode(configRoot.getAttributes(), this.global);

        NodeList childNodes = configRoot.getChildNodes();
        for (int i = 0, len = childNodes.getLength(); i < len; i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String elementName = node.getNodeName();
            boolean isResultMap = StringUtils.equalsIgnoreCase("resultMap", elementName);
            boolean isEntity = StringUtils.equalsIgnoreCase("entity", elementName);
            if (!isResultMap && !isEntity) {
                continue;
            }

            NamedNodeMap nodeAttributes = node.getAttributes();
            Node typeNode = nodeAttributes.getNamedItem("type");
            String typeString = (typeNode != null) ? typeNode.getNodeValue() : null;
            if (StringUtils.isBlank(typeString)) {
                throw new IOException("the <" + (isResultMap ? "resultMap" : "entity") + "> tag, type is null.");
            }

            Node idNode = nodeAttributes.getNamedItem("id");
            String idStr = (idNode != null) ? idNode.getNodeValue() : null;
            if (StringUtils.isBlank(idStr)) {
                idStr = typeString;
            }

            TableDef<?> def = this.xmlMappingResolve.resolveTableMapping(node, optInfile, this);
            this.saveDefToSpace(space, idStr, def, isEntity);
        }
    }

    /** load entity, optional annotation @Table. (space = "", name = classFullName) */
    public <T> TableMapping<T> loadEntityToSpace(Class<T> entityType) {
        return this.loadEntityToSpace(entityType, "", entityType.getName());
    }

    /** load entity, optional annotation @Table. (space = argument, name = classFullName) */
    public <T> TableMapping<T> loadEntityToSpace(Class<T> entityType, String space) {
        space = StringUtils.isBlank(space) ? "" : space;
        return this.loadEntityToSpace(entityType, space, entityType.getName());
    }

    /** load entity, optional annotation @Table. (space = argument, name = argument) */
    public <T> TableMapping<T> loadEntityToSpace(Class<T> entityType, String space, String name) {
        if (entityType == null) {
            throw new IllegalArgumentException("entityType is null.");
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name is empty.");
        }

        try {
            TableDef<T> def = this.entityClassResolve.resolveTableMapping(entityType, this.global, this);

            return this.saveDefToSpace(space, name, def, true);
        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /** load entity and override table info with argument, optional annotation @Table. (space = "", name = classFullName) */
    public <T> TableMapping<T> loadEntityAsTable(Class<T> entityType, String table) {
        if (entityType == null) {
            throw new IllegalArgumentException("entityType is null.");
        }
        if (StringUtils.isBlank(table)) {
            throw new IllegalArgumentException("loadEntity '" + entityType.getName() + "' missing table name.");
        }

        try {
            TableDef<T> def = this.entityClassResolve.resolveTableMapping(entityType, this.global, this);
            def.setTable(table);

            return this.saveDefToSpace("", entityType.getName(), def, true);
        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /** load entity and override table info with argument, optional annotation @Table. (space = "", name = classFullName) */
    public <T> TableMapping<T> loadEntityAsTable(Class<T> entityType, String catalog, String schema, String table) {
        if (entityType == null) {
            throw new IllegalArgumentException("entityType is null.");
        }
        if (StringUtils.isBlank(table)) {
            throw new IllegalArgumentException("loadEntity '" + entityType.getName() + "' missing table name.");
        }

        try {
            TableDef<T> def = this.entityClassResolve.resolveTableMapping(entityType, this.global, this);
            def.setCatalog(catalog);
            def.setSchema(schema);
            def.setTable(table);

            return this.saveDefToSpace("", entityType.getName(), def, true);
        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /** load resultMap, optional annotation @ResultMap. (when @ResultMap does not exist then, space = "" , name = classFullName) */
    public <T> TableMapping<T> loadResultMapToSpace(Class<T> resultType) {
        if (resultType == null) {
            throw new IllegalArgumentException("resultType is null.");
        }

        try {
            TableDef<T> def = this.entityClassResolve.resolveTableMapping(resultType, this.global, this);
            MappingHelper.NameInfo nameInfo = MappingHelper.findNameInfo(resultType);
            return this.saveDefToSpace(nameInfo.getSpace(), nameInfo.getName(), def, false);
        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /** load resultMap, override annotation @ResultMap if present. (space = argument , name = argument) */
    public <T> TableMapping<T> loadResultMapToSpace(Class<T> resultType, String space, String name) {
        if (resultType == null) {
            throw new IllegalArgumentException("resultType is null.");
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name is empty.");
        }

        try {
            TableDef<T> def = this.entityClassResolve.resolveTableMapping(resultType, this.global, this);

            return this.saveDefToSpace(space, name, def, false);
        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> TableMapping<T> saveDefToSpace(String space, String name, TableDef<?> def, boolean asTable) {
        space = StringUtils.isBlank(space) ? "" : space;

        Map<String, TableMapping<?>> typeMap = this.mapForSpace.computeIfAbsent(space, s -> new ConcurrentHashMap<>());
        if (typeMap.containsKey(name)) {
            TableMapping<?> tableMapping = typeMap.get(name);
            if (tableMapping != null) {
                String tag = asTable ? "entity" : "resultMap";
                String fullname = StringUtils.isBlank(space) ? name : (space + "." + name);
                throw new IllegalStateException("the " + tag + " '" + fullname + "' already exists.");
            }
        }

        if (asTable) {
            String catalog = def.getCatalog();
            String schema = def.getSchema();
            String table = def.getTable();
            catalog = StringUtils.isNotBlank(catalog) ? catalog : "";
            schema = StringUtils.isNotBlank(schema) ? schema : "";
            table = StringUtils.isNotBlank(table) ? table : "";

            if (StringUtils.isBlank(table)) {
                Class<?> entityType = def.entityType();
                if (def.isMapUnderscoreToCamelCase()) {
                    def.setTable(StringUtils.humpToLine(entityType.getSimpleName()));
                } else {
                    def.setTable(entityType.getSimpleName());
                }
                table = def.getTable();
            }

            Map<String, Map<String, Map<String, TableMapping<?>>>> schemaMap = this.mapForLevel.computeIfAbsent(catalog, s -> new ConcurrentHashMap<>());
            Map<String, Map<String, TableMapping<?>>> tableMap = schemaMap.computeIfAbsent(schema, s -> new ConcurrentHashMap<>());
            Map<String, TableMapping<?>> valuesMap = tableMap.computeIfAbsent(table, s -> new ConcurrentHashMap<>());

            if (valuesMap.containsKey(name)) {
                TableMapping<?> mapping = valuesMap.get(name);
                if (mapping != null) {
                    StringBuilder fullTable = new StringBuilder();
                    fullTable.insert(0, table);
                    if (StringUtils.isNotBlank(schema)) {
                        fullTable.insert(0, schema + ".");
                    }
                    if (StringUtils.isNotBlank(catalog)) {
                        fullTable.insert(0, catalog + ".");
                    }

                    throw new IllegalStateException("the entity of table '" + fullTable + "' of name '" + name + "' already exists.");
                }
            }

            this.mapForSpace.get(space).put(name, def);
            this.mapForLevel.get(catalog).get(schema).get(table).put(name, def);
        } else {
            def.setCatalog("");
            def.setSchema("");
            def.setTable("");
            this.mapForSpace.get(space).put(name, def);
        }

        return (TableMapping<T>) def;
    }

    /**
     * 根据实体类在默认命名空间("")中查找映射配置
     * @param entityType 要查找的实体类类型
     * @param <T> 返回的映射类型
     * @return 表映射配置，如果未找到则返回null
     */
    public <T> TableMapping<T> findByEntity(Class<?> entityType) {
        return this.findBySpace("", entityType.getName());
    }

    /**
     * 根据命名空间和实体类查找映射配置
     * @param space 命名空间
     * @param entityType 实体类类型
     * @param <T> 返回的映射类型
     * @return 表映射配置，未找到返回null
     */
    public <T> TableMapping<T> findBySpace(String space, Class<?> entityType) {
        return this.findBySpace(space, entityType.getName());
    }

    /**
     * 根据命名空间和映射名称查找映射配置
     * @param space 命名空间
     * @param name 映射名称(通常是类全名)
     * @param <T> 返回的映射类型
     * @return 表映射配置，未找到返回null
     */
    public <T> TableMapping<T> findBySpace(String space, String name) {
        String findSpace = StringUtils.isBlank(space) ? "" : space;

        if (this.mapForSpace.containsKey(findSpace)) {
            Map<String, TableMapping<?>> map = this.mapForSpace.get(findSpace);
            return (TableMapping<T>) map.get(name);
        } else {
            return null;
        }
    }

    /**
     * 根据表名查找映射配置（使用默认catalog和schema）
     * @param table 表名
     * @param <T> 返回的映射类型
     * @return 表映射配置，如果不存在则返回null
     */
    public <T> TableMapping<T> findByTable(String table) {
        return this.findByTable(null, null, table, null);
    }

    /**
     * 根据catalog、schema和表名查找映射配置
     * @param catalog catalog名称
     * @param schema schema名称
     * @param table 表名
     * @param <T> 返回的映射类型
     * @return 表映射配置，如果不存在则返回null
     */
    public <T> TableMapping<T> findByTable(String catalog, String schema, String table) {
        return this.findByTable(catalog, schema, table, null);
    }

    /**
     * 根据catalog、schema、表名和指定名称查找映射配置
     * @param catalog catalog名称
     * @param schema schema名称
     * @param table 表名
     * @param specifyName 指定的映射名称（当表有多个映射配置时需要指定）
     * @param <T> 返回的映射类型
     * @return 表映射配置，如果不存在则返回null
     * @throws IllegalStateException 当表有多个映射配置但未指定名称时抛出
     */
    public <T> TableMapping<T> findByTable(String catalog, String schema, String table, String specifyName) {
        catalog = StringUtils.isNotBlank(catalog) ? catalog : "";
        schema = StringUtils.isNotBlank(schema) ? schema : "";
        table = StringUtils.isNotBlank(table) ? table : "";

        Map<String, Map<String, Map<String, TableMapping<?>>>> schemaMap = this.mapForLevel.get(catalog);
        if (schemaMap != null) {
            Map<String, Map<String, TableMapping<?>>> tableMap = schemaMap.get(schema);
            if (tableMap != null) {
                Map<String, TableMapping<?>> values = tableMap.get(table);
                if (specifyName != null) {
                    return (TableMapping<T>) values.get(specifyName);
                } else {
                    if (values.size() == 1) {
                        return (TableMapping<T>) values.values().toArray()[0];
                    } else {
                        throw new IllegalStateException("table has multiple definitions, please specify a name");
                    }
                }
            }
        }

        return null;
    }

    // --------------------------------------------------------------------------------------------

    private void tryLoaded(String target, EConsumer<String, IOException> call) throws IOException {
        String cacheKey = "RES::" + target;
        if (!loaded.contains(cacheKey)) {
            logger.info("loadMapping '" + target + "'");
            call.eAccept(target);
            this.loaded.add(cacheKey);
        }
    }
}
