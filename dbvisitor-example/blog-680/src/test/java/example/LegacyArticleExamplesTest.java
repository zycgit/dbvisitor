package example;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.*;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.page.*;
import net.hasor.dbvisitor.session.*;
import net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler;
import org.junit.Test;
import static org.junit.Assert.*;

/** Historical article snippets exercised against the published 6.8.0 dependency. */
public class LegacyArticleExamplesTest {
    @Table("user_info")
    public static class UserInfo {
        @Column(primary = true)
        private Integer id;
        private String  name;
        private Integer age;
        private String  status;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    @RefMapper("/mapper/legacy-h2.xml")
    public interface Users extends BaseMapper<UserInfo> {
        @Query("SELECT * FROM user_info ORDER BY id")
        List<UserInfo> list(Page page);

        PageResult<UserInfo> page(Page page);

        default List<UserInfo> active(int minAge) throws SQLException {
            return query().eq(UserInfo::getStatus, "ENABLE").gt(UserInfo::getAge, minAge).queryForList();
        }
    }

    private Connection database() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:blog_legacy");
        JdbcTemplate jdbc = new JdbcTemplate(conn);
        jdbc.execute("CREATE TABLE user_info(id INT PRIMARY KEY, name VARCHAR(30), age INT, sex VARCHAR(1), status VARCHAR(20), email VARCHAR(50))");
        jdbc.execute("INSERT INTO user_info VALUES(1001,'Alice',18,'1','ENABLE','a'),(1002,'Bob',20,'0','ENABLE','b'),(1003,'Charlie',30,'1','DISABLE','c')");
        return conn;
    }

