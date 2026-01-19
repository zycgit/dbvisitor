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
package net.hasor.dbvisitor.adapter.redis.parser;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser.CommandContext;

public class JedisArgVisitor extends RedisParserBaseVisitor {
    private final List<CommandContext> commandList = new ArrayList<>();
    private       int                  argCount    = 0;

    public int getArgCount() {
        return this.argCount;
    }

    public void reset() {
        this.argCount = 0;
        this.commandList.clear();
    }

    public List<CommandContext> getCommandList() {
        return this.commandList;
    }

    @Override
    public Object visitCommand(CommandContext ctx) {
        this.commandList.add(ctx);
        return super.visitCommand(ctx);
    }

    @Override
    public Object visitDecimal(RedisParser.DecimalContext ctx) {
        if (ctx.ARG() != null) {
            this.argCount++;
        }

        return super.visitDecimal(ctx);
    }

    @Override
    public Object visitInteger(RedisParser.IntegerContext ctx) {
        if (ctx.ARG() != null) {
            this.argCount++;
        }

        return super.visitInteger(ctx);
    }

    @Override
    public Object visitDecimalScore(RedisParser.DecimalScoreContext ctx) {
        if (ctx.ARG() != null) {
            this.argCount++;
        }

        return super.visitDecimalScore(ctx);
    }

    @Override
    public Object visitIdentifier(RedisParser.IdentifierContext ctx) {
        if (ctx.ARG() != null) {
            this.argCount++;
        }

        return super.visitIdentifier(ctx);
    }
}
