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
package net.hasor.dbvisitor.internal;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlRuntime;

/**
 * OGNL 执行工具。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-05
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
