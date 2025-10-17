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
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import ognl.*;

public class OgnlSqlArgSourceAccessor implements PropertyAccessor {

    @Override
    public Object getProperty(OgnlContext context, Object target, Object name) throws OgnlException {
        Object result;
        SqlArgSource source = (SqlArgSource) target;
        Node currentNode = context.getCurrentNode().jjtGetParent();
        boolean indexedAccess = false;

        if (currentNode == null) {
            throw new OgnlException("node is null for '" + name + "'");
        }
        if (!(currentNode instanceof ASTProperty)) {
            currentNode = currentNode.jjtGetParent();
        }
        if (currentNode instanceof ASTProperty) {
            indexedAccess = ((ASTProperty) currentNode).isIndexedAccess();
        }

        if ((name instanceof String) && !indexedAccess) {
            if (name.equals("size")) {
                result = source.getParameterNames().length;
            } else if (name.equals("keys") || name.equals("keySet")) {
                result = source.getParameterNames();
            } else if (name.equals("values")) {
                result = Arrays.stream(source.getParameterNames()).map(source::getValue).collect(Collectors.toList());
            } else if (name.equals("isEmpty")) {
                result = source.getParameterNames().length == 0 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                result = source.getValue(name.toString());
            }
        } else {
            result = source.getValue(name == null ? null : name.toString());
        }

        return result;
    }

    @Override
    public void setProperty(OgnlContext context, Object target, Object name, Object value) {
        SqlArgSource source = (SqlArgSource) target;
        source.putValue(name.toString(), value);
    }

    public String getSourceAccessor(OgnlContext context, Object target, Object index) {
        Node currentNode = context.getCurrentNode().jjtGetParent();
        boolean indexedAccess = false;

        if (currentNode == null)
            throw new RuntimeException("node is null for '" + index + "'");

        if (!(currentNode instanceof ASTProperty))
            currentNode = currentNode.jjtGetParent();

        if (currentNode instanceof ASTProperty)
            indexedAccess = ((ASTProperty) currentNode).isIndexedAccess();

        String indexStr = index.toString();

        context.setCurrentAccessor(SqlArgSource.class);
        context.setCurrentType(Object.class);

        if (index instanceof String && !indexedAccess) {
            String key = (indexStr.indexOf('"') >= 0 ? indexStr.replaceAll("\"", "") : indexStr);

            if (key.equals("size")) {
                context.setCurrentType(int.class);
                return ".size()";
            } else if (key.equals("keys") || key.equals("keySet")) {
                context.setCurrentType(String[].class);
                return ".keySet()";
            } else if (key.equals("values")) {
                context.setCurrentType(Collection.class);
                return ".values()";
            } else if (key.equals("isEmpty")) {
                context.setCurrentType(boolean.class);
                return ".isEmpty()";
            }
        }

        return ".get(" + indexStr + ")";
    }

    public String getSourceSetter(OgnlContext context, Object target, Object index) {
        context.setCurrentAccessor(SqlArgSource.class);
        context.setCurrentType(Object.class);

        String indexStr = index.toString();
        if (index instanceof String) {
            String key = (indexStr.indexOf('"') >= 0 ? indexStr.replaceAll("\"", "") : indexStr);

            if (key.equals("size")) {
                return "";
            } else if (key.equals("keys") || key.equals("keySet")) {
                return "";
            } else if (key.equals("values")) {
                return "";
            } else if (key.equals("isEmpty")) {
                return "";
            }
        }

        return ".put(" + indexStr + ", $3)";
    }
}
