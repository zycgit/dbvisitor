package net.hasor.dbvisitor.adapter.elastic;
import java.io.InputStream;
import java.util.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.*;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

class ElasticCommandsForQuery extends ElasticCommands {
    public static Future<?> execMultiSearch(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive, ElasticConn conn) throws Exception {
        Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();

        Map<String, Object> hints = o.getHints();
        if (hints.containsKey("overwrite_find_limit") || hints.containsKey("overwrite_find_skip")) {
            if (jsonBody instanceof List) {
                List<Object> listBody = (List<Object>) jsonBody;
                for (int i = 1; i < listBody.size(); i += 2) {
                    Object item = listBody.get(i);
                    if (item instanceof Map) {
                        Map<String, Object> mapBody = (Map<String, Object>) item;
                        if (hints.containsKey("overwrite_find_limit")) {
                            mapBody.put("size", Long.parseLong(hints.get("overwrite_find_limit").toString()));
                        }
                        if (hints.containsKey("overwrite_find_skip")) {
                            mapBody.put("from", Long.parseLong(hints.get("overwrite_find_skip").toString()));
                        }
                    }
                }
            }
        }

        StringBuilder ndjson = new StringBuilder();
        if (jsonBody instanceof List) {
            for (Object obj : (List<?>) jsonBody) {
                ndjson.append(jsonMapper.writeValueAsString(obj)).append("\n");
            }
        } else if (jsonBody != null) {
            ndjson.append(jsonMapper.writeValueAsString(jsonBody)).append("\n");
        }
        esRequest.setJsonEntity(ndjson.toString());

        Response response = cmd.getClient().performRequest(esRequest);

        if (((ElasticRequest) o.getRequest()).isPreRead()) {
            return execMultiSearchWithPreRead(sync, o.getRequest(), receive, response, jsonMapper, conn);
        } else {
            return execMultiSearchWithDirect(sync, o.getRequest(), receive, response, jsonMapper);
        }
    }

