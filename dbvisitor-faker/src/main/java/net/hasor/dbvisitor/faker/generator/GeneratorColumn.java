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
package net.hasor.dbvisitor.faker.generator;

import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.util.Set;
import java.util.function.Supplier;

/**
 * 负责列数据生成
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class GeneratorColumn {
    private final FakerColumn      columnInfo;
    private final Set<UseFor>      ignoreAct;
    private final Integer          jdbcType;
    private final Class<?>         javaType;
    private final TypeHandler<?>   handler;
    private final Supplier<Object> valueSeed;

    public GeneratorColumn(FakerColumn columnInfo, TypeHandlerRegistry typeHandlerRegistry) {
        this.columnInfo = columnInfo;
        this.ignoreAct = columnInfo.getIgnoreAct();
        this.jdbcType = this.columnInfo.getSqlType();
        this.javaType = this.columnInfo.getJavaType();
        if (this.javaType != null) {
            this.handler = typeHandlerRegistry.getTypeHandler(this.javaType, this.jdbcType);
        } else {
            this.handler = typeHandlerRegistry.getDefaultTypeHandler();
        }

        SeedConfig seedConfig = columnInfo.getSeedConfig();
        SeedFactory<SeedConfig, Object> seedFactory = columnInfo.getSeedFactory();
        this.valueSeed = seedFactory.createSeed(seedConfig);
    }

    public FakerColumn getColumnInfo() {
        return this.columnInfo;
    }

    public boolean isGenerator(UseFor useFor) {
        return !this.ignoreAct.contains(useFor);
    }

    public SqlArg generatorData() {
        return new SqlArg(this.jdbcType, this.handler, this.valueSeed.get());
    }

    public SqlArg buildData(Object value) {
        return new SqlArg(this.jdbcType, this.handler, value);
    }

    @Override
    public String toString() {
        return this.columnInfo.getColumn() + ", ignoreAct=" + ignoreAct + ", jdbcType=" + jdbcType + ", javaType=" + javaType + '}';
    }
}