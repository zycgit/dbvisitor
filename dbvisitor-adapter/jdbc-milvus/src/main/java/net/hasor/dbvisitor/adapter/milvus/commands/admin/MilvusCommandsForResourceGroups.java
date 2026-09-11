package net.hasor.dbvisitor.adapter.milvus.commands.admin;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.milvus.common.resourcegroup.ResourceGroupConfig;
import io.milvus.v2.service.resourcegroup.request.*;
import io.milvus.v2.service.resourcegroup.response.DescribeResourceGroupResp;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;

/** Native cluster resource-group management; no database-level filtering or inferred scheduling state. */
public final class MilvusCommandsForResourceGroups extends MilvusCommands {
    private static final JdbcColumn GROUP     = column("RESOURCE_GROUP", AdapterType.String);
    private static final JdbcColumn CAPACITY  = column("CAPACITY", AdapterType.Int);
    private static final JdbcColumn AVAILABLE = column("AVAILABLE_NODES", AdapterType.Int);
    private static final JdbcColumn REPLICAS  = column("LOADED_REPLICAS", AdapterType.String);
    private static final JdbcColumn OUTGOING  = column("OUTGOING_NODES", AdapterType.String);
    private static final JdbcColumn INCOMING  = column("INCOMING_NODES", AdapterType.String);
    private static final JdbcColumn CONFIG    = column("CONFIG", AdapterType.String);
    private static final JdbcColumn NODES     = column("NODES", AdapterType.String);
    private static final Gson       JSON      = new Gson();

    private MilvusCommandsForResourceGroups() {
    }

    public static Future<?> execTransfer(Future<Object> future, MilvusCmd cmd, HintCommandContext h, TransferCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger args = new AtomicInteger(startArgIdx);
        readHints(args, request, h.hint());
        long count = readBound(c.count, args, request, "TRANSFER count", 1);
        String source = readName(c.sourceGroup);
        String target = readName(c.targetGroup);
        if (c.NODES() != null) {
            if (count > Integer.MAX_VALUE) {
                throw new SQLException("TRANSFER NODES count exceeds the SDK INTEGER range.");
            }
            cmd.transferNode(TransferNodeReq.builder().sourceGroupName(source).targetGroupName(target).numOfNodes((int) count).build());
        } else {
            cmd.transferReplica(TransferReplicaReq.builder().databaseName(cmd.getCatalog()).collectionName(readName(c.collectionName)).sourceGroupName(source).targetGroupName(target).numberOfReplicas(count).build());
        }
        // The SDK acknowledges the request; it does not return a migrated-row count.
        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execCreate(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger args = new AtomicInteger(startArgIdx);
        readHints(args, request, h.hint());
        CreateResourceGroupReq.CreateResourceGroupReqBuilder builder = CreateResourceGroupReq.builder().groupName(readName(c.groupName));
        if (c.config != null) {
            builder.config(MilvusResourceGroupConfig.read(MilvusResourceGroupConfig.object(parseLiteral(c.config, args, request))));
        }
        cmd.createResourceGroup(builder.build());
        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execAlter(Future<Object> future, MilvusCmd cmd, HintCommandContext h, AlterCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger args = new AtomicInteger(startArgIdx);
        readHints(args, request, h.hint());
        JsonObject config = MilvusResourceGroupConfig.object(parseLiteral(c.config, args, request));
        Map<String, ResourceGroupConfig> groups = new LinkedHashMap<>();
        if (c.GROUPS() == null) {
            groups.put(readName(c.groupName), MilvusResourceGroupConfig.read(config));
        } else {
            for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
                if (entry.getKey().trim().isEmpty() || !entry.getValue().isJsonObject()) {
                    throw new SQLException("Resource-group CONFIG map requires non-empty names and object values.");
                }
                groups.put(entry.getKey(), MilvusResourceGroupConfig.read(entry.getValue().getAsJsonObject()));
            }
            if (groups.isEmpty()) {
                throw new SQLException("Resource-group CONFIG map must not be empty.");
            }
        }
        // Validate all groups before issuing the single native multi-group update.
        cmd.updateResourceGroups(UpdateResourceGroupsReq.builder().resourceGroups(groups).build());
        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDrop(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        cmd.dropResourceGroup(DropResourceGroupReq.builder().groupName(readName(c.groupName)).build());
        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execShow(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        if (c.GROUPS() != null) {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (String name : cmd.listResourceGroups(ListResourceGroupsReq.builder().build()).getGroupNames()) {
                rows.add(Collections.singletonMap(GROUP.name, name));
            }
            receive.responseResult(request, listResult(request, Collections.singletonList(GROUP), rows));
        } else {
            DescribeResourceGroupResp group = cmd.describeResourceGroup(DescribeResourceGroupReq.builder().groupName(readName(c.groupName)).build());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(GROUP.name, group.getGroupName());
            row.put(CAPACITY.name, group.getCapacity());
            row.put(AVAILABLE.name, group.getNumberOfAvailableNode());
            row.put(REPLICAS.name, JSON.toJson(group.getNumberOfLoadedReplica()));
            row.put(OUTGOING.name, JSON.toJson(group.getNumberOfOutgoingNode()));
            row.put(INCOMING.name, JSON.toJson(group.getNumberOfIncomingNode()));
            row.put(CONFIG.name, MilvusResourceGroupConfig.write(group.getConfig()));
            row.put(NODES.name, JSON.toJson(group.getNodes()));
            receive.responseResult(request, listResult(request, Arrays.asList(GROUP, CAPACITY, AVAILABLE, REPLICAS, OUTGOING, INCOMING, CONFIG, NODES), Collections.singletonList(row)));
        }
        return completed(future);
    }

    private static JdbcColumn column(String name, String type) {
        return new JdbcColumn(name, type, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    }
}
