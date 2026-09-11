/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping.keyseq;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.reflect.Annotation;
import net.hasor.cobble.reflect.Annotations;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.KeyHolder;

/**
 * 支持 @KeyHolder 注解方式
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-01
 */
public class HolderKeySeqHolderFactory implements GeneratedKeyHandlerFactory {
    private static final Map<Class<?>, GeneratedKeyHandlerFactory> HolderCache = new ConcurrentHashMap<>();

    @Override
    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) throws ClassNotFoundException {
        Annotations annotations = context.getAnnotations();
        if (annotations == null) {
            return null;
        }
        Annotation keyHolder = annotations.getAnnotation(KeyHolder.class);
        if (keyHolder == null) {
            return null;
        }

        Class<?> keyHolderType = keyHolder.getClass("value", context.getRegistry().getClassLoader(), false);
        if (keyHolderType == null) {
            return null;
        }

        if (!HolderCache.containsKey(keyHolderType)) {
            HolderCache.put(keyHolderType, ClassUtils.newInstance(keyHolderType.asSubclass(GeneratedKeyHandlerFactory.class)));
        }

        return HolderCache.get(keyHolderType).createHolder(context);
    }
}
