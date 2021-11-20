SQL 模式
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

    // 查询 age > 40 的数据，并返回 List/Map 形式
    List<Map<String, Object>> dataList = jdbcTemplate//
            .queryForList("select * from test_user where age > 40");

.. code-block:: java
    :linenos:

    // 查询 age > 40 的数据，并返回 TestUser结果集
    List<TestUser> dataList = jdbcTemplate//
            .queryForList("select * from test_user where age > 40", TestUser.class);

.. code-block:: java
    :linenos:

    // 查询 age > 40 的数据，并返回 1 个结果，结果为 Map 形式
    Map<String, Object> data = jdbcTemplate//
            .queryForMap("select * from test_user where age > 40 limit 1");

.. code-block:: java
    :linenos:

    // 查询 age > 40 的数据，并返回 1 个结果，结果使用 TestUser 封装
    TestUser data = jdbcTemplate//
            .queryForObject("select * from test_user where age > 40 limit 1", TestUser.class);

.. code-block:: java
    :linenos:

    // 查询 age > 40 的数据总数
    int queryForInt = jdbcTemplate
            .queryForInt("select count(*) from test_user where age > 40");

.. code-block:: java
    :linenos:

    // 查询 age > 40 的数据总数
    long queryForLong = jdbcTemplate
            .queryForLong("select count(*) from test_user where age > 40");

.. code-block:: java
    :linenos:

    // 查询 id 为 1 的记录，并返回 name 字段值
    String queryForString = jdbcTemplate
            .queryForString("select name from test_user where id = 1");


更新数据并返回影响行数

.. code-block:: java
    :linenos:

    // 将 id 为 1 的数据 name 字段更新为 mala
    int result1 = jdbcTemplate
            .executeUpdate("update test_user set name = 'mala' where id = 1");

.. code-block:: java
    :linenos:

    // 删除 ID 为 1 的数据
    int result2 = jdbcTemplate
            .executeUpdate("delete from test_user where id = 1");

.. code-block:: java
    :linenos:

    // 新增数据
    int result3 = jdbcTemplate
            .executeUpdate("insert into `test_user` values (10, 'david', 26, now())");


参数化SQL
==============

参数化是指 SQL 执行语句中的条件以参数形式传给数据库，从而避免 SQL 注入产生的安全问题。

常规传参
    .. code-block:: java
        :linenos:

        String querySql1 = "select * from test_user where age > ?";
        Object[] queryArg1 = new Object[] { 40 };
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(querySql1, queryArg1);

    .. code-block:: java
        :linenos:

        String querySql2 = "select * from test_user where age > ?";
        Object[] queryArg2 = new Object[] { 40 };
        List<TestUser> dtoList = jdbcTemplate.queryForList(querySql2, queryArg2, TestUser.class);

    .. code-block:: java
        :linenos:

        String querySql3 = "select * from test_user where age > ? limit 1";
        Object[] queryArg3 = new Object[] { 40 };
        Map<String, Object> map = jdbcTemplate.queryForMap(querySql3, queryArg3);

    .. code-block:: java
        :linenos:

        String querySql4 = "select * from test_user where age > ? limit 1";
        Object[] queryArg4 = new Object[] { 40 };
        TestUser dto = jdbcTemplate.queryForObject(querySql4, queryArg4, TestUser.class);

    .. code-block:: java
        :linenos:

        String querySql5 = "select count(*) from test_user where age > ?";
        Object[] queryArg5 = new Object[] { 40 };
        int queryForInt = jdbcTemplate.queryForInt(querySql5, queryArg5);

    .. code-block:: java
        :linenos:

        String querySql6 = "select count(*) from test_user where age > ?";
        Object[] queryArg6 = new Object[] { 40 };
        long queryForLong = jdbcTemplate.queryForLong(querySql6, queryArg6);

    .. code-block:: java
        :linenos:

        String querySql7 = "select name from test_user where id = ?";
        Object[] queryArg7 = new Object[] { 1 };
        String queryForString = jdbcTemplate.queryForString(querySql7, queryArg7);

    .. code-block:: java
        :linenos:

        String querySql8 = "update test_user set name = ? where id = ?";
        Object[] queryArg8 = new Object[] { "mala", 1 };
        int result1 = jdbcTemplate.executeUpdate(querySql8, queryArg8);

    .. code-block:: java
        :linenos:

        String querySql9 = "delete from test_user where id = ?";
        Object[] queryArg9 = new Object[] { 1 };
        int result2 = jdbcTemplate.executeUpdate(querySql9, queryArg9);

    .. code-block:: java
        :linenos:

        String querySql10 = "insert into `test_user` values (?,?,?,?)";
        Object[] queryArg10 = new Object[] { 10, "'david'", 26, new Date() };
        int result3 = jdbcTemplate.executeUpdate(querySql10, queryArg10);


