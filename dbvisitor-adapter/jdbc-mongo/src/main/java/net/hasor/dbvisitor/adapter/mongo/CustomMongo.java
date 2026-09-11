/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.mongo;
import java.util.Map;
import com.mongodb.client.MongoClient;

public interface CustomMongo {
    /** return MongoClient */
    MongoClient createMongoClient(String jdbcUrl, Map<String, String> props);
}
