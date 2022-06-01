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
package net.hasor.dbvisitor.transaction;
import net.hasor.cobble.dynamic.AopClassConfig;
import net.hasor.cobble.dynamic.AopClassLoader;
import net.hasor.cobble.dynamic.AopMatchers;
import net.hasor.dbvisitor.transaction.support.TransactionalInterceptor;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * 可以标记在：方法、类 上面
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-10-30
 */
public class TransactionHelper {

    public static Object newProxyService(Object original, DataSource dataSource) throws IOException, ReflectiveOperationException {
        if (original == null) {
            return null;
        }

        Class<?> originalType = AopClassLoader.isDynamic(original) ? AopClassLoader.getPrototypeType(original) : original.getClass();

        AopClassConfig classConfig = new AopClassConfig(originalType);
        classConfig.addAopInterceptor(AopMatchers.annotatedWithMethod(Transactional.class), new TransactionalInterceptor(dataSource));

        return classConfig.buildProxy(original);
    }
}
