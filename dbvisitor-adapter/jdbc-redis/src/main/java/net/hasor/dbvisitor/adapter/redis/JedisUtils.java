package net.hasor.dbvisitor.adapter.redis;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.*;
import org.antlr.v4.runtime.tree.TerminalNode;
import redis.clients.jedis.resps.Tuple;

class JedisUtils {
    protected static final JdbcColumn COL_VALUE_BYTES    = new JdbcColumn("VALUE", AdapterType.Bytes, "", "", "");
    protected static final JdbcColumn COL_VALUE_STRING   = new JdbcColumn("VALUE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_VALUE_BOOLEAN  = new JdbcColumn("VALUE", AdapterType.Boolean, "", "", "");
    protected static final JdbcColumn COL_FIELD_STRING   = new JdbcColumn("FIELD", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_VALUE_LIST     = new JdbcColumn("VALUE", AdapterType.Array, "", "", "");
    protected static final JdbcColumn COL_VALUE_LONG     = new JdbcColumn("VALUE", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_VALUE_DOUBLE   = new JdbcColumn("VALUE", AdapterType.Double, "", "", "");
    protected static final JdbcColumn COL_KEY_STRING     = new JdbcColumn("KEY", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_CURSOR_STRING  = new JdbcColumn("CURSOR", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_RANK_LONG      = new JdbcColumn("RANK", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_SCORE_DOUBLE   = new JdbcColumn("SCORE", AdapterType.Double, "", "", "");
    protected static final JdbcColumn COL_ELEMENT_STRING = new JdbcColumn("ELEMENT", AdapterType.String, "", "", "");

    protected static <T> Map<String, T> singletonMap(String column, T keyCol) {
        Map<String, T> dataMap = new LinkedHashMap<>();
        dataMap.put(column, keyCol);
        return dataMap;
    }

    protected static AdapterResultCursor singleValueBytesResult(AdapterRequest request, byte[] value) throws SQLException {
        AdapterResultCursor result = new AdapterResultCursor(request, Collections.singletonList(COL_VALUE_BYTES));
        result.pushData(singletonMap(COL_VALUE_BYTES.name, value));
        result.pushFinish();
        return result;
    }

    protected static AdapterResultCursor singleValueStringResult(AdapterRequest request, String value) throws SQLException {
        AdapterResultCursor result = new AdapterResultCursor(request, Collections.singletonList(COL_VALUE_STRING));
        result.pushData(singletonMap(COL_VALUE_STRING.name, value));
        result.pushFinish();
        return result;
    }

    protected static AdapterResultCursor singleValueLongResult(AdapterRequest request, Long value) throws SQLException {
        AdapterResultCursor result = new AdapterResultCursor(request, Collections.singletonList(COL_VALUE_LONG));
        result.pushData(singletonMap(COL_VALUE_LONG.name, value));
        result.pushFinish();
        return result;
    }

    protected static AdapterResultCursor singleRankLongResult(AdapterRequest request, Long value) throws SQLException {
        AdapterResultCursor result = new AdapterResultCursor(request, Collections.singletonList(COL_RANK_LONG));
        result.pushData(singletonMap(COL_RANK_LONG.name, value));
        result.pushFinish();
        return result;
    }

    protected static AdapterResultCursor singleValueDoubleResult(AdapterRequest request, Double value) throws SQLException {
        AdapterResultCursor result = new AdapterResultCursor(request, Collections.singletonList(COL_VALUE_DOUBLE));
        result.pushData(singletonMap(COL_VALUE_DOUBLE.name, value));
        result.pushFinish();
        return result;
    }

    protected static AdapterResultCursor resultValueStringList(AdapterRequest request, Collection<String> result, long maxRows) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Collections.singletonList(COL_VALUE_STRING));
        int affectRows = 0;
        for (String item : result) {
            receiveCur.pushData(CollectionUtils.asMap(COL_VALUE_STRING.name, item));
            affectRows++;

            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        receiveCur.pushFinish();
        return receiveCur;
    }

    protected static AdapterResultCursor resultKeysStringList(AdapterRequest request, Collection<String> result, long maxRows) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Collections.singletonList(COL_KEY_STRING));
        int affectRows = 0;
        for (String item : result) {
            receiveCur.pushData(CollectionUtils.asMap(COL_KEY_STRING.name, item));
            affectRows++;

            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        receiveCur.pushFinish();
        return receiveCur;
    }

    protected static AdapterResultCursor resultValueLongList(AdapterRequest request, Collection<Long> result, long maxRows) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Collections.singletonList(COL_VALUE_LONG));
        int affectRows = 0;
        for (Long item : result) {
            receiveCur.pushData(CollectionUtils.asMap(COL_VALUE_LONG.name, item));
            affectRows++;

            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        receiveCur.pushFinish();
        return receiveCur;
    }

    protected static AdapterResultCursor resultValueDoubleList(AdapterRequest request, Collection<Double> result, long maxRows) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Collections.singletonList(COL_VALUE_DOUBLE));
        int affectRows = 0;
        for (Double item : result) {
            receiveCur.pushData(CollectionUtils.asMap(COL_VALUE_DOUBLE.name, item));
            affectRows++;

            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        receiveCur.pushFinish();
        return receiveCur;
    }

    protected static AdapterResultCursor resultValueBooleanList(AdapterRequest request, Collection<Boolean> result, long maxRows) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Collections.singletonList(COL_VALUE_BOOLEAN));
        int affectRows = 0;
        for (Boolean item : result) {
            receiveCur.pushData(CollectionUtils.asMap(COL_VALUE_BOOLEAN.name, item));
            affectRows++;

            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        receiveCur.pushFinish();
        return receiveCur;
    }

    protected static AdapterResultCursor resultFiledValueList(AdapterRequest request, AdapterReceive receive, Map<String, String> result, long maxRows) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(COL_FIELD_STRING, COL_VALUE_STRING));
        int affectRows = 0;
        for (Map.Entry<String, String> item : result.entrySet()) {
            receiveCur.pushData(CollectionUtils.asMap(    //
                    COL_FIELD_STRING.name, item.getKey(), //
                    COL_VALUE_STRING.name, item.getValue()//
            ));
            affectRows++;

            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        receiveCur.pushFinish();
        return receiveCur;
    }

