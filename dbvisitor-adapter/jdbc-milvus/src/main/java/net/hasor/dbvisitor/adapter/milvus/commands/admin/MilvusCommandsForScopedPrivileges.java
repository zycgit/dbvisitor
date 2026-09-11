package net.hasor.dbvisitor.adapter.milvus.commands.admin;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.v2.service.rbac.request.GrantPrivilegeReqV2;
import io.milvus.v2.service.rbac.request.RevokePrivilegeReqV2;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.GrantScopedPrivilegeContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HintCommandContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.PrivilegeScopeContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.RevokeScopedPrivilegeContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readHints;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readName;

/** Native V2 privileges with an explicit database and collection scope. */
public final class MilvusCommandsForScopedPrivileges extends MilvusCommands {
    private MilvusCommandsForScopedPrivileges() {
    }

    public static Future<?> execGrant(Future<Object> future, MilvusCmd cmd, HintCommandContext h, GrantScopedPrivilegeContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        cmd.grantPrivilegeV2(GrantPrivilegeReqV2.builder().roleName(readName(c.roleName)).privilege(readName(c.privilege)).dbName(readScope(c.database)).collectionName(readScope(c.collection)).build());
        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execRevoke(Future<Object> future, MilvusCmd cmd, HintCommandContext h, RevokeScopedPrivilegeContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        cmd.revokePrivilegeV2(RevokePrivilegeReqV2.builder().roleName(readName(c.roleName)).privilege(readName(c.privilege)).dbName(readScope(c.database)).collectionName(readScope(c.collection)).build());
        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    private static String readScope(PrivilegeScopeContext scope) {
        return scope.STAR() == null ? readName(scope.identifier()) : "*";
    }
}
