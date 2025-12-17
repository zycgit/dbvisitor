package net.hasor.dbvisitor.adapter.elastic;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class ElasticDistributeCall {
    public static Future<?> execElasticCmd(Future<Object> sync, ElasticCmd elasticCmd, HintCommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, ElasticConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        Map<String, Object> hints = readHints(argIndex, request, c.hint());

        EsCmdContext h = c.esCmd();
        if (h.index() != null) {
            return execIndex(sync, elasticCmd, h.index(), request, receive, argIndex, hints, conn);
        }
        if (h.select() != null) {
            return execSelect(sync, elasticCmd, h.select(), request, receive, argIndex, hints, conn);
        }
        if (h.insert() != null) {
            return execInsert(sync, elasticCmd, h.insert(), request, receive, argIndex, hints, conn);
        }
        if (h.update() != null) {
            return execUpdate(sync, elasticCmd, h.update(), request, receive, argIndex, hints, conn);
        }
        if (h.delete() != null) {
            return execDelete(sync, elasticCmd, h.delete(), request, receive, argIndex, hints, conn);
        }
        if (h.generic() != null) {
            return ElasticCommandsForOther.execGeneric(sync, elasticCmd, h.generic(), request, receive, argIndex, hints, conn);
        }
        throw new SQLException("unknown command.");
    }

    private static Future<?> execIndex(Future<Object> sync, ElasticCmd elasticCmd, IndexContext c, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        if (c.aliasesPath() != null) {
            return ElasticCommandsForIndex.execAliases(sync, elasticCmd, c, request, receive, argIndex, hints, conn);
        }

        IndexMgmtPathContext mgmtPath = c.indexMgmtPath();
        if (mgmtPath != null) {
            if (mgmtPath.MAPPING_KW() != null) {
                return ElasticCommandsForIndex.execIndexMapping(sync, elasticCmd, c, mgmtPath, request, receive, argIndex, hints, conn);
            }
            if (mgmtPath.SETTINGS_KW() != null) {
                return ElasticCommandsForIndex.execIndexSettings(sync, elasticCmd, c, mgmtPath, request, receive, argIndex, hints, conn);
            }
            if (mgmtPath.OPEN_KW() != null) {
                return ElasticCommandsForIndex.execIndexOpen(sync, elasticCmd, c, mgmtPath, request, receive, argIndex, hints, conn);
            }
            if (mgmtPath.CLOSE_KW() != null) {
                return ElasticCommandsForIndex.execIndexClose(sync, elasticCmd, c, mgmtPath, request, receive, argIndex, hints, conn);
            }
            return ElasticCommandsForIndex.execIndexGeneric(sync, elasticCmd, c, mgmtPath, request, receive, argIndex, hints, conn);
        }

        throw new SQLException("unsupported index command.");
    }

    private static Future<?> execSelect(Future<Object> sync, ElasticCmd elasticCmd, SelectContext c, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        SelectPathContext sp = c.selectPath();
        if (sp != null) {
            if (sp.SEARCH_KW() != null) {
                return ElasticCommandsForCrud.execSearch(sync, elasticCmd, c, sp, request, receive, argIndex, hints, conn);
            }
            if (sp.COUNT_KW() != null) {
                return ElasticCommandsForCrud.execCount(sync, elasticCmd, c, sp, request, receive, argIndex, hints, conn);
            }
            if (sp.MSEARCH_KW() != null) {
                return ElasticCommandsForCrud.execMultiSearch(sync, elasticCmd, c, sp, request, receive, argIndex, hints, conn);
            }
            return ElasticCommandsForCrud.execSelectGeneric(sync, elasticCmd, c, sp, request, receive, argIndex, hints, conn);
        }
        // GET/HEAD with arbitrary path
        return ElasticCommandsForCrud.execSelectPath(sync, elasticCmd, c, request, receive, argIndex, hints, conn);
    }

    private static Future<?> execInsert(Future<Object> sync, ElasticCmd elasticCmd, InsertContext c, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        InsertPathContext ip = c.insertPath();
        if (ip != null) {
            if (ip.DOC_KW() != null) {
                return ElasticCommandsForCrud.execInsertDoc(sync, elasticCmd, c, ip, request, receive, argIndex, hints, conn);
            }
            if (ip.CREATE_KW() != null) {
                return ElasticCommandsForCrud.execInsertCreate(sync, elasticCmd, c, ip, request, receive, argIndex, hints, conn);
            }
            return ElasticCommandsForCrud.execInsertGeneric(sync, elasticCmd, c, ip, request, receive, argIndex, hints, conn);
        }
        return ElasticCommandsForCrud.execInsertPath(sync, elasticCmd, c, request, receive, argIndex, hints, conn);
    }

    private static Future<?> execUpdate(Future<Object> sync, ElasticCmd elasticCmd, UpdateContext c, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        UpdatePathContext up = c.updatePath();
        if (up != null) {
            if (up.UPDATE_BY_QUERY_KW() != null) {
                return ElasticCommandsForCrud.execUpdateByQuery(sync, elasticCmd, c, up, request, receive, argIndex, hints, conn);
            }
            if (up.UPDATE_KW() != null) {
                return ElasticCommandsForCrud.execUpdateDoc(sync, elasticCmd, c, up, request, receive, argIndex, hints, conn);
            }
        }
        return ElasticCommandsForCrud.execUpdateGeneric(sync, elasticCmd, c, request, receive, argIndex, hints, conn);
    }

    private static Future<?> execDelete(Future<Object> sync, ElasticCmd elasticCmd, DeleteContext c, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        DeletePathContext dp = c.deletePath();
        if (dp != null && dp.DELETE_BY_QUERY_KW() != null) {
            return ElasticCommandsForCrud.execDeleteByQuery(sync, elasticCmd, c, dp, request, receive, argIndex, hints, conn);
        }
        return ElasticCommandsForCrud.execDeleteDoc(sync, elasticCmd, c, request, receive, argIndex, hints, conn);
    }

    private static Map<String, Object> readHints(AtomicInteger argIndex, AdapterRequest request, List<HintContext> hint) throws SQLException {
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
                } else if (hv.ARG1() != null || hv.ARG2() != null) {
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

}
