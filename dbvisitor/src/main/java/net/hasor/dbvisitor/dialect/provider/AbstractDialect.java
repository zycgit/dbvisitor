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
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.io.input.AutoCloseInputStream;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 公共 SqlDialect 实现
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractDialect implements SqlDialect, ConditionSqlDialect {
    private static final Logger      logger = LoggerFactory.getLogger(AbstractDialect.class);
    private              Set<String> keyWords;

    @Override
    public final Set<String> keywords() {
        if (this.keyWords == null) {
            this.keyWords = new HashSet<>();

            try {
                List<URL> ins = ResourcesUtils.getResources("/META-INF/custom.keywords");
                if (ins != null) {
                    for (URL in : ins) {
                        try {
                            InputStream input = new AutoCloseInputStream(in.openStream());
                            List<String> strings = IOUtils.readLines(input, StandardCharsets.UTF_8);
                            this.loadKeyWords(strings);
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
                return this.keyWords;
            }

            try {
                List<String> strings = IOUtils.readLines(ResourcesUtils.getResourceAsStream(keyWordsResource), StandardCharsets.UTF_8);
                this.loadKeyWords(strings);
            } catch (Exception e) {
                logger.error("load keywords '" + keyWordsResource + "' failed." + e.getMessage());
            }
        }
        return this.keyWords;
    }

    private void loadKeyWords(List<String> strings) {
        for (String term : strings) {
            term = term.trim().toUpperCase();
            if (!StringUtils.isBlank(term) && term.charAt(0) != '#') {
                this.keyWords.add(term);
            }
        }
    }

    protected String keyWordsResource() {
        return null;
    }

    @Override
    public String tableName(boolean useQualifier, String catalog, String schema, String table) {
        if (StringUtils.isBlank(schema)) {
            return fmtName(useQualifier, table);
        } else {
            return fmtName(useQualifier, schema) + "." + fmtName(useQualifier, table);
        }
    }

    @Override
    public String fmtName(boolean useQualifier, String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }
        if (this.keywords().contains(name.toUpperCase())) {
            useQualifier = true;
        }
        String leftQualifier = useQualifier ? leftQualifier() : "";
        String rightQualifier = useQualifier ? rightQualifier() : "";
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
}
