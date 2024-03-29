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
package net.hasor.dbvisitor.jdbc.core;
import net.hasor.dbvisitor.jdbc.PreparedStatementSetter;
import net.hasor.dbvisitor.jdbc.SqlParameter.InSqlParameter;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Simple adapter for PreparedStatementSetter that applies a given array of arguments.
 * @author Juergen Hoeller
 * @author 赵永春 (zyc@hasor.net)
 */
public class ArgPreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final Object[]            args;

    public ArgPreparedStatementSetter(Object[] args) {
        this(args, TypeHandlerRegistry.DEFAULT);
    }

    public ArgPreparedStatementSetter(Object[] args, TypeHandlerRegistry typeHandlerRegistry) {
        this.typeHandlerRegistry = typeHandlerRegistry;
        this.args = args;
    }

    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return this.typeHandlerRegistry;
    }

    @Override
    public void setValues(final PreparedStatement ps) throws SQLException {
        if (this.args != null) {
            for (int i = 0; i < this.args.length; i++) {
                Object arg = this.args[i];
                this.doSetValue(ps, i + 1, arg);
            }
        }
    }

    protected void doSetValue(final PreparedStatement ps, final int parameterPosition, Object argValue) throws SQLException {
        if (argValue instanceof InSqlParameter) {
            Object value = ((InSqlParameter) argValue).getValue();
            Integer jdbcType = ((InSqlParameter) argValue).getJdbcType();
            TypeHandler typeHandler = ((InSqlParameter) argValue).getTypeHandler();
            if (typeHandler != null && jdbcType != null) {
                typeHandler.setParameter(ps, parameterPosition, value, jdbcType);
                return;
            } else if (typeHandler != null) {
                if (value == null) {
                    ps.setObject(parameterPosition, null);
                } else {
                    typeHandler.setParameter(ps, parameterPosition, value, TypeHandlerRegistry.toSqlType(value.getClass()));
                }
                return;
            } else {
                argValue = value;
            }
        }
        this.typeHandlerRegistry.setParameterValue(ps, parameterPosition, argValue);
    }

    @Override
    public void cleanupParameters() {
        StatementSetterUtils.cleanupParameters(this.args);
    }
}
