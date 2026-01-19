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
package net.hasor.dbvisitor.adapter.mongo.parser;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.HintCommandContext;

public class MongoArgVisitor extends MongoParserBaseVisitor<Object> {
    private final List<HintCommandContext> commandList = new ArrayList<>();
    private       int                      argCount    = 0;

    public int getArgCount() {
        return this.argCount;
    }

    public void reset() {
        this.argCount = 0;
        this.commandList.clear();
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
    public Object visitDatabaseName(MongoParser.DatabaseNameContext ctx) {
        if (ctx.ARG() != null) {
            this.argCount++;
        }
        return super.visitDatabaseName(ctx);
    }

    @Override
    public Object visitLiteral(MongoParser.LiteralContext ctx) {
        if (ctx.ARG() != null) {
            this.argCount++;
        }
        return super.visitLiteral(ctx);
    }

    @Override
    public Object visitCollection(MongoParser.CollectionContext ctx) {
        if (ctx.ARG() != null) {
            this.argCount++;
        }
        return super.visitCollection(ctx);
    }

    @Override
    public Object visitPropertyName(MongoParser.PropertyNameContext ctx) {
        if (ctx.ARG() != null) {
            this.argCount++;
        }
        return super.visitPropertyName(ctx);
    }

}
