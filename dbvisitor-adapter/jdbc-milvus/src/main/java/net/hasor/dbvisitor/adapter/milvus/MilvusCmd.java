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
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.collection.response.GetLoadStateResp;
import io.milvus.v2.service.collection.response.ListCollectionsResp;
import io.milvus.v2.service.database.request.AlterDatabasePropertiesReq;
import io.milvus.v2.service.database.request.CreateDatabaseReq;
import io.milvus.v2.service.database.request.DropDatabaseReq;
import io.milvus.v2.service.database.response.ListDatabasesResp;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.index.request.DescribeIndexReq;
import io.milvus.v2.service.index.request.DropIndexReq;
import io.milvus.v2.service.index.response.DescribeIndexResp;
import io.milvus.v2.service.partition.request.*;
import io.milvus.v2.service.rbac.request.*;
import io.milvus.v2.service.rbac.response.DescribeRoleResp;
import io.milvus.v2.service.utility.request.AlterAliasReq;
import io.milvus.v2.service.utility.request.CreateAliasReq;
import io.milvus.v2.service.utility.request.DropAliasReq;
import io.milvus.v2.service.utility.request.FlushReq;
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

    public Boolean hasCollection(HasCollectionReq request) throws SQLException {
        return this.call("hasCollection", HasCollectionReq.class, request, MilvusClientV2::hasCollection);
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
        if (description == null || description.getCollectionSchema() == null)
            throw new SQLException("Milvus returned no schema for collection '" + collection + "'.");
        Map<String, FieldSchema> fields = new LinkedHashMap<>();
        for (FieldSchema field : MilvusSchema.collectionFields(description))
            fields.put(field.getName(), field);
        return fields;
    }
}
