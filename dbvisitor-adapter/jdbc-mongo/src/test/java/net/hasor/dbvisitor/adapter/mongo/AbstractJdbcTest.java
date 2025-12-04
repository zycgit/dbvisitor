package net.hasor.dbvisitor.adapter.mongo;

import java.lang.reflect.InvocationHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoCursor;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.powermock.api.mockito.PowerMockito;

public class AbstractJdbcTest {

    public Connection redisConnection() throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(MongoKeys.CUSTOM_MONGO, MongoCustomJedis.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:mongo://xxxxxx", prop);
    }

    public Connection redisConnection(String defaultDB) throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(MongoKeys.CUSTOM_MONGO, MongoCustomJedis.class.getName());
        prop.setProperty(MongoKeys.DATABASE, defaultDB);
        return new JdbcDriver().connect("jdbc:dbvisitor:mongo://xxxxxx", prop);
    }

    public InvocationHandler createInvocationHandler(String methodName, TestInvocationHandler handler) {
        return (proxy, method, args) -> {
            if (method.getName().equals(methodName)) {
                return handler.invoke(method.getName(), args);
            }
            return null;
        };
    }

    public InvocationHandler createInvocationHandler(String[] methodName, TestInvocationHandler handler) {
        return (proxy, method, args) -> {
            for (String c : methodName) {
                if (method.getName().equals(c)) {
                    return handler.invoke(method.getName(), args);
                }
            }
            return null;
        };
    }

    protected <T> ListDatabasesIterable<T> mockListDatabasesIterable(List<T> list) {
        ListDatabasesIterable<T> iterable = PowerMockito.mock(ListDatabasesIterable.class);
        MongoCursor<T> cursor = PowerMockito.mock(MongoCursor.class);
        Iterator<T> iterator = list.iterator();

        PowerMockito.when(iterable.iterator()).thenReturn(cursor);
        PowerMockito.when(cursor.hasNext()).thenAnswer(inv -> iterator.hasNext());
        PowerMockito.when(cursor.next()).thenAnswer(inv -> {
            T val = iterator.next();
            return val;
        });

        return iterable;
    }

    protected <T> com.mongodb.client.MongoIterable<T> mockMongoIterable(List<T> list) {
        com.mongodb.client.MongoIterable<T> iterable = PowerMockito.mock(com.mongodb.client.MongoIterable.class);
        MongoCursor<T> cursor = PowerMockito.mock(MongoCursor.class);
        Iterator<T> iterator = list.iterator();

        PowerMockito.when(iterable.iterator()).thenReturn(cursor);
        PowerMockito.when(cursor.hasNext()).thenAnswer(inv -> iterator.hasNext());
        PowerMockito.when(cursor.next()).thenAnswer(inv -> {
            T val = iterator.next();
            return val;
        });

        return iterable;
    }

    protected com.mongodb.client.ListCollectionNamesIterable mockListCollectionNamesIterable(List<String> list) {
        com.mongodb.client.ListCollectionNamesIterable iterable = PowerMockito.mock(com.mongodb.client.ListCollectionNamesIterable.class);
        MongoCursor<String> cursor = PowerMockito.mock(MongoCursor.class);
        Iterator<String> iterator = list.iterator();

        PowerMockito.when(iterable.iterator()).thenReturn(cursor);
        PowerMockito.when(cursor.hasNext()).thenAnswer(inv -> iterator.hasNext());
        PowerMockito.when(cursor.next()).thenAnswer(inv -> {
            String val = iterator.next();
            return val;
        });

        return iterable;
    }

    protected <T> com.mongodb.client.ListCollectionsIterable<T> mockListCollectionsIterable(List<T> list) {
        com.mongodb.client.ListCollectionsIterable<T> iterable = PowerMockito.mock(com.mongodb.client.ListCollectionsIterable.class);
        MongoCursor<T> cursor = PowerMockito.mock(MongoCursor.class);
        Iterator<T> iterator = list.iterator();

        PowerMockito.when(iterable.iterator()).thenReturn(cursor);
        PowerMockito.when(cursor.hasNext()).thenAnswer(inv -> iterator.hasNext());
        PowerMockito.when(cursor.next()).thenAnswer(inv -> {
            T val = iterator.next();
            return val;
        });

        return iterable;
    }
}
