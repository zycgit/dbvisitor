/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperSelectKeyCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Elastic7AnnotationMapperSelectKeyTest extends AnnotationMapperSelectKeyCase {
    private final Elastic7KeyMapperFixture            fixture = new Elastic7KeyMapperFixture();
    private       Elastic7KeyMapperFixture.NativeKeys keys;

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
        fixture.prepareReservation("AttrSelectKeyBefore");
        fixture.prepareReservation("AttrSelectKeyFull");
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        this.jdbcTemplate = fixture.open(profile().env());
        this.keys = fixture.session().createMapper(Elastic7KeyMapperFixture.NativeKeys.class);
    }

    @Override
    protected boolean numericGeneratedKeys() {
        return false;
    }

    @Override
    protected Object keyRecord(Object id, String name, int age, String email) {
        Elastic7KeyMapperFixture.Document record = new Elastic7KeyMapperFixture.Document();
        record.setId((String) id);
        record.setName(name);
        record.setAge(age);
        record.setEmail(email);
        return record;
    }

    @Override
    protected Object keyValue(Object record) {
        return ((Elastic7KeyMapperFixture.Document) record).getId();
    }

    @Override
    protected Object explicitKey() {
        return "explicit-mapper-key";
    }

    @Override
    protected int writeKeyRecord(KeyWrite operation, Object record) {
        Elastic7KeyMapperFixture.Document document = (Elastic7KeyMapperFixture.Document) record;
        return switch (operation) {
            case GENERATED -> keys.generated(document);
            case COLUMN -> keys.column(document);
            case EXPLICIT -> keys.explicit(document);
            case BEFORE -> keys.before(document);
            case AFTER -> keys.after(document);
            case OPTIONS -> keys.options(document);
            default -> throw new UnsupportedOperationException("Elasticsearch insert does not return a query ResultSet");
        };
    }

    @Override
    protected String readKeyName(Object id) {
        return keys.name((String) id);
    }

    @Override
    protected void assertGeneratedKey(Object id) {
        assertTrue(id instanceof String);
        assertFalse(((String) id).isEmpty());
    }

    @After
    public void closeKeysFixture() throws Exception {
        fixture.close();
    }
}
