---
id: commands
sidebar_position: 3
hide_table_of_contents: true
title: 语法手册
description: jdbc-milvus 支持的 SQL 风格语法手册，涵盖数据库管理、表管理、索引管理、分区管理、数据操作、查询搜索、用户权限等。
---

jdbc-milvus 通过 ANTLR4 解析 SQL 风格命令，将其转换为 Milvus SDK API 调用。本文档详细介绍所有支持的 SQL 语法。

:::tip[版本要求]
部分高级特性（如 `COUNT`、BulkInsert）依赖 Milvus 2.2+ 版本。建议使用 Milvus 2.3.x 或更高版本。
:::

---

## 数据库管理 {#database}

### 创建数据库

```sql
CREATE DATABASE [IF NOT EXISTS] db_name;
```

### 删除数据库

```sql
DROP DATABASE [IF EXISTS] db_name;
```

### 修改数据库属性

```sql
ALTER DATABASE db_name SET PROPERTIES ("key" = "value", ...);
```

### 查看数据库列表

```sql
SHOW DATABASES;
```

---

## 表管理（Collection） {#table}

### 创建表

支持定义主键、向量字段及其他属性。

```sql
CREATE TABLE [IF NOT EXISTS] table_name (
    id       INT64 PRIMARY KEY,
    vector   FLOAT_VECTOR(128),
    age      INT32 DEFAULT 0
) WITH (
    consistency_level = "Strong"
);
```

**支持的字段类型：**

| SQL 类型 | Milvus 类型 | 参数 |
|---|---|---|
| `bool` | Bool | — |
| `int8` / `int16` / `int32` / `int64` | Int8 / Int16 / Int32 / Int64 | — |
| `float` / `double` | Float / Double | — |
| `json` | JSON | — |
| `varchar(N)` | VarChar | N = 最大长度 |
| `array` | Array | — |
| `float_vector(N)` | FloatVector | N = 维度 |
| `binary_vector(N)` | BinaryVector | N = 维度 |
| `float16_vector(N)` | Float16Vector | N = 维度 |
| `bfloat16_vector(N)` | BFloat16Vector | N = 维度 |
| `sparse_float_vector(N)` | SparseFloatVector | N = 维度 |

**字段约束：**

| 约束 | 说明 |
|---|---|
| `PRIMARY KEY` | 标记为主键 |
| `AUTO_ID` | 自动生成 ID |
| `NOT NULL` | 非空约束 |
| `DEFAULT value` | 默认值 |
| `COMMENT 'text'` | 字段注释 |

**完整建表示例：**

```sql
CREATE TABLE book_vectors (
    book_id    INT64 PRIMARY KEY,
    title      VARCHAR(256) NOT NULL COMMENT '书名',
    word_count INT32 DEFAULT 0,
    book_intro FLOAT_VECTOR(128)
) WITH (
    consistency_level = "Strong"
);
```

### 删除表

```sql
DROP TABLE [IF EXISTS] table_name;
```

### 重命名表

```sql
ALTER TABLE old_name RENAME TO new_name;
```

### 查看表

```sql
-- 列出所有表
SHOW TABLES;

-- 查看表信息
SHOW TABLE table_name;

-- 查看建表语句
SHOW CREATE TABLE table_name;
```

---

## 索引管理 {#index}

### 创建索引

```sql
CREATE INDEX index_name ON TABLE table_name (vector_col)
    USING "IVF_FLAT" WITH (nlist = 1024, metric_type = "L2");
```

**支持的向量索引类型（USING）：**

- 内存索引：`FLAT`、`IVF_FLAT`、`IVF_SQ8`、`IVF_PQ`、`HNSW`、`SCANN`、`AUTOINDEX`
- 磁盘索引：`DISKANN`
- GPU 索引：`GPU_IVF_FLAT`、`GPU_IVF_PQ`
- 稀疏向量：`SPARSE_INVERTED_INDEX`

**支持的标量索引类型：**

