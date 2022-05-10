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
package net.hasor.dbvisitor.dal.execute;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.dal.dynamic.DynamicSql;
import net.hasor.dbvisitor.dal.execute.sequence.SelectKeySequenceHolderFactory;
import net.hasor.dbvisitor.dal.repository.StatementType;
import net.hasor.dbvisitor.dal.repository.config.DmlSqlConfig;
import net.hasor.dbvisitor.dal.repository.config.SelectKeySqlConfig;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.page.Page;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 执行器总入口
 * @version : 2021-07-20
 * @author 赵永春 (zyc@hasor.net)
 */
public class ExecuteProxy {
    private final DmlSqlConfig                dynamicSql;
    private final AbstractStatementExecute<?> execute;
    private       KeySequenceExecute          selectKeyHolder;

    public ExecuteProxy(String dynamicId, DynamicContext context) {
        DynamicSql sqlConfig = context.findDynamic(dynamicId);
        if (sqlConfig == null) {
            throw new NullPointerException("dynamic '" + dynamicId + "' is not found.");
        }
        if (!(sqlConfig instanceof DmlSqlConfig)) {
            throw new ClassCastException("dynamic '" + dynamicId + "' is not DmlSqlConfig");
        }

        this.dynamicSql = (DmlSqlConfig) context.findDynamic(dynamicId);
        this.execute = buildExecute(this.dynamicSql.getStatementType(), context);

        SelectKeySqlConfig selectKey = ((DmlSqlConfig) sqlConfig).getSelectKey();
        if (selectKey != null) {
            AbstractStatementExecute<?> selectKeyExecute = buildExecute(selectKey.getStatementType(), context);
            KeySequenceHolder sequenceHolder = null;

            if (StringUtils.isBlank(selectKey.getHandler())) {
                sequenceHolder = new SelectKeySequenceHolderFactory().createHolder(selectKey, selectKeyExecute);
            } else {
                try {
                    Class<?> aClass = context.getClassLoader().loadClass(selectKey.getHandler());
                    KeySequenceHolderFactory holderFactory = (KeySequenceHolderFactory) aClass.newInstance();
                    sequenceHolder = holderFactory.createHolder(selectKey, selectKeyExecute);
                    if (sequenceHolder == null) {
                        throw new NullPointerException("createSelectKeyHolder result is null.");
                    }
                } catch (Exception e) {
                    throw ExceptionUtils.toRuntime(e);
                }
            }

            this.selectKeyHolder = new KeySequenceExecute(selectKey, sequenceHolder);
        }
    }

    private AbstractStatementExecute<?> buildExecute(StatementType statementType, DynamicContext context) {
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

        if (this.selectKeyHolder != null) {
            this.selectKeyHolder.processBefore(conn, data);
        }

        Object result = this.execute.execute(conn, this.dynamicSql, data, page, pageResult, dialect);

        if (this.selectKeyHolder != null) {
            this.selectKeyHolder.processAfter(conn, data);
        }

        return result;
    }
}
