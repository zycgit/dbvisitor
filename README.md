介绍
------------------------------------

* Project Home: [https://www.hasordb.net](https://www.hasordb.net)
* [![QQ群:948706820](https://img.shields.io/badge/QQ%E7%BE%A4-948706820-orange)](https://qm.qq.com/cgi-bin/qm/qr?k=Qy3574A4VgI0ph4fqFbZW-w49gnyqu6p&jump_from=webapi)
  [![zyc@byshell.org](https://img.shields.io/badge/Email-zyc%40byshell.org-blue)](mailto:zyc@byshell.org)
  [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.hasor/dbvisitor/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.hasor/dbvisitor)

&emsp;&emsp;dbVisitor 是一个数据库 ORM 工具。提供对象映射、丰富的类型处理、动态SQL、存储过程、内置分页方言20+、
支持嵌套事务、多数据源、条件构造器、INSERT 策略、多语句/多结果。兼容 Spring 及 MyBatis 用法。
它不依赖任何其它框架，因此可以很方便的和任意一个框架整合在一起使用。

功能特性
-----------------------------

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
  - 更加丰富的 TypeHandler（MyBatis 40+，dbVisitor 60+）
  - Mapper XML 支持多语句、多结果
  - 提供独特的 `@{xxx, expr , xxxxx }` 规则扩展机制，让动态 SQL 更加简单
  - 支持 存储过程
  - 支持 JDBC 4.2 和 Java8 中时间类型
  - 支持多数据源


快速上手
------------------------------------
引入依赖

```xml
<dependency>
  <groupId>net.hasor</groupId>
  <artifactId>dbvisitor</artifactId>
  <version>5.0.0</version><!-- 查看最新版本：https://mvnrepository.com/artifact/net.hasor/dbvisitor -->
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

使用 dbVisitor 可以不依赖数据库连接池，但有数据库连接池是大多数项目的标配。这里选用 Alibaba 的 Druid

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
  
  // getters and setters omitted
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
                .updateBySample(sample)
                .doUpdate();

// 删除，ID 为 2 的数据
int result = lambdaTemplate.lambdaUpdate(TestUser.class)
                .eq(TestUser::getId, 2)
                .doDelete();
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
  
  // getters and setters omitted
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

统一管理 SQL 的最佳场所仍然是 Mapper 文件，而且 dbVisitor 的 Mapper 文件高度兼容 MyBatis 学习成本极低。

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

为了更好了解和使用 dbVisitor 的 Mapper 文件建议增加 DTD加以验证。另外 dbVisitor 兼容 MyBatis3 的 DTD 对于绝大部分 MyBatis 工程都可以正常兼容。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
                        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="com.example.demo.quick.dao3.TestUserDAO">
    <resultMap id="testuser_resultMap" type="com.example.demo.quick.dao3.TestUser">
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

    <select id="queryAll" resultMap="testuser_resultMap">
        select id,<include refid="testuser_columns"/>
        from `test_user`
    </select>
</mapper>
```

### 快速条件拼接

快速条件拼接包含 `快速'与'条件` 和 `快速'或'条件` 它们是两个规则用于取代简单的 `if` 标签和简单的 `foreach` 标签。
如下语句，当参数不为空时候才拼接 sql

```xml
<select id="queryUser">
    select * from `test_user`
    where 1 = 1
    <if test="age != null">
        and age = #{age}
    </if>
</select>
```

可以简化为快速规则写法，其中 `:age` 为属性名。

```xml
<select id="queryUser">
    select * from `test_user`
    @{and, age = :age}
</select>
```

例如如下 `foreach` 操作：

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

可以简化为快速规则写法，其中 `:list` 为集合属性名。

```xml
<select id="queryUser">
    select * from `test_user`
    @{and, id in (:list)}
</select>
```

如果多个简单条件，快速写法将会极大的减少 Mapper 的工作量。

### 分页查询

dbVisitor 的分页能力仅在 `LambdaTemplate`、`BaseMapper`、`Mapper DAO` 三个层面上受到支持。下面为不同的使用方式：

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

若想分页查询 Mapper 文件中的查询，仅需在对应 DAO 接口方法中增加一个 Page 参数即可。

```java
@RefMapper("/mapper/quick_page3/TestUserMapper.xml")
public interface TestUserDAO extends BaseMapper<TestUser> {
    // 可以直接返回分页之后的数据结果
    public List<TestUser> queryByAge(
              @Param("beginAge") int beginAge, 
              @Param("endAge") int endAge, 
              Page pageInfo);
    
    // 也可以返回包含分页信息的分页结果
    public PageResult<TestUser> queryByAge2(
              @Param("beginAge") int beginAge, 
              @Param("endAge") int endAge, 
              Page pageInfo);
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

### 使用事务

dbVisitor 提供了三种方式使用事务，分别为：

- **声明式事务**，通过调用 `TransactionManager` 接口来实现事务控制。
- **模版事务**，通过 `TransactionTemplate` 接口来实现事务控制。

### 声明式事务

启动和递交一个事务，例如：

```java {4,8}
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

TransactionStatus tranA = manager.begin();

...

manager.commit(tranA);
```

或者使用快捷方式

```java {4,8}
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

manager.begin(); // 开启一个事务

...

manager.commit(); //递交最近的一个事务
```

启动和递交多个事务，例如：

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

通过 `begin` 方法的参数可以设置事务的 **传播属性** 和 **隔离级别**

```java
TransactionStatus tranA = manager.begin(
        Propagation.REQUIRES_NEW, // 传播属性
        Isolation.READ_COMMITTED  // 隔离级别
);
```

### 模版事务

使用模版事务的方式为：

```java
Object result = template.execute(new TransactionCallback<Object>() {
    public Object doTransaction(TransactionStatus tranStatus) {
        ...
        return null;
    }
});
```