使用Map传参
    .. code-block:: java
        :linenos:

        String querySql1 = "select * from test_user where age > :age";
        Map<String, Object> queryArg1 = Collections.singletonMap("age", 40);
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(querySql1, queryArg1);

    .. code-block:: java
        :linenos:

        String querySql2 = "select * from test_user where age > :age";
        Map<String, Object> queryArg2 = Collections.singletonMap("age", 40);
        List<TestUser> dtoList = jdbcTemplate.queryForList(querySql2, queryArg2, TestUser.class);

    .. code-block:: java
        :linenos:

        String querySql3 = "select * from test_user where age > :age limit 1";
        Map<String, Object> queryArg3 = Collections.singletonMap("age", 40);
        Map<String, Object> map = jdbcTemplate.queryForMap(querySql3, queryArg3);

    .. code-block:: java
        :linenos:

        String querySql4 = "select * from test_user where age > :age limit 1";
        Map<String, Object> queryArg4 = Collections.singletonMap("age", 40);
        TestUser dto = jdbcTemplate.queryForObject(querySql4, queryArg4, TestUser.class);

    .. code-block:: java
        :linenos:

        String querySql5 = "select count(*) from test_user where age > :age";
        Map<String, Object> queryArg5 = Collections.singletonMap("age", 40);
        int queryForInt = jdbcTemplate.queryForInt(querySql5, queryArg5);

    .. code-block:: java
        :linenos:

        String querySql6 = "select count(*) from test_user where age > :age";
        Map<String, Object> queryArg6 = Collections.singletonMap("age", 40);
        long queryForLong = jdbcTemplate.queryForLong(querySql6, queryArg6);

    .. code-block:: java
        :linenos:

        String querySql7 = "select name from test_user where id = :id";
        Map<String, Object> queryArg7 = Collections.singletonMap("id", 1);
        String queryForString = jdbcTemplate.queryForString(querySql7, queryArg7);

    .. code-block:: java
        :linenos:

        String querySql8 = "update test_user set name = :name where id = :id";
        Map<String, Object> queryArg8 = new HashMap<>();
        queryArg8.put("name", "mala");
        queryArg8.put("id", 1);
        int result1 = jdbcTemplate.executeUpdate(querySql8, queryArg8);

    .. code-block:: java
        :linenos:

        String querySql9 = "delete from test_user where id = :id";
        Map<String, Object> queryArg9 = Collections.singletonMap("id", 1);
        int result2 = jdbcTemplate.executeUpdate(querySql9, queryArg9);

    .. code-block:: java
        :linenos:

        String querySql10 = "insert into `test_user` values (?,?,?,?)";
        Object[] queryArg10 = new Object[] { 10, "'david'", 26, new Date() };
        int result3 = jdbcTemplate.executeUpdate(querySql10, queryArg10);


批量
==============

HasorDB 提供提供了 5 个基于 SQL 批量操作接口，下面重点说其中 4 个。另外一个会在介绍 ``SqlParameterSource`` 接口时提到

.. code-block:: java
    :linenos:

    // 批量执行 SQL 命令
    int[] result1 = jdbcTemplate.executeBatch(new String[] {
        "insert into `test_user` values (11, 'david', 26, now())",
        "insert into `test_user` values (12, 'kevin', 26, now())"
    });

.. code-block:: java
    :linenos:

    // 批量执行带参的 SQL
    String querySql1 = "insert into `test_user` values (?,?,?,?)";
    Object[][] queryArg1 = new Object[][] {//
        new Object[] { 20, "david", 26, new Date() },//
        new Object[] { 22, "kevin", 26, new Date() } //
    };
    int[] result2 = jdbcTemplate.executeBatch(querySql1, queryArg1);

.. code-block:: java
    :linenos:

    // 批量执行带参的 SQL，使用 Map 作为入参
    String querySql2 = "update test_user set name = :name where id = :id";
    Map<String, Object> record1 = new HashMap<>();
    record1.put("name", "jack");
    record1.put("id", 1);
    Map<String, Object> record2 = new HashMap<>();
    record2.put("name", "steve");
    record2.put("id", 2);
    Map<String, Object>[] queryArg2 = new Map[] { record1, record2 };
    int[] result3 = jdbcTemplate.executeBatch(querySql2, queryArg2);

