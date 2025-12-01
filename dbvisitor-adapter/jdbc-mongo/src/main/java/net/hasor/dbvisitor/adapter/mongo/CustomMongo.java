package net.hasor.dbvisitor.adapter.mongo;
import java.util.Map;
import com.mongodb.MongoClient;

public interface CustomMongo {
    /** return MongoClient */
    MongoClient createMongoClient(String jdbcUrl, Map<String, String> props);
}
