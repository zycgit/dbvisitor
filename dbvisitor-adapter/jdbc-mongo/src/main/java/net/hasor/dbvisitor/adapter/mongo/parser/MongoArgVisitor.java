package net.hasor.dbvisitor.adapter.mongo.parser;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.CommandContext;

public class MongoArgVisitor extends MongoParserBaseVisitor<Object> {
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
    public Object visitCommand(MongoParser.CommandContext ctx) {
        this.commandList.add(ctx);
        return super.visitCommand(ctx);
    }

    @Override
    public Object visitDatabaseName(MongoParser.DatabaseNameContext ctx) {
        if (ctx.ARG() != null) {
            this.argCount++;
        }
        return super.visitDatabaseName(ctx);
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

    @Override
    public Object visitLiteral(MongoParser.LiteralContext ctx) {
        if (ctx.ARG() != null) {
            this.argCount++;
        }
        return super.visitLiteral(ctx);
    }
}
