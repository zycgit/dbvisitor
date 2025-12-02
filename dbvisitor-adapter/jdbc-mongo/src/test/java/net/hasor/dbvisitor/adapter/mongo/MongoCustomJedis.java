package net.hasor.dbvisitor.adapter.mongo;

import java.util.Map;
import com.mongodb.MongoClient;
import org.powermock.api.mockito.PowerMockito;

public class MongoCustomJedis implements CustomMongo {

    @Override
    public MongoClient createMongoClient(String jdbcUrl, Map<String, String> props) {
        return PowerMockito.mock(MongoClient.class);
    }
}
