package net.hasor.dbvisitor.adapter.milvus.commands.admin;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;
import io.milvus.v2.service.collection.ReplicaInfo;
import io.milvus.v2.service.collection.request.DescribeReplicasReq;
import io.milvus.v2.service.utility.request.GetPersistentSegmentInfoReq;
import io.milvus.v2.service.utility.request.GetQuerySegmentInfoReq;
import io.milvus.v2.service.utility.request.GetServerVersionReq;
import io.milvus.v2.service.utility.response.CheckHealthResp;
import io.milvus.v2.service.utility.response.GetPersistentSegmentInfoResp.PersistentSegmentInfo;
import io.milvus.v2.service.utility.response.GetQuerySegmentInfoResp.QuerySegmentInfo;
import io.milvus.v2.service.utility.response.GetServerVersionResp;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HintCommandContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.ShowCmdContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import static net.hasor.dbvisitor.adapter.milvus.MilvusRequest.checkActive;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readHints;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readName;

/** Read-only server, segment and replica snapshots; no implicit flush, load or entity query. */
public final class MilvusCommandsForDiagnostics extends MilvusCommands {
    private static final Gson             JSON               = new Gson();
    private static final List<JdbcColumn> VERSION_COLUMNS    = Arrays.asList(column("VERSION", AdapterType.String), column("BUILD_TIME", AdapterType.String), column("GIT_COMMIT", AdapterType.String), column("GO_VERSION", AdapterType.String), column("DEPLOY_MODE", AdapterType.String));
    private static final List<JdbcColumn> HEALTH_COLUMNS     = Arrays.asList(column("IS_HEALTHY", AdapterType.Boolean), column("REASONS", AdapterType.String), column("QUOTA_STATES", AdapterType.String));
    private static final List<JdbcColumn> REPLICA_COLUMNS    = Arrays.asList(column("REPLICA_ID", AdapterType.Long), column("COLLECTION_ID", AdapterType.Long), column("PARTITION_IDS", AdapterType.String), column("SHARD_REPLICAS", AdapterType.String), column("NODE_IDS", AdapterType.String), column("RESOURCE_GROUP", AdapterType.String), column("NUM_OUTBOUND_NODE", AdapterType.String));
    private static final List<JdbcColumn> PERSISTENT_COLUMNS = Arrays.asList(column("SEGMENT_ID", AdapterType.Long), column("COLLECTION_ID", AdapterType.Long), column("PARTITION_ID", AdapterType.Long), column("COLLECTION_NAME", AdapterType.String), column("NUM_ROWS", AdapterType.Long), column("STATE", AdapterType.String), column("LEVEL", AdapterType.String), column("STORAGE_VERSION", AdapterType.Long), column("IS_SORTED", AdapterType.Boolean));
    private static final List<JdbcColumn> QUERY_COLUMNS      = Arrays.asList(column("SEGMENT_ID", AdapterType.Long), column("COLLECTION_ID", AdapterType.Long), column("PARTITION_ID", AdapterType.Long), column("MEM_SIZE", AdapterType.Long), column("NUM_ROWS", AdapterType.Long), column("INDEX_NAME", AdapterType.String), column("INDEX_ID", AdapterType.Long), column("STATE", AdapterType.String), column("LEVEL", AdapterType.String), column("NODE_IDS", AdapterType.String), column("STORAGE_VERSION", AdapterType.Long), column("IS_SORTED", AdapterType.Boolean));

    private MilvusCommandsForDiagnostics() {
    }

    public static Future<?> execShow(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        checkActive(request);
        if (c.VERSION() != null) {
            GetServerVersionResp response = cmd.getServerVersionV2(GetServerVersionReq.builder().build());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("VERSION", response.getVersion());
            row.put("BUILD_TIME", response.getBuildTime());
            row.put("GIT_COMMIT", response.getGitCommit());
            row.put("GO_VERSION", response.getGoVersion());
            row.put("DEPLOY_MODE", response.getDeployMode());
            receive.responseResult(request, listResult(request, VERSION_COLUMNS, Collections.singletonList(row)));
        } else if (c.HEALTH() != null) {
            CheckHealthResp response = cmd.checkHealth();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("IS_HEALTHY", response.getIsHealthy());
            row.put("REASONS", jsonList(response.getReasons()));
            row.put("QUOTA_STATES", jsonList(response.getQuotaStates()));
            receive.responseResult(request, listResult(request, HEALTH_COLUMNS, Collections.singletonList(row)));
        } else if (c.REPLICAS() != null) {
            showReplicas(cmd, c, request, receive);
        } else {
            showSegments(cmd, c, request, receive);
        }
        return completed(future);
    }

