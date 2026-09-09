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
package net.hasor.dbvisitor.adapter.milvus.commands.schema;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.common.IndexParam.IndexType;
import io.milvus.v2.common.IndexParam.MetricType;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.index.request.DescribeIndexReq;
import io.milvus.v2.service.index.request.DropIndexReq;
import io.milvus.v2.service.index.response.DescribeIndexResp;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.CreateCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.DropCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HintCommandContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.ShowCmdContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;

public final class MilvusCommandsForIndex extends MilvusCommands {
    private MilvusCommandsForIndex() {
    }

    // Preserve the ten-minute synchronous index wait used by the previous SDK API.
    private static final long       INDEX_WAIT_TIMEOUT_MS = 600_000L;
    private static final JdbcColumn COL_PARAMS_STRING     = new JdbcColumn("PARAMS", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_TOTAL_LONG        = new JdbcColumn("TOTAL", AdapterType.Long, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_INDEXED_LONG      = new JdbcColumn("INDEXED", AdapterType.Long, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);

    // Index creation and lifecycle

    public static Future<?> execCreateIndex(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);
        String fieldName = readName(c.fieldName);
        String indexName = c.indexName != null ? readName(c.indexName) : null;

        Map<String, Object> props = new LinkedCaseInsensitiveMap<>();
        if (c.withOptionList() != null) {
            for (MilvusParser.WithOptionContext opt : c.withOptionList().withOption()) {
                String key = getIdentifier(opt.identifier(0).getText());
                Object value = null;

                if (opt.STRING_LITERAL() != null) {
                    value = getIdentifier(opt.STRING_LITERAL().getText());
                } else if (opt.INTEGER() != null) {
                    value = Long.parseLong(opt.INTEGER().getText());
                } else if (opt.FLOAT_LITERAL() != null) {
                    value = Double.parseDouble(opt.FLOAT_LITERAL().getText());
                } else if (opt.TRUE() != null || opt.FALSE() != null) {
                    value = opt.TRUE() != null;
                } else if (opt.identifier().size() > 1) {
                    value = opt.identifier(1).ARG() != null ? getArg(argIndex, request) : getIdentifier(opt.identifier(1).getText());
                }

                if (value != null) {
                    props.put(key, value);
                }
            }
        }

        String metricTypeStr = (String) props.remove(MilvusCommandKeys.METRIC_TYPE);
        if (metricTypeStr == null) {
            metricTypeStr = (String) props.remove(MilvusCommandKeys.METRIC);
        }
        MetricType metricType = null;
        if (metricTypeStr != null) {
            try {
                metricType = MetricType.valueOf(metricTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new SQLException("Unsupported metric type: " + metricTypeStr);
            }
        }

        IndexType indexType = null;
        if (c.algo != null) {
            String algoStr = c.algo.getText();
            if (algoStr.length() >= 2 && algoStr.startsWith("\"") && algoStr.endsWith("\"")) {
                algoStr = algoStr.substring(1, algoStr.length() - 1);
            } else if (algoStr.length() >= 2 && algoStr.startsWith("'") && algoStr.endsWith("'")) {
                algoStr = algoStr.substring(1, algoStr.length() - 1);
            }

            try {
                indexType = IndexType.valueOf(algoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Try parse from string literal if parser passed quoted string
                throw new SQLException("Unsupported index type: " + algoStr);
            }
        }

        IndexParam.IndexParamBuilder builder = IndexParam.builder().fieldName(fieldName).indexName(indexName == null ? "" : indexName);

        if (indexType != null) {
            builder.indexType(indexType);
        }
        if (metricType != null) {
            builder.metricType(metricType);
        }

        Map<String, Object> extraParams = new LinkedHashMap<>(props);
        builder.extraParams(extraParams);
        cmd.createIndex(CreateIndexReq.builder().databaseName(cmd.getCatalog()).collectionName(collectionName).indexParams(Collections.singletonList(builder.build())).sync(true).timeout(INDEX_WAIT_TIMEOUT_MS).build());

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropIndex(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);
        String indexName = readName(c.indexName);

        cmd.dropIndex(DropIndexReq.builder().databaseName(cmd.getCatalog())//
                .collectionName(collectionName)//
                .indexName(indexName)//
                .build());

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    // Index metadata and build progress

    public static Future<?> execShowIndex(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);
        String indexName = c.indexName != null ? readName(c.indexName) : null;

        DescribeIndexResp resp = cmd//
                .describeIndex(DescribeIndexReq.builder().databaseName(cmd.getCatalog())//
                        .collectionName(collectionName)//
                        .indexName(indexName == null ? "" : indexName)//
                        .build());

        List<Map<String, Object>> result = new ArrayList<>();
        DescribeIndexResp data = resp;

        if (data != null) {
            for (DescribeIndexResp.IndexDesc info : data.getIndexDescriptions()) {
                if (StringUtils.isNotBlank(indexName) && !indexName.equals(info.getIndexName())) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put(COL_INDEX_STRING.name, info.getIndexName());
                row.put(COL_FIELD_STRING.name, info.getFieldName());
                row.put(COL_ID_LONG.name, info.getId());

                Map<String, Object> params = new LinkedHashMap<>();
                params.put(MilvusCommandKeys.INDEX_TYPE, info.getIndexType());
                params.put(MilvusCommandKeys.METRIC_TYPE, info.getMetricType());
                params.putAll(info.getExtraParams());
                StringJoiner description = new StringJoiner(", ");
                params.forEach((key, value) -> description.add(key + "=" + value));
                row.put(COL_PARAMS_STRING.name, description.toString());
                result.add(row);
            }
        }

        List<JdbcColumn> columns = Arrays.asList(COL_INDEX_STRING, COL_FIELD_STRING, COL_ID_LONG, COL_PARAMS_STRING);
        receive.responseResult(request, listResult(request, columns, result));
        return completed(future);
    }

    public static Future<?> execShowIndexes(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        return execShowIndex(future, cmd, h, c, request, receive, startArgIdx);
    }

    public static Future<?> execShowProgressIndex(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);
        String indexName = c.indexName != null ? readName(c.indexName) : null;

        DescribeIndexResp response = cmd.describeIndex(DescribeIndexReq.builder().databaseName(cmd.getCatalog()).collectionName(collectionName).indexName(indexName == null ? "" : indexName).build());
        long total = 0;
        long indexed = 0;
        for (DescribeIndexResp.IndexDesc index : response.getIndexDescriptions()) {
            total += index.getTotalRows();
            indexed += index.getIndexedRows();
        }
        receive.responseResult(request, twoResult(request, COL_TOTAL_LONG, total, COL_INDEXED_LONG, indexed));
        return completed(future);
    }
}
