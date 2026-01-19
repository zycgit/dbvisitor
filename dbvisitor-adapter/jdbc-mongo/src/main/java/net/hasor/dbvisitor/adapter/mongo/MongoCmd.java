/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.adapter.mongo;
import java.io.IOException;
import java.sql.SQLException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.hasor.cobble.StringUtils;
import org.bson.Document;

public class MongoCmd implements AutoCloseable {
    private final MongoClient   client;
    private       String        currentCatalog;
    private       MongoDatabase currentDatabase;

    MongoCmd(MongoClient client, String defaultDB) throws SQLException {
        this.client = client;
        if (StringUtils.isNotBlank(defaultDB)) {
            this.setCatalog(defaultDB);
        }
    }

    MongoClient getClient() {
        return this.client;
    }

    public String getCatalog() {
        return this.currentCatalog;
    }

    public void setCatalog(String catalog) {
        this.currentCatalog = catalog;
        if (StringUtils.isBlank(catalog)) {
            this.currentDatabase = null;
        } else {
            this.currentDatabase = this.client.getDatabase(catalog);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.client.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    //

    public MongoDatabase getMongoDB(String database) {
        if (StringUtils.equals(database, this.currentCatalog)) {
            return this.currentDatabase;
        } else {
            return this.client.getDatabase(database);
        }
    }

    public MongoCollection<Document> getMongoSchema(String database, String schema) {
        MongoDatabase db = getMongoDB(database);
        return db.getCollection(schema);
    }
}

