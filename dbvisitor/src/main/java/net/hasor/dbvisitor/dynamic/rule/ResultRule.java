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
package net.hasor.dbvisitor.dynamic.rule;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.dynamic.*;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.ColumnMapResultSetExtractor;
import net.hasor.dbvisitor.mapping.MappingHelper;

import java.sql.SQLException;
import java.util.Map;

/**
 * 结果集规则实现类，用于处理存储过程/函数调用的结果集。
 * 这是一个特殊规则。在正常查询中不会产生任何效果，但通过 call 方法执行数据库 存储过程/函数调用 时可以用于决定结果集如何获取。
 * 功能特点：
 * 1. 实现SqlRule接口，提供结果集处理功能
 * 2. 支持三种结果集类型：ResultSet、ResultUpdate和Default
 * 3. 支持多种结果集处理方式：Java类型映射、行映射器、行处理器和提取器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public final class ResultRule implements SqlRule {
    // 内置规则名称
    public static final String  FUNC_RESULT_SET            = "resultSet";
    public static final String  FUNC_RESULT_UPDATE         = "resultUpdate";
    public static final String  FUNC_DEFAULT_RESULT        = "defaultResult";
    // 预定义实例
    public static final SqlRule INSTANCE_OF_RESULT_SET     = new ResultRule(FUNC_RESULT_SET);
    public static final SqlRule INSTANCE_OF_RESULT_UPDATE  = new ResultRule(FUNC_RESULT_UPDATE);
    public static final SqlRule INSTANCE_OF_DEFAULT_RESULT = new ResultRule(FUNC_DEFAULT_RESULT);

    private final String        ruleName;
    private final ResultArgType argType;

    /**
     * 构造函数
     * @param ruleName 规则名称
     */
    private ResultRule(String ruleName) {
        this.ruleName = ruleName;
        if (StringUtils.equalsIgnoreCase(ruleName, FUNC_RESULT_SET)) {
            this.argType = ResultArgType.ResultSet;
        } else if (StringUtils.equalsIgnoreCase(ruleName, FUNC_RESULT_UPDATE)) {
            this.argType = ResultArgType.ResultUpdate;
        } else if (StringUtils.equalsIgnoreCase(ruleName, FUNC_DEFAULT_RESULT)) {
            this.argType = ResultArgType.Default;
        } else {
            throw new UnsupportedOperationException(ruleName + " Unsupported.");
        }
    }

    @Override
    public String toString() {
        return this.ruleName + " [" + this.hashCode() + "]";
    }

    /**
     * 测试条件是否满足（总是返回true）
     * @param data 参数源
     * @param context 查询上下文
     * @param activeExpr 活动表达式
     * @return 总是返回true
     */
    @Override
    public boolean test(SqlArgSource data, QueryContext context, String activeExpr) {
        return true;
    }

    /** 执行结果集规则 */
    @Override
    public void executeRule(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        sqlBuilder.appendResult(parserConfig(context, activeExpr, ruleValue, this.argType));
    }

    /**
     * 解析配置并创建结果集参数
     * @param registry 查询上下文
     * @param activeExpr 活动表达式
     * @param ruleValue 规则值
     * @param argType 结果集类型
     * @return 结果集参数对象
     */
    public static ResultArg parserConfig(QueryContext registry, String activeExpr, String ruleValue, ResultArgType argType) {
        // restore body
        String body = "";
        if (activeExpr != null) {
            body = activeExpr;
            if (ruleValue != null) {
                body += ",";
            }
        }
        if (ruleValue != null) {
            body += ruleValue;
        }

        // parser config to Map.
        Map<String, String> config = new LinkedCaseInsensitiveMap<>();
        for (String item : body.split(",")) {
            String[] kv = item.split("=");
            if (kv.length != 2) {
                throw new IllegalArgumentException("analysisSQL failed, config must be 'key = value' , '" + body + "' with '" + item + "'");
            }
            if (StringUtils.isNotBlank(kv[0])) {
                config.put(kv[0].trim(), kv[1].trim());
            }
        }

        String name = config.get(ResultArg.CFG_KEY_NAME);
        ResultArg arg = new ResultArg((argType == ResultArgType.Default ? null : name), argType);

        Class<?> javaType = convertJavaType(registry, config.get(ResultArg.CFG_KEY_JAVA_TYPE));
        if (javaType != null) {
            arg.setJavaType(javaType);
            return arg;
        }

        Class<?> mapperType = convertJavaType(registry, config.get(ResultArg.CFG_KEY_ROW_MAPPER));
        if (mapperType != null) {
            arg.setRowMapper(ClassUtils.newInstance(mapperType));
            return arg;
        }

        Class<?> handlerType = convertJavaType(registry, config.get(ResultArg.CFG_KEY_ROW_HANDLER));
        if (handlerType != null) {
            arg.setRowHandler(ClassUtils.newInstance(handlerType));
            return arg;
        }

        Class<?> extractorType = convertJavaType(registry, config.get(ResultArg.CFG_KEY_EXTRACTOR));
        if (extractorType != null) {
            arg.setExtractor(ClassUtils.newInstance(extractorType));
            return arg;
        }

        arg.setExtractor(defaultExtractor(registry));
        return arg;
    }

    private static Class<?> convertJavaType(QueryContext registry, String javaType) {
        try {
            if (StringUtils.isNotBlank(javaType)) {
                return registry.loadClass(javaType);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static ResultSetExtractor<?> defaultExtractor(QueryContext registry) {
        boolean resultsCaseInsensitive = MappingHelper.caseInsensitive(registry.options());
        return new ColumnMapResultSetExtractor(0, registry.getTypeRegistry(), resultsCaseInsensitive);
    }
}
