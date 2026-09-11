/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.parser;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HintCommandContext;

public class MilvusArgVisitor extends MilvusParserBaseVisitor<Object> {
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
    public Object visitTerminal(org.antlr.v4.runtime.tree.TerminalNode node) {
        if (node.getSymbol().getType() == MilvusParser.ARG) {
            this.argCount++;
        }
        return super.visitTerminal(node);
    }
}
