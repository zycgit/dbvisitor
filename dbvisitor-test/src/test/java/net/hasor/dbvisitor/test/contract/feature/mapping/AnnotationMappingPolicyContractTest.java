package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ColumnMappedUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ColumnNameUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ColumnValueUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.DiffPkEntity;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ExplicitMappingUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.IgnoreOnMethodUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.IgnoreWithColumnUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.IgnoredEmailUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.InsertExcludedUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.MultipleIgnoreUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.PrimaryMarkerUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ReadOnlyEmailUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.SimpleResultMap;
import net.hasor.dbvisitor.test.contract.material.model.annotation.UpdateExcludedUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.UserResultMap;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class AnnotationMappingPolicyContractTest extends AbstractNxnContractTest {
    private LambdaTemplate lambda;

    @Before
    public void createLambdaTemplate() throws SQLException {
        this.lambda = new LambdaTemplate(dataSource);
    }

    protected int baseId() {
        return 920000;
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_PRIMARY_KEY_METADATA)
    public void annotationMapping_shouldExposePrimaryKeyMetadata() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class);

        TableMapping<?> mapping = registry.findByEntity(UserInfo.class);
        assertNotNull(mapping);

        ColumnMapping idColumn = mapping.getPropertyByName("id");
        ColumnMapping nameColumn = mapping.getPropertyByName("name");
        ColumnMapping ageColumn = mapping.getPropertyByName("age");

        assertNotNull(idColumn);
        assertNotNull(nameColumn);
        assertNotNull(ageColumn);
        assertTrue(idColumn.isPrimaryKey());
        assertFalse(nameColumn.isPrimaryKey());
        assertFalse(ageColumn.isPrimaryKey());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_PRIMARY_KEY_MULTIPLE_ENTITIES)
    public void annotationMapping_shouldKeepPrimaryKeyMetadataPerEntity() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class, "", "userInfo");
        registry.loadEntityToSpace(DiffPkEntity.class, "", "diffPk");

        TableMapping<?> userMapping = registry.findBySpace("", "userInfo");
        TableMapping<?> diffMapping = registry.findBySpace("", "diffPk");

        assertTrue(userMapping.getPropertyByName("id").isPrimaryKey());
        assertTrue(diffMapping.getPropertyByName("code").isPrimaryKey());
        assertFalse(diffMapping.getPropertyByName("id").isPrimaryKey());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_PRIMARY_KEY_CONDITION_UPDATE)
    public void annotationMapping_shouldUsePrimaryKeyPropertyInLambdaConditions() throws SQLException {
        int firstId = baseId() + 101;
        int secondId = baseId() + 102;
        insertRaw(firstId, "PolicyPkUpdate1", 25, "pk1@nxn.test");
        insertRaw(secondId, "PolicyPkUpdate2", 30, "pk2@nxn.test");

        int updated = this.lambda.update(UserInfo.class)//
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

        this.lambda.insert(ColumnMappedUser.class).applyEntity(user).executeSumResult();
        ColumnMappedUser loaded = this.lambda.query(ColumnMappedUser.class)//
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
        this.lambda.insert(ColumnNameUser.class).applyEntity(nameUser).executeSumResult();

        ColumnValueUser valueUser = new ColumnValueUser();
        valueUser.setId(valueId);
        valueUser.setUserName("PolicyValueAttr");
        valueUser.setAge(27);
        valueUser.setEmail("value-attr@nxn.test");
        valueUser.setCreateTime(new Date());
        this.lambda.insert(ColumnValueUser.class).applyEntity(valueUser).executeSumResult();

        assertEquals("PolicyNameAttr", queryRaw(nameId).getName());
        assertEquals("PolicyValueAttr", queryRaw(valueId).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_PRIMARY_MARKER)
    public void annotationMapping_shouldUsePrimaryMarkerForDuplicateColumnMappings() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(PrimaryMarkerUser.class);

        TableMapping<?> mapping = registry.findByEntity(PrimaryMarkerUser.class);
        assertNotNull(mapping);

        List<ColumnMapping> nameColumns = mapping.getPropertyByColumn("name");
        assertEquals(2, nameColumns.size());

        ColumnMapping primaryColumn = mapping.getPrimaryPropertyByColumn("name");
        assertNotNull(primaryColumn);
        assertEquals("primaryName", primaryColumn.getProperty());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_RESULT_MAP_METADATA)
    public void resultMapAnnotation_shouldLoadSpaceIdValueAndCustomOverride() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(UserResultMap.class);
        registry.loadResultMapToSpace(SimpleResultMap.class);
        registry.loadResultMapToSpace(SimpleResultMap.class, "customSpace", "customName");

        TableMapping<?> userResultMap = registry.findBySpace("mySpace", "myResultMap");
        assertNotNull(userResultMap);
        assertTrue(userResultMap.isAutoProperty());
        assertTrue(userResultMap.isCaseInsensitive());
        assertTrue(userResultMap.isToCamelCase());
        assertTrue(userResultMap.getTable() == null || userResultMap.getTable().isEmpty());

        assertNotNull(registry.findBySpace("", "simpleResult"));
        assertNotNull(registry.findBySpace("customSpace", "customName"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_MAP_QUERY_RESULT)
    public void annotationMapping_shouldExposeMapQueryResults() throws SQLException {
        int id = baseId() + 114;
        insertRaw(id, "PolicyMapResult", 30, "map-result@nxn.test");

        Map<String, Object> result = this.lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .queryForMap();

        assertNotNull(result);
        assertEquals("PolicyMapResult", value(result, "name"));
        assertEquals("map-result@nxn.test", value(result, "email"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_BASIC_VALUE_ROUND_TRIP)
    public void annotationMapping_shouldRoundTripDefaultColumnsNullsAndEmptyStrings() throws SQLException {
        requiresNxnFeature(FeatureId.DISTINCT_EMPTY_STRING);

        int fullId = baseId() + 115;
        int nullId = baseId() + 116;
        int emptyId = baseId() + 117;
        deleteRaw(fullId);
        deleteRaw(nullId);
        deleteRaw(emptyId);

        Date createTime = new Date();
        UserInfo full = new UserInfo();
        full.setId(fullId);
        full.setName("PolicyDefaultColumns");
        full.setAge(35);
        full.setEmail("default-columns@nxn.test");
        full.setCreateTime(createTime);
        this.lambda.insert(UserInfo.class).applyEntity(full).executeSumResult();

        UserInfo loadedFull = queryRaw(fullId);
        assertNotNull(loadedFull);
        assertEquals("PolicyDefaultColumns", loadedFull.getName());
        assertEquals(Integer.valueOf(35), loadedFull.getAge());
        assertEquals("default-columns@nxn.test", loadedFull.getEmail());
        assertNotNull(loadedFull.getCreateTime());

        UserInfo nullValue = new UserInfo();
        nullValue.setId(nullId);
        nullValue.setName("PolicyNullColumns");
        nullValue.setAge(null);
        nullValue.setEmail(null);
        this.lambda.insert(UserInfo.class).applyEntity(nullValue).executeSumResult();

        UserInfo loadedNull = queryRaw(nullId);
        assertEquals("PolicyNullColumns", loadedNull.getName());
        assertNull(loadedNull.getAge());
        assertNull(loadedNull.getEmail());

        UserInfo emptyValue = new UserInfo();
        emptyValue.setId(emptyId);
        emptyValue.setName("");
        emptyValue.setAge(25);
        emptyValue.setEmail("empty-columns@nxn.test");
        this.lambda.insert(UserInfo.class).applyEntity(emptyValue).executeSumResult();

        UserInfo loadedEmpty = queryRaw(emptyId);
        assertNotNull(loadedEmpty.getName());
        assertEquals("", loadedEmpty.getName());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_RESULT_MAP_NON_ENTITY)
    public void resultMapAnnotation_shouldNotMakeClassAnEntity() {
        assertFalse(MappingRegistry.isEntity(UserResultMap.class));
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_WRITE_POLICY_METADATA)
    public void annotationMapping_shouldExposeInsertAndUpdatePolicyMetadata() {
        MappingRegistry registry = new MappingRegistry();

        registry.loadEntityToSpace(UserInfo.class, "", "default");
        registry.loadEntityToSpace(InsertExcludedUser.class, "", "insertExcluded");
        registry.loadEntityToSpace(UpdateExcludedUser.class, "", "updateExcluded");
        registry.loadEntityToSpace(ReadOnlyEmailUser.class, "", "readOnly");

        ColumnMapping defaultEmail = registry.findBySpace("", "default").getPropertyByName("email");
        assertTrue(defaultEmail.isInsert());
        assertTrue(defaultEmail.isUpdate());

        ColumnMapping insertExcludedEmail = registry.findBySpace("", "insertExcluded").getPropertyByName("email");
        assertFalse(insertExcludedEmail.isInsert());
        assertTrue(insertExcludedEmail.isUpdate());

        ColumnMapping updateExcludedEmail = registry.findBySpace("", "updateExcluded").getPropertyByName("email");
        assertTrue(updateExcludedEmail.isInsert());
        assertFalse(updateExcludedEmail.isUpdate());

        ColumnMapping readOnlyEmail = registry.findBySpace("", "readOnly").getPropertyByName("email");
        assertFalse(readOnlyEmail.isInsert());
        assertFalse(readOnlyEmail.isUpdate());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_INSERT_FALSE)
    public void annotationMapping_shouldExcludeInsertFalseColumnFromInsert() throws SQLException {
        InsertExcludedUser user = new InsertExcludedUser();
        user.setId(baseId() + 1);
        user.setName("PolicyInsertFalse");
        user.setAge(25);
        user.setEmail("should-not-insert@nxn.test");
        user.setCreateTime(new Date());

        this.lambda.insert(InsertExcludedUser.class).applyEntity(user).executeSumResult();
        UserInfo raw = queryRaw(baseId() + 1);

        assertEquals("PolicyInsertFalse", raw.getName());
        assertEquals(Integer.valueOf(25), raw.getAge());
        assertNull(raw.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_UPDATE_FALSE)
    public void annotationMapping_shouldExcludeUpdateFalseColumnFromUpdate() throws SQLException {
        insertRaw(baseId() + 11, "PolicyUpdateFalse", 30, "before-update-false@nxn.test");

        UpdateExcludedUser update = new UpdateExcludedUser();
        update.setName("PolicyUpdateFalseChanged");
        update.setEmail("should-not-update@nxn.test");
        int rows = this.lambda.update(UpdateExcludedUser.class) //
                .eq(UpdateExcludedUser::getId, baseId() + 11) //
                .updateToSample(update) //
                .doUpdate();

        UserInfo raw = queryRaw(baseId() + 11);
        assertEquals(1, rows);
        assertEquals("PolicyUpdateFalseChanged", raw.getName());
        assertEquals("before-update-false@nxn.test", raw.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_READ_ONLY_FIELD)
    public void annotationMapping_shouldTreatInsertFalseAndUpdateFalseAsReadOnly() throws SQLException {
        ReadOnlyEmailUser user = new ReadOnlyEmailUser();
        user.setId(baseId() + 21);
        user.setName("PolicyReadOnly");
        user.setAge(28);
        user.setEmail("should-not-write@nxn.test");
        user.setCreateTime(new Date());

        this.lambda.insert(ReadOnlyEmailUser.class).applyEntity(user).executeSumResult();
        assertNull(queryRaw(baseId() + 21).getEmail());

        jdbcTemplate.executeUpdate("UPDATE user_info SET email = ? WHERE id = ?", new Object[] { "db-value@nxn.test", baseId() + 21 });

        ReadOnlyEmailUser update = new ReadOnlyEmailUser();
        update.setName("PolicyReadOnlyChanged");
        update.setEmail("should-not-overwrite@nxn.test");
        this.lambda.update(ReadOnlyEmailUser.class) //
                .eq(ReadOnlyEmailUser::getId, baseId() + 21) //
                .updateToSample(update) //
                .doUpdate();

        UserInfo raw = queryRaw(baseId() + 21);
        assertEquals("PolicyReadOnlyChanged", raw.getName());
        assertEquals("db-value@nxn.test", raw.getEmail());

        ReadOnlyEmailUser loaded = this.lambda.query(ReadOnlyEmailUser.class).eq(ReadOnlyEmailUser::getId, baseId() + 21).queryForObject();
        assertEquals("db-value@nxn.test", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_IGNORE_LIFECYCLE)
    public void annotationMapping_shouldExcludeIgnoredFieldFromInsertSelectWhereAndUpdate() throws SQLException {
        IgnoredEmailUser user = new IgnoredEmailUser();
        user.setId(baseId() + 31);
        user.setName("PolicyIgnore");
        user.setAge(25);
        user.setEmail("ignored-insert@nxn.test");
        user.setCreateTime(new Date());

        this.lambda.insert(IgnoredEmailUser.class).applyEntity(user).executeSumResult();
        assertNull(queryRaw(baseId() + 31).getEmail());

        jdbcTemplate.executeUpdate("UPDATE user_info SET email = ? WHERE id = ?", new Object[] { "db-ignore@nxn.test", baseId() + 31 });

        IgnoredEmailUser loaded = this.lambda.query(IgnoredEmailUser.class).eq(IgnoredEmailUser::getId, baseId() + 31).queryForObject();
        assertNotNull(loaded);
        assertNull(loaded.getEmail());

        IgnoredEmailUser sample = new IgnoredEmailUser();
        sample.setName("PolicyIgnore");
        sample.setEmail("wrong-email@nxn.test");
        IgnoredEmailUser bySample = this.lambda.query(IgnoredEmailUser.class).eqBySample(sample).queryForObject();
        assertNotNull(bySample);
        assertEquals(Integer.valueOf(baseId() + 31), bySample.getId());

        loaded.setName("PolicyIgnoreChanged");
        loaded.setAge(99);
        loaded.setEmail("ignored-update@nxn.test");
        this.lambda.update(IgnoredEmailUser.class).eq(IgnoredEmailUser::getId, baseId() + 31).updateRow(loaded).doUpdate();

        UserInfo raw = queryRaw(baseId() + 31);
        assertEquals("PolicyIgnoreChanged", raw.getName());
        assertEquals(Integer.valueOf(99), raw.getAge());
        assertEquals("db-ignore@nxn.test", raw.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_IGNORE_MULTIPLE_FIELDS)
    public void annotationMapping_shouldExcludeMultipleIgnoredFields() throws SQLException {
        MultipleIgnoreUser user = new MultipleIgnoreUser();
        user.setId(baseId() + 32);
        user.setName("PolicyMultiIgnore");
        user.setAge(30);
        user.setEmail("multi-ignore@nxn.test");
        user.setCreateTime(new Date());

        this.lambda.insert(MultipleIgnoreUser.class).applyEntity(user).executeSumResult();
        UserInfo raw = queryRaw(baseId() + 32);

        assertEquals("PolicyMultiIgnore", raw.getName());
        assertNull(raw.getAge());
        assertNull(raw.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_IGNORE_OVERRIDES_COLUMN)
    public void annotationMapping_shouldLetIgnoreOverrideColumnAnnotation() throws SQLException {
        IgnoreWithColumnUser user = new IgnoreWithColumnUser();
        user.setId(baseId() + 33);
        user.setName("PolicyIgnoreColumn");
        user.setAge(26);
        user.setEmail("ignore-column@nxn.test");
        user.setCreateTime(new Date());

        this.lambda.insert(IgnoreWithColumnUser.class).applyEntity(user).executeSumResult();
        UserInfo raw = queryRaw(baseId() + 33);

        assertEquals("PolicyIgnoreColumn", raw.getName());
        assertNull(raw.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_IGNORE_ON_METHOD)
    public void annotationMapping_shouldHonorIgnoreOnGetterMethod() throws SQLException {
        IgnoreOnMethodUser user = new IgnoreOnMethodUser();
        user.setId(baseId() + 34);
        user.setName("PolicyIgnoreMethod");
        user.setAge(27);
        user.setEmail("ignore-method@nxn.test");
        user.setCreateTime(new Date());

        this.lambda.insert(IgnoreOnMethodUser.class).applyEntity(user).executeSumResult();
        UserInfo raw = queryRaw(baseId() + 34);

        assertEquals("PolicyIgnoreMethod", raw.getName());
        assertNull(raw.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_AUTO_MAPPING_FALSE)
    public void annotationMapping_shouldMapOnlyAnnotatedColumnsWhenAutoMappingFalse() throws SQLException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(ExplicitMappingUser.class, "", "explicit");
        TableMapping<?> mapping = registry.findBySpace("", "explicit");
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("name"));
        assertNull(mapping.getPropertyByName("age"));
        assertNull(mapping.getPropertyByName("email"));

        ExplicitMappingUser user = new ExplicitMappingUser();
        user.setId(baseId() + 41);
        user.setName("PolicyExplicit");
        user.setAge(28);
        user.setEmail("explicit@nxn.test");
        user.setCreateTime(new Date());

        this.lambda.insert(ExplicitMappingUser.class).applyEntity(user).executeSumResult();
        UserInfo raw = queryRaw(baseId() + 41);

        assertEquals("PolicyExplicit", raw.getName());
        assertNull(raw.getAge());
        assertNull(raw.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_UPDATE_NULL_VALUE)
    public void annotationMapping_shouldUpdateExplicitNullValues() throws SQLException {
        int id = baseId() + 51;
        insertRaw(id, "PolicyUpdateNull", 30, "update-null@nxn.test");

        int updated = this.lambda.update(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .updateTo(UserInfo::getAge, null)//
                .updateTo(UserInfo::getEmail, null)//
                .doUpdate();

        UserInfo loaded = queryRaw(id);
        assertEquals(1, updated);
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_PARTIAL_INSERT)
    public void annotationMapping_shouldAllowPartialEntityInsert() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(baseId() + 52);
        user.setName("PolicyPartialInsert");
        user.setAge(28);

        this.lambda.insert(UserInfo.class).applyEntity(user).executeSumResult();
        UserInfo loaded = queryRaw(baseId() + 52);

        assertNotNull(loaded);
        assertEquals("PolicyPartialInsert", loaded.getName());
        assertEquals(Integer.valueOf(28), loaded.getAge());
        assertNull(loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_PARTIAL_UPDATE)
    public void annotationMapping_shouldUpdateOnlyExplicitFields() throws SQLException {
        int id = baseId() + 53;
        insertRaw(id, "PolicyPartialUpdate", 25, "partial-update@nxn.test");

        int updated = this.lambda.update(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .updateTo(UserInfo::getName, "PolicyOnlyNameUpdated")//
                .doUpdate();

        UserInfo loaded = queryRaw(id);
        assertEquals(1, updated);
        assertEquals("PolicyOnlyNameUpdated", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
        assertEquals("partial-update@nxn.test", loaded.getEmail());
    }

    private void insertRaw(int id, String name, Integer age, String email) throws SQLException {
        deleteRaw(id);
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        user.setCreateTime(new Date());
        this.lambda.insert(UserInfo.class).applyEntity(user).executeSumResult();
    }

    private UserInfo queryRaw(int id) throws SQLException {
        return this.lambda.query(UserInfo.class).eq(UserInfo::getId, id).queryForObject();
    }

    private void deleteRaw(int id) throws SQLException {
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id = ?", new Object[] { id });
    }

    private Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
