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
package net.hasor.dbvisitor.keyholder.sequence;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.dbvisitor.keyholder.CreateContext;
import net.hasor.dbvisitor.keyholder.KeyHolder;
import net.hasor.dbvisitor.keyholder.KeySeqHolder;
import net.hasor.dbvisitor.keyholder.KeySeqHolderFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持 @KeyHolder 注解方式
 * @version : 2022-12-01
 * @author 赵永春 (zyc@hasor.net)
 */
public class HolderKeySeqHolderFactory implements KeySeqHolderFactory {
    private static final Map<Class<?>, KeySeqHolderFactory> HolderCache = new ConcurrentHashMap<>();

    @Override
    public KeySeqHolder createHolder(CreateContext context) {
        Map<String, Object> contextMap = context.getContext();
        if (contextMap == null || !contextMap.containsKey(KeyHolder.class.getName())) {
            return null;
        }

        KeyHolder keyHolder = (KeyHolder) contextMap.get(KeyHolder.class.getName());
        Class<? extends KeySeqHolderFactory> keyHolderType = keyHolder.value();

        if (!HolderCache.containsKey(keyHolderType)) {
            try {
                HolderCache.put(keyHolderType, keyHolderType.newInstance());
            } catch (ReflectiveOperationException e) {
                throw ExceptionUtils.toRuntime(e);
            }
        }

        return HolderCache.get(keyHolderType).createHolder(context);
    }
}