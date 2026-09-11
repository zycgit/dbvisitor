/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.keygen;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeySequenceEmptyNameUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeySequenceNoAnnotationUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeySequenceUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NxnContract
public abstract class SequenceKeyContractTest extends KeyGenerationSupport {
    @Test
    @Capability(CapabilityId.KEYGEN_SEQUENCE_METADATA)
    public void keygenSequenceMetadata_shouldRegisterSequenceHolderWhenKeySeqExists() {
        requiresNxnFeature(FeatureId.SEQUENCE);
        MappingRegistry registry = new MappingRegistry(null, Options.of().dialect(sequenceDialect()));
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

    @Test
    @Capability(CapabilityId.KEYGEN_SEQUENCE_EMPTY_NAME)
    public void keygenSequenceMetadata_shouldRejectEmptySequenceName() {
        requiresNxnFeature(FeatureId.SEQUENCE);
        MappingRegistry registry = new MappingRegistry(null, Options.of().dialect(sequenceDialect()));
        try {
            registry.loadEntityToSpace(KeySequenceEmptyNameUser.class);
            fail("Expected empty sequence name to be rejected");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @Capability(CapabilityId.KEYGEN_SEQUENCE)
    public void keygenSequence_shouldUseDatabaseSequenceWhenSupported() throws SQLException {
        requiresNxnFeature(FeatureId.SEQUENCE);
        resetSequence("seq_key_test_seq", 2000);

        KeySequenceUser first = sequenceUser("Seq User 1", 20);
        KeySequenceUser second = sequenceUser("Seq User 2", 21);

        assertNull(first.getId());
        LambdaTemplate seqLambda = new LambdaTemplate(dataSource, Options.of().dialect(sequenceDialect()));
        assertEquals(1, seqLambda.insert(KeySequenceUser.class).applyEntity(first).executeSumResult());
        assertEquals(1, seqLambda.insert(KeySequenceUser.class).applyEntity(second).executeSumResult());

        assertEquals(Integer.valueOf(2000), first.getId());
        assertEquals(Integer.valueOf(2001), second.getId());
    }
}
