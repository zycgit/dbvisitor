package net.hasor.dbvisitor.adapter.elastic;
import java.util.Map;
import org.elasticsearch.client.RestClient;

public interface CustomElastic {
    /** return RestClient */
    RestClient createElasticClient(String jdbcUrl, Map<String, String> props);
}
