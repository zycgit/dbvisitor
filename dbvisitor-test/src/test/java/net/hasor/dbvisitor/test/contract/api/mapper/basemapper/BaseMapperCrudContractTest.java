package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NxnContract
public abstract class BaseMapperCrudContractTest extends AbstractNxnContractTest {
    private BaseMapper<UserInfo> mapper;

    @Before
    public void createBaseMapper() throws SQLException {
        this.mapper = newSession().createBaseMapper(UserInfo.class);
    }

    protected int baseId() {
        return 900000;
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_INSERT)
    public void baseMapperInsert_shouldInsertSingleEntity() {
        UserInfo user = user(baseId() + 1, "BaseInsert", 30, "insert@basemapper.com");

        int result = this.mapper.insert(user);
        UserInfo loaded = this.mapper.selectById(baseId() + 1);

        assertEquals(1, result);
        assertNotNull(loaded);
        assertEquals("BaseInsert", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_BATCH_INSERT)
    public void baseMapperBatchInsert_shouldInsertEntityList() {
        List<UserInfo> users = Arrays.asList(//
                user(baseId() + 11, "BaseBatch1", 21, "batch1@basemapper.com"), //
                user(baseId() + 12, "BaseBatch2", 22, "batch2@basemapper.com"), //
                user(baseId() + 13, "BaseBatch3", 23, "batch3@basemapper.com"));

        int result = this.mapper.insert(users);
        List<UserInfo> loaded = this.mapper.selectByIds(Arrays.asList(baseId() + 11, baseId() + 12, baseId() + 13));

        assertEquals(3, result);
        assertEquals(3, loaded.size());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_INSERT_BOUNDARY)
    public void baseMapperInsert_shouldHandleNullsEmptyListsDuplicateKeysAndLargeBatches() {
        int nullFieldsId = baseId() + 201;
        int allNullId = baseId() + 202;
        int duplicateId = baseId() + 203;
        int specialId = baseId() + 204;
        int unicodeId = baseId() + 205;

        assertEquals(1, this.mapper.insert(user(nullFieldsId, "BaseNullFields", null, null)));
        UserInfo nullFields = this.mapper.selectById(nullFieldsId);
        assertNull(nullFields.getAge());
        assertNull(nullFields.getEmail());

        UserInfo allNull = new UserInfo();
        allNull.setId(allNullId);
        assertEquals(1, this.mapper.insert(allNull));
        UserInfo loadedAllNull = this.mapper.selectById(allNullId);
        assertNotNull(loadedAllNull);
        assertNull(loadedAllNull.getName());
        assertNull(loadedAllNull.getAge());
        assertNull(loadedAllNull.getEmail());

        assertEquals(0, this.mapper.insert(Arrays.<UserInfo>asList()));

        if (profile().supportsFeature(FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED)) {
            assertEquals(1, this.mapper.insert(user(duplicateId, "BaseDuplicate1", 20, null)));
            try {
                this.mapper.insert(user(duplicateId, "BaseDuplicate2", 21, null));
                fail("Duplicate primary key should be rejected.");
            } catch (Exception e) {
                assertTrue(isDuplicateKeyMessage(e));
            }
        }

        assertEquals(1, this.mapper.insert(user(specialId, "O'Brien & Co.", 35, "special@basemapper.com")));
        assertEquals("O'Brien & Co.", this.mapper.selectById(specialId).getName());

        assertEquals(1, this.mapper.insert(user(unicodeId, "Test User Unicode", 28, "unicode@basemapper.com")));
        assertEquals("Test User Unicode", this.mapper.selectById(unicodeId).getName());

        List<UserInfo> batch = new java.util.ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            batch.add(user(baseId() + 220 + i, "BaseLargeBatch" + i, 20 + (i % 10), "large" + i + "@basemapper.com"));
        }
        assertEquals(25, this.mapper.insert(batch));
        assertNotNull(this.mapper.selectById(baseId() + 221));
        assertNotNull(this.mapper.selectById(baseId() + 245));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_EDGE_INSERT)
    public void baseMapperInsert_shouldHandleNullAutoKeyNegativeKeyEscapedValuesAndLengthErrors() {
        UserInfo autoKey = user(null, "BaseAutoKey", 25, null);
        assertEquals(1, this.mapper.insert(autoKey));

        int negativeId = -baseId();
        assertEquals(1, this.mapper.insert(user(negativeId, "BaseNegativeKey", 25, null)));
        assertEquals("BaseNegativeKey", this.mapper.selectById(negativeId).getName());

        int quotedId = baseId() + 361;
        String quotedName = "Test'Quote\"Double\\Slash";
        assertEquals(1, this.mapper.insert(user(quotedId, quotedName, 28, null)));
        assertEquals(quotedName, this.mapper.selectById(quotedId).getName());

        char[] chars = new char[1000];
        Arrays.fill(chars, 'A');
        if (profile().supportsFeature(FeatureId.LENGTH_LIMIT_ENFORCED)) {
            try {
                this.mapper.insert(user(baseId() + 362, new String(chars), 25, null));
                fail("Name longer than user_info.name should be rejected.");
            } catch (Exception e) {
                assertTrue(isLengthLimitMessage(e));
            }
        }
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_EDGE_BATCH_FAILURE)
    public void baseMapperBatchInsert_shouldRejectBatchContainingDuplicatePrimaryKey() {
        requiresNxnFeature(FeatureId.BATCH_DUPLICATE_FAILURE_PROPAGATED);

        int duplicateId = baseId() + 371;
        this.mapper.insert(user(duplicateId, "BaseBatchExisting", 25, null));

        List<UserInfo> batch = Arrays.asList(//
                user(baseId() + 372, "BaseBatchCandidate", 26, null), //
                user(duplicateId, "BaseBatchDuplicate", 27, null));
        try {
            this.mapper.insert(batch);
            fail("Batch insert containing duplicate primary key should fail.");
        } catch (Exception e) {
            assertTrue(lowerMessage(e), isDuplicateKeyMessage(e));
        }
    }

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
    @Capability(CapabilityId.BASEMAPPER_RESULT_BATCH)
    public void baseMapperBatchResults_shouldReportAffectedRowsAndMapLargeSelections() {
        List<UserInfo> users = Arrays.asList(//
                user(baseId() + 401, "BaseResultBatch1", 31, null), //
                user(baseId() + 402, "BaseResultBatch2", 32, null), //
                user(baseId() + 403, "BaseResultBatch3", 33, null));
        assertEquals(3, this.mapper.insert(users));

        UserInfo update1 = user(baseId() + 401, "BaseResultBatch1", 36, null);
        UserInfo update2 = user(baseId() + 402, "BaseResultBatch2", 37, null);
        assertEquals(2, this.mapper.replace(update1) + this.mapper.replace(update2));
        assertEquals(Integer.valueOf(36), this.mapper.selectById(baseId() + 401).getAge());
        assertEquals(Integer.valueOf(37), this.mapper.selectById(baseId() + 402).getAge());

        List<Integer> ids = Arrays.asList(baseId() + 401, baseId() + 402, baseId() + 403);
        List<UserInfo> selected = this.mapper.selectByIds(ids);
        assertEquals(3, selected.size());
        assertTrue(selected.stream().allMatch(user -> user.getName().startsWith("BaseResultBatch")));

        assertMutationRows(3, this.mapper.deleteByIds(ids));
        assertEquals(0, this.mapper.selectByIds(ids).size());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_QUERY_ALL)
    public void baseMapperQueryAll_shouldCountAllAndAllowLambdaFiltering() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            this.mapper.insert(user(baseId() + 250 + i, "BaseAll" + i, 28, null));
        }

        assertTrue(this.mapper.countAll() >= 5);
        List<UserInfo> loaded = this.mapper.query().like(UserInfo::getName, "BaseAll%").queryForList();

        assertEquals(5, loaded.size());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_PARAMETER_SAMPLE)
    public void baseMapperSampleParameters_shouldMatchMultiplePropertiesAndIgnoreNulls() {
        this.mapper.insert(user(baseId() + 331, "BaseParamSample", 25, "sample1@basemapper.com"));
        this.mapper.insert(user(baseId() + 332, "BaseParamSample", 25, null));
        this.mapper.insert(user(baseId() + 333, "BaseParamSample", 30, "sample3@basemapper.com"));

        UserInfo sample = new UserInfo();
        sample.setName("BaseParamSample");
        sample.setAge(25);
        List<UserInfo> loaded = this.mapper.listBySample(sample);

        assertEquals(2, loaded.size());
        for (UserInfo user : loaded) {
            assertEquals("BaseParamSample", user.getName());
            assertEquals(Integer.valueOf(25), user.getAge());
        }
        assertEquals(2, this.mapper.countBySample(sample));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_PARAMETER_MAP)
    public void baseMapperMapParameters_shouldCountAndDeleteByNamedValues() {
        this.mapper.insert(user(baseId() + 341, "BaseParamMap", 25, null));
        this.mapper.insert(user(baseId() + 342, "BaseParamMap", 25, null));
        this.mapper.insert(user(baseId() + 343, "BaseParamMapOther", 25, null));

        Map<String, Object> countByName = new HashMap<>();
        countByName.put("name", "BaseParamMap");
        assertEquals(2, this.mapper.countBySample(countByName));

        Map<String, Object> deleteOne = new HashMap<>();
        deleteOne.put("id", baseId() + 341);
        assertEquals(1, this.mapper.deleteByMap(deleteOne));
        assertNull(this.mapper.selectById(baseId() + 341));
        assertNotNull(this.mapper.selectById(baseId() + 342));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_PARAMETER_BATCH)
    public void baseMapperBatchParameters_shouldRoundTripSelectedPrimaryKeys() {
        for (int i = 1; i <= 10; i++) {
            this.mapper.insert(user(baseId() + 350 + i, "BaseParamBatch" + i, 20 + i, null));
        }
        List<Integer> ids = Arrays.asList(baseId() + 352, baseId() + 354, baseId() + 356, baseId() + 358, baseId() + 360);

        List<UserInfo> loaded = this.mapper.selectByIds(ids);
        int deleted = this.mapper.deleteByIds(ids);

        assertEquals(5, loaded.size());
        assertTrue(loaded.stream().allMatch(user -> user.getName().startsWith("BaseParamBatch")));
        assertMutationRows(5, deleted);
        assertEquals(0, this.mapper.selectByIds(ids).size());
        assertNotNull(this.mapper.selectById(baseId() + 351));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_LIST_BY_SAMPLE)
    public void baseMapperListBySample_shouldFilterByNonNullFields() {
        this.mapper.insert(user(baseId() + 41, "BaseSample1", 51, null));
        this.mapper.insert(user(baseId() + 42, "BaseSample2", 52, null));
        this.mapper.insert(user(baseId() + 43, "BaseSample3", 51, null));

        UserInfo sample = new UserInfo();
        sample.setAge(51);

        List<UserInfo> loaded = this.mapper.listBySample(sample);

        assertEquals(2, loaded.size());
        for (UserInfo user : loaded) {
            assertEquals(Integer.valueOf(51), user.getAge());
        }
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_COUNT_BY_SAMPLE)
    public void baseMapperCountBySample_shouldCountMatchingRows() {
        this.mapper.insert(user(baseId() + 51, "BaseCount1", 61, null));
        this.mapper.insert(user(baseId() + 52, "BaseCount2", 62, null));
        this.mapper.insert(user(baseId() + 53, "BaseCount3", 61, null));

        UserInfo sample = new UserInfo();
        sample.setAge(61);

        int count = this.mapper.countBySample(sample);

        assertEquals(2, count);
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_PAGE_BY_SAMPLE)
    public void baseMapperPageBySample_shouldReturnPagedRows() {
        for (int i = 1; i <= 8; i++) {
            this.mapper.insert(user(baseId() + 60 + i, "BasePage" + i, 71, null));
        }
        UserInfo sample = new UserInfo();
        sample.setAge(71);

        Page page = this.mapper.pageInitBySample(sample, 1, 3);
        List<UserInfo> loaded = this.mapper.pageBySample(sample, page).getData();

        assertEquals(8, page.getTotalCount());
        assertEquals(3, page.getTotalPage());
        assertEquals(3, loaded.size());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_PAGE_BOUNDARY)
    public void baseMapperPageBySample_shouldHandleZeroLargeSizeAndEmptySamplePages() {
        for (int i = 1; i <= 10; i++) {
            this.mapper.insert(user(baseId() + 420 + i, "BasePageBoundary" + i, 40, null));
        }

        UserInfo sample = new UserInfo();
        sample.setAge(40);

        PageObject first = new PageObject();
        first.setPageSize(5);
        first.setCurrentPage(0);
        assertEquals(5, this.mapper.pageBySample(sample, first).getData().size());

        PageObject large = new PageObject();
        large.setPageSize(10);
        large.setCurrentPage(100);
        assertEquals(0, this.mapper.pageBySample(sample, large).getData().size());

        PageObject zeroSize = new PageObject();
        zeroSize.setPageSize(0);
        zeroSize.setCurrentPage(0);
        assertEquals(10, this.mapper.pageBySample(sample, zeroSize).getData().size());

        PageObject emptySamplePage = new PageObject();
        emptySamplePage.setPageSize(5);
        emptySamplePage.setCurrentPage(0);
        assertTrue(this.mapper.pageBySample(new UserInfo(), emptySamplePage).getData().size() >= 5);
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

    @Test
    @Capability(CapabilityId.BASEMAPPER_ACCESSORS)
    public void baseMapperAccessors_shouldExposeEntityTypeSessionJdbcAndLambdaApis() throws SQLException {
        int lambdaId = baseId() + 141;
        int jdbcId = baseId() + 142;

        assertEquals(UserInfo.class, this.mapper.entityType());
        assertNotNull(this.mapper.session());
        assertNotNull(this.mapper.lambda());
        assertNotNull(this.mapper.jdbc());

        int lambdaResult = this.mapper.lambda().insert(UserInfo.class).applyEntity(user(lambdaId, "BaseAccessorLambda", 141, null)).executeSumResult();
        int jdbcResult = this.mapper.jdbc().executeUpdate(//
                "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, @{macro, currentTimestamp})", //
                new Object[] { jdbcId, "BaseAccessorJdbc", 142 });

        assertEquals(1, lambdaResult);
        assertEquals(1, jdbcResult);
        assertNotNull(this.mapper.selectById(lambdaId));
        assertNotNull(this.mapper.selectById(jdbcId));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_MIXED_OPERATIONS)
    public void baseMapperMixedOperations_shouldSupportCrudAndUpsertInOneMapper() {
        int id = baseId() + 151;

        this.mapper.insert(user(id, "BaseMixed", 151, "mixed@basemapper.com"));
        UserInfo loaded = this.mapper.selectById(id);
        assertNotNull(loaded);

        loaded.setAge(152);
        this.mapper.update(loaded);
        assertEquals(Integer.valueOf(152), this.mapper.selectById(id).getAge());

        loaded.setAge(153);
        this.mapper.upsert(loaded);
        assertEquals(Integer.valueOf(153), this.mapper.selectById(id).getAge());

        assertEquals(1, this.mapper.deleteById(id));
        assertNull(this.mapper.selectById(id));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_REPLACE)
    public void baseMapperReplace_shouldWriteNullFields() {
        int id = baseId() + 261;
        this.mapper.insert(user(id, "BaseReplaceBefore", 28, "replace@basemapper.com"));

        UserInfo replacement = new UserInfo();
        replacement.setId(id);
        replacement.setName("BaseReplaced");
        replacement.setAge(null);
        int result = this.mapper.replace(replacement);
        UserInfo loaded = this.mapper.selectById(id);

        assertEquals(1, result);
        assertEquals("BaseReplaced", loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_UPSERT_INSERT)
    public void baseMapperUpsert_shouldInsertMissingEntity() {
        int id = baseId() + 271;

        int result = this.mapper.upsert(user(id, "BaseUpsertInsert", 32, "upsert-insert@basemapper.com"));
        UserInfo loaded = this.mapper.selectById(id);

        assertTrue(result >= 1);
        assertNotNull(loaded);
        assertEquals("BaseUpsertInsert", loaded.getName());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_UPSERT_UPDATE)
    public void baseMapperUpsert_shouldUpdateExistingEntity() {
        int id = baseId() + 281;
        this.mapper.insert(user(id, "BaseUpsertBefore", 25, "upsert-update@basemapper.com"));

        int result = this.mapper.upsert(user(id, "BaseUpsertAfter", 26, "upsert-update@basemapper.com"));
        UserInfo loaded = this.mapper.selectById(id);

        assertTrue(result >= 1);
        assertEquals("BaseUpsertAfter", loaded.getName());
        assertEquals(Integer.valueOf(26), loaded.getAge());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_UPDATE)
    public void baseMapperUpdate_shouldUpdateOnlyNonNullFields() {
        this.mapper.insert(user(baseId() + 81, "BaseUpdate", 81, "before@basemapper.com"));

        UserInfo update = new UserInfo();
        update.setId(baseId() + 81);
        update.setAge(82);

        int result = this.mapper.update(update);
        UserInfo loaded = this.mapper.selectById(baseId() + 81);

        assertEquals(1, result);
        assertEquals("BaseUpdate", loaded.getName());
        assertEquals(Integer.valueOf(82), loaded.getAge());
        assertEquals("before@basemapper.com", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_UPDATE_BOUNDARY)
    public void baseMapperUpdate_shouldReturnZeroForMissingOrNullPrimaryKey() {
        UserInfo missing = user(baseId() + 99901, "BaseMissingUpdate", 30, null);
        assertMutationRows(0, this.mapper.update(missing));

        UserInfo nullPrimaryKey = new UserInfo();
        nullPrimaryKey.setName("BaseNullPrimaryKey");
        nullPrimaryKey.setAge(30);
        assertMutationRows(0, this.mapper.update(nullPrimaryKey));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_UPDATE_BY_MAP)
    public void baseMapperUpdateByMap_shouldUpdateMapFields() {
        this.mapper.insert(user(baseId() + 91, "BaseMapUpdate", 91, "map@basemapper.com"));

        Map<String, Object> update = new HashMap<>();
        update.put("id", baseId() + 91);
        update.put("name", "BaseMapUpdated");
        update.put("age", 92);

        int result = this.mapper.updateByMap(update);
        UserInfo loaded = this.mapper.selectById(baseId() + 91);

        assertEquals(1, result);
        assertEquals("BaseMapUpdated", loaded.getName());
        assertEquals(Integer.valueOf(92), loaded.getAge());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE)
    public void baseMapperDelete_shouldDeleteById() {
        this.mapper.insert(user(baseId() + 101, "BaseDelete", 101, null));

        int result = this.mapper.deleteById(baseId() + 101);

        assertEquals(1, result);
        assertNull(this.mapper.selectById(baseId() + 101));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE_BY_IDS)
    public void baseMapperDeleteByIds_shouldDeleteOnlyRequestedPrimaryKeys() {
        for (int i = 1; i <= 5; i++) {
            this.mapper.insert(user(baseId() + 290 + i, "BaseDeleteIds" + i, 20 + i, null));
        }

        int result = this.mapper.deleteByIds(Arrays.asList(baseId() + 291, baseId() + 292, baseId() + 293));

        assertMutationRows(3, result);
        assertNull(this.mapper.selectById(baseId() + 291));
        assertNull(this.mapper.selectById(baseId() + 292));
        assertNull(this.mapper.selectById(baseId() + 293));
        assertNotNull(this.mapper.selectById(baseId() + 294));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE_ENTITY)
    public void baseMapperDeleteEntity_shouldDeleteUsingEntityPrimaryKey() {
        int id = baseId() + 301;
        this.mapper.insert(user(id, "BaseDeleteEntity", 28, null));

        UserInfo delete = new UserInfo();
        delete.setId(id);
        int result = this.mapper.delete(delete);

        assertEquals(1, result);
        assertNull(this.mapper.selectById(id));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE_LIST)
    public void baseMapperDeleteList_shouldDeleteEntityList() {
        List<UserInfo> users = Arrays.asList(//
                user(baseId() + 311, "BaseDeleteList1", 31, null), //
                user(baseId() + 312, "BaseDeleteList2", 32, null), //
                user(baseId() + 313, "BaseDeleteList3", 33, null), //
                user(baseId() + 314, "BaseDeleteList4", 34, null));
        this.mapper.insert(users);

        int result = this.mapper.deleteList(users.subList(0, 3));

        assertMutationRows(3, result);
        assertNull(this.mapper.selectById(baseId() + 311));
        assertNull(this.mapper.selectById(baseId() + 312));
        assertNull(this.mapper.selectById(baseId() + 313));
        assertNotNull(this.mapper.selectById(baseId() + 314));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE_BOUNDARY)
    public void baseMapperDelete_shouldHandleMissingEmptyAndNullPrimaryKeyCases() {
        int reinsertId = baseId() + 321;

        assertMutationRows(0, this.mapper.deleteById(baseId() + 99902));
        assertMutationRows(0, this.mapper.deleteByIds(Arrays.asList()));

        this.mapper.insert(user(reinsertId, "BaseReinsertBefore", 30, null));
        assertEquals(1, this.mapper.deleteById(reinsertId));
        assertEquals(1, this.mapper.insert(user(reinsertId, "BaseReinsertAfter", 35, null)));
        assertEquals("BaseReinsertAfter", this.mapper.selectById(reinsertId).getName());

        try {
            this.mapper.deleteById(null);
            fail("Null primary key should be rejected.");
        } catch (Exception e) {
            String message = String.valueOf(e.getMessage()).toLowerCase();
            assertTrue(message.contains("null"));
        }
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE_BY_MAP)
    public void baseMapperDeleteByMap_shouldDeleteMatchingPrimaryKey() {
        this.mapper.insert(user(baseId() + 111, "BaseMapDelete", 111, null));
        Map<String, Object> delete = new HashMap<>();
        delete.put("id", baseId() + 111);

        int result = this.mapper.deleteByMap(delete);

        assertEquals(1, result);
        assertNull(this.mapper.selectById(baseId() + 111));
    }

    private UserInfo user(Integer id, String name, Integer age, String email) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        user.setCreateTime(new Date());
        return user;
    }

}
