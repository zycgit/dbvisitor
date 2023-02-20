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
package net.hasor.dbvisitor.faker.dsl;
import net.hasor.dbvisitor.faker.dsl.model.DataModel;
import net.hasor.dbvisitor.faker.dsl.parser.TypeProcessorDSLLexer;
import net.hasor.dbvisitor.faker.dsl.parser.TypeProcessorDSLParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 配置
 * @version : 2023-02-10
 * @author 赵永春 (zyc@hasor.net)
 */
public class TypeProcessConfSet {
    private final List<TypeProcessConf>              all        = new ArrayList<>();
    private final Map<String, List<TypeProcessConf>> columnConf = new HashMap<>();
    private final Map<String, DataModel>             defConf    = new HashMap<>();
    private final Map<String, String>                defThrow   = new HashMap<>();
    private       String                             databaseType;
    private       URI                                source;

    public String getDatabaseType() {
        return this.databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public URI getSource() {
        return this.source;
    }

    public void setSource(URI source) {
        this.source = source;
    }

    public void putConfig(String colType, List<TypeProcessConf> lastConfList) {
        if (colType.equals("*")) {
            this.all.addAll(lastConfList);
        } else {
            this.columnConf.put(colType, lastConfList);
        }
    }

    public void putDefConfig(String defConfName, DataModel data) {
        this.defConf.put(defConfName, data);
    }

    public void putThrow(String typeName, String throwString) {
        this.defThrow.put(typeName, throwString);
    }

    public String getThrow(String typeName) {
        return this.defThrow.get(typeName);
    }

    public Set<String> getConfigKeys() {
        return this.columnConf.keySet();
    }

    public Set<String> getThrowKeys() {
        return this.defThrow.keySet();
    }

    public List<TypeProcessConf> getConfig(String colType) {
        return this.columnConf.get(colType);
    }

    public DataModel getDefConfig(String defConfName) {
        return this.defConf.get(defConfName);
    }

    void finishParser() {
        if (!this.all.isEmpty()) {
            for (String keys : this.getConfigKeys()) {
                this.columnConf.get(keys).addAll(0, this.all);
            }
        }
    }

    public static TypeProcessConfSet parserTypeProcessConf(InputStream inputStream, Charset charset) throws DslException, IOException {
        CharStream charStream = CharStreams.fromStream(Objects.requireNonNull(inputStream), charset);
        TypeProcessorDSLLexer lexer = new TypeProcessorDSLLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

        TypeProcessorDSLParser dslParser = new TypeProcessorDSLParser(new CommonTokenStream(lexer));
        dslParser.removeErrorListeners();
        dslParser.addErrorListener(ThrowingErrorListener.INSTANCE);
        InnerTypeProcessDSLVisitor visitor = new InnerTypeProcessDSLVisitor();
        visitor.visit(dslParser.rootInstSet());
        return visitor.getConfSet();
    }
}