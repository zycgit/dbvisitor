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
    private final String                          column;
    private       Integer                         jdbcType;
    private       TypeHandler<?>                  typeHandler;
    private final boolean                         key;
    private final boolean                         canBeCut;
    private final Set<UseFor>                     ignoreAct;
    private final SeedConfig                      seedConfig;
    private       SeedFactory<SeedConfig, Object> seedFactory;
    private       Supplier<Object>                valueSeed;

    FakerColumn(JdbcColumn jdbcColumn, SeedConfig seedConfig) {
        this.column = jdbcColumn.getColumnName();
        this.jdbcType = jdbcColumn.getJdbcNumber();
        this.typeHandler = seedConfig.getTypeHandler();
        this.key = jdbcColumn.isPrimaryKey() || jdbcColumn.isUniqueKey();
        this.canBeCut = StringUtils.isNotBlank(jdbcColumn.getDefaultValue()) || Boolean.TRUE.equals(jdbcColumn.getNullable());
        this.ignoreAct = new HashSet<>();
        this.seedConfig = seedConfig;
    }

    /** 获取列名 */
    public String getColumn() {
        return column;
    }

    /** 写入时用作的 jdbc type */
    public Integer getJdbcType() {
        return jdbcType;
    }

    /** 写入时用作的 jdbc type */
    public void setJdbcType(Integer jdbcType) {
        this.jdbcType = jdbcType;
    }

    /** 执行 ps set 的 TypeHandler */
    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    /** 执行 ps set 的 TypeHandler */
    public void setTypeHandler(TypeHandler<?> typeHandler) {
        this.typeHandler = typeHandler;
    }

    /** 列是否为被当作 key（是 pk 或 uk）*/
    public boolean isKey() {
        return key;
    }

    /** insert 操作中 列如果具有 默认值或者允许为空，则表示可以在生成语句中被裁剪掉，否则必须含有该列 */
    public boolean isCanBeCut() {
        return canBeCut;
    }

    public boolean isGenerator(UseFor useFor) {
        return !this.ignoreAct.contains(useFor);
    }

    /** 生成随机值 */
    public SqlArg generatorData() {
        return new SqlArg(this.jdbcType, this.typeHandler, this.valueSeed.get());
    }

    /** 生成 value 值 */
    public SqlArg buildData(Object value) {
        return new SqlArg(this.jdbcType, this.typeHandler, value);
    }

    void initColumn(Set<UseFor> ignoreAct, SeedFactory<SeedConfig, Object> seedFactory) {
        this.ignoreAct.clear();
        this.ignoreAct.addAll(ignoreAct);
        this.seedFactory = seedFactory;
        this.applyConfig();
    }

    /** 重新创建随机数据发生器 */
    void applyConfig() {
        this.valueSeed = this.seedFactory.createSeed(this.seedConfig);
    }

    /** 像列配置一个忽略规则 */
    public void ignoreAct(UseFor ignoreAct) {
        this.ignoreAct.add(ignoreAct);
    }

    /** 随机种子的配置 */
    public <T extends SeedConfig> T seedConfig() {
        return (T) this.seedConfig;
    }

    @Override
    public String toString() {
        String handlerType = this.seedConfig.getTypeHandler().getClass().toString();
        return this.column + ", ignoreAct=" + ignoreAct + ", jdbcType=" + this.jdbcType + ", javaType=" + handlerType + '}';
    }
}