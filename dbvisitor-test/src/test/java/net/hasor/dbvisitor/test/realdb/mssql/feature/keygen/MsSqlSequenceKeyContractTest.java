/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mssql.feature.keygen;

import java.sql.SQLException;
import java.util.Arrays;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.KeySeq;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.test.contract.feature.keygen.SequenceKeyContractTest;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeySequenceUser;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MsSqlSequenceKeyContractTest extends SequenceKeyContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }

    @Test
    public void sequence_shouldAssignAndPersistEveryEntity() throws SQLException {
        resetSequence("seq_key_test_seq", 3000);
        KeySequenceUser first = sequenceUser("Sequence first", 20);
        KeySequenceUser second = sequenceUser("Sequence second", 21);
        LambdaTemplate template = new LambdaTemplate(dataSource, Options.of().dialect(sequenceDialect()));

        assertEquals(2, template.insert(KeySequenceUser.class).applyEntity(Arrays.asList(first, second)).executeSumResult());
        assertEquals(Integer.valueOf(3000), first.getId());
        assertEquals(Integer.valueOf(3001), second.getId());
        assertEquals(first.getName(), jdbcTemplate.queryForObject("SELECT name FROM user_info WHERE id = ?", new Object[] { first.getId() }, String.class));
        assertEquals(second.getName(), jdbcTemplate.queryForObject("SELECT name FROM user_info WHERE id = ?", new Object[] { second.getId() }, String.class));
    }

    @Test
    public void sequence_shouldUseExplicitSchema() throws SQLException {
        resetSequence("seq_key_test_seq", 4000);
        String schema = jdbcTemplate.queryForObject("SELECT SCHEMA_NAME()", String.class);
        LambdaTemplate template = new LambdaTemplate(dataSource, Options.of().dialect(sequenceDialect()).schema(schema));
        KeySequenceUser user = sequenceUser("Schema sequence", 22);

        assertEquals(1, template.insert(KeySequenceUser.class).applyEntity(user).executeSumResult());
        assertEquals(Integer.valueOf(4000), user.getId());
        assertEquals(user.getName(), jdbcTemplate.queryForObject("SELECT name FROM user_info WHERE id = ?", new Object[] { user.getId() }, String.class));
    }

    @Test
    public void sequence_shouldPreserveQuotedNames() throws SQLException {
        dropTableIfExists("[Sequence User]");
        resetSequence("[User Sequence]", 5000);
        try {
            jdbcTemplate.executeUpdate("CREATE TABLE [Sequence User] ([id] BIGINT PRIMARY KEY, [name] VARCHAR(100))");
            String schema = jdbcTemplate.queryForObject("SELECT SCHEMA_NAME()", String.class);
            LambdaTemplate template = new LambdaTemplate(dataSource, Options.of().dialect(sequenceDialect()).schema(schema));
            QuotedSequenceUser user = new QuotedSequenceUser();
            user.setName("Quoted sequence");

            assertEquals(1, template.insert(QuotedSequenceUser.class).applyEntity(user).executeSumResult());
            assertEquals(Long.valueOf(5000), user.getId());
            assertEquals(user.getName(), jdbcTemplate.queryForObject("SELECT [name] FROM [Sequence User] WHERE [id] = ?", new Object[] { user.getId() }, String.class));
        } finally {
            dropTableIfExists("[Sequence User]");
            dropSequenceIfExists("[User Sequence]");
        }
    }

    @Test
    public void sequence_shouldUseCatalogWithDefaultSchema() throws SQLException {
        resetSequence("seq_key_test_seq", 6000);
        String catalog = jdbcTemplate.queryForObject("SELECT DB_NAME()", String.class);
        LambdaTemplate template = new LambdaTemplate(dataSource, Options.of().dialect(sequenceDialect()).catalog(catalog));
        KeySequenceUser user = sequenceUser("Catalog sequence", 23);

        assertEquals(1, template.insert(KeySequenceUser.class).applyEntity(user).executeSumResult());
        assertEquals(Integer.valueOf(6000), user.getId());
        assertEquals(user.getName(), jdbcTemplate.queryForObject("SELECT name FROM user_info WHERE id = ?", new Object[] { user.getId() }, String.class));
    }

    @Table(value = "Sequence User", useDelimited = true)
    public static class QuotedSequenceUser {
        @Column(primary = true, keyType = KeyType.Sequence)
        @KeySeq("User Sequence")
        private Long id;
        private String name;

        public Long getId() {
            return this.id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
