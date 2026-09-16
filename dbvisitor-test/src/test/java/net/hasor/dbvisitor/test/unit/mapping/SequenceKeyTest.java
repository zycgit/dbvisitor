/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.mapping;

import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.provider.*;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeySequenceNoAnnotationUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeySequenceUser;
import org.junit.Test;
import static org.junit.Assert.*;

public class SequenceKeyTest {
    @Test
    public void keygenSequenceMetadata_shouldRegisterSequenceHolderWhenKeySeqExists() {
        SqlDialect[] dialects = { H2Dialect.DEFAULT, PostgreSqlDialect.DEFAULT, OracleDialect.DEFAULT, Db2Dialect.DEFAULT, SqlServerDialect.DEFAULT };
        for (SqlDialect dialect : dialects) {
            MappingRegistry registry = new MappingRegistry(null, Options.of().dialect(dialect));
            registry.loadEntityToSpace(KeySequenceUser.class);

            TableMapping<?> mapping = registry.findByEntity(KeySequenceUser.class);
            ColumnMapping idColumn = mapping.getPropertyByName("id");

            assertTrue(idColumn.isPrimaryKey());
            assertEquals(KeyType.Sequence, idColumn.getKeyType());
            assertNotNull(idColumn.getKeySeqHolder());

            registry.loadEntityToSpace(KeySequenceNoAnnotationUser.class);
            TableMapping<?> missing = registry.findByEntity(KeySequenceNoAnnotationUser.class);
            assertNull(missing.getPropertyByName("id").getKeySeqHolder());
        }
    }
}
