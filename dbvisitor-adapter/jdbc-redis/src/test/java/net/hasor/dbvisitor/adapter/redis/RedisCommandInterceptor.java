/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.redis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisCommandInterceptor implements InvocationHandler {
    private static final Map<Class<?>, InvocationHandler> interceptorList = new ConcurrentHashMap<>();

    public static void resetInterceptor() {
        interceptorList.clear();
    }

    public static void addInterceptor(Class<?> type, InvocationHandler handler) {
        interceptorList.put(type, handler);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InvocationHandler interceptor = interceptorList.get(method.getDeclaringClass());
        if (interceptor == null) {
            try {
                return method.invoke(proxy, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        } else {
            return interceptor.invoke(proxy, method, args);
        }
    }
}
