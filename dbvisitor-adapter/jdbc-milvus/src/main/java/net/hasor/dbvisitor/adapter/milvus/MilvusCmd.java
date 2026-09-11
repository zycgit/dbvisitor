/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.net.ssl.SSLContext;
import io.milvus.grpc.FieldSchema;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.collection.response.*;
import io.milvus.v2.service.database.request.*;
import io.milvus.v2.service.database.response.DescribeDatabaseResp;
import io.milvus.v2.service.database.response.ListDatabasesResp;
import io.milvus.v2.service.index.request.*;
import io.milvus.v2.service.index.response.DescribeIndexResp;
import io.milvus.v2.service.partition.request.*;
import io.milvus.v2.service.partition.response.GetPartitionStatsResp;
import io.milvus.v2.service.rbac.request.*;
import io.milvus.v2.service.rbac.response.DescribeRoleResp;
import io.milvus.v2.service.rbac.response.DescribeUserResp;
import io.milvus.v2.service.rbac.response.ListPrivilegeGroupsResp;
import io.milvus.v2.service.resourcegroup.request.*;
import io.milvus.v2.service.resourcegroup.response.DescribeResourceGroupResp;
import io.milvus.v2.service.resourcegroup.response.ListResourceGroupsResp;
import io.milvus.v2.service.utility.request.*;
import io.milvus.v2.service.utility.response.*;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.response.*;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.adapter.milvus.mapping.MilvusSchema;
import net.hasor.dbvisitor.adapter.milvus.transport.MilvusImportClient;
import net.hasor.dbvisitor.driver.AdapterRequest;

/** Typed calls to the connection's SDK client, with a shared interception boundary. */
public class MilvusCmd implements AutoCloseable {
    private final    MilvusClientV2     client;
    private final    InvocationHandler  invocation;
    private final    String             catalog;
    private final    ConnectConfig      config;
    private final    SSLContext         httpTls;
    private          MilvusImportClient importEndpoint;
    private volatile boolean            closed;

    MilvusCmd(MilvusClientV2 client, String catalog, InvocationHandler invocation, ConnectConfig config, SSLContext httpTls) {
        this.client = client;
        this.catalog = catalog;
        this.invocation = invocation;
        this.config = config;
        this.httpTls = httpTls;
    }

    public String getCatalog() {
        return this.catalog;
    }

    public void setCatalog(String catalog) {
        if (!StringUtils.equals(this.catalog, catalog)) {
            throw new UnsupportedOperationException("Milvus does not support changing catalogs.");
        }
    }

    public MilvusClientV2 getClient() throws SQLException {
        this.checkOpen();
        return this.client;
    }

    private void checkOpen() throws SQLException {
        if (this.closed) {
            throw new SQLException("Milvus connection is closed.");
        }
    }

