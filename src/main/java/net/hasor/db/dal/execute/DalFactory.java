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
import net.hasor.db.dal.repository.manager.DalRegistry;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;

/**
 * 基础数据库操作接口
 * @version : 2021-05-19
 * @author 赵永春 (zyc@hasor.net)
 */
public class DalFactory {

    public static <T> T newRepository(Class<T> dalType, final DataSource dataSource, DalRegistry dalRegistry) {
        ClassLoader classLoader = dalRegistry.getClassLoader();
        InvocationHandler handler = new RepositoryInvocationHandler(dataSource, dalType, dalRegistry);
        return (T) Proxy.newProxyInstance(classLoader, new Class[] { dalType }, handler);
    }

    public static <T> T newRepository(Class<T> dalType, final Connection connection, DalRegistry dalRegistry) {
        ClassLoader classLoader = dalRegistry.getClassLoader();
        InvocationHandler handler = new RepositoryInvocationHandler(connection, dalType, dalRegistry);
        return (T) Proxy.newProxyInstance(classLoader, new Class[] { dalType }, handler);
    }

}
