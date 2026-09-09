package net.hasor.dbvisitor.adapter.milvus;
import java.util.Map;
import io.milvus.v2.client.MilvusClientV2;
import org.powermock.api.mockito.PowerMockito;

public class MilvusCustomClient implements CustomMilvus {

    @Override
    public MilvusClientV2 createMilvusClient(String jdbcUrl, Map<String, String> props) {
        return PowerMockito.mock(MilvusClientV2.class);
    }
}
