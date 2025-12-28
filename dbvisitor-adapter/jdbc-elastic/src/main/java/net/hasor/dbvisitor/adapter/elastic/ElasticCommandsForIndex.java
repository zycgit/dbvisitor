package net.hasor.dbvisitor.adapter.elastic;
import java.io.InputStream;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.JdbcColumn;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

class ElasticCommandsForIndex extends ElasticCommands {
    // POST or GET /_aliases
    public static Future<?> execAliases(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
        if (jsonBody != null) {
            ObjectMapper mapper = new ObjectMapper();
            esRequest.setJsonEntity(mapper.writeValueAsString(jsonBody));
        }

        if (o.getMethod() == ElasticHttpMethod.GET) {
            Response response = cmd.getClient().performRequest(esRequest);
            try (InputStream content = response.getEntity().getContent()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(content);

                List<Map<String, Object>> result = new ArrayList<>();
                Iterator<String> fieldNames = root.fieldNames();
                while (fieldNames.hasNext()) {
                    String indexName = fieldNames.next();

                    // Add Index
                    result.add(CollectionUtils.asMap(           //
                            COL_NAME_STRING.name, indexName,    //
                            COL_SOURCE_STRING.name, null,  //
                            COL_ALIASES_BOOLEAN.name, false));

                    // aliases
                    JsonNode indexNode = root.get(indexName);
                    if (indexNode.has("aliases")) {
                        Iterator<String> aliasNames = indexNode.get("aliases").fieldNames();
                        while (aliasNames.hasNext()) {
                            String aliasName = aliasNames.next();
                            result.add(CollectionUtils.asMap(           //
                                    COL_NAME_STRING.name, aliasName,    //
                                    COL_SOURCE_STRING.name, indexName,  //
                                    COL_ALIASES_BOOLEAN.name, true));
                        }
                    }
                }

                List<JdbcColumn> columns = Arrays.asList(COL_NAME_STRING, COL_SOURCE_STRING, COL_ALIASES_BOOLEAN);
                receive.responseResult(o.getRequest(), listResult(o.getRequest(), columns, result));
            }
        } else {
            cmd.getClient().performRequest(esRequest);
            receive.responseUpdateCount(o.getRequest(), 1);
        }
        return completed(sync);
    }

    public static Future<?> execIndexOpen(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(ElasticHttpMethod.POST.name(), operation.getEndpoint());
        elasticCmd.getClient().performRequest(esRequest);
        receive.responseUpdateCount(operation.getRequest(), 1);
        return completed(sync);
    }

    public static Future<?> execIndexClose(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(ElasticHttpMethod.POST.name(), operation.getEndpoint());
        elasticCmd.getClient().performRequest(esRequest);
        receive.responseUpdateCount(operation.getRequest(), 1);
        return completed(sync);
    }

    public static Future<?> execIndexMapping(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(operation.getMethod().name(), operation.getEndpoint());
        if (jsonBody != null) {
            ObjectMapper mapper = new ObjectMapper();
            esRequest.setJsonEntity(mapper.writeValueAsString(jsonBody));
        }

        if (operation.getMethod() == ElasticHttpMethod.GET) {
            Response response = elasticCmd.getClient().performRequest(esRequest);
            try (InputStream content = response.getEntity().getContent()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(content);

                List<Map<String, Object>> result = new ArrayList<>();
                Iterator<String> indexNames = root.fieldNames();
                while (indexNames.hasNext()) {
                    String indexName = indexNames.next();
                    JsonNode indexNode = root.get(indexName);
                    JsonNode mappingsNode = indexNode.get("mappings");

                    if (mappingsNode == null) {
                        continue;
                    }

                    // Handle ES 6.x structure where type is present, or ES 7.x where properties is direct
                    JsonNode propertiesNode = mappingsNode.get("properties");
                    if (propertiesNode == null) {
                        // Try to find properties under a type
                        Iterator<JsonNode> typeNodes = mappingsNode.elements();
                        while (typeNodes.hasNext()) {
                            JsonNode typeNode = typeNodes.next();
                            if (typeNode.has("properties")) {
                                propertiesNode = typeNode.get("properties");
                                break;
                            }
                        }
                    }

                    if (propertiesNode != null) {
                        Iterator<String> propNames = propertiesNode.fieldNames();
                        while (propNames.hasNext()) {
                            String propName = propNames.next();
                            JsonNode propNode = propertiesNode.get(propName);

                            // Add main property
                            String type = propNode.has("type") ? propNode.get("type").asText() : null;
                            boolean isNested = propNode.has("properties") || "nested".equals(type) || "object".equals(type);

                            result.add(CollectionUtils.asMap(         //
                                    COL_NAME_STRING.name, indexName,  //
                                    COL_MAPPING_STRING.name, propName,//
                                    COL_FIELD_STRING.name, null, //
                                    COL_TYPE_STRING.name, type,       //
                                    COL_NESTED_BOOLEAN.name, isNested,//
                                    COL_OPTION_JSON.name, propNode.toString()));

                            // Add multi-fields if any
                            if (propNode.has("fields")) {
                                JsonNode subProps = propNode.get("fields");
                                Iterator<String> subPropNames = subProps.fieldNames();
                                while (subPropNames.hasNext()) {
                                    String subPropName = subPropNames.next();
                                    JsonNode subPropNode = subProps.get(subPropName);
                                    String subType = subPropNode.has("type") ? subPropNode.get("type").asText() : null;
                                    boolean isSubNested = subPropNode.has("properties") || "nested".equals(subType) || "object".equals(subType);

                                    result.add(CollectionUtils.asMap(            //
                                            COL_NAME_STRING.name, indexName,     //
                                            COL_MAPPING_STRING.name, propName,   //
                                            COL_FIELD_STRING.name, subPropName,  //
                                            COL_TYPE_STRING.name, subType,       //
                                            COL_NESTED_BOOLEAN.name, isSubNested,//
                                            COL_OPTION_JSON.name, subPropNode.toString()));
                                }
                            }
                        }
                    }
                }

                List<JdbcColumn> columns = Arrays.asList(COL_NAME_STRING, COL_MAPPING_STRING, COL_FIELD_STRING, COL_TYPE_STRING, COL_NESTED_BOOLEAN, COL_OPTION_JSON);
                receive.responseResult(operation.getRequest(), listResult(operation.getRequest(), columns, result));
            }
        } else {
            elasticCmd.getClient().performRequest(esRequest);
            receive.responseUpdateCount(operation.getRequest(), 1);
        }
        return completed(sync);
    }

    public static Future<?> execIndexSettings(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(operation.getMethod().name(), operation.getEndpoint());
        if (jsonBody != null) {
            ObjectMapper mapper = new ObjectMapper();
            esRequest.setJsonEntity(mapper.writeValueAsString(jsonBody));
        }

        if (operation.getMethod() == ElasticHttpMethod.GET) {
            Response response = elasticCmd.getClient().performRequest(esRequest);
            try (InputStream content = response.getEntity().getContent()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(content);
                receive.responseResult(operation.getRequest(), singleResult(operation.getRequest(), COL_JSON_STRING, root.toString()));
            }
        } else {
            elasticCmd.getClient().performRequest(esRequest);
            receive.responseUpdateCount(operation.getRequest(), 1);
        }
        return completed(sync);
    }
}
