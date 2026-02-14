---
id: milvus
sidebar_position: 4
hide_table_of_contents: true
title: Milvus 特异性
description: Milvus 数据源使用 dbVisitor 的能力范围与限制。
---

# Milvus 数据源特异性

dbVisitor 通过 [JDBC-Milvus](../../../drivers/milvus/about) 驱动，基于 JDBC 协议访问 Milvus 向量数据库。
与 MongoDB/ElasticSearch 的原生命令风格不同，Milvus 适配器采用 **SQL 风格语法**（`CREATE TABLE`、`INSERT`、`SELECT`、`DELETE` 等），学习成本更低。

**支持的能力：**
- 使用 SQL 风格命令操作数据（[支持的命令列表](../../../drivers/milvus/commands)）
- [JdbcTemplate](#exec-command)、[构造器 API](#exec-lambda)、[BaseMapper](#exec-mapper)、[方法注解](#exec-annotation)、[Mapper 文件](#exec-file)
- 对象映射、结果集映射、[规则](../../rules/about)、[参数传递](../../args/about)、[ResultSetExtractor/RowMapper](../../result/about)
- **向量搜索**：KNN 近邻搜索（`ORDER BY field <-> vector`）、范围搜索（`vector_range()`）

**不支持：** executeBatch、存储过程、`Statement.RETURN_GENERATED_KEYS`

## 概念类比

Milvus 适配器使用标准 SQL 风格语法，其行为与传统关系型数据库 JDBC 非常接近：
- **DDL** — `CREATE TABLE`、`DROP TABLE`、`CREATE INDEX` 等，用 `executeUpdate` 执行
- **DML** — `INSERT`、`UPDATE`、`DELETE`，用 `executeUpdate` 获取影响行数
- **DQL** — `SELECT` 查询返回标准 `ResultSet`，列名与集合字段一一对应

:::info[Milvus 特殊要求]
Milvus 要求在查询前将集合 **加载到内存**，需先执行 `LOAD TABLE table_name` 命令。
此外，Milvus 的 Update 本质是 "Search-to-Upsert"（先查后写），不建议对海量数据执行全表 Update。
:::

---

## 命令方式（JdbcTemplate）{#exec-command}

使用 JdbcTemplate 可以直接执行 SQL 风格命令操作 Milvus，在此之前请确保已经正确配置好 Milvus 数据源，具体请参考 [Milvus 驱动使用指南](../../../drivers/milvus/usecase)。

:::tip[提示]
更多使用方式请参考 [JdbcTemplate 类](../../core/jdbc/about#guide)，在使用过程中下面两个特性由于驱动原因无法支持：
- 批量化
- 存储过程
:::

```java title='创建 JdbcTemplate'
JdbcTemplate jdbc = new JdbcTemplate(dataSource);
// 或者
JdbcTemplate jdbc = new JdbcTemplate(connection);
```

```java title='创建集合与索引'
// 创建集合（Collection）
jdbc.execute("CREATE TABLE book_vectors ("
    + "book_id INT64 PRIMARY KEY, "
    + "title VARCHAR(256) NOT NULL, "
    + "word_count INT32 DEFAULT 0, "
    + "book_intro FLOAT_VECTOR(128)"
    + ")");

// 创建向量索引
jdbc.execute("CREATE INDEX idx_intro ON book_vectors (book_intro) "
    + "USING \"IVF_FLAT\" WITH (nlist = 1024, metric_type = \"L2\")");

// 加载集合到内存（搜索前必须执行）
jdbc.execute("LOAD TABLE book_vectors");
```

```java title='插入数据'
// 直接插入
jdbc.execute("INSERT INTO book_vectors (book_id, title, word_count, book_intro) "
    + "VALUES (1, 'Java编程思想', 1000, [0.1, 0.2, ...])");
// 参数化插入（推荐）
jdbc.execute("INSERT INTO book_vectors (book_id, title, word_count, book_intro) "
    + "VALUES (?, ?, ?, ?)",
    new Object[] { 2L, "设计模式", 500, Arrays.asList(0.3f, 0.4f, ...) });
```

```java title='标量条件查询'
List<Map<String, Object>> list = jdbc.queryForList(
    "SELECT * FROM book_vectors WHERE word_count > 500 LIMIT 10");

Map<String, Object> book = jdbc.queryForMap(
    "SELECT * FROM book_vectors WHERE book_id = 1");
```

```java title='向量搜索（KNN）'
// 使用 <-> 运算符 + ORDER BY + LIMIT 实现 TopK 近邻搜索
List<Map<String, Object>> results = jdbc.queryForList(
    "SELECT book_id, title FROM book_vectors "
    + "ORDER BY book_intro <-> ? LIMIT 5",
    new Object[] { Arrays.asList(0.1f, 0.2f, ...) });
```

```java title='向量范围搜索'
// 方式 1：vector_range 函数（推荐）
List<Map<String, Object>> results = jdbc.queryForList(
    "SELECT * FROM book_vectors WHERE vector_range(book_intro, ?, 0.8)",
    new Object[] { Arrays.asList(0.1f, 0.2f, ...) });

// 方式 2：比较表达式
List<Map<String, Object>> results = jdbc.queryForList(
    "SELECT * FROM book_vectors WHERE book_intro <-> ? < 0.8",
    new Object[] { Arrays.asList(0.1f, 0.2f, ...) });
```

```java title='混合查询（标量过滤 + 向量搜索）'
List<Map<String, Object>> results = jdbc.queryForList(
    "SELECT book_id, title FROM book_vectors "
    + "WHERE word_count > 500 "
    + "ORDER BY book_intro <-> ? LIMIT 3",
    new Object[] { Arrays.asList(0.1f, 0.2f, ...) });
```

```java title='更新与删除'
// 按主键更新
jdbc.execute("UPDATE book_vectors SET word_count = 1200 WHERE book_id = 1");
// 按主键删除
jdbc.execute("DELETE FROM book_vectors WHERE book_id = 1");
```

---

## 构造器 API（LambdaTemplate）{#exec-lambda}

构造器 API 提供了一种类型安全、流式调用的方式操作 Milvus，用法与 RDBMS 一致，详细请参考 [构造器 API](../../core/lambda/about)。

```java title='初始化'
LambdaTemplate lambda = new LambdaTemplate(dataSource);
```

```java title='新增'
BookVector book = new BookVector();
book.setBookId(1L);
book.setTitle("Java编程思想");
book.setWordCount(1000);
book.setBookIntro(Arrays.asList(0.1f, 0.2f));

int result = lambda.insert(BookVector.class)
                   .applyEntity(book)
                   .executeSumResult();
```

```java title='查询'
BookVector book = lambda.query(BookVector.class)
                       .eq(BookVector::getBookId, 1L)
                       .queryForObject();

List<BookVector> books = lambda.query(BookVector.class)
                              .gt(BookVector::getWordCount, 500)
                              .queryForList();
```

```java title='更新'
int result = lambda.update(BookVector.class)
                   .eq(BookVector::getBookId, 1L)
                   .updateTo(BookVector::getWordCount, 1200)
                   .doUpdate();
```

```java title='删除'
int result = lambda.delete(BookVector.class)
                   .eq(BookVector::getBookId, 1L)
                   .doDelete();
```

### 分页查询

```java title='分页查询'
PageObject pageInfo = new PageObject(0, 10); // 第 0 页，每页 10 条

List<BookVector> page1 = lambda.query(BookVector.class)
                               .gt(BookVector::getWordCount, 0)
                               .usePage(pageInfo)
                               .queryForList();

pageInfo.nextPage();
List<BookVector> page2 = lambda.query(BookVector.class)
                               .gt(BookVector::getWordCount, 0)
                               .usePage(pageInfo)
                               .queryForList();
```

---

## BaseMapper 接口 {#exec-mapper}

`BaseMapper` 提供了通用 CRUD 方法，用法与 RDBMS 一致，详细请参考 [BaseMapper](../../core/mapper/about)。

```java title='定义对象'
@Table("book_vectors")
public class BookVector {
    @Column(value = "book_id", primary = true)
    private Long bookId;
    @Column("title")
    private String title;
    @Column("word_count")
    private Integer wordCount;
    @Column("book_intro")
    private List<Float> bookIntro;
    ... // 省略 getter/setter 方法
}
```

```java title='定义 Mapper 接口'
@SimpleMapper
public interface BookVectorMapper extends BaseMapper<BookVector> {
}
```

```java title='使用 Mapper 进行 CRUD'
Session session = ...;
BookVectorMapper mapper = session.createMapper(BookVectorMapper.class);

// 新增
mapper.insert(book);
// 查询
BookVector loaded = mapper.selectById(1L);
// 更新
loaded.setWordCount(1200);
mapper.update(loaded);
// 删除
mapper.deleteById(1L);
```

---

## 注解方式 {#exec-annotation}

:::tip[提示]
对于 [核心 API 提供的注解](../../core/annotation/about) 方式，除了 `@Call` 注解不支持之外，其它所有注解都可以在 Milvus 数据源上正常使用。
Milvus 使用的是 SQL 风格语法，注解中的命令写法与传统 SQL 非常接近。
:::

```java title='1. 定义对象'
@Table("book_vectors")
public class BookVector {
    @Column(value = "book_id", primary = true)
    private Long bookId;
    @Column("title")
    private String title;
    @Column("word_count")
    private Integer wordCount;
    @Column("book_intro")
    private List<Float> bookIntro;
    ... // 省略 getter/setter 方法
}
```

```java title='2. 定义 Mapper 接口'
@SimpleMapper
public interface BookVectorMapper {
    @Insert("INSERT INTO book_vectors (book_id, title, word_count, book_intro) "
          + "VALUES (#{bookId}, #{title}, #{wordCount}, #{bookIntro})")
    int saveBook(BookVector book);

    @Query("SELECT * FROM book_vectors WHERE book_id = #{bookId}")
    BookVector loadBook(@Param("bookId") Long bookId);

    @Delete("DELETE FROM book_vectors WHERE book_id = #{bookId}")
    int deleteBook(@Param("bookId") Long bookId);
}
```

```java title='3. 创建并使用 Mapper'
Configuration config = new Configuration();
Session session = config.newSession(dataSource);

BookVectorMapper mapper = session.createMapper(BookVectorMapper.class);
```

:::info[一致性级别]
Milvus 默认的一致性级别可能导致刚插入的数据无法立即查到。推荐在 JDBC 连接 URL 中添加 `consistencyLevel=Strong` 参数来确保即时可见：

```text
jdbc:dbvisitor:milvus://host:port?consistencyLevel=Strong
```
:::

### 分页查询

在 Mapper 方法中添加 `Page` 参数即可实现分页查询。

```java title='Mapper 定义'
@SimpleMapper
public interface BookVectorMapper {
    @Query("SELECT * FROM book_vectors WHERE word_count > 0")
    List<BookVector> queryAll(Page page);
}
```

```java title='调用分页'
Page page = new PageObject();
page.setPageSize(10);

List<BookVector> list = mapper.queryAll(page);
```

---

## 文件方式（Mapper File）{#exec-file}

```java title='1. 定义对象'
@Table("book_vectors")
public class BookVector {
    @Column(value = "book_id", primary = true)
    private Long bookId;
    @Column("title")
    private String title;
    @Column("word_count")
    private Integer wordCount;
    @Column("book_intro")
    private List<Float> bookIntro;
    ... // 省略 getter/setter 方法
}
```

```xml title='2. 定义 Mapper 文件'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
"https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="com.example.mapper.BookVectorMapper">
    <resultMap id="bookResultMap" type="com.example.dto.BookVector">
        <id property="bookId" column="book_id"/>
        <result property="title" column="title"/>
        <result property="wordCount" column="word_count"/>
        <result property="bookIntro" column="book_intro"/>
    </resultMap>

    <insert id="saveBook">
        INSERT INTO book_vectors (book_id, title, word_count, book_intro)
        VALUES (#{bookId}, #{title}, #{wordCount}, #{bookIntro})
    </insert>

    <select id="loadBook" resultMap="bookResultMap">
        SELECT * FROM book_vectors WHERE book_id = #{bookId}
    </select>

    <select id="queryAll" resultMap="bookResultMap">
        SELECT * FROM book_vectors LIMIT 100
    </select>

    <delete id="deleteBook">
        DELETE FROM book_vectors WHERE book_id = #{bookId}
    </delete>
</mapper>
```

```java title='3. 定义 Mapper 接口'
@RefMapper("/mapper/book-vector-mapper.xml")
public interface BookVectorMapper {
    int saveBook(BookVector book);

    BookVector loadBook(@Param("bookId") Long bookId);

    List<BookVector> queryAll();

    int deleteBook(@Param("bookId") Long bookId);
}
```

---

## 向量搜索 {#vector-search}

Milvus 的核心能力是向量相似性检索。dbVisitor 的构造器 API 提供了类型安全的向量搜索方法，无需手写 SQL，推荐优先使用。
完整的向量查询文档请参考 [构造器 API - 向量查询](../../core/lambda/vector)。

### KNN 近邻搜索（orderBy*）

使用 `orderByL2` / `orderByCosine` / `orderByIP` 按向量距离排序，配合 `initPage` 取前 K 条最相似记录：

```java title='L2 距离 TopK 搜索'
LambdaTemplate lambda = new LambdaTemplate(dataSource);
List<Float> target = Arrays.asList(0.1f, 0.2f);

List<BookVector> results = lambda.query(BookVector.class)
        .orderByL2(BookVector::getBookIntro, target)
        .initPage(10, 0)  // TopK = 10
        .queryForList();
```

```java title='带标量前置过滤的 KNN 搜索'
List<BookVector> results = lambda.query(BookVector.class)
        .gt(BookVector::getWordCount, 500)
        .orderByL2(BookVector::getBookIntro, target)
        .initPage(5, 0)
        .queryForList();
```

```java title='余弦距离搜索'
List<BookVector> results = lambda.query(BookVector.class)
        .orderByCosine(BookVector::getBookIntro, target)
        .initPage(10, 0)
        .queryForList();
```

### 范围搜索（vectorBy*）

使用 `vectorByL2` / `vectorByCosine` / `vectorByIP` 过滤出距离小于阈值的所有记录，属于 WHERE 条件，可与其他条件自由组合：

```java title='L2 距离范围过滤'
List<Float> target = Arrays.asList(0.1f, 0.2f);

List<BookVector> results = lambda.query(BookVector.class)
        .vectorByL2(BookVector::getBookIntro, target, 0.8)
        .queryForList();
```

```java title='混合条件：范围搜索 + 标量过滤'
List<BookVector> results = lambda.query(BookVector.class)
        .vectorByL2(BookVector::getBookIntro, target, 0.8)
        .gt(BookVector::getWordCount, 500)
        .queryForList();
```

### API 速查

| 查询模式 | 方法 | 说明 |
|---------|------|------|
| KNN 排序 | `orderByL2(property, vector)` | 按 L2 欧氏距离排序 |
| KNN 排序 | `orderByCosine(property, vector)` | 按余弦距离排序 |
| KNN 排序 | `orderByIP(property, vector)` | 按内积距离排序 |
| KNN 排序 | `orderByMetric(MetricType, property, vector)` | 通用度量接口 |
| 范围过滤 | `vectorByL2(property, vector, threshold)` | L2 距离 < 阈值 |
| 范围过滤 | `vectorByCosine(property, vector, threshold)` | 余弦距离 < 阈值 |
| 范围过滤 | `vectorByIP(property, vector, threshold)` | 内积距离 < 阈值 |

:::tip[SQL 命令方式]
如需直接使用 SQL 风格语法进行向量搜索（如 `ORDER BY field <-> vector`、`vector_range()` 等），请参考 [支持的命令列表](../../../drivers/milvus/commands#dql)。
:::
