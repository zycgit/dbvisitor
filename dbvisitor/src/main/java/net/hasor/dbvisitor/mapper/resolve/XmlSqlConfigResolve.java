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
package net.hasor.dbvisitor.mapper.resolve;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.logic.*;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.mapper.def.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * parse dynamic SQL from mapperFile
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-05
 */
public class XmlSqlConfigResolve implements SqlConfigResolve<Node>, ConfigKeys {
    @Override
    public SqlConfig parseSqlConfig(String namespace, Node config) {
        String elementName = config.getNodeName();
        QueryType queryType = QueryType.valueOfTag(elementName.toLowerCase().trim());
        if (queryType == null) {
            throw new UnsupportedOperationException("xml element '" + elementName + "' Unsupported.");
        }

        ArrayDynamicSql dynamicSql = new ArrayDynamicSql();
        parseNodeList(namespace, dynamicSql, config.getChildNodes());
        switch (queryType) {
            case Execute: {
                Map<String, String> cfg = new HashMap<>();
                cfg.put(STATEMENT_TYPE, getNodeAttributeValue(config, "statementType"));
                cfg.put(TIMEOUT, getNodeAttributeValue(config, "timeout"));
                cfg.put(BIND_OUT, getNodeAttributeValue(config, "bindOut"));
                return new ExecuteConfig(dynamicSql, cfg::get);
            }
            case Insert: {
                Map<String, String> cfg = new HashMap<>();
                cfg.put(STATEMENT_TYPE, getNodeAttributeValue(config, "statementType"));
                cfg.put(TIMEOUT, getNodeAttributeValue(config, "timeout"));
                cfg.put(KEY_GENERATED, getNodeAttributeValue(config, "useGeneratedKeys"));
                cfg.put(KEY_PROPERTY, getNodeAttributeValue(config, "keyProperty"));
                cfg.put(KEY_COLUMN, getNodeAttributeValue(config, "keyColumn"));

                // find selectKey
                SelectKeyConfig keyConfig = null;
                NodeList nodeList = config.getChildNodes();
                for (int i = 0, len = nodeList.getLength(); i < len; i++) {
                    Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE && "selectKey".equalsIgnoreCase(node.getNodeName())) {
                        keyConfig = parseSelectKeySqlNode(namespace, node);
                    }
                }
                InsertConfig insertConfig = new InsertConfig(dynamicSql, cfg::get);
                insertConfig.setSelectKey(keyConfig);
                return insertConfig;
            }
            case Update: {
                Map<String, String> cfg = new HashMap<>();
                cfg.put(STATEMENT_TYPE, getNodeAttributeValue(config, "statementType"));
                cfg.put(TIMEOUT, getNodeAttributeValue(config, "timeout"));
                return new UpdateConfig(dynamicSql, cfg::get);
            }
            case Delete: {
                Map<String, String> cfg = new HashMap<>();
                cfg.put(STATEMENT_TYPE, getNodeAttributeValue(config, "statementType"));
                cfg.put(TIMEOUT, getNodeAttributeValue(config, "timeout"));
                return new DeleteConfig(dynamicSql, cfg::get);
            }
            case Select: {
                Map<String, String> cfg = new HashMap<>();
                cfg.put(STATEMENT_TYPE, getNodeAttributeValue(config, "statementType"));
                cfg.put(TIMEOUT, getNodeAttributeValue(config, "timeout"));
                cfg.put(FETCH_SIZE, getNodeAttributeValue(config, "fetchSize"));
                cfg.put(RESULT_SET_TYPE, getNodeAttributeValue(config, "resultSetType"));
                cfg.put(RESULT_MAP_SPACE, namespace);
                cfg.put(RESULT_MAP_ID, getNodeAttributeValue(config, "resultMap"));
                cfg.put(RESULT_TYPE, getNodeAttributeValue(config, "resultType"));
                cfg.put(RESULT_SET_EXTRACTOR, getNodeAttributeValue(config, "resultSetExtractor"));
                cfg.put(RESULT_ROW_CALLBACK, getNodeAttributeValue(config, "resultRowCallback"));
                cfg.put(RESULT_ROW_MAPPER, getNodeAttributeValue(config, "resultRowMapper"));
                cfg.put(BIND_OUT, getNodeAttributeValue(config, "bindOut"));
                return new SelectConfig(dynamicSql, cfg::get);
            }
            case Segment: {
                return new SegmentConfig(dynamicSql, s -> null);
            }
            default:
                throw new UnsupportedOperationException("queryType '" + queryType.name() + "' Unsupported.");
        }
    }

    protected String getNodeAttributeValue(Node node, String attributeKey) {
        Node item = node.getAttributes().getNamedItem(attributeKey);
        return item != null ? item.getNodeValue() : null;
    }

    protected void parseNodeList(String namespace, ArrayDynamicSql parentSqlNode, NodeList nodeList) {
        for (int i = 0, len = nodeList.getLength(); i < len; i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                parseTextSqlNode(namespace, parentSqlNode, node);
            } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = node.getNodeName();
                if ("foreach".equalsIgnoreCase(nodeName)) {
                    parseForeachSqlNode(namespace, parentSqlNode, node);
                } else if ("if".equalsIgnoreCase(nodeName)) {
                    parseIfSqlNode(namespace, parentSqlNode, node);
                } else if ("trim".equalsIgnoreCase(nodeName)) {
                    parseTrimSqlNode(namespace, parentSqlNode, node);
                } else if ("where".equalsIgnoreCase(nodeName)) {
                    parseWhereSqlNode(namespace, parentSqlNode, node);
                } else if ("set".equalsIgnoreCase(nodeName)) {
                    parseSetSqlNode(namespace, parentSqlNode, node);
                } else if ("bind".equalsIgnoreCase(nodeName)) {
                    parseBindSqlNode(namespace, parentSqlNode, node);
                } else if ("choose".equalsIgnoreCase(nodeName)) {
                    parseChooseSqlNode(namespace, parentSqlNode, node);
                } else if ("when".equalsIgnoreCase(nodeName)) {
                    parseWhenSqlNode(namespace, parentSqlNode, node);
                } else if ("otherwise".equalsIgnoreCase(nodeName)) {
                    parseOtherwiseSqlNode(namespace, parentSqlNode, node);
                } else if ("include".equalsIgnoreCase(nodeName)) {
                    parseIncludeSqlNode(namespace, parentSqlNode, node);
                } else if ("selectKey".equalsIgnoreCase(nodeName)) {
                    //skip and Skip special treatment.
                } else {
                    throw new UnsupportedOperationException("Unsupported tags :" + nodeName);
                }
            } else if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
                parseTextSqlNode(namespace, parentSqlNode, node);
            }
        }
    }

    /** append text */
    protected void parseTextSqlNode(String namespace, ArrayDynamicSql parentSqlNode, Node curXmlNode) {
        String sqlNode = curXmlNode.getNodeValue();
        if (parentSqlNode.lastIsText()) {
            ((PlanDynamicSql) parentSqlNode.lastNode()).parsedAppend(sqlNode);
        } else {
            parentSqlNode.addChildNode(new PlanDynamicSql(sqlNode));
        }
    }

    /** passer &lt;foreach&gt; xmlNode */
    protected void parseForeachSqlNode(String namespace, ArrayDynamicSql parentSqlNode, Node curXmlNode) {
        String collection = getNodeAttributeValue(curXmlNode, "collection");
        String item = getNodeAttributeValue(curXmlNode, "item");
        String open = getNodeAttributeValue(curXmlNode, "open");
        String close = getNodeAttributeValue(curXmlNode, "close");
        String separator = getNodeAttributeValue(curXmlNode, "separator");

        ArrayDynamicSql parent = new ForeachDynamicSql(collection, item, open, close, separator);
        parentSqlNode.addChildNode(parent);
        this.parseNodeList(namespace, parent, curXmlNode.getChildNodes());
    }

    /** passer &lt;if&gt; xmlNode */
    protected void parseIfSqlNode(String namespace, ArrayDynamicSql parentSqlNode, Node curXmlNode) {
        String test = getNodeAttributeValue(curXmlNode, "test");

        ArrayDynamicSql parent = new IfDynamicSql(test);
        parentSqlNode.addChildNode(parent);
        this.parseNodeList(namespace, parent, curXmlNode.getChildNodes());
    }

    /** passer &lt;trim&gt; xmlNode */
    protected void parseTrimSqlNode(String namespace, ArrayDynamicSql parentSqlNode, Node curXmlNode) {
        String prefix = getNodeAttributeValue(curXmlNode, "prefix");
        String prefixOverrides = getNodeAttributeValue(curXmlNode, "prefixOverrides");
        String suffix = getNodeAttributeValue(curXmlNode, "suffix");
        String suffixOverrides = getNodeAttributeValue(curXmlNode, "suffixOverrides");

        ArrayDynamicSql parent = new TrimDynamicSql(prefix, suffix, prefixOverrides, suffixOverrides);
        parentSqlNode.addChildNode(parent);
        this.parseNodeList(namespace, parent, curXmlNode.getChildNodes());
    }

    /** passer &lt;where&gt; xmlNode */
    protected void parseWhereSqlNode(String namespace, ArrayDynamicSql parentSqlNode, Node curXmlNode) {
        ArrayDynamicSql parent = new WhereDynamicSql();
        parentSqlNode.addChildNode(parent);
        this.parseNodeList(namespace, parent, curXmlNode.getChildNodes());
    }

    /** passer &lt;set&gt; xmlNode */
    protected void parseSetSqlNode(String namespace, ArrayDynamicSql parentSqlNode, Node curXmlNode) {

        ArrayDynamicSql parent = new SetDynamicSql();
        parentSqlNode.addChildNode(parent);
        this.parseNodeList(namespace, parent, curXmlNode.getChildNodes());
    }

    /** passer &lt;bind&gt; xmlNode */
    protected void parseBindSqlNode(String namespace, ArrayDynamicSql parentSqlNode, Node curXmlNode) {
        String name = getNodeAttributeValue(curXmlNode, "name");
        String value = getNodeAttributeValue(curXmlNode, "value");

        parentSqlNode.addChildNode(new BindDynamicSql(name, value));
    }

    /** passer &lt;choose&gt; xmlNode */
    protected void parseChooseSqlNode(String namespace, ArrayDynamicSql parentSqlNode, Node curXmlNode) {
        ArrayDynamicSql parent = new ChooseDynamicSql();
        parentSqlNode.addChildNode(parent);
        this.parseNodeList(namespace, parent, curXmlNode.getChildNodes());
    }

    /** passer &lt;when&gt; xmlNode */
    protected void parseWhenSqlNode(String namespace, ArrayDynamicSql parentSqlNode, Node curXmlNode) {
        if (!(parentSqlNode instanceof ChooseDynamicSql)) {
            throw new UnsupportedOperationException("the tag `<when>` parent tag must be `<choose>`");
        }
        String test = getNodeAttributeValue(curXmlNode, "test");
        ChooseDynamicSql chooseSqlNode = (ChooseDynamicSql) parentSqlNode;

        ArrayDynamicSql parent = new ArrayDynamicSql();
        chooseSqlNode.addThen(test, parent);
        this.parseNodeList(namespace, parent, curXmlNode.getChildNodes());
    }

    /** passer &lt;otherwise&gt; xmlNode */
    protected void parseOtherwiseSqlNode(String namespace, ArrayDynamicSql parentSqlNode, Node curXmlNode) {
        if (!(parentSqlNode instanceof ChooseDynamicSql)) {
            throw new UnsupportedOperationException("the tag `<otherwise>` parent tag must be `<choose>`");
        }
        ChooseDynamicSql chooseSqlNode = (ChooseDynamicSql) parentSqlNode;

        ArrayDynamicSql parent = new ArrayDynamicSql();
        chooseSqlNode.setDefaultNode(parent);
        this.parseNodeList(namespace, parent, curXmlNode.getChildNodes());
    }

    /** passer &lt;include&gt; xmlNode */
    protected void parseIncludeSqlNode(String namespace, ArrayDynamicSql parentSqlNode, Node curXmlNode) {
        String refId = getNodeAttributeValue(curXmlNode, "refid");
        String macroId = StringUtils.isBlank(namespace) ? refId : (namespace + "." + refId);
        parentSqlNode.addChildNode(new MacroDynamicSql(macroId));
    }

    /** passer &lt;selectKey&gt; xmlNode */
    private SelectKeyConfig parseSelectKeySqlNode(String namespace, Node curXmlNode) {
        Map<String, String> cfg = new HashMap<>();
        cfg.put(STATEMENT_TYPE, getNodeAttributeValue(curXmlNode, "statementType"));
        cfg.put(TIMEOUT, getNodeAttributeValue(curXmlNode, "timeout"));
        cfg.put(FETCH_SIZE, getNodeAttributeValue(curXmlNode, "fetchSize"));
        cfg.put(RESULT_SET_TYPE, getNodeAttributeValue(curXmlNode, "resultSetType"));
        cfg.put(KEY_PROPERTY, getNodeAttributeValue(curXmlNode, "keyProperty"));
        cfg.put(KEY_COLUMN, getNodeAttributeValue(curXmlNode, "keyColumn"));
        cfg.put(ORDER, getNodeAttributeValue(curXmlNode, "order"));
        cfg.put(RESULT_TYPE, getNodeAttributeValue(curXmlNode, "resultType"));
        cfg.put(RESULT_HANDLER, getNodeAttributeValue(curXmlNode, "resultHandler"));

        ArrayDynamicSql parent = new ArrayDynamicSql();
        this.parseNodeList(namespace, parent, curXmlNode.getChildNodes());

        return new SelectKeyConfig(parent, cfg::get);
    }
}