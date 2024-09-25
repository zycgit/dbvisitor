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
package net.hasor.dbvisitor.dal.repository.parser.xmlnode;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dal.repository.QueryType;
import net.hasor.dbvisitor.dal.repository.ResultSetType;
import net.hasor.dbvisitor.dal.repository.StatementType;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * All DML SqlConfig
 * @version : 2021-06-19
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class DmlSqlConfig extends SegmentSqlConfig {
    private StatementType      statementType = StatementType.Prepared;
    private int                timeout       = -1;
    private SelectKeySqlConfig selectKey;

    public DmlSqlConfig(DynamicSql target) {
        super(target);
        this.processSelectKey(target);
    }

    public DmlSqlConfig(DynamicSql target, Node operationNode) {
        super(target);
        NamedNodeMap nodeAttributes = operationNode.getAttributes();
        Node statementTypeNode = nodeAttributes.getNamedItem("statementType");
        Node timeoutNode = nodeAttributes.getNamedItem("timeout");
        String statementType = (statementTypeNode != null) ? statementTypeNode.getNodeValue() : null;
        String timeout = (timeoutNode != null) ? timeoutNode.getNodeValue() : null;

        this.statementType = StatementType.valueOfCode(statementType, StatementType.Prepared);
        this.timeout = StringUtils.isBlank(timeout) ? -1 : Integer.parseInt(timeout);

        this.processSelectKey(target);
    }

    protected void processSelectKey(DynamicSql target) {
        if (target instanceof ArrayDynamicSql) {
            for (DynamicSql dynamicSql : ((ArrayDynamicSql) target).getSubNodes()) {
                if (dynamicSql instanceof SelectKeyDynamicSql) {
                    SelectKeyDynamicSql skDynamicSql = (SelectKeyDynamicSql) dynamicSql;
                    StatementType skStatementType = StatementType.valueOfCode(skDynamicSql.getStatementType(), StatementType.Prepared);
                    ResultSetType skResultSetType = ResultSetType.valueOfCode(skDynamicSql.getResultSetType(), ResultSetType.DEFAULT);

                    this.selectKey = new SelectKeySqlConfig(skDynamicSql);
                    this.selectKey.setStatementType(skStatementType);
                    this.selectKey.setTimeout(skDynamicSql.getTimeout());
                    this.selectKey.setResultMap(skDynamicSql.getResultMap());
                    this.selectKey.setResultType(skDynamicSql.getResultType());
                    this.selectKey.setFetchSize(skDynamicSql.getFetchSize());
                    this.selectKey.setResultSetType(skResultSetType);
                    this.selectKey.setKeyProperty(skDynamicSql.getKeyProperty());
                    this.selectKey.setKeyColumn(skDynamicSql.getKeyColumn());
                    this.selectKey.setOrder(skDynamicSql.getOrder());
                    this.selectKey.setHandler(skDynamicSql.getHandler());
                }
            }
        }
    }

    public SelectKeySqlConfig getSelectKey() {
        return this.selectKey;
    }

    public abstract QueryType getDynamicType();

    public StatementType getStatementType() {
        return this.statementType;
    }

    public void setStatementType(StatementType statementType) {
        this.statementType = statementType;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
