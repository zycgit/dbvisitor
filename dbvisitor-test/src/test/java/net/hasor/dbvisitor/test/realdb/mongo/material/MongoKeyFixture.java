/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo.material;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

public final class MongoKeyFixture implements AutoCloseable {
    private final MongoMapperFixture target   = new MongoMapperFixture();
    private final MongoMapperFixture reserved = new MongoMapperFixture();

    public JdbcTemplate open() throws SQLException {
        this.reserved.open();
        return this.target.open();
    }

    public Session session(Configuration configuration) throws Exception {
        open();
        configuration.addMacro("mongoKeySource", this.reserved.source());
        return this.target.session(configuration);
    }

    public void reserve(String name) throws SQLException {
        this.reserved.open().executeUpdate(this.reserved.source() + ".insert({name: ?})", new Object[] { name });
    }

    @Override
    public void close() throws Exception {
        try {
            this.target.close();
        } finally {
            this.reserved.close();
        }
    }
}
