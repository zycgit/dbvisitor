/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.util.function.Function;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.exception.ServerException;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeIndexResponse;
import io.milvus.grpc.ErrorCode;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.alias.CreateAliasParam;
import io.milvus.param.alias.DropAliasParam;
import io.milvus.param.collection.*;
import io.milvus.param.credential.ListCredUsersParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DescribeIndexParam;
import io.milvus.param.index.DropIndexParam;
import io.milvus.param.partition.HasPartitionParam;
import io.milvus.param.role.SelectGrantForRoleParam;
import io.milvus.param.role.SelectRoleParam;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import org.junit.Before;

public class AbstractMilvusCmdForTest {
    protected static final String  MILVUS_HOST         = "127.0.0.1";
    protected static final int     MILVUS_PORT         = 2953;
    protected static final String  MILVUS_URL          = "jdbc:dbvisitor:milvus://" + MILVUS_HOST + ":" + MILVUS_PORT + "?consistencylevel=strong";
    protected static final String  TEST_COLLECTION     = "dbv_table_col";
    protected static final String  TEST_COLLECTION_NEW = "dbv_table_col_renamed";
    protected static final String  TEST_DATABASE       = "dbv_test_db";
    // Server error identifiers: milvus/pkg/util/merr/errors.go.
    private static final   int     INDEX_NOT_FOUND     = 700;
    private static final   int     ALIAS_NOT_FOUND     = 1600;
    protected              boolean milvusSelected;

    @Before
    public void before() {
        OneApiDataSourceManager.assumeCurrentDataSource("milvus");
        this.milvusSelected = true;
        withClient(client -> requireSuccess(client.listDatabases()));
    }

    protected MilvusServiceClient newClient() {
        return new MilvusServiceClient(ConnectParam.newBuilder().withHost(MILVUS_HOST).withPort(MILVUS_PORT).build());
    }

    private <T> T withClient(Function<MilvusServiceClient, T> operation) {
        MilvusServiceClient client = newClient();
        try (ClientCloser ignored = client::close) {
            return operation.apply(client);
        }
    }

    @FunctionalInterface
    private interface ClientCloser extends AutoCloseable {
        @Override
        void close();
    }

    private <T> T requireSuccess(R<T> response) {
        if (!Integer.valueOf(R.Status.Success.getCode()).equals(response.getStatus())) {
            throw new IllegalStateException("Milvus SDK operation failed, status=" + response.getStatus(), response.getException());
        }
        return response.getData();
    }

    private boolean missingIndex(R<?> response) {
        if (response.getException() instanceof ServerException server) {
            return Integer.valueOf(INDEX_NOT_FOUND).equals(server.getStatus()) || server.getCompatibleCode() == ErrorCode.IndexNotExist;
        }
        return Integer.valueOf(R.Status.IndexNotExist.getCode()).equals(response.getStatus());
    }

    protected boolean hasCollection(String collectionName) {
        return withClient(client -> requireSuccess(client.hasCollection(HasCollectionParam.newBuilder().withCollectionName(collectionName).build())));
    }

    protected boolean hasPartition(String collectionName, String partitionName) {
        return withClient(client -> requireSuccess(client.hasPartition(HasPartitionParam.newBuilder().withCollectionName(collectionName).withPartitionName(partitionName).build())));
    }

    protected void dropCollection(String collectionName) {
        withClient(client -> {
            boolean exists = requireSuccess(client.hasCollection(HasCollectionParam.newBuilder().withCollectionName(collectionName).build()));
            if (exists) {
                // DropCollection also removes loaded collection resources.
                requireSuccess(client.dropCollection(DropCollectionParam.newBuilder().withCollectionName(collectionName).build()));
            }
            return null;
        });
    }

    protected void createCollection(String collectionName) {
        withClient(client -> {
            if (requireSuccess(client.hasCollection(HasCollectionParam.newBuilder().withCollectionName(collectionName).build()))) {
                return null;
            }
            FieldType id = FieldType.newBuilder().withName("book_id").withDataType(DataType.Int64).withPrimaryKey(true).withAutoID(false).build();
            FieldType count = FieldType.newBuilder().withName("word_count").withDataType(DataType.Int64).build();
            FieldType vector = FieldType.newBuilder().withName("book_intro").withDataType(DataType.FloatVector).withDimension(2).build();
            return requireSuccess(client.createCollection(CreateCollectionParam.newBuilder().withCollectionName(collectionName).withDescription("Test Collection").withConsistencyLevel(ConsistencyLevelEnum.STRONG).addFieldType(id).addFieldType(count).addFieldType(vector).build()));
        });
    }