.. code-block:: java
    :linenos:

    // 使用 BatchPreparedStatementSetter 接口进行参数批量设置
    String querySql4 = "delete from test_user where id = ?";
    Object[][] queryArg4 = new Object[][] { new Object[] { 1 }, new Object[] { 2 } };
    int[] result4 = jdbcTemplate.executeBatch(querySql4, new BatchPreparedStatementSetter() {
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setObject(1, queryArg4[i][0]);
        }
        public int getBatchSize() {
            return queryArg4.length;
        }
    });


存储过程
==============

HasorDB 支持存储过程的调用，并且可以完整接收到存储过程产生的输出参数及结果集。比起使用原声 JDBC 接口要便捷很多。

.. code-block:: sql
    :linenos:

    -- 以 MySQL 为例，有下例存储。执行存储过程后会产生 1个入参，1个出参，2个结果集
    drop procedure if exists proc_select_table;
    create procedure proc_select_table(in userName varchar(200), out outName varchar(200))
    begin
        select * from test_user where name = userName;
        select * from test_user;
        set outName = concat(userName,'-str');
    end;

.. code-block:: java
    :linenos:

    DataSource dataSource = DsUtils.dsMySql();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.loadSQL("CreateDB.sql");

    // 执行存储过程并接收所有返回的数据
    List<SqlParameter> parameters = new ArrayList<>();
    parameters.add(SqlParameterUtils.withInput("dative", Types.VARCHAR));
    parameters.add(SqlParameterUtils.withOutputName("outName", Types.VARCHAR));
    Map<String, Object> resultMap = jdbcTemplate.call("{call proc_select_table(?,?)}", parameters);

    // 输出参数
    String outName = (String) resultMap.get("outName");
    // 第一个 select 的结果
    List<Map<String, Object>> result1 = (List<Map<String, Object>>) resultMap.get("#result-set-1");
    // 第二个 select 的结果
    List<Map<String, Object>> result2 = (List<Map<String, Object>>) resultMap.get("#result-set-2");

.. HINT::
    通过 ``jdbcTemplate.call`` 调用存储过程返回的结果集，只会以 ``List/Map`` 形式返回。


多语句/多返回值
==============

通过多语句能力可以让应用发起更加复杂的 SQL 脚本查询，并一起将它们发送给数据库然后接收所有的返回值。

.. code-block:: java
    :linenos:

    DataSource dataSource = DsUtils.dsMySql();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.loadSQL("CreateDB.sql");

    String querySql = "set @userName = convert(? USING utf8); select * from test_user where name = @userName;";
    Object[] queryArg = new Object[] { "dative" };
    List<Object> resultList = jdbcTemplate.multipleExecute(querySql, queryArg);


这条 SQL 先是将查询参数存入MySQL 变量 ``userName``，然后在通过查询语句引用这个变量。

.. HINT::

    由于是两条 SQL 语句，因此 ``resultList`` 的结果

    - 第一个元素是 ``set`` 语句的执行结果
    - 第二个元素是 ``select`` 语句的执行结果


RowMapper
==============

``RowMapper`` 负责将 ``ResultSet`` 一行数据读取出来并且转换成对象。HasorDB 一共内置了三种 RowMapper

- ``ColumnMapRowMapper`` 将行转换为 Map
- ``MappingRowMapper`` 基于 ``对象映射`` 处理行数据。
- ``SingleColumnRowMapper`` 只有当查询结果中包含一列数据的时候才可以使用。它会利用 ``TypeHandler`` 机制读取出这一列数据。

**ColumnMapRowMapper**
    .. code-block:: java
        :linenos:

        String querySql = "select * from test_user where age > 40";
        // 使用 ColumnMapRowMapper
        List<Map<String, Object>> mapList = jdbcTemplate.query(querySql, new ColumnMapRowMapper());
        // 下列是简化形式
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(querySql)

**MappingRowMapper**
    .. code-block:: java
        :linenos:

        String querySql = "select * from test_user where age > 40";
        // 使用 MappingRowMapper
        List<TestUser> dtoList = jdbcTemplate.query(querySql, new MappingRowMapper<>(TestUser.class));
        // 下列是简化形式
        List<TestUser> dtoList = jdbcTemplate.queryForList(querySql, TestUser.class);

