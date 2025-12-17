package net.hasor.dbvisitor.adapter.elastic;
import java.io.IOException;
import org.elasticsearch.client.RestClient;

public class ElasticCmd implements AutoCloseable {
    private final RestClient client;

    ElasticCmd(RestClient client) {
        this.client = client;
    }

    RestClient getClient() {
        return this.client;
    }

    @Override
    public void close() throws IOException {
        try {
            this.client.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}