    private static Future<?> execMultiSearchWithPreRead(Future<Object> sync, AdapterRequest request, AdapterReceive receive, Response response, ObjectMapper jsonMapper, ElasticConn conn) throws Exception {
        try (InputStream inputStream = response.getEntity().getContent()) {
            JsonParser parser = jsonMapper.getFactory().createParser(inputStream);

            if (navigateToResponses(parser)) {
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    try (ElasticResultBuffer buffer = new ElasticResultBuffer(conn.getPreReadThreshold(), conn.getPreReadMaxFileSize(), conn.getPreReadCacheDir())) {
                        if (navigateToHits(parser)) {
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                JsonNode hitNode = jsonMapper.readTree(parser);
                                Map<String, Object> row = parseHit(hitNode);
                                buffer.add(row);
                            }
                            // Exit hits object
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                parser.skipChildren();
                            }
                            // Exit response object
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                parser.skipChildren();
                            }
                        }

                        buffer.finish();

                        Set<String> keySet = new LinkedHashSet<>();
                        keySet.add(COL_ID_STRING.name);
                        keySet.add(COL_DOC_JSON.name);
                        for (Map<String, Object> row : buffer) {
                            keySet.addAll(row.keySet());
                        }

                        List<JdbcColumn> columns = new ArrayList<>();
                        for (String key : keySet) {
                            columns.add(new JdbcColumn(key, AdapterType.String, "", "", ""));
                        }

                        AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
                        for (Map<String, Object> row : buffer) {
                            cursor.pushData(row);
                        }
                        cursor.pushFinish();
                        receive.responseResult(request, cursor);
                    }
                }
            }
        }
        return completed(sync);
    }

    private static Future<?> execMultiSearchWithDirect(Future<Object> sync, AdapterRequest request, AdapterReceive receive, Response response, ObjectMapper jsonMapper) throws Exception {
        try (InputStream inputStream = response.getEntity().getContent()) {
            JsonParser parser = jsonMapper.getFactory().createParser(inputStream);

            if (navigateToResponses(parser)) {
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    if (navigateToHits(parser)) {
                        if (parser.nextToken() != JsonToken.END_ARRAY) {
                            JsonNode firstHit = jsonMapper.readTree(parser);
                            Map<String, Object> firstRow = parseHit(firstHit);

                            List<JdbcColumn> columns = Arrays.asList(COL_ID_STRING, COL_DOC_JSON);
                            AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
                            cursor.pushData(firstRow);

                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                JsonNode hitNode = jsonMapper.readTree(parser);
                                Map<String, Object> row = parseHit(hitNode);
                                cursor.pushData(row);
                            }
                            cursor.pushFinish();
                            receive.responseResult(request, cursor);

                            // Exit hits object
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                parser.skipChildren();
                            }
                            // Exit response object
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                parser.skipChildren();
                            }
                        } else {
                            List<JdbcColumn> columns = Arrays.asList(COL_ID_STRING, COL_DOC_JSON);
                            AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
                            cursor.pushFinish();
                            receive.responseResult(request, cursor);

                            // Exit hits object
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                parser.skipChildren();
                            }
                            // Exit response object
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                parser.skipChildren();
                            }
                        }
                    } else {
                        List<JdbcColumn> columns = Arrays.asList(COL_ID_STRING, COL_DOC_JSON);
                        AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
                        cursor.pushFinish();
                        receive.responseResult(request, cursor);
                    }
                }
            }
        }
        return completed(sync);
    }

    private static boolean navigateToResponses(JsonParser parser) throws java.io.IOException {
        if (parser.currentToken() == null) {
            parser.nextToken();
        }
        if (parser.currentToken() != JsonToken.START_OBJECT) {
            return false;
        }

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            parser.nextToken(); // move to value

            if ("responses".equals(fieldName) && parser.currentToken() == JsonToken.START_ARRAY) {
                return true;
            } else {
                parser.skipChildren();
            }
        }
        return false;
    }

    private static boolean navigateToDocs(JsonParser parser) throws java.io.IOException {
        if (parser.currentToken() == null) {
            parser.nextToken();
        }
        if (parser.currentToken() != JsonToken.START_OBJECT) {
            return false;
        }

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            parser.nextToken(); // move to value

            if ("docs".equals(fieldName) && parser.currentToken() == JsonToken.START_ARRAY) {
                return true;
            } else {
                parser.skipChildren();
            }
        }
        return false;
    }

    //

    public static Future<?> execSearch(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive, ElasticConn conn) throws Exception {
        Map<String, Object> hints = o.getHints();
        if (hints != null && !hints.isEmpty() && jsonBody instanceof Map) {
            Map<String, Object> mapBody = (Map<String, Object>) jsonBody;
            if (hints.containsKey("overwrite_find_limit")) {
                mapBody.put("size", Long.parseLong(hints.get("overwrite_find_limit").toString()));
            }
            if (hints.containsKey("overwrite_find_skip")) {
                mapBody.put("from", Long.parseLong(hints.get("overwrite_find_skip").toString()));
            }
        }

        Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        if (((ElasticRequest) o.getRequest()).isPreRead()) {
            try (ElasticResultBuffer buffer = new ElasticResultBuffer(conn.getPreReadThreshold(), conn.getPreReadMaxFileSize(), conn.getPreReadCacheDir())) {
                return execSearchWithPreRead(sync, o.getRequest(), receive, buffer, response, jsonMapper);
            }
        } else {
            return execSearchWithDirect(sync, o.getRequest(), receive, response, jsonMapper);
        }
    }

    private static Future<?> execSearchWithPreRead(Future<Object> sync, AdapterRequest request, AdapterReceive receive, ElasticResultBuffer buffer, Response response, ObjectMapper jsonMapper) throws Exception {
        try (InputStream inputStream = response.getEntity().getContent()) {
            JsonParser parser = jsonMapper.getFactory().createParser(inputStream);

            // Navigate to hits.hits
            if (navigateToHits(parser)) {
                long maxRows = request.getMaxRows();
                int affectRows = 0;

                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    JsonNode hitNode = jsonMapper.readTree(parser);
                    Map<String, Object> row = parseHit(hitNode);
                    buffer.add(row);

                    affectRows++;
                    if (maxRows > 0 && affectRows >= maxRows) {
                        break;
                    }
                }
            }
            buffer.finish();

            Set<String> keySet = new LinkedHashSet<>();
            keySet.add(COL_ID_STRING.name);
            keySet.add(COL_DOC_JSON.name);
            for (Map<String, Object> row : buffer) {
                keySet.addAll(row.keySet());
            }

            List<JdbcColumn> columns = new ArrayList<>();
            for (String key : keySet) {
                columns.add(new JdbcColumn(key, AdapterType.String, "", "", ""));
            }
            AdapterResultCursor cursor = new AdapterResultCursor(request, columns);

            for (Map<String, Object> row : buffer) {
                cursor.pushData(row);
            }
            cursor.pushFinish();
            receive.responseResult(request, cursor);
        }
        return completed(sync);
    }

    private static Future<?> execSearchWithDirect(Future<Object> sync, AdapterRequest request, AdapterReceive receive, Response response, ObjectMapper jsonMapper) throws Exception {
        try (InputStream inputStream = response.getEntity().getContent()) {
            JsonParser parser = jsonMapper.getFactory().createParser(inputStream);

            // Navigate to hits.hits
            if (navigateToHits(parser)) {
                if (parser.nextToken() != JsonToken.END_ARRAY) {
                    JsonNode firstHit = jsonMapper.readTree(parser);
                    Map<String, Object> firstRow = parseHit(firstHit);

                    List<JdbcColumn> columns = Arrays.asList(COL_ID_STRING, COL_DOC_JSON);
                    AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
                    cursor.pushData(firstRow);

                    long maxRows = request.getMaxRows();
                    int affectRows = 1;

                    if (maxRows <= 0 || affectRows < maxRows) {
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            JsonNode hitNode = jsonMapper.readTree(parser);
                            Map<String, Object> row = parseHit(hitNode);
                            cursor.pushData(row);

                            affectRows++;
                            if (maxRows > 0 && affectRows >= maxRows) {
                                break;
                            }
                        }
                    }
                    cursor.pushFinish();
                    receive.responseResult(request, cursor);
                } else {
                    List<JdbcColumn> columns = Arrays.asList(COL_ID_STRING, COL_DOC_JSON);
                    AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
                    cursor.pushFinish();
                    receive.responseResult(request, cursor);
                }
            } else {
                List<JdbcColumn> columns = Arrays.asList(COL_ID_STRING, COL_DOC_JSON);
                AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
                cursor.pushFinish();
                receive.responseResult(request, cursor);
            }
        }
        return completed(sync);
    }

    private static boolean navigateToHits(JsonParser parser) throws java.io.IOException {
        if (parser.currentToken() == null) {
            parser.nextToken();
        }
        if (parser.currentToken() != JsonToken.START_OBJECT) {
            return false;
        }

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            parser.nextToken(); // move to value

            if ("hits".equals(fieldName) && parser.currentToken() == JsonToken.START_OBJECT) {
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String innerFieldName = parser.getCurrentName();
                    parser.nextToken(); // move to value

                    if ("hits".equals(innerFieldName) && parser.currentToken() == JsonToken.START_ARRAY) {
                        return true;
                    } else if (parser.currentToken() == JsonToken.START_OBJECT || parser.currentToken() == JsonToken.START_ARRAY) {
                        parser.skipChildren();
                    }
                }
                return false; // Found "hits" object but no "hits" array inside
            } else if (parser.currentToken() == JsonToken.START_OBJECT || parser.currentToken() == JsonToken.START_ARRAY) {
                parser.skipChildren();
            }
        }
        return false;
    }

    private static Map<String, Object> parseHit(JsonNode hit) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(COL_ID_STRING.name, hit.path("_id").asText());
        if (hit.has("_source")) {
            JsonNode source = hit.path("_source");
            row.put(COL_DOC_JSON.name, source.toString());

            Iterator<Map.Entry<String, JsonNode>> fields = source.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode value = field.getValue();
                if (value.isValueNode()) {
                    row.put(field.getKey(), value.asText());
                } else {
                    row.put(field.getKey(), value.toString());
                }
            }
        }
        return row;
    }

    //

    public static Future<?> execGetSource(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive, ElasticConn conn) throws Exception {
        Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        if (((ElasticRequest) o.getRequest()).isPreRead()) {
            return execGetSourceWithPreRead(sync, o.getRequest(), receive, response, jsonMapper, conn);
        } else {
            return execGetSourceWithDirect(sync, o.getRequest(), receive, response, jsonMapper);
        }
    }

    private static Future<?> execGetSourceWithPreRead(Future<Object> sync, AdapterRequest request, AdapterReceive receive, Response response, ObjectMapper jsonMapper, ElasticConn conn) throws Exception {
        try (InputStream inputStream = response.getEntity().getContent()) {
            Map<String, Object> responseMap = jsonMapper.readValue(inputStream, Map.class);
            try (ElasticResultBuffer buffer = new ElasticResultBuffer(conn.getPreReadThreshold(), conn.getPreReadMaxFileSize(), conn.getPreReadCacheDir())) {
                Map<String, Object> row = parseSourceMap(responseMap, jsonMapper);
                buffer.add(row);
                buffer.finish();

                Set<String> keySet = new LinkedHashSet<>();
                keySet.add(COL_ID_STRING.name);
                keySet.add(COL_DOC_JSON.name);
                for (Map<String, Object> r : buffer) {
                    keySet.addAll(r.keySet());
                }

                List<JdbcColumn> columns = new ArrayList<>();
                for (String key : keySet) {
                    columns.add(new JdbcColumn(key, AdapterType.String, "", "", ""));
                }

                AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
                for (Map<String, Object> r : buffer) {
                    cursor.pushData(r);
                }
                cursor.pushFinish();
                receive.responseResult(request, cursor);
            }
        }
        return completed(sync);
    }

    private static Future<?> execGetSourceWithDirect(Future<Object> sync, AdapterRequest request, AdapterReceive receive, Response response, ObjectMapper jsonMapper) throws Exception {
        try (InputStream inputStream = response.getEntity().getContent()) {
            Map<String, Object> responseMap = jsonMapper.readValue(inputStream, Map.class);
            Map<String, Object> row = parseSourceMap(responseMap, jsonMapper);

            List<JdbcColumn> columns = Arrays.asList(COL_ID_STRING, COL_DOC_JSON);

            AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
            cursor.pushData(row);
            cursor.pushFinish();
            receive.responseResult(request, cursor);
        }
        return completed(sync);
    }

    private static Map<String, Object> parseSourceMap(Map<String, Object> sourceMap, ObjectMapper jsonMapper) throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(COL_ID_STRING.name, null);
        row.put(COL_DOC_JSON.name, jsonMapper.writeValueAsString(sourceMap));

        for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof Map || val instanceof List) {
                row.put(entry.getKey(), jsonMapper.writeValueAsString(val));
            } else {
                row.put(entry.getKey(), val != null ? val.toString() : null);
            }
        }
        return row;
    }

    //

    public static Future<?> execMGet(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive, ElasticConn conn) throws Exception {
        Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        if (((ElasticRequest) o.getRequest()).isPreRead()) {
            return execMGetWithPreRead(sync, o.getRequest(), receive, response, jsonMapper, conn);
        } else {
            return execMGetWithDirect(sync, o.getRequest(), receive, response, jsonMapper);
        }
    }

    private static Future<?> execMGetWithPreRead(Future<Object> sync, AdapterRequest request, AdapterReceive receive, Response response, ObjectMapper jsonMapper, ElasticConn conn) throws Exception {
        try (InputStream inputStream = response.getEntity().getContent()) {
            JsonParser parser = jsonMapper.getFactory().createParser(inputStream);

            if (navigateToDocs(parser)) {
                try (ElasticResultBuffer buffer = new ElasticResultBuffer(conn.getPreReadThreshold(), conn.getPreReadMaxFileSize(), conn.getPreReadCacheDir())) {
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        JsonNode hitNode = jsonMapper.readTree(parser);
                        Map<String, Object> row = parseHit(hitNode);
                        buffer.add(row);
                    }
                    buffer.finish();

                    Set<String> keySet = new LinkedHashSet<>();
                    keySet.add(COL_ID_STRING.name);
                    keySet.add(COL_DOC_JSON.name);
                    for (Map<String, Object> row : buffer) {
                        keySet.addAll(row.keySet());
                    }

                    List<JdbcColumn> columns = new ArrayList<>();
                    for (String key : keySet) {
                        columns.add(new JdbcColumn(key, AdapterType.String, "", "", ""));
                    }

                    AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
                    for (Map<String, Object> row : buffer) {
                        cursor.pushData(row);
                    }
                    cursor.pushFinish();
                    receive.responseResult(request, cursor);
                }
            }
        }
        return completed(sync);
    }

    private static Future<?> execMGetWithDirect(Future<Object> sync, AdapterRequest request, AdapterReceive receive, Response response, ObjectMapper jsonMapper) throws Exception {
        try (InputStream inputStream = response.getEntity().getContent()) {
            JsonParser parser = jsonMapper.getFactory().createParser(inputStream);

            if (navigateToDocs(parser)) {
                if (parser.nextToken() != JsonToken.END_ARRAY) {
                    JsonNode firstHit = jsonMapper.readTree(parser);
                    Map<String, Object> firstRow = parseHit(firstHit);

                    List<JdbcColumn> columns = Arrays.asList(COL_ID_STRING, COL_DOC_JSON);
                    AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
                    cursor.pushData(firstRow);

                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        JsonNode hitNode = jsonMapper.readTree(parser);
                        Map<String, Object> row = parseHit(hitNode);
                        cursor.pushData(row);
                    }
                    cursor.pushFinish();
                    receive.responseResult(request, cursor);
                } else {
                    List<JdbcColumn> columns = Arrays.asList(COL_ID_STRING, COL_DOC_JSON);
                    AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
                    cursor.pushFinish();
                    receive.responseResult(request, cursor);
                }
            } else {
                List<JdbcColumn> columns = Arrays.asList(COL_ID_STRING, COL_DOC_JSON);
                AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
                cursor.pushFinish();
                receive.responseResult(request, cursor);
            }
        }
        return completed(sync);
    }

    //

    public static Future<?> execCount(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        Map<String, Object> hints = o.getHints();
        if (hints.containsKey("overwrite_find_limit") || hints.containsKey("overwrite_find_skip")) {
            if (jsonBody == null) {
                jsonBody = new LinkedHashMap<>();
            }
            if (jsonBody instanceof Map) {
                Map<String, Object> mapBody = (Map<String, Object>) jsonBody;
                if (hints.containsKey("overwrite_find_limit")) {
                    mapBody.put("size", Long.parseLong(hints.get("overwrite_find_limit").toString()));
                }
                if (hints.containsKey("overwrite_find_skip")) {
                    mapBody.put("from", Long.parseLong(hints.get("overwrite_find_skip").toString()));
                }
            }
        }

        try (InputStream inputStream = response.getEntity().getContent()) {
            JsonNode root = jsonMapper.readTree(inputStream);
            long count = root.path("count").asLong();

            AdapterResultCursor cursor = new AdapterResultCursor(o.getRequest(), Collections.singletonList(COL_COUNT_LONG));
            cursor.pushData(Collections.singletonMap(COL_COUNT_LONG.name, count));
            cursor.pushFinish();

            receive.responseResult(o.getRequest(), cursor);
        }
        return completed(sync);
    }

    public static Future<?> execHeader(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
        Response response = cmd.getClient().performRequest(esRequest);

        List<JdbcColumn> columns = Collections.singletonList(COL_STATUS_INT);
        AdapterResultCursor cursor = new AdapterResultCursor(o.getRequest(), columns);
        Map<String, Object> row = new HashMap<>();
        row.put(COL_STATUS_INT.name, response.getStatusLine().getStatusCode());
        cursor.pushData(row);
        cursor.pushFinish();
        receive.responseResult(o.getRequest(), cursor);
        return completed(sync);
    }

    public static Future<?> execExplain(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        try (InputStream inputStream = response.getEntity().getContent()) {
            Map<String, Object> responseMap = jsonMapper.readValue(inputStream, Map.class);

            List<JdbcColumn> columns = new ArrayList<>();
            for (String key : responseMap.keySet()) {
                columns.add(new JdbcColumn(key, AdapterType.String, "", "", ""));
            }

            AdapterResultCursor cursor = new AdapterResultCursor(o.getRequest(), columns);
            Map<String, Object> row = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : responseMap.entrySet()) {
                Object val = entry.getValue();
                row.put(entry.getKey(), val != null ? val.toString() : null);
            }
            cursor.pushData(row);
            cursor.pushFinish();
            receive.responseResult(o.getRequest(), cursor);
        }
        return completed(sync);
    }
}
