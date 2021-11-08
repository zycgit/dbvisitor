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
package net.hasor.db.dal.session;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.BeanMap;
import net.hasor.db.dal.dynamic.DynamicSql;
import net.hasor.db.dal.execute.ExecuteProxy;
import net.hasor.db.dal.repository.DalRegistry;
import net.hasor.db.dal.repository.Param;
import net.hasor.db.dialect.PageSqlDialect;
import net.hasor.db.jdbc.ConnectionCallback;
import net.hasor.db.page.Page;
import net.hasor.db.page.PageResult;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;

class ExecuteInvocationHandler implements InvocationHandler {
    private final String                            space;
    private final DalSession                        dalSession;
    private final Map<String, ExecuteProxy>         dynamicSqlMap = new HashMap<>();
    private final Map<String, Integer>              pageInfoMap   = new HashMap<>();
    private final Map<String, Map<String, Integer>> argNamesMap   = new HashMap<>();
    private final BaseMapperHandler                 mapperHandler;

    public ExecuteInvocationHandler(DalSession dalSession, Class<?> dalType, DalRegistry dalRegistry, BaseMapperHandler mapperHandler) {
        this.space = dalType.getName();
        this.dalSession = dalSession;
        this.initDynamicSqlMap(dalType, dalRegistry);
        this.mapperHandler = mapperHandler;
    }

    private void initDynamicSqlMap(Class<?> dalType, DalRegistry dalRegistry) {
        for (Method method : dalType.getMethods()) {
            if (method.getDeclaringClass() == BaseMapper.class || method.getDeclaringClass() == Object.class) {
                continue;
            }

            String dynamicId = method.getName();
            DynamicSql parseXml = dalRegistry.findDynamicSql(this.space, dynamicId);
            if (parseXml == null) {
                continue;
            }

            this.dynamicSqlMap.put(dynamicId, new ExecuteProxy(dynamicId, dalRegistry.createContext(this.space)));
            this.pageInfoMap.put(dynamicId, -1);
            Map<String, Integer> argNames = this.argNamesMap.computeIfAbsent(dynamicId, s -> new HashMap<>());

            int parameterCount = method.getParameterCount();
            Annotation[][] annotations = method.getParameterAnnotations();
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

                if (Page.class.isAssignableFrom(method.getParameterTypes()[i])) {
                    this.pageInfoMap.put(dynamicId, i);
                }

            }
        }
    }

    protected Page extractPage(String dynamicId, Object[] objects) {
        Integer integer = this.pageInfoMap.get(dynamicId);
        if (integer < 0) {
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
        argNames.forEach((key, idx) -> {
            argMap.put(key, objects[idx]);
        });
        mergedMap.appendMap(argMap, false);

        if (objects.length == 1) {
            if (objects[0] instanceof Map) {
                mergedMap.appendMap((Map<? extends String, ?>) objects[0], false);
            } else if (!(objects[0] instanceof Collection)) {
                mergedMap.appendMap(new BeanMap(objects[0]), true);
            }
        }

        return mergedMap;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if (this.mapperHandler != null && method.getDeclaringClass() == BaseMapper.class) {
            return method.invoke(this.mapperHandler, objects);
        }

        String dynamicId = method.getName();
        Page page = extractPage(dynamicId, objects);
        boolean pageResult = method.getReturnType() == PageResult.class;
        Map<String, Object> data = extractData(dynamicId, objects);

        final ExecuteProxy execute = this.dynamicSqlMap.get(dynamicId);
        PageSqlDialect dialect = this.dalSession.getDialect();
        Object result = this.dalSession.lambdaTemplate().execute((ConnectionCallback<Object>) con -> {
            return execute.execute(con, data, page, pageResult, dialect);
        });

        Class<?> returnType = method.getReturnType();
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
}