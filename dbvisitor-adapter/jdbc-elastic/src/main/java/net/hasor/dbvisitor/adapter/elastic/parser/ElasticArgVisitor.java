package net.hasor.dbvisitor.adapter.elastic.parser;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser.HintCommandContext;
import org.antlr.v4.runtime.tree.TerminalNode;

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
