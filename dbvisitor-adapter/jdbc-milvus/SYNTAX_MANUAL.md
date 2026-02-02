# Milvus JDBC Adapter Syntax Manual

> **注意**：本驱动的部分高级特性（如 `count` 统计、BulkInsert 等）依赖 Milvus 2.2+ 以上版本。建议使用 Milvus 2.3.x 或更高版本以获得最佳体验。

本文档详细介绍了 Milvus JDBC Adapter 所支持的 SQL 语法。相比官方 CLI，本 Adapter 采用了更符合标准 SQL 习惯的语法结构，支持数据库管理、表管理、数据操作及查询功能。

## 1. 数据库管理 (Database Management)

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

## 2. 表与分区管理 (Table & Partition Management)

### 创建表 (Collection)
支持定义主键、向量字段及其他属性。
```sql
CREATE TABLE [IF NOT EXISTS] table_name (
    id INT64 PRIMARY KEY,
    vector_col FLOAT_VECTOR(128),
    age INT32 DEFAULT 0
) WITH (
    consistency_level = "Strong"
);
```

### 向量数据格式支持
在 `INSERT`, `SEARCH` (SELECT ... ORDER BY vector), `DELETE` 等包含向量操作的语句中，支持多种向量表达形式：
1. **JSON 数组字面量**:
   - `[0.1, 0.2, 0.3, ...]`
2. **JDBC 参数绑定**:
   - `?` (PreparedStatement)
   - 支持绑定的 Java 类型: `List<Float>`, `List<Double>` (自动转换), `float[]`, `double[]`。
3. **批量向量**:
   - 在 `SEARCH` 中支持一次传入多个向量进行批量搜索: `[[0.1, 0.2], [0.3, 0.4]]`。

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
SHOW TABLES;                        -- 列出所有表
SHOW CREATE TABLE table_name;       -- 查看建表详细语句
```

### 分区管理
```sql
CREATE PARTITION [IF NOT EXISTS] partition_name ON TABLE table_name;
DROP PARTITION [IF EXISTS] partition_name ON TABLE table_name;
SHOW PARTITIONS FROM table_name;    -- 列出某表的所有分区
SHOW PARTITION p_name ON TABLE t_name;
```

### 别名管理
```sql
CREATE ALIAS alias_name FOR TABLE table_name;
ALTER ALIAS alias_name FOR TABLE table_name;
DROP ALIAS [IF EXISTS] alias_name;
```

---

## 3. 索引管理 (Index Management)

### 创建索引
```sql
CREATE INDEX index_name ON TABLE table_name (vector_col) USING "IVF_FLAT" WITH (nlist = 1024);
```

#### 支持的索引类型 (USING)
- **向量索引 (Vector Indices)**:
  - 内存索引: `FLAT`, `IVF_FLAT`, `IVF_SQ8`, `IVF_PQ`, `HNSW`, `SCANN`, `AUTOINDEX`
  - 磁盘索引: `DISKANN`
  - GPU 索引: `GPU_IVF_FLAT`, `GPU_IVF_PQ`
  - 旧版/其他: `RNSG`, `ANNOY`
  - 稀疏向量: `SPARSE_INVERTED_INDEX`
- **标量索引 (Scalar Indices)**:
  - `STL_SORT` (排序索引，用于数值)
  - `Trie` (字典树索引，用于字符串)
  - `INVERTED` (倒排索引，用于关键词检索)

### 删除索引
```sql
DROP INDEX index_name ON TABLE table_name;
```

### 查看索引
```sql
SHOW INDEXES FROM table_name;
SHOW INDEX index_name ON TABLE table_name;
```

---

## 4. 用户与权限管理 (User & Role Management)

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
GRANT ROLE role_name TO username;
REVOKE ROLE role_name FROM username;
SHOW GRANTS FOR ROLE role_name ON GLOBAL;
SHOW GRANTS FOR ROLE role_name ON TABLE table_name;
```

---

## 5. 数据操作 (DML)

### 插入数据
```sql
-- 插入到默认分区
INSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- 插入到指定分区
INSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);
```

### 删除数据 (DELETE)

> **注意**： Milvus 的删除操作是逻辑删除，底层通过写入 Delete log 实现。由于 Milvus 原生删除操作主要依赖主键（或基于标量表达式的过滤），本驱动实现向量相关删除时采用了 "Search-to-Delete"（先搜索主键再删除）的策略。

#### 1. 基础删除 (标量过滤)
支持标准的 SQL `WHERE` 子句进行过滤删除。

```sql
-- 按主键删除
DELETE FROM table_name WHERE id = 1;
DELETE FROM table_name WHERE id IN [1, 2, 3];

-- 按标量条件删除 (需开启 partition 支持或全表 scan)
DELETE FROM table_name WHERE age > 18 AND status = 'inactive';

-- 指定分区删除
DELETE FROM table_name PARTITION partition_name WHERE age > 10;
```

#### 2. 最近邻删除 (KNN Delete)
删除距离目标向量最近的 K 条记录。需配合 `ORDER BY` 和 `LIMIT` 使用。
底层机制：执行 TopK 搜索获取主键 -> 执行删除。

