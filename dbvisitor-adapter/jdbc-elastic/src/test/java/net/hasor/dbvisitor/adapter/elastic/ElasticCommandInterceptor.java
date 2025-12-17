package net.hasor.dbvisitor.adapter.elastic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ElasticCommandInterceptor implements InvocationHandler {
    private static final Map<Class<?>, InvocationHandler> interceptorList = new ConcurrentHashMap<>();

    public static void resetInterceptor() {
        interceptorList.clear();
    }

    public static void addInterceptor(Class<?> type, InvocationHandler handler) {
        interceptorList.put(type, handler);
    }

    public static InvocationHandler getInterceptor(Class<?> type) {
        return interceptorList.get(type);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        InvocationHandler interceptor = interceptorList.get(declaringClass);
        if (interceptor == null) {
            for (Map.Entry<Class<?>, InvocationHandler> entry : interceptorList.entrySet()) {
                if (declaringClass.isAssignableFrom(entry.getKey())) {
                    interceptor = entry.getValue();
                    break;
                }
            }
        }

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
