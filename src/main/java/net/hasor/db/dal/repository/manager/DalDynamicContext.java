/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.db.dal.repository.manager;
import net.hasor.cobble.ref.BeanMap;
import net.hasor.db.dal.dynamic.DynamicContext;
import net.hasor.db.dal.dynamic.DynamicSql;
import net.hasor.db.dal.dynamic.rule.RuleRegistry;
import net.hasor.db.mapping.TableReader;
import net.hasor.db.types.TypeHandlerRegistry;

import java.util.Map;

/**
 * 生成动态 SQL 的 Build 环境
 * @version : 2021-06-05
 * @author 赵永春 (zyc@byshell.org)
 */
public class DalDynamicContext extends DynamicContext {
    private final String      space;
    private final DalRegistry dalRegistry;

    public DalDynamicContext(String space, Object context, DalRegistry dalRegistry) {
        this.space = space;

        if (context instanceof Map) {
            super.context.putAll((Map) context);
        } else if (context != null) {
            super.context.putAll(new BeanMap(context));
        }

        this.dalRegistry = dalRegistry;
    }

    public DynamicSql findDynamic(String dynamicId) {
        return this.dalRegistry.findDynamicSql(this.space, dynamicId);
    }

    public TableReader<?> findTableReader(String resultMap) {
        return this.dalRegistry.findTableReader(this.space, resultMap);
    }

    protected TypeHandlerRegistry getTypeRegistry() {
        return this.dalRegistry.getTypeRegistry();
    }

    protected RuleRegistry getRuleRegistry() {
        return this.dalRegistry.getRuleRegistry();
    }

    protected ClassLoader getClassLoader() {
        return this.dalRegistry.getClassLoader();
    }
}