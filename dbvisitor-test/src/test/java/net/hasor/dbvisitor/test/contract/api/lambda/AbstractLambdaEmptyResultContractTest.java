package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractLambdaEmptyResultContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 710000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EMPTY_LIST)
    public void lambdaQueryForList_shouldReturnNonNullEmptyListWhenNoRowsMatch() throws SQLException {
        List<UserInfo> result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 1)//
                .queryForList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EMPTY_MAP_LIST)
    public void lambdaQueryForMapList_shouldReturnNonNullEmptyListWhenNoRowsMatch() throws SQLException {
        List<Map<String, Object>> result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 2)//
                .queryForMapList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EMPTY_COUNT)
    public void lambdaQueryForCount_shouldReturnZeroWhenNoRowsMatch() throws SQLException {
        long count = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 3)//
                .queryForCount();

        assertEquals(0, count);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EMPTY_PAGE)
    public void lambdaPageQuery_shouldReturnEmptyPageWhenNoRowsMatch() throws SQLException {
        Page pageInfo = PageObject.of(1, 10);
        EntityQuery<UserInfo> query = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 4)//
                .usePage(pageInfo);

        List<UserInfo> pageData = query.queryForList();
        Page page = query.pageInfo();

        assertNotNull(pageData);
        assertTrue(pageData.isEmpty());
        assertNotNull(page);
        assertEquals(0, page.getTotalCount());
        assertEquals(0, page.getTotalPage());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EMPTY_STRING_VS_NULL)
    public void lambdaQuery_shouldDistinguishEmptyStringAndNullValues() throws SQLException {
        requiresNxnFeature(FeatureId.DISTINCT_EMPTY_STRING);
        insert(baseId() + 11, "", 25, "empty-string@nxn.test");
        insert(baseId() + 12, null, 26, "null-name@nxn.test");

        List<UserInfo> emptyStringRows = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(baseId() + 11, baseId() + 12))//
                .eq(UserInfo::getName, "")//
                .queryForList();
        List<UserInfo> nullRows = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(baseId() + 11, baseId() + 12))//
                .isNull(UserInfo::getName)//
                .queryForList();

        assertEquals(1, emptyStringRows.size());
        assertEquals(Integer.valueOf(baseId() + 11), emptyStringRows.get(0).getId());
        assertEquals(1, nullRows.size());
        assertEquals(Integer.valueOf(baseId() + 12), nullRows.get(0).getId());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EMPTY_LIKE_EMPTY_STRING)
    public void lambdaLike_shouldApplyEmptyStringPatternConsistently() throws SQLException {
        insert(baseId() + 21, "LikeEmptyOne", 25, "like-empty-1@nxn.test");
        insert(baseId() + 22, "LikeEmptyTwo", 26, "like-empty-2@nxn.test");

        List<UserInfo> result = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(baseId() + 21, baseId() + 22))//
                .like(UserInfo::getName, "")//
                .queryForList();

        assertEquals(2, result.size());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EMPTY_GROUP_BY)
    public void lambdaGroupBy_shouldReturnEmptyListWhenNoRowsMatch() throws SQLException {
        List<Map<String, Object>> result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 31)//
                .applySelect("age, count(*) as cnt")//
                .groupBy("age")//
                .queryForMapList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EMPTY_AGGREGATE)
    public void lambdaAggregate_shouldReturnCountZeroAndNullMaxWhenNoRowsMatch() throws SQLException {
        Long countResult = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 41)//
                .applySelect("count(*)")//
                .queryForObject(Long.class);
        Integer maxResult = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 41)//
                .applySelect("max(age)")//
                .queryForObject(Integer.class);

        assertNotNull(countResult);
        assertEquals(Long.valueOf(0), countResult);
        assertNull(maxResult);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EMPTY_DISTINCT)
    public void lambdaDistinct_shouldReturnEmptyListWhenNoRowsMatch() throws SQLException {
        List<Integer> result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 51)//
                .applySelect("distinct id")//
                .queryForList(Integer.class);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EMPTY_LIMIT_ZERO)
    public void lambdaInitPageZero_shouldMeanNoPaging() throws SQLException {
        insert(baseId() + 61, "NXN-Lambda-Empty-Limit0", 25, "limit-zero@nxn.test");

        List<UserInfo> result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, "NXN-Lambda-Empty-Limit0")//
                .initPage(0, 0)//
                .queryForList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(baseId() + 61), result.get(0).getId());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EMPTY_REVERSED_BETWEEN)
    public void lambdaBetween_shouldReturnEmptyListForReversedRange() throws SQLException {
        insert(baseId() + 71, "NXN-Lambda-Empty-Between", 25, "between@nxn.test");

        List<UserInfo> result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, "NXN-Lambda-Empty-Between")//
                .rangeBetween(UserInfo::getAge, 100, 50)//
                .queryForList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private void insert(int id, String name, Integer age, String email) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }
}
