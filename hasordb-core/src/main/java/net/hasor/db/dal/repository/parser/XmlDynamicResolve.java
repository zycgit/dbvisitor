/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.db.dal.repository.parser;
import net.hasor.cobble.StringUtils;
import net.hasor.db.dal.dynamic.DynamicParser;
import net.hasor.db.dal.dynamic.DynamicSql;
import net.hasor.db.dal.repository.QueryType;
import net.hasor.db.dal.repository.config.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * 解析动态 SQL 配置（XML形式）
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class XmlDynamicResolve extends DynamicParser implements DynamicResolve<Node> {
    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();

    public DynamicSql parseSqlConfig(String sqlString) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder documentBuilder = FACTORY.newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource(new StringReader(sqlString)));
        Element root = document.getDocumentElement();
        return parseSqlConfig(root);
    }

    public DynamicSql parseSqlConfig(Node configNode) {
        QueryType queryType = getQueryType(configNode.getNodeName().toLowerCase().trim());
        if (queryType == null) {
            return null;
        }
        DynamicSql dynamicSql = super.parseDynamicSql(configNode);
        if (dynamicSql == null) {
            return null;
        }
        switch (queryType) {
            case Insert:
                return new InsertSqlConfig(dynamicSql, configNode);
            case Delete:
                return new DeleteSqlConfig(dynamicSql, configNode);
            case Update:
                return new UpdateSqlConfig(dynamicSql, configNode);
            case Query:
                return new QuerySqlConfig(dynamicSql, configNode);
            case Segment:
                return new SegmentSqlConfig(dynamicSql);
            default:
                throw new UnsupportedOperationException("queryType '" + queryType.name() + "' Unsupported.");
        }
    }

    protected QueryType getQueryType(String elementName) {
        if (StringUtils.isBlank(elementName)) {
            throw new UnsupportedOperationException("tag name is Empty.");
        }
        return QueryType.valueOfTag(elementName);
    }
}
