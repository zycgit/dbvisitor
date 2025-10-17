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
package net.hasor.dbvisitor.session;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.convert.ConverterBean;
import net.hasor.cobble.ref.BeanMap;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageResult;
import net.hasor.dbvisitor.jdbc.DynamicConnection;

/**
 * prototype is a no database status session.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-02-02
 */
public class SessionPrototype {
    private final Configuration                configuration;
    private final Map<String, FacadeStatement> cache = new ConcurrentHashMap<>();

    protected SessionPrototype(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    /** create {@link Session} using {@link SessionPrototype} */
    public Session newSession(Connection conn) throws SQLException {
        return new Session(conn, this);
    }

    /** create {@link Session} using {@link SessionPrototype} */
    public Session newSession(DataSource ds) throws SQLException {
        return new Session(ds, this);
    }

    /** create {@link Session} using {@link SessionPrototype} */
    public Session newSession(DynamicConnection dc) throws SQLException {
        return new Session(dc, this);
    }

    public boolean hasStatement(String stId) {
        if (this.cache.containsKey(stId)) {
            return true;
        } else {
            String configSpace = "";
            String configID = stId;
            if (stId.contains(".")) {
                int index = stId.lastIndexOf(".");
                configSpace = stId.substring(0, index);
                configID = stId.substring(index + 1);
            }
            return this.configuration.getMapperRegistry().findStatement(configSpace, configID) != null;
        }
    }

    public Object executeStatement(Connection conn, String stId, Object parameter) throws SQLException {
        return executeStatement(conn, stId, parameter, null, false);
    }

    public <E> List<E> queryStatement(Connection conn, String stId, Object parameter, Page page) throws SQLException {
        return asList(executeStatement(conn, stId, parameter, page, false));
    }

    public <E> PageResult<E> pageStatement(Connection conn, String stId, Object parameter, Page page) throws SQLException {
        return (PageResult<E>) executeStatement(conn, stId, parameter, page, true);
    }

    private Object executeStatement(Connection conn, String stId, Object parameter, Page page, boolean pageResult) throws SQLException {
        FacadeStatement proxy = this.cache.computeIfAbsent(stId, s -> {
            String space = "";
            String dynamicId = stId;
            if (stId.contains(".")) {
                int index = stId.lastIndexOf(".");
                space = stId.substring(0, index);
                dynamicId = stId.substring(index + 1);
            }
            return new FacadeStatement(space, dynamicId, this.configuration);
        });

        Map<String, Object> mapData = this.extractData(parameter);
        return proxy.execute(conn, mapData, page, pageResult);
    }

    protected static <E> List<E> asList(Object result) {
        if (result instanceof List) {
            return (List<E>) result;
        } else {
            List<E> list = new ArrayList<>();
            list.add((E) result);
            return list;
        }
    }

    protected static Map<String, Object> extractData(Object parameter) {
        if (parameter instanceof Map) {
            return (Map) parameter;
        } else if (!(parameter instanceof Collection)) {
            BeanMap beanMap = new BeanMap(parameter);
            beanMap.setTransformConvert(ConverterBean.getInstance());
            return beanMap;
        } else {
            return CollectionUtils.asMap("arg0", parameter);
        }
    }
}