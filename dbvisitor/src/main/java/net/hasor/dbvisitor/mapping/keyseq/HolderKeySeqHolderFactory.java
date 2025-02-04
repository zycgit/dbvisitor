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
package net.hasor.dbvisitor.mapping.keyseq;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.reflect.Annotation;
import net.hasor.cobble.reflect.Annotations;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.KeyHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持 @KeyHolder 注解方式
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-12-01
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
            try {
                HolderCache.put(keyHolderType, (GeneratedKeyHandlerFactory) keyHolderType.newInstance());
            } catch (ReflectiveOperationException e) {
                throw ExceptionUtils.toRuntime(e);
            }
        }

        return HolderCache.get(keyHolderType).createHolder(context);
    }
}