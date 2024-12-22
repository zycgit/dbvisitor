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
package net.hasor.dbvisitor.dal.session;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.convert.ConverterBean;
import net.hasor.cobble.ref.BeanMap;
import net.hasor.dbvisitor.dal.MapperRegistry;
import net.hasor.dbvisitor.dal.execute.ExecuteProxy;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageResult;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;

/**
 * Mapper 代理接口类
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-10-30
 */
class ExecuteInvocationHandler implements InvocationHandler {
    private final String                            space;
    private final DalSession                        dalSession;
    private final Map<String, ExecuteProxy>         dynamicSqlMap = new HashMap<>();
    private final Map<String, Integer>              pageInfoMap   = new HashMap<>();
    private final Map<String, Map<String, Integer>> argNamesMap   = new HashMap<>();
    private final BaseMapperHandler                 mapperHandler;

    public ExecuteInvocationHandler(DalSession dalSession, Class<?> dalType, MapperRegistry dalRegistry, BaseMapperHandler mapperHandler) {
        this.space = dalType.getName();
        this.dalSession = dalSession;
        this.initDynamicSqlMap(dalType, dalRegistry);
        this.mapperHandler = mapperHandler;
    }

    private void initDynamicSqlMap(Class<?> dalType, MapperRegistry dalRegistry) {
        for (Method method : dalType.getMethods()) {
            if (method.getDeclaringClass() == BaseMapper.class || method.getDeclaringClass() == Object.class) {
                continue;
            }

            String dynamicId = method.getName();
            DynamicSql parseXml = dalRegistry.findDynamicSql(this.space, dynamicId);
            if (parseXml == null) {
                continue;
            }

            DalContext context = new DalContext(this.space, dalRegistry);
            this.dynamicSqlMap.put(dynamicId, new ExecuteProxy(dynamicId, context));
            Map<String, Integer> argNames = this.argNamesMap.computeIfAbsent(dynamicId, s -> new HashMap<>());

            int parameterCount = method.getParameterCount();
            Annotation[][] annotations = method.getParameterAnnotations();
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterCount; i++) {
                String fixedName = "arg" + i;
                argNames.put(fixedName, i);

                String name = method.getParameters()[i].getName();
                if (!argNames.containsKey(name)) {
                    argNames.put(name, i);
                }

                for (Annotation paramAnno : annotations[i]) {
                    if (!(paramAnno instanceof Param)) {
                        continue;
                    }

                    String paramName = ((Param) paramAnno).value();
                    if (StringUtils.isBlank(paramName)) {
                        continue;
                    }

                    if (!argNames.containsKey(paramName)) {
                        argNames.put(paramName, i);
                    }
                }

                if (Page.class.isAssignableFrom(parameterTypes[i])) {
                    this.pageInfoMap.put(dynamicId, i);
                }
            }
        }
    }

    protected Page extractPage(String dynamicId, Object[] objects) {
        Integer integer = this.pageInfoMap.get(dynamicId);
        if (integer == null || integer < 0) {
            return null;
        } else {
            return (Page) objects[integer];
        }
    }

    protected Map<String, Object> extractData(String dynamicId, Object[] objects) {
        if (objects == null || objects.length == 0) {
            return new HashMap<>();
        }

        Map<String, Integer> argNames = this.argNamesMap.get(dynamicId);
        MergedMap<String, Object> mergedMap = new MergedMap<>();

        Map<String, Object> argMap = new HashMap<>();
        argNames.forEach((key, idx) -> argMap.put(key, objects[idx]));
        mergedMap.appendMap(argMap, false);

        if (objects.length == 1) {
            if (objects[0] instanceof Map) {
                mergedMap.appendMap((Map<? extends String, ?>) objects[0], true);
            } else if (!(objects[0] instanceof Collection)) {
                BeanMap beanMap = new BeanMap(objects[0]);
                beanMap.setTransformConvert(ConverterBean.getInstance());
                mergedMap.appendMap(beanMap, true);
            }
        }

        return mergedMap;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if (method.getName().equals("toString") && method.getParameterCount() == 0) {
            return "Mapper Proxy " + this.space + " [" + this.dalSession + "]";
        }
        if (this.mapperHandler != null && method.getDeclaringClass() == BaseMapper.class) {
            return method.invoke(this.mapperHandler, objects);
        }
        String dynamicId = method.getName();
        final ExecuteProxy execute = this.dynamicSqlMap.get(dynamicId);
        if (execute != null) {
            // use xml mapper
            Object result = executeByMapper(dynamicId, execute, method, objects);
            return processResult(result, method.getReturnType());
        } else if (method.isDefault()) {
            // use interface default method
            MethodHandle handle;
            if (privateLookupInMethod == null) {
                handle = getMethodHandleJava8(method);
            } else {
                handle = getMethodHandleJava9(method);
            }
            return handle.bindTo(o).invokeWithArguments(objects);
        } else {
            throw new NoSuchMethodException("method '" + method.getDeclaringClass().getName() + "." + method.getName() + "' does not exist in mapper.");
        }
    }

    private Object executeByMapper(String dynamicId, ExecuteProxy execute, Method method, Object[] objects) throws SQLException {
        Page page = extractPage(dynamicId, objects);
        boolean pageResult = method.getReturnType() == PageResult.class;
        Map<String, Object> data = extractData(dynamicId, objects);

        PageSqlDialect dialect = this.dalSession.getDialect();
        return this.dalSession.lambdaTemplate().getJdbc().execute((ConnectionCallback<Object>) con -> {
            return execute.execute(con, data, page, pageResult, dialect);
        });
    }

    private Object processResult(Object result, Class<?> returnType) throws SQLException {
        if (List.class == returnType || Collection.class == returnType || Iterable.class == returnType) {
            if (result instanceof List) {
                return result;
            } else {
                List<Object> list = new ArrayList<>();
                list.add(result);
                return list;
            }
        } else if (Map.class == returnType) {
            if (result instanceof Map) {
                return result;
            } else if (result instanceof Iterable) {
                Map<String, Object> map = new HashMap<>();
                int i = 0;
                for (Object obj : (Iterable) result) {
                    map.put("result-" + (i++), obj);
                }
                return map;
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("result", result);
                return map;
            }
        } else {
            if (result instanceof List) {
                int nrOfColumns = ((List<?>) result).size();
                if (nrOfColumns > 1) {
                    throw new SQLException("Incorrect row count: expected 1, actual " + nrOfColumns);
                } else {
                    return ((List<?>) result).get(0);
                }
            } else {
                return result;
            }
        }
    }

    //
    //
    //
    private static final Constructor<MethodHandles.Lookup> lookupConstructor;
    private static final Method                            privateLookupInMethod;
    private static final int                               ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;

    static {
        Method privateLookupIn;
        try {
            privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
        } catch (NoSuchMethodException e) {
            privateLookupIn = null;
        }
        privateLookupInMethod = privateLookupIn;

        Constructor<MethodHandles.Lookup> lookup = null;
        if (privateLookupInMethod == null) {
            // JDK 1.8
            try {
                lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                lookup.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("There is neither 'privateLookupIn(Class, Lookup)' nor 'Lookup(Class, int)' method in java.lang.invoke.MethodHandles.", e);
            } catch (Throwable t) {
                lookup = null;
            }
        }
        lookupConstructor = lookup;
    }

    private static MethodHandle getMethodHandleJava9(Method method) throws ReflectiveOperationException {
        final Class<?> declaringClass = method.getDeclaringClass();
        MethodHandles.Lookup lookup = ((MethodHandles.Lookup) privateLookupInMethod.invoke(null, declaringClass, MethodHandles.lookup()));
        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        return lookup.findSpecial(declaringClass, method.getName(), methodType, declaringClass);
    }

    private static MethodHandle getMethodHandleJava8(Method method) throws ReflectiveOperationException {
        final Class<?> declaringClass = method.getDeclaringClass();
        return lookupConstructor.newInstance(declaringClass, ALLOWED_MODES).unreflectSpecial(method, declaringClass);
    }
}