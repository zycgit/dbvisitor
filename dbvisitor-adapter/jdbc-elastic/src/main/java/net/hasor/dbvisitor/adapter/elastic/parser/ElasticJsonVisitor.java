package net.hasor.dbvisitor.adapter.elastic.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.JdbcArg;

public class ElasticJsonVisitor extends ElasticParserBaseVisitor<Object> {
    private final AdapterRequest request;
    private final AtomicInteger  argIndex;

    public ElasticJsonVisitor(AdapterRequest request, AtomicInteger argIndex) {
        this.request = request;
        this.argIndex = argIndex;
    }

    protected Object getArg() {
        int argIdx = this.argIndex.getAndIncrement();
        String argName = "arg" + (argIdx + 1);
        JdbcArg jdbcArg = this.request.getArgMap().get(argName);
        if (jdbcArg == null) {
            throw new RuntimeException(argName + " not found in request.");
        } else {
            return jdbcArg.getValue();
        }
    }

    protected String getIdentifier(String nodeText) {
        if (nodeText == null) {
            return null;
        }
        char firstChar = nodeText.charAt(0);
        char endChar = nodeText.charAt(nodeText.length() - 1);
        if (firstChar == '"' && endChar == '"') {
            return nodeText.substring(1, nodeText.length() - 1);
        } else {
            return nodeText;
        }
    }

    @Override
    public Object visitJson(ElasticParser.JsonContext ctx) {
        if (ctx.ARG1() != null) {
            return getArg();
        }
        return super.visitJson(ctx);
    }

    @Override
    public Object visitObject(ElasticParser.ObjectContext ctx) {
        Map<String, Object> document = new LinkedHashMap<>();
        if (ctx.pair() != null) {
            for (ElasticParser.PairContext pair : ctx.pair()) {
                String key;
                if (pair.ARG1() != null) {
                    key = String.valueOf(getArg());
                } else {
                    key = getIdentifier(pair.STRING().getText());
                }
                Object value = visit(pair.value());
                document.put(key, value);
            }
        }
        return document;
    }

    @Override
    public Object visitArray(ElasticParser.ArrayContext ctx) {
        List<Object> list = new ArrayList<>();
        if (ctx.value() != null) {
            for (ElasticParser.ValueContext valueCtx : ctx.value()) {
                list.add(visit(valueCtx));
            }
        }
        return list;
    }

    @Override
    public Object visitValue(ElasticParser.ValueContext ctx) {
        if (ctx.ARG1() != null) {
            return getArg();
        }
        if (ctx.STRING() != null) {
            return getIdentifier(ctx.STRING().getText());
        }
        if (ctx.NUMBER() != null) {
            String text = ctx.NUMBER().getText();
            if (text.contains(".") || text.contains("e") || text.contains("E")) {
                return Double.parseDouble(text);
            }
            return Long.parseLong(text);
        }
        if (ctx.TRUE() != null) {
            return Boolean.TRUE;
        }
        if (ctx.FALSE() != null) {
            return Boolean.FALSE;
        }
        if (ctx.NULL() != null) {
            return null;
        }
        return super.visitValue(ctx);
    }
}
