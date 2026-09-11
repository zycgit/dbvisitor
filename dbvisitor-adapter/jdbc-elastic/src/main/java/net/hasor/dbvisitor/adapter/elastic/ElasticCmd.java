/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
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
