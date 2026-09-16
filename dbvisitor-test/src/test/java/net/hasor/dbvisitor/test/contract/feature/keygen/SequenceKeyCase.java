/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.keygen;

import java.sql.SQLException;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeySequenceUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class SequenceKeyCase extends KeyGenerationSupport {
    // 能力归属：对象映射 / 主键策略 / 序列主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_SEQUENCE, column = "mapping-keys/key-generators/strategies")
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
        KeySequenceUser storedFirst = seqLambda.query(KeySequenceUser.class)//
                .eq(KeySequenceUser::getId, first.getId())//
                .queryForObject();
        KeySequenceUser storedSecond = seqLambda.query(KeySequenceUser.class)//
                .eq(KeySequenceUser::getId, second.getId())//
                .queryForObject();
        assertNotNull(storedFirst);
        assertNotNull(storedSecond);
        assertEquals("Seq User 1", storedFirst.getName());
        assertEquals(Integer.valueOf(20), storedFirst.getAge());
        assertEquals("Seq User 2", storedSecond.getName());
        assertEquals(Integer.valueOf(21), storedSecond.getAge());
    }
}