- `STL_SORT` — 排序索引，适用于数值字段
- `Trie` — 字典树索引，适用于字符串字段
- `INVERTED` — 倒排索引，适用于关键词检索

**向量索引与标量索引混合示例：**

```sql
CREATE INDEX idx_intro ON TABLE book_vectors (book_intro)
    USING "IVF_FLAT" WITH (nlist = 1024, metric_type = "L2");

CREATE INDEX idx_title ON TABLE book_vectors (title) USING "INVERTED";
```

### 删除索引

```sql
DROP INDEX index_name ON TABLE table_name;
```

### 查看索引

```sql
-- 列出表的所有索引
SHOW INDEXES FROM table_name;

-- 查看指定索引信息
SHOW INDEX index_name ON TABLE table_name;

-- 查看索引构建进度
SHOW PROGRESS OF INDEX index_name ON TABLE table_name;
```

---

## 分区管理 {#partition}

### 创建分区

```sql
CREATE PARTITION [IF NOT EXISTS] partition_name ON TABLE table_name;
```

### 删除分区

```sql
DROP PARTITION [IF EXISTS] partition_name ON TABLE table_name;
```

### 查看分区

```sql
-- 列出表的所有分区
SHOW PARTITIONS FROM table_name;

-- 查看指定分区信息
SHOW PARTITION partition_name ON TABLE table_name;
```

---

## 别名管理 {#alias}

```sql
-- 创建别名
CREATE ALIAS alias_name FOR TABLE table_name;

-- 修改别名指向的集合
ALTER ALIAS alias_name FOR TABLE table_name;

-- 删除别名
DROP ALIAS [IF EXISTS] alias_name;
```

---

## 数据操作（DML） {#dml}

### 插入数据

```sql
-- 插入到默认分区
INSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- 插入到指定分区
INSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);
```

### Upsert（插入或覆盖）

```sql
-- 默认分区
UPSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- 指定分区
UPSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);
```

### 更新数据

:::caution[注意]
Milvus 的 Update 本质是 "Search-to-Upsert"：先查询符合条件的记录，在内存中修改对应字段，然后 Upsert 回写。**不建议对海量数据进行全表 Update**。
:::

#### 基础更新（标量过滤）

```sql
-- 按主键更新
UPDATE table_name SET age = 20 WHERE id = 1;

-- 按标量条件更新（查询后回写）
UPDATE table_name SET status = 'active' WHERE age > 18;
```

#### KNN 更新

更新距离目标向量最近的 K 条记录。底层机制：执行 TopK 搜索 → 内存修改 → Upsert。

```sql
UPDATE table_name SET status = 1
    ORDER BY vector_col <-> [0.1, 0.2] LIMIT 1;
```

#### 范围更新

更新所有落在目标向量指定距离（半径）内的记录。

```sql
-- 使用 vector_range 函数（推荐）
UPDATE table_name SET tag = 'A'
    WHERE vector_range(vector_col, [0.1, 0.2], 0.5);

-- 使用比较表达式
UPDATE table_name SET tag = 'A'
    WHERE vector_col <-> [0.1, 0.2] < 0.5;
```

### 删除数据

:::caution[注意]
Milvus 的删除是逻辑删除，底层通过写入 Delete log 实现。向量相关的删除采用 "Search-to-Delete"（先搜索主键再删除）策略。
:::

#### 基础删除（标量过滤）

```sql
-- 按主键删除
DELETE FROM table_name WHERE id = 1;
DELETE FROM table_name WHERE id IN [1, 2, 3];

-- 按标量条件删除
DELETE FROM table_name WHERE age > 18 AND status = 'inactive';

-- 指定分区删除
DELETE FROM table_name PARTITION partition_name WHERE age > 10;
```

#### KNN 删除

删除距离目标向量最近的 K 条记录。底层机制：执行 TopK 搜索获取主键 → 执行删除。

