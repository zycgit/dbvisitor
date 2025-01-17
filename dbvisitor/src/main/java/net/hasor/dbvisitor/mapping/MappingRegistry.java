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
 * @version : 2021-06-21
 */
public class MappingRegistry {
    private static final Logger                                                              logger      = LoggerFactory.getLogger(MappingRegistry.class);
    public static final  MappingRegistry                                                     DEFAULT     = new MappingRegistry();
    private final        Map<String, Map<String, Map<String, Map<String, TableMapping<?>>>>> mapForLevel = new ConcurrentHashMap<>();
    private final        Map<String, Map<String, TableMapping<?>>>                           mapForSpace = new ConcurrentHashMap<>();
    protected final      ClassLoader                                                         classLoader;
    protected final      TypeHandlerRegistry                                                 typeRegistry;
    protected final      MappingOptions                                                      global;
    private final        XmlTableMappingResolve                                              xmlMappingResolve;
    private final        ClassTableMappingResolve                                            entityClassResolve;
    protected final      Set<String>                                                         loaded;

    public MappingRegistry() {
        this(null, TypeHandlerRegistry.DEFAULT, MappingOptions.buildNew());
    }

    public MappingRegistry(ClassLoader classLoader) {
        this(classLoader, TypeHandlerRegistry.DEFAULT, MappingOptions.buildNew());
    }

    public MappingRegistry(ClassLoader classLoader, TypeHandlerRegistry registry) {
        this(classLoader, registry, MappingOptions.buildNew());
        Objects.requireNonNull(registry, "registry is null.");
    }

    public MappingRegistry(ClassLoader classLoader, TypeHandlerRegistry typeRegistry, MappingOptions global) {
        this.classLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        this.typeRegistry = (typeRegistry == null) ? TypeHandlerRegistry.DEFAULT : typeRegistry;
        this.global = global;
        this.xmlMappingResolve = new XmlTableMappingResolve();
        this.entityClassResolve = new ClassTableMappingResolve();
        this.loaded = new HashSet<>();
    }

    public static <T> boolean isEntity(Class<T> testClass) {
        return testClass.isAnnotationPresent(Table.class);
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public TypeHandlerRegistry getTypeRegistry() {
        return this.typeRegistry;
    }

    public MappingOptions getGlobalOptions() {
        return this.global;
    }

    /** load `mapper.xml` and escape decoding is not used */
    public void loadMapper(final String resource) throws IOException {
        this.loadMapper(resource, false);
    }

    /** load `mapper.xml` */
    public void loadMapper(final String resource, boolean escape) throws IOException {
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
                    this.loadMapper(namespace, root);
                } catch (ParserConfigurationException | SAXException | ReflectiveOperationException e) {
                    throw new IOException(e);
                }
            }
        });
    }

    protected void loadMapper(String space, Element configRoot) throws IOException, ReflectiveOperationException {
        MappingOptions optInfile = this.xmlMappingResolve.fromXmlNode(configRoot.getAttributes(), this.global);

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

            TableDef<?> def = this.xmlMappingResolve.resolveTableMapping(node, optInfile, this.classLoader, this.typeRegistry);
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
            TableDef<T> def = this.entityClassResolve.resolveTableMapping(entityType, this.global, this.classLoader, this.typeRegistry);

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
            TableDef<T> def = this.entityClassResolve.resolveTableMapping(entityType, this.global, this.classLoader, this.typeRegistry);
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
            TableDef<T> def = this.entityClassResolve.resolveTableMapping(entityType, this.global, this.classLoader, this.typeRegistry);
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
            TableDef<T> def = this.entityClassResolve.resolveTableMapping(resultType, this.global, this.classLoader, this.typeRegistry);
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
            TableDef<T> def = this.entityClassResolve.resolveTableMapping(resultType, this.global, this.classLoader, this.typeRegistry);

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

    public <T> TableMapping<T> findByEntity(Class<?> entityType) {
        return this.findBySpace("", entityType.getName());
    }

    public <T> TableMapping<T> findBySpace(String space, Class<?> entityType) {
        return this.findBySpace(space, entityType.getName());
    }

    public <T> TableMapping<T> findBySpace(String space, String name) {
        String findSpace = StringUtils.isBlank(space) ? "" : space;

        if (this.mapForSpace.containsKey(findSpace)) {
            Map<String, TableMapping<?>> map = this.mapForSpace.get(findSpace);
            return (TableMapping<T>) map.get(name);
        } else {
            return null;
        }
    }

    /** find Entity using (catalog = "", schema = "", table = argument, specifyName = ""), specifyName */
    public <T> TableMapping<T> findByTable(String table) {
        return this.findByTable(null, null, table, null);
    }

    public <T> TableMapping<T> findByTable(String catalog, String schema, String table) {
        return this.findByTable(catalog, schema, table, null);
    }

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
