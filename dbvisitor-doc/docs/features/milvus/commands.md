---
id: commands
sidebar_position: 3
title: 语法手册
description: 当前 jdbc-milvus SQL 子集、参数规则、DDL、DML、向量查询、融合搜索和 JDBC 返回约定。
---

> **版本基线**：本文适用于 6.7.1-SNAPSHOT，Java 17、Java SDK 2.6.22，服务端最低基线 Milvus 2.6.2。不支持 2.5.x 及更早版本；后续版本不自动纳入支持范围，参阅[版本与支持范围](./compatibility.md)。

连接与依赖见[安装与连接](../../drivers/milvus/connection.mdx)，完整入门程序见[JDBC 操作用法](./jdbc.mdx)。本文以当前解析器和命令实现为准，不把完整 Milvus SDK 或关系数据库 SQL 当作隐含支持。

TLS、证书和 Cloud 使用 JDBC 连接属性，不增加 SQL 语法或 Hint。`secure`、`caPemPath`/`serverPemPath`、`clientPemPath`/`clientKeyPath`、`serverName` 对 SDK 与 Import REST 共用，两者始终访问 JDBC URL 指定的主机和端口。Milvus 2.6.2 原生 TLS 的两个内部监听需要通过统一入口提供完整 Import 能力；驱动不自动连接其他端口。参数组合、证书格式、部署限制和示例见[连接参数与 TLS](../../drivers/milvus/connection.mdx#tls)。

## 阅读约定

- `text` 代码块是语法模板：`[IF EXISTS]` 等方括号表示可选部分，不要原样执行；`sql` 块是语法示例，替换集合/字段名并满足 schema 后使用。向量 `[0.1, 0.2]` 的方括号属于真实语法。
- SQL 关键字不区分大小写；名称使用普通标识符，示例采用字母/下划线开头。当前不提供带引号标识符或跨库限定名解析；字符串用单/双引号，不能将字符串引用当作字段名引用。
- ? 用于 WHERE、向量、SET、VALUES、LIMIT/OFFSET、Hint、查询/字段/function/索引及集合 CREATE TABLE WITH 的值。名称、类型和约束关键字不可绑定。
- SELECT 支持 `*`、字段列表或独立的 `COUNT(*)`，不支持 AS、JOIN、GROUP BY、任意投影表达式或标量 ORDER BY。距离排序仅支持本文列出的单查询向量操作；不支持多个排序键。
- 标量 WHERE 支持比较、AND/OR/NOT、括号、BETWEEN/NOT BETWEEN、LIKE、IN/NOT IN 列表、IS NULL/IS NOT NULL 及部分表达式透传；不是所有关系数据库函数。BETWEEN 包含两个边界，转换为原生 `>=` 和 `<=`，边界支持值参数；不调换反向边界。IN 和 NOT IN 支持 [1,2]、(1,2) 或绑定 List，列表元素也可以使用参数。SQL NULL 可用于值；一般负数字面量仍请用参数绑定（DEFAULT 单独支持正负号）。
- Milvus 2.6.2 不接受 LIKE 右侧的模板参数，因此 `LIKE ?` 虽可解析并通过 SDK 绑定，执行时仍会被服务端拒绝。固定 SQL 模式字面量可用；不要拼接不可信输入。另外，原生 SDK 的整数模板比较 `NOT (age = ?)` 及双重 NOT 可触发 2.6.2 QueryNode 断言；普通 `age != ?`、字面量 NOT、`NOT (field IS NULL)` 不属于该问题。驱动保留 SQL 的 NOT 运算范围，将 `<>` 转为原生 `!=`，但不为服务端问题拼接参数或消除 NOT 兜底。
- 同一请求可用分号分隔多条 SQL，参数从第一条到最后一条连续编号，返回通过标准 JDBC 多结果接口读取。它不构成事务，也不是 JDBC batch。


## 1. 数据库管理 (Database Management) {#database}

### 创建数据库

```text
CREATE DATABASE [IF NOT EXISTS] db_name;
CREATE DATABASE [IF NOT EXISTS] db_name WITH ("key" = "value", ...);
```

WITH 的值可用 `?` 绑定，属性作为字符串传给原生 CreateDatabaseReq。IF NOT EXISTS 遇到已有数据库不修改其属性，修改时使用 ALTER DATABASE。

### 删除数据库

```text
DROP DATABASE [IF EXISTS] db_name;
```

### 修改数据库属性

```text
ALTER DATABASE db_name SET PROPERTIES ("key" = "value", ...);
ALTER DATABASE db_name DROP PROPERTIES ("key", ...);
```

SET 的值可使用 `?` 绑定，属性名不能绑定。DROP 只移除所列属性，不删除数据库。

### 查看数据库列表

```sql
SHOW DATABASES;
SHOW DATABASE db_name;
```

SHOW DATABASES 返回数据库名称列表；SHOW DATABASE 返回一行 `DATABASE`、`PROPERTIES`。PROPERTIES 是 JSON 对象文本，可用 `ResultSet.getString("PROPERTIES")` 读取后解析，包含服务端实际返回的属性；数据库不存在或无权限时返回 SQLException，不返回伪造的空属性。属性的名称、取值范围和动态生效条件由 Milvus 服务端决定。

---

## 2. 表与分区管理 (Table & Partition Management) {#table}

### 创建表 (Collection)

支持定义主键、向量字段及其他属性。
```text
CREATE TABLE [IF NOT EXISTS] table_name (
    id INT64 PRIMARY KEY,
    vector_col FLOAT_VECTOR(2),
    age INT32 DEFAULT 0
) WITH (
    consistency_level = "Strong"
);
```

### 字段类型与建表选项

| 类型/约束 | 当前实现边界 |
| --- | --- |
| `BOOL`、`INT8/16/32/64`、`FLOAT`、`DOUBLE` | 标量字段类型；布尔类型的 DDL 关键字为 BOOL。 |
| `VARCHAR(n)` | n 对应 max_length；主键可使用 Int64 或 VarChar，具体合法性由 SDK/服务端校验。 |
| `JSON` | JSON 字段；INSERT/UPSERT 按 schema 编码，不等同于支持所有 JSON 查询表达式。 |
| `FLOAT_VECTOR(dim)` | 已打通数值 List/基本类型数组的主要读写搜索链路。 |
| `BINARY_VECTOR(dim)`、`FLOAT16_VECTOR(dim)`、`BFLOAT16_VECTOR(dim)`、`SPARSE_FLOAT_VECTOR` | 完整映射读写、KNN/范围搜索；Sparse 无需维度，旧 `(dim)` 兼容解析但不向 SDK 传递。 |
| `INT8_VECTOR(dim)` | 每维一个 -128～127 的有符号整数，读写使用字节编码，支持 KNN/范围和 Hybrid 搜索；使用 HNSW 索引，不自动量化浮点向量。 |
| `ARRAY<element_type>(max_capacity)` | BOOL、INT8/16/32/64、FLOAT、DOUBLE、VARCHAR(n) 元素，容量 1–4096；不允许嵌套或 NULL 元素，裸 ARRAY 明确拒绝。 |
| `PRIMARY KEY`、`AUTO_ID`、`NULL`、`NOT NULL`、`COMMENT 'text'` | 默认 NOT NULL，NULL 开启 nullable；主键不可 nullable，冲突约束拒绝。动态字段默认关闭。 |

CREATE TABLE 的 WITH 接受以下选项，值可用 `?` 绑定；未知名称、重复名称、NULL 和错误类型会报错，不会被静默忽略。字段、函数、集合选项中的参数按 SQL 出现顺序绑定。不要将 DQL 的 WITH 或连接参数规则套用到 DDL。

| 创建选项 | SDK 字段 | 类型与默认值 |
| --- | --- | --- |
| `consistency_level` | consistencyLevel | Strong / Bounded / Session / Eventually，默认 SDK Bounded。 |
| `num_partitions` | numPartitions | 正 INT32 整数，必须同时声明 PARTITION KEY；省略时使用服务端默认分区数。 |
| `num_shards` | numShards | 正 INT32 整数，默认 SDK 1；最终范围由服务端校验。 |
| `description` | description | 字符串，默认空；可使用 setString 或 setObject 绑定，不作为 SQL 执行。 |

### 分区键与聚簇键 {#collection-keys}

```sql
CREATE TABLE tenant_books (
    id INT64 PRIMARY KEY,
    tenant VARCHAR(64) PARTITION KEY,
    age INT32 CLUSTERING KEY,
    v FLOAT_VECTOR(2)
) WITH (num_partitions=4, num_shards=1, consistency_level=Strong);
```

`PARTITION KEY` 映射到 SDK `isPartitionKey`，支持非主键、非 nullable 的 INT64 或 VARCHAR 字段。一个集合最多一个分区键；Milvus 根据键值分配物理分区，驱动不计算路由。`num_partitions` 不是租户数量或主机数量，查询仍通过 WHERE 绑定键值，不能依赖分区实现权限隔离。分区键模式下的手动分区操作受服务端限制，见[官方分区键说明](https://milvus.io/docs/v2.6.x/use-partition-key.md)。

`CLUSTERING KEY` 映射到 `isClusteringKey`，一个集合最多一个，可与分区键使用同一字段。支持的标量类型和实际压缩条件由 SDK/服务端决定；设置标志本身不触发压缩，也不保证性能提升。已有 `COMPACT ... WITH (is_clustering=true)` 可提交原生任务，但集群需配置聚簇压缩和数据裁剪能力，见[官方聚簇压缩说明](https://milvus.io/docs/v2.6.x/clustering-compaction.md)。不通过 ADD COLUMN 后补分区键或聚簇键。

SHOW TABLE 在末尾增加 `PARTITION_KEY`、`CLUSTERING_KEY` 布尔列；SHOW CREATE 保留这些约束以及服务端返回的分区数、分片数、一致性和说明。它不是包含索引、权限及任意集合属性的完整备份脚本。

### DEFAULT 默认值

支持非主键标量字段的 `DEFAULT`：`BOOL`、`INT8/INT16/INT32/INT64`、`FLOAT/DOUBLE`、`VARCHAR`。数值允许正负号；整数默认值必须精确落在字段范围内，浮点值必须有限，字符串不能超过声明的 UTF-8 字节长度。非法值、重复 DEFAULT，以及主键、JSON、Array、向量字段的 DEFAULT 会在发送建表请求前被拒绝。

普通 INSERT/完整 UPSERT 省略字段或绑定 null 时，按 SDK 规则由 Milvus 应用 DEFAULT；无 DEFAULT 的 nullable 字段保存 NULL，非 nullable 字段由 SDK/服务端校验。SHOW CREATE 保留默认值、nullable、Array 元素/容量、分析器和函数。向量 nullable 需 2.6.18+，不支持向量 IS NULL/IS NOT NULL；标量 nullable/Array 维持 2.6.2 基线。

### 向量数据格式支持

在 `INSERT`, `SEARCH` (SELECT ... ORDER BY vector), `DELETE` 等包含向量操作的语句中，支持多种向量表达形式：

1. **SQL 数组字面量**:
   - `[0.1, 0.2]`
2. **JDBC 参数绑定**:
   - `?` (PreparedStatement)
   - FloatVector 支持数值列表 `List<? extends Number>`，例如 `List<Byte>`、`List<Short>`、`List<Integer>`、`List<Long>`、`List<Float>`、`List<Double>`。
   - 支持六种一维数值基本类型数组：`byte[]`、`short[]`、`int[]`、`long[]`、`float[]`、`double[]`。使用 `PreparedStatement.setObject(index, vector)` 绑定。
   - 适用于 FloatVector 的 INSERT/UPSERT，以及 SELECT、UPDATE、DELETE 的 KNN 和范围向量条件（`vector_range` 或距离比较表达式）。UPDATE 的 SET 字段通过原生 Partial Update 发送。
   - 向量查询和 INSERT/UPSERT 将各元素转换为 Float；较大的整数和 double 可能损失精度。`byte[]` 按有符号数值逐元素转换，不表示 BinaryVector 的位数据。
   - 不支持将 `boolean[]`、`char[]`、包装类型数组（例如 `Float[]`）或多维 Java 数组作为单个 FloatVector。维度必须与字段定义匹配。
3. **单查询向量限制**:
   - `ORDER BY vector_col <-> ?` 只接受一个查询向量，例如 `[1, 1]`；有无 LIMIT 均遵守此规则。
   - 嵌套向量列表（例如 `[[1, 1], [99, 99]]`，包括只包装一个向量的 `[[1, 1]]`）会报参数错误。该限制也适用于 UPDATE/DELETE 的距离排序和向量范围条件。
   - Milvus SDK 的多查询向量批量搜索不通过此 ORDER BY 语法提供；它也不等同于 JDBC `addBatch`/`executeBatch`。SELECT 仍返回一个结果集，输出字段由 SELECT 指定。

### 删除表

```text
DROP TABLE [IF EXISTS] table_name;
```

### 清空集合 {#truncate}

```sql
TRUNCATE TABLE books;
TRUNCATE TABLE books IN DATABASE archive_db;
```

**此操作清空指定集合的全部数据。** 它直接调用官方 `truncateCollection`，保留集合 schema、索引和别名；不是 DELETE 的别名，也不通过 DROP/CREATE 重建集合。没有 WHERE、LIMIT、分区或通配范围，`fetchSize`、`setMaxRows` 和查询 Hint 不限制清空行数。名称使用普通 SQL 标识符，不接受 `?` 值参数；默认当前连接数据库，显式 `IN DATABASE` 不改变 `Connection.getCatalog()`。

使用 `executeUpdate()` 或 `execute()`；成功返回更新计数 0，表示 SDK 确认，并非删除了零行，也不返回受影响行数、主键或结果集。该操作不可通过 JDBC 事务回滚。超时或连接中断不代表服务端未执行，驱动不追加重试、扫描实体或改用其他删除方式；操作前应确认目标范围和数据恢复方案。

原生 API 在 Milvus 2.6.11 引入，见[官方发布说明](https://milvus.io/docs/v2.6.x/release_notes.md#v2611)与[Java API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/truncateCollection.md)。2.6.2 基线返回原生 `UNIMPLEMENTED`，已验证该失败保留 schema、索引和原记录，并阻止同批后续删除执行。驱动不提供旧版本替代实现；较新服务端的清空成功、schema/索引/别名保留及后续写入尚未完成真实验证。

### 重命名表 {#rename}

```text
ALTER TABLE old_name RENAME TO new_name [IN DATABASE target_database];
```

未指定 `IN DATABASE` 时，在当前连接的数据库内重命名；指定时将 SDK `RenameCollectionReq.targetDbName` 设为目标数据库，由 Milvus 原生完成集合移动与重命名。源数据库始终取当前 JDBC 连接，不改变 `Connection.getCatalog()`，后续语句仍在原数据库执行。

```sql
ALTER TABLE books RENAME TO archived_books IN DATABASE archive;
```

目标数据库须已存在，并具有执行所需权限。这是同一 Milvus 实例/集群内的操作，不是跨集群复制；驱动不会自动建库、复制实体、重建索引、改写调用方 SQL 或调整授权。名称冲突、数据库不存在或服务端限制直接返回 SQLException，不进行替代操作。成功返回更新计数 0，而不是搬移的行数。

数据库和集合名使用 SQL 标识符，不支持 `?` 值参数。操作后请通过目标数据库的 JDBC URL 访问新名称；旧名称不自动成为别名。原生重命名接口见[官方 API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/renameCollection.md)。

Milvus 2.6.2 已验证未加载集合的本库重命名、跨数据库往返移动、移动后向量检索与修改数据，连接数据库保持不变；测试在移回前显式释放集合。权限继承、别名关联和其他加载状态组合需按目标服务端规则确认。

### 在线字段与属性变更

```sql
ALTER TABLE books ADD COLUMN priority INT64 NULL DEFAULT 7;
ALTER TABLE books ALTER COLUMN title SET PROPERTIES (max_length=1024);
ALTER TABLE books SET PROPERTIES ('collection.ttl.seconds'=3600);
ALTER TABLE books DROP PROPERTIES ('collection.ttl.seconds');
ALTER INDEX title_idx ON books SET PROPERTIES ('mmap.enabled'=true);
ALTER INDEX title_idx ON books DROP PROPERTIES ('mmap.enabled');
```

字段属性也可通过 `ALTER TABLE books ALTER COLUMN title DROP PROPERTIES ('key')` 移除。SET 接受标量参数值；NULL 不等同于删除属性，移除时使用 DROP PROPERTIES。

这些命令直接调用 SDK 的在线变更 API，不重建集合或复制数据。ADD COLUMN 复用建表字段定义，新增字段必须显式声明 NULL，不能新增主键或 AUTO_ID 字段。可添加的字段类型、可修改/移除的属性、加载状态要求取决于服务端版本；SDK 包含 API 不代表所有 2.6.x 补丁版本都支持。服务器拒绝时返回 SQLException，不提供模拟回退。DDL 成功返回更新计数 0，不表示受影响实体数。

### 查看表

```sql
SHOW TABLES;                        -- 列出所有表
SHOW TABLE table_name;              -- 查看字段信息
SHOW CREATE TABLE table_name;       -- 查看建表详细语句
```

### 分区管理 {#partition}

集合与分区的原生统计信息使用：

```sql
SHOW STATS FROM table_name;
SHOW STATS FROM table_name PARTITION partition_name;
```

返回一行 `NUM_ENTITIES`（BIGINT）和 `STATS`（VARCHAR，原始统计 Map 的 JSON 对象文本），分别对应 SDK 的实体数和统计明细。此命令读取服务端统计，不接受 WHERE，不隐式 FLUSH，也不执行客户端扫描。需要过滤后的逻辑行数时使用 COUNT；不要把统计快照当作强一致、事务性的计数结果。对应官方 API：[集合统计](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/getCollectionStats.md)、[分区统计](https://milvus.io/api-reference/java/v2.6.x/v2/Partitions/getPartitionStats.md)。

```text
CREATE PARTITION [IF NOT EXISTS] partition_name ON TABLE table_name;
DROP PARTITION [IF EXISTS] partition_name ON TABLE table_name;
SHOW PARTITIONS FROM table_name;    -- 列出某表的所有分区
SHOW PARTITION p_name ON TABLE t_name;
```

### 别名管理 {#alias}

```text
CREATE ALIAS alias_name FOR TABLE table_name;
ALTER ALIAS alias_name FOR TABLE table_name;
DROP ALIAS [IF EXISTS] alias_name;
SHOW ALIASES FROM [TABLE] table_name;
SHOW ALIAS alias_name;
```

SHOW ALIASES 列出指定集合的别名，每行返回 `ALIAS`、`TABLE`（VARCHAR）；无别名时返回保留列元数据的空结果集。SHOW ALIAS 描述一个别名，返回一行 `DATABASE`、`ALIAS`、`TABLE`（VARCHAR），不存在或无权限时返回 SQLException。名称不是值参数，不能用 `?` 替代。列表顺序由服务端决定，`Statement.setMaxRows()` 可限制返回行数。

别名改指向由 Milvus 执行，不复制实体。改指向后新执行的查询访问新集合；已经打开的 ResultSet 不因此重新执行，也不提供跨查询事务保证。

---

## 3. 索引管理 (Index Management) {#index}

### 创建索引

```sql
CREATE INDEX index_name ON TABLE table_name (vector_col) USING 'IVF_FLAT' WITH (nlist = 1024, metric_type = 'L2');
```

#### 索引选项与支持边界

USING 按 SDK 2.6.22 的 IndexType 枚举解析，省略时沿用 SDK 的 AUTOINDEX。常用 FloatVector 类型包括 FLAT、IVF_FLAT、IVF_SQ8、IVF_PQ、HNSW、SCANN、DISKANN、AUTOINDEX；常用标量类型包括 STL_SORT、TRIE、INVERTED、BITMAP。旧说明中的 RNSG、ANNOY 不在当前 SDK 枚举中。

服务端版本、字段类型和运行环境仍决定索引是否可用。Binary 索引包括 BIN_FLAT/BIN_IVF_FLAT，Sparse 包括 SPARSE_INVERTED_INDEX/SPARSE_WAND；SDK 枚举不是所有部署都支持的保证。

索引 WITH 的 metric_type（或 metric）映射距离类型，其余值作为 extraParams；支持字符串、整数、小数、布尔、标识符和标量 ?，不支持直接书写嵌套 JSON 对象。特殊格式按 SDK 参数约定传递字符串。索引 metric 必须与查询距离算子一致。

CREATE INDEX 当前同步等待，SDK 等待上限为 600000ms；这里不消费 IMPORT/LOAD/RELEASE 的 sync/timeout Hint。

### 删除索引

```sql
DROP INDEX index_name ON TABLE table_name;
```

### 查看索引 {#index-metadata}

```sql
SHOW INDEXES FROM table_name;
SHOW INDEX index_name ON TABLE table_name;
SHOW PROGRESS OF INDEX ON TABLE table_name;
SHOW PROGRESS OF INDEX index_name ON TABLE table_name;
```

索引详情返回 `INDEX`、`FIELD`、`ID`、`PARAMS` 四列；集合没有索引时返回保留列元数据的空结果集。查询全部索引时，驱动先调用 SDK `listIndexes`，再按名称调用 `describeIndex`；指定索引名时直接查询该索引。它们不是原子快照，若查询期间索引被删除或原生请求失败，会传播 SQLException，不返回已收集的部分列表当作成功。

进度返回一行 BIGINT `TOTAL` 与 `INDEXED`，分别为 SDK 的总行数和已索引行数。省略索引名时按各索引累加；同一批实体有两个索引时可能被计数两次，因此不是集合实体数或 COUNT 的替代。没有索引时两者为 0，原生计数的刷新时机由 Milvus 决定。2.6.2 已验证空列表、多索引增删、具名查询及两个 FLAT 索引的进度汇总。

---

## 4. 用户与权限管理 (User & Role Management) {#user}

### 用户管理

```text
CREATE USER [IF NOT EXISTS] username PASSWORD 'password' [WITH (description='...')];
ALTER USER username PASSWORD 'new-password' REPLACE 'old-password';
DROP USER [IF EXISTS] username;
SHOW USERS;
SHOW USER username;
```

CREATE USER 的密码以及 ALTER USER 的新旧密码均可写 `?`，用 `PreparedStatement.setString()` 绑定；用户名仍是 SQL 标识符，不能作为值参数绑定。ALTER USER 按 SQL 顺序绑定新密码、旧密码，直接调用 SDK `updatePassword`，成功返回更新计数 0。服务端决定密码约束、权限和旧密码校验，驱动不实现本地认证，也不在修改成功后自动重放修改请求。

```java
try (PreparedStatement stmt = conn.prepareStatement(
        "ALTER USER app_user PASSWORD ? REPLACE ?")) {
    stmt.setString(1, newPassword);
    stmt.setString(2, oldPassword);
    stmt.executeUpdate();
}
```

ALTER USER 可追加 `WITH (reset_connection=false, description='...')`，值均支持参数绑定。`reset_connection` 为布尔值，默认保持 SDK 的 false；true 会让 SDK 以被修改用户和新密码重建当前客户端连接，清除原 token，可能切换当前连接身份，不能把它当作无副作用的刷新开关。`description` 为字符串，直接传给 SDK，支持情况由服务端版本决定；省略时保持 SDK 默认值。密码修改和随后重连不是原子操作：重连失败不代表密码未修改。连接池中其他连接不会被自动更新。

SHOW USER 返回一行 USER、ROLES、DESCRIPTION，均为 VARCHAR；ROLES 为 JSON 角色名称数组，无角色时是 `[]`。该命令读取 SDK 用户信息，不返回密码，也不将角色展开为有效权限。SDK/服务端未提供的说明信息不会由驱动补造。

### 用户和角色说明 {#principal-descriptions}

创建用户或角色时也可指定 `WITH (description=?)`，分别传给 SDK `CreateUserReq.description`、`CreateRoleReq.description`。说明必须是非 NULL 字符串，可以为空；省略时保持 SDK 的空字符串默认值。创建用户按密码、说明的 SQL 顺序绑定参数。`IF NOT EXISTS` 命中已有对象时不修改密码或说明；参数仍会被读取和校验，不影响同次执行的后续语句。

```sql
CREATE USER app_user PASSWORD ? WITH (description=?);
CREATE ROLE reader WITH (description=?);
```

**Milvus 2.6.2 接受这些创建请求，但 SHOW USER / SHOW ROLE 读到的说明为空**，直接使用官方 SDK 也如此。创建成功不等于说明已持久化；驱动不会缓存输入来填充 DESCRIPTION。需要说明存储时，请确认部署版本的原生支持。

```sql
ALTER USER app_user WITH (description='应用查询账号');
ALTER ROLE reader WITH (description='只读访问角色');
ALTER USER app_user WITH (description=?);
```

这两个独立命令分别调用 SDK `updateUser`、`alterRole`，不调用密码修改、角色重建或授权接口。WITH 只接受 `description`，其值必须是非 NULL 字符串；空字符串表示清空说明。可以用 `PreparedStatement.setString()` 绑定说明内容，名称仍为 SQL 标识符。SDK 调用成功时返回更新计数 0，不表示修改了 0 个用户或角色。

**Milvus 2.6.2 不支持这两个说明更新操作**：用户说明更新会被旧服务端按密码修改校验并返回密码长度错误，角色说明更新返回 `UNIMPLEMENTED`。驱动原样传播失败，不通过重设密码或重建角色模拟支持。仅在部署的服务端确实支持相应原生 API 时使用；SDK 2.6.22 提供方法并不代表所有 2.6.x 服务端都支持。其他服务端版本的说明更新及读取尚未完成真实验证。

### 角色管理

```text
CREATE ROLE [IF NOT EXISTS] role_name [WITH (description='...')];
DROP ROLE [IF EXISTS] role_name [WITH (force_drop=false)];
SHOW ROLES;
SHOW ROLE role_name;
```

`force_drop` 是非 NULL 布尔值，可用 `setBoolean()` 绑定；省略时保持 SDK 的 false。`true` 直接设置 `DropRoleReq.forceDrop`，由服务端删除角色及其授权、成员关系。该操作可能撤销用户权限，请仅在确实需要强制删除时启用。Milvus 2.6.2 已验证：存在授权与成员时 false 失败，true 删除角色及其关联关系但保留用户。驱动不逐条撤销权限兜底，不提供事务或失败重放。`IF EXISTS` 只处理角色不存在，不吞掉参数错误、权限错误或 SDK 失败；成功或不存在时返回更新计数 0。

SHOW ROLE 调用 SDK `describeRole`，返回一行 ROLE、DESCRIPTION、GRANTS，均为 VARCHAR。GRANTS 为当前连接数据库范围内的直接授权 JSON 数组；没有授权时是 `[]`，仍保留角色详情行。数组保留 SDK 返回的 `objectType`、`objectName`、`privilege`、`grantor`、`dbName` 等字段，SDK 未返回的字段可能省略。说明信息按 SDK 返回值提供；空字符串或 NULL 不表示支持修改说明。该查询不展开角色成员或计算用户的全部有效权限。SDK 内部先读取授权再读取角色，两次读取不是事务快照。

### 授权与撤销

```sql
GRANT ROLE role_name TO username;
REVOKE ROLE role_name FROM username;

GRANT Search ON Collection table_name TO ROLE role_name;
REVOKE Search ON Collection table_name FROM ROLE role_name;
GRANT Query ON Collection * TO ROLE role_name;
SHOW GRANTS FOR ROLE role_name;
SHOW GRANTS FOR ROLE role_name ON GLOBAL;
SHOW GRANTS FOR ROLE role_name ON TABLE table_name;
SHOW GRANTS FOR ROLE role_name ON USER username;
```

权限名和 GRANT/REVOKE 的对象类型按 SDK 原样传递，示例的对象类型是 Collection，不是 TABLE；应使用服务端认可的大小写和权限名。SHOW GRANTS 则使用本适配器的 ON TABLE/USER/GLOBAL 过滤语法。

不带 ON 返回 SDK 查询范围内的授权记录，不表示跨所有数据库汇总；ON TABLE / USER 按对象类型和名称精确过滤，ON GLOBAL 只返回 Global 类型的记录。这里查询授权记录，不展开全局或通配授权来计算某个对象的有效权限。ROLE 列从 SDK 角色详情取得，不依赖单条授权记录是否带有角色名。

执行授权操作时应提供有权执行该操作的连接身份。Milvus 2.6.2 即使关闭认证检查，GRANT 仍需从连接认证信息取得授权者；匿名连接可能返回授权信息缺失错误。驱动不会自动补入管理员凭据。

### 显式数据库范围的授权 {#scoped-privileges}

```sql
GRANT PRIVILEGE Search ON DATABASE app_db TABLE books TO ROLE reader;
REVOKE PRIVILEGE Search ON DATABASE app_db TABLE books FROM ROLE reader;
-- 明确授权 app_db 下的所有集合；权限组由服务端解释
GRANT PRIVILEGE CollectionReadOnly ON DATABASE app_db TABLE * TO ROLE reader;
REVOKE PRIVILEGE CollectionReadOnly ON DATABASE app_db TABLE * FROM ROLE reader;
```

带 `PRIVILEGE ... ON DATABASE ... TABLE ...` 的形式分别调用 SDK `grantPrivilegeV2`、`revokePrivilegeV2`，与前面的旧版 `GRANT Search ON Collection ...` 命令区分。数据库、集合范围都必须提供，不使用连接当前数据库作为隐式替代，也不自动补 `*`；数据库和集合位置均可显式写 `*`，实际权限范围及权限组名称由服务端校验。名称与权限均为 SQL 标识符，不是可用 `?` 绑定的值。

执行后连接的当前数据库不变，成功返回更新计数 0（SDK 确认，不是授权数量）。用 `SHOW ROLE reader ON DATABASE app_db` 检查指定数据库中的角色授权，也可显式用 `ON DATABASE *` 查询 SDK 的通配数据库范围；省略 ON DATABASE 时仍使用连接数据库。无需也不支持通过 JDBC `setCatalog()` 切换当前连接的数据库。结果是授权记录，不是鉴权效果证明。服务端不支持 V2 或拒绝操作时抛出 SQLException，不回退到缺少显式数据库范围的旧接口。

---

### 权限组定义 {#privilege-groups}

```sql
CREATE PRIVILEGE GROUP app_query;
ALTER PRIVILEGE GROUP app_query ADD (Search, Query);
SHOW PRIVILEGE GROUPS;
ALTER PRIVILEGE GROUP app_query DROP (Query);
DROP PRIVILEGE GROUP app_query;
```

分别调用 SDK 的 `createPrivilegeGroup`、`addPrivilegesToGroup`、`listPrivilegeGroups`、`removePrivilegesFromGroup`、`dropPrivilegeGroup`。`ALTER ... DROP (...)` 只移除列出的组内权限，`DROP PRIVILEGE GROUP` 才删除整个组。ADD/DROP 的列表至少包含一个 SQL 标识符；组名和权限名均不是 `?` 值参数。列表以一个 SDK 请求提交，驱动不拆成逐个权限的请求，也不据此承诺跨命令事务。

SHOW 返回 PRIVILEGE_GROUP、PRIVILEGES 两个 VARCHAR 列，每组一行；PRIVILEGES 是权限名称的 JSON 数组，空组为 `[]`。没有组时保留列元数据而不返回数据行。组和组内权限的顺序按 SDK 返回，不提供排序保证；JDBC `setMaxRows` 只限制返回的组行数，不截断组内权限数组。

这些命令管理权限组定义，不创建角色、不自动授权，也不在删除组时自动撤销角色授权。要赋予角色某个权限组，请使用前面的 `GRANT PRIVILEGE ... ON DATABASE ... TABLE ...` 语法。已授权权限组的修改可能影响使用该组的角色；内置组保护、权限名称合法性及正在使用的组能否删除由服务端决定，失败作为 SQLException 返回。修改命令成功返回更新计数 0，表示 SDK 已确认，不是修改的权限数量。

### 验证分词结果 {#analyze}

```sql
ANALYZE 'hello world' WITH (analyzer_params='{"tokenizer":"standard"}');
ANALYZE ['hello world', '', 'milvus'] WITH (with_detail=true, with_hash=true);
ANALYZE ? WITH (analyzer_params=?, with_detail=true);
-- 使用集合字段上下文；支持情况取决于服务端版本和字段配置
ANALYZE ? ON TABLE books(body) WITH (analyzer_names=?);
```

ANALYZE 调用官方 SDK `runAnalyzer`，不扫描集合，也不在 Java 中分词。输入可为一个字符串或非空字符串列表；`?` 可用 `setString` 绑定单个文本，或用 `setObject` 绑定字符串列表/`String[]`。列表元素不能是 NULL，但允许空字符串。输入与 WITH 选项按 SQL 顺序绑定，文本内容不会拼入表达式。

WITH 接受以下选项，不接受未知名称：

| 选项 | 值与行为 |
| --- | --- |
| `analyzer_params` | Map、JsonObject 或 JSON 对象字符串；原样作为分词器配置传入 SDK。 |
| `with_detail` | 布尔值，默认保留 SDK 的 false。 |
| `with_hash` | 布尔值，默认保留 SDK 的 false。 |
| `analyzer_names` | 非 NULL 字符串列表，默认保留 SDK 空列表；选择语义及与字段配置的组合由服务端校验。 |

所有选项值均支持 `?`。可选 `ON TABLE collection(field)` 设置 SDK 集合和字段上下文，数据库取自当前连接；省略时保留 SDK 的空集合/字段默认值。不创建、修改或加载集合。分词器名称、语言、字段上下文和选项组合是否可用，由对应服务端决定。

返回一个 JDBC ResultSet：每个 SDK 文本结果一行，TEXT_INDEX 为从 1 开始的 BIGINT 序号，TOKENS 为 VARCHAR JSON 数组。每个 token 保留 SDK 的 `token`、`startOffset`、`endOffset`、`position`、`positionLength`、`hash`；缺失的 SDK 字段可能省略，未启用的详情字段也可能返回默认 0，不应当作有效测量值。偏移量使用服务端语义，不转换为 Java 字符索引；SDK 的 32 位无符号 hash 使用 long 表示。无 token 的文本仍有一行 `[]`。`setMaxRows` 限制文本结果行数，不截断一段文本的 token 数组，也不限制服务端分析的输入量。

Milvus 2.6.2 已验证独立 standard 分词器、多文本及空文本、详情/hash、参数绑定和语句复用。集合字段上下文及命名分词器选项已映射并有单元测试，尚未完成真实验证；不能据 SDK 存在这些字段认定所有 2.6.x 服务端都支持。

## 5. 数据操作 (DML) {#dml}

INSERT/UPSERT 支持多个 VALUES 元组（须显式列名），或 VALUES ? 绑定 Iterable/Iterator，每行为 Map（可省略列名）、List/Object[]（须列名）。不支持 INSERT SELECT。返回 SDK 实际确认的 long 计数；请求 RETURN_GENERATED_KEYS 时另提供标准主键游标。

### 插入数据

```sql
-- 插入到默认分区
INSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- 插入到指定分区
INSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);
```

### Upsert (插入或覆盖) {#upsert}

```sql
-- 插入或覆盖到默认分区
UPSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- 插入或覆盖到指定分区
UPSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);

-- 原生部分更新：已有主键只修改 age，新主键按服务端插入规则处理
/*+ partial_update=true */ UPSERT INTO table_name (id, age) VALUES (?, ?);
```

`partial_update` 是 UPSERT 的布尔 hint，可用 `?` 绑定，映射 SDK `UpsertReq.partialUpdate`。省略或 false 保留完整覆盖模式；true 使用 Milvus 2.6.2+ 的原生部分更新，保留已有实体中未提供的字段。新主键仍必须满足 schema 的必填字段、默认值和函数输入要求；不会由驱动先查询完整实体。该 hint 不适用于 INSERT，非布尔值会报错。跨多页不保证原子性或回滚，失败不会自动重放不确定的写入。

INSERT/UPSERT 和 UPDATE SET 都按 schema 转换 JSON、向量及 Array。JSON 字符串、Map、List、JsonElement 或数值数组按 JSON 编码，不执行 FloatVector 降精度转换；这不是任意输入无损往返的保证，具体格式仍受 SDK/服务端校验约束。

### 更新数据 (UPDATE)

> **注意**：UPDATE 使用 Milvus 2.6.2+ 的原生 Partial Update。驱动分页查询主键，每页只发送主键及 SET 字段，不回写未修改字段，不会将整个选择集合一次性积攒到内存。跨页不提供事务或整体回滚；同一 SET 字段的并发修改仍由 Milvus 的写入语义决定。

#### 1. 基础更新 (标量过滤)

使用本手册的标量 WHERE 语法过滤。省略 WHERE 且无向量条件时会更新整个集合；按需求明确条件或 LIMIT。不能修改主键。SET 是值赋值，支持常量/参数，不支持 `age = age + 1` 这种逐行计算。

```sql
-- 按主键更新
UPDATE table_name SET age = 20 WHERE id = 1;

-- 按标量条件选取主键，再提交 Partial Update
UPDATE table_name SET status = 'active' WHERE age > 18;
```

#### 2. 最近邻更新 (KNN Update)

更新距离目标向量最近的 K 条记录。
底层机制：按距离迭代选取主键 -> 逐页 Partial Upsert。

```sql
-- 将距离 [0.1, 0.2] 最近的 1 条记录的 status 更新为 1
UPDATE table_name SET status = 1 ORDER BY vector_col <-> [0.1, 0.2] LIMIT 1;
```

#### 3. 范围更新 (Range Update)

更新所有落在目标向量指定距离（半径）内的记录。
可以使用 `vector_range` 函数或 `<->` 比较表达式。

```sql
-- 使用 vector_range 函数 (推荐)
-- 语法: vector_range(vector_field, target_vector, radius)
UPDATE table_name SET tag = 'A' WHERE vector_range(vector_col, [0.1, 0.2], 0.5);

-- 使用比较表达式
UPDATE table_name SET tag = 'A' WHERE vector_col <-> [0.1, 0.2] < 0.5;
```

### 删除数据 (DELETE)

> **注意**：无 WHERE 的 DELETE 表示删除整个集合中的记录；指定 PARTITION 时仅作用于该分区。请先确认操作范围。无 LIMIT 的标量 DELETE 直接提交服务端过滤表达式；有 LIMIT 时先分页选取主键再删除。向量 DELETE 先分页搜索主键再删除，不提供整条 SQL 的原子性或回滚。

无 WHERE 的标量 DELETE 会在驱动内部使用集合的实际主键字段构造 `主键 IS NOT NULL` 过滤条件。Milvus 主键不允许为 NULL，因此该条件覆盖全部记录，包括负数、零和字符串主键，不要求字段名为 `id`。该转换仅用于省略 WHERE 的情况，不改写用户已有的条件；也不会使用 `1=1` 兜底，Milvus 2.6.2 的原生执行路径不支持这种恒真过滤。LIMIT、分区和 JDBC fetchSize 的既有语义保持不变；fetchSize 是取数页大小，不是删除总量上限。

#### 1. 基础删除 (标量过滤)

使用本手册的标量 WHERE 语法过滤删除。

```sql
-- 删除全部记录，保留集合及其 schema、索引
DELETE FROM table_name;

-- 最多删除 1000 条；不指定排序时不保证具体选中哪些记录
DELETE FROM table_name LIMIT 1000;

-- 仅删除指定分区的全部记录
DELETE FROM table_name PARTITION partition_name;

-- 按主键删除
DELETE FROM table_name WHERE id = 1;
DELETE FROM table_name WHERE id IN [1, 2, 3];

-- 按标量条件删除 (无 LIMIT 时直接调用服务端过滤删除)
DELETE FROM table_name WHERE age > 18 AND status = 'inactive';

-- 指定分区删除
DELETE FROM table_name PARTITION partition_name WHERE age > 10;
```

#### 2. 最近邻删除 (KNN Delete)

使用 `ORDER BY` 指定距离排序；有 LIMIT 时最多选择 K 条，无 LIMIT 时迭代整个符合条件的搜索结果。
底层机制：按距离分页选取主键 -> 逐页删除。

```sql
-- 删除距离 [0.1, 0.2] 最近的 100 条记录
DELETE FROM table_name ORDER BY vector_col <-> [0.1, 0.2] LIMIT 100;

-- 结合标量过滤：删除 category='book' 且最相似的 10 条
DELETE FROM table_name WHERE category = 'book' ORDER BY vector_col <-> [0.1, 0.2] LIMIT 10;
```

#### 3. 范围删除 (Range Delete)

删除所有落在目标向量指定距离（半径）内的记录。可以使用 `vector_range` 函数或 `<->` 比较表达式。
底层机制：范围搜索迭代选取主键 -> 逐页删除。

```sql
-- 使用 vector_range 函数 (推荐)
DELETE FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.5);

-- 使用比较表达式：删除所有距离 [0.1, 0.2] 小于 0.5 的记录
DELETE FROM table_name WHERE vector_col <-> [0.1, 0.2] < 0.5;

-- 结合 LIMIT 进行保护 (最多删除符合条件的 1000 条)
DELETE FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.5) LIMIT 1000;
```

### 分页写入、取消和重试

- UPDATE/DELETE 的 LIMIT 来自 SQL 本身，SELECT 的 overwrite_find_limit/skip Hint 和 JDBC maxRows 不限制 DML 写入量。向量 UPDATE/DELETE 的显式 LIMIT 当前至多 Integer.MAX_VALUE，标量 LIMIT 使用 long；无 LIMIT 仍表示无主动总量截断。
- UPDATE、带 LIMIT 的标量 DELETE、KNN/范围 DELETE 使用迭代器分页。无 LIMIT 不设置任意固定总条数；无 LIMIT 的标量 DELETE 直接使用服务端过滤删除。
- `Statement.setFetchSize(n)` 控制页大小，不能代替 LIMIT；未设置时使用 SDK 页大小，超出 SDK 单页上限时按 SDK 上限取页。
- `Statement.cancel()` 或查询超时生效后，不再主动读取下一页或发起后续写入/重试，并关闭迭代器。在途 RPC 和已成功页面不承诺撤销。
- JDBC 连接参数 `maxRetry` 是初次写入失败后的最大重试次数；目前共享于分页 Partial Upsert 和 DELETE 写调用。重试耗尽时可能已经部分成功，不保证精确一次执行。不是所有 SDK 操作都自动重试。
- 仅重试已识别的临时错误，例如 gRPC UNAVAILABLE、RESOURCE_EXHAUSTED、ABORTED、DEADLINE_EXCEEDED、SDK 限流/服务暂不可用及 JDBC 临时连接异常；参数、权限、集合不存在和未知错误直接失败。退避从 100ms 开始翻倍，单次等待最多 1000ms；等待期间持续检查取消和查询超时。
- 读取下一页不自动重试，避免在游标位置不确定时跳过或重复数据。普通 INSERT/UPSERT、Import 创建也不自动套用该重试策略。
- 分页 DML 异常携带 `phase=read/write/close`、`iterator`、`page`、`confirmedPages`、`confirmedRows`、`currentPageRows`，并保留 SQLState、错误码和 cause。已有主异常时，迭代器关闭异常放入 suppressed，不覆盖主异常。已确认进度不是服务端最终状态的完整证明：失败中的页面仍可能有部分写入已生效。
- 更新计数（包括跨页累加）保留为 `long`，通过 `executeLargeUpdate()` / `getLargeUpdateCount()` 获取。超过 `Integer.MAX_VALUE` 时，普通 `executeUpdate()` / `getUpdateCount()` 沿用公共驱动策略返回 `Statement.SUCCESS_NO_INFO`，不将计数截断或使已完成的分页操作报整数溢出错误。

### 数据导入 (Import)

```sql
-- 文件必须预先放入 Milvus 可访问的对象存储
IMPORT FROM 'prepared/1.parquet' INTO TABLE table_name;
IMPORT FROM 'prepared/1.json' INTO TABLE table_name PARTITION partition_name;

-- 等待导入任务完成，最多等待 60 秒
/*+ timeout=60000 */ IMPORT FROM 'prepared/1.json' INTO TABLE table_name;

-- 仅提交导入任务，不等待完成
/*+ sync=false */ IMPORT FROM 'prepared/1.json' INTO TABLE table_name;
```

路径是 Milvus 对象存储中的已准备文件，不是 Java 本地文件；驱动不读取或上传文件。支持单路径和多文件分组 [[...],[...]]，也可用 ? 绑定 List&lt;List&lt;String>>。JSON/Parquet 每组一个文件，NumPy 每组相关列文件；格式和资源限制见[官方 Import 说明](https://milvus.io/docs/v2.6.x/import-data.md)。

导入使用官方 REST API，复用 JDBC 地址/database/认证，地址必须开放 REST。同一连接的任务创建、列表和进度查询使用同一个 JDBC 地址。普通 IMPORT 返回更新计数 0（非导入行数）；RETURNING JOB_ID 显式返回字符串任务 ID，SHOW IMPORT/SHOW IMPORTS 提供后续查询。

`IMPORT FROM` 默认同步等待 Milvus Import 任务完成；如需异步返回，可使用 `sync=false` Hint。`timeout` Hint 用于设置同步等待超时时间，单位为毫秒。

### 加载与释放 (Load / Release) {#load}

Milvus 要求在搜索前将 Collection 加载到内存。
```text
LOAD TABLE table_name [PARTITION partition_name] [WITH (option=value, ...)];
RELEASE TABLE table_name [PARTITION partition_name];

-- 设置加载等待超时
/*+ timeout=60000 */ LOAD TABLE table_name;

-- 仅提交释放请求，不等待释放完成
/*+ sync=false */ RELEASE TABLE table_name;
```

`LOAD TABLE` 默认通过 SDK 同步等待加载完成；刷新加载等待的是 SDK 的刷新进度，不能用既有 Loaded 状态代替。`RELEASE TABLE` 默认等待进入 NotLoad 状态；可使用 `sync=false` 只提交请求。LOAD 的 `timeout` 提示以毫秒计，默认 60000，必须大于 0，传入 SDK 的加载请求与同步等待；异步提交不表示后台任务已经成功。命令返回更新计数 0，不是加载行数。

| LOAD WITH 参数 | SDK 请求字段 | 类型与默认值 |
| --- | --- | --- |
| num_replicas | numReplicas | 正 INT32 整数，默认 1；可用数量取决于集群资源。 |
| refresh | refresh | boolean，默认 false；刷新已加载集合或分区。 |
| load_fields | loadFields | 字符串列表，默认空列表，沿用 SDK 默认加载字段范围。 |
| skip_load_dynamic_field | skipLoadDynamicField | boolean，默认 false；true 时动态字段不能用于过滤或输出。 |
| resource_groups | resourceGroups | 字符串列表，默认空列表，使用服务端默认资源组安排。 |

以上参数同时适用于集合和分区；列表可使用 SQL 列表、JDBC `setObject` 绑定 `List<String>` / `String[]`，或绑定 JSON 字符串数组。列表元素不能为空或空白；未知、重复参数和错误类型在调用加载 API 前报错。`sync` / `timeout` 仍使用 Hint，不放入 WITH。

```sql
LOAD TABLE books WITH (num_replicas=1, load_fields=['id','book_intro'], skip_load_dynamic_field=true);
/*+ timeout=30000 */ LOAD TABLE books WITH (refresh=true);
/*+ sync=false */ LOAD TABLE books PARTITION p WITH (resource_groups=['query_group']);
```

按官方说明，部分字段加载应包含主键和至少一个向量字段，查询过滤和输出应只引用加载字段；变更加载字段范围需显式 RELEASE 后重新 LOAD。官方将此功能列为 beta，生产采用前应确认对应版本条件，见[官方加载说明](https://milvus.io/docs/load-and-release.md)。驱动不会自动释放集合，也不把加载字段列表变成访问控制。在当前 2.6.2 环境中，原生 SDK 与 JDBC 都仍能读取列表外的标量字段，因此不保证此版本拒绝访问未指定字段，也未验证内存节省效果。`SHOW PROGRESS OF LOADING` 不提供刷新任务的独立进度。

Milvus 2.6.2 环境已验证指定字段、显式释放后完整加载、集合/分区同步刷新及单副本读取。多副本、指定非默认资源组调度和跳过动态字段后的业务效果不在这些真实验证结论内；它们的 SDK 请求映射由单元测试覆盖。

---

### Flush {#flush}

```sql
FLUSH table_name;
FLUSH first_table, second_table IN DATABASE archive WITH (wait_flushed_timeout_ms=60000);
FLUSH ALL TABLES;
FLUSH ALL TABLES IN DATABASE archive WITH (wait_flushed_timeout_ms=?);
SHOW FLUSH ALL ? IN DATABASE archive;
```

`FLUSH table_name[, ...]` 一次提交 SDK `FlushReq.collectionNames`，等待后返回更新计数 0。`FLUSH ALL TABLES` 调用 SDK `flushAll`，**同样同步等待完成**，成功返回一行 `FLUSH_ALL_TS`（BIGINT）。使用 `executeQuery()` 或 `execute()` 取得该结果，不是 `getGeneratedKeys()`，也不是提交即返回的异步任务。`FLUSH all` 仍表示刷新名为 `all` 的单个集合，不是全库操作。

两种命令默认当前连接数据库，可用 `IN DATABASE db_name` 显式指定其他库，不改变连接的 catalog。只有 `FLUSH ALL TABLES IN DATABASE *` 才请求所有数据库；状态查询使用相同作用域，例如 `SHOW FLUSH ALL ? IN DATABASE *`。集合名和库名必须是非空 SQL 标识符，不能绑定 `?`；按集合刷新不接受数据库通配符。

`WITH` 只接受 `wait_flushed_timeout_ms`，可绑定非负 BIGINT 范围内的整数。默认沿用原有 60000ms 等待上限；显式 0 表示 SDK 无限等待，请谨慎使用。该参数控制 SDK 的完成等待，不是整个 JDBC 调用的总耗时上限；提交 RPC、网络和 SDK 重试还受连接配置影响。不消费 sync/timeout Hint，不支持用 `sync=false` 改为异步。

`SHOW FLUSH ALL timestamp` 单次调用 `getFlushAllState`，返回一行 `FLUSH_ALL_TS`（BIGINT）和 `FLUSHED`（BOOLEAN）。时间戳接受非负 BIGINT 或 `setLong()` 参数，应保留 SDK 返回的原始整数，不能转换成 Java 日期或浮点数；状态查询不触发落盘、不自动等待。false 表示尚未满足原生完成条件，SDK 未提供的状态保留 NULL，不补造完成结果。

**2.6.2 的按库 FlushAll 完成等待存在服务端限制**：其 `GetFlushAllState` 虽然筛选了目标数据库，但检查 checkpoint 时仍遍历全部数据库，可能被其他库拖住并最终超时，见[官方 2.6.2 实现](https://github.com/milvus-io/milvus/blob/v2.6.2/internal/datacoord/services.go#L1519)。驱动和官方 SDK 的等待都受此影响。该基线可使用显式集合列表的 FLUSH；不将全库完成等待标为已验收，也不保证全实例通配作用域可用。其他版本需确认原生修复情况。

FLUSH 不是 JDBC commit，也不是跨语句事务边界。失败、超时或取消不表示服务端未落盘；SDK 等待失败时可能没有可返回的时间戳。驱动不通过逐集合补刷、额外轮询或伪造成功兜底；也不在 SDK 之外自动重提。频繁刷新会受服务端限流影响。

### 副本与分片状态 {#replicas}

```sql
SHOW REPLICAS FROM TABLE books;
SHOW REPLICAS FROM books;
```

通过 `executeQuery()` 返回当前连接 database 中指定集合的副本快照，每个副本一行；`TABLE` 可省略。集合名是可信 SQL 标识符，不能用 `?` 绑定。查询调用 SDK `describeReplicas`，不会隐式 LOAD、RELEASE、迁移副本或扫描实体。

| 返回列（按顺序） | JDBC 类型 | 含义 |
| --- | --- | --- |
| REPLICA_ID | BIGINT | 副本 ID |
| COLLECTION_ID | BIGINT | 集合 ID |
| PARTITION_IDS | VARCHAR | 原生分区 ID 列表的 JSON 数组文本 |
| SHARD_REPLICAS | VARCHAR | 分片信息的 JSON 数组文本；对象包含 leaderID、leaderAddress、channelName、nodeIDs |
| NODE_IDS | VARCHAR | 副本查询节点 ID 列表的 JSON 数组文本，包括负责人节点 |
| RESOURCE_GROUP | VARCHAR | 所属资源组名称 |
| NUM_OUTBOUND_NODE | VARCHAR | 原生 numOutboundNode 映射的 JSON 对象文本，键为资源组名、值为节点数 |

嵌套分片不会展开成额外行或结果集。整数 ID 以 Long/JSON 整数保留；解析 JSON 的应用应避免将其转为可能丢失精度的浮点数。SDK 返回的空容器保留为 `[]` / `{}`，NULL 容器保留为 SQL NULL。空副本列表返回带列元数据的空结果集；集合不存在、未加载或权限不足时，保留服务端实际响应，错误不转换为空结果。

`setMaxRows()` 限制 JDBC 返回的副本行数；`fetchSize` 不会将这个 SDK 快照请求变成分页查询。节点地址仅供观察，驱动不会据此改为多地址连接或读写路由。快照不保证跨节点事务一致性，不能用于证明副本容灾或调度性能。

在当前 2.6.2 单实例环境中，即使集合已写入并 FLUSH，分片的 `nodeIDs` 和副本的 `partitionIDs` 仍可能是空列表；原生 SDK 与 JDBC 返回一致。驱动不会用副本节点列表或其他查询结果补造这些字段，空分片节点列表也不等于副本没有查询节点。

API 对照：[describeReplicas](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/describeReplicas.md)。

### 服务与 segment 状态 {#diagnostics}

```sql
SHOW VERSION;
SHOW HEALTH;
SHOW PERSISTENT SEGMENTS FROM TABLE books;
SHOW QUERY SEGMENTS FROM TABLE books;
```

以上命令均使用 `executeQuery()` 或 `execute()` 获取 ResultSet，不返回更新计数；segment 查询中的 `TABLE` 可省略，集合名不能绑定为 `?`，数据库取当前连接的 catalog。

| 命令 | 返回列（按顺序） |
| --- | --- |
| SHOW VERSION | VERSION、BUILD_TIME、GIT_COMMIT、GO_VERSION、DEPLOY_MODE，均为 VARCHAR |
| SHOW HEALTH | IS_HEALTHY（BOOLEAN）、REASONS、QUOTA_STATES（后两列为 JSON 数组文本 / VARCHAR） |
| SHOW PERSISTENT SEGMENTS | SEGMENT_ID、COLLECTION_ID、PARTITION_ID（BIGINT）、COLLECTION_NAME（VARCHAR）、NUM_ROWS（BIGINT）、STATE、LEVEL（VARCHAR）、STORAGE_VERSION（BIGINT）、IS_SORTED（BOOLEAN） |
| SHOW QUERY SEGMENTS | SEGMENT_ID、COLLECTION_ID、PARTITION_ID、MEM_SIZE、NUM_ROWS（BIGINT）、INDEX_NAME（VARCHAR）、INDEX_ID（BIGINT）、STATE、LEVEL、NODE_IDS（VARCHAR，其中 NODE_IDS 为 JSON 数组文本）、STORAGE_VERSION（BIGINT）、IS_SORTED（BOOLEAN） |

VERSION 和 HEALTH 各返回一行；健康检查返回 `IS_HEALTHY=false` 时，仍可读取原因和限流状态。RPC、认证或权限错误则抛出 SQLException，不伪装为“不健康”或空结果。segment 每项占一行，无项时保留元数据并返回空结果集；SDK 返回的 NULL 保持为 SQL NULL，未提供的字段不由驱动推算。

这些命令直接调用 SDK，不隐式 FLUSH、LOAD、查询实体或探测另一个端口。`Statement.setMaxRows()` 可以限制返回的 segment 行数，但不会减少 SDK 已获取的状态快照。NUM_ROWS 是 segment 的物理状态信息，不能直接累加作为带删除、过滤或副本语义的业务 COUNT；MEM_SIZE 的单位为字节。不同节点的状态不构成事务一致快照。构建详情及较新的 segment 字段取决于服务端返回，旧版本可能给出空值或协议默认值，不能据此判定相应特性已启用。

Milvus 2.6.2 单实例环境已验证版本/健康结果、空集合状态和显式写入、FLUSH、LOAD 后的两类 segment 查询。未验证多节点故障、真实限流或各版本新增字段的业务语义。

API 对照：[健康检查](https://milvus.io/api-reference/java/v2.6.x/v2/Management/checkHealth.md)、[持久化 segment](https://milvus.io/api-reference/java/v2.6.x/v2/Management/getPersistentSegmentInfo.md)、[查询节点 segment](https://milvus.io/api-reference/java/v2.6.x/v2/Management/getQuerySegmentInfo.md)。

### 资源组查询 {#resource-groups}

```sql
SHOW RESOURCE GROUPS;
SHOW RESOURCE GROUP __default_resource_group;
```

这两个命令读取原生集群级资源组信息，不按当前 JDBC database 过滤，也不修改资源配置或迁移节点。列表每行返回一个 VARCHAR `RESOURCE_GROUP`，不承诺排序，可用 JDBC maxRows 限制返回行数。

详情返回一行：`RESOURCE_GROUP` 为名称；`CAPACITY`、`AVAILABLE_NODES` 为 INTEGER；`LOADED_REPLICAS`、`OUTGOING_NODES`、`INCOMING_NODES` 为 VARCHAR JSON 对象，分别保留 SDK 返回的集合副本数量、迁出节点数量和迁入节点数量映射；`CONFIG` 为 VARCHAR JSON 配置对象，SDK 未提供时为 SQL NULL；`NODES` 为 VARCHAR JSON 节点数组。字段含义和状态由服务端决定，查询不等待迁移完成，也不从计数推算额外调度状态。列表与详情是独立快照。

### 资源组配置 {#resource-group-config}

```sql
CREATE RESOURCE GROUP rg_demo CONFIG '{"requests":{"nodeNum":0},"limits":{"nodeNum":0}}';
ALTER RESOURCE GROUP rg_demo CONFIG ?;
ALTER RESOURCE GROUPS CONFIG ?;
DROP RESOURCE GROUP rg_demo;
```

创建时 CONFIG 可省略，保留 SDK 的默认创建行为。CONFIG 支持 JSON 对象字符串（`setString`）、Map 或 JsonObject（`setObject`）。单组 CONFIG 是配置对象；复数 GROUPS 的 CONFIG 是“资源组名 → 配置对象”的非空 Map，驱动先验证全部配置，再通过一次 SDK `updateResourceGroups` 调用提交，不拆为逐组请求。

配置采用官方 ResourceGroupConfig 的 Protobuf JSON：`requests.nodeNum` 为期望节点数量、`limits.nodeNum` 为节点数量上限；`transferFrom` 和 `transferTo` 是含 `resourceGroup` 名称的对象数组；`nodeFilter.nodeLabels` 是字符串键值对象数组，例如 `[ {"key":"zone","value":"east"} ]`。未知字段、错误类型和超范围整数由官方 JSON 解析器拒绝。SHOW 返回的 CONFIG 使用同一格式，可再次绑定提交；省略的字段采用 Protobuf 默认值，不表示保留原配置，因此修改时应提交完整目标配置。

上述写命令成功返回更新计数 0，不返回节点迁移数量。修改配置可能触发服务端调度，SQL 不等待调度完成，也不承诺多组事务。删除是否允许由服务端检查，驱动不会隐式释放集合或移走节点。示例使用零节点配置；真实验证覆盖空资源组的创建、配置往返、单组/多组更新与删除，未验证非零请求导致的节点调度或标签选择效果。

### 节点与副本迁移 {#resource-group-transfers}

```sql
TRANSFER NODES 1 FROM RESOURCE GROUP source_group TO RESOURCE GROUP target_group;
TRANSFER REPLICAS 1 OF TABLE table_name FROM RESOURCE GROUP source_group TO RESOURCE GROUP target_group;
```

这两个命令分别调用 SDK `transferNode`、`transferReplica`。数量可用 `?` 绑定，必须为正整数；节点数量不超过 Integer.MAX_VALUE，副本数量保留 Long 范围。资源组名称和集合名称是 SQL 标识符，不是值参数。节点迁移是集群级操作，副本迁移使用当前 JDBC database 中的指定集合。

命令成功返回更新计数 0；SDK 不返回迁移任务 ID 或已迁移数量，驱动不会补造。SQL 不等待调度完成，不隐式创建资源组、修改配置或加载集合。资源不足、组不存在及不允许的迁移由服务端判断。可用 SHOW RESOURCE GROUP 观察迁入、迁出和副本信息，但这些是资源组快照，不是某次调用的独立任务状态。

迁移是运维写操作，可能影响查询容量和集合可用性。错误或连接中断不能保证操作没有生效，应先核查服务端状态再决定是否重提。当前已验证 SQL 解析、SDK 字段映射、数量边界及失败传播；未在统一测试容器中移动现有节点或副本，实际迁移效果不属于已验证矩阵。

### 压缩任务 {#compaction}

```sql
COMPACT TABLE table_name;
COMPACT table_name WITH (is_clustering=false, is_l0=false, target_size=512);
SHOW COMPACTION 123456789;
SHOW COMPACTION PLANS 123456789;
```

`COMPACT` 提交原生异步压缩任务，返回一个 ResultSet，其中一行 `COMPACTION_ID` 为 BIGINT；用 `executeQuery()` 或 `execute()` 获取，不是更新计数，也不是 `getGeneratedKeys()`。驱动不轮询完成、不隐式 FLUSH、不把压缩当作 commit。提交失败时没有已知任务 ID 不代表服务端必然未收到请求，重提之前应确认任务状态。

WITH 可选项与 SDK 对应：`is_clustering`、`is_l0` 为布尔值，`target_size` 为正 long 整数，单位 MB；未指定时保留 SDK 默认值。特殊压缩模式和目标大小是否支持及其效果取决于服务端版本、集合配置和 SDK，不由驱动模拟。参数值可写 `?`，分别以 `setBoolean` / `setLong` 绑定。

`SHOW COMPACTION ?` 接受 `setLong` 绑定的任务 ID，返回一行 `COMPACTION_ID`、`STATE`、`EXECUTING_PLANS`、`COMPLETED_PLANS`、`TIMEOUT_PLANS`。STATE 是 SDK 状态名称，后三列为 BIGINT 计划数量，不是百分比或受影响行数。

`SHOW COMPACTION PLANS ?` 返回一行 `COMPACTION_ID`、`STATE`、`PLANS`；PLANS 是 VARCHAR JSON 数组，每个计划包含 `sources` 源段 ID 列表和 `target` 目标段 ID。即使没有合并计划，也保留状态行并返回 `[]`。两次 SHOW 是独立快照，结果可能随任务推进变化；不存在的任务或服务端错误作为 SQLException 返回。SQL 不扩大 SDK 本身提供的状态或失败信息。

## 6. 查询与搜索 (DQL) {#dql}

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

### 查询级选项 {#query-options}

标量 SELECT、COUNT 和普通 KNN/范围 SELECT 支持以下 `WITH` 选项；普通请求与官方分页迭代器使用相同设置。

| 选项 | JDBC 参数类型 | SDK 字段与行为 |
| --- | --- | --- |
| ignore_growing | Boolean / setBoolean | ignoreGrowing；true 时跳过 growing segments，可能排除刚写入、尚未封存的数据。默认沿用 SDK 的 false。 |
| timezone | String / setString | timezone；时区名称，例如 UTC、Asia/Shanghai，供服务端时间表达式使用。合法名称及实际时间语义由服务端判断；未指定或空字符串时沿用 SDK 默认行为，不发送时区参数。 |

```sql
SELECT id FROM books WHERE id > ? LIMIT ? WITH(ignore_growing=?,timezone=?);
SELECT COUNT(*) FROM books WHERE id > ? WITH(ignore_growing=?,timezone=?);
COUNT FROM books WITH(ignore_growing=false,timezone='UTC');
```

WITH 参数在 WHERE、LIMIT/OFFSET 后依次绑定，不能把布尔值写成 `'true'` 等字符串。标量 SELECT/COUNT 不接受 `nprobe`、`ef`、`round_decimal` 等向量搜索参数，也不允许通过 WITH 设置 limit/offset；未知选项会抛出 SQLException，不再静默忽略。原有通用 WITH 解析器对重复键保留最后一个值，但所有 `?` 仍按 SQL 顺序消费，建议每个键只写一次。

`timezone` 只传入 SDK 请求，不修改 JVM/JDBC 时区，也不把 VARCHAR 日期字段变成原生时间类型。传递该参数不等于已支持 Milvus 的全部时间类型或时间函数。`overwrite_find_as_count` 将 SELECT 改为 COUNT 时仍保留这两项标量选项。Hybrid 不接受这两项外层选项，其时区应按路设置，见 [Hybrid Search](#hybrid)。API 对照：[Query](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/query.md)、[QueryIterator](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/queryIterator.md)。

### 向量搜索 (Search)

使用特有的 `<->` 运算符或 `vector_range` 函数表示向量距离计算。

```sql
-- 基本搜索 (KNN, 默认参数)
SELECT * FROM table_name ORDER BY vector_col <-> [0.1, 0.2] LIMIT 10;

-- 带前置过滤的搜索
SELECT * FROM table_name WHERE category = 'book' ORDER BY vector_col <-> [0.1, 0.2] LIMIT 5;

-- 范围搜索/距离过滤 (Range Search)
-- 方式1：使用 vector_range 函数 (推荐)
SELECT * FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.8) LIMIT 5;

-- 方式2：使用比较表达式 (WHERE vector_col <-> [vector] < distance_threshold)
SELECT * FROM table_name WHERE vector_col <-> [0.1, 0.2] < 0.8 LIMIT 5;
```

### 距离类型与范围约束

`<->` 为 L2（越小越近），`<=>` 为 COSINE，`<#>` 为 IP（后两者越大越相似）。SELECT、UPDATE、DELETE 使用相同映射。

范围条件支持 `v <-> ? < radius`，以及 `v <=> ? > threshold`、`v <#> ? > threshold`。`vector_range(v, vector, radius)` 固定表示 L2 小于半径。半径/阈值必须为有限数值参数或数值字面量；L2 非负，COSINE 在 [-1,1] 内，IP 可为负；负阈值使用 PreparedStatement 参数绑定。字符串、null、NaN、Infinity 等会在搜索/写入前失败。

仅支持一个向量范围通过 AND 与标量条件组合，不支持含向量范围的 OR/NOT、多个向量范围、其他比较方向或与 ORDER BY 叠加；这些情况明确报错，不丢弃约束。WHERE 范围不能被 WITH 的 radius/range_filter 覆盖。边界方向遵循 [Milvus 2.6 范围搜索规则](https://milvus.io/docs/v2.6.x/range-search.md)。

### 分页与 JDBC 返回上限 {#pagination}

- SQL LIMIT 必须为正整数，OFFSET 必须为非负整数；不截断小数，不接受数字字符串，溢出时报错。参数可用 Byte/Short/Integer/Long/BigInteger，或数值为整数且范围合法的 BigDecimal；Float/Double 不用于条数。
- 优先顺序：Hint 的 `overwrite_find_limit/skip` 覆盖 SQL LIMIT/OFFSET；正值 `setMaxRows` 再限制最多返回行数。即使 SQL 参数被覆盖，也会按位置绑定和校验。
- 标量、KNN、范围 SELECT 均按需读取。无 LIMIT、有效上限大于单页或带 OFFSET 时使用官方迭代器；一页内且无 OFFSET 的有界查询保留普通 Query/Search 请求。COUNT 仍返回单行统计结果。
- `Statement.setFetchSize(n)` 在执行前设置页大小；未设置时使用 SDK 默认值，超过 SDK 单页最大值时截到单页上限，不影响返回总量。OFFSET 在 JDBC 游标内逐页跳过，再按有效 LIMIT/maxRows 返回；很大的 OFFSET 仍有相应扫描成本。OFFSET 与有效上限之和超出 long 范围时在查询前拒绝。
- 查询在 SQL 执行时建立，SDK 初始化可能预取首批数据；ResultSet 游标推进时才继续消费页，不在返回 ResultSet 前遍历完整集合。驱动只保留当前页和当前行；SDK 内部缓存与服务端搜索限制仍由 SDK/服务端决定，无 LIMIT 不另加固定总条数。
- 达到返回上限、读到 EOF、关闭 ResultSet、重新执行/关闭 Statement 或关闭 Connection 都释放迭代器。多语句结果通过标准 `getMoreResults` 访问，KEEP_CURRENT_RESULT 保留各自游标，CLOSE_ALL_RESULTS 释放已保留结果。
- execute 返回后仍可用 `Statement.cancel()` 取消本次执行的未读结果，不影响同连接其他 Statement。查询超时从执行开始计时，包含后续读取和调用方停顿；取页前后检查，空闲超时在下一次读取时报告并释放。取消/关闭不会等待在途 SDK 取页完成，该调用返回后丢弃结果并释放；不承诺强制中断 RPC。
- 后续取页错误可能从 `ResultSet.next()` 抛出，游标不会自动重开或重放；读失败与关闭失败同时发生时保留主异常及 suppressed。始终用 try-with-resources 关闭未读完的结果。

迭代器参数对照：[QueryIterator](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/queryIterator.md)、[SearchIterator](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/searchIterator.md)。

### 高级搜索参数 (WITH 子句)

本节描述向量 SELECT 的搜索参数；标量 SELECT/COUNT 仅接受[查询级选项](#query-options)，不接受索引搜索或分组参数。

WITH 在 SQL 最后绑定；键是固定名称，值支持字符串、数值、布尔和 `?`。属性通过 JSON 序列化器编码，引号、反斜杠、控制字符不会产生额外属性；带引号的数字仍是字符串。搜索参数例如 `nprobe`、`ef` 直接写在 WITH 中。

普通 KNN 和范围 SELECT 还支持 `round_decimal`（整数，距离分数的小数位数，具体范围由服务端验证），并使用上述 `ignore_growing` / `timezone` 查询级选项。这些值进入 SDK 独立请求字段，不混入索引 searchParams；普通、迭代及分组检索均保留设置。示例：`WITH (round_decimal=2, ignore_growing=false, timezone='UTC')`；参数分别使用 `setInt`、`setBoolean`、`setString`，不使用带引号的数字或布尔字符串。本段不适用于 Hybrid Search。

`metric_type` 若指定，必须与 SQL 距离运算符一致。分页用 SQL OFFSET 或 Hint，不使用 WITH offset。WITH 是搜索参数透传，不代表支持所有 SDK builder 属性；一致性使用 JDBC 连接参数，输出字段由 SELECT 决定。

```sql
SELECT id, score FROM table_name
WHERE age > ?
ORDER BY vector_col <=> ?
LIMIT ? OFFSET ?
WITH (metric_type='COSINE', nprobe=?);
```

### 向量分组搜索 {#grouping}

当同一文档的多个片段占据相似度搜索的前几名时，可以按文档 ID 或类别分组，由 Milvus 选取每组的匹配实体。这是原生 ANN/Hybrid 分组，不是关系型 GROUP BY、DISTINCT 或 SUM/COUNT 聚合。

```sql
SELECT id, category, score FROM books ORDER BY vector_col <-> ?
WITH (group_by_field='category', group_limit=3, group_size=2, strict_group_size=true);

SELECT id, category FROM books ORDER BY vector_col <-> ? LIMIT 3 OFFSET 1
WITH (group_by_field='category', group_limit=2, group_offset=1, group_size=2, strict_group_size=true);
```

| WITH 选项 | 含义 |
| --- | --- |
| group_by_field | 必填的非空字段名字符串，映射到 SDK groupByFieldName；具体字段类型和索引兼容性由 Milvus 检查 |
| group_limit | 必填的正 INT32，表示最多返回多少组，映射到原生 Search topK / Hybrid limit |
| group_offset | 非负 BIGINT，原生组偏移，省略时为 0；与 group_limit 相加不得溢出 |
| group_size | 可选正 INT32，每组期望返回的实体数；省略时保留 SDK/服务端默认值 |
| strict_group_size | 可选布尔值，要求服务端尽力达到每组的 group_size；不足的组不会由驱动补齐 |

以上值均可用 `?` 绑定，整数和布尔值使用对应 JDBC 类型。选项只放在 SELECT 最后的 WITH 中；Hybrid 的每路 ANN WITH 不接受分组选项。SQL LIMIT/OFFSET 及其覆盖 Hint、JDBC maxRows 始终以**行**为单位，在原生组窗口返回后应用，可能截断某个组。未写 SQL LIMIT 时读取所选组窗口的全部行，不是扫描全部组；group_limit 仍必须显式指定，不从 SQL LIMIT 推断，也没有固定上限兜底。

第一例选择最多 3 组、每组期望 2 条，可能返回 6 行；第二例先由 Milvus 跳过 1 组并选择 2 组，然后 JDBC 跳过其中 1 行、最多返回 3 行。分组、组内选择和排序由服务端完成，驱动不重新聚合、补满组或重新排序。结果仍是一个扁平 ResultSet，列只取 SELECT 投影；需要组字段时应显式选择该列。

分组搜索使用一次有界 Search/Hybrid 请求，不支持分组迭代器。fetchSize 不会把组窗口拆成多次服务端搜索，maxRows 也不会缩小已请求的组数；请按业务需要选择 group_limit 和 group_size，并遵守服务端检索窗口限制。范围检索、数据类型、索引和服务端版本的组合限制直接返回 SQLException，不回退成客户端分组。

Java SDK 2.6.22 的 SearchIteratorReqV2 虽声明了 groupByFieldName，但 Milvus 2.6.2 原生调用拒绝该组合，返回 `Not allowed to do groupBy when doing iteration`。同一环境的非分组迭代正常；不能以请求类存在字段推定支持分组分页，也不将此结论自动推广到后续服务端版本。

Hybrid 也可在最终 WITH 中同时指定 reranker 和以上选项；此时 group_limit 取代“必须提供外层 SQL LIMIT”的要求，各路候选 LIMIT 仍以候选实体为单位：

```sql
SELECT id,category,score FROM books ORDER BY HYBRID (
    vector_col <-> ? LIMIT 20,
    other_vector <-> ? LIMIT 20
) WITH (reranker='rrf',group_by_field='category',group_limit=3,group_size=2,strict_group_size=true);
```

Milvus 2.6.2 已验证稠密 FLAT/L2 下按 VARCHAR 字段分组、默认每组一条、严格每组多条、组偏移与行窗口组合及 RRF Hybrid 分组。这不代表所有索引/字段/融合策略组合均已验收，稀疏、二进制及其他向量类型的分组组合需另行验证。参数语义参阅[官方分组搜索说明](https://blog.milvus.io/docs/v2.6.x/grouping-search.md)。

### JDBC 结果元数据

列顺序遵循 SELECT，空结果保留 schema 元数据。Int8/16/32/64 对应 TINYINT/SMALLINT/INTEGER/BIGINT，Float/Double/Bool/VarChar 对应标准类型；JSON 为 OTHER（JsonElement），FloatVector/Array 为 ARRAY（List/getArray），Binary/FP16/BF16/Int8Vector 为 VARBINARY（byte[]/getBytes），Sparse 为 OTHER（SPARSE_FLOAT_VECTOR，SortedMap&lt;Long,Float>）。getArray 元素类型、isNullable、isAutoIncrement 来自 schema。向量 score 为 Float，不作为存储字段发送；向量 SELECT * 包含它，显式投影仅指定 score 时返回；标量查询不生成 score。

### 统计总数 (Count)

使用 `COUNT FROM ...` 或 `SELECT COUNT(*) FROM ...` 查询集合或分区的记录总数，两者映射同一个 Milvus 原生计数请求，不遍历客户端结果来计数。
结果是一行 `COUNT`（BIGINT），用 `rs.getLong("COUNT")` 获取；空匹配返回 0。可在末尾用 WITH 指定[查询级选项](#query-options)，`ignore_growing=true` 计数同样排除 growing segments。不支持 COUNT(field)、混合字段投影、GROUP BY 或向量范围条件。

```sql
-- 查询全表总数
count from table_name;
SELECT COUNT(*) FROM table_name;

-- 查询指定分区的总数
COUNT FROM table_name PARTITION partition_name;

-- 带条件过滤的总数 (支持标量过滤)
count from table_name where age > 18;
```

### 查询提示 (Hints) {#hint}

本适配器支持使用 SQL Hint (`/*+ ... */`) 来覆盖默认的分页或查询行为，类似于 Elastic/Mongo 适配器中的用法。

支持的 Hint：

- `overwrite_find_limit`: 强制覆盖查询的 LIMIT (TopK)。
- `overwrite_find_skip`: 强制覆盖查询的 OFFSET。
- `overwrite_find_as_count`: 按是否出现切换为 COUNT，忽略投影/排序，不按向量选择计算数量，向量范围条件会被拒绝。即使值为 false 也触发，需要关闭时删除 Hint。WHERE、向量、分页、WITH 的占位符仍需按原 SQL 位置绑定并通过校验。

普通 Query/Search 请求的一致性使用连接参数 `consistencyLevel`，`consistency_level` 不是已实现的查询 Hint。Java SDK 2.6.22 的 QueryIterator 会使用集合默认一致性；依赖分页查询“写后立即可读”时，建表应显式使用 `WITH (consistency_level='Strong')`，不能仅依赖连接参数。上述三个 overwrite Hint 仅影响 SELECT，不影响 UPDATE/DELETE；IMPORT/LOAD/RELEASE 的 sync/timeout 见前文。

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

## 7. 进度监控 (Progress) {#progress}

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


## 8. JDBC 调用与返回结果 {#jdbc-results}

`SELECT/COUNT/SHOW` 返回 ResultSet；写入及管理命令返回更新计数。调用 executeQuery/executeUpdate 时应匹配这个分类；未知或混合结果使用 execute。

### 多语句与多结果集

以下片段使用已经打开的 `conn` 和[入门程序](./jdbc.mdx)建立的 `books_demo`，每个结果集都是一条 SQL 的返回，不是一个 SELECT 中多查询向量的结果组：

```java
String sql = "SELECT book_id FROM books_demo LIMIT 2; " +
        "UPDATE books_demo SET word_count = ? WHERE book_id = ? LIMIT 1; " +
        "COUNT FROM books_demo";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, 2000);
    ps.setLong(2, 1L);
    boolean hasResult = ps.execute();
    while (true) {
        if (hasResult) {
            try (ResultSet rs = ps.getResultSet()) {
                ResultSetMetaData md = rs.getMetaData();
                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        System.out.println(md.getColumnLabel(i) + "=" + rs.getObject(i));
                    }
                }
            }
        } else {
            long count = ps.getLargeUpdateCount();
            if (count == -1) {
                break;
            }
            System.out.println("updated=" + count);
        }
        hasResult = ps.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
    }
}
```

`getMoreResults()` 默认关闭当前结果；需要保留时用 KEEP_CURRENT_RESULT 并自行关闭，CLOSE_ALL_RESULTS 关闭已保留结果。重新执行同一 Statement 会关闭旧结果。不要将“更新计数为 0”误判为结果结束；只有当前无结果集且更新计数为 -1 才结束。

### 返回列速查

| 命令 | 返回 |
| --- | --- |
| SELECT | SELECT 字段列表；向量查询可包含 score。 |
| COUNT FROM / SELECT COUNT(*) | COUNT（BIGINT），一行。 |
| SHOW DATABASES / TABLES / PARTITIONS | DATABASE / TABLE / PARTITION（VARCHAR）。 |
| SHOW DATABASE | DATABASE、PROPERTIES（VARCHAR），PROPERTIES 为 JSON 对象文本。 |
| SHOW ALIASES FROM / SHOW ALIAS | ALIAS、TABLE；SHOW ALIAS 另含 DATABASE（均为 VARCHAR）。 |
| SHOW PARTITION | PARTITION（VARCHAR），不是分区统计信息。 |
| SHOW STATS FROM | NUM_ENTITIES（BIGINT）、STATS（VARCHAR，JSON 对象文本）。 |
| SHOW TABLE | FIELD, TYPE, DIMENSION, PRIMARY, AUTO_ID, DESCRIPTION, NULLABLE, ELEMENT_TYPE, MAX_CAPACITY, MAX_LENGTH, PARTITION_KEY, CLUSTERING_KEY。 |
| SHOW CREATE TABLE | CREATE SCRIPT（VARCHAR）；用于查看 schema 描述，不承诺外部复杂 schema 的无损还原。 |
| SHOW INDEX / INDEXES | INDEX、FIELD、ID、PARAMS。 |
| SHOW USERS / ROLES | USER / ROLE（VARCHAR）。 |
| SHOW USER | USER、ROLES（JSON 角色数组）、DESCRIPTION，均为 VARCHAR。 |
| SHOW ROLE | ROLE、DESCRIPTION、GRANTS（JSON 直接授权数组），均为 VARCHAR。 |
| SHOW PRIVILEGE GROUPS | PRIVILEGE_GROUP、PRIVILEGES（JSON 权限名称数组），均为 VARCHAR。 |
| ANALYZE | TEXT_INDEX（BIGINT，从 1 开始）、TOKENS（VARCHAR，JSON token 数组）。 |
| SHOW GRANTS | DATABASE、ROLE、OBJECT、OBJECT_NAME、PRIVILEGE。 |
| SHOW PROGRESS OF LOADING | PROGRESS（BIGINT）。 |
| SHOW PROGRESS OF INDEX | TOTAL、INDEXED（BIGINT）；未指定索引名时累计 SDK 返回的索引记录，不是完成百分比。 |
| INSERT / UPSERT | SDK 实际确认的 long 计数；请求主键时另提供 getGeneratedKeys 游标。 |
| UPDATE / DELETE | SDK 确认的更新/删除计数；不保证等于状态实际发生变化的实体数。大计数使用 Large 方法。 |
| COMPACT / SHOW COMPACTION / SHOW COMPACTION PLANS | 单行 ResultSet：任务 ID / 状态与计划数量 / 状态与计划 JSON，见[压缩任务](#compaction)。 |
| DDL、授权、普通 IMPORT、LOAD/RELEASE、按集合 FLUSH | 更新计数 0；IMPORT RETURNING JOB_ID 则返回一行任务 ID。 |
| FLUSH ALL TABLES / SHOW FLUSH ALL | ResultSet：原生落盘时间戳 / 时间戳与完成状态。不是更新计数或事务提交。 |

简单主键条件的无 LIMIT DELETE 由服务端直接处理。Milvus 2.6.2 对 `pk = ?` 的重复删除，即使实体已不存在也可能返回 1；它计数的是提交的删除主键，而不是预查询得出的现存行数。驱动不会先查再删或把这个计数改写成关系数据库式影响行数。不要用 DELETE 返回值判断实体在删除前是否存在。参见[服务端删除实现](https://github.com/milvus-io/milvus/blob/v2.6.2/internal/proxy/task_delete.go)。

### JDBC 使用边界

- ResultSet 为只读、前向访问，读取后及时关闭。FloatVector/Array 用 List/getArray；Binary/FP16/BF16/Int8Vector 用 getBytes；Sparse 用 getObject 得到 SortedMap，不假定所有向量都是 float[]。
- RETURN_GENERATED_KEYS 支持 INSERT/UPSERT 的 Int64/VarChar、AUTO_ID 和显式主键；只返回 SDK ID，NO_GENERATED_KEYS 为空游标。默认不请求主键；请求大量主键需要 O(主键数) 内存。
- 不支持 JDBC addBatch/executeBatch、savepoint、可更新 ResultSet 或跨页事务。PreparedStatement 参数化与 JDBC batch 是不同能力。
- 本文列出的 SQL/SHOW 结果是当前可用入口，不承诺所有 DatabaseMetaData 方法满足 ORM/BI/迁移工具的关系数据库假设。

## 9. 类型、多行写入及融合搜索 {#extended}

### 类型绑定与 schema

完整 Java 绑定/主键示例见 [类型绑定与主键回传](./jdbc.mdx#typed-values)。Binary 使用 byte[]/ByteBuffer/字节 List；FP16/BF16 数值输入经官方 Float16Utils 编码，byte[]/ByteBuffer 是小端原始编码；Sparse 使用非空 Map&lt;Number,Number>，索引为 [0,4294967295) 内的整数，权重有限。ByteBuffer 只消费 remaining，不改变其位置。FloatVector 的 byte[] 仍按数值解释。

```sql
CREATE TABLE types_demo (
    id INT64 PRIMARY KEY AUTO_ID,
    tags ARRAY<VARCHAR(30)>(8) NULL,
    flags ARRAY<BOOL>(8),
    bits BINARY_VECTOR(16),
    half FLOAT16_VECTOR(2),
    brain BFLOAT16_VECTOR(2),
    sparse SPARSE_FLOAT_VECTOR
);
INSERT INTO types_demo (tags,flags,bits,half,brain,sparse) VALUES (?, ?, ?, ?, ?, ?);
SELECT id,bits FROM types_demo ORDER BY bits ~= ? LIMIT 10;
SELECT id,score FROM types_demo WHERE bits <%> ? < 0.5 LIMIT 10;
SELECT id,half FROM types_demo ORDER BY half <=> ? LIMIT 10;
SELECT id,brain FROM types_demo ORDER BY brain <-> ? LIMIT 10;
SELECT id,sparse FROM types_demo ORDER BY sparse <#> ? LIMIT 10;
SELECT id,tags FROM types_demo WHERE tags IS NOT NULL LIMIT 10;
```

ARRAY 的容量、元素类型及 VARCHAR 字节长度写入 SDK schema；读取通过 getArray 得到实际元素 JDBC 类型。数组本身可 NULL 或空数组，元素不可 NULL。nullable 由 isNullable 报告；SHOW TABLE 追加 NULLABLE、ELEMENT_TYPE、MAX_CAPACITY、MAX_LENGTH 列，原有列顺序不变。SHOW CREATE 保留这些定义。向量 NULL 从 Milvus 2.6.18 起可用，且不能用向量 IS NULL 过滤，见[官方版本说明](https://github.com/milvus-io/milvus/releases/tag/v2.6.18)与[nullable 文档](https://milvus.io/docs/v2.6.x/nullable-and-default.md)。

### 多行写入与 Generated Keys {#generated-keys}

```sql
INSERT INTO docs (id,body,dense) VALUES (1,'first',[1,2]),(2,'second',[3,4]);
UPSERT INTO docs (id,body,dense) VALUES (1,'changed',[1,2]),(3,'third',[3,4]);
INSERT INTO docs (id,body,dense) VALUES ?;
UPSERT INTO docs VALUES ?;
```

VALUES ? 可绑定 Iterable/Iterator：带列名时每行 List/Object[]，Map 的键须匹配列名；省略列名时每行必须为 Map。省略 schema 默认值/nullable/function 输出字段时交给 SDK 处理。fetchSize 控制每次发送的实体数，0 沿用 SDK 默认，超大值截到 SDK 单页上限；不限制总量。驱动不关闭调用方提供的 Iterator/流。

INSERT/UPSERT 返回实际 SDK long 计数。使用 prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) 或 Statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS) 请求主键；getGeneratedKeys 返回 SDK ID，列名为主键名。默认/NO_GENERATED_KEYS 为空游标；maxRows 不限制写入或主键。

不对普通 INSERT/UPSERT 自动重试。分页失败包含 phase、confirmedPages、confirmedRows、currentPageRows；确认过的页不会回滚，未确认的页可能已经写入。主键仅在成功响应时交付；请求全量主键会使用 O(主键数) 内存。

### BM25 / TextEmbedding schema 函数 {#functions}

函数定义置于字段之后；文本输入须非 nullable VARCHAR，输出由服务端生成，不在 INSERT/UPSERT 中手工填写。

```sql
CREATE TABLE docs (
    id INT64 PRIMARY KEY,
    body VARCHAR(2000) WITH (enable_analyzer=true, analyzer_params='{"type":"standard"}'),
    dense FLOAT_VECTOR(2), sparse SPARSE_FLOAT_VECTOR,
    FUNCTION bm25_fn USING BM25 (body) INTO (sparse)
);
CREATE INDEX bm25_idx ON docs(sparse) USING SPARSE_INVERTED_INDEX
    WITH (metric_type=BM25,bm25_k1=1.2,bm25_b=0.75);
SELECT id,score FROM docs ORDER BY sparse <?> 'hybrid search' LIMIT 10;

CREATE TABLE embedded_docs (
    id INT64 PRIMARY KEY, body VARCHAR(2000), dense FLOAT_VECTOR(1536),
    FUNCTION embed USING TEXTEMBEDDING (body) INTO (dense)
        WITH (provider='openai',model_name='text-embedding-3-small')
);
SELECT id,score FROM embedded_docs ORDER BY dense <=> 'search text' LIMIT 10;
```

BM25 必须 enable_analyzer=true、Sparse 输出；TextEmbedding 使用稠密浮点向量输出。字段 WITH 仅开放 enable_analyzer、enable_match（布尔）、analyzer_params（JSON 对象字符串）；函数 WITH 的标量值转为 SDK 字符串参数。TextEmbedding 需要对应 Milvus 版本、provider 配置、模型和网络/凭据，模型维度须与输出一致；运行上例前需完成外部模型服务配置。provider 密钥建议配置在服务端，不把秘密写进 SQL/SHOW CREATE。当前 schema 函数为 BM25/TEXTEMBEDDING，不承诺所有未来 FunctionType。

函数定义支持 `INTO (...) DESCRIPTION '说明' WITH (...)`，`DESCRIPTION` 可使用 `?` 绑定非 NULL 字符串。说明写入 SDK 的独立 `description` 字段，并由 `SHOW CREATE TABLE` 保留；`WITH (description=...)` 仍是函数参数，不是说明。省略说明时使用空字符串。Milvus 2.6.2 已验证建表说明往返及 BM25 自动生成向量后的检索。

说明字面量中，单引号写作 `''`，反斜杠写作 `\\`；`\n`、`\r`、`\t` 分别表示换行、回车和制表符。PreparedStatement 绑定值直接传入，不需要这些 SQL 转义；SHOW CREATE 会生成可重新解析的说明字面量。

#### 在线函数管理 {#alter-functions}

服务端提供对应 API 时，可对已有集合提交以下命令：

```sql
ALTER TABLE online_docs ADD FUNCTION embed USING TEXTEMBEDDING (body) INTO (dense)
    DESCRIPTION 'Document embedding'
    WITH (provider='openai',model_name='text-embedding-3-small');
ALTER TABLE online_docs ALTER FUNCTION embed USING TEXTEMBEDDING (body) INTO (dense)
    DESCRIPTION 'Updated description'
    WITH (provider='openai',model_name='text-embedding-3-small');
ALTER TABLE online_docs DROP FUNCTION embed;
```

`online_docs` 必须已具有符合函数要求的输入、输出字段；TextEmbedding 还需上述模型服务配置。ADD/ALTER 复用建表的 BM25/TEXTEMBEDDING 定义。ALTER 提交完整的新定义，不是合并参数；函数名标识被替换的函数，不提供重命名。各操作使用当前连接数据库，不隐式加字段、加载集合、重建索引或回填历史数据；允许的 schema、加载状态及历史数据处理以服务端规则为准。

每条 SQL 分别对应官方 `addCollectionFunction`、`alterCollectionFunction` 或 `dropCollectionFunction` 请求，成功返回更新计数 0，不返回结果集。名称不支持 `?`；说明和 WITH 参数可以绑定。失败通过 SQLException 返回，后续语句不继续执行，不通过 DROP/CREATE 兜底。

**版本边界：Milvus 2.6.2 对三个在线 API 均返回 UNIMPLEMENTED。** 已验证失败后集合定义及原有 BM25 功能保留；这不等于运行中的任意失败均可回滚。驱动已接入请求，但未验收新版服务端的在线变更效果，部署前请核对服务端支持，参见[官方新增函数 API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/addCollectionFunction.md)及[修改函数 API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/alterCollectionFunction.md)。

### Hybrid Search 与 rerank {#hybrid}

```sql
SELECT id,score FROM docs WHERE id > ? ORDER BY HYBRID (
    dense <-> ? LIMIT 20 WITH (nprobe=10),
    sparse <?> ? LIMIT 30
) LIMIT 10 OFFSET 2 WITH (reranker='rrf',k=60);

SELECT id,score FROM docs ORDER BY HYBRID (
    dense <=> ? LIMIT 20,
    sparse <#> ? LIMIT 30
) LIMIT 10 WITH (reranker='weighted',weights='[0.7,0.3]');
```

每路仍只有一个查询向量，可以查询相同字段的不同向量，也可以不同字段/metric；向量列表嵌套不表示另一种 NQ 批量接口。可在每路向量后、LIMIT 前写独立的标量 WHERE。该路的有效过滤条件为 `(公共 WHERE) AND (本路 WHERE)`；局部 OR 保留括号，不能绕过公共条件。两种 WHERE 都可省略，局部过滤不会影响其他路。参数按 SQL 顺序连续绑定：公共 WHERE、逐路向量/WHERE/LIMIT/WITH、最终 LIMIT/OFFSET/WITH；所有条件值仍通过 SDK filterTemplateValues 绑定，不拼接回表达式。

```sql
SELECT id,score FROM docs WHERE tenant_id = ? ORDER BY HYBRID (
    dense <-> ? WHERE category = ? OR category = ? LIMIT 20 WITH(timezone='UTC',nprobe=10),
    sparse <?> ? WHERE publish_year >= ? LIMIT 30 WITH(timezone=?)
) LIMIT 10 WITH(reranker='rrf',k=60,round_decimal=3);
```

上例要求集合中已存在 tenant_id、category、publish_year 及相应向量字段。每路 `timezone` 是字符串，进入 AnnSearchReq 的独立字段；空字符串沿用 SDK 默认行为。公共时间过滤也在各路指定的时区下解释。时区不放入最外层 WITH，不提供原生时间类型的客户端模拟。每路 WHERE 只接受标量条件；不要在其中另加向量范围表达式，范围参数仍使用该路的原生搜索参数。

外层 `round_decimal` 是 INT32 范围的整数，映射 HybridSearchReq.roundDecimal，由服务端对最终融合分数舍入，不改变 JDBC 的 score 类型，也不由驱动重新排序。未指定时沿用 SDK 默认值 -1（不舍入）；具体可用精度范围由服务端判断。该参数不能放入每路 WITH。它也可与外层分组参数同时使用。对应 API：[hybridSearch](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/hybridSearch.md)。

每路 LIMIT 是候选量，最终 LIMIT/OFFSET 作用于服务端融合后的一个结果集。RRF 的 k 省略时沿用 SDK 默认；Weighted 必须提供与路数相同的 [0,1] 有限权重。reranker 必须显式选择；不识别的 rerank 参数报错。SDK 无 Hybrid Iterator，所以普通 Hybrid 外层必须显式 LIMIT；启用[分组搜索](#grouping)时改由必填的 group_limit 限定组窗口，SQL LIMIT/OFFSET 仍限定结果行。fetchSize 不将融合变为分页；服务端窗口和候选路数限制仍适用，驱动不以固定总量兜底。普通 ORDER BY 只接受一个查询向量的规则不变。

### 多文件 Import 与任务观察 {#import}

```sql
/*+ sync=false */ IMPORT FROM [['prepared/a.parquet'],['prepared/b.parquet']]
    INTO docs WITH (timeout='2h') RETURNING JOB_ID;
/*+ sync=false */ IMPORT FROM [['prepared/id.npy','prepared/dense.npy']]
    INTO docs RETURNING JOB_ID;
SHOW IMPORT 'job-id';
SHOW PROGRESS OF IMPORT ?;
SHOW IMPORTS FROM docs WITH (page_size=20,current_page=1);
```

RETURNING JOB_ID 使用 executeQuery；不带 RETURNING 保留更新计数 0。SHOW 返回 JOB_ID(VARCHAR)、STATE(VARCHAR)、PROGRESS/TOTAL_ROWS/IMPORTED_ROWS(BIGINT)、REASON(VARCHAR)、DETAILS(JSON)，服务端未给出的字段为 NULL，DETAILS 保留文件级状态。SHOW IMPORTS 的分页由目标 REST 版本决定，不承诺单条语句遍历全部历史任务。

复用 JDBC 认证/database，REST 地址须可达；同一连接的任务提交和状态查询使用同一个 JDBC 地址。重连后应连接原任务所在的服务。输入文件须已在 Milvus 服务端配置的存储中：对象存储部署使用对象路径；本地存储的单机部署使用服务端可读取的绝对文件路径，不是 JDBC 客户端文件路径。驱动不提供文件生成/上传，官方 BulkWriter 可用于准备。

Milvus 2.6.2 单机本地存储下已验证 JSON 文件导入、RETURNING JOB_ID、SHOW IMPORT 的 Completed/100% 进度及精确行数回读。缺失文件的异步与同步导入也已验证：任务失败原因及 ID 可继续查询，同步失败异常保留这些信息，任务不会被驱动重复提交。这不代表分布式文件可见性、所有导入格式或大规模吞吐已经验证；文件准备、访问权限及资源限额仍由部署负责。

sync=true 默认等待；Hint timeout 是客户端毫秒等待期限，WITH timeout 是服务端任务期限字符串。超时、取消、状态 Failed 或后续 HTTP 失败保留已知 Job ID 和最后进度，不撤销、不重复提交任务；创建响应丢失时任务可能存在但没有 ID，需要列表核实。整个导入不承诺事务或精确一次。

注意：主动 Statement.cancel() 或公共 JDBC 超时可能优先返回标准取消/超时异常，而不携带适配器进度；不能把该异常当作“零行已写入”。需要可靠记录导入任务 ID 时使用 sync=false RETURNING JOB_ID，再独立查询。

版本与功能要求见[版本与支持范围](./compatibility.md)。
