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
package net.hasor.dbvisitor.adapter.milvus;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.grpc.DescribeIndexResponse;
import io.milvus.grpc.GetIndexBuildProgressResponse;
import io.milvus.grpc.IndexDescription;
import io.milvus.grpc.KeyValuePair;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DescribeIndexParam;
import io.milvus.param.index.DropIndexParam;
import io.milvus.param.index.GetIndexBuildProgressParam;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.CreateCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.DropCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HintCommandContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.ShowCmdContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;

class MilvusCommandsForIndex extends MilvusCommands {
    private static final JdbcColumn COL_PARAMS_STRING = new JdbcColumn("PARAMS", AdapterType.String, "", "", "");
    private static final JdbcColumn COL_TOTAL_LONG    = new JdbcColumn("TOTAL", AdapterType.Long, "", "", "");
    private static final JdbcColumn COL_INDEXED_LONG  = new JdbcColumn("INDEXED", AdapterType.Long, "", "", "");

    public static Future<?> execCreateIndex(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = argAsName(argIndex, request, c.collectionName);
        String fieldName = argAsName(argIndex, request, c.fieldName);
        String indexName = c.indexName != null ? argAsName(argIndex, request, c.indexName) : null;

        Map<String, Object> props = new LinkedCaseInsensitiveMap<>();
        if (c.withOptionList() != null) {
            for (MilvusParser.WithOptionContext opt : c.withOptionList().withOption()) {
                String key = getIdentifier(opt.identifier(0).getText());
                Object value = null;

                if (opt.STRING_LITERAL() != null) {
                    value = getIdentifier(opt.STRING_LITERAL().getText());
                } else if (opt.INTEGER() != null) {
                    value = Long.parseLong(opt.INTEGER().getText());
                } else if (opt.identifier().size() > 1) {
                    value = getIdentifier(opt.identifier(1).getText());
                }

                if (value != null) {
                    props.put(key, value);
                }
            }
        }

        String metricTypeStr = (String) props.remove("metric_type");
        if (metricTypeStr == null) {
            metricTypeStr = (String) props.remove("metric");
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

        CreateIndexParam.Builder builder = CreateIndexParam.newBuilder()//
                .withCollectionName(collectionName)                     //
                .withFieldName(fieldName)                               //
                .withIndexName(indexName == null ? "" : indexName)      //
                .withSyncMode(Boolean.TRUE);

        if (indexType != null) {
            builder.withIndexType(indexType);
        }
        if (metricType != null) {
            builder.withMetricType(metricType);
        }

        StringBuilder extraParam = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Object> entry : props.entrySet()) {
            if (i > 0) {
                extraParam.append(",");
            }

            extraParam.append("\"").append(entry.getKey()).append("\":");
            Object v = entry.getValue();
            if (v instanceof Number || v instanceof Boolean) {
                extraParam.append(v);
            } else {
                extraParam.append("\"").append(v).append("\"");
            }
            i++;
        }
        extraParam.append("}");
        if (extraParam.length() > 2) {
            builder.withExtraParam(extraParam.toString());
        }

        R<?> resp = cmd.getClient().createIndex(builder.build());
        if (resp.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(resp.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropIndex(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = argAsName(argIndex, request, c.collectionName);
        String indexName = argAsName(argIndex, request, c.indexName);

        R<?> resp = cmd.getClient().dropIndex(DropIndexParam.newBuilder()//
                .withCollectionName(collectionName)//
                .withIndexName(indexName)//
                .build());

        if (resp.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(resp.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execShowIndex(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = argAsName(argIndex, request, c.collectionName);
        String indexName = c.indexName != null ? argAsName(argIndex, request, c.indexName) : null;

        R<DescribeIndexResponse> resp = cmd.getClient()//
                .describeIndex(DescribeIndexParam.newBuilder()//
                        .withCollectionName(collectionName)//
                        .withIndexName(indexName == null ? "" : indexName)//
                        .build());

        if (resp.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(resp.getMessage());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        DescribeIndexResponse data = resp.getData();

        if (data != null) {
            for (IndexDescription info : data.getIndexDescriptionsList()) {
                if (StringUtils.isNotBlank(indexName) && !indexName.equals(info.getIndexName())) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put(COL_INDEX_STRING.name, info.getIndexName());
                row.put(COL_FIELD_STRING.name, info.getFieldName());
                row.put(COL_ID_LONG.name, info.getIndexID());

                StringBuilder params = new StringBuilder();
                for (KeyValuePair kv : info.getParamsList()) {
                    if (params.length() > 0) {
                        params.append(", ");
                    }
                    params.append(kv.getKey()).append("=").append(kv.getValue());
                }

                row.put(COL_PARAMS_STRING.name, params.toString());
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
        String collectionName = argAsName(argIndex, request, c.collectionName);
        String indexName = c.indexName != null ? argAsName(argIndex, request, c.indexName) : null;

        R<GetIndexBuildProgressResponse> resp = cmd.getClient().getIndexBuildProgress(GetIndexBuildProgressParam.newBuilder().withCollectionName(collectionName).withIndexName(indexName == null ? "" : indexName).build());
        if (resp.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(resp.getMessage());
        }

        GetIndexBuildProgressResponse data = resp.getData();
        receive.responseResult(request, twoResult(request, COL_TOTAL_LONG, data.getTotalRows(), COL_INDEXED_LONG, data.getIndexedRows()));
        return completed(future);
    }
}