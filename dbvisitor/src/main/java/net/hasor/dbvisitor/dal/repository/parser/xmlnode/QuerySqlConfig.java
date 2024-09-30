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
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dal.repository.QueryType;
import net.hasor.dbvisitor.dal.repository.ResultSetType;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Query SqlConfig
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-19
 */
public class QuerySqlConfig extends DmlSqlConfig {
    private String        resultMap;
    private String        resultType;
    private int           fetchSize;
    private ResultSetType resultSetType;
    private String[]      bindOut;

    public QuerySqlConfig(DynamicSql target) {
        super(target);
    }

    public QuerySqlConfig(DynamicSql target, Node operationNode) {
        super(target, operationNode);
        NamedNodeMap nodeAttributes = operationNode.getAttributes();
        Node resultMapNode = nodeAttributes.getNamedItem("resultMap");
        Node resultTypeNode = nodeAttributes.getNamedItem("resultType");
        Node fetchSizeNode = nodeAttributes.getNamedItem("fetchSize");
        Node resultSetTypeNode = nodeAttributes.getNamedItem("resultSetType");
        Node bindOutNode = nodeAttributes.getNamedItem("bindOut");
        String resultMap = (resultMapNode != null) ? resultMapNode.getNodeValue() : null;
        String resultType = (resultTypeNode != null) ? resultTypeNode.getNodeValue() : null;
        String fetchSize = (fetchSizeNode != null) ? fetchSizeNode.getNodeValue() : null;
        String resultSetType = (resultSetTypeNode != null) ? resultSetTypeNode.getNodeValue() : null;
        String bindOut = (bindOutNode != null) ? bindOutNode.getNodeValue() : null;

        this.resultMap = resultMap;
        this.resultType = resultType;
        this.fetchSize = StringUtils.isBlank(fetchSize) ? 256 : Integer.parseInt(fetchSize);
        this.resultSetType = ResultSetType.valueOfCode(resultSetType, ResultSetType.DEFAULT);
        this.bindOut = StringUtils.isNotBlank(bindOut) ? bindOut.split(",") : ArrayUtils.EMPTY_STRING_ARRAY;
    }

    @Override
    public QueryType getDynamicType() {
        return QueryType.Select;
    }

    public String getResultMap() {
        return this.resultMap;
    }

    public void setResultMap(String resultMap) {
        this.resultMap = resultMap;
    }

    public String getResultType() {
        return this.resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public int getFetchSize() {
        return this.fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public ResultSetType getResultSetType() {
        return this.resultSetType;
    }

    public void setResultSetType(ResultSetType resultSetType) {
        this.resultSetType = resultSetType;
    }

    public String[] getBindOut() {
        return this.bindOut;
    }

    public void setBindOut(String[] bindOut) {
        this.bindOut = bindOut;
    }
}
