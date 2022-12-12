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
import net.hasor.cobble.setting.SettingNode;
import net.hasor.dbvisitor.faker.FakerConfigEnum;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.seed.SeedConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 要生成数据的列基本信息和配置信息
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerColumn {
    private final FakerTable    table;
    private final String        column;
    private final boolean       key;
    private       boolean       canBeCut;
    private final Set<UseFor>   ignoreAct;
    private final TypeProcessor typeProcessor;
    private final FakerFactory  factory;
    //
    private       String        selectTemplate;
    private       String        insertTemplate;
    private       String        setColTemplate;
    private       String        setValueTemplate;
    private       String        whereColTemplate;
    private       String        whereValueTemplate;

    FakerColumn(FakerTable table, JdbcColumn jdbcColumn, TypeProcessor typeProcessor, Set<UseFor> ignoreAct, FakerFactory factory, SettingNode columnConfig) {
        this.table = table;
        this.column = jdbcColumn.getColumnName();
        this.key = jdbcColumn.isPrimaryKey() || jdbcColumn.isUniqueKey();
        this.canBeCut = Boolean.TRUE.equals(jdbcColumn.isHasDefaultValue()) || Boolean.TRUE.equals(jdbcColumn.getNullable());
        this.ignoreAct = new HashSet<>(ignoreAct);
        this.typeProcessor = typeProcessor;
        this.factory = factory;

        if (columnConfig != null) {
            this.selectTemplate = columnConfig.getSubValue(FakerConfigEnum.SELECT_TEMPLATE.getConfigKey());
            this.insertTemplate = columnConfig.getSubValue(FakerConfigEnum.INSERT_TEMPLATE.getConfigKey());
            this.setColTemplate = columnConfig.getSubValue(FakerConfigEnum.SET_COL_TEMPLATE.getConfigKey());
            this.setValueTemplate = columnConfig.getSubValue(FakerConfigEnum.SET_VALUE_TEMPLATE.getConfigKey());
            this.whereColTemplate = columnConfig.getSubValue(FakerConfigEnum.WHERE_COL_TEMPLATE.getConfigKey());
            this.whereValueTemplate = columnConfig.getSubValue(FakerConfigEnum.WHERE_VALUE_TEMPLATE.getConfigKey());
        }

        if (StringUtils.isBlank(this.selectTemplate)) {
            this.selectTemplate = "{name}";
        }
        if (StringUtils.isBlank(this.insertTemplate)) {
            this.insertTemplate = "?";
        }
        if (StringUtils.isBlank(this.setColTemplate)) {
            this.setColTemplate = "{name}";
        }
        if (StringUtils.isBlank(this.setValueTemplate)) {
            this.setValueTemplate = "?";
        }
        if (StringUtils.isBlank(this.whereColTemplate)) {
            this.whereColTemplate = "{name}";
        }
        if (StringUtils.isBlank(this.whereValueTemplate)) {
            this.whereValueTemplate = "?";
        }

        String colName = this.factory.getSqlDialect().fmtName(table.isUseQualifier(), this.column);
        this.selectTemplate = this.selectTemplate.replace("{name}", colName);
        this.setColTemplate = this.setColTemplate.replace("{name}", colName);
        this.whereColTemplate = this.whereColTemplate.replace("{name}", colName);
    }

    /** 获取列名 */
    public String getColumn() {
        return column;
    }

    /** 用于 select 语句的列名拼写 */
    public String getSelectTemplate() {
        return selectTemplate;
    }

    /** 用于 select 语句的列名拼写 */
    public void setSelectTemplate(String selectTemplate) {
        this.selectTemplate = selectTemplate;
    }

    /** 用于 insert 语句的参数 */
    public String getInsertTemplate() {
        return this.insertTemplate;
    }

    /** 用于 update 语句的参数 列名 部分的拼写 */
    public String getSetColTemplate() {
        return setColTemplate;
    }

    /** 用于 update 语句的参数 列名 部分的拼写 */
    public void setSetColTemplate(String setColTemplate) {
        this.setColTemplate = setColTemplate;
    }

    /** 用于 update 语句的参数 列值 部分的拼写 */
    public String getSetValueTemplate() {
        return setValueTemplate;
    }

    /** 用于 update 语句的参数 列值 部分的拼写 */
    public void setSetValueTemplate(String setValueTemplate) {
        this.setValueTemplate = setValueTemplate;
    }

    /** 用于 update/delete 中 where 语句的参数 */
    public String getWhereColTemplate() {
        return this.whereColTemplate;
    }

    /** 用于 update/delete 中 where 语句的参数 */
    public String getWhereValueTemplate() {
        return this.whereValueTemplate;
    }

    /** 列是否为被当作 key（是 pk 或 uk）*/
    public boolean isKey() {
        return key;
    }

    /** 表示在 insert 操作中该列是否允许被裁掉，通常具有 默认值或者允许为空 的列才可以被裁掉 */
    public boolean isCanBeCut() {
        return canBeCut;
    }

    public boolean isGenerator(UseFor useFor) {
        return !this.ignoreAct.contains(useFor);
    }

    /** 生成随机值 */
    public SqlArg generatorData() {
        return this.typeProcessor.buildData(this.column);
    }

    /** 从 RS 中读取并生成 SqlArg */
    public SqlArg readData(ResultSet rs) throws SQLException {
        return this.typeProcessor.buildData(rs, this.column);
    }

    /** 随机种子的配置 */
    public <T extends SeedConfig> T seedConfig() {
        return (T) this.typeProcessor.getSeedConfig();
    }

    @Override
    public String toString() {
        String seedAndWriterString = this.typeProcessor.toString();
        return this.column + ", ignoreAct=" + ignoreAct + ", seedAndWriter=" + seedAndWriterString + '}';
    }

    /** 像列配置一个忽略规则 */
    public FakerColumn ignoreAct(UseFor... ignoreAct) {
        this.ignoreAct.addAll(Arrays.asList(ignoreAct));
        return this;
    }

    /** 重置列忽略规则 */
    public FakerColumn ignoreReset() {
        this.ignoreAct.clear();
        this.ignoreAct.addAll(this.typeProcessor.getDefaultIgnoreAct());
        return this;
    }

    public FakerColumn doNotCut() {
        this.canBeCut = false;
        return this;
    }

    public FakerColumn canBeCut() {
        this.canBeCut = true;
        return this;
    }

    /** 重新创建随机数据发生器 */
    void applyConfig() {
        this.typeProcessor.applyConfig();
        this.ignoreAct.addAll(this.typeProcessor.getDefaultIgnoreAct());
    }
}