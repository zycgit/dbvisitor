介绍
------------------------------------

* Project Home: [https://www.hasor.net](https://www.hasor.net)
* [![QQ群:193943114](https://img.shields.io/badge/QQ%E7%BE%A4-193943114-orange)](https://qm.qq.com/cgi-bin/qm/qr?k=0ZqU8WlKVENanH6ajgpbVua7WJUMOKQ9&jump_from=webapi)
  [![Gitter](https://badges.gitter.im/hasor/hasor-dataql.svg)](https://gitter.im/hasor/hasor-dataql?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
  [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.hasor/hasor-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.hasor/hasor-core)
  [![Build Status](https://travis-ci.org/zycgit/hasor.svg?branch=master)](https://travis-ci.org/zycgit/hasor)

&emsp;&emsp;HasorDB 是一款数据库访问框架，其思想和编程方式来自于 SpringJDBC、MyBatis、MyBatisPlus。
可以简单理解 HasorDB 是前面三者集合体, 不同于一般整合方式的是 HasorDB 采用了参照原有接口完全重新实现，尽可能的接近原始框架的使用体验。
虽然 HasorDB 是参照已有框架重新实现，但同时也融入了其独特的一些新特性。这使得使用起来更加便捷。

功能特性
------------------------------------

- 熟悉的方式
  - JdbcTemplate 接口方式（高度兼容 Spring JDBC）
  - Mapper 文件方式（高度兼容 MyBatis）
  - LambdaTemplate （高度接近 MyBatis Plus、jOOQ 和 BeetlSQL）
  - @Insert、@Update、@Delete、@Query、@Callable 注解（类似 JPA）

- 事务支持
  - 支持 5 个事务隔离级别、7 个事务传播行为（与 Spring tx 相同）
  - 提供 TransactionTemplate、TransactionManager 接口方式声明式事务控制能力（用法与 Spring 相同）

- 特色优势
  - 支持 分页查询 并且提供多种数据库方言（20+）
  - 支持 INSERT 策略（INTO、UPDATE、IGNORE）
  - 更加丰富的 TypeHandler（MyBatis 40+，HasorDB 60+）
  - Mapper XML 支持多语句、多结果
  - 提供独特的 `@{xxx, expr , xxxxx }` 规则扩展机制，让动态 SQL 更加简单
  - 支持 存储过程
  - 全面支持 JDBC 4.2 各种数据类型
  - 全面支持 Java8 中的各种时间类型
  - 支持多数据源


快速上手
------------------------------------
引入依赖

```xml
<dependency>
  <groupId>net.hasor</groupId>
  <artifactId>hasor-db</artifactId>
  <version>4.3.0</version><!-- 查看最新版本：https://mvnrepository.com/artifact/net.hasor/hasor-db -->
</dependency>
```

引入数据库驱动，例如 MySQL 驱动

```xml
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
  <version>8.0.22</version>
</dependency>
```

使用 HasorDB 可以不依赖数据库连接池，但有数据库连接池是大多数项目的标配。这里选用 Alibaba 的 Druid

```xml
<dependency>
  <groupId>com.alibaba</groupId>
  <artifactId>druid</artifactId>
  <version>1.1.23</version>
</dependency>
```

最后准备一个数据库表，并初始化一些数据（`CreateDB.sql` 文件）

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

### SQL 方式

使用 SQL 的方式读取数据 `PrintUtils` 和 `DsUtils` 两个工具类可以在例子工程中找到

```java
// 创建数据源
DataSource dataSource = DsUtils.dsMySql();
// 创建 JdbcTemplate 对象
JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
// 加载测试数据脚本
jdbcTemplate.loadSQL("CreateDB.sql");

// 查询数据并 Map 形式返回
List<Map<String, Object>> mapList = jdbcTemplate.queryForList("select * from test_user");
// 打印测试数据
PrintUtils.printMapList(mapList);
```

控制台可以得到如下结果

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

如果想使用 DTO 对象接收数据，则需要创建一个 DTO 对象。

```java
// 如果属性名和列名可以完全匹配，那么无需任何注解。
//  - 本列中由于 `test_user` 的表名和列名符合驼峰转下划线，那么可以简单的通过 @Table 注解声明一下。
//  - 如果需要映射表名和列名请参照注解 @Table、@Column 更多的属性
@Table(mapUnderscoreToCamelCase = true)
  public class TestUser {
  private Integer id;
  private String  name;
  private Integer age;
  private Date    createTime;
  
  /* ... 省略 get/set 方法 ... */
}

// 然后通过 `queryForList` 方法直接查询，控制台就可以得到相同的结果
List<TestUser> dtoList = jdbcTemplate.queryForList("select * from test_user", TestUser.class);
PrintUtils.printObjectList(dtoList);
```

### 单表 CURD
对于单表 CURD 操作可以使用 `JdbcTemplate` 的子类 `LambdaTemplate`

```java
// 创建数据源
DataSource dataSource = DsUtils.dsMySql();
// 创建 LambdaTemplate 对象和创建 JdbcTemplate 一样
LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
// 初始化一些数据
lambdaTemplate.loadSQL("CreateDB.sql");

// 查询，所有数据
List<TestUser> dtoList = lambdaTemplate.lambdaQuery(TestUser.class)
                .queryForList();
PrintUtils.printObjectList(dtoList);

// 插入新数据
TestUser newUser = new TestUser();
newUser.setName("new User");
newUser.setAge(33);
newUser.setCreateTime(new Date());
int result = lambdaTemplate.lambdaInsert(TestUser.class)
                .applyEntity(newUser)
                .executeSumResult();

// 更新，将name 从 mali 更新为 mala
TestUser sample = new TestUser();
sample.setName("mala");
int result = lambdaTemplate.lambdaUpdate(TestUser.class)
                .eq(TestUser::getId, 1)
                .updateToBySample(sample)
                .doUpdate();

// 删除，ID 为 2 的数据
int result = lambdaTemplate.lambdaUpdate(TestUser.class)
                .eq(TestUser::getId, 1)
                .updateToBySample(sample)
                .doUpdate();
```

### 使用 DAO
使用 DAO 可以继承 `BaseMapper<T>` 通用 DAO 接口来完成一些基本操作，仍然以单表 CRUD 为例。

```java
// DAO 的一些接口需要识别 ID 属性，因此有必要在 DTO 对象上通过 @Column 注解标记出它们
@Table(mapUnderscoreToCamelCase = true)
  public class TestUser {
  @Column(primary = true)
  private Integer id;
  private String  name;
  private Integer age;
  private Date    createTime;
  
  /* ... 省略 get/set 方法 ... */
}

// 创建数据源
DataSource dataSource = DsUtils.dsMySql();
// 创建通用 DAO
DalSession session = new DalSession(dataSource);
BaseMapper<TestUser> baseMapper = session.createBaseMapper(TestUser.class);
// 初始化一些数据
baseMapper.template().loadSQL("CreateDB.sql");

// 查询数据
List<TestUser> dtoList = baseMapper.query().queryForList();
PrintUtils.printObjectList(dtoList);

// 插入新数据
TestUser newUser = new TestUser();
newUser.setName("new User");
newUser.setAge(33);
newUser.setCreateTime(new Date());
int result = baseMapper.insert(newUser);

// 更新，将name 从 mali 更新为 mala
TestUser sample = baseMapper.queryById(1);
sample.setName("mala");
int result = baseMapper.updateById(sample);

// 删除，ID 为 2 的数据
int result = baseMapper.deleteById(2);

```

作为 DAO 可以定义自己的方法，并通过注解配置具体执行的 SQL 语句。

```java
// BaseMapper 是可选的，继承它相当于多了一组单表 CURD 的扩展功能。
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
// 创建 DalRegistry 并注册 TestUserDAO
DalRegistry dalRegistry = new DalRegistry();
dalRegistry.loadMapper(TestUserDAO.class);

// 使用 DalRegistry 创建 Session
DalSession session = new DalSession(dataSource, dalRegistry);

// 创建 DAO 接口
TestUserDAO userDAO = session.createMapper(TestUserDAO.class);
```

### 使用 Mapper

统一管理 SQL 的最佳场所仍然是 Mapper 文件，而且 HasorDB 的 Mapper 文件高度兼容 MyBatis 学习成本极低。

```java
// 利用 @RefMapper 注解将 Mapper 文件和 接口类联系起来（继承 BaseMapper 是可选的）
@RefMapper("/mapper/quick_dao3/TestUserMapper.xml")
public interface TestUserDAO extends BaseMapper<TestUser> {
    public int insertUser(@Param("name") String name, @Param("age") int age);

    public int updateAge(@Param("id") int userId, @Param("age") int newAge);

    public int deleteByAge(@Param("age") int age);

    public List<TestUser> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);
}
```

为了更好了解和使用 HasorDB 的 Mapper 文件建议增加 DTD加以验证。另外 HasorDB 兼容 MyBatis3 的 DTD 对于绝大部分 MyBatis 工程都可以正常兼容，特殊的 MyBatis 用法需要做改造。

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

提示：
- 由于 Mapper 文件无法感知 BaseMapper 接口的范型，如果需要 Mapper 文件混合 BaseMapper 一起使用，建议 `@Table`、`@Column` 注解配置也加上。

### 分页查询

HasorDB 的分页能力仅在 `LambdaTemplate`、`BaseMapper`、`Mapper DAO` 三个层面上受到支持。下面为不同的使用方式：

使用 `LambdaTemplate` 进行分页查询

```java
// 构造 LambdaTemplate 和初始化一些数据
DataSource dataSource = DsUtils.dsMySql();
LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
lambdaTemplate.loadSQL("CreateDB.sql");

// 构建分页对象，每页 3 条数据(默认第一页的页码为 0)
Page pageInfo = new PageObject();
pageInfo.setPageSize(3);

// 分页查询数据
List<TestUser> pageData1 = lambdaTemplate.lambdaQuery(TestUser.class)
    .usePage(pageInfo)
    .queryForList();

// 分页查询下一页数据
pageInfo.nextPage();
List<TestUser> pageData2 = lambdaTemplate.lambdaQuery(TestUser.class)
    .usePage(pageInfo)
    .queryForList();
```

用接口 `BaseMapper` 进行分页查询

```java
// 构造 BaseMapper 和初始化一些数据
DataSource dataSource = DsUtils.dsMySql();
DalSession session = new DalSession(dataSource);
BaseMapper<TestUser> baseMapper = session.createBaseMapper(TestUser.class);
baseMapper.template().loadSQL("CreateDB.sql");

// 构建分页对象，每页 3 条数据(默认第一页的页码为 0)
Page pageInfo = new PageObject();
pageInfo.setPageSize(3);

// 分页查询数据
PageResult<TestUser> pageData1 = baseMapper.queryByPage(pageInfo);

// 分页查询下一页数据
pageInfo.nextPage();
PageResult<TestUser> pageData2 = baseMapper.queryByPage(pageInfo);
```

若想分页查询 Mapper 文件中的查询，仅需在对应 DAO 接口方法中增加一个 Page 参数，并且将返回值。

```java
@RefMapper("/mapper/quick_page3/TestUserMapper.xml")
public interface TestUserDAO extends BaseMapper<TestUser> {
    // 可以直接返回分页之后的数据结果
    public List<TestUser> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge, Page pageInfo);
    
    // 也可以返回包含分页信息的分页结果
    public List<TestUser> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge, Page pageInfo);
}
```

```java
// 构建分页条件
Page pageInfo = new PageObject();
pageInfo.setPageSize(3);

// 分页方式查询 mapper 中的查询
List<TestUser> data1 = userDAO.queryByAge(25, 100, pageInfo);
PageResult<TestUser> page1 = userDAO.queryByAge2(25, 100, pageInfo);

// 分页方式查询 mapper 中的查询
pageInfo.nextPage();
List<TestUser> data2 = userDAO.queryByAge(25, 100, pageInfo);
PageResult<TestUser> page2 = userDAO.queryByAge2(25, 100, pageInfo);
```
