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
package net.hasor.dbvisitor.dal.execute;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dal.session.DalContext;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.mapper.StatementType;
import net.hasor.dbvisitor.mapper.def.DmlConfig;
import net.hasor.dbvisitor.mapper.def.SelectKeyConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 执行器总入口
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-07-20
 */
public class ExecuteProxy {
    private final DmlConfig                   dynamicSql;
    private final AbstractStatementExecute<?> execute;
    private       SelectKeyExecute            selectKeyExecute;

    public ExecuteProxy(String dynamicId, DalContext context) {
        DynamicSql sqlConfig = context.findMacro(dynamicId);
        if (sqlConfig == null) {
            throw new NullPointerException("dynamic '" + dynamicId + "' is not found.");
        }
        if (!(sqlConfig instanceof DmlConfig)) {
            throw new ClassCastException("dynamic '" + dynamicId + "' is not DmlSqlConfig");
        }

        this.dynamicSql = (DmlConfig) context.findMacro(dynamicId);
        this.execute = buildExecute(this.dynamicSql.getStatementType(), context);

        SelectKeyConfig selectKey = ((DmlConfig) sqlConfig).getSelectKey();
        //        if (selectKey != null) {
        //            AbstractStatementExecute<?> selectKeyExecute = buildExecute(selectKey.getStatementType(), context);
        //            SelectKeyHandler selectKeyHandler = null;
        //
        //            if (StringUtils.isBlank(selectKey.getHandler())) {
        //                selectKeyHandler = new SelectKeySequenceHolder(selectKey, selectKeyExecute);
        //            } else {
        //                try {
        //                    Class<?> aClass = context.getClassLoader().loadClass(selectKey.getHandler());
        //                    SelectKeyHandlerFactory holderFactory = (SelectKeyHandlerFactory) aClass.newInstance();
        //                    selectKeyHandler = holderFactory.createHandler(selectKey, selectKeyExecute);
        //                    if (selectKeyHandler == null) {
        //                        throw new NullPointerException("createSelectKeyHolder result is null.");
        //                    }
        //                } catch (Exception e) {
        //                    throw ExceptionUtils.toRuntime(e);
        //                }
        //            }
        //            this.selectKeyExecute = new SelectKeyExecute(selectKey, selectKeyHandler);
        //        }
    }

    private AbstractStatementExecute<?> buildExecute(StatementType statementType, DalContext context) {
        switch (statementType) {
            case Statement: {
                return new StatementExecute(context);
            }
            case Prepared: {
                return new PreparedStatementExecute(context);
            }
            case Callable: {
                return new CallableStatementExecute(context);
            }
            default: {
                throw new UnsupportedOperationException("statementType '" + statementType.getTypeName() + "' Unsupported.");
            }
        }
    }

    public Object execute(Connection conn, Map<String, Object> data, Page page, boolean pageResult, PageSqlDialect dialect) throws SQLException {
        if (this.selectKeyExecute != null) {
            this.selectKeyExecute.processBefore(conn, data);
        }

        Object result = this.execute.execute(conn, this.dynamicSql, data, page, pageResult, dialect);

        if (this.selectKeyExecute != null) {
            this.selectKeyExecute.processAfter(conn, data);
        }

        return result;
    }

    private static class SelectKeySequenceHolder implements SelectKeyHandler {
        private final SelectKeyConfig             keySqlConfig;
        private final AbstractStatementExecute<?> selectKeyExecute;

        public SelectKeySequenceHolder(SelectKeyConfig keySqlConfig, AbstractStatementExecute<?> selectKeyExecute) {
            this.keySqlConfig = keySqlConfig;
            this.selectKeyExecute = selectKeyExecute;
        }

        public Object processSelectKey(Connection conn, Map<String, Object> parameter, boolean onBefore) throws SQLException {
            String keyColumn = this.keySqlConfig.getKeyColumn();
            Object resultValue = null;

            if (StringUtils.isBlank(keyColumn)) {
                // maybe is single value.
                resultValue = this.selectKeyExecute.execute(conn, this.keySqlConfig, parameter, null, false, null);
            } else {
                resultValue = this.selectKeyExecute.execute(conn, this.keySqlConfig, parameter, null, false, null, true);
            }

            if (resultValue instanceof List) {
                resultValue = ((List<?>) resultValue).get(0);
            }

            return resultValue;
        }
    }
}
