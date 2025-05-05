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
package net.hasor.dbvisitor.dynamic;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.rule.ArgRule;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.error.RuntimeSQLException;

import java.util.Map;

/**
 * Holds information about a parsed SQL statement.
 * @author 赵永春 (zyc@hasor.net)
 * @since 2.0
 */
public class DynamicParsed {
    /** Set of characters that qualify as comment or quotes starting characters. */
    private static final String[] START_SKIP           = new String[] { "'", "\"", "--", "/*" };
    /** Set of characters that qualify as parameter separators, indicating that a parameter name in a SQL String has ended. */
    private static final char[]   PARAMETER_SEPARATORS = new char[] { //
            ' ', ':', ',', ';', '(', ')', '{', '}', '\\', // 其它符号，如分隔符等
            '|', '&', '^', '~', '!',            // 位运算符
            '=', '<', '>',                      // 逻辑运算符
            '+', '-', '*', '%', '/'             // 数学运算符
    };

    /**
     * support like them
     * <pre>
     * select from user where id = ?
     * select from user where id = :id
     * select from user where id = :id.ccc['aaa'][0]
     * select from user where id = &id
     * select from user where id = &id.ccc['aaa'][0]
     * select from user where id = @{abc}
     * select from user where id = #{abc}
     * select from user where id = ${abc}
     * select from user where id = @{abc,true, :name}
     * </pre>
     */
    public static PlanDynamicSql getParsedSql(final String originalSql) {
        PlanDynamicSql segment = new PlanDynamicSql();
        if (StringUtils.isBlank(originalSql)) {
            return segment;
        }

        parsedSqlTo(originalSql, segment);
        return segment;
    }

    /**
     * support like them
     * <pre>
     * select from user where id = ?
     * select from user where id = :id
     * select from user where id = :id.ccc['aaa'][0]
     * select from user where id = &id
     * select from user where id = &id.ccc['aaa'][0]
     * select from user where id = @{abc}
     * select from user where id = #{abc}
     * select from user where id = ${abc}
     * select from user where id = @{abc,true, :name}
     * </pre>
     */
    public static void parsedSqlTo(final String originalSql, PlanDynamicSql segment) {
        if (segment == null) {
            return;
        }

        int positionArgs = 0;

        char[] statement = originalSql.toCharArray();
        int pos = 0;
        int i = 0;
        while (i < statement.length) {
            // skip like this: 'xx', "xxx", --xxx\n, /* */
            int skipToPosition = skipCommentsAndQuotes(statement, i);//从当前为止掠过的长度
            if (i != skipToPosition) {
                segment.appendString(statement, pos, skipToPosition - pos);
                pos = skipToPosition;
                i = skipToPosition;

                if (skipToPosition >= statement.length) {
                    break;
                }
            }

            //
            char c = statement[i];
            String c2 = null;
            if (statement.length > i + 1) {
                c2 = new String(statement, i, 2);
            }

            if (c == '?') {
                if (i != pos) {
                    segment.appendString(statement, pos, i - pos);
                }
                segment.appendPositionArg(positionArgs++);
                i++;
                pos = i;
            } else if (c == ':' || c == '&') {
                int j = i + 1;
                if (j < statement.length && statement[j] == ':' && c == ':') {
                    i = i + 2;// Postgres-style "::" casting operator - to be skipped.
                    continue;
                }

                if (i != pos) {
                    segment.appendString(statement, pos, i - pos);
                }

                while (j < statement.length && !isParameterSeparator(statement[j])) {
                    j++;
                }
                if (j - i > 1) {
                    String parameter = originalSql.substring(i + 1, j);
                    if (statement[i + 1] == '#' || statement[i + 1] == '@') {
                        throw new RuntimeSQLException("expr cannot include '#' or '@', the expr is " + parameter);// 禁止可以造成安全隐患的 #,@操作符
                    }

                    positionArgs++;
                    segment.appendNamedParameter(parameter, c == '&');
                    i = j;
                    pos = j;
                } else {
                    i = j - 1;
                }
            } else if (c2 != null && c2.equals("@{")) {
                if ((i > 0 && statement[i - 1] != '\\') || i == 0) {
                    if (i != pos) {
                        segment.appendString(statement, pos, i - pos);
                    }

                    int j = i + 1;
                    while (j < statement.length && statement[j] != '}') {
                        j++;
                    }

                    if (j - i > 1) {
                        String ruleContent = originalSql.substring(i + 2, j);
                        parserRule(segment, ruleContent);
                        i = j + 1;
                        pos = i;
                    } else {
                        i = j - 1;
                    }
                } else {
                    i++;
                }
            } else if (c2 != null && c2.equals("#{")) {
                if ((i > 0 && statement[i - 1] != '\\') || i == 0) {
                    if (i != pos) {
                        segment.appendString(statement, pos, i - pos);
                    }

                    int j = i + 1;
                    while (j < statement.length && statement[j] != '}') {
                        j++;
                    }

                    if (j - i > 1) {
                        String ruleContent = originalSql.substring(i + 2, j);
                        positionArgs++;
                        parserValue(segment, ruleContent);
                        i = j + 1;
                        pos = i;
                    } else {
                        i = j - 1;
                    }
                } else {
                    i++;
                }
            } else if (c2 != null && c2.equals("${")) {
                if ((i > 0 && statement[i - 1] != '\\') || i == 0) {
                    if (i != pos) {
                        segment.appendString(statement, pos, i - pos);
                    }

                    int j = i + 1;
                    while (j < statement.length && statement[j] != '}') {
                        j++;
                    }

                    if (j - i > 1) {
                        String ruleContent = originalSql.substring(i + 2, j);
                        parserInjection(segment, ruleContent);
                        i = j + 1;
                        pos = i;
                    } else {
                        i = j - 1;
                    }
                } else {
                    i++;
                }
            } else {
                i++;
            }
        }

        // last
        if (i != pos) {
            segment.appendString(statement, pos, i - pos);
        }
    }

