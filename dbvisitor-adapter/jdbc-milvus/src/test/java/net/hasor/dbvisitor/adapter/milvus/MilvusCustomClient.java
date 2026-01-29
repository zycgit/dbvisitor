package net.hasor.dbvisitor.adapter.milvus;

import io.milvus.client.MilvusServiceClient;
import java.util.Map;
import org.powermock.api.mockito.PowerMockito;

public class MilvusCustomClient implements CustomMilvus {

    @Override
    public MilvusServiceClient createMilvusClient(String jdbcUrl, Map<String, String> props) {
        return PowerMockito.mock(MilvusServiceClient.class);
    }
}
