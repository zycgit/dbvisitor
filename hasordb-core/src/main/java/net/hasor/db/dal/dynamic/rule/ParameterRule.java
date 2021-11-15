/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.db.dal.dynamic.rule;
import net.hasor.cobble.NumberUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.db.dal.dynamic.DynamicContext;
import net.hasor.db.dal.dynamic.SqlArg;
import net.hasor.db.dal.dynamic.SqlMode;
import net.hasor.db.dal.dynamic.ognl.OgnlUtils;
import net.hasor.db.dialect.SqlBuilder;
import net.hasor.db.types.TypeHandler;
import net.hasor.db.types.TypeHandlerRegistry;

import java.sql.JDBCType;
import java.sql.Types;
import java.util.Map;

/**
 * 动态参数规则，负责动态 SQL 中 #{} 的解析。
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class ParameterRule implements SqlBuildRule {
    public static final ParameterRule INSTANCE          = new ParameterRule();
    public static final String        CFG_KEY_NAME      = "name";
    public static final String        CFG_KEY_MODE      = "mode";
    public static final String        CFG_KEY_JDBC_TYPE = "jdbcType";
    public static final String        CFG_KEY_JAVA_TYPE = "javaType";
    public static final String        CFG_KEY_HANDLER   = "typeHandler";

    private SqlMode convertSqlMode(String sqlMode) {
        if (StringUtils.isNotBlank(sqlMode)) {
            for (SqlMode mode : SqlMode.values()) {
                if (mode.name().equalsIgnoreCase(sqlMode)) {
                    return mode;
                }
            }
        }
        return null;
    }

    private Integer convertJdbcType(String jdbcType) {
        if (NumberUtils.isNumber(jdbcType)) {
            return NumberUtils.createInteger(jdbcType);
        }

        if (StringUtils.isNotBlank(jdbcType)) {
            for (JDBCType typeElement : JDBCType.values()) {
                if (typeElement.name().equalsIgnoreCase(jdbcType)) {
                    return typeElement.getVendorTypeNumber();
                }
            }
        }
        return null;
    }

    private Class<?> convertJavaType(DynamicContext context, String javaType) {
        try {
            if (StringUtils.isNotBlank(javaType)) {
                return context.loadClass(javaType);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private TypeHandler<?> convertTypeHandler(DynamicContext context, String typeHandler) {
        try {
            if (StringUtils.isNotBlank(typeHandler)) {
                Class<?> aClass = context.loadClass(typeHandler);
                return context.findTypeHandler(aClass);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Map<String, String> parserConfig(String[] content, int start, int length) {
        Map<String, String> exprMap = new LinkedCaseInsensitiveMap<>();
        for (int i = start; i < length; i++) {
            if (i >= content.length) {
                break;
            }

            String data = content[i];
            String[] kv = data.split("=");
            if (kv.length != 2) {
                throw new IllegalArgumentException("analysisSQL failed, config must be 'key = value' , '" + content[i] + "' with '" + data + "'");
            }
            if (StringUtils.isNotBlank(kv[0])) {
                exprMap.put(kv[0].trim(), kv[1].trim());
            }
        }

        return exprMap;
    }

    @Override
    public void executeRule(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder, String ruleValue) {
        String[] testSplit = ruleValue.split(",");
        if (testSplit.length > 6 || testSplit.length == 0) {
            throw new IllegalArgumentException("analysisSQL failed, format error -> '#{valueExpr [,name= xxx] [,mode= IN|OUT|INOUT] [,jdbcType=INT] [,javaType=java.lang.String] [,typeHandler=YouTypeHandlerClassName]}'");
        }

        boolean noExpr = StringUtils.contains(testSplit[0], "=");
        String expr = noExpr ? "" : testSplit[0];
        Map<String, String> config = ParameterRule.INSTANCE.parserConfig(testSplit, noExpr ? 0 : 1, testSplit.length);

        executeRule(data, context, sqlBuilder, expr, config);
    }

    public void executeRule(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder, String expr, Map<String, String> config) {
        String name = (config != null) ? config.get(CFG_KEY_NAME) : null;
        SqlMode sqlMode = convertSqlMode((config != null) ? config.get(CFG_KEY_MODE) : null);
        Integer jdbcType = convertJdbcType((config != null) ? config.get(CFG_KEY_JDBC_TYPE) : null);
        Class<?> javaType = convertJavaType(context, (config != null) ? config.get(CFG_KEY_JAVA_TYPE) : null);
        TypeHandler<?> typeHandler = convertTypeHandler(context, (config != null) ? config.get(CFG_KEY_HANDLER) : null);
        Object argValue = sqlMode == SqlMode.Out ? null : OgnlUtils.evalOgnl(expr, data);

        if (sqlMode == null) {
            sqlMode = SqlMode.In;
        }
        if (javaType == null && argValue != null) {
            javaType = argValue.getClass();
        }

        if (argValue == null && jdbcType == null && javaType == null) {
            jdbcType = Types.VARCHAR;// fix all parameters unknown.
        }

        if (typeHandler == null) {
            if (javaType != null && jdbcType != null) {
                typeHandler = context.findTypeHandler(javaType, jdbcType);
            } else if (javaType != null) {
                typeHandler = context.findTypeHandler(javaType);
            } else if (jdbcType != null) {
                typeHandler = context.findTypeHandler(jdbcType);
            }
        }
        if (typeHandler == null) {
            typeHandler = TypeHandlerRegistry.DEFAULT.getDefaultTypeHandler();
        }

        sqlBuilder.appendSql("?", new SqlArg(name, expr, argValue, sqlMode, jdbcType, javaType, typeHandler));
    }

    @Override
    public String toString() {
        return "parameter [" + this.hashCode() + "]";
    }
}