    /** Skip over comments and quoted names present in an SQL statement */
    private static int skipCommentsAndQuotes(final char[] statement, final int position) {
        for (int i = 0; i < START_SKIP.length; i++) {
            if (statement[position] == START_SKIP[i].charAt(0)) {
                boolean match = true;
                for (int j = 1; j < START_SKIP[i].length(); j++) {
                    if ((position + j) >= statement.length) {
                        match = false;
                        break;
                    }
                    if (!(statement[position + j] == START_SKIP[i].charAt(j))) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    // -- this is /* ... */
                    if (statement[position] == '/' && statement[position + 1] == '*') {
                        for (int m = position + START_SKIP[i].length(); m < (statement.length - 1); m++) {
                            if (statement[m] == '*' && statement[m + 1] == '/') {
                                return m + 2;
                            }
                        }
                        return statement.length;// last comment not closed properly
                    }
                    // -- this is --
                    else if (statement[position] == '-' && statement[position + 1] == '-') {
                        for (int m = position + START_SKIP[i].length(); m < statement.length; m++) {
                            if (statement[m] == '\n') {
                                return m + 1;
                            }
                        }
                        return statement.length;// last comment not closed properly
                    }
                    // -- this is '
                    else if (statement[position] == '\'') {
                        for (int m = position + START_SKIP[i].length(); m < statement.length; m++) {
                            if (m + 1 >= statement.length) {
                                break;
                            }

                            if (statement[m] == '\'' && statement[m + 1] == '\'') {
                                m++;
                                continue;
                            }
                            if (statement[m] == '\'') {
                                if (m == statement.length) {
                                    return statement.length;// last comment not closed properly
                                } else {
                                    return m + 1;
                                }
                            }
                        }
                        return statement.length;// last comment not closed properly
                    }
                    // -- this is "
                    else if (statement[position] == '\"') {
                        for (int m = position + START_SKIP[i].length(); m < statement.length; m++) {
                            if (m + 1 >= statement.length) {
                                break;
                            }

                            if (statement[m] == '\"' && statement[m + 1] == '\"') {
                                m++;
                                continue;
                            }
                            if (statement[m] == '\"') {
                                if (m == statement.length) {
                                    return statement.length;// last comment not closed properly
                                } else {
                                    return m + 1;
                                }
                            }
                        }
                        return statement.length;// last comment not closed properly
                    }

                    // character sequence ending comment or quote not found
                    return statement.length;
                }
            }
        }
        return position;
    }

    /** Determine whether a parameter name ends at the current position, that is, whether the given character qualifies as a separator. */
    private static boolean isParameterSeparator(final char c) {
        if (Character.isWhitespace(c)) {
            return true;
        }
        for (char separator : PARAMETER_SEPARATORS) {
            if (c == separator) {
                return true;
            }
        }
        return false;
    }

    private static void parserRule(PlanDynamicSql fxQuery, String content) {
        int index = 0;
        int readIndex = 0;
        int length = content.length();
        String ruleName = null;
        String activeExpr = null;
        String ruleContent = null;

        if (index < length) {
            readIndex = nextTokenIndex(index, content);
            ruleName = content.substring(index, readIndex);
            index = readIndex + 1;
        }
        if (index < length) {
            readIndex = nextTokenIndex(index, content);
            activeExpr = content.substring(index, readIndex);
            index = readIndex + 1;
        }
        if (index <= length) {
            ruleContent = content.substring(index);
        }

        if (StringUtils.isBlank(ruleName)) {
            throw new IllegalArgumentException("analysisSQL failed, rule name is null.");
        }

        fxQuery.appendRuleExpr(content, ruleName.trim(), activeExpr, ruleContent);
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

    private static void parserValue(PlanDynamicSql fxQuery, String content) {
        String[] testSplit = content.split(",");
        if (testSplit.length > 10 || testSplit.length == 0) {
            throw new IllegalArgumentException("analysisSQL failed, format error -> '#{valueExpr [,mode= IN|OUT|INOUT] [,jdbcType=INT] [,javaType=java.lang.String] [,typeHandler=YouTypeHandlerClassName]}'");
        }

        boolean noExpr = StringUtils.contains(testSplit[0], "=");
        String expr = noExpr ? "" : testSplit[0];
        Map<String, String> config = ArgRule.INSTANCE.parserConfig(testSplit, noExpr ? 0 : 1, testSplit.length);
        fxQuery.appendNamedParameter(content, expr, config);
    }

    private static void parserInjection(PlanDynamicSql fxQuery, String content) {
        fxQuery.appendInjectionExpr(content);
    }
}