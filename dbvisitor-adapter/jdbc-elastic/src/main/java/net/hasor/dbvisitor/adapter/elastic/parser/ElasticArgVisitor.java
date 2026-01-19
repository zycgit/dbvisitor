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
package net.hasor.dbvisitor.adapter.elastic.parser;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser.HintCommandContext;

public class ElasticArgVisitor extends ElasticParserBaseVisitor<Object> {
    private final List<HintCommandContext> commandList = new ArrayList<>();
    private       int                      argCount    = 0;

    public int getArgCount() {
        return this.argCount;
    }

    public void reset() {
        this.argCount = 0;
    }

    public List<HintCommandContext> getCommandList() {
        return this.commandList;
    }

    @Override
    public Object visitHintCommand(HintCommandContext ctx) {
        this.commandList.add(ctx);
        return super.visitHintCommand(ctx);
    }

    @Override
    public Object visitHintValue(ElasticParser.HintValueContext ctx) {
        if (ctx.ARG1() != null) {
            this.argCount++;
        }
        return super.visitHintValue(ctx);
    }

    @Override
    public Object visitJson(ElasticParser.JsonContext ctx) {
        if (ctx.ARG1() != null) {
            this.argCount++;
        }
        return super.visitJson(ctx);
    }

    @Override
    public Object visitPair(ElasticParser.PairContext ctx) {
        if (ctx.ARG1() != null) {
            this.argCount++;
        }
        return super.visitPair(ctx);
    }

    @Override
    public Object visitValue(ElasticParser.ValueContext ctx) {
        if (ctx.ARG1() != null) {
            this.argCount++;
        }
        return super.visitValue(ctx);
    }

    @Override
    public Object visitPathValue(ElasticParser.PathValueContext ctx) {
        if (ctx.ARG2() != null && !ctx.ARG2().isEmpty()) {
            this.argCount += ctx.ARG2().size();
        }
        return super.visitPathValue(ctx);
    }

    @Override
    public Object visitQueryParam(ElasticParser.QueryParamContext ctx) {
        if (ctx.ARG2() != null) {
            this.argCount++;
        }
        return super.visitQueryParam(ctx);
    }
}
