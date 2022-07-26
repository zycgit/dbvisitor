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
package net.hasor.dbvisitor.dal.dynamic.segment;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dal.dynamic.DynamicSql;
import net.hasor.dbvisitor.dal.dynamic.rule.ArgRule;
import net.hasor.dbvisitor.dal.dynamic.tokens.GenericTokenParser;

import java.util.Map;

/**
 * DynamicSql 解析器，仅支持：@{}、#{}、${} 三种写法。
 * @version : 2021-05-19
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlSegmentParser {
    public static DynamicSql analysisSQL(String fragmentString) {
        final DefaultSqlSegment fxSql = new DefaultSqlSegment();
        final String result = new GenericTokenParser(new String[] { "@{", "#{", "${" }, "}", (builder, token, content) -> {
            fxSql.appendString(builder.toString());
            if (token.equalsIgnoreCase("@{")) {
                parserRule(fxSql, content);
            }
            if (token.equalsIgnoreCase("${")) {
                parserPlaceholder(fxSql, content);
            }
            if (token.equalsIgnoreCase("#{")) {
                parserValue(fxSql, content);
            }
            builder.delete(0, builder.length());
            return "";
        }).parse(fragmentString);
        fxSql.appendString(result);
        return fxSql;
    }

    private static void parserPlaceholder(DefaultSqlSegment fxQuery, String content) {
        fxQuery.appendPlaceholderExpr(content);
    }

    public static void parserRule(DefaultSqlSegment fxQuery, String content) {
        int index = 0;
        int readIndex = 0;
        int length = content.length();
        String ruleName = null;
        String activeExpr = "true";
        String ruleContent = null;

        if (index < length) {
            readIndex = nextTokenIndex(index, content);
            ruleName = content.substring(index, readIndex);
            index = readIndex + 1;
        }
        if (index < length) {
            readIndex = nextTokenIndex(index, content);
            activeExpr = content.substring(index, readIndex);
            if (StringUtils.isBlank(activeExpr)) {
                activeExpr = "true";
            }
            index = readIndex + 1;
        }
        if (index < length) {
            ruleContent = content.substring(index);
        }

        if (StringUtils.isBlank(ruleName)) {
            throw new IllegalArgumentException("analysisSQL failed, rule name is null.");
        }

        fxQuery.appendRuleExpr(ruleName, activeExpr, ruleContent);
    }

    private static int nextTokenIndex(int start, String content) {
        int index = start;

        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean inEscape = false;
        for (; index < content.length(); index++) {
            char c = content.charAt(index);

            if (inEscape) {
                inEscape = false;
                continue;
            }

            if ('\'' == c) {

                if (!inSingleQuotes && !inDoubleQuotes) {
                    inSingleQuotes = true;
                } else if (inSingleQuotes) {
                    inSingleQuotes = false;
                }

            } else if ('\"' == c) {

                if (!inDoubleQuotes && !inSingleQuotes) {
                    inDoubleQuotes = true;
                } else if (inDoubleQuotes) {
                    inDoubleQuotes = false;
                }

            } else if ('\\' == c) {

                if (inDoubleQuotes || inSingleQuotes) {
                    inEscape = true;
                }

            } else if (',' == c) {
                if (!inDoubleQuotes && !inSingleQuotes) {
                    return index;
                }
            }
        }

        return index;
    }

    private static void parserValue(DefaultSqlSegment fxQuery, String content) {
        String[] testSplit = content.split(",");
        if (testSplit.length > 6 || testSplit.length == 0) {
            throw new IllegalArgumentException("analysisSQL failed, format error -> '#{valueExpr [,mode= IN|OUT|INOUT] [,jdbcType=INT] [,javaType=java.lang.String] [,typeHandler=YouTypeHandlerClassName]}'");
        }

        boolean noExpr = StringUtils.contains(testSplit[0], "=");
        String expr = noExpr ? "" : testSplit[0];
        Map<String, String> config = ArgRule.INSTANCE.parserConfig(testSplit, noExpr ? 0 : 1, testSplit.length);
        fxQuery.appendValueExpr(expr, config);
    }
}
