/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo.material;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeDocumentParameterFixture;

/** Isolated Mongo collection shared by native Mapper scenario fixtures. */
public final class MongoMapperFixture implements AutoCloseable {
    private final NativeDocumentParameterFixture collection = new NativeDocumentParameterFixture();
    private JdbcTemplate jdbc;
    private Session session;

    public JdbcTemplate open() throws SQLException {
        if (this.jdbc == null) {
            this.jdbc = this.collection.open("mongo");
        }
        return this.jdbc;
    }

    public String source() {
        return "test." + this.collection.table();
    }

    public String table() {
        return this.collection.table();
    }

    public Session session(Configuration configuration) throws Exception {
        configuration.addMacro("mongoSource", source());
        this.session = configuration.newSession(open().getConnection());
        return this.session;
    }

    public void seed(int baseId, String prefix, String emailPrefix) throws SQLException {
        for (int i = 1; i <= 10; i++) {
            this.jdbc.executeUpdate(source() + ".insert({id: ?, name: ?, age: ?, email: ?, create_time: ?})",
                    new Object[] { baseId + i, prefix + i, 20 + i, emailPrefix + i + "@nxn.test", new Date() });
        }
    }

    public void uniqueIds() throws SQLException {
        this.jdbc.execute(source() + ".createIndex({id: 1}, {unique: true})");
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
