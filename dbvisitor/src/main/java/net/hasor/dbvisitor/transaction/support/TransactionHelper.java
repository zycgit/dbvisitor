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
package net.hasor.dbvisitor.transaction.support;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;
import javax.sql.DataSource;
import net.hasor.cobble.dynamic.Proxy;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.Transactional;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-30
 */
public class TransactionHelper {
    private static final WeakHashMap<DataSource, TransactionManager> txManagerMap = new WeakHashMap<>();

    public static TransactionManager txManager(DataSource dataSource) {
        TransactionManager txManager = txManagerMap.get(dataSource);
        if (txManager == null) {
            synchronized (txManagerMap) {
                txManager = txManagerMap.get(dataSource);
                if (txManager != null) {
                    return txManager;
                }

                txManager = new LocalTransactionManager(dataSource);
                txManagerMap.put(dataSource, txManager);
            }
        }
        return txManager;
    }

    public static <T> T support(T object, DataSource... ctxDs) {
        return support(object, Arrays.asList(ctxDs));
    }

    public static <T> T support(T object, List<DataSource> ctxDs) {
        TransactionInterceptor[] txInterceptors = dsToTemplate(ctxDs);
        return Proxy.newProxyInstance(object, TransactionHelper::testAop, txInterceptors);
    }

    private static TransactionInterceptor[] dsToTemplate(List<DataSource> ctxDs) {
        TransactionInterceptor[] interceptors = new TransactionInterceptor[ctxDs.size()];
        for (int i = 0; i < ctxDs.size(); i++) {
            TransactionManager txManager = txManager(ctxDs.get(i));
            interceptors[i] = new TransactionInterceptor(txManager);
        }
        return interceptors;
    }

    private static boolean testAop(final Method matcherType) {
        if (matcherType.isAnnotationPresent(Transactional.class)) {
            return true;
        } else {
            return matcherType.getDeclaringClass().isAnnotationPresent(Transactional.class);
        }
    }
}
