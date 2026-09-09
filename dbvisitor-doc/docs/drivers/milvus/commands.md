---
id: commands
sidebar_position: 3
title: 语法手册
description: 当前 jdbc-milvus SQL 子集、参数规则、DDL、DML、向量查询、融合搜索和 JDBC 返回约定。
---

> **版本基线**：本文对应当前 6.7.1-SNAPSHOT 实现，Java 17、Java SDK 2.6.22，服务端最低基线 Milvus 2.6.2。不支持 2.5.x 及更早版本；后续版本的兼容性仍需真集群验证。

连接、依赖和完整入门程序见 [安装与使用](./usecase.mdx)。本文以当前解析器和命令实现为准，不把完整 Milvus SDK 或关系数据库 SQL 当作隐含支持。

TLS、证书和 Cloud 使用 JDBC 连接属性，不增加 SQL 语法或 Hint。`secure`、`caPemPath`/`serverPemPath`、`clientPemPath`/`clientKeyPath`、`serverName` 对 SDK 与 Import REST 共用，两者始终访问 JDBC URL 指定的主机和端口。Milvus 2.6.2 原生 TLS 的两个内部监听需要通过统一入口提供完整 Import 能力；驱动不自动连接其他端口。参数组合、证书格式、部署限制和示例见[连接参数与 TLS](./params.md#tls)。

## 阅读约定

- `text` 代码块是语法模板：`[IF EXISTS]` 等方括号表示可选部分，不要原样执行；`sql` 块是语法示例，替换集合/字段名并满足 schema 后使用。向量 `[0.1, 0.2]` 的方括号属于真实语法。
- SQL 关键字不区分大小写；名称使用普通标识符，示例采用字母/下划线开头。当前不提供带引号标识符或跨库限定名解析；字符串用单/双引号，不能将字符串引用当作字段名引用。
- ? 用于 WHERE、向量、SET、VALUES、LIMIT/OFFSET、Hint、查询/字段/function/索引 WITH 的值。名称和类型不可绑定；集合级 CREATE TABLE WITH 仍只使用常量 consistency_level。
- SELECT 只投影 `*` 或字段列表，不支持 AS、JOIN、GROUP BY、任意投影表达式或标量 ORDER BY。距离排序仅支持本文列出的单查询向量操作；不支持多个排序键。
- 标量 WHERE 支持比较、AND/OR/NOT、括号、LIKE、IN 列表、IS NULL/IS NOT NULL 及部分表达式透传；不是所有关系数据库函数。IN 支持 [1,2]、(1,2) 或绑定 List。SQL NULL 可用于值；一般负数字面量仍请用参数绑定（DEFAULT 单独支持正负号）。
- 同一请求可用分号分隔多条 SQL，参数从第一条到最后一条连续编号，返回通过标准 JDBC 多结果接口读取。它不构成事务，也不是 JDBC batch。


## 1. 数据库管理 (Database Management) {#database}

### 创建数据库

```text
CREATE DATABASE [IF NOT EXISTS] db_name;
```

### 删除数据库

```text
DROP DATABASE [IF EXISTS] db_name;
```

### 修改数据库属性

```text
ALTER DATABASE db_name SET PROPERTIES ("key" = "value", ...);
```

### 查看数据库列表

```sql
SHOW DATABASES;
```

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
| `ARRAY<element_type>(max_capacity)` | BOOL、INT8/16/32/64、FLOAT、DOUBLE、VARCHAR(n) 元素，容量 1–4096；不允许嵌套或 NULL 元素，裸 ARRAY 明确拒绝。 |
| `PRIMARY KEY`、`AUTO_ID`、`NULL`、`NOT NULL`、`COMMENT 'text'` | 默认 NOT NULL，NULL 开启 nullable；主键不可 nullable，冲突约束拒绝。动态字段默认关闭。 |

CREATE TABLE 的 WITH 当前只消费 `consistency_level`，例如 Strong/Bounded/Session/Eventually，其他集合属性没有通用透传支持。不要将 DQL 的 WITH 或连接参数规则套用到 DDL。

### DEFAULT 默认值

支持非主键标量字段的 `DEFAULT`：`BOOL`、`INT8/INT16/INT32/INT64`、`FLOAT/DOUBLE`、`VARCHAR`。数值允许正负号；整数默认值必须精确落在字段范围内，浮点值必须有限，字符串不能超过声明的 UTF-8 字节长度。非法值、重复 DEFAULT，以及主键、JSON、Array、向量字段的 DEFAULT 会在发送建表请求前被拒绝。

INSERT/UPSERT 省略字段或绑定 null 时，按 SDK 规则由 Milvus 应用 DEFAULT；无 DEFAULT 的 nullable 字段保存 NULL，非 nullable 字段由 SDK/服务端校验。SHOW CREATE 保留默认值、nullable、Array 元素/容量、分析器和函数。向量 nullable 需 2.6.18+，不支持向量 IS NULL/IS NOT NULL；标量 nullable/Array 维持 2.6.2 基线。

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

### 重命名表

```sql
ALTER TABLE old_name RENAME TO new_name;
```

### 查看表

```sql
SHOW TABLES;                        -- 列出所有表
SHOW TABLE table_name;              -- 查看字段信息
SHOW CREATE TABLE table_name;       -- 查看建表详细语句
```

### 分区管理 {#partition}

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
```

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

### 查看索引

```sql
SHOW INDEXES FROM table_name;
SHOW INDEX index_name ON TABLE table_name;
```

---

## 4. 用户与权限管理 (User & Role Management) {#user}

### 用户管理

```text
CREATE USER [IF NOT EXISTS] username PASSWORD 'password';
DROP USER [IF EXISTS] username;
SHOW USERS;
```

### 角色管理

```text
CREATE ROLE [IF NOT EXISTS] role_name;
DROP ROLE [IF EXISTS] role_name;
SHOW ROLES;
```

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

不带 ON 返回角色的全部授权记录；ON TABLE / USER 按对象类型和名称精确过滤，ON GLOBAL 只返回 Global 类型的记录。这里查询授权记录，不展开全局或通配授权来计算某个对象的有效权限。

---

## 5. 数据操作 (DML) {#dml}

INSERT/UPSERT 支持多个 VALUES 元组（须显式列名），或 VALUES ? 绑定 Iterable/Iterator，每行为 Map（可省略列名）、List/Object[]（须列名）。不支持 INSERT SELECT。返回 SDK 实际确认的 long 计数；请求 RETURN_GENERATED_KEYS 时另提供标准主键游标。

### 插入数据

```sql
-- 插入到默认分区
INSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- 插入到指定分区
INSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);
```

### Upsert (插入或覆盖)

```sql
-- 插入或覆盖到默认分区
UPSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- 插入或覆盖到指定分区
UPSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);
```

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

> **注意**：无 LIMIT 的标量 DELETE 直接提交服务端过滤表达式；有 LIMIT 时先分页选取主键再删除。向量 DELETE 先分页搜索主键再删除，不提供整条 SQL 的原子性或回滚。标量 DELETE 必须带 WHERE，`DELETE FROM table_name` 不能直接执行。

#### 1. 基础删除 (标量过滤)

使用本手册的标量 WHERE 语法过滤删除。

```sql
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