    private static void showSegments(MilvusCmd cmd, ShowCmdContext c, AdapterRequest request, AdapterReceive receive) throws SQLException {
        String collection = readName(c.collectionName);
        List<Map<String, Object>> rows = new ArrayList<>();
        if (c.PERSISTENT() != null) {
            List<PersistentSegmentInfo> segments = cmd.getPersistentSegmentInfo(GetPersistentSegmentInfoReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).build()).getSegmentInfos();
            for (PersistentSegmentInfo segment : segments) {
                checkActive(request);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("SEGMENT_ID", segment.getSegmentID());
                row.put("COLLECTION_ID", segment.getCollectionID());
                row.put("PARTITION_ID", segment.getPartitionID());
                row.put("COLLECTION_NAME", segment.getCollectionName());
                row.put("NUM_ROWS", segment.getNumOfRows());
                row.put("STATE", segment.getState());
                row.put("LEVEL", segment.getLevel());
                row.put("STORAGE_VERSION", segment.getStorageVersion());
                row.put("IS_SORTED", segment.getIsSorted());
                rows.add(row);
            }
            receive.responseResult(request, listResult(request, PERSISTENT_COLUMNS, rows));
        } else {
            List<QuerySegmentInfo> segments = cmd.getQuerySegmentInfo(GetQuerySegmentInfoReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).build()).getSegmentInfos();
            for (QuerySegmentInfo segment : segments) {
                checkActive(request);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("SEGMENT_ID", segment.getSegmentID());
                row.put("COLLECTION_ID", segment.getCollectionID());
                row.put("PARTITION_ID", segment.getPartitionID());
                row.put("MEM_SIZE", segment.getMemSize());
                row.put("NUM_ROWS", segment.getNumOfRows());
                row.put("INDEX_NAME", segment.getIndexName());
                row.put("INDEX_ID", segment.getIndexID());
                row.put("STATE", segment.getState());
                row.put("LEVEL", segment.getLevel());
                row.put("NODE_IDS", jsonList(segment.getNodeIDs()));
                row.put("STORAGE_VERSION", segment.getStorageVersion());
                row.put("IS_SORTED", segment.getIsSorted());
                rows.add(row);
            }
            receive.responseResult(request, listResult(request, QUERY_COLUMNS, rows));
        }
    }

    private static void showReplicas(MilvusCmd cmd, ShowCmdContext c, AdapterRequest request, AdapterReceive receive) throws SQLException {
        List<ReplicaInfo> replicas = cmd.describeReplicas(DescribeReplicasReq.builder().databaseName(cmd.getCatalog()).collectionName(readName(c.collectionName)).build()).getReplicas();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ReplicaInfo replica : replicas) {
            checkActive(request);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("REPLICA_ID", replica.getReplicaID());
            row.put("COLLECTION_ID", replica.getCollectionID());
            row.put("PARTITION_IDS", jsonList(replica.getPartitionIDs()));
            row.put("SHARD_REPLICAS", jsonList(replica.getShardReplicas()));
            row.put("NODE_IDS", jsonList(replica.getNodeIDs()));
            row.put("RESOURCE_GROUP", replica.getResourceGroupName());
            row.put("NUM_OUTBOUND_NODE", replica.getNumOutboundNode() == null ? null : JSON.toJson(replica.getNumOutboundNode()));
            rows.add(row);
        }
        receive.responseResult(request, listResult(request, REPLICA_COLUMNS, rows));
    }

    private static String jsonList(List<?> values) {
        return values == null ? null : JSON.toJson(values);
    }

    private static JdbcColumn column(String name, String type) {
        return new JdbcColumn(name, type, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    }
}