```sql
-- 删除距离 [0.1, 0.2] 最近的 100 条记录
DELETE FROM table_name ORDER BY vector_col <-> [0.1, 0.2] LIMIT 100;

-- 结合标量过滤：删除 category='book' 且最相似的 10 条
DELETE FROM table_name WHERE category = 'book' ORDER BY vector_col <-> [0.1, 0.2] LIMIT 10;
```

#### 3. 范围删除 (Range Delete)
删除所有落在目标向量指定距离（半径）内的记录。使用 `WHERE` 子句中的 `<->` 运算符配合比较操作符。
底层机制：执行 Range Search 获取主键 -> 执行删除。

```sql
-- 删除所有距离 [0.1, 0.2] 小于 0.5 的记录
DELETE FROM table_name WHERE vector_col <-> [0.1, 0.2] < 0.5;

-- 结合 LIMIT 进行保护 (最多删除符合条件的 1000 条)
DELETE FROM table_name WHERE vector_col <-> [0.1, 0.2] < 0.5 LIMIT 1000;
```

### 数据导入 (Import)
```sql
-- 从文件导入 (用于服务端文件加载)
IMPORT FROM 'path/to/file.csv' INTO TABLE table_name;
IMPORT FROM 'file.json' INTO TABLE table_name PARTITION partition_name;
```

### 加载与释放 (Load / Release)
Milvus 要求在搜索前将 Collection 加载到内存。
```sql
LOAD TABLE table_name [PARTITION partition_name];
RELEASE TABLE table_name [PARTITION partition_name];
```

---

## 6. 查询与搜索 (DQL)

本适配器统一使用 `SELECT` 语法进行标量查询（Query）和向量相似度搜索（Search）。

### 标量查询 (Query)
用于精确匹配或范围过滤。
```sql
-- 查询所有字段
SELECT * FROM table_name;

-- 带条件过滤
SELECT * FROM table_name WHERE age > 20 AND status = 1;

-- 指定返回字段与分页
SELECT id, name FROM table_name LIMIT 10 OFFSET 0;

-- 查询指定分区
SELECT * FROM table_name PARTITION partition_name WHERE tag = 'A';
```

### 向量搜索 (Search)
使用特有的 `<->` 运算符表示向量距离计算。

```sql
-- 基本搜索 (默认参数)
SELECT * FROM table_name ORDER BY vector_col <-> [0.1, 0.2, ...] LIMIT 10;

-- 带前置过滤的搜索
SELECT * FROM table_name WHERE category = 'book' ORDER BY vector_col <-> [0.1, ...] LIMIT 5;

-- 范围搜索/距离过滤 (Range Search)
-- 语法：WHERE vector_col <-> [vector] < distance_threshold
SELECT * FROM table_name WHERE vector_col <-> [0.1, 0.2] < 0.8 LIMIT 5;
```

### 高级搜索参数 (WITH 子句)
在 `SELECT` 语句末尾使用 `WITH` 子句传递 Milvus 特定的搜索参数。

支持参数：
- `metric_type`: 距离度量类型 (L2, IP, COSINE 等)
- `params`: 索引特定参数 (JSON 字符串，如 `{"nprobe": 10}`)
- `consistency_level`: 一致性级别 (Strong, Session, Bounded, Eventually)
- `round_decimal`: 结果小数位精度
- `out_fields`: (通常不需手动指定，由 SELECT 列表决定)

示例：
```sql
SELECT id, score FROM table_name 
ORDER BY vector_col <-> [0.1, 0.2] 
LIMIT 10 
WITH (
    metric_type = "L2", 
    params = "{\"nprobe\": 10}", 
    consistency_level = "Strong"
);
```

### 统计总数 (Count)
本适配器支持使用 `count` 语法查询集合或分区的记录总数。
> **要求**：Milvus 服务端版本需 >= 2.2.0。

```sql
-- 查询全表总数
count from table_name;

-- 查询指定分区的总数
count from table_name partition(partition_name);

-- 带条件过滤的总数 (支持标量过滤)
count from table_name where age > 18;
```

### 查询提示 (Hints)
本适配器支持使用 SQL Hint (`/*+ ... */`) 来覆盖默认的分页或查询行为，类似于 Elastic/Mongo 适配器中的用法。

支持的 Hint：
- `overwrite_find_limit`: 强制覆盖查询的 LIMIT (TopK)。
- `overwrite_find_skip`: 强制覆盖查询的 OFFSET。
- `overwrite_find_as_count`: 将当前的查询强制转换为 COUNT 查询 (忽略 SELECT 字段和排序)。

示例：

```sql
-- 强制限制返回 5 条记录，跳过前 10 条
/*+ overwrite_find_limit=5, overwrite_find_skip=10 */
SELECT * FROM table_name WHERE status = 1;

-- 使用 Hint 获取匹配条件的记录总数 (等同于 count from ... where ...)
/*+ overwrite_find_as_count=true */
SELECT * FROM table_name WHERE age > 20;
```

---

## 7. 进度监控 (Progress)

### 查看加载进度
```sql
SHOW PROGRESS OF LOADING ON TABLE table_name;
SHOW PROGRESS OF LOADING ON TABLE table_name PARTITION partition_name;
```

### 查看索引构建进度
```sql
SHOW PROGRESS OF INDEX ON TABLE table_name;
SHOW PROGRESS OF INDEX index_name ON TABLE table_name;
```