导入使用官方 REST API，复用 JDBC 地址/database/认证，地址必须开放 REST。同连接创建/列表/进度固定在首次选定的导入端点。普通 IMPORT 返回更新计数 0（非导入行数）；RETURNING JOB_ID 显式返回字符串任务 ID，SHOW IMPORT/SHOW IMPORTS 提供后续查询。

`IMPORT FROM` 默认同步等待 Milvus Import 任务完成；如需异步返回，可使用 `sync=false` Hint。`timeout` Hint 用于设置同步等待超时时间，单位为毫秒。

### 加载与释放 (Load / Release) {#load}

Milvus 要求在搜索前将 Collection 加载到内存。
```text
LOAD TABLE table_name [PARTITION partition_name];
RELEASE TABLE table_name [PARTITION partition_name];

-- 设置加载等待超时
/*+ timeout=60000 */ LOAD TABLE table_name;

-- 仅提交释放请求，不等待释放完成
/*+ sync=false */ RELEASE TABLE table_name;
```

`LOAD TABLE` 默认等待集合或分区进入 Loaded 状态，`RELEASE TABLE` 默认等待进入 NotLoad 状态；可使用 `sync=false` 关闭等待。

---

### Flush {#flush}

```sql
FLUSH table_name;
```

触发并等待落盘，当前 SDK 等待上限为 60000ms，返回更新计数 0；不消费 sync/timeout Hint。FLUSH 不是 JDBC commit，也不是跨语句事务边界。

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

