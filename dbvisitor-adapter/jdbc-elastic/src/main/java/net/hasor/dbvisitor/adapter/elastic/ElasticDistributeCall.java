package net.hasor.dbvisitor.adapter.elastic;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.StringUtils;
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
            if (h.query() != null) {
                ElasticHttpMethod method = h.query().POST() != null ? ElasticHttpMethod.POST : ElasticHttpMethod.GET;
                ElasticOperation op = createOperation(h.query().queryPath(), hints, argIndex, method, request);
                Object jsonBody = resolveJson(h.query().json(), argIndex, request);
                String path = op.getQueryPath();

                if (path.contains("/_search")) {
                    return ElasticCommandsForQuery.execSearch(sync, elasticCmd, op, jsonBody, receive, conn);
                } else if (path.contains("/_count")) {
                    return ElasticCommandsForQuery.execCount(sync, elasticCmd, op, jsonBody, receive);
                } else if (path.contains("/_msearch")) {
                    return ElasticCommandsForQuery.execMultiSearch(sync, elasticCmd, op, jsonBody, receive, conn);
                } else if (path.contains("/_mget")) {
                    return ElasticCommandsForQuery.execMGet(sync, elasticCmd, op, jsonBody, receive, conn);
                }
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
            if (h.update() != null) {
                ElasticOperation op = createOperation(h.update().updatePath1(), hints, argIndex, ElasticHttpMethod.POST, request);
                op.setUseRefresh(((ElasticRequest) request).isIndexRefresh());
                Object jsonBody = resolveJson(h.update().json(), argIndex, request);
                return ElasticCommandsForCrud.execUpdateDoc(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.updateQuery() != null) {
                ElasticOperation op = createOperation(h.updateQuery().updatePath2(), hints, argIndex, ElasticHttpMethod.POST, request);
                op.setUseRefresh(((ElasticRequest) request).isIndexRefresh());
                Object jsonBody = resolveJson(h.updateQuery().json(), argIndex, request);
                return ElasticCommandsForCrud.execUpdateByQuery(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.header() != null) {
                ElasticOperation op = createOperation(h.header().path(), hints, argIndex, ElasticHttpMethod.HEAD, request);
                return ElasticCommandsForQuery.execHeader(sync, elasticCmd, op, receive);
            }

            if (h.insert() != null) {
                ElasticHttpMethod method = h.insert().POST() != null ? ElasticHttpMethod.POST : ElasticHttpMethod.PUT;
                ElasticOperation op = createOperation(h.insert().insertPath(), hints, argIndex, method, request);
                op.setUseRefresh(((ElasticRequest) request).isIndexRefresh());
                Object jsonBody = resolveJson(h.insert().json(), argIndex, request);
                return ElasticCommandsForCrud.execInsert(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.deleteQuery() != null) {
                ElasticOperation op = createOperation(h.deleteQuery().deletePath2(), hints, argIndex, ElasticHttpMethod.POST, request);
                op.setUseRefresh(((ElasticRequest) request).isIndexRefresh());
                Object jsonBody = resolveJson(h.deleteQuery().json(), argIndex, request);
                return ElasticCommandsForCrud.execDeleteByQuery(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.delete() != null) {
                ElasticOperation op = createOperation(h.delete().deletePath1(), hints, argIndex, ElasticHttpMethod.DELETE, request);
                op.setUseRefresh(((ElasticRequest) request).isIndexRefresh());
                return ElasticCommandsForCrud.execDelete(sync, elasticCmd, op, null, receive);
            }
            if (h.cat() != null) {
                ElasticOperation op = createOperation(h.cat().catPath(), hints, argIndex, ElasticHttpMethod.GET, request);
                String path = op.getQueryPath();
                if (StringUtils.startsWithIgnoreCase(path, "/_cat/indices")) {
                    return ElasticCommandsForCat.execCatIndices(sync, elasticCmd, op, receive);
                } else if (StringUtils.startsWithIgnoreCase(path, "/_cat/nodes")) {
                    return ElasticCommandsForCat.execCatNodes(sync, elasticCmd, op, receive);
                } else if (StringUtils.startsWithIgnoreCase(path, "/_cat/health")) {
                    return ElasticCommandsForCat.execCatHealth(sync, elasticCmd, op, receive);
                }
            }
            if (h.refresh() != null) {
                ElasticHttpMethod method = h.refresh().POST() != null ? ElasticHttpMethod.POST : ElasticHttpMethod.GET;
                ElasticOperation op = createOperation(h.refresh().refreshPath(), hints, argIndex, method, request);
                return ElasticCommandsForIndex.execIndexRefresh(sync, elasticCmd, op, receive);
            }
            if (h.reindex() != null) {
                ElasticOperation op = createOperation(h.reindex().reindexPath(), hints, argIndex, ElasticHttpMethod.POST, request);
                Object jsonBody = resolveJson(h.reindex().json(), argIndex, request);
                return ElasticCommandsForIndex.execReindex(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.explain() != null) {
                ElasticHttpMethod method = h.explain().POST() != null ? ElasticHttpMethod.POST : ElasticHttpMethod.GET;
                ElasticOperation op = createOperation(h.explain().explainPath(), hints, argIndex, method, request);
                Object jsonBody = resolveJson(h.explain().json(), argIndex, request);
                return ElasticCommandsForQuery.execExplain(sync, elasticCmd, op, jsonBody, receive);
            }
            if (h.source() != null) {
                ElasticHttpMethod method = h.source().POST() != null ? ElasticHttpMethod.POST : ElasticHttpMethod.GET;
                ElasticOperation op = createOperation(h.source().sourcePath(), hints, argIndex, method, request);
                Object jsonBody = resolveJson(h.source().json(), argIndex, request);
                return ElasticCommandsForQuery.execGetSource(sync, elasticCmd, op, jsonBody, receive, conn);
            }
            if (h.generic() != null) {
                ElasticHttpMethod method;
                if (h.generic().GET() != null) {
                    method = ElasticHttpMethod.GET;
                } else if (h.generic().POST() != null) {
                    method = ElasticHttpMethod.POST;
                } else if (h.generic().PUT() != null) {
                    method = ElasticHttpMethod.PUT;
                } else {
                    method = ElasticHttpMethod.DELETE;
                }
                ElasticOperation op = createOperation(h.generic().path(), hints, argIndex, method, request);
                op.setUseRefresh(((ElasticRequest) request).isIndexRefresh());
                Object jsonBody = resolveJson(h.generic().json(), argIndex, request);

                if (method == ElasticHttpMethod.GET) {
                    return ElasticCommandsForGeneric.execGeneric(sync, elasticCmd, op, jsonBody, receive);
                } else {
                    return ElasticCommandsForCrud.execInsert(sync, elasticCmd, op, jsonBody, receive);
                }
            }

            sync.failed(new SQLException("unknown command."));
            return sync;
        } catch (Exception e) {
            sync.failed(readError(e, request));
            return sync;
        }
    }

    private static SQLException readError(Exception e, AdapterRequest request) {
        if (e instanceof ResponseException) {
            String errorMsg = null;
            Response response = ((ResponseException) e).getResponse();
            try {
                if (response.getEntity() != null) {
                    try (InputStream content = response.getEntity().getContent()) {
                        ObjectMapper mapper = ((ElasticRequest) request).getJson();
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
                        if (StringUtils.contains(num, ".")) {
                            value = Double.parseDouble(num);
                        } else {
                            value = Long.parseLong(num);
                        }
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

        return new ElasticOperation(method, endpoint, queryPath, queryParams, hints, request);
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