**SingleColumnRowMapper**
    .. code-block:: java
        :linenos:

        String querySql = "select * from test_user where age > 40";
        // 使用 SingleColumnRowMapper
        List<String> stringList = jdbcTemplate.query(querySql, new SingleColumnRowMapper<>(String.class));
        // 下列是简化形式
        List<String> stringList = jdbcTemplate.queryForList(querySql, String.class);

**自定义**
    .. code-block:: java
        :linenos:

        // 自定义方式只设置 age 和 name
        String queryString = "select * from test_user where age > 40";
        List<TestUser> mapList = jdbcTemplate.query(queryString, new RowMapper<TestUser>() {
            public TestUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                TestUser testUser = new TestUser();
                testUser.setAge(rs.getInt("age"));
                testUser.setName(rs.getString("name"));
                return testUser;
            }
        });


ResultSetExtractor
==============

``ResultSetExtractor`` 负责处理整个结果集，通常和 ``RowMapper`` 配合使用，或者实现对结果集的更复杂处理。

举一个例子，查询所有用户，并且构建一个用户 ID 和 用户名的 Map

.. code-block:: java
    :linenos:

    DataSource dataSource = DsUtils.dsMySql();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.loadSQL("CreateDB.sql");

    String queryString = "select * from test_user";
    Map<Integer, String> idMap = jdbcTemplate.query(queryString, new ResultSetExtractor<Map<Integer, String>>() {
        public Map<Integer, String> extractData(ResultSet rs) throws SQLException {
            Map<Integer, String> hashMap = new HashMap<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                hashMap.put(id, name);
            }
            return hashMap;
        }
    });
    System.out.println(idMap);

执行结果为：

.. code-block:: text
    :linenos:

    {1=mali, 2=dative, 3=jon wes, 4=mary, 5=matt}


定制Statement
==============

举一个例子，当查询一张超大表并获取它的结果集时要使用 ``流式返回`` 否则内存极易出现溢出。不同的数据库开启流式返回的方式虽有差异，
但都需要设置 ``Statement/PreparedStatement`` 的参数。

下面就以 MySQL 为例展示一下通过定制 Statement 实现流式查询的例子：

.. code-block:: java
    :linenos:

    DataSource dataSource = DsUtils.dsMySql();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.loadSQL("CreateDB.sql");

    List<TestUser> resultList = jdbcTemplate.query(con -> {
        PreparedStatement ps = con.prepareStatement(
            "select * from test_user", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ps.setFetchSize(Integer.MIN_VALUE);
        return ps;
    }, new RowMapperResultSetExtractor<>(new MappingRowMapper<>(TestUser.class)));


SqlParameterSource
==============

``SqlParameterSource`` 是通过接口形式给动态 SQL 传参数，功效和 ``数组/Map`` 传参数一样。
HasorDB 内置了两个实现，分别为：

- ``BeanSqlParameterSource`` 将一个 Bean 转换为 ``SqlParameterSource`` 接口
- ``MapSqlParameterSource`` 将一个 Map 转换为 ``SqlParameterSource`` 接口

下面用相同的功效列举不同的查询写法

常规传参
    .. code-block:: java
        :linenos:

        String querySql = "select * from test_user where age > ?";
        Object[] queryArg = new Object[] { 40 };
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(querySql, queryArg);

使用Map传参
    .. code-block:: java
        :linenos:

        String querySql = "select * from test_user where age > :age";
        Map<String, Object> queryArg = Collections.singletonMap("age", 40);
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(querySql, queryArg);

使用MapSqlParameterSource传参
    .. code-block:: java
        :linenos:

        String querySql = "select * from test_user where age > :age";
        Map<String, Object> queryArg = Collections.singletonMap("age", 40);
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(querySql, queryArg);

使用BeanSqlParameterSource传参
    .. code-block:: java
        :linenos:

        String querySql = "select * from test_user where age > :age";
        TestUser argDTO = new TestUser();
        argDTO.setAge(40);

        BeanSqlParameterSource queryArg = new BeanSqlParameterSource(argDTO);
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(querySql, queryArg);

.. HINT::
    上述 4 种查询方式返回结果相同/功效相同。

使用原始连接
==============

有时候我们需要获取最原始的 Connection 那么可以采用下面方式：

.. code-block:: java
    :linenos:

    List<TestUser> resultList = jdbcTemplate.execute(new ConnectionCallback<List<TestUser>>() {
        public List<TestUser> doInConnection(Connection con) throws SQLException {
            List<TestUser> result = ...
            // do some thing
            return result;
        }
    });