```sql
-- 删除距离 [0.1, 0.2] 最近的 100 条记录
DELETE FROM table_name
    ORDER BY vector_col <-> [0.1, 0.2] LIMIT 100;

-- 结合标量过滤
DELETE FROM table_name
    WHERE category = 'book'
    ORDER BY vector_col <-> [0.1, 0.2] LIMIT 10;
```

#### 范围删除

删除所有落在目标向量指定距离（半径）内的记录。底层机制：执行 Range Search 获取主键 → 执行删除。

```sql
-- 使用 vector_range 函数（推荐）
DELETE FROM table_name
    WHERE vector_range(vector_col, [0.1, 0.2], 0.5);

-- 使用比较表达式
DELETE FROM table_name
    WHERE vector_col <-> [0.1, 0.2] < 0.5;

-- 结合 LIMIT 保护（最多删除 1000 条）
DELETE FROM table_name
    WHERE vector_range(vector_col, [0.1, 0.2], 0.5) LIMIT 1000;
```

### 数据导入

```sql
IMPORT FROM 'path/to/file.csv' INTO TABLE table_name;
IMPORT FROM 'file.json' INTO TABLE table_name PARTITION partition_name;
```

---

## 加载与释放 {#load}

Milvus 要求在搜索前将 Collection 加载到内存。

```sql
-- 加载集合
LOAD TABLE table_name;

-- 加载指定分区
LOAD TABLE table_name PARTITION partition_name;

-- 释放集合
RELEASE TABLE table_name;

-- 释放指定分区
RELEASE TABLE table_name PARTITION partition_name;
```

### 查看加载进度

```sql
SHOW PROGRESS OF LOADING ON TABLE table_name;
SHOW PROGRESS OF LOADING ON TABLE table_name PARTITION partition_name;
```

---

## 查询与搜索（DQL） {#dql}

本适配器统一使用 `SELECT` 语法进行标量查询（Query）和向量相似度搜索（Search）。

### 标量查询

```sql
-- 查询所有字段
SELECT * FROM table_name;

-- 条件过滤
SELECT * FROM table_name WHERE age > 20 AND status = 1;

-- 指定返回字段与分页
SELECT id, name FROM table_name LIMIT 10 OFFSET 0;

-- 查询指定分区
SELECT * FROM table_name PARTITION partition_name WHERE tag = 'A';
```

### 向量搜索（KNN）

使用 `<->` 运算符表示向量距离，配合 `ORDER BY` 和 `LIMIT` 实现 TopK 近邻搜索。

```sql
-- 基本搜索（默认参数）
SELECT * FROM table_name
    ORDER BY vector_col <-> [0.1, 0.2, ...] LIMIT 10;

-- 带前置过滤
SELECT * FROM table_name
    WHERE category = 'book'
    ORDER BY vector_col <-> [0.1, 0.2] LIMIT 5;

-- 使用 PreparedStatement 参数绑定
SELECT * FROM table_name
    ORDER BY vector_col <-> ? LIMIT 10;
```

### 向量范围搜索

```sql
-- 方式 1：使用 vector_range 函数（推荐）
SELECT * FROM table_name
    WHERE vector_range(vector_col, [0.1, 0.2], 0.8) LIMIT 5;

-- 方式 2：使用比较表达式
SELECT * FROM table_name
    WHERE vector_col <-> [0.1, 0.2] < 0.8 LIMIT 5;
```

### 搜索参数（WITH 子句）

在 `SELECT` 语句末尾使用 `WITH` 子句传递 Milvus 特定的搜索参数。

```sql
SELECT id, score FROM table_name
    ORDER BY vector_col <-> [0.1, 0.2] LIMIT 10
    WITH (
        metric_type = "L2",
        params = "{\"nprobe\": 10}",
        consistency_level = "Strong"
    );
```

**支持的参数：**
- `metric_type` — 距离度量类型（`L2`、`IP`、`COSINE` 等）
- `params` — 索引特定参数（JSON 字符串，如 `{"nprobe": 10}`）
- `consistency_level` — 一致性级别（`Strong`、`Session`、`Bounded`、`Eventually`）
- `round_decimal` — 结果小数位精度

### 统计总数

