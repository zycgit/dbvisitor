介绍
------------------------------------
``Documents are translated using translation software, The original for README.md``

* Project Home: [https://www.hasordb.net](https://www.hasordb.net)
* [![QQ群:948706820](https://img.shields.io/badge/QQ%E7%BE%A4-948706820-orange)](https://qm.qq.com/cgi-bin/qm/qr?k=Qy3574A4VgI0ph4fqFbZW-w49gnyqu6p&jump_from=webapi)
  [![zyc@byshell.org](https://img.shields.io/badge/Email-zyc%40byshell.org-blue)](mailto:zyc@byshell.org)
  [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.hasor/hasor-db/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.hasor/hasor-db)

&emsp;&emsp;HasorDB is a Full-featured database access tool, Providing object mapping,Richer type handling than Mybatis,
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
  - Richer TypeHandler（MyBatis 40+，HasorDB 60+）
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
  <artifactId>hasor-db</artifactId>
  <version>4.3.4</version><!-- see new version https://mvnrepository.com/artifact/net.hasor/hasor-db -->
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

HasorDB can be used without relying on database connection pools, 
but having a database connection pool is standard for most projects. Druid of Alibaba

```xml
<dependency>
  <groupId>com.alibaba</groupId>
  <artifactId>druid</artifactId>
  <version>1.1.23</version>
</dependency>
```

Finally, prepare a database table and initialize some data (' createdB.sql 'file)

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

Using SQL to read data 'PrintUtils' and' DsUtils' can be found in the example project

```java
// creating a data source
DataSource dataSource = DsUtils.dsMySql();
// create JdbcTemplate object
JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
// load the test data script
jdbcTemplate.loadSQL("CreateDB.sql");

// Query the data and return it as a Map
List<Map<String, Object>> mapList = jdbcTemplate.queryForList("select * from test_user");
// print daa
PrintUtils.printMapList(mapList);
```

console results:

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

If you want to receive data using a DTO object, you need to create a DTO object.

```java
// If the attribute and column names match exactly, no annotations are required.
// - This column is simply declared with the @table annotation, since the Table name and column name of 'test_user' match the hump underline.
// - If you need to map Table and Column names please refer to @table, @column for more attributes
@Table(mapUnderscoreToCamelCase = true)
  public class TestUser {
  private Integer id;
  private String  name;
  private Integer age;
  private Date    createTime;
  
  // getters and setters omitted
}

// Then use the 'queryForList' method to query directly, and the console can get the same result
List<TestUser> dtoList = jdbcTemplate.queryForList("select * from test_user", TestUser.class);
PrintUtils.printObjectList(dtoList);
```

### using CURD
for single-table CURD operations, you can use 'LambdaTemplate', it is a subclass of 'JdbcTemplate' 

```java
// creating a data source
DataSource dataSource = DsUtils.dsMySql();
// create LambdaTemplate object
LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
// load the test data script
lambdaTemplate.loadSQL("CreateDB.sql");

// Query, all data
List<TestUser> dtoList = lambdaTemplate.lambdaQuery(TestUser.class)
                .queryForList();
PrintUtils.printObjectList(dtoList);

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

### using DAO
Using DAOs, you can extends from the 'BaseMapper<T>' for generic DAO to perform some basic operations, again using the single-table CRUD example.

```java
// Some of the DAO's interfaces need to recognize ID attributes, 
// so it is necessary to mark them with the @column annotation on the DTO object
@Table(mapUnderscoreToCamelCase = true)
  public class TestUser {
  @Column(primary = true)
  private Integer id;
  private String  name;
  private Integer age;
  private Date    createTime;
  
  // getters and setters omitted
}

// creating a data source
DataSource dataSource = DsUtils.dsMySql();
// Creating a BaseMapper
DalSession session = new DalSession(dataSource);
BaseMapper<TestUser> baseMapper = session.createBaseMapper(TestUser.class);
// load the test data script
baseMapper.template().loadSQL("CreateDB.sql");

// Query, all data
List<TestUser> dtoList = baseMapper.query().queryForList();
PrintUtils.printObjectList(dtoList);

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

As a DAO, you can define your own methods and configure the SQL statements to be executed through annotations.

```java
// BaseMapper is optional, and inheriting it is equivalent to adding an extended set of single-table curds.
@SimpleMapper
public interface TestUserDAO extends BaseMapper<TestUser> {
  @Insert("insert into `test_user` (name,age,create_time) values (#{name}, #{age}, now())")
  public int insertUser(@Param("name") String name, @Param("age") int age);
  
  @Update("update `test_user` set age = #{age} where id = #{id}")
  public int updateAge(@Param("id") int userId, @Param("age") int newAge);
  
  @Delete("delete from `test_user` where age > #{age}")
  public int deleteByAge(@Param("age") int age);
  
  @Query(value = "select * from `test_user` where  #{beginAge} < age and age < #{endAge}", resultType = TestUser.class)
  public List<TestUser> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);
}
```

```java
// Create DalRegistry and register the TestUserDAO
DalRegistry dalRegistry = new DalRegistry();
dalRegistry.loadMapper(TestUserDAO.class);

// Create a Session using DalRegistry
DalSession session = new DalSession(dataSource, dalRegistry);

// Creating the DAO Interface
TestUserDAO userDAO = session.createMapper(TestUserDAO.class);
```

### using Mapper

The best place for unified SQL management is still Mapper files, and HasorDB Mapper files are highly compatible with MyBatis at a very low learning cost.

```java
// Use the @RefMapper to associate Mapper files with interface classes (extends from BaseMapper is optional)
@RefMapper("/mapper/quick_dao3/TestUserMapper.xml")
public interface TestUserDAO extends BaseMapper<TestUser> {
    public int insertUser(@Param("name") String name, @Param("age") int age);

    public int updateAge(@Param("id") int userId, @Param("age") int newAge);

    public int deleteByAge(@Param("age") int age);

    public List<TestUser> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);
}
```

In order to better understand and use HasorDB Mapper files, it is recommended to add DTD validation.
In addition HasorDB compatible with MyBatis3 DTD for most of the MyBatis project can be normally compatible.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//hasor.net//DTD Mapper 1.0//EN" "https://www.hasor.net/schema/hasordb-mapper.dtd">
<mapper namespace="net.hasor.db.example.quick.dao3.TestUserDAO">
  <resultMap id="testuser_resultMap" type="net.hasor.db.example.quick.dao3.TestUser">
    <id column="id" property="id"/>
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
</mapper>
```

### fast quick building

fast quick building is consists of 'and' and 'or'.
These are two rules used to replace the simple 'if' tag and the simple 'foreach' tag.
The following statement concatenates SQL when the parameter is not null

```xml
<select id="queryUser">
    select * from `test_user`
    where 1 = 1
    <if test="age != null">
        and age = #{age}
    </if>
</select>
```

quick rule writing, where ':age' is the attribute name.

```xml
<select id="queryUser">
    select * from `test_user`
    @{and, age = :age}
</select>
```

For example, 'foreach' :

```xml
<select id="queryUser">
    select * from `test_user`
    where
    id in <foreach item="item" index="index" collection="list"
             open="(" separator="," close=")">
        #{item}
    </foreach>
</select>
```

quick rule writing, where ':list' is the collection attribute name.

```xml
<select id="queryUser">
    select * from `test_user`
    @{and, id in (:list)}
</select>
```

If there are multiple simple conditions, fast writing can greatly reduce Mapper's workload.

### Paging query

HasorDB's paging capability is supported only at the 'LambdaTemplate', 'BaseMapper', and 'Mapper DAO' levels.
The following are different ways of using:

Use 'LambdaTemplate' for paging queries

```java
DataSource dataSource = DsUtils.dsMySql();
LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
lambdaTemplate.loadSQL("CreateDB.sql");

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
DataSource dataSource = DsUtils.dsMySql();
DalSession session = new DalSession(dataSource);
BaseMapper<TestUser> baseMapper = session.createBaseMapper(TestUser.class);
baseMapper.template().loadSQL("CreateDB.sql");

// Build a paging object with 3 pieces of data per page (default first page is 0)
Page pageInfo = new PageObject();
pageInfo.setPageSize(3);

// paging query data
PageResult<TestUser> pageData1 = baseMapper.queryByPage(pageInfo);

// query next page
pageInfo.nextPage();
PageResult<TestUser> pageData2 = baseMapper.queryByPage(pageInfo);
```

To query queries in Mapper files in pages, simply add a Page parameter to the DAO interface method.

```java
@RefMapper("/mapper/quick_page3/TestUserMapper.xml")
public interface TestUserDAO extends BaseMapper<TestUser> {
    // You can directly return the result of paged data
    public List<TestUser> queryByAge(
              @Param("beginAge") int beginAge, 
              @Param("endAge") int endAge, 
              Page pageInfo);
    
    // You can also return paging results with paging information
    public PageResult<TestUser> queryByAge2(
              @Param("beginAge") int beginAge, 
              @Param("endAge") int endAge, 
              Page pageInfo);
}
```

```java
// Building paging conditions
Page pageInfo = new PageObject();
pageInfo.setPageSize(3);

List<TestUser> data1 = userDAO.queryByAge(25, 100, pageInfo);
PageResult<TestUser> page1 = userDAO.queryByAge2(25, 100, pageInfo);

pageInfo.nextPage();
List<TestUser> data2 = userDAO.queryByAge(25, 100, pageInfo);
PageResult<TestUser> page2 = userDAO.queryByAge2(25, 100, pageInfo);
```

### using transaction

HasorDB provides two ways to use transactions:

- ** using API **, by calling the 'TransactionManager' interface to achieve transaction control.
- ** Template **, through the 'TransactionTemplate' interface to achieve transaction control.

### transaction using API

Start and submit a transaction, for example:

```java {4,8}
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

TransactionStatus tranA = manager.begin();

...

manager.commit(tranA);
```

Or use shortcuts

```java {4,8}
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

manager.begin();

...

manager.commit(); //commit last trans
```

Start and submit multiple transactions, such as:

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

The 'begin' method allows you to set the transaction propagation ** and the isolation level **

```java
TransactionStatus tranA = manager.begin(
        Propagation.REQUIRES_NEW, // transaction propagation is same as Spring TX
        Isolation.READ_COMMITTED  // isolation level
);
```

### Template transaction

Typically, transactions follow the following logic:

```java {2,6,8}
try {
    manager.begin(behavior, level);

    ...

    manager.commit();
} catch (Throwable e) {
    manager.rollBack();
    throw e;
}
```

The way to use a template transaction is：

```java
Object result = template.execute(new TransactionCallback<Object>() {
    @Override
    public Object doTransaction(TransactionStatus tranStatus) {
        ...
        return null;
    }
});

// Using the Java8 Lambda syntax can be simplified as follows
Object result = template.execute(tranStatus -> {
    return ...;
});
```

You can also set the transaction state to 'rollBack' or 'readOnly' to cause rollBack

```java {3,5}
Object result = template.execute(new TransactionCallback<Object>() {
    public Object doTransaction(TransactionStatus tranStatus) {
        tranStatus.setReadOnly();
        // 或
        tranStatus.setRollback();

        return ...;
    }
});
```
