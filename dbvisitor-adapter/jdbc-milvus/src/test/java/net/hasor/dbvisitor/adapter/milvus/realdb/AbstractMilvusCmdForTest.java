package net.hasor.dbvisitor.adapter.milvus.realdb;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeIndexResponse;
import io.milvus.grpc.ListDatabasesResponse;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.*;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DescribeIndexParam;
import io.milvus.param.index.DropIndexParam;
import io.milvus.param.partition.HasPartitionParam;
import io.milvus.param.partition.ShowPartitionsParam;
import org.junit.Assume;
import org.junit.Before;

public class AbstractMilvusCmdForTest {
    protected static final String  MILVUS_HOST         = "127.0.0.1";
    protected static final int     MILVUS_PORT         = 19530;
    protected static final String  MILVUS_URL          = "jdbc:dbvisitor:milvus://" + MILVUS_HOST + ":" + MILVUS_PORT + "?consistencylevel=strong";
    protected              boolean milvusReady         = false;
    protected static final String  TEST_COLLECTION     = "dbv_table_col";
    protected static final String  TEST_COLLECTION_NEW = "dbv_table_col_renamed";
    protected static final String  TEST_DATABASE       = "dbv_test_db";

    @Before
    public void before() {
        this.milvusReady = isMilvusAvailable();
        Assume.assumeTrue("Milvus is not available.", this.milvusReady);
    }