:::tip
需要 Milvus 服务端 >= 2.2.0。
:::

```sql
-- 查询全表总数
COUNT FROM table_name;

-- 查询指定分区的总数
COUNT FROM table_name PARTITION(partition_name);

-- 带条件过滤
COUNT FROM table_name WHERE age > 18;
```

### 向量数据格式

在 `INSERT`、`SELECT`（向量搜索）、`DELETE` 等包含向量操作的语句中，支持多种向量表达形式：

1. **JSON 数组字面量** — `[0.1, 0.2, 0.3, ...]`
2. **JDBC 参数绑定** — `?`（PreparedStatement），支持绑定 `List<Float>`、`List<Double>`（自动转换）、`float[]`、`double[]`
3. **批量向量** — 在搜索中一次传入多个向量：`[[0.1, 0.2], [0.3, 0.4]]`

### 距离运算符

| 运算符 | 含义 |
|---|---|
| `<->` | 通用距离（常用于 L2） |
| `<=>` | 余弦距离 |
| `<#>` | 内积距离 |
| `~=` | 近似匹配 |
| `<%>` | 百分比距离 |
| `<?>` | 未知/自定义距离 |

---

## 用户与权限管理 {#user}

### 用户管理

```sql
CREATE USER [IF NOT EXISTS] username PASSWORD 'password';
DROP USER [IF EXISTS] username;
SHOW USERS;
```

### 角色管理

```sql
CREATE ROLE [IF NOT EXISTS] role_name;
DROP ROLE [IF EXISTS] role_name;
SHOW ROLES;
```

### 授权与撤销

```sql
-- 将角色授予用户
GRANT ROLE role_name TO username;
REVOKE ROLE role_name FROM username;

-- 授予角色权限
GRANT privilege ON objectType objectName TO ROLE role_name;
REVOKE privilege ON objectType objectName FROM ROLE role_name;

-- 查看角色权限
SHOW GRANTS FOR ROLE role_name ON GLOBAL;
SHOW GRANTS FOR ROLE role_name ON TABLE table_name;
```

---

## Hint 支持 {#hint}

通过 SQL Hint (`/*+ ... */`) 覆盖或增强查询行为。Hint 必须位于 SQL 语句开头。

### overwrite_find_limit

强制覆盖查询的 LIMIT（TopK）。

```sql
/*+ overwrite_find_limit=5 */
SELECT * FROM table_name WHERE status = 1;
```

### overwrite_find_skip

强制覆盖查询的 OFFSET。

```sql
/*+ overwrite_find_limit=5, overwrite_find_skip=10 */
SELECT * FROM table_name WHERE status = 1;
```

### overwrite_find_as_count

将当前查询强制转换为 COUNT 查询（忽略 SELECT 字段和排序）。

```sql
/*+ overwrite_find_as_count=true */
SELECT * FROM table_name WHERE age > 20;
```

### consistency_level

:::caution[已移除]
`consistency_level` Hint 已被移除。请改用 JDBC 连接参数 `consistencyLevel` 在连接级别统一设置一致性级别。

```text
jdbc:dbvisitor:milvus://host:port?consistencyLevel=Strong
```

详见 [连接参数](params.md)。
:::

---

## 数据刷新 {#flush}

将指定集合的内存数据刷新到持久化存储。

```sql
FLUSH collection_name;
```

:::tip
在大多数场景中，推荐使用 `consistencyLevel=Strong` 连接参数而非 `FLUSH`，因为 `FLUSH` 是较重的持久化操作。`FLUSH` 更适合批量写入后确保数据落盘的场景。
:::

---

## 进度监控 {#progress}

```sql
-- 查看加载进度
SHOW PROGRESS OF LOADING ON TABLE table_name;
SHOW PROGRESS OF LOADING ON TABLE table_name PARTITION partition_name;

-- 查看索引构建进度
SHOW PROGRESS OF INDEX ON TABLE table_name;
SHOW PROGRESS OF INDEX index_name ON TABLE table_name;
```
