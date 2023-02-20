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
package net.hasor.dbvisitor.faker.generator.parameter;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * ParameterProcessor 注册器
 * @version : 2023-02-14
 * @author 赵永春 (zyc@hasor.net)
 */
public class ParameterRegistry {
    public static final ParameterRegistry                              DEFAULT             = new ParameterRegistry();
    private final       Map<String, ParameterProcessor>                defaultProcessorMap = new LinkedCaseInsensitiveMap<>();
    private final       Map<Class<?>, Map<String, ParameterProcessor>> typedProcessorMap   = new HashMap<>();

    private ParameterRegistry() {
    }

    static {
        ServiceLoader.load(ParameterProcessorLookUp.class).forEach(p -> p.loopUp(DEFAULT));
    }

    /** 查找 ParameterProcessor */
    public ParameterProcessor findByName(String parameterName, Class<?> seedConfigType) {
        if (seedConfigType == null) {
            return this.defaultProcessorMap.get(parameterName);
        }

        Map<String, ParameterProcessor> processorMap = this.typedProcessorMap.get(seedConfigType);
        if (processorMap == null || !processorMap.containsKey(parameterName)) {
            return this.defaultProcessorMap.get(parameterName);
        } else {
            return processorMap.get(parameterName);
        }
    }

    /** 注册 ParameterProcessor */
    public synchronized void register(String parameterName, ParameterProcessor processor) {
        register(parameterName, processor, null);
    }

    /** 注册 ParameterProcessor */
    public synchronized void register(String parameterName, ParameterProcessor processor, Class<?> withConfigType) {
        Objects.requireNonNull(processor, "processor is null.");

        if (withConfigType == null) {
            this.defaultProcessorMap.put(parameterName, processor);
        } else {
            Map<String, ParameterProcessor> processorMap = this.typedProcessorMap.get(withConfigType);
            if (processorMap == null) {
                processorMap = new LinkedCaseInsensitiveMap<>();
                this.typedProcessorMap.put(withConfigType, processorMap);
            }
            processorMap.put(parameterName, processor);
        }
    }

}
