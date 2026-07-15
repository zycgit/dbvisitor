package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ParameterBindingMapper;
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

@NxnContract
public abstract class AnnMapperParameterBindingTest extends AbstractNxnContractTest {
    private ParameterBindingMapper mapper;

    @Before
    public void createAnnMapper() throws Exception {
        Configuration configuration = newConfiguration();
        Session session = configuration.newSession(dataSource);
        this.mapper = session.createMapper(ParameterBindingMapper.class);
    }

    protected int baseId() {
        return 950000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_POSITIONAL)
    public void positionalParamsByDeclarationOrder() throws Exception {
        int id = baseId() + 1;
        assertEquals(1, this.mapper.insertByPosition(id, "AnnoPositional", 28));
        assertEquals(1, this.mapper.updateByPosition(29, id));

        UserInfo loaded = this.mapper.selectById(id);
        assertNotNull(loaded);
        assertEquals("AnnoPositional", loaded.getName());
        assertEquals(Integer.valueOf(29), loaded.getAge());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_NAMED)
    public void namedParamsByParamAnnAndSupportNulls() throws Exception {
        requiresNxnFeature(FeatureId.DISTINCT_EMPTY_STRING);
        int fullId = baseId() + 11;
        int nullId = baseId() + 12;
        int emptyId = baseId() + 13;
        int specialId = baseId() + 14;
        String specialName = "Anno'Quote\"Double\\Slash";

        assertEquals(1, this.mapper.insertWithParam(fullId, "AnnoNamed", 31, "named@nxn.test"));
        assertEquals(1, this.mapper.insertWithParam(nullId, "AnnoNamedNull", null, null));
        assertEquals(1, this.mapper.insertWithParam(emptyId, "", 28, ""));
        assertEquals(1, this.mapper.insertWithParam(specialId, specialName, 29, "special@nxn.test"));

        UserInfo full = this.mapper.selectById(fullId);
        UserInfo nulls = this.mapper.selectById(nullId);
        UserInfo empty = this.mapper.selectById(emptyId);
        UserInfo special = this.mapper.selectById(specialId);
        assertEquals("AnnoNamed", full.getName());
        assertEquals(Integer.valueOf(31), full.getAge());
        assertEquals("named@nxn.test", full.getEmail());
        assertNull(nulls.getAge());
        assertNull(nulls.getEmail());
        assertEquals("", empty.getName());
        assertEquals("", empty.getEmail());
        assertEquals(specialName, special.getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_BEAN)
    public void beanParamExpandProperties() throws Exception {
        int id = baseId() + 21;
        UserInfo user = user(id, "AnnoBean", 32, "bean@nxn.test");

        assertEquals(1, this.mapper.insertBean(user));

        UserInfo loaded = this.mapper.selectById(id);
        assertNotNull(loaded);
        assertEquals("AnnoBean", loaded.getName());
        assertEquals(Integer.valueOf(32), loaded.getAge());
        assertNotNull(loaded.getCreateTime());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_MAP)
    public void mapParamByMapKeys() throws Exception {
        int id = baseId() + 31;
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("name", "AnnoMap");
        params.put("age", 33);
        params.put("email", "map@nxn.test");

        assertEquals(1, this.mapper.insertByMap(params));

        UserInfo loaded = this.mapper.selectById(id);
        assertEquals("AnnoMap", loaded.getName());
        assertEquals(Integer.valueOf(33), loaded.getAge());
        assertEquals("map@nxn.test", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_MIXED)
    public void mixedParamsNestedBeanAndScalarParams() throws Exception {
        int id = baseId() + 41;
        UserInfo user = user(id, "AnnoMixed", 34, null);

        assertEquals(1, this.mapper.insertMixed(user, "mixed@nxn.test"));

        UserInfo loaded = this.mapper.selectById(id);
        assertEquals("AnnoMixed", loaded.getName());
        assertEquals(Integer.valueOf(34), loaded.getAge());
        assertEquals("mixed@nxn.test", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_REUSE)
    public void reusedParamSameValueInMultiPlaces() throws Exception {
        int id = baseId() + 51;

        assertEquals(1, this.mapper.insertWithReuse(id, "AnnoReuse"));

        UserInfo loaded = this.mapper.selectById(id);
        assertEquals("AnnoReuse", loaded.getName());
        assertEquals("AnnoReuse", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_MANY)
    public void manyParamsReferencedParamsAndIgnoreExtraParams() throws Exception {
        int id = baseId() + 61;

        assertEquals(1, this.mapper.insertWithManyParams(id, "AnnoMany", 35, "many@nxn.test", new Date(), "extra1", "extra2"));

        UserInfo loaded = this.mapper.selectById(id);
        assertEquals("AnnoMany", loaded.getName());
        assertEquals(Integer.valueOf(35), loaded.getAge());
        assertEquals("many@nxn.test", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_RANGE)
    public void rangeQueryMultiNamedParams() throws Exception {
        this.mapper.insertWithParam(baseId() + 71, "AnnoRange1", 20, "r1@nxn.test");
        this.mapper.insertWithParam(baseId() + 72, "AnnoRange2", 25, "r2@nxn.test");
        this.mapper.insertWithParam(baseId() + 73, "AnnoRange3", 30, "r3@nxn.test");

        List<UserInfo> users = this.mapper.selectByAgeRange(20, 30);

        assertTrue(users.size() >= 3);
        for (UserInfo user : users) {
            assertTrue(user.getAge() >= 20 && user.getAge() <= 30);
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_IN_LIST)
    public void inRuleExpandArrayAndListParams() throws Exception {
        this.mapper.insertWithParam(baseId() + 81, "AnnoInArray1", 61, "a1@nxn.test");
        this.mapper.insertWithParam(baseId() + 82, "AnnoInArray2", 62, "a2@nxn.test");
        this.mapper.insertWithParam(baseId() + 83, "AnnoInList1", 71, "l1@nxn.test");
        this.mapper.insertWithParam(baseId() + 84, "AnnoInList2", 72, "l2@nxn.test");

        List<UserInfo> arrayUsers = this.mapper.selectByAgesArray(new Integer[] { 61, 62 });
        List<UserInfo> listUsers = this.mapper.selectByAgesList(Arrays.asList(71, 72));

        assertEquals(2, arrayUsers.size());
        assertEquals(2, listUsers.size());
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