    @Test
    public void pairsAcrossApisAndEdgeCases() throws Exception {
        try (Connection conn = database()) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            LambdaTemplate lambda = new LambdaTemplate(conn);
            Map<Integer, String> expected = Map.of(1001, "Alice", 1002, "Bob", 1003, "Charlie");
            assertEquals(expected, lambda.query(UserInfo.class).rangeBetween(UserInfo::getId, 1001, 1003).queryForPairs(UserInfo::getId, UserInfo::getName, Integer.class, String.class));
            assertEquals(expected, lambda.query(UserInfo.class).ge("id", 1001).le("id", 1003).queryForPairs("id", "name", Integer.class, String.class));
            assertEquals(expected, jdbc.queryForPairs("SELECT id,name FROM user_info", Integer.class, String.class));
            assertEquals(expected, jdbc.queryForPairs("SELECT id,name FROM user_info WHERE id>=? AND id<=?", Integer.class, String.class, new Object[] { 1001, 1003 }));
            assertEquals(expected, jdbc.queryForPairs("SELECT id,name FROM user_info WHERE id>=:min AND id<=:max", Integer.class, String.class, Map.of("min", 1001, "max", 1003)));
            Map<Integer, Object> single = jdbc.queryForPairs("SELECT id FROM user_info", Integer.class, Object.class);
            assertEquals(3, single.size());
            assertTrue(single.values().stream().allMatch(Objects::isNull));
            assertEquals(Map.of(1, "Charlie"), jdbc.queryForPairs("SELECT 1,name FROM user_info ORDER BY id", Integer.class, String.class));
        }
    }

    @Test
    public void mapperPaginationAndDefaultMethod() throws Exception {
        try (Connection conn = database(); Session session = new Configuration().newSession(conn)) {
            Users mapper = session.createMapper(Users.class);
            Page page = new PageObject(0, 2);
            assertEquals(2, mapper.list(page).size());
            assertEquals(0, page.getTotalCount()); // List does not request COUNT.
            // Published 6.8.0: use the XML PageResult path shown in the articles.
            PageResult<UserInfo> xmlPage = mapper.page(new PageObject(0, 2));
            assertEquals(3, xmlPage.getTotalCount());
            assertEquals(2, xmlPage.getData().size());
            assertEquals("Alice", xmlPage.getData().get(0).getName());
            // BaseMapper's pageBySample does not count automatically either.
            page = mapper.pageInitBySample(new UserInfo(), 0, 2, 0);
            PageResult<UserInfo> first = mapper.pageBySample(new UserInfo(), page);
            assertEquals(3, first.getTotalCount());
            assertEquals(2, first.getData().size());
            page.nextPage();
            assertEquals(1, mapper.pageBySample(new UserInfo(), page).getData().size());
            assertEquals("Bob", mapper.active(18).get(0).getName());
        }
    }

    @Test
    public void independentDialectBuilders() throws Exception {
        net.hasor.dbvisitor.dialect.SqlCommandBuilder builder = net.hasor.dbvisitor.dialect.provider.MySqlDialect.DEFAULT.newBuilder();
        builder.setTable(null, null, "user_info");
        builder.addSelectAll();
        net.hasor.dbvisitor.dialect.BoundSql sql = builder.buildSelect(true);
        try (Connection conn = database()) {
            assertEquals(3, new JdbcTemplate(conn).queryForList(sql.getSqlString(), sql.getArgs()).size());
        }
        assertNotSame(builder, net.hasor.dbvisitor.dialect.provider.MySqlDialect.DEFAULT.newBuilder());
    }

    @Test
    public void compoundRulesKeepTheWholeExpression() throws Exception {
        try (Connection conn = database()) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            String sql = "SELECT * FROM user_info WHERE status='ENABLE' @{and, ((age = :age and sex = '1') or (name = :name and id in @{in, :ids})) }";
            Map<String, Object> args = new HashMap<>();
            args.put("age", 18);
            args.put("name", "Bob");
            args.put("ids", List.of(1002));
            assertEquals(2, jdbc.queryForList(sql, args).size());
            args.put("age", null);
            assertEquals(1, jdbc.queryForList(sql, args).size());
            args.put("name", null);
            args.put("ids", null);
            assertEquals(2, jdbc.queryForList(sql, args).size());
        }
    }

    @Test
    public void optionalConditionsInSetAndCase() throws Exception {
        try (Connection conn = database()) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            Map<String, Object> args = new HashMap<>();
            args.put("name", "Alice");
            args.put("age", null);
            assertEquals(1, jdbc.queryForList("SELECT * FROM user_info @{and, name=:name} @{and, age=:age}", args).size());
            args.put("name", "");
            assertEquals(0, jdbc.queryForList("SELECT * FROM user_info @{and, name=:name}", args).size());
            assertEquals(2, jdbc.queryForList("SELECT * FROM user_info @{ifand, !showAll, status='ENABLE'}", Map.of("showAll", false)).size());
            String in = "SELECT * FROM user_info @{ifand, idList != null && !idList.isEmpty(), id IN @{in, :idList}}";
            assertEquals(1, jdbc.queryForList(in, Map.of("idList", List.of(1001))).size());
            assertEquals(3, jdbc.queryForList(in, Map.of("idList", List.of())).size());
            args.put("name", null);
            args.put("age", 22);
            args.put("id", 1001);
            jdbc.executeUpdate("UPDATE user_info @{set, name=:name} @{set, age=:age} WHERE id=:id", args);
            assertNull(jdbc.queryForString("SELECT name FROM user_info WHERE id=1001"));
            args.put("name", "Restored");
            args.put("age", null);
            jdbc.executeUpdate("UPDATE user_info SET @{if, name != null, @{set, name=:name}} @{if, age != null, @{set, age=:age}} WHERE id=:id", args);
            assertEquals(Integer.valueOf(22), jdbc.queryForInt("SELECT age FROM user_info WHERE id=1001"));
            String branch = "SELECT * FROM user_info @{ifand, true, @{case, , @{when, name != null, name=#{name}}, @{when, age != null, age=#{age}}, @{else, status='ENABLE'}}}";
            assertEquals(1, jdbc.queryForList(branch, args).size());
            args.put("name", null);
            assertEquals(2, jdbc.queryForList(branch, args).size());
        }
    }

    @Test
    public void julianDateRoundTrip() throws Exception {
        try (Connection conn = database()) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            jdbc.execute("CREATE TABLE events(id INT PRIMARY KEY,julian_day BIGINT)");
            LocalDate date = LocalDate.of(-99, 1, 1);
            jdbc.executeUpdate("INSERT INTO events VALUES(#{id},#{date,typeHandler=net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler})", Map.of("id", 1, "date", date));
            assertEquals(Long.valueOf(1684901L), jdbc.queryForLong("SELECT julian_day FROM events"));
            assertEquals(date, jdbc.queryForObject("SELECT julian_day FROM events WHERE id=?", new Object[] { 1 }, (rs, row) -> new JulianDayTypeHandler().getResult(rs, "julian_day")));
        }
    }
}