    @Override
    public synchronized void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        this.client.close();
    }

    // SQL commands use typed SDK calls; reflection is limited to the configured interceptor.
    private <P, T> T call(String name, Class<P> parameterType, P parameter, BiFunction<MilvusClientV2, P, T> action) throws SQLException {
        MilvusClientV2 connected = this.getClient();
        try {
            if (this.invocation == null) {
                return action.apply(connected, parameter);
            }
            Method method = MilvusClientV2.class.getMethod(name, parameterType);
            return (T) this.invoke(connected, method, new Object[] { parameter });
        } catch (Exception e) {
            throw e instanceof SQLException ? (SQLException) e : new SQLException(e.getMessage(), e);
        }
    }

    private <T> T call(String name, Function<MilvusClientV2, T> action) throws SQLException {
        MilvusClientV2 connected = this.getClient();
        try {
            if (this.invocation == null) {
                return action.apply(connected);
            }
            return (T) this.invoke(connected, MilvusClientV2.class.getMethod(name), new Object[0]);
        } catch (Exception e) {
            throw e instanceof SQLException ? (SQLException) e : new SQLException(e.getMessage(), e);
        }
    }

    private Object invoke(Object target, Method method, Object[] arguments) throws Exception {
        try {
            return this.invocation.invoke(target, method, arguments);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw (Error) cause;
        } catch (Exception e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException("Milvus interception failed: " + e.getMessage(), e);
        }
    }

    // Database, collection, index and partition commands

    public String getServerVersion() throws SQLException {
        return this.call("getServerVersion", MilvusClientV2::getServerVersion);
    }

    public GetServerVersionResp getServerVersionV2(GetServerVersionReq request) throws SQLException {
        return this.call("getServerVersionV2", GetServerVersionReq.class, request, MilvusClientV2::getServerVersionV2);
    }

    public CheckHealthResp checkHealth() throws SQLException {
        return this.call("checkHealth", MilvusClientV2::checkHealth);
    }

    public GetPersistentSegmentInfoResp getPersistentSegmentInfo(GetPersistentSegmentInfoReq request) throws SQLException {
        return this.call("getPersistentSegmentInfo", GetPersistentSegmentInfoReq.class, request, MilvusClientV2::getPersistentSegmentInfo);
    }

    public GetQuerySegmentInfoResp getQuerySegmentInfo(GetQuerySegmentInfoReq request) throws SQLException {
        return this.call("getQuerySegmentInfo", GetQuerySegmentInfoReq.class, request, MilvusClientV2::getQuerySegmentInfo);
    }

    public void createDatabase(CreateDatabaseReq request) throws SQLException {
        this.call("createDatabase", CreateDatabaseReq.class, request, (client, value) -> {
            client.createDatabase(value);
            return null;
        });
    }

    public void dropDatabase(DropDatabaseReq request) throws SQLException {
        this.call("dropDatabase", DropDatabaseReq.class, request, (client, value) -> {
            client.dropDatabase(value);
            return null;
        });
    }

    public void alterDatabaseProperties(AlterDatabasePropertiesReq request) throws SQLException {
        this.call("alterDatabaseProperties", AlterDatabasePropertiesReq.class, request, (client, value) -> {
            client.alterDatabaseProperties(value);
            return null;
        });
    }

    public ListDatabasesResp listDatabases() throws SQLException {
        return this.call("listDatabases", MilvusClientV2::listDatabases);
    }

    public DescribeDatabaseResp describeDatabase(DescribeDatabaseReq request) throws SQLException {
        return this.call("describeDatabase", DescribeDatabaseReq.class, request, MilvusClientV2::describeDatabase);
    }

    public void dropDatabaseProperties(DropDatabasePropertiesReq request) throws SQLException {
        this.call("dropDatabaseProperties", DropDatabasePropertiesReq.class, request, (client, value) -> {
            client.dropDatabaseProperties(value);
            return null;
        });
    }

    public void addCollectionFunction(AddCollectionFunctionReq request) throws SQLException {
        this.call("addCollectionFunction", AddCollectionFunctionReq.class, request, (client, value) -> {
            client.addCollectionFunction(value);
            return null;
        });
    }

    public void alterCollectionFunction(AlterCollectionFunctionReq request) throws SQLException {
        this.call("alterCollectionFunction", AlterCollectionFunctionReq.class, request, (client, value) -> {
            client.alterCollectionFunction(value);
            return null;
        });
    }

    public void dropCollectionFunction(DropCollectionFunctionReq request) throws SQLException {
        this.call("dropCollectionFunction", DropCollectionFunctionReq.class, request, (client, value) -> {
            client.dropCollectionFunction(value);
            return null;
        });
    }

    public void addCollectionField(AddCollectionFieldReq request) throws SQLException {
        this.call("addCollectionField", AddCollectionFieldReq.class, request, (client, value) -> {
            client.addCollectionField(value);
            return null;
        });
    }

    public void alterCollectionField(AlterCollectionFieldReq request) throws SQLException {
        this.call("alterCollectionField", AlterCollectionFieldReq.class, request, (client, value) -> {
            client.alterCollectionField(value);
            return null;
        });
    }

    public void dropCollectionFieldProperties(DropCollectionFieldPropertiesReq request) throws SQLException {
        this.call("dropCollectionFieldProperties", DropCollectionFieldPropertiesReq.class, request, (client, value) -> {
            client.dropCollectionFieldProperties(value);
            return null;
        });
    }

    public void alterCollectionProperties(AlterCollectionPropertiesReq request) throws SQLException {
        this.call("alterCollectionProperties", AlterCollectionPropertiesReq.class, request, (client, value) -> {
            client.alterCollectionProperties(value);
            return null;
        });
    }

    public void dropCollectionProperties(DropCollectionPropertiesReq request) throws SQLException {
        this.call("dropCollectionProperties", DropCollectionPropertiesReq.class, request, (client, value) -> {
            client.dropCollectionProperties(value);
            return null;
        });
    }

    public void alterIndexProperties(AlterIndexPropertiesReq request) throws SQLException {
        this.call("alterIndexProperties", AlterIndexPropertiesReq.class, request, (client, value) -> {
            client.alterIndexProperties(value);
            return null;
        });
    }

    public void dropIndexProperties(DropIndexPropertiesReq request) throws SQLException {
        this.call("dropIndexProperties", DropIndexPropertiesReq.class, request, (client, value) -> {
            client.dropIndexProperties(value);
            return null;
        });
    }

    public Boolean hasCollection(HasCollectionReq request) throws SQLException {
        return this.call("hasCollection", HasCollectionReq.class, request, MilvusClientV2::hasCollection);
    }

    public DescribeAliasResp describeAlias(DescribeAliasReq request) throws SQLException {
        return this.call("describeAlias", DescribeAliasReq.class, request, MilvusClientV2::describeAlias);
    }

    public ListAliasResp listAliases(ListAliasesReq request) throws SQLException {
        return this.call("listAliases", ListAliasesReq.class, request, MilvusClientV2::listAliases);
    }

    public GetCollectionStatsResp getCollectionStats(GetCollectionStatsReq request) throws SQLException {
        return this.call("getCollectionStats", GetCollectionStatsReq.class, request, MilvusClientV2::getCollectionStats);
    }

    public GetPartitionStatsResp getPartitionStats(GetPartitionStatsReq request) throws SQLException {
        return this.call("getPartitionStats", GetPartitionStatsReq.class, request, MilvusClientV2::getPartitionStats);
    }

    public void createCollection(CreateCollectionReq request) throws SQLException {
        this.call("createCollection", CreateCollectionReq.class, request, (client, value) -> {
            client.createCollection(value);
            return null;
        });
    }

    public void dropCollection(DropCollectionReq request) throws SQLException {
        this.call("dropCollection", DropCollectionReq.class, request, (client, value) -> {
            client.dropCollection(value);
            return null;
        });
    }

    public void truncateCollection(TruncateCollectionReq request) throws SQLException {
        this.call("truncateCollection", TruncateCollectionReq.class, request, (client, value) -> {
            client.truncateCollection(value);
            return null;
        });
    }

    public void renameCollection(RenameCollectionReq request) throws SQLException {
        this.call("renameCollection", RenameCollectionReq.class, request, (client, value) -> {
            client.renameCollection(value);
            return null;
        });
    }

    public ListCollectionsResp listCollectionsV2(ListCollectionsReq request) throws SQLException {
        return call("listCollectionsV2", ListCollectionsReq.class, request, MilvusClientV2::listCollectionsV2);
    }

    public DescribeCollectionResp describeCollection(DescribeCollectionReq request) throws SQLException {
        return this.call("describeCollection", DescribeCollectionReq.class, request, MilvusClientV2::describeCollection);
    }

    public void createIndex(CreateIndexReq request) throws SQLException {
        this.call("createIndex", CreateIndexReq.class, request, (client, value) -> {
            client.createIndex(value);
            return null;
        });
    }

    public void dropIndex(DropIndexReq request) throws SQLException {
        this.call("dropIndex", DropIndexReq.class, request, (client, value) -> {
            client.dropIndex(value);
            return null;
        });
    }

    public DescribeIndexResp describeIndex(DescribeIndexReq request) throws SQLException {
        return this.call("describeIndex", DescribeIndexReq.class, request, MilvusClientV2::describeIndex);
    }

    public List<String> listIndexes(ListIndexesReq request) throws SQLException {
        return this.call("listIndexes", ListIndexesReq.class, request, MilvusClientV2::listIndexes);
    }

    public Boolean hasPartition(HasPartitionReq request) throws SQLException {
        return this.call("hasPartition", HasPartitionReq.class, request, MilvusClientV2::hasPartition);
    }

    public void createPartition(CreatePartitionReq request) throws SQLException {
        this.call("createPartition", CreatePartitionReq.class, request, (client, value) -> {
            client.createPartition(value);
            return null;
        });
    }

    public void dropPartition(DropPartitionReq request) throws SQLException {
        this.call("dropPartition", DropPartitionReq.class, request, (client, value) -> {
            client.dropPartition(value);
            return null;
        });
    }

    public List<String> listPartitions(ListPartitionsReq request) throws SQLException {
        return this.call("listPartitions", ListPartitionsReq.class, request, MilvusClientV2::listPartitions);
    }

    public void loadPartitions(LoadPartitionsReq request) throws SQLException {
        this.call("loadPartitions", LoadPartitionsReq.class, request, (client, value) -> {
            client.loadPartitions(value);
            return null;
        });
    }

    public void releasePartitions(ReleasePartitionsReq request) throws SQLException {
        this.call("releasePartitions", ReleasePartitionsReq.class, request, (client, value) -> {
            client.releasePartitions(value);
            return null;
        });
    }

    public void loadCollection(LoadCollectionReq request) throws SQLException {
        this.call("loadCollection", LoadCollectionReq.class, request, (client, value) -> {
            client.loadCollection(value);
            return null;
        });
    }

    public void releaseCollection(ReleaseCollectionReq request) throws SQLException {
        this.call("releaseCollection", ReleaseCollectionReq.class, request, (client, value) -> {
            client.releaseCollection(value);
            return null;
        });
    }

    public GetLoadStateResp getLoadStateV2(GetLoadStateReq request) throws SQLException {
        return this.call("getLoadStateV2", GetLoadStateReq.class, request, MilvusClientV2::getLoadStateV2);
    }

    public DescribeReplicasResp describeReplicas(DescribeReplicasReq request) throws SQLException {
        return this.call("describeReplicas", DescribeReplicasReq.class, request, MilvusClientV2::describeReplicas);
    }

    // Data selection and writes

    public InsertResp insert(InsertReq request) throws SQLException {
        return this.call("insert", InsertReq.class, request, MilvusClientV2::insert);
    }

    public UpsertResp upsert(UpsertReq request) throws SQLException {
        return this.call("upsert", UpsertReq.class, request, MilvusClientV2::upsert);
    }

    public DeleteResp delete(DeleteReq request) throws SQLException {
        return this.call("delete", DeleteReq.class, request, MilvusClientV2::delete);
    }

    public QueryResp query(QueryReq request) throws SQLException {
        return this.call("query", QueryReq.class, request, MilvusClientV2::query);
    }

    public SearchResp search(SearchReq request) throws SQLException {
        return this.call("search", SearchReq.class, request, MilvusClientV2::search);
    }

    public SearchResp hybridSearch(HybridSearchReq request) throws SQLException {
        return this.call("hybridSearch", HybridSearchReq.class, request, MilvusClientV2::hybridSearch);
    }

    public QueryIterator queryIterator(QueryIteratorReq request) throws SQLException {
        return this.call("queryIterator", QueryIteratorReq.class, request, MilvusClientV2::queryIterator);
    }

    public SearchIteratorV2 searchIteratorV2(SearchIteratorReqV2 request) throws SQLException {
        return this.call("searchIteratorV2", SearchIteratorReqV2.class, request, MilvusClientV2::searchIteratorV2);
    }

    public void createAlias(CreateAliasReq request) throws SQLException {
        this.call("createAlias", CreateAliasReq.class, request, (client, value) -> {
            client.createAlias(value);
            return null;
        });
    }

    public void dropAlias(DropAliasReq request) throws SQLException {
        this.call("dropAlias", DropAliasReq.class, request, (client, value) -> {
            client.dropAlias(value);
            return null;
        });
    }

    public void alterAlias(AlterAliasReq request) throws SQLException {
        this.call("alterAlias", AlterAliasReq.class, request, (client, value) -> {
            client.alterAlias(value);
            return null;
        });
    }

    public void flush(FlushReq request) throws SQLException {
        this.call("flush", FlushReq.class, request, (client, value) -> {
            client.flush(value);
            return null;
        });
    }

    public CompactResp compact(CompactReq request) throws SQLException {
        return this.call("compact", CompactReq.class, request, MilvusClientV2::compact);
    }

    public FlushAllResp flushAll(FlushAllReq request) throws SQLException {
        return this.call("flushAll", FlushAllReq.class, request, MilvusClientV2::flushAll);
    }

    public GetFlushAllStateResp getFlushAllState(GetFlushAllStateReq request) throws SQLException {
        return this.call("getFlushAllState", GetFlushAllStateReq.class, request, MilvusClientV2::getFlushAllState);
    }

    public GetCompactionStateResp getCompactionState(GetCompactionStateReq request) throws SQLException {
        return this.call("getCompactionState", GetCompactionStateReq.class, request, MilvusClientV2::getCompactionState);
    }

    public GetCompactionPlansResp getCompactionPlans(GetCompactionPlansReq request) throws SQLException {
        return this.call("getCompactionPlans", GetCompactionPlansReq.class, request, MilvusClientV2::getCompactionPlans);
    }

    // Cluster resource groups

    public void transferNode(TransferNodeReq request) throws SQLException {
        this.call("transferNode", TransferNodeReq.class, request, (client, value) -> {
            client.transferNode(value);
            return null;
        });
    }

    public void transferReplica(TransferReplicaReq request) throws SQLException {
        this.call("transferReplica", TransferReplicaReq.class, request, (client, value) -> {
            client.transferReplica(value);
            return null;
        });
    }

    public void createResourceGroup(CreateResourceGroupReq request) throws SQLException {
        this.call("createResourceGroup", CreateResourceGroupReq.class, request, (client, value) -> {
            client.createResourceGroup(value);
            return null;
        });
    }

    public void updateResourceGroups(UpdateResourceGroupsReq request) throws SQLException {
        this.call("updateResourceGroups", UpdateResourceGroupsReq.class, request, (client, value) -> {
            client.updateResourceGroups(value);
            return null;
        });
    }

    public void dropResourceGroup(DropResourceGroupReq request) throws SQLException {
        this.call("dropResourceGroup", DropResourceGroupReq.class, request, (client, value) -> {
            client.dropResourceGroup(value);
            return null;
        });
    }

    public ListResourceGroupsResp listResourceGroups(ListResourceGroupsReq request) throws SQLException {
        return this.call("listResourceGroups", ListResourceGroupsReq.class, request, MilvusClientV2::listResourceGroups);
    }

    public DescribeResourceGroupResp describeResourceGroup(DescribeResourceGroupReq request) throws SQLException {
        return this.call("describeResourceGroup", DescribeResourceGroupReq.class, request, MilvusClientV2::describeResourceGroup);
    }

    // Users, roles and privileges

    public List<String> listUsers() throws SQLException {
        return this.call("listUsers", MilvusClientV2::listUsers);
    }

    public List<String> listRoles() throws SQLException {
        return this.call("listRoles", MilvusClientV2::listRoles);
    }

    public void createUser(CreateUserReq request) throws SQLException {
        this.call("createUser", CreateUserReq.class, request, (client, value) -> {
            client.createUser(value);
            return null;
        });
    }

    public DescribeUserResp describeUser(DescribeUserReq request) throws SQLException {
        return this.call("describeUser", DescribeUserReq.class, request, MilvusClientV2::describeUser);
    }

    public void updatePassword(UpdatePasswordReq request) throws SQLException {
        this.call("updatePassword", UpdatePasswordReq.class, request, (client, value) -> {
            client.updatePassword(value);
            return null;
        });
    }

    public void updateUser(UpdateUserReq request) throws SQLException {
        this.call("updateUser", UpdateUserReq.class, request, (client, value) -> {
            client.updateUser(value);
            return null;
        });
    }

    public void alterRole(AlterRoleReq request) throws SQLException {
        this.call("alterRole", AlterRoleReq.class, request, (client, value) -> {
            client.alterRole(value);
            return null;
        });
    }

    public void dropUser(DropUserReq request) throws SQLException {
        this.call("dropUser", DropUserReq.class, request, (client, value) -> {
            client.dropUser(value);
            return null;
        });
    }

    public void createRole(CreateRoleReq request) throws SQLException {
        this.call("createRole", CreateRoleReq.class, request, (client, value) -> {
            client.createRole(value);
            return null;
        });
    }

    public void dropRole(DropRoleReq request) throws SQLException {
        this.call("dropRole", DropRoleReq.class, request, (client, value) -> {
            client.dropRole(value);
            return null;
        });
    }

    public DescribeRoleResp describeRole(DescribeRoleReq request) throws SQLException {
        return this.call("describeRole", DescribeRoleReq.class, request, MilvusClientV2::describeRole);
    }

    public void grantRole(GrantRoleReq request) throws SQLException {
        this.call("grantRole", GrantRoleReq.class, request, (client, value) -> {
            client.grantRole(value);
            return null;
        });
    }

    public void revokeRole(RevokeRoleReq request) throws SQLException {
        this.call("revokeRole", RevokeRoleReq.class, request, (client, value) -> {
            client.revokeRole(value);
            return null;
        });
    }

    public void grantPrivilege(GrantPrivilegeReq request) throws SQLException {
        this.call("grantPrivilege", GrantPrivilegeReq.class, request, (client, value) -> {
            client.grantPrivilege(value);
            return null;
        });
    }

    public void createPrivilegeGroup(CreatePrivilegeGroupReq request) throws SQLException {
        this.call("createPrivilegeGroup", CreatePrivilegeGroupReq.class, request, (client, value) -> {
            client.createPrivilegeGroup(value);
            return null;
        });
    }

    public RunAnalyzerResp runAnalyzer(RunAnalyzerReq request) throws SQLException {
        return this.call("runAnalyzer", RunAnalyzerReq.class, request, MilvusClientV2::runAnalyzer);
    }

    public void dropPrivilegeGroup(DropPrivilegeGroupReq request) throws SQLException {
        this.call("dropPrivilegeGroup", DropPrivilegeGroupReq.class, request, (client, value) -> {
            client.dropPrivilegeGroup(value);
            return null;
        });
    }

    public void addPrivilegesToGroup(AddPrivilegesToGroupReq request) throws SQLException {
        this.call("addPrivilegesToGroup", AddPrivilegesToGroupReq.class, request, (client, value) -> {
            client.addPrivilegesToGroup(value);
            return null;
        });
    }

    public void removePrivilegesFromGroup(RemovePrivilegesFromGroupReq request) throws SQLException {
        this.call("removePrivilegesFromGroup", RemovePrivilegesFromGroupReq.class, request, (client, value) -> {
            client.removePrivilegesFromGroup(value);
            return null;
        });
    }

    public ListPrivilegeGroupsResp listPrivilegeGroups(ListPrivilegeGroupsReq request) throws SQLException {
        return this.call("listPrivilegeGroups", ListPrivilegeGroupsReq.class, request, MilvusClientV2::listPrivilegeGroups);
    }

    public void grantPrivilegeV2(GrantPrivilegeReqV2 request) throws SQLException {
        this.call("grantPrivilegeV2", GrantPrivilegeReqV2.class, request, (client, value) -> {
            client.grantPrivilegeV2(value);
            return null;
        });
    }

    public void revokePrivilegeV2(RevokePrivilegeReqV2 request) throws SQLException {
        this.call("revokePrivilegeV2", RevokePrivilegeReqV2.class, request, (client, value) -> {
            client.revokePrivilegeV2(value);
            return null;
        });
    }

    public void revokePrivilege(RevokePrivilegeReq request) throws SQLException {
        this.call("revokePrivilege", RevokePrivilegeReq.class, request, (client, value) -> {
            client.revokePrivilege(value);
            return null;
        });
    }

    // Import uses the official REST API with the JDBC address and TLS configuration.
    public synchronized MilvusImportClient importClient() throws SQLException {
        this.checkOpen();
        if (this.importEndpoint == null) {
            this.importEndpoint = new MilvusImportClient(this.config, this.httpTls);
        }
        return this.importEndpoint;
    }

    public Map<String, FieldSchema> describeFields(String collection, AdapterRequest request) throws SQLException {
        MilvusRequest.checkActive(request);
        DescribeCollectionResp description = this.describeCollection(DescribeCollectionReq.builder().databaseName(this.getCatalog()).collectionName(collection).build());
        MilvusRequest.checkActive(request);
        if (description == null || description.getCollectionSchema() == null) {
            throw new SQLException("Milvus returned no schema for collection '" + collection + "'.");
        }
        Map<String, FieldSchema> fields = new LinkedHashMap<>();
        for (FieldSchema field : MilvusSchema.collectionFields(description)) {
            fields.put(field.getName(), field);
        }
        return fields;
    }
}
