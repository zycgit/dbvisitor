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
import net.hasor.cobble.reflect.resolvable.ResolvableType;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.mapper.def.SqlConfig;
import net.hasor.dbvisitor.mapper.resolve.ClassSqlConfigResolve;
import net.hasor.dbvisitor.mapper.resolve.SqlConfigResolve;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapper 配置中心
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-05
 */
public class MapperRegistry {
    public static final MapperRegistry                      DEFAULT   = new MapperRegistry(MappingRegistry.DEFAULT, TypeHandlerRegistry.DEFAULT);
    private final       Map<String, Map<String, SqlConfig>> configMap = new ConcurrentHashMap<>();
    private final       MappingRegistry                     mappingRegistry;
    private final       TypeHandlerRegistry                 typeRegistry;

    public MapperRegistry(MappingRegistry mappingRegistry, TypeHandlerRegistry typeRegistry) {
        this.mappingRegistry = mappingRegistry;
        this.typeRegistry = typeRegistry;
    }

    public void loadMapper(Class<?> mapperType) throws IOException, ReflectiveOperationException {
        if (!mapperType.isInterface()) {
            throw new UnsupportedOperationException("the '" + mapperType.getName() + "' must interface.");
        }
        String namespace = mapperType.getName();
        boolean simpleMapper = false;

        Annotation[] annotations = mapperType.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof DalMapper || annotation.annotationType().getAnnotation(DalMapper.class) != null) {
                simpleMapper = true;
                break;
            }
        }

        if (!simpleMapper) {
            throw new UnsupportedOperationException("type '" + mapperType.getName() + "' need @RefMapper or @SimpleMapper or @DalMapper");
        }

        RefMapper refMapper = mapperType.getAnnotation(RefMapper.class);
        if (refMapper != null) {
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
                //                try (InputStream stream = this.classLoader.getResourceAsStream(resource)) {
                //                    if (stream == null) {
                //                        return;//throw new FileNotFoundException("not found mapper file '" + resource + "'"); // Don't block the app from launching
                //                    }
                //
                //                    Document document = loadXmlRoot(stream);
                //                    Element root = document.getDocumentElement();
                //
                //                    //this.loadedResource.add(resource);
                //                    this.loadMapper(namespace, root);
                //                    this.loadDynamic(namespace, root);
                //                } catch (ParserConfigurationException | SAXException e) {
                //                    throw new IOException(e);
                //                }
            }
        }

        Method[] dalTypeMethods = mapperType.getMethods();
        SqlConfigResolve<Method> resolve = getMethodDynamicResolve();
        for (Method method : dalTypeMethods) {
            if (!ClassSqlConfigResolve.matchMethod(method)) {
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
            //            if (resultType != null && findTableReader(namespace, resultType.getName()) == null) {
            //                this.asResultMap(namespace, resultType);
            //            }

            String identify = method.getName();
            DynamicSql dynamicSql = resolve.parseSqlConfig(method);

            if (dynamicSql != null) {
                //                saveDynamic(namespace, identify, dynamicSql);
            }
        }

        if (BaseMapper.class.isAssignableFrom(mapperType)) {
            ResolvableType type = ResolvableType.forClass(mapperType).as(BaseMapper.class);
            Class<?>[] generics = type.resolveGenerics(Object.class);
            Class<?> entityType = generics[0];
            entityType = (entityType == Object.class) ? null : entityType;
            //            if (entityType != null && findByEntity(entityType) == null) {
            //                this.loadEntityToSpace(entityType);
            //            }
        }
    }
    // --------------------------------------------------------------------------------------------

    protected SqlConfigResolve<Method> getMethodDynamicResolve() {
        return new ClassSqlConfigResolve();
    }

    //    protected DynamicResolve<Node> getXmlDynamicResolve() {
    //        return new XmlDynamicResolve();
    //    }
}
