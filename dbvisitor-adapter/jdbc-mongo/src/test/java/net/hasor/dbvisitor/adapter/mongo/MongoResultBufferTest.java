package net.hasor.dbvisitor.adapter.mongo;

import java.io.File;
import java.io.IOException;
import org.bson.Document;
import org.junit.Test;

public class MongoResultBufferTest {
    @Test
    public void testBufferInMemory() throws IOException, java.sql.SQLException {
        try (MongoResultBuffer buffer = new MongoResultBuffer(1024 * 1024, 20 * 1024 * 1024, new File(System.getProperty("java.io.tmpdir")))) {
            for (int i = 0; i < 10; i++) {
                buffer.add(new Document("key", "value" + i));
            }
            buffer.finish();

            int count = 0;
            for (Document doc : buffer) {
                assert doc.getString("key").equals("value" + count);
                count++;
            }
            assert count == 10;
            assert !buffer.isSpilled();
        }
    }

    @Test
    public void testBufferSpillToDisk() throws IOException, java.sql.SQLException {
        try (MongoResultBuffer buffer = new MongoResultBuffer(100, 20 * 1024 * 1024, new File(System.getProperty("java.io.tmpdir")))) {
            // Add enough data to exceed 100 bytes
            for (int i = 0; i < 100; i++) {
                buffer.add(new Document("key", "value" + i + " - some long string to take up space"));
            }
            buffer.finish();

            assert buffer.isSpilled();

            int count = 0;
            for (Document doc : buffer) {
                assert doc.getString("key").startsWith("value" + count);
                count++;
            }
            assert count == 100;
        }
    }
}
