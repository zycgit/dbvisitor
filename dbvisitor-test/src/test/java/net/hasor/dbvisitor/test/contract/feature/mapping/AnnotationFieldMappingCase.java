/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ColumnMappedUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ColumnNameUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ColumnValueUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class AnnotationFieldMappingCase extends AnnotationMappingPolicySupport {
    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_PRIMARY_KEY_CONDITION_UPDATE)
    public void annotationMapping_shouldUsePrimaryKeyPropertyInLambdaConditions() throws SQLException {
        int firstId = baseId() + 101;
        int secondId = baseId() + 102;
        insertRaw(firstId, "PolicyPkUpdate1", 25, "pk1@nxn.test");
        insertRaw(secondId, "PolicyPkUpdate2", 30, "pk2@nxn.test");

        int updated = this.lambdaTemplate.update(UserInfo.class)//
                .eq(UserInfo::getId, firstId)//
                .updateTo(UserInfo::getName, "PolicyPkUpdated")//
                .doUpdate();

        UserInfo first = queryRaw(firstId);
        UserInfo second = queryRaw(secondId);

        assertEquals(1, updated);
        assertEquals("PolicyPkUpdated", first.getName());
        assertEquals("PolicyPkUpdate2", second.getName());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_COLUMN_FIELD_CRUD)
    public void annotationMapping_shouldRoundTripFieldLevelColumnMappings() throws SQLException {
        int id = baseId() + 111;
        deleteRaw(id);

        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(ColumnMappedUser.class);
        TableMapping<?> mapping = registry.findByEntity(ColumnMappedUser.class);
        assertNotNull(mapping);
        assertEquals("user_info", mapping.getTable());
        assertEquals("name", mapping.getPropertyByName("userName").getColumn());
        assertEquals("email", mapping.getPropertyByName("mailAddr").getColumn());
        assertEquals("create_time", mapping.getPropertyByName("createTime").getColumn());
        assertTrue(mapping.getPropertyByName("id").isPrimaryKey());

        ColumnMappedUser user = new ColumnMappedUser();
        user.setId(id);
        user.setUserName("PolicyFieldColumn");
        user.setAge(25);
        user.setMailAddr("field-column@nxn.test");
        user.setCreateTime(new Date());

        this.lambdaTemplate.insert(ColumnMappedUser.class).applyEntity(user).executeSumResult();
        ColumnMappedUser loaded = this.lambdaTemplate.query(ColumnMappedUser.class)//
                .eq(ColumnMappedUser::getId, id)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("PolicyFieldColumn", loaded.getUserName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
        assertEquals("field-column@nxn.test", loaded.getMailAddr());
        assertNotNull(loaded.getCreateTime());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_COLUMN_NAME_VALUE_EQUIVALENCE)
    public void annotationMapping_shouldTreatColumnNameAndValueAsEquivalent() throws SQLException {
        int nameId = baseId() + 112;
        int valueId = baseId() + 113;
        deleteRaw(nameId);
        deleteRaw(valueId);

        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(ColumnNameUser.class, "", "nameUser");
        registry.loadEntityToSpace(ColumnValueUser.class, "", "valueUser");

        ColumnMapping nameColumn = registry.findBySpace("", "nameUser").getPropertyByName("userName");
        ColumnMapping valueColumn = registry.findBySpace("", "valueUser").getPropertyByName("userName");
        assertNotNull(nameColumn);
        assertNotNull(valueColumn);
        assertEquals("name", nameColumn.getColumn());
        assertEquals(nameColumn.getColumn(), valueColumn.getColumn());

        ColumnNameUser nameUser = new ColumnNameUser();
        nameUser.setId(nameId);
        nameUser.setUserName("PolicyNameAttr");
        nameUser.setAge(26);
        nameUser.setEmail("name-attr@nxn.test");
        nameUser.setCreateTime(new Date());
        this.lambdaTemplate.insert(ColumnNameUser.class).applyEntity(nameUser).executeSumResult();

        ColumnValueUser valueUser = new ColumnValueUser();
        valueUser.setId(valueId);
        valueUser.setUserName("PolicyValueAttr");
        valueUser.setAge(27);
        valueUser.setEmail("value-attr@nxn.test");
        valueUser.setCreateTime(new Date());
        this.lambdaTemplate.insert(ColumnValueUser.class).applyEntity(valueUser).executeSumResult();

        assertEquals("PolicyNameAttr", queryRaw(nameId).getName());
        assertEquals("PolicyValueAttr", queryRaw(valueId).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_MAP_QUERY_RESULT)
    public void annotationMapping_shouldExposeMapQueryResults() throws SQLException {
        int id = baseId() + 114;
        insertRaw(id, "PolicyMapResult", 30, "map-result@nxn.test");

        Map<String, Object> result = this.lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .queryForMap();

        assertNotNull(result);
        assertEquals("PolicyMapResult", value(result, "name"));
        assertEquals("map-result@nxn.test", value(result, "email"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_BASIC_VALUE_ROUND_TRIP)
    public void annotationMapping_shouldRoundTripDefaultColumns() throws SQLException {
        int fullId = baseId() + 115;
        deleteRaw(fullId);

        Date createTime = new Date();
        UserInfo full = new UserInfo();
        full.setId(fullId);
        full.setName("PolicyDefaultColumns");
        full.setAge(35);
        full.setEmail("default-columns@nxn.test");
        full.setCreateTime(createTime);
        this.lambdaTemplate.insert(UserInfo.class).applyEntity(full).executeSumResult();

        UserInfo loadedFull = queryRaw(fullId);
        assertNotNull(loadedFull);
        assertEquals("PolicyDefaultColumns", loadedFull.getName());
        assertEquals(Integer.valueOf(35), loadedFull.getAge());
        assertEquals("default-columns@nxn.test", loadedFull.getEmail());
        assertNotNull(loadedFull.getCreateTime());
    }
}
