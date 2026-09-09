package net.hasor.dbvisitor.adapter.milvus.commands.schema;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.v2.service.utility.request.AlterAliasReq;
import io.milvus.v2.service.utility.request.CreateAliasReq;
import io.milvus.v2.service.utility.request.DropAliasReq;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.AlterCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.CreateCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.DropCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HintCommandContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readHints;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readName;

/** Collection alias definitions. */
public final class MilvusCommandsForAlias extends MilvusCommands {
    private MilvusCommandsForAlias() {
    }

    // Collection aliases

    public static Future<?> execCreateAlias(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String aliasName = readName(c.aliasName);
        String collectionName = readName(c.collectionName);

        CreateAliasReq param = CreateAliasReq.builder().databaseName(cmd.getCatalog())//
                .alias(aliasName)//
                .collectionName(collectionName)//
                .build();

        cmd.createAlias(param);

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropAlias(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String aliasName = readName(c.aliasName);

        DropAliasReq param = DropAliasReq.builder().databaseName(cmd.getCatalog())//
                .alias(aliasName)//
                .build();

        try {
            cmd.dropAlias(param);
        } catch (SQLException e) {
            boolean ifExists = c.IF() != null && c.EXISTS() != null;
            if (!ifExists || e.getMessage() == null || !e.getMessage().contains("alias does not exist")) {
                throw e;
            }
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execAlterAlias(Future<Object> future, MilvusCmd cmd, HintCommandContext h, AlterCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String aliasName = readName(c.aliasName);
        String collectionName = readName(c.collectionName);

        AlterAliasReq param = AlterAliasReq.builder().databaseName(cmd.getCatalog())//
                .alias(aliasName)//
                .collectionName(collectionName)//
                .build();

        cmd.alterAlias(param);

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

}
