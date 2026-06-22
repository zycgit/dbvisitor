package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractAnnotationMapperAttributeContractTest extends AbstractNxnContractTest {
    private static final String PATTERN = "AttrNxn%";

    private AnnotationAttributesMapper mapper;

    @Before
    public void createAnnotationMapper() throws Exception {
        Configuration configuration = newConfiguration();
        Session session = configuration.newSession(dataSource);
        this.mapper = session.createMapper(AnnotationAttributesMapper.class);
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                    new Object[] { baseId() + i, "AttrNxn" + i, 20 + i, "attr-nxn" + i + "@nxn.test" });
        }
    }

    protected int baseId() {
        return 959000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_STATEMENT_TYPE)
    public void annotationAttributes_shouldSupportPreparedAndStatementTypes() throws Exception {
        UserInfo prepared = this.mapper.selectByIdPrepared(baseId() + 1);
        UserInfo statement = this.mapper.selectByIdStatement(baseId() + 2);

        assertNotNull(prepared);
        assertEquals("AttrNxn1", prepared.getName());
        assertNotNull(statement);
        assertEquals("AttrNxn2", statement.getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_TIMEOUT)
    public void annotationAttributes_shouldApplyTimeoutOnQueryAndDml() throws Exception {
        assertEquals("AttrNxn3", this.mapper.selectByIdDefaultTimeout(baseId() + 3).getName());
        assertEquals("AttrNxn4", this.mapper.selectByIdWithTimeout(baseId() + 4).getName());
        assertEquals("AttrNxn5", this.mapper.selectWithMaxTimeout(baseId() + 5).getName());

        assertEquals(1, this.mapper.updateWithTimeout(baseId() + 6, 99));
        assertEquals(Integer.valueOf(99), this.mapper.selectByIdPrepared(baseId() + 6).getAge());
        assertEquals(1, this.mapper.deleteWithTimeout(baseId() + 7));
        assertNull(this.mapper.selectByIdPrepared(baseId() + 7));

        int insertId = explicitId(100);
        assertEquals(1, this.mapper.insertWithTimeout(user(insertId, "AttrTimeoutInsert", 25, "timeout@nxn.test")));
        assertEquals("AttrTimeoutInsert", this.mapper.selectByIdPrepared(insertId).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_FETCH_SIZE)
    public void annotationAttributes_shouldApplyFetchSizeVariantsWithoutChangingResults() throws Exception {
        assertAtLeastSeedRows(this.mapper.selectWithDefaultFetchSize(PATTERN));
        assertAtLeastSeedRows(this.mapper.selectWithSmallFetchSize(PATTERN));
        assertAtLeastSeedRows(this.mapper.selectWithLargeFetchSize(PATTERN));
        assertAtLeastSeedRows(this.mapper.selectWithFetchSizeOne(PATTERN));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_RESULT_SET_TYPE)
    public void annotationAttributes_shouldApplyResultSetTypeVariantsWithoutChangingResults() throws Exception {
        assertAtLeastSeedRows(this.mapper.selectWithDefaultResultSetType(PATTERN));
        assertAtLeastSeedRows(this.mapper.selectWithForwardOnly(PATTERN));
        assertAtLeastSeedRows(this.mapper.selectWithScrollInsensitive(PATTERN));
        assertAtLeastSeedRows(this.mapper.selectWithScrollSensitive(PATTERN));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_GENERATED_KEYS)
    public void annotationAttributes_shouldPopulateGeneratedKeysAndSupportExplicitIds() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        UserInfo generated = user(null, "AttrGeneratedKey", 31, "generated@nxn.test");
        assertNull(generated.getId());

        assertEquals(1, this.mapper.insertWithGeneratedKeyNoKeyColumn(generated));

        assertNotNull(generated.getId());
        assertTrue(generated.getId() > 0);
        assertEquals("AttrGeneratedKey", this.mapper.selectByIdPrepared(generated.getId()).getName());

        int explicitId = explicitId(101);
        UserInfo explicit = user(explicitId, "AttrManualKey", 32, "manual@nxn.test");
        assertEquals(1, this.mapper.insertWithoutGeneratedKey(explicit));
        assertEquals(Integer.valueOf(explicitId), explicit.getId());
        assertEquals("AttrManualKey", this.mapper.selectByIdPrepared(explicitId).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_KEY_COLUMN)
    public void annotationAttributes_shouldPopulateGeneratedKeysWithKeyColumnWhenSupported() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEY_COLUMN);
        UserInfo user = user(null, "AttrKeyColumn", 33, "key-column@nxn.test");

        assertEquals(1, this.mapper.insertWithKeyProperty(user));

        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
        assertEquals("AttrKeyColumn", this.mapper.selectByIdPrepared(user.getId()).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SELECT_KEY)
    public void annotationAttributes_shouldRunSelectKeySqlWhenFixtureSequenceIsSupported() throws Exception {
        requiresNxnFeature(FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE);

        UserInfo before = user(null, "AttrSelectKeyBefore", 34, "select-before@nxn.test");
        assertEquals(1, this.mapper.insertWithSelectKeyBefore(before));
        assertNotNull(before.getId());
        assertEquals("AttrSelectKeyBefore", this.mapper.selectByIdPrepared(before.getId()).getName());

        UserInfo after = user(null, "AttrSelectKeyAfter", 35, "select-after@nxn.test");
        assertEquals(1, this.mapper.insertWithSelectKeyAfter(after));
        assertNotNull(after.getId());
        assertEquals("AttrSelectKeyAfter", this.mapper.selectByIdPrepared(after.getId()).getName());

        UserInfo full = user(null, "AttrSelectKeyFull", 36, "select-full@nxn.test");
        assertEquals(1, this.mapper.insertWithSelectKeyFullAttrs(full));
        assertNotNull(full.getId());
        assertEquals("AttrSelectKeyFull", this.mapper.selectByIdPrepared(full.getId()).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_MULTILINE)
    public void annotationAttributes_shouldJoinMultilineSqlValueArray() throws Exception {
        int insertId = explicitId(102);
        assertEquals(1, this.mapper.insertMultiLine(user(insertId, "AttrMultiLine", 27, "multiline@nxn.test")));

        assertEquals("AttrMultiLine", this.mapper.selectByIdPrepared(insertId).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_COMBINED)
    public void annotationAttributes_shouldSupportCombinedAndDefaultAttributes() throws Exception {
        assertAtLeastSeedRows(this.mapper.selectWithCombinedAttributes(PATTERN));
        assertEquals("AttrNxn1", this.mapper.selectWithAllDefaults(baseId() + 1).getName());
    }

    private void assertAtLeastSeedRows(List<UserInfo> users) {
        assertNotNull(users);
        assertTrue(users.size() >= 10);
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

    private int explicitId(int offset) {
        return -baseId() - offset;
    }
}
