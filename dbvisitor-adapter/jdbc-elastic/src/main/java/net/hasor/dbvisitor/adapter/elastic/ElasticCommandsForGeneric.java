package net.hasor.dbvisitor.adapter.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import java.io.InputStream;
import java.util.*;

class ElasticCommandsForGeneric extends ElasticCommands {
    public static Future<?> execGeneric(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        try (InputStream inputStream = response.getEntity().getContent()) {
            JsonNode rootNode = jsonMapper.readTree(inputStream);
            
            List<Map<String, Object>> dataList = new ArrayList<>();
            Set<String> keys = new LinkedHashSet<>();

            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    if (node.isObject()) {
                        Map<String, Object> map = jsonMapper.convertValue(node, Map.class);
                        dataList.add(map);
                        keys.addAll(map.keySet());
                    } else {
                        Map<String, Object> map = new HashMap<>();
                        map.put("value", node.asText());
                        dataList.add(map);
                        keys.add("value");
                    }
                }
            } else if (rootNode.isObject()) {
                Map<String, Object> map = jsonMapper.convertValue(rootNode, Map.class);
                dataList.add(map);
                keys.addAll(map.keySet());
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("value", rootNode.asText());
                dataList.add(map);
                keys.add("value");
            }

            List<JdbcColumn> columns = new ArrayList<>();
            for (String key : keys) {
                columns.add(new JdbcColumn(key, AdapterType.String, "", "", ""));
            }

            AdapterResultCursor cursor = listResult(o.getRequest(), columns, dataList);
            receive.responseResult(o.getRequest(), cursor);
        }

        return completed(sync);
    }
}
