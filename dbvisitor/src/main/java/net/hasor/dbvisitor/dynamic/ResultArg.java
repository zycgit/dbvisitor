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
import net.hasor.dbvisitor.dynamic.rule.ArgRule;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;

/**
 * 结果参数类，用于存储过程/函数调用的结果处理配置
 * 功能特点：
 * 1. 封装结果处理的多种配置方式
 * 2. 支持Java类型、行映射器、行处理器和结果集提取器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-30
 */
public class ResultArg {
    public static final String                CFG_KEY_NAME        = ArgRule.CFG_KEY_NAME;
    public static final String                CFG_KEY_JAVA_TYPE   = ArgRule.CFG_KEY_JAVA_TYPE;
    public static final String                CFG_KEY_ROW_MAPPER  = ArgRule.CFG_KEY_ROW_MAPPER;
    public static final String                CFG_KEY_ROW_HANDLER = ArgRule.CFG_KEY_ROW_HANDLER;
    public static final String                CFG_KEY_EXTRACTOR   = ArgRule.CFG_KEY_EXTRACTOR;
    private             ResultArgType         argType;
    private             String                name;
    private             Class<?>              javaType;
    private             RowMapper<?>          rowMapper;
    private             RowCallbackHandler    rowHandler;
    private             ResultSetExtractor<?> extractor;

    /**
     * 构造函数（基础配置）
     * @param name 参数名称
     * @param argType 参数类型
     */
    public ResultArg(String name, ResultArgType argType) {
        this.name = name;
        this.argType = argType;
    }

    /**
     * 构造函数（完整配置）
     * @param name 参数名称
     * @param argType 参数类型
     * @param javaType Java类型
     * @param rowMapper 行映射器
     * @param rowHandler 行处理器
     * @param extractor 结果集提取器
     */
    public ResultArg(String name, ResultArgType argType, Class<?> javaType, RowMapper<?> rowMapper, RowCallbackHandler rowHandler, ResultSetExtractor<?> extractor) {
        this.name = name;
        this.argType = argType;
        this.javaType = javaType;
        this.rowMapper = rowMapper;
        this.rowHandler = rowHandler;
        this.extractor = extractor;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResultArgType getArgType() {
        return this.argType;
    }

    public void setArgType(ResultArgType argType) {
        this.argType = argType;
    }

    public Class<?> getJavaType() {
        return this.javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public RowMapper<?> getRowMapper() {
        return this.rowMapper;
    }

    public void setRowMapper(RowMapper<?> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public RowCallbackHandler getRowHandler() {
        return this.rowHandler;
    }

    public void setRowHandler(RowCallbackHandler rowHandler) {
        this.rowHandler = rowHandler;
    }

    public ResultSetExtractor<?> getExtractor() {
        return this.extractor;
    }

    public void setExtractor(ResultSetExtractor<?> extractor) {
        this.extractor = extractor;
    }
}
