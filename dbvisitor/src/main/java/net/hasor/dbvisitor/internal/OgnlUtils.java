/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.internal;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlRuntime;

/**
 * OGNL 执行工具。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public class OgnlUtils {

    static {
        OgnlRuntime.setPropertyAccessor(SqlArgSource.class, new OgnlSqlArgSourceAccessor());
    }

    public static Object evalOgnl(String exprString, Object root) {
        try {
            OgnlContext context = new OgnlContext(null, null, new OgnlMemberAccess(true));
            return Ognl.getValue(exprString, context, root);
        } catch (Exception e) {
            throw ExceptionUtils.toRuntime(e);
        }
    }

    public static void writeByExpr(String exprString, Object root, Object value) {
        try {
            OgnlContext context = new OgnlContext(null, null, new OgnlMemberAccess(true));
            Ognl.setValue(exprString, context, root, value);
        } catch (Exception e) {
            throw ExceptionUtils.toRuntime(e);
        }
    }
}
