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
package net.hasor.dbvisitor.session;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.mapper.StatementDef;
import net.hasor.dbvisitor.mapper.StatementType;
import net.hasor.dbvisitor.mapper.def.InsertConfig;
import net.hasor.dbvisitor.mapper.def.SelectKeyConfig;
import net.hasor.dbvisitor.page.Page;

/**
 * 执行器总入口
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-07-20
 */
class FacadeStatement {
    private final StatementDef              statementDef;
    private       AbstractStatementExecute  statementExecute;
    private       SelectKeyStatementExecute selectKeyExecute;

    FacadeStatement(String namespace, String statementId, Configuration config) {
        this.statementDef = config.getMapperRegistry().findStatement(namespace, statementId);
        if (this.statementDef == null) {
            String fullName = StringUtils.isBlank(namespace) ? statementId : (namespace + "." + statementId);
            throw new IllegalStateException("statement '" + fullName + "' is not found.");
        }
        this.initExecute(config);
    }

    FacadeStatement(StatementDef def, Configuration config) {
        this.statementDef = Objects.requireNonNull(def, "statementDef is null.");
        this.initExecute(config);
    }

    private void initExecute(Configuration config) {
        this.statementExecute = this.createExecute(this.statementDef.getConfig().getStatementType(), config);
        if (this.statementDef.getConfig() instanceof InsertConfig) {
            SelectKeyConfig keyConfig = ((InsertConfig) this.statementDef.getConfig()).getSelectKey();
            if (keyConfig != null) {
                AbstractStatementExecute selectKey = this.createExecute(this.statementDef.getConfig().getStatementType(), config);
                this.selectKeyExecute = new SelectKeyStatementExecute(this.statementDef, keyConfig, selectKey);
            }
        }
    }

    private AbstractStatementExecute createExecute(StatementType statementType, Configuration registry) {
        switch (statementType) {
            case Statement:
                return new StatementExecute(registry);
            case Prepared:
                return new PreparedStatementExecute(registry);
            case Callable:
                return new CallableStatementExecute(registry);
            default: {
                throw new UnsupportedOperationException("statementType '" + statementType.getTypeName() + "' Unsupported.");
            }
        }
    }

    public Object execute(Connection conn, Map<String, Object> data, Page pageInfo, boolean pageResult) throws SQLException {
        if (this.selectKeyExecute != null) {
            this.selectKeyExecute.processBefore(conn, data);
        }

        Object result = this.statementExecute.execute(conn, this.statementDef, data, pageInfo, pageResult);

        if (this.selectKeyExecute != null) {
            this.selectKeyExecute.processAfter(conn, data);
        }

        return result;
    }
}