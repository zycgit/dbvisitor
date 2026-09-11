/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.scene.transaction;
import javax.sql.DataSource;
import net.hasor.cobble.dynamic.DynamicConfig;
import net.hasor.cobble.dynamic.Matchers;
import net.hasor.cobble.dynamic.Proxy;

/**
 * 可以标记在：方法、类 上面
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-30
 */
public class TransactionHelper {

    public static Object newProxyService(Object original, DataSource dataSource) {
        if (original == null) {
            return null;
        }

        Class<?> originalType = Proxy.getPrototypeType(original);
        DynamicConfig classConfig = new DynamicConfig(originalType);
        classConfig.addAopInterceptor(Matchers.annotatedWithMethod(Transactional.class), new TransactionalInterceptor(dataSource));

        return Proxy.newProxyInstance(original);
    }
}
