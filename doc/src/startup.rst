快速上手
------------------------------------

.. CAUTION::
    从 4.3.0 开始 HasorDB 从技术架构上完全独立于 Hasor 技术体系不在依赖 Hasor 架构。

截止到目前为止 HasorDB 的最新版本为：**4.3.0** ，下面以 maven 为例。

- 这个网站会提供给您各种依赖管理框架的引入配置，支持：Maven、Gradle、SBT、Ivy、Grape、Leiningen、Buildr
- https://mvnrepository.com/artifact/net.hasor

.. code-block:: xml
    :linenos:

    <dependency>
        <groupId>net.hasor</groupId>
        <artifactId>hasor-db</artifactId>
        <version>4.3.0</version><!-- 查看最新版本：https://mvnrepository.com/artifact/net.hasor/hasor-db -->
    </dependency>

然后再引入数据库驱动，例如 MySQL 驱动

.. code-block:: xml
    :linenos:

    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.22</version>
    </dependency>


使用 HasorDB 可以不依赖数据库连接池，但有数据库连接池是大多数项目的标配。这里选用 Alibaba 的 Druid

.. code-block:: xml
    :linenos:

    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid</artifactId>
        <version>1.1.23</version>
    </dependency>


最后准备一个数据库表，并初始化一些数据（``CreateDB.sql`` 文件）

.. code-block:: sql
    :linenos:

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


SQL 方式
==============

使用 SQL 的方式读取数据，``PrintUtils`` 和 ``DsUtils`` 两个工具类可以在例子工程中找到

.. code-block:: java
    :linenos:

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

控制台可以得到如下结果

.. code-block:: text
    :linenos:

    /--------------------------------------------\
    | id | name    | age | create_time           |
    |--------------------------------------------|
    | 1  | mali    | 26  | 2021-11-12 19:14:06.0 |
    | 2  | dative  | 32  | 2021-11-12 19:14:06.0 |
    | 3  | jon wes | 41  | 2021-11-12 19:14:06.0 |
    | 4  | mary    | 66  | 2021-11-12 19:14:06.0 |
    | 5  | matt    | 25  | 2021-11-12 19:14:06.0 |
    \--------------------------------------------/


如果想使用 DTO 对象接收数据，则需要创建一个 DTO 对象。

.. code-block:: java
    :linenos:

    // 如果属性名和列名可以完全匹配，那么无需任何注解。
    //  - 本列中由于 `test_user` 的表名和列名符合驼峰转下划线，那么可以简单的通过 @Table 注解声明一下。
    //  - 如果需要映射表名和列名请参照注解 @Table、@Column 更多的属性
    @Table(mapUnderscoreToCamelCase = true)
    public class TestUser {
        private Integer id;
        private String  name;
        private Integer age;
        private Date    createTime;

        ... 省略 get/set 方法 ...
    }

    // 然后通过 `queryForList` 方法直接查询，控制台就可以得到相同的结果
    List<TestUser> dtoList = jdbcTemplate.queryForList("select * from test_user", TestUser.class);
    PrintUtils.printObjectList(dtoList);


单表 CURD
==============

对于单表 CURD 操作可以使用 ``JdbcTemplate`` 的子类 ``LambdaTemplate``

.. code-block:: java
    :linenos:

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


使用 DAO
==============

使用 DAO 可以使用 ``BaseMapper<T>`` 通用 DAO 接口来完成一些基本操作，仍然以单表 CRUD 为例。

.. code-block:: java
    :linenos:

    // DAO 的一些接口需要识别 ID 属性，因此有必要在 DTO 对象上通过 @Column 注解标记出它们
    @Table(mapUnderscoreToCamelCase = true)
    public class TestUser {
        @Column(primary = true)
        private Integer id;
        private String  name;
        private Integer age;
        private Date    createTime;

        ... 省略 get/set 方法 ...
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


作为 DAO，可以定义自己的查询方法。并通过相应的注解配置具体执行的 SQL 语句

.. code-block:: java
    :linenos:







使用 Mapper wenjian
==============