    protected boolean hasDatabase(String dbName) {
        return withClient(client -> requireSuccess(client.listDatabases()).getDbNamesList().contains(dbName));
    }

    protected void dropDatabase(String dbName) {
        withClient(client -> requireSuccess(client.dropDatabase(DropDatabaseParam.newBuilder().withDatabaseName(dbName).build())));
    }

    protected void createDatabase(String dbName) {
        withClient(client -> {
            if (!requireSuccess(client.listDatabases()).getDbNamesList().contains(dbName)) {
                requireSuccess(client.createDatabase(CreateDatabaseParam.newBuilder().withDatabaseName(dbName).build()));
            }
            return null;
        });
    }

    protected boolean hasIndex(String collectionName, String indexName) {
        return withClient(client -> {
            R<DescribeIndexResponse> response = client.describeIndex(DescribeIndexParam.newBuilder().withCollectionName(collectionName).withIndexName(indexName).build());
            if (missingIndex(response)) {
                return false;
            }
            return requireSuccess(response).getIndexDescriptionsList().stream().anyMatch(index -> indexName.equals(index.getIndexName()));
        });
    }

    protected void createIndex(String collectionName, String indexName) {
        withClient(client -> requireSuccess(client.createIndex(CreateIndexParam.newBuilder().withCollectionName(collectionName).withFieldName("book_intro").withIndexName(indexName).withIndexType(IndexType.IVF_FLAT).withMetricType(MetricType.L2).withExtraParam("{\"nlist\":1024}").build())));
    }

    protected void dropIndex(String collectionName, String indexName) {
        withClient(client -> requireSuccess(client.dropIndex(DropIndexParam.newBuilder().withCollectionName(collectionName).withIndexName(indexName).build())));
    }

    protected void createAlias(String alias, String collectionName) {
        withClient(client -> requireSuccess(client.createAlias(CreateAliasParam.newBuilder().withAlias(alias).withCollectionName(collectionName).build())));
    }

    protected void dropAlias(String alias) {
        withClient(client -> {
            R<?> response = client.dropAlias(DropAliasParam.newBuilder().withAlias(alias).build());
            if (response.getException() instanceof ServerException server && Integer.valueOf(ALIAS_NOT_FOUND).equals(server.getStatus())) {
                return null;
            }
            return requireSuccess(response);
        });
    }

    protected boolean hasUserSdk(String username) {
        return withClient(client -> requireSuccess(client.listCredUsers(ListCredUsersParam.newBuilder().build())).getUsernamesList().contains(username));
    }

    protected boolean hasRoleSdk(String roleName) {
        // The legacy selectRole endpoint fails for missing roles. V2 listRoles gives an
        // authoritative successful list without guessing what an error message means.
        MilvusClientV2 client = new MilvusClientV2(ConnectConfig.builder().uri("http://" + MILVUS_HOST + ":" + MILVUS_PORT).build());
        try (ClientCloser ignored = client::close) {
            return client.listRoles().contains(roleName);
        }
    }

    protected boolean userHasRoleSdk(String username, String roleName) {
        return withClient(client -> requireSuccess(client.selectRole(SelectRoleParam.newBuilder().withRoleName(roleName).withIncludeUserInfo(true).build())).getResultsList().stream().filter(role -> role.getRole().getName().equals(roleName)).flatMap(role -> role.getUsersList().stream()).anyMatch(user -> user.getName().equals(username)));
    }

    protected boolean roleHasPrivilegeSdk(String roleName, String objectType, String objectName, String privilege) {
        return withClient(client -> requireSuccess(client.selectGrantForRole(SelectGrantForRoleParam.newBuilder().withRoleName(roleName).build())).getEntitiesList().stream().anyMatch(grant -> grant.getObject().getName().equals(objectType) && grant.getObjectName().equals(objectName) && grant.getGrantor().getPrivilege().getName().equals(privilege)));
    }
}
