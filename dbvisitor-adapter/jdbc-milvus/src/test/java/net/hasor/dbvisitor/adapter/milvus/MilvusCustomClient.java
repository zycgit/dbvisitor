package net.hasor.dbvisitor.adapter.milvus;
import java.util.Map;
import io.milvus.client.MilvusServiceClient;
import org.powermock.api.mockito.PowerMockito;

public class MilvusCustomClient implements CustomMilvus {

    @Override
    public MilvusServiceClient createMilvusClient(String jdbcUrl, Map<String, String> props) {
        return PowerMockito.mock(MilvusServiceClient.class);
    }
}
