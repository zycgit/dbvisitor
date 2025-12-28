package net.hasor.dbvisitor.adapter.elastic;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

class ElasticCommandsForCat extends ElasticCommands {
    // for /_cat/indices
    protected static final JdbcColumn COL_INDEX_STRING                 = new JdbcColumn("INDEX", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_HEALTH_STRING                = new JdbcColumn("HEALTH", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_STATUS_STRING                = new JdbcColumn("STATUS", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_UUID_STRING                  = new JdbcColumn("UUID", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_PRI_STRING                   = new JdbcColumn("PRI", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_PRI_STORE_SIZE_STRING        = new JdbcColumn("PRI.STORE.SIZE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_REP_STRING                   = new JdbcColumn("REP", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_DOCS_COUNT_STRING            = new JdbcColumn("DOCS.COUNT", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_DOCS_DELETED_STRING          = new JdbcColumn("DOCS.DELETED", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_STORE_SIZE_STRING            = new JdbcColumn("STORE.SIZE", AdapterType.String, "", "", "");
    // for /_cat/nodes
    protected static final JdbcColumn COL_IP_STRING                    = new JdbcColumn("IP", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_NAME_STRING                  = new JdbcColumn("NAME", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_MASTER_STRING                = new JdbcColumn("MASTER", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_HEAP_PERCENT_STRING          = new JdbcColumn("HEAP.PERCENT", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_RAM_PERCENT_STRING           = new JdbcColumn("RAM.PERCENT", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_CPU_STRING                   = new JdbcColumn("CPU", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_LOAD_1M_STRING               = new JdbcColumn("LOAD_1M", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_LOAD_5M_STRING               = new JdbcColumn("LOAD_5M", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_LOAD_15M_STRING              = new JdbcColumn("LOAD_15M", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_NODE_ROLE_STRING             = new JdbcColumn("NODE.ROLE", AdapterType.String, "", "", "");
    // for /_cat/health
    protected static final JdbcColumn COL_CLUSTER_STRING               = new JdbcColumn("CLUSTER", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_NODE_TOTAL_STRING            = new JdbcColumn("NODE.TOTAL", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_NODE_DATA_STRING             = new JdbcColumn("NODE.DATA", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_SHARDS_STRING                = new JdbcColumn("SHARDS", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_RELO_STRING                  = new JdbcColumn("RELO", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_INIT_STRING                  = new JdbcColumn("INIT", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_UNASSIGN_STRING              = new JdbcColumn("UNASSIGN", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_PENDING_TASKS_STRING         = new JdbcColumn("PENDING_TASKS", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_MAX_TASK_WAIT_TIME_STRING    = new JdbcColumn("MAX_TASK_WAIT_TIME", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_ACTIVE_SHARDS_PERCENT_STRING = new JdbcColumn("ACTIVE_SHARDS_PERCENT", AdapterType.String, "", "", "");

    public static Future<?> execCatIndices(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, AdapterReceive receive) throws Exception {
        return execRequest(sync, cmd, o, receive, Arrays.asList(//
                COL_INDEX_STRING,                //
                COL_HEALTH_STRING,               //
                COL_STATUS_STRING,               //
                COL_UUID_STRING,                 //
                COL_PRI_STRING,                  //
                COL_PRI_STORE_SIZE_STRING,       //
                COL_REP_STRING,                  //
                COL_DOCS_COUNT_STRING,           //
                COL_DOCS_DELETED_STRING,         //
                COL_STORE_SIZE_STRING            //
        ));
    }

    public static Future<?> execCatNodes(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, AdapterReceive receive) throws Exception {
        return execRequest(sync, cmd, o, receive, Arrays.asList(//
                COL_IP_STRING,                   //
                COL_NAME_STRING,                 //
                COL_MASTER_STRING,               //
                COL_HEAP_PERCENT_STRING,         //
                COL_RAM_PERCENT_STRING,          //
                COL_CPU_STRING,                  //
                COL_LOAD_1M_STRING,              //
                COL_LOAD_5M_STRING,              //
                COL_LOAD_15M_STRING,             //
                COL_NODE_ROLE_STRING             //
        ));
    }

    public static Future<?> execCatHealth(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, AdapterReceive receive) throws Exception {
        return execRequest(sync, cmd, o, receive, Arrays.asList(//
                COL_CLUSTER_STRING,              //
                COL_STATUS_STRING,               //
                COL_NODE_TOTAL_STRING,           //
                COL_NODE_DATA_STRING,            //
                COL_SHARDS_STRING,               //
                COL_PRI_STRING,                  //
                COL_RELO_STRING,                 //
                COL_INIT_STRING,                 //
                COL_UNASSIGN_STRING,             //
                COL_PENDING_TASKS_STRING,        //
                COL_MAX_TASK_WAIT_TIME_STRING,   //
                COL_ACTIVE_SHARDS_PERCENT_STRING //
        ));
    }

    private static Future<?> execRequest(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, AdapterReceive receive, List<JdbcColumn> columns) throws Exception {
        String endpoint = o.getEndpoint();
        Map<String, Object> queryParams = o.getQueryParams();

        if (queryParams.containsKey("format")) {
            Object format = queryParams.get("format");
            if (!"json".equalsIgnoreCase(String.valueOf(format))) {
                throw new SQLException("The '_cat' command only supports 'format=json'.");
            }
        } else {
            if (endpoint.contains("?")) {
                endpoint += "&format=json";
            } else {
                endpoint += "?format=json";
            }
        }

        Request esRequest = new Request(o.getMethod().name(), endpoint);
        Response response = cmd.getClient().performRequest(esRequest);
        try (InputStream content = response.getEntity().getContent()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(content);

            List<Map<String, Object>> result = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    Map<String, Object> row = new HashMap<>();
                    for (JdbcColumn col : columns) {
                        String jsonField = col.name.toLowerCase();
                        if (node.has(jsonField)) {
                            row.put(col.name, node.get(jsonField).asText());
                        } else if (node.has(col.name)) {
                            row.put(col.name, node.get(col.name).asText());
                        } else {
                            row.put(col.name, null);
                        }
                    }
                    result.add(row);
                }
            }

            receive.responseResult(o.getRequest(), listResult(o.getRequest(), columns, result));
        }
        return completed(sync);
    }
}
