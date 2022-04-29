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
package net.hasor.db.dal.dynamic;
import net.hasor.cobble.ClassUtils;
import net.hasor.db.dal.dynamic.rule.RuleRegistry;
import net.hasor.db.dal.dynamic.rule.SqlBuildRule;
import net.hasor.db.dialect.DefaultSqlDialect;
import net.hasor.db.dialect.PageSqlDialect;
import net.hasor.db.mapping.TableReader;
import net.hasor.db.mapping.def.TableMapping;
import net.hasor.db.types.TypeHandler;
import net.hasor.db.types.TypeHandlerRegistry;

/**
 * 解析动态 SQL 配置
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class DynamicContext {

    public PageSqlDialect findDialect(String dbType) {
        return DefaultSqlDialect.DEFAULT;
    }

    public DynamicSql findDynamic(String dynamicId) {
        return null;
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

    public TypeHandler<?> findTypeHandler(Class<?> handlerType) {
        if (getTypeRegistry().hasTypeHandler(handlerType)) {
            return getTypeRegistry().getTypeHandler(handlerType);
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
        return TypeHandlerRegistry.DEFAULT;
    }

    public RuleRegistry getRuleRegistry() {
        return RuleRegistry.DEFAULT;
    }

    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}