本节 WITH 仅用于向量 SELECT 的搜索参数；标量 SELECT 虽能解析该子句，不应用其中的搜索参数。

WITH 在 SQL 最后绑定；键是固定名称，值支持字符串、数值、布尔和 `?`。属性通过 JSON 序列化器编码，引号、反斜杠、控制字符不会产生额外属性；带引号的数字仍是字符串。搜索参数例如 `nprobe`、`ef` 直接写在 WITH 中。

`metric_type` 若指定，必须与 SQL 距离运算符一致。分页用 SQL OFFSET 或 Hint，不使用 WITH offset。WITH 是搜索参数透传，不代表支持所有 SDK builder 属性；一致性使用 JDBC 连接参数，输出字段由 SELECT 决定。

```sql
SELECT id, score FROM table_name
WHERE age > ?
ORDER BY vector_col <=> ?
LIMIT ? OFFSET ?
WITH (metric_type='COSINE', nprobe=?);
```

### JDBC 结果元数据

列顺序遵循 SELECT，空结果保留 schema 元数据。Int8/16/32/64 对应 TINYINT/SMALLINT/INTEGER/BIGINT，Float/Double/Bool/VarChar 对应标准类型；JSON 为 OTHER（JsonElement），FloatVector/Array 为 ARRAY（List/getArray），Binary/FP16/BF16 为 VARBINARY（byte[]/getBytes），Sparse 为 OTHER（SPARSE_FLOAT_VECTOR，SortedMap&lt;Long,Float>）。getArray 元素类型、isNullable、isAutoIncrement 来自 schema。向量 score 为 Float，不作为存储字段发送；向量 SELECT * 包含它，显式投影仅指定 score 时返回；标量查询不生成 score。

### 统计总数 (Count)

本适配器支持使用 `count` 语法查询集合或分区的记录总数。
结果是一行 `COUNT`（BIGINT），用 `rs.getLong("COUNT")` 获取；不是 SELECT COUNT(*)，也不接受向量范围条件。

