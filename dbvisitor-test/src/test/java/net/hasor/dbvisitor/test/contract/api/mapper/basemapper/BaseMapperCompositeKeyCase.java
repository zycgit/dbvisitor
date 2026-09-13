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

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.test.contract.material.model.UserRole;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NxnContract
public abstract class BaseMapperCompositeKeyCase extends AbstractNxnContractTest {
    private BaseMapper<UserRole> mapper;

    @Before
    public void createBaseMapper() throws SQLException {
        this.mapper = newSession().createBaseMapper(UserRole.class);
    }

    protected int baseId() {
        return 920000;
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_COMPOSITE_INSERT)
    public void compositeInsert_shouldPersistDistinctCompositeKeys() {
        List<UserRole> roles = Arrays.asList(//
                role(1, 100, "admin"), //
                role(1, 200, "editor"), //
                role(2, 100, "auditor"));

        int result = this.mapper.insert(roles);

        assertEquals(3, result);
        assertEquals(3, this.mapper.countAll());
        assertEquals("admin", this.mapper.loadBy(ref(1, 100)).getRoleName());
        assertEquals("editor", this.mapper.loadBy(ref(1, 200)).getRoleName());
        assertEquals("auditor", this.mapper.loadBy(ref(2, 100)).getRoleName());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_COMPOSITE_LOAD)
    public void compositeLoad_shouldSupportEntityAndMapReferences() {
        this.mapper.insert(role(11, 100, "entity-ref"));
        this.mapper.insert(role(11, 200, "map-ref"));

        UserRole entityLoaded = this.mapper.loadBy(ref(11, 100));
        Map<String, Object> mapRef = new HashMap<>();
        mapRef.put("userId", baseId() + 11);
        mapRef.put("roleId", 200);
        UserRole mapLoaded = this.mapper.loadBy(mapRef);

        assertNotNull(entityLoaded);
        assertEquals("entity-ref", entityLoaded.getRoleName());
        assertNotNull(mapLoaded);
        assertEquals("map-ref", mapLoaded.getRoleName());
        assertNull(this.mapper.loadBy(ref(99, 99)));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_COMPOSITE_QUERY)
    public void compositeQuery_shouldFilterAndCountBySample() {
        this.mapper.insert(role(21, 100, "admin"));
        this.mapper.insert(role(21, 200, "editor"));
        this.mapper.insert(role(22, 100, "admin"));

        UserRole sameUser = new UserRole();
        sameUser.setUserId(baseId() + 21);
        List<UserRole> sameUserRoles = this.mapper.listBySample(sameUser);

        UserRole admins = new UserRole();
        admins.setRoleName("admin");

        assertEquals(2, sameUserRoles.size());
        assertEquals(2, this.mapper.countBySample(admins));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_COMPOSITE_UPDATE)
    public void compositeUpdate_shouldUseAllPrimaryKeyColumns() {
        this.mapper.insert(role(31, 100, "before"));
        this.mapper.insert(role(31, 200, "untouched"));

        UserRole update = ref(31, 100);
        update.setRoleName("after");
        int entityRows = this.mapper.update(update);

        Map<String, Object> mapUpdate = new HashMap<>();
        mapUpdate.put("userId", baseId() + 31);
        mapUpdate.put("roleId", 200);
        mapUpdate.put("roleName", "after-map");
        int mapRows = this.mapper.updateByMap(mapUpdate);

        assertEquals(1, entityRows);
        assertEquals(1, mapRows);
        assertEquals("after", this.mapper.loadBy(ref(31, 100)).getRoleName());
        assertEquals("after-map", this.mapper.loadBy(ref(31, 200)).getRoleName());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_COMPOSITE_REPLACE)
    public void compositeReplace_shouldWriteNullFieldsAndSupportMapReplacement() {
        this.mapper.insert(role(51, 100, "before"));
        this.mapper.insert(role(51, 200, "before-map"));

        UserRole replacement = ref(51, 100);
        replacement.setRoleName(null);
        int entityRows = this.mapper.replace(replacement);

        Map<String, Object> mapReplacement = new HashMap<>();
        mapReplacement.put("userId", baseId() + 51);
        mapReplacement.put("roleId", 200);
        mapReplacement.put("roleName", "after-map");
        int mapRows = this.mapper.replaceByMap(mapReplacement);

        assertEquals(1, entityRows);
        assertEquals(1, mapRows);
        assertNull(this.mapper.loadBy(ref(51, 100)).getRoleName());
        assertEquals("after-map", this.mapper.loadBy(ref(51, 200)).getRoleName());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_COMPOSITE_UPSERT)
    public void compositeUpsert_shouldInsertOrUpdateByFullCompositeKey() {
        this.mapper.insert(role(61, 100, "before"));

        int insertRows = this.mapper.upsert(role(61, 200, "inserted"));
        int updateRows = this.mapper.upsert(role(61, 100, "updated"));

        Map<String, Object> mapInsert = new HashMap<>();
        mapInsert.put("userId", baseId() + 62);
        mapInsert.put("roleId", 100);
        mapInsert.put("roleName", "map-inserted");
        int mapInsertRows = this.mapper.upsertByMap(mapInsert);

        Map<String, Object> mapUpdate = new HashMap<>();
        mapUpdate.put("userId", baseId() + 62);
        mapUpdate.put("roleId", 100);
        mapUpdate.put("roleName", "map-updated");
        int mapUpdateRows = this.mapper.upsertByMap(mapUpdate);

        assertTrue(insertRows >= 1);
        assertTrue(updateRows >= 1);
        assertTrue(mapInsertRows >= 1);
        assertTrue(mapUpdateRows >= 1);
        assertEquals(3, this.mapper.countAll());
        assertEquals("updated", this.mapper.loadBy(ref(61, 100)).getRoleName());
        assertEquals("inserted", this.mapper.loadBy(ref(61, 200)).getRoleName());
        assertEquals("map-updated", this.mapper.loadBy(ref(62, 100)).getRoleName());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_COMPOSITE_DELETE)
    public void compositeDelete_shouldUseAllPrimaryKeyColumns() {
        this.mapper.insert(role(41, 100, "delete-entity"));
        this.mapper.insert(role(41, 200, "keep"));
        this.mapper.insert(role(42, 100, "delete-map"));

        int entityRows = this.mapper.delete(ref(41, 100));

        Map<String, Object> mapDelete = new HashMap<>();
        mapDelete.put("userId", baseId() + 42);
        mapDelete.put("roleId", 100);
        int mapRows = this.mapper.deleteByMap(mapDelete);

        assertEquals(1, entityRows);
        assertEquals(1, mapRows);
        assertNull(this.mapper.loadBy(ref(41, 100)));
        assertNotNull(this.mapper.loadBy(ref(41, 200)));
        assertNull(this.mapper.loadBy(ref(42, 100)));
        assertEquals(1, this.mapper.countAll());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_COMPOSITE_BATCH)
    public void compositeBatchOperations_shouldHandleDeleteListsAndRepeatedMutations() {
        List<UserRole> roles = Arrays.asList(//
                role(71, 100, "a"), //
                role(71, 200, "b"), //
                role(72, 100, "c"), //
                role(72, 200, "d"));
        this.mapper.insert(roles);

        int replaceRows = 0;
        for (UserRole role : Arrays.asList(role(71, 100, "a2"), role(71, 200, "b2"))) {
            replaceRows += this.mapper.replace(role);
        }
        int updateRows = 0;
        for (UserRole role : Arrays.asList(role(72, 100, "c2"), role(72, 200, "d2"))) {
            updateRows += this.mapper.update(role);
        }
        int upsertRows = 0;
        for (UserRole role : Arrays.asList(role(73, 100, "e"), role(71, 100, "a3"))) {
            upsertRows += this.mapper.upsert(role);
        }

        List<UserRole> deleteList = Arrays.asList(ref(71, 100), null, ref(72, 100));
        int deleteRows = this.mapper.deleteList(deleteList);

        Map<String, Object> deleteMap = new HashMap<>();
        deleteMap.put("userId", baseId() + 71);
        deleteMap.put("roleId", 200);
        int deleteMapRows = this.mapper.deleteListByMap(Arrays.asList(deleteMap));

        assertEquals(2, replaceRows);
        assertEquals(2, updateRows);
        assertTrue(upsertRows >= 2);
        assertMutationRows(2, deleteRows);
        assertMutationRows(1, deleteMapRows);
        assertNull(this.mapper.loadBy(ref(71, 100)));
        assertNull(this.mapper.loadBy(ref(71, 200)));
        assertNotNull(this.mapper.loadBy(ref(72, 200)));
        assertNotNull(this.mapper.loadBy(ref(73, 100)));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_COMPOSITE_MISSING_KEY)
    public void compositeMissingKey_shouldRejectMapReferencesWithoutAllPrimaryKeys() {
        assertRejectsMissingPrimaryKey(() -> {
            Map<String, Object> load = new HashMap<>();
            load.put("userId", baseId() + 81);
            this.mapper.loadBy(load);
        });
        assertRejectsMissingPrimaryKey(() -> {
            Map<String, Object> delete = new HashMap<>();
            delete.put("userId", baseId() + 81);
            this.mapper.deleteByMap(delete);
        });
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_COMPOSITE_PAGE)
    public void compositePage_shouldPageBySampleOnCompositeKeyTable() {
        for (int i = 1; i <= 10; i++) {
            this.mapper.insert(role(90 + i, 100, "page-admin"));
        }
        UserRole sample = new UserRole();
        sample.setRoleName("page-admin");

        PageObject page = new PageObject();
        page.setPageSize(3);
        page.setCurrentPage(0);
        PageResult<UserRole> result = this.mapper.pageBySample(sample, page);

        assertEquals(3, result.getData().size());
        for (UserRole role : result.getData()) {
            assertEquals("page-admin", role.getRoleName());
        }
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_COMPOSITE_KEY_ISOLATION)
    public void compositeKeyIsolation_shouldMutateOnlyExactCompositeKeyMatches() {
        this.mapper.insert(role(111, 100, "a"));
        this.mapper.insert(role(111, 200, "b"));
        this.mapper.insert(role(112, 100, "c"));
        this.mapper.insert(role(112, 200, "d"));

        UserRole update = ref(111, 100);
        update.setRoleName("updated");
        assertEquals(1, this.mapper.update(update));
        assertEquals("updated", this.mapper.loadBy(ref(111, 100)).getRoleName());
        assertEquals("b", this.mapper.loadBy(ref(111, 200)).getRoleName());

        UserRole replacement = ref(112, 100);
        replacement.setRoleName(null);
        assertEquals(1, this.mapper.replace(replacement));
        assertNull(this.mapper.loadBy(ref(112, 100)).getRoleName());
        assertEquals("d", this.mapper.loadBy(ref(112, 200)).getRoleName());

        assertTrue(this.mapper.upsert(role(111, 200, "upserted")) >= 1);
        assertTrue(this.mapper.upsert(role(113, 100, "inserted")) >= 1);
        assertEquals("upserted", this.mapper.loadBy(ref(111, 200)).getRoleName());
        assertNotNull(this.mapper.loadBy(ref(113, 100)));
        assertEquals(5, this.mapper.countAll());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_COMPOSITE_REJECT_SINGLE_ID)
    public void compositeKey_shouldRejectSingleIdShortcuts() {
        assertRejectsCompositePrimaryKey(() -> this.mapper.selectById(baseId() + 1));
        assertRejectsCompositePrimaryKey(() -> this.mapper.selectByIds(Arrays.asList(baseId() + 1, baseId() + 2)));
        assertRejectsCompositePrimaryKey(() -> this.mapper.deleteById(baseId() + 1));
        assertRejectsCompositePrimaryKey(() -> this.mapper.deleteByIds(Arrays.asList(baseId() + 1, baseId() + 2)));
    }

    private UserRole role(int userOffset, int roleId, String roleName) {
        UserRole role = new UserRole(baseId() + userOffset, roleId, roleName);
        return role;
    }

    private UserRole ref(int userOffset, int roleId) {
        UserRole ref = new UserRole();
        ref.setUserId(baseId() + userOffset);
        ref.setRoleId(roleId);
        return ref;
    }

    private void assertRejectsCompositePrimaryKey(Runnable runnable) {
        try {
            runnable.run();
            fail("Single-id shortcut should reject composite primary keys.");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("does not support composite primary key"));
        }
    }

    private void assertRejectsMissingPrimaryKey(Runnable runnable) {
        try {
            runnable.run();
            fail("Map reference without all composite primary key columns should be rejected.");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("missing primary key"));
        }
    }
}
