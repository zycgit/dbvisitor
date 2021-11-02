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
package net.hasor.db.dal.repository.config;
import net.hasor.cobble.StringUtils;
import net.hasor.db.dal.dynamic.DynamicSql;
import net.hasor.db.dal.repository.MultipleResultsType;
import net.hasor.db.dal.repository.QueryType;
import net.hasor.db.dal.repository.StatementType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Callable SqlConfig
 * @version : 2021-06-19
 * @author 赵永春 (zyc@byshell.org)
 */
public class CallableSqlConfig extends QuerySqlConfig {
    private final Set<String> resultOut = new HashSet<>();

    public CallableSqlConfig(DynamicSql target) {
        super(target);
        this.setStatementType(StatementType.Callable);
    }

    public CallableSqlConfig(DynamicSql target, Node operationNode) {
        super(target, operationNode);
        this.setStatementType(StatementType.Callable);

        NamedNodeMap nodeAttributes = operationNode.getAttributes();
        Node resultOutNode = nodeAttributes.getNamedItem("resultOut");
        String resultOutString = (resultOutNode != null) ? resultOutNode.getNodeValue() : null;
        if (StringUtils.isNotBlank(resultOutString)) {
            for (String out : resultOutString.split(",")) {
                this.resultOut.add(out.trim());
            }
        }
    }

    public void addResultOut(String resultOut) {
        this.resultOut.add(resultOut.trim());
    }

    public Set<String> getResultOut() {
        return this.resultOut;
    }

    @Override
    public QueryType getDynamicType() {
        return QueryType.Callable;
    }

    protected MultipleResultsType defaultMultipleResultsType() {
        return MultipleResultsType.ALL;
    }
}
