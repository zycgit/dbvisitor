/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper.def;
import java.util.function.Function;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.logic.ArrayDynamicSql;
import net.hasor.dbvisitor.mapper.GeneratedKeySource;

/**
 * Insert SqlConfig
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
public class InsertConfig extends DmlConfig {
    private SelectKeyConfig    selectKey;
    private boolean            useGeneratedKeys;
    private GeneratedKeySource generatedKeySource;
    private String             keyProperty;
    private String             keyColumn;

    /**
     * 构造函数
     * @param target 动态 sql 构建目标对象
     * @param config 配置获取函数
     */
    public InsertConfig(ArrayDynamicSql target, Function<String, String> config) {
        super(target, config);

        if (config != null) {
            String generated = config.apply(KEY_GENERATED);
            this.useGeneratedKeys = StringUtils.isNotBlank(generated) && Boolean.parseBoolean(generated);
            this.keyProperty = config.apply(KEY_PROPERTY);
            this.keyColumn = config.apply(KEY_COLUMN);
            this.generatedKeySource = GeneratedKeySource.valueOfCode(config.apply(KEY_SOURCE), GeneratedKeySource.GeneratedKeys);
        }
    }

    @Override
    public QueryType getType() {
        return QueryType.Insert;
    }

    public SelectKeyConfig getSelectKey() {
        return this.selectKey;
    }

    public void setSelectKey(SelectKeyConfig selectKey) {
        this.selectKey = selectKey;
    }

    public boolean isUseGeneratedKeys() {
        return this.useGeneratedKeys;
    }

    public void setUseGeneratedKeys(boolean useGeneratedKeys) {
        this.useGeneratedKeys = useGeneratedKeys;
    }

    public String getKeyProperty() {
        return this.keyProperty;
    }

    public void setKeyProperty(String keyProperty) {
        this.keyProperty = keyProperty;
    }

    public String getKeyColumn() {
        return this.keyColumn;
    }

    public void setKeyColumn(String keyColumn) {
        this.keyColumn = keyColumn;
    }

    public GeneratedKeySource getGeneratedKeySource() {
        return this.generatedKeySource;
    }

    public void setGeneratedKeySource(GeneratedKeySource generatedKeySource) {
        this.generatedKeySource = generatedKeySource;
    }
}
