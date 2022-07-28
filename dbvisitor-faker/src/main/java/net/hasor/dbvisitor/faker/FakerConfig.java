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
package net.hasor.dbvisitor.faker;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.faker.generator.DataLoader;
import net.hasor.dbvisitor.faker.strategy.ConservativeStrategy;
import net.hasor.dbvisitor.faker.strategy.Strategy;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/**
 * FakerTable 构建器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerConfig {
    private ClassLoader         classLoader;
    private TypeHandlerRegistry typeRegistry;
    private DataLoader          dataLoader;
    private Strategy            strategy;
    private SqlDialect          dialect;
    private boolean             useQualifier;

    public FakerConfig() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.typeRegistry = TypeHandlerRegistry.DEFAULT;
        this.dataLoader = null;
        this.strategy = new ConservativeStrategy();
        this.dialect = null;
        this.useQualifier = true;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public TypeHandlerRegistry getTypeRegistry() {
        return typeRegistry;
    }

    public void setTypeRegistry(TypeHandlerRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    public DataLoader getDataLoader() {
        return dataLoader;
    }

    public void setDataLoader(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    public SqlDialect getDialect() {
        return dialect;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public void setDialect(SqlDialect dialect) {
        this.dialect = dialect;
    }

    public boolean isUseQualifier() {
        return useQualifier;
    }

    public void setUseQualifier(boolean useQualifier) {
        this.useQualifier = useQualifier;
    }
}