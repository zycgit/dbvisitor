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
package net.hasor.dbvisitor.dialect.provider;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.io.input.AutoCloseInputStream;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlDialect;

/**
 * 公共 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public abstract class AbstractDialect implements SqlDialect {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDialect.class);
    private static final char[] FIRST_CHAR;
    private static final char[] CONTAINS_CHAR;

    static {
        CONTAINS_CHAR = new char[] {//
                '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '=', '+', '~', '`', ' ',     //
                '{', '}', '[', ']', '\\', '|', ';', ':', '\"', '\'', ',', '<', '.', '>', '/', '?'   //
        };
        FIRST_CHAR = new char[] {//
                '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '=', '+', '~', '`', ' ',     //
                '{', '}', '[', ']', '\\', '|', ';', ':', '\"', '\'', ',', '<', '.', '>', '/', '?',  //
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'                                    //
        };
    }

    private static final Map<Class<?>, Set<String>> KEYWORDS_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public Set<String> keywords() {
        return KEYWORDS_CACHE.computeIfAbsent(this.getClass(), key -> {
            Set<String> keyWords = new HashSet<>();
            try {
                List<URL> ins = ResourcesUtils.getResources("/META-INF/custom.keywords");
                if (ins != null) {
                    for (URL in : ins) {
                        try {
                            InputStream input = new AutoCloseInputStream(in.openStream());
                            List<String> strings = IOUtils.readLines(input, StandardCharsets.UTF_8);
                            loadKeyWords(keyWords, strings);
                        } catch (Exception e) {
                            logger.error("load '" + in + "' failed." + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("load 'custom.keywords' failed." + e.getMessage());
            }

            String keyWordsResource = keyWordsResource();
            if (StringUtils.isBlank(keyWordsResource)) {
                return keyWords;
            }

            try {
                List<String> strings = IOUtils.readLines(ResourcesUtils.getResourceAsStream(keyWordsResource), StandardCharsets.UTF_8);
                loadKeyWords(keyWords, strings);
            } catch (Exception e) {
                logger.error("load keywords '" + keyWordsResource + "' failed." + e.getMessage());
            }
            return keyWords;
        });
    }

    private void loadKeyWords(Set<String> keyWords, List<String> strings) {
        for (String term : strings) {
            term = term.trim().toUpperCase();
            if (!StringUtils.isBlank(term) && term.charAt(0) != '#') {
                keyWords.add(term);
            }
        }
    }

    protected String keyWordsResource() {
        return null;
    }

    @Override
    public String tableName(boolean useQualifier, String catalog, String schema, String table) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(catalog)) {
            sb.append(fmtName(useQualifier, catalog));
            sb.append(".");
        }
        if (StringUtils.isNotBlank(schema)) {
            sb.append(fmtName(useQualifier, schema));
            sb.append(".");
        }
        sb.append(fmtName(useQualifier, table));
        return sb.toString();
    }

    @Override
    public String fmtName(boolean useQualifier, String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }

        // if name contains the right qualifier character, force qualifier and escape it
        String rq = rightQualifier();
        if (!rq.isEmpty() && name.contains(rq)) {
            useQualifier = true;
        }

        if (this.keywords().contains(name.toUpperCase())) {
            useQualifier = true;
        }
        if (!useQualifier && !name.isEmpty()) {
            useQualifier = StringUtils.containsAny(String.valueOf(name.charAt(0)), FIRST_CHAR);
        }
        if (!useQualifier && !name.isEmpty()) {
            useQualifier = StringUtils.containsAny(name, CONTAINS_CHAR);
        }
        String leftQualifier = useQualifier ? leftQualifier() : "";
        String rightQualifier = useQualifier ? rq : "";

        // escape right qualifier inside name to prevent identifier injection (SQL standard: double it)
        if (useQualifier && !rq.isEmpty() && name.contains(rq)) {
            name = name.replace(rq, rq + rq);
        }

        return leftQualifier + name + rightQualifier;
    }

    protected String defaultQualifier() {
        return "";
    }

    public String leftQualifier() {
        return this.defaultQualifier();
    }

    public String rightQualifier() {
        return this.defaultQualifier();
    }

    @Override
    public String aliasSeparator() {
        return " ";
    }

    @Override
    public SqlCommandBuilder newBuilder() {
        throw new UnsupportedOperationException("Just a Dialect.");
    }
}
