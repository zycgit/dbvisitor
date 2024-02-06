介绍
------------------------------------
``Documents are translated using translation software, The original for README.md``

* Project Home: [https://www.dbvisitor.net](https://www.dbvisitor.net)
* [![QQ群:948706820](https://img.shields.io/badge/QQ%E7%BE%A4-948706820-orange)](https://qm.qq.com/cgi-bin/qm/qr?k=Qy3574A4VgI0ph4fqFbZW-w49gnyqu6p&jump_from=webapi)
  [![zyc@byshell.org](https://img.shields.io/badge/Email-zyc%40byshell.org-blue)](mailto:zyc@byshell.org)
  [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.hasor/dbvisitor/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.hasor/dbvisitor)

&emsp;&emsp;dbVisitor is a database orm tool, Providing object mapping,Richer type handling than Mybatis,
Dynamic SQL, stored procedures, more dialect 20+, nested transactions, multiple data sources, conditional constructors,
INSERT strategies, multiple statements/multiple results. And compatible with Spring and MyBatis usage.

It doesn't depend on any other framework, so it can be easily integrated with any framework.

Features
------------------------------------

- Familiar
  - JdbcTemplate（like Spring JDBC）
  - Mapper files（Compatible with most MyBatis）
  - LambdaTemplate （Close to the MyBatis Plus、jOOQ and BeetlSQL）
  - @Insert、@Update、@Delete、@Query、@Callable (like JPA)

- Transaction support
  - Support for 5 transaction isolation levels, 7 transaction propagation behaviors (same as Spring TX)
  - provides TransactionTemplate and TransactionManager transaction control (same usage as Spring)

- Feature
  - Support for paging queries and multiple database dialects (20+)
  - Support for INSERT strategies (INTO, UPDATE, IGNORE)
  - Richer TypeHandler（MyBatis 40+，dbVisitor 60+）
  - Mapper file supports multiple statements and multiple results
  - provides special '@{XXX, expr, XXXXX}' rule extension mechanism to make dynamic SQL simpler
  - Support for stored procedures
  - Supports time types in JDBC 4.2 and Java8
  - Support for multiple data sources


Quick Start
------------------------------------
dependency

```xml
<dependency>
  <groupId>net.hasor</groupId>
  <artifactId>dbvisitor</artifactId>
  <version>5.4.1</version><!-- 查看最新版本：https://mvnrepository.com/artifact/net.hasor/dbvisitor -->
</dependency>
```

database drivers, for example:

```xml
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
  <version>8.0.22</version>
</dependency>
```

dbVisitor can be used without relying on database connection pools, 
but having a database connection pool is standard for most projects. HikariCP

```xml
<dependency>
  <groupId>com.zaxxer</groupId>
  <artifactId>HikariCP</artifactId>
  <version>4.0.3</version>
</dependency>
```

Finally, prepare a database table and initialize some data ('createdB.sql' file)

```sql
drop table if exists `test_user`;
create table `test_user` (
    `id`          int(11) auto_increment,
    `name`        varchar(255),
    `age`         int,
    `create_time` datetime,
    primary key (`id`)
);

insert into `test_user` values (1, 'mali', 26, now());
insert into `test_user` values (2, 'dative', 32, now());
insert into `test_user` values (3, 'jon wes', 41, now());
insert into `test_user` values (4, 'mary', 66, now());
insert into `test_user` values (5, 'matt', 25, now());
```

### using SQL

```java
// creating a data source
DataSource dataSource = DsUtils.dsMySql();

// create JdbcTemplate object
JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

// load the test data script
jdbcTemplate.loadSQL("CreateDB.sql");

// Query the data and return it as a Map
List<Map<String, Object>> mapList = jdbcTemplate.queryForList("select * from test_user");
```

print 'mapList' to the console

```text
/--------------------------------------------\
| id | name    | age | create_time           |
|--------------------------------------------|
| 1  | mali    | 26  | 2021-11-12 19:14:06.0 |
| 2  | dative  | 32  | 2021-11-12 19:14:06.0 |
| 3  | jon wes | 41  | 2021-11-12 19:14:06.0 |
| 4  | mary    | 66  | 2021-11-12 19:14:06.0 |
| 5  | matt    | 25  | 2021-11-12 19:14:06.0 |
\--------------------------------------------/
```

To receive data using a DTO object, you need to create a DTO object

```java
// Declare it with the @Table annotation
// - 'test_user' converts the hump to get the class name 'TestUser'
@Table(mapUnderscoreToCamelCase = true)
  public class TestUser {
  private Integer id;
  private String  name;
  private Integer age;
  private Date    createTime;
  
  // getters and setters omitted
}
```

Then use the 'queryForList' method to query directly, and the console can get the same result

```java
List<TestUser> dtoList = jdbcTemplate.queryForList("select * from test_user", TestUser.class);
```

### using CURD

for single-table CURD operations, you can use a subclass of JdbcTemplate, LambdaTemplate

```java
// creating a data source
DataSource dataSource = DsUtils.dsMySql();

// create LambdaTemplate object
LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);

// Query, all data
List<TestUser> dtoList = lambdaTemplate.lambdaQuery(TestUser.class)
                .queryForList();

// Insert new data
TestUser newUser = new TestUser();
newUser.setName("new User");
newUser.setAge(33);
newUser.setCreateTime(new Date());

int result = lambdaTemplate.lambdaInsert(TestUser.class)
                .applyEntity(newUser)
                .executeSumResult();

// Update, update name from Mali to mala
TestUser sample = new TestUser();
sample.setName("mala");

int result = lambdaTemplate.lambdaUpdate(TestUser.class)
                .eq(TestUser::getId, 1)
                .updateBySample(sample)
                .doUpdate();

// Delete data whose ID is 2
int result = lambdaTemplate.lambdaUpdate(TestUser.class)
                .eq(TestUser::getId, 2)
                .doDelete();
```

### Common Mapper

The generic Mapper interface does some basic operations, still using single-table CRUD as an example.

```java
// creating a data source
DataSource dataSource = DsUtils.dsMySql();

// Creating a BaseMapper
DalSession session = new DalSession(dataSource);
BaseMapper<TestUser> baseMapper = session.createBaseMapper(TestUser.class);

// Query, all data
List<TestUser> dtoList = baseMapper.query().queryForList();

// Insert new data
TestUser newUser = new TestUser();
newUser.setName("new User");
newUser.setAge(33);
newUser.setCreateTime(new Date());

int result = baseMapper.insert(newUser);

// Update, update name from Mali to mala
TestUser sample = baseMapper.queryById(1);
sample.setName("mala");

int result = baseMapper.updateById(sample);

// Delete data whose ID is 2
int result = baseMapper.deleteById(2);
```

### Annotate Mapper

As Mapper, you can define your own methods and execute SQL statements through annotations.

```java
// BaseMapper is optional, and inheriting it is equivalent to adding an extended set of single-table curds.
@SimpleMapper
public interface TestUserMapper extends BaseMapper<TestUser> {
  @Insert("insert into `test_user` (name,age,create_time) values (#{name}, #{age}, now())")
  int insertUser(@Param("name") String name, @Param("age") int age);
  
  @Update("update `test_user` set age = #{age} where id = #{id}")
  int updateAge(@Param("id") int userId, @Param("age") int newAge);
  
  @Delete("delete from `test_user` where age > #{age}")
  int deleteByAge(@Param("age") int age);
  
  @Query(value = "select * from `test_user` where  #{beginAge} < age and age < #{endAge}", resultType = TestUser.class)
  List<TestUser> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);
}
```

```java
// Create a Session
DalSession session = new DalSession(dataSource);

// Creating the Mapper Interface
TestUserMapper userMapper = session.createMapper(TestUserMapper.class);
```

### ### Manage SQL using XML

The best place for unified SQL management is still Mapper files, and dbVisitor Mapper files are highly compatible with MyBatis at a very low learning cost.

```java
// Use the @RefMapper to associate Mapper files with interface classes (extends from BaseMapper is optional)
@RefMapper("/mapper/quick_dao3/TestUserMapper.xml")
public interface TestUserDAO extends BaseMapper<TestUser> {
    int insertUser(@Param("name") String name, @Param("age") int age);

    int updateAge(@Param("id") int userId, @Param("age") int newAge);

    int deleteByAge(@Param("age") int age);

    List<TestUser> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);
}
```

In order to better understand and use dbVisitor Mapper files, it is recommended to add DTD validation.
In addition dbVisitor compatible with MyBatis3 DTD for most of the MyBatis project can be normally compatible.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="com.example.demo.quick.dao3.TestUserDAO">
  <resultMap id="testuser_resultMap" type="com.example.demo.quick.dao3.TestUser">
    <result column="id" property="id"/>
    <result column="name" property="name"/>
    <result column="age" property="age"/>
    <result column="create_time" property="createTime"/>
  </resultMap>

  <sql id="testuser_columns">
    name,age,create_time
  </sql>

  <insert id="insertUser">
    insert into `test_user` (
      <include refid="testuser_columns"/>
    ) values (
      #{name}, #{age}, now()
    )
  </insert>

  <update id="updateAge">
    update `test_user` set age = #{age} where id = #{id}
  </update>

  <delete id="deleteByAge"><![CDATA[
        delete from `test_user` where age > #{age}
    ]]></delete>

  <select id="queryByAge" resultMap="testuser_resultMap">
    select id,<include refid="testuser_columns"/>
    from `test_user`
    where  #{beginAge} &lt; age and age &lt; #{endAge}
  </select>

  <select id="queryAll" resultMap="testuser_resultMap">
    select id,<include refid="testuser_columns"/>
    from `test_user`
  </select>
</mapper>
```

### Simplify XML configuration with rules

The following statement


```xml title='XML configuration with if/foreach'
<select id="queryUser">
    select * from `test_user`
    where 1 = 1
    <if test="age != null">
        and age = #{age}
    </if>
    and id in <foreach item="item" index="index" collection="list"
             open="(" separator="," close=")">
      #{item}
    </foreach>
  #{item}
</select>
```

```xml title='Use AND rules instead of if and foreach'
<select id="queryUser">
    select * from `test_user`
    @{and, age = :age}
    @{and, id in (:list)}
</select>
```

### Paging query

Use 'LambdaTemplate' for paging queries

```java
DataSource dataSource = ...
LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);

// Build a paging object with 3 pieces of data per page (default first page is 0)
Page pageInfo = new PageObject();
pageInfo.setPageSize(3);

// paging query data
List<TestUser> pageData1 = lambdaTemplate.lambdaQuery(TestUser.class)
    .usePage(pageInfo)
    .queryForList();

// query next page
pageInfo.nextPage();
List<TestUser> pageData2 = lambdaTemplate.lambdaQuery(TestUser.class)
    .usePage(pageInfo)
    .queryForList();
```

Use interface 'BaseMapper' for paging queries

```java
BaseMapper<TestUser> baseMapper = ...

// Build a paging object with 3 pieces of data per page (default first page is 0)
Page pageInfo = new PageObject();
pageInfo.setPageSize(3);

// paging query data
PageResult<TestUser> pageData1 = baseMapper.queryByPage(pageInfo);

// query next page
pageInfo.nextPage();
PageResult<TestUser> pageData2 = baseMapper.queryByPage(pageInfo);
```

If you want to page the query in the Mapper file, you only need to add a Page parameter to the corresponding Mapper interface method.

```java
@RefMapper("/mapper/quick_page3/TestUserMapper.xml")
public interface TestUserDAO extends BaseMapper<TestUser> {
    // You can directly return the result of paged data
    List<TestUser> queryByAge(
              @Param("beginAge") int beginAge, 
              @Param("endAge") int endAge, 
              Page pageInfo);
    
    // You can also return paging results with paging information
    PageResult<TestUser> queryByAge2(
              @Param("beginAge") int beginAge, 
              @Param("endAge") int endAge, 
              Page pageInfo);
}
```

### using transaction

Starting and handing off a transaction, you can also enable multiple transactions consecutively, for example:

```java {4,8}
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

TransactionStatus tranA = manager.begin();
...
manager.commit(tranA);
```

```java
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

TransactionStatus tranA = manager.begin();
TransactionStatus tranB = manager.begin();
TransactionStatus tranC = manager.begin();

...

manager.commit(tranC);
manager.commit(tranB);
manager.commit(tranA);
```

The parameters of the 'begin' method allow you to set the **propagation** 和 **isolation** of the transaction 

```java
TransactionStatus tranA = manager.begin(
        Propagation.REQUIRES_NEW, // propagation
        Isolation.READ_COMMITTED  // isolation
);
```