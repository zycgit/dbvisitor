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
package net.hasor.db.dal.execute;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.EFunction;
import net.hasor.cobble.ref.BeanMap;
import net.hasor.db.dal.dynamic.DynamicSql;
import net.hasor.db.dal.repository.Param;
import net.hasor.db.dal.repository.manager.DalRegistry;
import net.hasor.db.dialect.Page;
import net.hasor.db.jdbc.core.JdbcAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

class RepositoryInvocationHandler extends JdbcAccessor implements InvocationHandler {
    private static final Logger                            logger        = LoggerFactory.getLogger(RepositoryInvocationHandler.class);
    private final        String                            space;
    private final        Map<String, ExecuteProxy>         dynamicSqlMap = new HashMap<>();
    private final        Map<String, Integer>              pageInfoMap   = new HashMap<>();
    private final        Map<String, Map<String, Integer>> argNamesMap   = new HashMap<>();

    public RepositoryInvocationHandler(final DataSource dataSource, Class<?> dalType, DalRegistry dalRegistry) {
        this.space = dalType.getName();
        this.setDataSource(dataSource);
        this.initDynamicSqlMap(dalType, dalRegistry);
    }

    public RepositoryInvocationHandler(final Connection conn, Class<?> dalType, DalRegistry dalRegistry) {
        this.space = dalType.getName();
        this.setConnection(conn);
        this.initDynamicSqlMap(dalType, dalRegistry);
    }

    private void initDynamicSqlMap(Class<?> dalType, DalRegistry dalRegistry) {
        for (Method method : dalType.getMethods()) {
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
        Map<String, Object> argMap = new HashMap<>();

        argNames.forEach((key, idx) -> {
            argMap.put(key, objects[idx]);
        });

        if (objects.length == 1) {
            if (objects[0] instanceof Map) {
                argMap.putAll((Map<? extends String, ?>) objects[0]);
            } else if (!(objects[0] instanceof Collection)) {
                argMap.putAll(new BeanMap(objects[0]));
            }
        }

        return argMap;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {

        String dynamicId = method.getName();
        Page page = extractPage(dynamicId, objects);
        Map<String, Object> data = extractData(dynamicId, objects);

        final ExecuteProxy execute = this.dynamicSqlMap.get(dynamicId);
        Object result = this.execute(conn -> {
            return execute.execute(conn, data, page);
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

    protected <R> R execute(final EFunction<Connection, R, SQLException> apply) throws SQLException {
        Connection localConn = this.getConnection();
        DataSource localDS = this.getDataSource();//获取数据源
        boolean usingDS = (localConn == null);
        if (logger.isDebugEnabled()) {
            logger.debug("database connection using DataSource = {}", usingDS);
        }
        if (localConn == null && localDS == null) {
            throw new IllegalArgumentException("DataSource or Connection are not available.");
        }

        Connection useConn = null;
        try {
            if (usingDS) {
                useConn = applyConnection(localDS);
            } else {
                useConn = localConn;
            }
            return apply.eApply(useConn);
        } finally {
            if (usingDS && useConn != null) {
                useConn.close();
            }
        }
    }
}