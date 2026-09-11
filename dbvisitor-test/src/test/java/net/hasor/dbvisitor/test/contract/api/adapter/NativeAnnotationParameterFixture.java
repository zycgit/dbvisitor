/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.adapter;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.NativeParameterBindingMapper;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ParameterBindingMapper;

/** Isolated collection and native command material for the common annotated-parameter assertions. */
public final class NativeAnnotationParameterFixture implements AutoCloseable {
    private final NativeDocumentParameterFixture collection = new NativeDocumentParameterFixture();
    private JdbcTemplate jdbc;
    private Session session;
    private boolean mongo;

    public JdbcTemplate open(String environment) throws SQLException {
        if (this.jdbc == null) {
            this.jdbc = this.collection.open(environment);
            this.mongo = "mongo".equals(environment);
        }
        return this.jdbc;
    }

    public ParameterBindingMapper createMapper(Configuration configuration) throws Exception {
        configuration.addMacro("nxnInsertPosition", insert("\"id\": ?, \"name\": ?, \"age\": ?"));
        configuration.addMacro("nxnInsertNamed", insert("\"id\": #{id}, \"name\": #{name}, \"age\": #{age}, \"email\": #{email}"));
        configuration.addMacro("nxnInsertBean", insert("""
                "id": #{id}, "name": #{name}, "age": #{age}, "email": #{email}, "create_time": #{createTime}
                """));
        configuration.addMacro("nxnInsertMixed", insert("""
                "id": #{user.id}, "name": #{user.name}, "age": #{user.age}, "email": #{email}
                """));
        configuration.addMacro("nxnInsertReuse", insert("\"id\": #{id}, \"name\": #{name}, \"age\": 25, \"email\": #{name}"));
        if (this.mongo) {
            configureMongo(configuration);
        } else {
            configureElastic(configuration);
        }
        this.session = configuration.newSession(this.jdbc.getConnection());
        return this.session.createMapper(NativeParameterBindingMapper.class);
    }

    private String insert(String fields) {
        String document = "{" + fields + "}";
        if (this.mongo) {
            return "test." + this.collection.table() + ".insert(" + document + ")";
        }
        // The realdb connection already sets indexRefresh=true.
        return "POST /" + this.collection.table() + "/_doc " + document;
    }

    private void configureMongo(Configuration configuration) {
        String source = "test." + this.collection.table();
        // The update value precedes the filter so the two '?' keep the declared (age, id) order.
        configuration.addMacro("nxnUpdatePosition", source + """
                .bulkWrite([{updateOne: {update: {$set: {age: ?}}, filter: {id: ?}}}])
                """);
        configuration.addMacro("nxnSelectId", source + ".find({id: #{id}})");
        configuration.addMacro("nxnSelectRange", source + ".find({age: {$gte: #{minAge}, $lte: #{maxAge}}})");
    }

    private void configureElastic(Configuration configuration) {
        String source = "POST /" + this.collection.table();
        configuration.addMacro("nxnUpdatePosition", source + """
                /_update_by_query {
                  "script": {"source": "ctx._source.age = params.age", "params": {"age": ?}},
                  "query": {"term": {"id": ?}}
                }
                """);
        configuration.addMacro("nxnSelectId", source + "/_search {\"query\": {\"term\": {\"id\": #{id}}}}");
        configuration.addMacro("nxnSelectRange", source + """
                /_search {"query": {"range": {"age": {"gte": #{minAge}, "lte": #{maxAge}}}}}
                """);
    }

    @Override
    public void close() throws Exception {
        try {
            this.collection.close();
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
}
