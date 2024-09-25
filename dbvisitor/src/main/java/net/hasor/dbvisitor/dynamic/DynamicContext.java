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
package net.hasor.dbvisitor.dynamic;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.dynamic.rule.SqlBuildRule;
import net.hasor.dbvisitor.mapping.TableReader;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * 解析动态 SQL 配置
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class DynamicContext {
    public static final DynamicContext          DEFAULT      = new DynamicContext();
    private             TypeHandlerRegistry     typeRegistry = TypeHandlerRegistry.DEFAULT;
    private             RuleRegistry            ruleRegistry = RuleRegistry.DEFAULT;
    private final       Map<String, DynamicSql> macroMap     = new HashMap<>();

    public DynamicSql findMacro(String dynamicId) {
        return this.macroMap.get(dynamicId);
    }

    public void addMacro(String macroName, String sqlSegment) {
        if (StringUtils.isNotBlank(macroName)) {
            this.macroMap.put(macroName, DynamicParsed.getParsedSql(sqlSegment));
        }
    }

    public TableMapping<?> findTableMapping(String resultMap) {
        return null;
    }

    public TableReader<?> findTableReader(String resultType) {
        return null;
    }

    public TypeHandler<?> findTypeHandler(Integer jdbcType) {
        if (getTypeRegistry().hasTypeHandler(jdbcType)) {
            return getTypeRegistry().getTypeHandler(jdbcType);
        } else {
            return null;
        }
    }

    public TypeHandler<?> findTypeHandler(Class<?> javaType) {
        if (getTypeRegistry().hasTypeHandler(javaType)) {
            return getTypeRegistry().getTypeHandler(javaType);
        } else {
            return null;
        }
    }

    public TypeHandler<?> findTypeHandler(Class<?> javaType, Integer jdbcType) {
        if (getTypeRegistry().hasTypeHandler(javaType, jdbcType)) {
            return getTypeRegistry().getTypeHandler(javaType, jdbcType);
        } else {
            return null;
        }
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return ClassUtils.getClass(getClassLoader(), className);
    }

    public SqlBuildRule findRule(String ruleName) {
        return getRuleRegistry().findByName(ruleName);
    }

    public TypeHandlerRegistry getTypeRegistry() {
        return this.typeRegistry;
    }

    public void setTypeRegistry(TypeHandlerRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    public RuleRegistry getRuleRegistry() {
        return this.ruleRegistry;
    }

    public void setRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}