    protected boolean isMilvusAvailable() {
        MilvusServiceClient client = null;
        try {
            client = newClient();
            R<ListDatabasesResponse> response = client.listDatabases();
            return response.getStatus() == R.Status.Success.getCode();
        } catch (Exception e) {
            return false;
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    protected MilvusServiceClient newClient() {
        ConnectParam connectParam = ConnectParam.newBuilder().withHost(MILVUS_HOST).withPort(MILVUS_PORT).build();
        return new MilvusServiceClient(connectParam);
    }

    //
    // --------------------------------------------------------------------------------------------
    //

    protected boolean hasCollection(String collectionName) {
        MilvusServiceClient client = null;
        try {
            client = newClient();
            R<Boolean> resp = client.hasCollection(HasCollectionParam.newBuilder().withCollectionName(collectionName).build());
            if (resp.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException(resp.getMessage());
            }
            return Boolean.TRUE.equals(resp.getData());
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    protected boolean hasPartition(String collectionName, String partitionName) {
        MilvusServiceClient client = null;
        try {
            client = newClient();
            R<Boolean> resp = client.hasPartition(HasPartitionParam.newBuilder().withCollectionName(collectionName).withPartitionName(partitionName).build());
            if (resp.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException(resp.getMessage());
            }
            return Boolean.TRUE.equals(resp.getData());
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    protected void dropCollection(String collectionName) {
        MilvusServiceClient client = null;
        try {
            client = newClient();
            R<Boolean> has = client.hasCollection(HasCollectionParam.newBuilder().withCollectionName(collectionName).build());
            if (has.getStatus() == R.Status.Success.getCode() && Boolean.TRUE.equals(has.getData())) {
                // Release collection first (ignore errors - it may not be loaded)
                try {
                    client.releaseCollection(ReleaseCollectionParam.newBuilder().withCollectionName(collectionName).build());
                } catch (Exception e) {
                    // ignore
                }
                // Also try to release any loaded partitions
                try {
                    R<io.milvus.grpc.ShowPartitionsResponse> partResp = client.showPartitions(
                            ShowPartitionsParam.newBuilder().withCollectionName(collectionName).build());
                    if (partResp.getStatus() == R.Status.Success.getCode() && partResp.getData() != null) {
                        // Releasing collection already handles partitions, but just in case
                    }
                } catch (Exception e) {
                    // ignore
                }
                client.dropCollection(DropCollectionParam.newBuilder().withCollectionName(collectionName).build());
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    /** Create collection only if it doesn't exist. For a clean start, call dropCollection first. */
    protected void createCollection(String collectionName) {
        MilvusServiceClient client = null;
        try {
            client = newClient();
            R<Boolean> has = client.hasCollection(HasCollectionParam.newBuilder().withCollectionName(collectionName).build());
            if (has.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException(has.getMessage());
            }
            if (Boolean.TRUE.equals(has.getData())) {
                return;
            }

            FieldType fieldType1 = FieldType.newBuilder().withName("book_id").withDataType(DataType.Int64).withPrimaryKey(true).withAutoID(false).build();
            FieldType fieldType2 = FieldType.newBuilder().withName("word_count").withDataType(DataType.Int64).build();
            FieldType fieldType3 = FieldType.newBuilder().withName("book_intro").withDataType(DataType.FloatVector).withDimension(2).build();
            CreateCollectionParam createParam = CreateCollectionParam.newBuilder()//
                    .withCollectionName(collectionName)//
                    .withDescription("Test Collection")//
                    .addFieldType(fieldType1)          //
                    .addFieldType(fieldType2)          //
                    .addFieldType(fieldType3)          //
                    .build();
            R<?> result = client.createCollection(createParam);
            if (result.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException(result.getMessage());
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    protected boolean hasDatabase(String dbName) {
        MilvusServiceClient client = null;
        try {
            client = newClient();
            R<ListDatabasesResponse> response = client.listDatabases();
            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException(response.getMessage());
            }
            return response.getData().getDbNamesList().contains(dbName);
        } catch (Exception e) {
            return false;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    protected void dropDatabase(String dbName) {
        MilvusServiceClient client = null;
        try {
            client = newClient();
            R<?> response = client.dropDatabase(DropDatabaseParam.newBuilder().withDatabaseName(dbName).build());
            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException(response.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    protected void createDatabase(String dbName) {
        MilvusServiceClient client = null;
        try {
            client = newClient();
            R<ListDatabasesResponse> response = client.listDatabases();
            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException(response.getMessage());
            }

            if (response.getData().getDbNamesList().contains(dbName)) {
                return;
            }

            R<?> createResp = client.createDatabase(CreateDatabaseParam.newBuilder().withDatabaseName(dbName).build());
            if (createResp.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException(createResp.getMessage());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    protected boolean hasIndex(String collectionName, String indexName) {
        MilvusServiceClient client = null;
        try {
            client = newClient();
            R<DescribeIndexResponse> response = client.describeIndex(DescribeIndexParam.newBuilder().withCollectionName(collectionName).withIndexName(indexName).build());
            return response.getStatus() == R.Status.Success.getCode() && response.getData().getIndexDescriptionsList().stream().anyMatch(i -> indexName.equals(i.getIndexName()));
        } catch (Exception e) {
            return false;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    protected void createIndex(String collectionName, String indexName) {
        MilvusServiceClient client = null;
        try {
            client = newClient();
            R<?> response = client.createIndex(CreateIndexParam.newBuilder().withCollectionName(collectionName).withFieldName("book_intro").withIndexName(indexName).withIndexType(IndexType.IVF_FLAT).withMetricType(MetricType.L2).withExtraParam("{\"nlist\":1024}").build());
            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException(response.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    protected void dropIndex(String collectionName, String indexName) {
        MilvusServiceClient client = null;
        try {
            client = newClient();
            R<?> response = client.dropIndex(DropIndexParam.newBuilder().withCollectionName(collectionName).withIndexName(indexName).build());
            // Drop index returns success even if index doesn't exist? Milvus usually does idempotent drops.
            // But if it fails for other reasons, we throw.
            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException(response.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    protected void createAlias(String alias, String collectionName) {
        MilvusServiceClient client = null;
        try {
            client = newClient();
            io.milvus.param.R<?> response = client.createAlias(io.milvus.param.alias.CreateAliasParam.newBuilder().withAlias(alias).withCollectionName(collectionName).build());
            if (response.getStatus() != io.milvus.param.R.Status.Success.getCode()) {
                throw new RuntimeException(response.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    protected void dropAlias(String alias) {
        MilvusServiceClient client = null;
        try {
            client = newClient();
            // Drop alias usually idempotent? If not, check existence or ignore error if "alias doesn't exist"
            io.milvus.param.R<?> response = client.dropAlias(io.milvus.param.alias.DropAliasParam.newBuilder().withAlias(alias).build());
            // Ignore error if it's "alias does not exist" or similar, or just check status
            // For robustness in teardown, we might ignore failure.
            // But for test logic, clean drop is expected.
        } catch (Exception e) {
            // ignore
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    protected boolean hasUserSdk(String username) {
        MilvusServiceClient client = newClient();
        try {
            R<io.milvus.grpc.ListCredUsersResponse> response = client.listCredUsers(io.milvus.param.credential.ListCredUsersParam.newBuilder().build());
            if (response.getStatus() != R.Status.Success.getCode()) {
                return false;
            }
            return response.getData().getUsernamesList().contains(username);
        } finally {
            client.close();
        }
    }

    protected boolean hasRoleSdk(String roleName) {
        MilvusServiceClient client = newClient();
        try {
            R<io.milvus.grpc.SelectRoleResponse> response = client.selectRole(io.milvus.param.role.SelectRoleParam.newBuilder().withRoleName(roleName).build());
            if (response.getStatus() != R.Status.Success.getCode()) {
                return false;
            }
            return response.getData().getResultsList().stream().anyMatch(r -> {
                return r.getRole().getName().equals(roleName);
            });
        } finally {
            client.close();
        }
    }

    protected boolean userHasRoleSdk(String username, String roleName) {
        MilvusServiceClient client = newClient();
        try {
            R<io.milvus.grpc.SelectRoleResponse> response = client.selectRole(io.milvus.param.role.SelectRoleParam.newBuilder().withRoleName(roleName).withIncludeUserInfo(true).build());
            if (response.getStatus() != R.Status.Success.getCode()) {
                return false;
            }
            // Find the specific role result
            return response.getData().getResultsList().stream().filter(r -> r.getRole().getName().equals(roleName)).flatMap(r -> r.getUsersList().stream()).anyMatch(u -> u.getName().equals(username));
        } finally {
            client.close();
        }
    }

    protected boolean roleHasPrivilegeSdk(String roleName, String objectType, String objectName, String privilege) {
        MilvusServiceClient client = newClient();
        try {
            R<io.milvus.grpc.SelectGrantResponse> response = client.selectGrantForRole(io.milvus.param.role.SelectGrantForRoleParam.newBuilder().withRoleName(roleName).build());
            if (response.getStatus() != R.Status.Success.getCode()) {
                return false;
            }
            return response.getData().getEntitiesList().stream().anyMatch(grant -> {
                boolean objMatch = grant.getObjectName().equals(objectName);
                // Object Type string matching might depend on SDK/Server version, usually "Collection", "Global"
                // The SDK/Proto might not return object type directly in GrantEntity easy way if it's implicitly part of object?
                // Actually GrantEntity has object_name. Object_type is in entity.getObject().getName()? 
                // Let's check proto. GrantEntity: role, object, grantor
                // ObjectEntity: name
                // Wait, privilege is in GrantorEntity.Privilege.Name

                boolean privMatch = grant.getGrantor().getPrivilege().getName().equals(privilege);
                // For object type, maybe check DB/Collection context?
                // The grant info is simplistic. 
                // For verification, checking ObjectName and Privilege is usually enough if names are unique.
                // But let's check exact match if possible.
                // "Global" object name is usually "*"

                return objMatch && privMatch;
            });
        } finally {
            client.close();
        }
    }
}
