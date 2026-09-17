package example;

import java.io.InputStream;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.jdbc.*;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.mapping.*;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.page.*;
import net.hasor.dbvisitor.session.*;
import net.hasor.dbvisitor.types.handler.time.PgDateTypeHandler;
import net.hasor.dbvisitor.types.handler.vector.PgVectorTypeHandler;
import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.*;

/** Explicit opt-in: -Dblog.jdbc.config.dir=/path/to/test-connection-properties. */
public class LegacyDatabaseArticlesTest {
    private Connection connection(String name) throws Exception {
        String directory = System.getProperty("blog.jdbc.config.dir");
        Assume.assumeTrue("Set blog.jdbc.config.dir for real database article tests", directory != null);
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(Path.of(directory, "jdbc-" + name + ".properties"))) {
            p.load(in);
        }
        return DriverManager.getConnection(p.getProperty("jdbc.url"), p.getProperty("jdbc.username", ""), p.getProperty("jdbc.password", ""));
    }

    @Test
    public void mysqlStreamingCallback() throws Exception {
        try (Connection conn = connection("mysql")) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            jdbc.execute("CREATE TEMPORARY TABLE blog_stream_users(id INT PRIMARY KEY,name VARCHAR(30),age INT)");
            jdbc.execute("INSERT INTO blog_stream_users VALUES(1,'Alice',18),(2,'Bob',20)");
            PreparedStatementCreator creator = c -> {
                PreparedStatement ps = c.prepareStatement("SELECT id,name,age FROM blog_stream_users ORDER BY id", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                ps.setFetchSize(Integer.MIN_VALUE);
                return ps;
            };
            BeanMappingRowMapper<LegacyArticleExamplesTest.UserInfo> mapper = new BeanMappingRowMapper<>(LegacyArticleExamplesTest.UserInfo.class);
            AtomicInteger rows = new AtomicInteger();
            jdbc.executeCreator(creator, (RowCallbackHandler) (rs, n) -> {
                assertNotNull(mapper.mapRow(rs, n).getName());
                rows.incrementAndGet();
            });
            assertEquals(2, rows.get());
            assertEquals(Integer.valueOf(1), jdbc.queryForInt("SELECT 1")); // ResultSet has been released.
        }
    }

    @Test
    public void postgresDateHandlerReadAndWrite() throws Exception {
        try (Connection conn = connection("pg")) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            jdbc.execute("CREATE TEMPORARY TABLE blog_date_events(id INT,event_date DATE)");
            LocalDate date = LocalDate.of(-99, 1, 1);
            jdbc.executeUpdate("INSERT INTO blog_date_events VALUES(#{id},#{date,typeHandler=net.hasor.dbvisitor.types.handler.time.PgDateTypeHandler})", Map.of("id", 1, "date", date));
            assertEquals("0100-01-01 BC", jdbc.queryForString("SELECT event_date::text FROM blog_date_events"));
            assertEquals(date, jdbc.queryForObject("SELECT event_date FROM blog_date_events", (rs, n) -> new PgDateTypeHandler().getResult(rs, "event_date")));
        }
    }

    @Table("blog_vector_products")
    public static class Product {
        @Column(primary = true)
        private Integer     id;
        @Column(typeHandler = PgVectorTypeHandler.class)
        private List<Float> embedding;
        private Integer     price;
        private String      category;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public List<Float> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Float> value) {
            embedding = value;
        }

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer value) {
            price = value;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String value) {
            category = value;
        }
    }

    @Test
    public void pgvectorFieldHandlerAlsoBindsOrdering() throws Exception {
        try (Connection conn = connection("pg")) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            jdbc.execute("CREATE TEMPORARY TABLE blog_vector_products(id INT PRIMARY KEY, embedding vector(3),price INT,category TEXT)");
            LambdaTemplate lambda = new LambdaTemplate(conn);
            Product p = new Product();
            p.setId(1);
            p.setPrice(299);
            p.setCategory("electronics");
            p.setEmbedding(List.of(.1f, .2f, .3f));
            assertEquals(1, lambda.insert(Product.class).applyEntity(p).executeSumResult());
            assertEquals(p.getEmbedding(), lambda.query(Product.class).eq(Product::getId, 1).queryForObject().getEmbedding());
            List<Float> query = List.of(.15f, .25f, .35f);
            assertEquals(1, lambda.query(Product.class).orderByL2(Product::getEmbedding, query).initPage(5, 0).queryForList().size());
            assertEquals(1, lambda.query(Product.class).orderByCosine(Product::getEmbedding, query).initPage(5, 0).queryForList().size());
            assertEquals(1, lambda.query(Product.class).orderByIP(Product::getEmbedding, query).initPage(5, 0).queryForList().size());
            assertEquals(1, lambda.query(Product.class).orderByMetric(MetricType.L2, Product::getEmbedding, query).initPage(10, 0).queryForList().size());
            assertEquals(1, lambda.query(Product.class).rangeBetween(Product::getPrice, 100, 500).eq(Product::getCategory, "electronics").orderByL2(Product::getEmbedding, query).initPage(10, 0).queryForList().size());
        }
    }

    @Table("blog_legacy_users")
    public static class User {
        @Column(value = "_id", primary = true)
        private String  id;
        private String  name;
        private Integer age;

        public String getId() {
            return id;
        }

        public void setId(String id) {
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
    }

    @RefMapper("/mapper/legacy-mongo.xml")
    public interface MongoUsers extends BaseMapper<User> {
        @Insert("db.blog_legacy_users.insertOne({_id: #{info.id}, name: #{info.name}, age: #{info.age}})")
        int saveUser(@Param("info") User info);

        @Query("db.blog_legacy_users.find({_id: #{id}})")
        User loadById(@Param("id") String id);

        @Delete("db.blog_legacy_users.remove({_id: #{id}})")
        int deleteUser(@Param("id") String id);

        PageResult<User> listByUserName(@Param("userName") String name, Page page);
    }

    @Test
    public void mongoMapperXmlAndBuilder() throws Exception {
        try (Connection conn = connection("mongo")) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            // Fail if present; never drop a collection owned by another run/user.
            jdbc.execute("db.createCollection('blog_legacy_users')");
            try {
                Session session = new Configuration().newSession(conn);
                MongoUsers mapper = session.createMapper(MongoUsers.class);
                User user = new User();
                user.setId("blog-1");
                user.setName("mali");
                user.setAge(18);
                assertEquals(1, mapper.saveUser(user));
                assertEquals("mali", mapper.loadById("blog-1").getName());
                user.setId("blog-2");
                assertEquals(1, mapper.insert(user));
                PageResult<User> result = mapper.listByUserName("mali", new PageObject(0, 1));
                assertEquals(2, result.getTotalCount());
                assertEquals(1, result.getData().size());
                assertEquals(1, mapper.update().eq(User::getId, "blog-1").updateTo(User::getAge, 27).doUpdate());
                assertEquals(Integer.valueOf(27), mapper.selectById("blog-1").getAge());
                assertEquals(1, mapper.deleteUser("blog-1"));
                assertTrue(conn.isWrapperFor(com.mongodb.client.MongoClient.class));
            } finally {
                jdbc.execute("db.blog_legacy_users.drop()");
            }
        }
    }

    @RefMapper("/mapper/legacy-elastic.xml")
    public interface ElasticUsers extends BaseMapper<User> {
        @Insert(value = "POST /blog_legacy_users/_doc\\?refresh=wait_for {\"name\":#{info.name},\"age\":#{info.age}}", useGeneratedKeys = true, keyProperty = "id", keyColumn = "_id")
        int saveUser(@Param("info") User info);

        @Query("POST /blog_legacy_users/_search {\"query\": {\"term\": {\"_id\": #{id}}}}")
        User loadById(@Param("id") String id);

        @Delete("DELETE /blog_legacy_users/_doc/{#{id}}\\?refresh=wait_for")
        int deleteUser(@Param("id") String id);

        PageResult<User> listByUserName(@Param("userName") String name, Page page);
    }

    @Test
    public void elasticMapperGeneratedIdXmlAndBuilder() throws Exception {
        try (Connection conn = connection("es7")) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            jdbc.execute("PUT /blog_legacy_users {\"mappings\":{\"properties\":{\"name\":{\"type\":\"keyword\"},\"age\":{\"type\":\"integer\"}}}}");
            try {
                Session session = new Configuration().newSession(conn);
                ElasticUsers mapper = session.createMapper(ElasticUsers.class);
                User user = new User();
                user.setName("mali");
                user.setAge(18);
                assertEquals(1, mapper.saveUser(user));
                assertNotNull(user.getId());
                assertEquals("mali", mapper.loadById(user.getId()).getName());
                PageResult<User> result = mapper.listByUserName("mali", new PageObject(0, 10));
                assertEquals(1, result.getTotalCount());
                assertEquals(1, result.getData().size());
                assertEquals(1, mapper.update().eq(User::getId, user.getId()).updateTo(User::getAge, 27).doUpdate());
                assertEquals(Integer.valueOf(27), mapper.selectById(user.getId()).getAge());
                assertEquals(1, mapper.deleteUser(user.getId()));
                assertTrue(conn.isWrapperFor(org.elasticsearch.client.RestClient.class));
            } finally {
                jdbc.execute("DELETE /blog_legacy_users");
            }
        }
    }
}
