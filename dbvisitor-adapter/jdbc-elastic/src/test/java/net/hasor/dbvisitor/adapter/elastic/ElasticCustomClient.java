package net.hasor.dbvisitor.adapter.elastic;

import java.util.Map;
import org.elasticsearch.client.RestClient;
import org.powermock.api.mockito.PowerMockito;

public class ElasticCustomClient implements CustomElastic {

    @Override
    public RestClient createElasticClient(String jdbcUrl, Map<String, String> props) {
        return PowerMockito.mock(RestClient.class);
    }
}
