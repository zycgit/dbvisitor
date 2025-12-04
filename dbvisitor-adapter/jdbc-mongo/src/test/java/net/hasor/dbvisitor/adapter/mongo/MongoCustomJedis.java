package net.hasor.dbvisitor.adapter.mongo;

import java.lang.reflect.InvocationHandler;
import java.util.Map;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

public class MongoCustomJedis implements CustomMongo {

    @Override
    public MongoClient createMongoClient(String jdbcUrl, Map<String, String> props) {
        MongoClient root = PowerMockito.mock(MongoClient.class);

        PowerMockito.when(root.getDatabase(Mockito.anyString())).thenAnswer(invocation -> {
            String dbName = invocation.getArgument(0);

            MongoDatabase md = PowerMockito.mock(MongoDatabase.class);
            PowerMockito.when(md.getName()).thenAnswer(im -> dbName);

            PowerMockito.when(md.getCollection(Mockito.anyString())).thenAnswer(im -> {
                String collectionName = im.getArgument(0);
                return PowerMockito.mock(MongoCollection.class);
            });

            return md;
        });

        try {
            InvocationHandler handler = new MongoCommandInterceptor();
            MongoClientProxy proxy = new MongoClientProxy(handler);
            proxy.updateTarget(root);
            return proxy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
