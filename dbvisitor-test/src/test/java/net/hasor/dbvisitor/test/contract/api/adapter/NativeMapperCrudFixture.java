/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.adapter;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterCommand;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationTestMapper;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.NativeCrudMapper;

/** Private native collection shared by the annotation and XML CRUD fixtures. */
public final class NativeMapperCrudFixture implements AutoCloseable {
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

    public AnnotationTestMapper createMapper(Configuration configuration) throws Exception {
        configure(configuration);
        this.session = configuration.newSession(this.jdbc.getConnection());
        return this.session.createMapper(NativeCrudMapper.class);
    }

    public Session createXmlSession(Configuration configuration) throws Exception {
        configure(configuration);
        configuration.loadMapper("/mapper/NativeCrudMapper.xml");
        this.session = configuration.newSession(this.jdbc.getConnection());
        return this.session;
    }

    public void seedXmlUsers(int baseId) throws SQLException {
        for (int i = 1; i <= 5; i++) {
            this.jdbc.executeUpdate(this.collection.command(JdbcParameterCommand.INSERT_POSITIONAL),
                    new Object[] { baseId + i, "XmlCrud" + i, 20 + i, "crud" + i + "@test.com", new Date() });
        }
    }

    private void configure(Configuration configuration) {
        String fields = "\"id\": #{id}, \"name\": #{name}, \"age\": #{age}, \"email\": #{email}";
        configuration.addMacro("nxnCrudInsertBean", insert(fields + ", \"create_time\": #{createTime}"));
        configuration.addMacro("nxnCrudInsertParams", insert(fields));
        // Match the epoch-millisecond representation used by the Date parameters in the seed rows.
        configuration.addMacro("nxnCrudInsertXml", insert(fields + ", \"create_time\": " + System.currentTimeMillis()));
        if (this.mongo) {
            configureMongo(configuration);
        } else {
            configureElastic(configuration);
        }
    }

    private String insert(String fields) {
        String document = "{" + fields + "}";
        return this.mongo ? "test." + this.collection.table() + ".insert(" + document + ")"
                : "POST /" + this.collection.table() + "/_doc " + document;
    }

    private void configureMongo(Configuration configuration) {
        String source = "test." + this.collection.table();
        configuration.addMacro("nxnCrudSelect", source + ".find({id: #{id}})");
        configuration.addMacro("nxnCrudDelete", source + ".remove({id: #{id}})");
        configuration.addMacro("nxnCrudUpdateAge", source + ".update({id: #{id}}, {$set: {age: #{age}}})");
        configuration.addMacro("nxnCrudUpdateInfo", source + ".update({id: #{id}}, {$set: {name: #{name}, age: #{age}}})");
        configuration.addMacro("nxnCrudUpdateEmail", source + ".update({id: #{id}}, {$set: {email: #{email}}})");
    }

    private void configureElastic(Configuration configuration) {
        String source = "POST /" + this.collection.table();
        configuration.addMacro("nxnCrudSelect", source + "/_search {\"query\": {\"term\": {\"id\": #{id}}}}");
        configuration.addMacro("nxnCrudDelete", source + "/_delete_by_query {\"query\": {\"term\": {\"id\": #{id}}}}");
        configuration.addMacro("nxnCrudUpdateAge", updateElastic("ctx._source.age = params.age", "\"age\": #{age}"));
        configuration.addMacro("nxnCrudUpdateInfo", updateElastic("ctx._source.name = params.name; ctx._source.age = params.age",
                "\"name\": #{name}, \"age\": #{age}"));
        configuration.addMacro("nxnCrudUpdateEmail", updateElastic("ctx._source.email = params.email", "\"email\": #{email}"));
    }

    private String updateElastic(String script, String parameters) {
        return "POST /" + this.collection.table() + "/_update_by_query " + """
                {"query": {"term": {"id": #{id}}},
                 "script": {"source": "%s", "params": {%s}}}
                """.formatted(script, parameters);
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
