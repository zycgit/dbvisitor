Jdbc模式
------------------------------------
Jdbc 模式主要围绕 ``JdbcTemplate`` 工具展开，共提供了大量工具方法。它的核心能力是提供基于 SQL 的数据库交互。

与 Spring 关系
    - ``JdbcTemplate`` 的核心接口，主要逻辑全部来自于 Spring JDBC，这包括 ``RowMapper`` 和 ``ResultSetExtractor`` 两个十分重要的接口。
    - Spring 的 ``NamedParameterJdbcTemplate`` 基于 Map 为参数名的能力也全部集中在 ``JdbcTemplate`` 接口中。
    - ``JdbcTemplate`` 可以理解是 Spring ``JdbcTemplate、NamedParameterJdbcTemplate`` 两者的合集。

增强的部分
    - 在使用 ``queryForObject(String,Class<?>)`` 和 ``queryForList(String,Class<?>)`` 系列方法时，Class 表示的实体类型将会遵循 ``对象映射``。而 Spring 并没有这方面能力。
    - 新增了一组 ``loadSQL``、``loadSplitSQL`` 方法可以将本地资源文件加载到内存然后执行它们。
    - 新增 ``multipleExecute`` 系列方法用于处理 ``多语句`` 或 ``多返回值``

``JdbcTemplate`` 使用非常简单只需要 new 出来就可以。

.. code-block:: java
    :linenos:

    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    // 或者
    JdbcTemplate jdbcTemplate = new JdbcTemplate(connection);


执行SQL
==============

执行查询并返回结果

.. code-block:: java
    :linenos:

    DataSource dataSource = DsUtils.dsMySql();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.loadSQL("CreateDB.sql");

    // 查询 age > 20 的数据，并返回 List/Map 形式
    List<Map<String, Object>> dataList = jdbcTemplate//
            .queryForList("select * from test_user where age > 20");

    // 查询 age > 20 的数据，并返回 TestUser结果集
    List<TestUser> dataList = jdbcTemplate//
            .queryForList("select * from test_user where age > 20", TestUser.class);

    // 查询 age > 20 的数据，并返回 1 个结果，结果为 Map 形式
    Map<String, Object> data = jdbcTemplate//
            .queryForMap("select * from test_user where age > 20 limit 1");

    // 查询 age > 20 的数据，并返回 1 个结果，结果使用 TestUser 封装
    TestUser data = jdbcTemplate//
            .queryForObject("select * from test_user where age > 20 limit 1", TestUser.class);

    // 查询 age > 20 的数据总数
    int queryForInt = jdbcTemplate
            .queryForInt("select count(*) from test_user where age > 20");

    // 查询 age > 20 的数据总数
    long queryForLong = jdbcTemplate
            .queryForLong("select count(*) from test_user where age > 20");

    // 查询 id 为 1 的记录，并返回 name 字段值
    String queryForString = jdbcTemplate
            .queryForString("select name from test_user where id = 1");


更新数据并返回影响行数

.. code-block:: java
    :linenos:

    // 将 id 为 1 的数据 name 字段更新为 mala
    int result1 = jdbcTemplate
            .executeUpdate("update test_user set name = 'mala' where id = 1");

    // 删除 ID 为 1 的数据
    int result2 = jdbcTemplate
            .executeUpdate("delete from test_user where id = 1");

    // 新增数据
    int result3 = jdbcTemplate
            .executeUpdate("insert into `test_user` values (10, 'david', 26, now())");


参数化SQL
==============

参数化是指 SQL 执行语句中的条件以参数形式传给数据库，从而避免 SQL 注入产生的安全问题。

执行查询并返回结果

.. code-block:: java
    :linenos:

    DataSource dataSource = DsUtils.dsMySql();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.loadSQL("CreateDB.sql");

    // 查询 age > 20 的数据，并返回 List/Map 形式
    List<Map<String, Object>> dataList = jdbcTemplate//
            .queryForList("select * from test_user where age > ?", 20);

    // 查询 age > 20 的数据，并返回 TestUser结果集
    List<TestUser> dataList = jdbcTemplate//
            .queryForList("select * from test_user where age > ?", TestUser.class, 20);

    // 查询 age > 20 的数据，并返回 1 个结果，结果为 Map 形式
    Map<String, Object> data = jdbcTemplate//
            .queryForMap("select * from test_user where age > ? limit 1", 20);

    // 查询 age > 20 的数据，并返回 1 个结果，结果使用 TestUser 封装
    TestUser data = jdbcTemplate//
            .queryForObject("select * from test_user where age > ? limit 1", TestUser.class, 20);

    // 查询 age > 20 的数据总数
    int queryForInt = jdbcTemplate
            .queryForInt("select count(*) from test_user where age > ?", 20);

    // 查询 age > 20 的数据总数
    long queryForLong = jdbcTemplate
            .queryForLong("select count(*) from test_user where age > ?", 20);

    // 查询 id 为 1 的记录，并返回 name 字段值
    String queryForString = jdbcTemplate
            .queryForString("select name from test_user where id = ?", 1);

Map作为SQL参数
==============

更新数据并返回影响行数

.. code-block:: java
    :linenos:

    // 将 id 为 1 的数据 name 字段更新为 mala
    int result1 = jdbcTemplate
            .executeUpdate("update test_user set name = ? where id = ?", "mala", 1);

    // 删除 ID 为 1 的数据
    int result2 = jdbcTemplate
            .executeUpdate("delete from test_user where id = ?", 1);

    // 新增数据
    int result2 = jdbcTemplate
            .executeUpdate("insert into `test_user` values (?,?,?,?)", 10, "'david'", 26, new Date());


批量
==============




存储过程
==============



多语句/多返回值
==============



定制Statement
==============


RowMapper
==============


ResultSetExtractor
==============


使用原始连接
==============

