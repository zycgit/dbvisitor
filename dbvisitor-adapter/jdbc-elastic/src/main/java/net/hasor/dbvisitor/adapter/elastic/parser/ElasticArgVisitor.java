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
    public Object visitTerminal(TerminalNode node) {
        int type = node.getSymbol().getType();
        if (type == ElasticLexer.ARG1 || type == ElasticLexer.ARG2) {
            this.argCount++;
        }
        return super.visitTerminal(node);
    }
}
