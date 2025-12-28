package net.hasor.dbvisitor.adapter.elastic;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticJsonVisitor;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser.EsCmdContext;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser.HintCommandContext;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser.HintContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.http.StatusLine;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;

class ElasticDistributeCall {
    public static Future<?> execElasticCmd(Future<Object> sync, ElasticCmd elasticCmd, HintCommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, ElasticConn conn) throws SQLException {
        try {
            AtomicInteger argIndex = new AtomicInteger(startArgIdx);
            Map<String, Object> hints = readHints(argIndex, request, c.hint());

            EsCmdContext h = c.esCmd();
            if (h.search() != null) {
                ElasticHttpMethod method = h.search().POST() != null ? ElasticHttpMethod.POST : ElasticHttpMethod.GET;
                ElasticOperation op = createOperation(h.search().searchPath(), hints, argIndex, method, request);
                Object jsonBody = resolveJson(h.search().json(), argIndex, request);
                return ElasticCommandsForCrud.execSearch(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.count() != null) {
                ElasticHttpMethod method = h.count().POST() != null ? ElasticHttpMethod.POST : ElasticHttpMethod.GET;
                ElasticOperation op = createOperation(h.count().countPath(), hints, argIndex, method, request);
                Object jsonBody = resolveJson(h.count().json(), argIndex, request);
                return ElasticCommandsForCrud.execCount(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.msearch() != null) {
                ElasticHttpMethod method = h.msearch().POST() != null ? ElasticHttpMethod.POST : ElasticHttpMethod.GET;
                ElasticOperation op = createOperation(h.msearch().msearchPath(), hints, argIndex, method, request);
                Object jsonBody = resolveJson(h.msearch().json(), argIndex, request);
                return ElasticCommandsForCrud.execMultiSearch(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.mapping() != null) {
                ElasticHttpMethod method;
                if (h.mapping().PUT() != null) {
                    method = ElasticHttpMethod.PUT;
                } else if (h.mapping().POST() != null) {
                    method = ElasticHttpMethod.POST;
                } else {
                    method = ElasticHttpMethod.GET;
                }
                ElasticOperation op = createOperation(h.mapping().mappingPath(), hints, argIndex, method, request);
                Object jsonBody = resolveJson(h.mapping().json(), argIndex, request);
                return ElasticCommandsForIndex.execIndexMapping(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.settings() != null) {
                ElasticHttpMethod method = h.settings().PUT() != null ? ElasticHttpMethod.PUT : ElasticHttpMethod.GET;
                ElasticOperation op = createOperation(h.settings().settingsPath(), hints, argIndex, method, request);
                Object jsonBody = resolveJson(h.settings().json(), argIndex, request);
                if (method == ElasticHttpMethod.GET) {
                    return ElasticCommandsForIndex.execGetIndexSettings(sync, elasticCmd, op, receive);
                } else {
                    return ElasticCommandsForIndex.execSetIndexSettings(sync, elasticCmd, op, jsonBody, receive);
                }
            }
            if (h.open() != null) {
                ElasticOperation op = createOperation(h.open().openPath(), hints, argIndex, ElasticHttpMethod.POST, request);
                return ElasticCommandsForIndex.execIndexOpen(sync, elasticCmd, op, receive);
            }
            if (h.close() != null) {
                ElasticOperation op = createOperation(h.close().closePath(), hints, argIndex, ElasticHttpMethod.POST, request);
                return ElasticCommandsForIndex.execIndexClose(sync, elasticCmd, op, receive);
            }
            if (h.aliases() != null) {
                ElasticHttpMethod method = h.aliases().POST() != null ? ElasticHttpMethod.POST : ElasticHttpMethod.GET;
                ElasticOperation op = createOperation(h.aliases().aliasesPath(), hints, argIndex, method, request);
                Object jsonBody = resolveJson(h.aliases().json(), argIndex, request);
                return ElasticCommandsForIndex.execAliases(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.doc() != null) {
                ElasticHttpMethod method = h.doc().PUT() != null ? ElasticHttpMethod.PUT : ElasticHttpMethod.POST;
                ElasticOperation op = createOperation(h.doc().docPath(), hints, argIndex, method, request);
                Object jsonBody = resolveJson(h.doc().json(), argIndex, request);
                return ElasticCommandsForCrud.execInsertDoc(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.create() != null) {
                ElasticHttpMethod method = h.create().PUT() != null ? ElasticHttpMethod.PUT : ElasticHttpMethod.POST;
                ElasticOperation op = createOperation(h.create().createPath(), hints, argIndex, method, request);
                Object jsonBody = resolveJson(h.create().json(), argIndex, request);
                return ElasticCommandsForCrud.execInsertCreate(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.update() != null) {
                ElasticOperation op = createOperation(h.update().updateDocPath(), hints, argIndex, ElasticHttpMethod.POST, request);
                Object jsonBody = resolveJson(h.update().json(), argIndex, request);
                return ElasticCommandsForCrud.execUpdateDoc(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.updateByQuery() != null) {
                ElasticOperation op = createOperation(h.updateByQuery().updateByQueryPath(), hints, argIndex, ElasticHttpMethod.POST, request);
                Object jsonBody = resolveJson(h.updateByQuery().json(), argIndex, request);
                return ElasticCommandsForCrud.execUpdateByQuery(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.deleteByQuery() != null) {
                ElasticOperation op = createOperation(h.deleteByQuery().deleteByQueryPath(), hints, argIndex, ElasticHttpMethod.POST, request);
                Object jsonBody = resolveJson(h.deleteByQuery().json(), argIndex, request);
                return ElasticCommandsForCrud.execDeleteByQuery(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.cat() != null) {
                ElasticOperation op = createOperation(h.cat().catPath(), hints, argIndex, ElasticHttpMethod.GET, request);
                return ElasticCommandsForCat.execCat(sync, elasticCmd, op, null, receive);
            }
            if (h.generic() != null) {
                ElasticHttpMethod method = ElasticHttpMethod.valueOf(h.generic().method().getText().toUpperCase());
                ElasticOperation op = createOperation(h.generic().path(), hints, argIndex, method, request);
                Object jsonBody = resolveJson(h.generic().json(), argIndex, request);
                return ElasticCommandsForCrud.execGeneric(sync, elasticCmd, op, jsonBody, receive);
            }

            sync.failed(new SQLException("unknown command."));
            return sync;
        } catch (Exception e) {
            sync.failed(readError(e));
            return sync;
        }
    }

    private static SQLException readError(Exception e) {
        if (e instanceof ResponseException) {
            String errorMsg = null;
            Response response = ((ResponseException) e).getResponse();
            try {
                if (response.getEntity() != null) {
                    try (InputStream content = response.getEntity().getContent()) {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(content);
                        if (jsonNode.has("error")) {
                            JsonNode errorNode = jsonNode.get("error");
                            if (errorNode.isTextual()) {
                                errorMsg = errorNode.asText();
                            } else if (errorNode.has("reason")) {
                                errorMsg = errorNode.get("reason").asText();
                            } else {
                                errorMsg = errorNode.toString();
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
                return new SQLException(e.getMessage(), e);
            }

            StatusLine statusLine = response.getStatusLine();
            if (errorMsg == null) {
                errorMsg = statusLine.getStatusCode() + " " + statusLine.getReasonPhrase();
            }
            return new SQLException(errorMsg, String.valueOf("E" + statusLine.getStatusCode()), e);
        } else {
            return new SQLException(e.getMessage(), e);
        }
    }

    private static Object resolveJson(ElasticParser.JsonContext ctx, AtomicInteger argIndex, AdapterRequest request) {
        if (ctx == null) {
            return null;
        }
        return new ElasticJsonVisitor(request, argIndex).visit(ctx);
    }

    private static Map<String, Object> readHints(AtomicInteger argIndex, AdapterRequest request, List<HintContext> hint) throws Exception {
        Map<String, Object> hintMap = new LinkedCaseInsensitiveMap<>();
        if (hint == null || hint.isEmpty()) {
            return hintMap;
        }

        for (ElasticParser.HintContext ctx : hint) {
            if (ctx == null || ctx.hints() == null) {
                continue;
            }
            for (ElasticParser.HintItContext it : ctx.hints().hintIt()) {
                if (it == null || it.hintName == null) {
                    continue;
                }
                String key = it.hintName.getText();
                Object value;
                ElasticParser.HintValueContext hv = it.hintVal;
                if (hv == null) {
                    value = Boolean.TRUE;
                } else if (hv.ARG1() != null) {
                    value = ElasticCommands.getArg(argIndex, request);
                } else if (hv.STRING() != null) {
                    String text = hv.STRING().getText();
                    value = text.length() >= 2 ? text.substring(1, text.length() - 1) : text;
                } else if (hv.NUMBER() != null) {
                    String num = hv.NUMBER().getText();
                    try {
                        value = num.contains(".") ? Double.parseDouble(num) : Long.parseLong(num);
                    } catch (NumberFormatException e) {
                        value = num;
                    }
                } else if (hv.TRUE() != null) {
                    value = Boolean.TRUE;
                } else if (hv.FALSE() != null) {
                    value = Boolean.FALSE;
                } else {
                    value = hv.getText();
                }
                hintMap.put(key, value);
            }
        }
        return hintMap;
    }

    private static ElasticOperation createOperation(ParseTree pathCtx, Map<String, Object> hints, AtomicInteger argIndex, ElasticHttpMethod method, AdapterRequest request) throws SQLException {
        StringBuilder endpointBuilder = new StringBuilder();
        if (pathCtx != null) {
            buildEndpoint(pathCtx, endpointBuilder, argIndex, request);
        }

        String endpoint = endpointBuilder.toString();
        String queryPath = endpoint;
        Map<String, Object> queryParams = new LinkedCaseInsensitiveMap<>();
        int idx = endpoint.indexOf('?');
        if (idx >= 0) {
            queryPath = endpoint.substring(0, idx);
            String qs = endpoint.substring(idx + 1);
            String[] params = qs.split("&");
            for (String param : params) {
                int eqIdx = param.indexOf('=');
                if (eqIdx > 0) {
                    String key = param.substring(0, eqIdx);
                    String value = param.substring(eqIdx + 1);
                    queryParams.put(key, value);
                } else if (!param.isEmpty()) {
                    queryParams.put(param, "");
                }
            }
        }

        return new ElasticOperation(method, endpoint, queryPath, queryParams, hints, request, argIndex);
    }

    private static void buildEndpoint(ParseTree tree, StringBuilder builder, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        if (tree instanceof TerminalNode) {
            TerminalNode node = (TerminalNode) tree;
            String text = node.getText();
            if ("{?}".equals(text)) {
                Object arg = ElasticCommands.getArg(argIndex, request);
                builder.append(arg);
            } else {
                builder.append(text);
            }
        } else {
            for (int i = 0; i < tree.getChildCount(); i++) {
                buildEndpoint(tree.getChild(i), builder, argIndex, request);
            }
        }
    }
}