    protected static AdapterResultCursor resultScoreAndElement(AdapterRequest request, AdapterReceive receive, List<Tuple> result, long maxRows) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(//
                COL_SCORE_DOUBLE,   //
                COL_ELEMENT_STRING));
        receive.responseResult(request, receiveCur);

        int affectRows = 0;
        for (Tuple tuple : result) {
            receiveCur.pushData(CollectionUtils.asMap(          //
                    COL_SCORE_DOUBLE.name, tuple.getScore(),    //
                    COL_ELEMENT_STRING.name, tuple.getElement() //
            ));
            affectRows++;

            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }

        receiveCur.pushFinish();
        return receiveCur;
    }

    protected static AdapterResultCursor resultCursorAndKeyStringList(AdapterRequest request, String cursor, Collection<String> result, long maxRows) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(COL_CURSOR_STRING, COL_KEY_STRING));
        int affectRows = 0;
        for (String item : result) {
            receiveCur.pushData(CollectionUtils.asMap(COL_CURSOR_STRING.name, cursor, COL_KEY_STRING.name, item));
            affectRows++;

            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        receiveCur.pushFinish();
        return receiveCur;
    }

    protected static AdapterResultCursor resultCursorAndValueStringList(AdapterRequest request, String cursor, Collection<String> result, long maxRows) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(COL_CURSOR_STRING, COL_VALUE_STRING));
        int affectRows = 0;
        for (String item : result) {
            receiveCur.pushData(CollectionUtils.asMap(COL_CURSOR_STRING.name, cursor, COL_VALUE_STRING.name, item));
            affectRows++;

            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        receiveCur.pushFinish();
        return receiveCur;
    }

    protected static AdapterResultCursor resultCursorAndScoreAndElement(AdapterRequest request, AdapterReceive receive, String cursor, List<Tuple> result, long maxRows) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(//
                COL_CURSOR_STRING,   //
                COL_SCORE_DOUBLE,   //
                COL_ELEMENT_STRING));
        receive.responseResult(request, receiveCur);

        int affectRows = 0;
        for (Tuple tuple : result) {
            receiveCur.pushData(CollectionUtils.asMap(          //
                    COL_CURSOR_STRING.name, cursor,             //
                    COL_SCORE_DOUBLE.name, tuple.getScore(),    //
                    COL_ELEMENT_STRING.name, tuple.getElement() //
            ));
            affectRows++;

            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }

        receiveCur.pushFinish();
        return receiveCur;
    }

    private static String getIdentifier(TerminalNode term) {
        String nodeText = term.getText();
        char firstChar = nodeText.charAt(0);
        char endChar = nodeText.charAt(nodeText.length() - 1);

        if (firstChar == '"' && endChar == '"') {
            return nodeText.substring(1, nodeText.length() - 1);
        } else if (firstChar == '\'' && endChar == '\'') {
            return nodeText.substring(1, nodeText.length() - 1);
        } else {
            return nodeText;
        }
    }

    protected static Object getArg(AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        int argIdx = argIndex.getAndIncrement();
        String argName = "arg" + (argIdx + 1);
        JdbcArg jdbcArg = request.getArgMap().get(argName);
        if (jdbcArg == null) {
            throw new SQLException(argName + " not found in request.");
        } else {
            return jdbcArg.getValue();
        }
    }

    protected static Object argOrValue(AtomicInteger argIndex, AdapterRequest request, RedisParser.IdentifierContext ctx) throws SQLException {
        if (ctx.ARG() != null) {
            return getArg(argIndex, request);
        } else if (ctx.IDENTIFIER() != null) {
            return getIdentifier(ctx.IDENTIFIER());
        } else {
            return ctx.getText();
        }
    }

    protected static Object argOrValue(AtomicInteger argIndex, AdapterRequest request, RedisParser.DecimalContext ctx) throws SQLException {
        if (ctx.ARG() != null) {
            return getArg(argIndex, request);
        } else {
            return ctx.getText();
        }
    }

    protected static Object argOrValue(AtomicInteger argIndex, AdapterRequest request, RedisParser.DecimalScoreContext ctx) throws SQLException {
        if (ctx.ARG() != null) {
            return getArg(argIndex, request);
        } else {
            return ctx.getText();
        }
    }

    protected static String argAsString(AtomicInteger argIndex, AdapterRequest request, RedisParser.DecimalScoreContext ctx) throws SQLException {
        if (ctx.ARG() != null) {
            Object arg = getArg(argIndex, request);
            return arg == null ? null : arg.toString();
        } else {
            return ctx.getText();
        }
    }

    protected static String argAsString(AtomicInteger argIndex, AdapterRequest request, RedisParser.IdentifierContext ctx) throws SQLException {
        if (ctx.ARG() != null) {
            Object arg = getArg(argIndex, request);
            return arg == null ? null : arg.toString();
        } else {
            return ctx.getText();
        }
    }

    protected static Object argOrValue(AtomicInteger argIndex, AdapterRequest request, RedisParser.IntegerContext ctx) throws SQLException {
        if (ctx.ARG() != null) {
            return getArg(argIndex, request);
        } else {
            return ctx.getText();
        }
    }
}
