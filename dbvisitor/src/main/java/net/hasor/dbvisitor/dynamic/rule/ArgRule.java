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
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.NumberUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.dynamic.DynamicContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.SqlMode;
import net.hasor.dbvisitor.internal.OgnlUtils;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandler;

import java.lang.reflect.Constructor;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.Map;

/**
 * 动态参数规则，负责动态 SQL 中 #{} 的解析。
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class ArgRule implements SqlBuildRule {
    public static final ArgRule INSTANCE          = new ArgRule();
    public static final String  CFG_KEY_MODE      = "mode";
    public static final String  CFG_KEY_JDBC_TYPE = "jdbcType";
    public static final String  CFG_KEY_JAVA_TYPE = "javaType";
    public static final String  CFG_KEY_HANDLER   = "typeHandler";

    private static final Map<String, Integer> JDBC_TYPE_MAP = new LinkedCaseInsensitiveMap<>();

    static {
        JDBC_TYPE_MAP.put("INT", JDBCType.INTEGER.getVendorTypeNumber());
        for (JDBCType typeElement : JDBCType.values()) {
            JDBC_TYPE_MAP.put(typeElement.name(), typeElement.getVendorTypeNumber());
        }
    }

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
            return JDBC_TYPE_MAP.get(jdbcType);
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

    public static Map<String, String> parserConfig(String[] content, int start, int length) {
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
    public boolean test(SqlArgSource data, DynamicContext context, String activeExpr) {
        return true;
    }

    @Override
    public void executeRule(SqlArgSource data, DynamicContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        String[] testSplit = ruleValue.split(",");
        if (testSplit.length > 6 || testSplit.length == 0) {
            throw new IllegalArgumentException("analysisSQL failed, format error -> '#{valueExpr [,mode= IN|OUT|INOUT] [,jdbcType=INT] [,javaType=java.lang.String] [,typeHandler=YouTypeHandlerClassName]}'");
        }

        boolean noExpr = StringUtils.contains(testSplit[0], "=");
        String expr = noExpr ? "" : testSplit[0];
        Map<String, String> config = ArgRule.INSTANCE.parserConfig(testSplit, noExpr ? 0 : 1, testSplit.length);

        executeRule(data, context, sqlBuilder, expr, config);
    }

    public void executeRule(SqlArgSource data, DynamicContext context, SqlBuilder sqlBuilder, String expr, Map<String, String> config) throws SQLException {
        SqlMode sqlMode = convertSqlMode((config != null) ? config.get(CFG_KEY_MODE) : null);
        Integer jdbcType = convertJdbcType((config != null) ? config.get(CFG_KEY_JDBC_TYPE) : null);
        Class<?> javaType = convertJavaType(context, (config != null) ? config.get(CFG_KEY_JAVA_TYPE) : null);
        String handlerType = (config != null) ? config.get(CFG_KEY_HANDLER) : null;
        Object argValue = sqlMode == SqlMode.Out ? null : OgnlUtils.evalOgnl(expr, data);

        if (argValue instanceof SqlArg) {
            sqlBuilder.appendSql("?", argValue);
            return;
        }

        TypeHandler<?> typeHandler = createTypeHandler(context, javaType, handlerType, argValue);
        sqlBuilder.appendSql("?", new SqlArg(expr, argValue, sqlMode, jdbcType, javaType, typeHandler));
    }

    private TypeHandler<?> createTypeHandler(DynamicContext context, Class<?> javaType, String handlerType, Object argValue) throws SQLException {
        if (StringUtils.isBlank(handlerType)) {
            return null;
        }

        // try load from cache
        TypeHandler<?> typeHandler = context.getTypeRegistry().getHandlerByHandlerType(handlerType);
        if (typeHandler != null) {
            return typeHandler;
        }

        // argClass and handlerClass
        Class<?> javaClass;
        Class<?> handlerClass;
        try {
            javaClass = javaType != null ? javaType : (argValue != null ? argValue.getClass() : null);
            handlerClass = context.loadClass(handlerType);
        } catch (ClassNotFoundException e) {
            throw new SQLException("handlerType '" + handlerType + "' ClassNotFoundException.");
        }

        if (javaClass == null) {
            return context.getTypeRegistry().createTypeHandler(handlerClass, () -> ClassUtils.newInstance(handlerClass));
        } else {
            return context.getTypeRegistry().createTypeHandler(handlerClass, () -> {
                try {
                    Constructor<?> constructor = handlerClass.getConstructor(Class.class);
                    return (TypeHandler<?>) constructor.newInstance(javaType);
                } catch (NoSuchMethodException e) {
                    return ClassUtils.newInstance(handlerClass);
                } catch (ReflectiveOperationException e) {
                    throw ExceptionUtils.toRuntime(e);
                }
            });
        }
    }

    @Override
    public String toString() {
        return "arg [" + this.hashCode() + "]";
    }
}
