/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.feature.naming.IdentifierQuotingCase;
import net.hasor.dbvisitor.test.contract.material.model.naming.*;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.Before;
import org.junit.After;

public class Elastic7IdentifierQuotingTest extends IdentifierQuotingCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();
    private boolean keywordIndexCreated;

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        jdbcTemplate = fixture.open(profile().env());
        for (Class<?> model : new Class<?>[] {AllNamingOptionsUser.class, DelimitedUser.class, CaseTestUpperCI.class,
                KeywordColumnEntity.class, KeywordColumnNoDelimitedEntity.class}) {
            fixture.registry().loadEntityAsTable(model, fixture.index());
        }
        lambdaTemplate = fixture.lambdaTemplate();
    }

    @Override
    protected String mappedTableName(String table) {
        return "order".equals(table) ? "order" : fixture.index();
    }

    @Override
    protected void ensureKeywordColumnTable() {
        // Elasticsearch permits these field names without SQL keyword escaping.
    }

    @Override
    protected void ensureKeywordTable() throws SQLException {
        if (keywordIndexCreated) {
            return;
        }
        boolean exists = true;
        try {
            jdbcTemplate.execute("GET /order");
        } catch (SQLException error) {
            if (!"E404".equals(error.getSQLState())) {
                throw error;
            }
            exists = false;
        }
        if (exists) {
            throw new SQLException("Keyword-table fixture conflict: index 'order' already exists; it will not be modified or deleted");
        }
        String fields = "{\"properties\": {\"id\": {\"type\": \"integer\"}, \"name\": {\"type\": \"keyword\"}, \"memo\": {\"type\": \"keyword\"}}}";
        String mapping = "es6".equals(profile().env()) ? "{\"_doc\": " + fields + "}" : fields;
        jdbcTemplate.execute("PUT /order {\"mappings\": " + mapping + "}");
        keywordIndexCreated = true;
        fixture.registry().loadEntityAsTable(KeywordTableEntity.class, "order");
        fixture.registry().loadEntityAsTable(KeywordTableNoDelimitedEntity.class, "order");
    }

    @After
    public void closeFixture() throws SQLException {
        SQLException failure = null;
        if (keywordIndexCreated) {
            try {
                jdbcTemplate.execute("DELETE /order");
            } catch (SQLException error) {
                failure = error;
            }
        }
        try {
            fixture.close();
        } catch (SQLException error) {
            if (failure == null) {
                failure = error;
            } else {
                failure.addSuppressed(error);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }
}