```sql
-- 查询全表总数
count from table_name;

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

查询一致性使用连接参数 `consistencyLevel`，`consistency_level` 不是已实现的查询 Hint。上述三个 overwrite Hint 仅影响 SELECT，不影响 UPDATE/DELETE；IMPORT/LOAD/RELEASE 的 sync/timeout 见前文。

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

以下片段使用已经打开的 `conn` 和[入门程序](./usecase.mdx)建立的 `books_demo`，每个结果集都是一条 SQL 的返回，不是一个 SELECT 中多查询向量的结果组：

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
| COUNT FROM | COUNT（BIGINT），一行。 |
| SHOW DATABASES / TABLES / PARTITIONS | DATABASE / TABLE / PARTITION（VARCHAR）。 |
| SHOW PARTITION | PARTITION（VARCHAR），不是分区统计信息。 |
| SHOW TABLE | FIELD, TYPE, DIMENSION, PRIMARY, AUTO_ID, DESCRIPTION, NULLABLE, ELEMENT_TYPE, MAX_CAPACITY, MAX_LENGTH。 |
| SHOW CREATE TABLE | CREATE SCRIPT（VARCHAR）；用于查看 schema 描述，不承诺外部复杂 schema 的无损还原。 |
| SHOW INDEX / INDEXES | INDEX、FIELD、ID、PARAMS。 |
| SHOW USERS / ROLES | USER / ROLE（VARCHAR）。 |
| SHOW GRANTS | DATABASE、ROLE、OBJECT、OBJECT_NAME、PRIVILEGE。 |
| SHOW PROGRESS OF LOADING | PROGRESS（BIGINT）。 |
| SHOW PROGRESS OF INDEX | TOTAL、INDEXED（BIGINT）；未指定索引名时累计 SDK 返回的索引记录，不是完成百分比。 |
| INSERT / UPSERT | SDK 实际确认的 long 计数；请求主键时另提供 getGeneratedKeys 游标。 |
| UPDATE / DELETE | 更新/删除计数；大计数使用 Large 方法。 |
| DDL、授权、普通 IMPORT、LOAD/RELEASE、FLUSH | 更新计数 0；IMPORT RETURNING JOB_ID 则返回一行任务 ID。 |

### JDBC 使用边界

- ResultSet 为只读、前向访问，读取后及时关闭。FloatVector/Array 用 List/getArray；Binary/FP16/BF16 用 getBytes；Sparse 用 getObject 得到 SortedMap，不假定所有向量都是 float[]。
- RETURN_GENERATED_KEYS 支持 INSERT/UPSERT 的 Int64/VarChar、AUTO_ID 和显式主键；只返回 SDK ID，NO_GENERATED_KEYS 为空游标。默认不请求主键；请求大量主键需要 O(主键数) 内存。
- 不支持 JDBC addBatch/executeBatch、savepoint、可更新 ResultSet 或跨页事务。PreparedStatement 参数化与 JDBC batch 是不同能力。
- 本文列出的 SQL/SHOW 结果是当前可用入口，不承诺所有 DatabaseMetaData 方法满足 ORM/BI/迁移工具的关系数据库假设。

## 9. 新增类型、批量写入及融合搜索 {#extended}

### 类型绑定与 schema

完整 Java 绑定/主键示例见 [类型绑定与主键回传](./usecase.mdx#typed-values)。Binary 使用 byte[]/ByteBuffer/字节 List；FP16/BF16 数值输入经官方 Float16Utils 编码，byte[]/ByteBuffer 是小端原始编码；Sparse 使用非空 Map&lt;Number,Number>，索引为 [0,4294967295) 内的整数，权重有限。ByteBuffer 只消费 remaining，不改变其位置。FloatVector 的 byte[] 仍按数值解释。

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

BM25 必须 enable_analyzer=true、Sparse 输出；TextEmbedding 使用稠密浮点向量输出。字段 WITH 仅开放 enable_analyzer、enable_match（布尔）、analyzer_params（JSON 对象字符串）；函数 WITH 的标量值转为 SDK 字符串参数。TextEmbedding 需要对应 Milvus 版本、provider 配置、模型和网络/凭据，模型维度须与输出一致；上例不是已配置外部服务的承诺。provider 密钥建议配置在服务端，不把秘密写进 SQL/SHOW CREATE。当前 schema 函数为 BM25/TEXTEMBEDDING，不承诺所有未来 FunctionType。

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

每路仍只有一个查询向量，可以查询相同字段的不同向量，也可以不同字段/metric；向量列表嵌套不表示另一种 NQ 批量接口。公共 WHERE 复制到每路。参数按 SQL 顺序连续绑定：WHERE、逐路向量/LIMIT/WITH、最终 LIMIT/OFFSET/WITH。

每路 LIMIT 是候选量，最终 LIMIT/OFFSET 作用于服务端融合后的一个结果集。RRF 的 k 省略时沿用 SDK 默认；Weighted 必须提供与路数相同的 [0,1] 有限权重。reranker 必须显式选择；不识别的 rerank 参数报错。SDK 无 Hybrid Iterator，所以外层必须显式 LIMIT，fetchSize 不将融合变为分页；服务端窗口和候选路数限制仍适用，驱动不以固定总量兜底。普通 ORDER BY 只接受一个查询向量的规则不变。

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

复用 JDBC 认证/database，REST 地址须可达；同一连接的任务提交/观察固定到首次选择的端点。重连后请使用原任务所在端点。输入文件须已在 Milvus 可访问的对象存储；驱动不提供文件生成/上传，官方 BulkWriter 可用于准备。文件格式、资源限额和大规模吞吐须实测。

sync=true 默认等待；Hint timeout 是客户端毫秒等待期限，WITH timeout 是服务端任务期限字符串。超时、取消、状态 Failed 或后续 HTTP 失败保留已知 Job ID 和最后进度，不撤销、不重复提交任务；创建响应丢失时任务可能存在但没有 ID，需要列表核实。整个导入不承诺事务或精确一次。

注意：主动 Statement.cancel() 或公共 JDBC 超时可能优先返回标准取消/超时异常，而不携带适配器进度；不能把该异常当作“零行已写入”。需要可靠记录导入任务 ID 时使用 sync=false RETURNING JOB_ID，再独立查询。

版本、已接入能力与真环境验证范围见[发布与支持矩阵](./compatibility.md)。
