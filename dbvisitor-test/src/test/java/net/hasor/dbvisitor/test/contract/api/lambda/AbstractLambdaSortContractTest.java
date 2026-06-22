package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractLambdaSortContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 850000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SORT_MULTI_COLUMN)
    public void lambdaSort_shouldHonorMultiColumnPriorityAndMixedDirections() throws SQLException {
        insertUser(baseId() + 1, "Charlie", 25, "sort-a@test.com");
        insertUser(baseId() + 2, "Alice", 25, "sort-a@test.com");
        insertUser(baseId() + 3, "Bob", 25, "sort-b@test.com");
        insertUser(baseId() + 4, "David", 30, "sort-c@test.com");
        insertUser(baseId() + 5, "Eve", 20, "sort-d@test.com");

        List<UserInfo> ageDescNameAsc = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(1, 2, 3, 4, 5))//
                .desc("age")//
                .asc("name")//
                .queryForList();
        assertNames(ageDescNameAsc, "David", "Alice", "Bob", "Charlie", "Eve");

        List<UserInfo> ageDescEmailAscNameAsc = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(1, 2, 3, 4))//
                .desc("age")//
                .asc("email")//
                .asc("name")//
                .queryForList();
        assertNames(ageDescEmailAscNameAsc, "David", "Alice", "Charlie", "Bob");

        List<UserInfo> ageAscNameDesc = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(1, 2, 3, 4, 5))//
                .asc("age")//
                .desc("name")//
                .queryForList();
        assertNames(ageAscNameDesc, "Eve", "Charlie", "Bob", "Alice", "David");
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SORT_NULL_ORDERING)
    public void lambdaSort_shouldExposeDatasourceNullOrderingForMultiColumnSorts() throws SQLException {
        insertUser(baseId() + 11, "Alpha", 25, "null-sort-a@test.com");
        insertUser(baseId() + 12, "Beta", null, "null-sort-b@test.com");
        insertUser(baseId() + 13, "Gamma", 30, "null-sort-c@test.com");
        insertUser(baseId() + 14, "Delta", null, "null-sort-d@test.com");

        List<UserInfo> rows = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(11, 12, 13, 14))//
                .desc("age")//
                .asc("name")//
                .queryForList();
        assertEquals(4, rows.size());

        if (profile().id() == DataSourceId.PG || profile().id() == DataSourceId.ORACLE || profile().id() == DataSourceId.DB2) {
            assertNull(rows.get(0).getAge());
            assertNull(rows.get(1).getAge());
            assertNames(rows, "Beta", "Delta", "Gamma", "Alpha");
        } else {
            assertNames(rows, "Gamma", "Alpha", "Beta", "Delta");
            assertNull(rows.get(2).getAge());
            assertNull(rows.get(3).getAge());
        }
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SORT_REPEATED_COLUMN)
    public void lambdaSort_shouldAppendRepeatedColumnOrderingWithoutOverridingPrimaryOrder() throws SQLException {
        insertUser(baseId() + 21, "User1", 20, "repeat-sort-1@test.com");
        insertUser(baseId() + 22, "User2", 25, "repeat-sort-2@test.com");
        insertUser(baseId() + 23, "User3", 30, "repeat-sort-3@test.com");

        List<UserInfo> rows = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(21, 22, 23))//
                .asc("age")//
                .desc("age")//
                .queryForList();

        assertEquals(3, rows.size());
        assertTrue(rows.get(0).getAge() <= rows.get(1).getAge());
        assertTrue(rows.get(1).getAge() <= rows.get(2).getAge());
    }

    private void insertUser(int id, String name, Integer age, String email) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }

    private List<Integer> ids(int... offsets) {
        return Arrays.stream(offsets).map(offset -> baseId() + offset).boxed().collect(Collectors.toList());
    }

    private void assertNames(List<UserInfo> rows, String... names) {
        assertEquals(names.length, rows.size());
        for (int i = 0; i < names.length; i++) {
            assertEquals(names[i], rows.get(i).getName());
        }
    }
}
