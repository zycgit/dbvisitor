package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationTestMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public abstract class AnnMapperCrudTest extends AbstractNxnContractTest {
    private AnnotationTestMapper mapper;

    @Before
    public void createAnnMapper() throws Exception {
        Configuration configuration = newConfiguration();
        Session session = configuration.newSession(dataSource);
        this.mapper = session.createMapper(AnnotationTestMapper.class);
    }

    protected int baseId() {
        return 48000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_INSERT)
    public void annotationMapperInsertBeanParam() throws Exception {
        UserInfo user = user(baseId() + 1, "AnnoInsert", 25, "insert@test.com");
        user.setCreateTime(new Date());

        int result = this.mapper.insertUser(user);
        UserInfo loaded = this.mapper.selectById(baseId() + 1);

        assertEquals(1, result);
        assertNotNull(loaded);
        assertEquals("AnnoInsert", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
        assertEquals("insert@test.com", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_INSERT)
    public void annotationMapperInsertNamedParamsAndMultilineSql() throws Exception {
        int first = this.mapper.insertUserWithParams(baseId() + 2, "AnnoParams", 28, "params@test.com");
        int second = this.mapper.insertUserMultiLine(user(baseId() + 3, "AnnoMultiline", 27, "multi@test.com"));

        assertEquals(1, first);
        assertEquals(1, second);
        assertEquals("AnnoParams", this.mapper.selectById(baseId() + 2).getName());
        assertEquals("AnnoMultiline", this.mapper.selectById(baseId() + 3).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_UPDATE)
    public void annotationMapperUpdateSingleAndMultiFields() throws Exception {
        this.mapper.insertUserWithParams(baseId() + 4, "AnnoUpdate", 30, "update@test.com");

        assertEquals(1, this.mapper.updateUserAge(baseId() + 4, 35));
        assertEquals(Integer.valueOf(35), this.mapper.selectById(baseId() + 4).getAge());

        assertEquals(1, this.mapper.updateUserInfo(baseId() + 4, "AnnoUpdated", 36));
        UserInfo loaded = this.mapper.selectById(baseId() + 4);
        assertEquals("AnnoUpdated", loaded.getName());
        assertEquals(Integer.valueOf(36), loaded.getAge());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_DELETE)
    public void annotationMapperDeleteByIdAndCond() throws Exception {
        this.mapper.insertUserWithParams(baseId() + 5, "AnnoDeleteOne", 41, "d1@test.com");
        this.mapper.insertUserWithParams(baseId() + 6, "AnnoDeleteTwo", 42, "d2@test.com");
        this.mapper.insertUserWithParams(baseId() + 7, "AnnoDeleteThree", 42, "d3@test.com");

        assertEquals(1, this.mapper.deleteById(baseId() + 5));
        assertNull(this.mapper.selectById(baseId() + 5));

        int deletedByAge = this.mapper.deleteByAge(42);
        if (profile().supportsFeature(FeatureId.EXACT_MUTATION_AFFECTED_ROWS)) {
            assertEquals(2, deletedByAge);
        }
        assertEquals(0, this.mapper.countByAge(42));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_QUERY)
    public void annotationMapperQueryObjectListAndScalar() throws Exception {
        this.mapper.insertUserWithParams(baseId() + 8, "AnnoQueryOne", 51, "q1@test.com");
        this.mapper.insertUserWithParams(baseId() + 9, "AnnoQueryTwo", 51, "q2@test.com");
        this.mapper.insertUserWithParams(baseId() + 10, "OtherQuery", 52, "q3@test.com");

        List<UserInfo> byAge = this.mapper.selectByAge(51);
        List<UserInfo> byName = this.mapper.selectByNameLike("AnnoQuery%");

        assertEquals(2, byAge.size());
        assertEquals(2, this.mapper.countByAge(51));
        assertEquals(2, byName.size());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_DYNAMIC_IN)
    public void annotationMapperDynamicInExpandArrayParam() throws Exception {
        this.mapper.insertUserWithParams(baseId() + 11, "AnnoInOne", 61, "in1@test.com");
        this.mapper.insertUserWithParams(baseId() + 12, "AnnoInTwo", 62, "in2@test.com");
        this.mapper.insertUserWithParams(baseId() + 13, "AnnoInThree", 63, "in3@test.com");

        List<UserInfo> users = this.mapper.selectByAgeIn(new Integer[] { 61, 63 });

        assertEquals(2, users.size());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_EXECUTE)
    public void annotationMapperExecuteDdlAndDml() throws Exception {
        dropTableIfExists("temp_anno_test");
        jdbcTemplate.executeUpdate(createSimpleTempTableSql("temp_anno_test"));
        this.mapper.insertTempData(1, "temp-one");

        assertEquals("temp-one", this.mapper.selectTempData(1));

        dropTableIfExists("temp_anno_test");
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
