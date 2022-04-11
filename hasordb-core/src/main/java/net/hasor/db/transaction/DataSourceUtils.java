/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.db.transaction;
import net.hasor.db.transaction.support.LocalTransactionManager;

import javax.sql.DataSource;
import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 某一个数据源的事务管理器
 * @version : 2013-10-30
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class DataSourceUtils {
    private final static ThreadLocal<Map<DataSource, TransactionManager>> managerMap = ThreadLocal.withInitial(ConcurrentHashMap::new);
    private final static ThreadLocal<Map<DataSource, ConnectionHolder>>   holderMap  = ThreadLocal.withInitial(ConcurrentHashMap::new);

    /** 获取或创建 数据源的当前本地事务管理器 */
    private static synchronized TransactionManager createOrGetManager(final DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        Map<DataSource, TransactionManager> localMap = managerMap.get();
        return localMap.computeIfAbsent(dataSource, LocalTransactionManager::new);
    }

    /** 获取或创建 数据源的当前本地连接 Holder */
    private static synchronized ConnectionHolderImpl createOrGetHolder(final DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        Map<DataSource, ConnectionHolder> localMap = holderMap.get();
        return (ConnectionHolderImpl) localMap.computeIfAbsent(dataSource, ConnectionHolderImpl::new);
    }

    /** 获取或创建 数据源的当前本地连接 Holder */
    public static synchronized ConnectionHolder getHolder(final DataSource dataSource) {
        return createOrGetHolder(dataSource);
    }

    /** 获取当前本地 {@link TransactionManager} */
    public static TransactionManager getManager(DataSource dataSource) {
        return createOrGetManager(dataSource);
    }

    /** 强制设置当前本地 ConnectionHolder */
    protected static void unsafeResetHolder(DataSource dataSource, ConnectionHolder holder) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(holder);
        Map<DataSource, ConnectionHolder> localMap = holderMap.get();
        localMap.put(dataSource, holder);
    }

    /** 强制清空当前本地 ConnectionHolder */
    protected static void unsafeClearHolder(DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        Map<DataSource, ConnectionHolder> localMap = holderMap.get();
        localMap.remove(dataSource);
    }

    /** 获取 DataSource 的当前本地 Connection，在使用完毕之后必须要 close 它。在使用者列表中（最后一个 close 的才会真正触发连接关闭） */
    public static Connection getConnection(DataSource dataSource) {
        ConnectionHolderImpl holder = createOrGetHolder(dataSource);
        holder.requested();// ref++

        CloseSuppressingInvocationHandlerForHolder handler = new CloseSuppressingInvocationHandlerForHolder(holder); // when close ref--
        return (ConnectionProxy) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(), new Class[] { ConnectionProxy.class, Closeable.class }, handler);
    }

    /** Connection 接口代理，目的是为了控制一些方法的调用。同时进行一些特殊类型的处理 */
    private static class CloseSuppressingInvocationHandlerForHolder implements InvocationHandler {
        private final ConnectionHolderImpl holder;
        private final AtomicBoolean        closed;

        CloseSuppressingInvocationHandlerForHolder(ConnectionHolderImpl holder) {
            this.holder = holder;
            this.closed = new AtomicBoolean(false);
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            switch (method.getName()) {
                case "getTargetSource":
                    return this.holder.getDataSource();
                case "toString":
                    return this.holder.toString();
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                default:
                    break;
            }

            if (this.closed.get()) {
                throw new IllegalStateException("connection is close.");
            }

            Connection conn = this.holder.getConnection();
            switch (method.getName()) {
                case "getTargetConnection":
                    return conn;
                case "setSavepoint":
                    if (args.length == 0) {
                        return this.holder.createSavepoint();
                    } else {
                        break;
                    }
                case "close":
                    if (this.holder.isOpen()) {
                        this.holder.released();//ref--
                    }
                    this.closed.set(true);
                    return null;
                default:
                    break;
            }

            try {
                return method.invoke(conn, args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }
}