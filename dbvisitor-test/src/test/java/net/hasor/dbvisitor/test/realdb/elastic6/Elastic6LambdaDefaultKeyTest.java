/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.SQLException;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaDefaultKeyCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic6LambdaDefaultKeyTest extends LambdaDefaultKeyCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        jdbcTemplate = fixture.open(profile().env());
        fixture.registry().loadEntityAsTable(Document.class, fixture.index());
        lambdaTemplate = fixture.lambdaTemplate();
    }

    @Override
    protected Class<?> defaultKeyEntityType() {
        return Document.class;
    }

    @Override
    protected Object newDefaultKeyEntity() {
        Document document = new Document();
        document.setName("NXN-Lambda-Entity-Auto-Id");
        return document;
    }

    @Override
    protected Object defaultKeyValue(Object entity) {
        return ((Document) entity).getId();
    }

    public static class Document {
        @Column(value = "_ID", primary = true)
        private String id;
        private String name;

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
