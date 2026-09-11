/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class BaseMapperQueryContractTest extends BaseMapperCrudSupport {
    @Test
    @Capability(CapabilityId.BASEMAPPER_SELECT_BY_ID)
    public void baseMapperSelectById_shouldReturnSingleEntity() {
        this.mapper.insert(user(baseId() + 21, "BaseSelect", 31, "select@basemapper.com"));

        UserInfo loaded = this.mapper.selectById(baseId() + 21);

        assertNotNull(loaded);
        assertEquals("BaseSelect", loaded.getName());
        assertEquals(Integer.valueOf(31), loaded.getAge());
        assertEquals("select@basemapper.com", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_SELECT_BY_IDS)
    public void baseMapperSelectByIds_shouldReturnMatchingEntities() {
        this.mapper.insert(user(baseId() + 31, "BaseIds1", 41, null));
        this.mapper.insert(user(baseId() + 32, "BaseIds2", 42, null));
        this.mapper.insert(user(baseId() + 33, "BaseIds3", 43, null));

        List<UserInfo> loaded = this.mapper.selectByIds(Arrays.asList(baseId() + 31, baseId() + 33));
        loaded.sort((left, right) -> Integer.compare(left.getId(), right.getId()));

        assertEquals(2, loaded.size());
        assertEquals("BaseIds1", loaded.get(0).getName());
        assertEquals("BaseIds3", loaded.get(1).getName());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_QUERY_EMPTY)
    public void baseMapperQuery_shouldReturnNullOrEmptyForMissingRows() {
        assertNull(this.mapper.selectById(baseId() + 99999));

        List<UserInfo> emptyIds = this.mapper.selectByIds(Arrays.asList(baseId() + 99991, baseId() + 99992));
        assertNotNull(emptyIds);
        assertEquals(0, emptyIds.size());

        UserInfo sample = new UserInfo();
        sample.setAge(999999);
        List<UserInfo> emptySample = this.mapper.listBySample(sample);
        assertNotNull(emptySample);
        assertEquals(0, emptySample.size());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_RESULT_MAPPING)
    public void baseMapperResults_shouldMapEntityFieldsAndCountsConsistently() {
        for (int i = 1; i <= 10; i++) {
            this.mapper.insert(user(baseId() + 380 + i, "BaseResult" + i, 20 + i, "result" + i + "@basemapper.com"));
        }

        UserInfo one = this.mapper.selectById(baseId() + 381);
        assertNotNull(one);
        assertEquals(Integer.valueOf(baseId() + 381), one.getId());
        assertEquals("BaseResult1", one.getName());
        assertEquals(Integer.valueOf(21), one.getAge());
        assertEquals("result1@basemapper.com", one.getEmail());

        List<UserInfo> selected = this.mapper.selectByIds(Arrays.asList(baseId() + 382, baseId() + 384, baseId() + 386));
        selected.sort((left, right) -> Integer.compare(left.getId(), right.getId()));
        assertEquals(3, selected.size());
        assertEquals("BaseResult2", selected.get(0).getName());
        assertEquals("BaseResult4", selected.get(1).getName());
        assertEquals("BaseResult6", selected.get(2).getName());

        UserInfo sample = new UserInfo();
        sample.setAge(26);
        List<UserInfo> byAge = this.mapper.listBySample(sample);
        assertEquals(1, byAge.size());
        assertEquals("BaseResult6", byAge.get(0).getName());

        Map<String, Object> mapSample = new HashMap<>();
        mapSample.put("age", 28);
        assertEquals(1, this.mapper.countBySample(mapSample));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_QUERY_ALL)
    public void baseMapperQueryAll_shouldCountAllAndAllowLambdaFiltering() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            this.mapper.insert(user(baseId() + 250 + i, "BaseAll" + i, 28, null));
        }

        assertTrue(this.mapper.countAll() >= 5);
        List<UserInfo> loaded = this.mapper.query().eq(UserInfo::getAge, 28).queryForList();

        assertEquals(5, loaded.size());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_LOAD_BY)
    public void baseMapperLoadBy_shouldLoadEntityUsingReferenceObject() {
        this.mapper.insert(user(baseId() + 121, "BaseLoadBy", 121, "load-by@basemapper.com"));

        UserInfo reference = new UserInfo();
        reference.setId(baseId() + 121);
        UserInfo loaded = this.mapper.loadBy(reference);

        assertNotNull(loaded);
        assertEquals("BaseLoadBy", loaded.getName());
        assertEquals(Integer.valueOf(121), loaded.getAge());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_LOAD_LIST_BY)
    public void baseMapperLoadListBy_shouldLoadEntitiesUsingReferenceList() {
        this.mapper.insert(user(baseId() + 131, "BaseLoadList1", 131, null));
        this.mapper.insert(user(baseId() + 132, "BaseLoadList2", 132, null));
        this.mapper.insert(user(baseId() + 133, "BaseLoadList3", 133, null));

        UserInfo ref1 = new UserInfo();
        ref1.setId(baseId() + 131);
        UserInfo ref3 = new UserInfo();
        ref3.setId(baseId() + 133);
        List<UserInfo> loaded = this.mapper.loadListBy(Arrays.<Object>asList(ref1, ref3));
        loaded.sort((left, right) -> Integer.compare(left.getId(), right.getId()));

        assertEquals(2, loaded.size());
        assertEquals("BaseLoadList1", loaded.get(0).getName());
        assertEquals("BaseLoadList3", loaded.get(1).getName());
    }
}
