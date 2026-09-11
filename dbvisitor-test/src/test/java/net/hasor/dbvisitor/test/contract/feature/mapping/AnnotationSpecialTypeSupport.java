/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.material.model.types.SpecialArrayTypeEntity;
import net.hasor.dbvisitor.test.contract.material.model.types.SpecialJsonTypeEntity;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertNotNull;

public abstract class AnnotationSpecialTypeSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 950000;
    }

    protected void insert(SpecialJsonTypeEntity entity) throws SQLException {
        this.lambdaTemplate.insert(SpecialJsonTypeEntity.class).applyEntity(entity).executeSumResult();
    }

    protected void insert(SpecialArrayTypeEntity entity) throws SQLException {
        this.lambdaTemplate.insert(SpecialArrayTypeEntity.class).applyEntity(entity).executeSumResult();
    }

    protected SpecialJsonTypeEntity queryJson(int id) throws SQLException {
        SpecialJsonTypeEntity loaded = this.lambdaTemplate.query(SpecialJsonTypeEntity.class) //
                .eq(SpecialJsonTypeEntity::getId, id) //
                .queryForObject();
        assertNotNull(loaded);
        return loaded;
    }

    protected SpecialArrayTypeEntity queryArray(int id) throws SQLException {
        SpecialArrayTypeEntity loaded = this.lambdaTemplate.query(SpecialArrayTypeEntity.class) //
                .eq(SpecialArrayTypeEntity::getId, id) //
                .queryForObject();
        assertNotNull(loaded);
        return loaded;
    }
}
