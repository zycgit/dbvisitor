package net.hasor.dbvisitor.adapter.mongo;
import java.util.Map;
import com.mongodb.client.MongoClient;

public interface CustomMongo {
    /** return MongoClient */
    MongoClient createMongoClient(String jdbcUrl, Map<String, String> props);
}
