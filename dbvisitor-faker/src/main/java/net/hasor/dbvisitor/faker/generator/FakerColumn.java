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
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;
import net.hasor.dbvisitor.types.TypeHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 要生成数据的列基本信息和配置信息
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerColumn {
    private final String           column;
    private       Integer          jdbcType;
    private       TypeHandler<?>   typeHandler;
    private final boolean          key;
    private final boolean          canBeCut;
    private final Set<UseFor>      ignoreAct;
    private final SeedConfig       seedConfig;
    private       Supplier<Object> valueSeed;

    FakerColumn(JdbcColumn jdbcColumn, SeedConfig seedConfig) {
        this.column = jdbcColumn.getColumnName();
        this.jdbcType = jdbcColumn.getJdbcNumber();
        this.typeHandler = seedConfig.getTypeHandler();
        this.key = jdbcColumn.isPrimaryKey() || jdbcColumn.isUniqueKey();
        this.canBeCut = StringUtils.isNotBlank(jdbcColumn.getDefaultValue()) || Boolean.TRUE.equals(jdbcColumn.getNullable());
        this.ignoreAct = new HashSet<>();
        this.seedConfig = seedConfig;
    }

    public String getColumn() {
        return column;
    }

    public boolean isKey() {
        return key;
    }

    public boolean isCanBeCut() {
        return canBeCut;
    }

    public boolean isGenerator(UseFor useFor) {
        return !this.ignoreAct.contains(useFor);
    }

    public SqlArg generatorData() {
        return new SqlArg(this.jdbcType, this.typeHandler, this.valueSeed.get());
    }

    public SqlArg buildData(Object value) {
        return new SqlArg(this.jdbcType, this.typeHandler, value);
    }

    void initColumn(Set<UseFor> ignoreAct, SeedFactory<SeedConfig, Object> seedFactory) {
        this.ignoreAct.clear();
        this.ignoreAct.addAll(ignoreAct);
        this.valueSeed = seedFactory.createSeed(this.seedConfig);
    }

    public void ignoreAct(UseFor ignoreAct) {
        this.ignoreAct.add(ignoreAct);
    }

    @Override
    public String toString() {
        String handlerType = this.seedConfig.getTypeHandler().getClass().toString();
        return this.column + ", ignoreAct=" + ignoreAct + ", jdbcType=" + this.jdbcType + ", javaType=" + handlerType + '}';
    }
}