---
id: import
slug: /features/milvus/sql/import
sidebar_position: 5
title: IMPORT
---

:::info[说明]
使用官方 Import REST API，不对应 Java SDK 方法。

- 创建任务：`POST /v2/vectordb/jobs/import/create`
- 同步等待时查询进度：`POST /v2/vectordb/jobs/import/describe`
:::

## 语法

```text
IMPORT FROM [FILE] files INTO [TABLE] collection_name
    [PARTITION partition_name]
    [WITH (option = value [, ...])]
    [RETURNING JOB_ID];
```

files 为单个路径或文件分组列表，可用 ? 绑定。导入由 Milvus 服务端执行，不是 JDBC 客户端读取文件后逐行 INSERT。普通 IMPORT 返回更新计数 0；RETURNING JOB_ID 返回任务 ID 结果集。


## 数据导入 (Import)

```sql
-- 文件必须预先放入 Milvus 可访问的对象存储
IMPORT FROM 'prepared/1.parquet' INTO TABLE table_name;
IMPORT FROM 'prepared/1.json' INTO TABLE table_name PARTITION partition_name;

-- 等待导入任务完成，最多等待 60 秒
/*+ timeout=60000 */ IMPORT FROM 'prepared/1.json' INTO TABLE table_name;

-- 仅提交导入任务，不等待完成
/*+ sync=false */ IMPORT FROM 'prepared/1.json' INTO TABLE table_name;
```

路径是 Milvus 服务端配置存储中的已准备文件，不是 JDBC 客户端的本地文件；驱动不读取或上传文件。支持单路径和多文件分组 [[...],[...]]，也可用 ? 绑定 List&lt;List&lt;String>>。JSON/Parquet 每组一个文件，NumPy 每组相关列文件；格式和资源限制见[官方 Import 说明](https://milvus.io/docs/v2.6.x/import-data.md)。

导入使用官方 REST API，复用 JDBC 地址/database/认证，地址必须开放 REST。同一连接的任务创建、列表和进度查询使用同一个 JDBC 地址。普通 IMPORT 返回更新计数 0（非导入行数）；RETURNING JOB_ID 显式返回字符串任务 ID，SHOW IMPORT/SHOW IMPORTS 提供后续查询。

`IMPORT FROM` 默认同步等待 Milvus Import 任务完成；如需异步返回，可使用 `sync=false` Hint。`timeout` Hint 用于设置同步等待超时时间，单位为毫秒。



## 多文件 Import 与任务观察 {#import}

```sql
/*+ sync=false */ IMPORT FROM [['prepared/a.parquet'],['prepared/b.parquet']]
    INTO docs WITH (timeout='2h') RETURNING JOB_ID;
/*+ sync=false */ IMPORT FROM [['prepared/id.npy','prepared/dense.npy']]
    INTO docs RETURNING JOB_ID;
```

复用 JDBC 认证/database，REST 地址须可达；同一连接的任务提交和状态查询使用同一个 JDBC 地址。重连后应连接原任务所在的服务。输入文件须已在 Milvus 服务端配置的存储中：对象存储部署使用对象路径；本地存储的单机部署使用服务端可读取的绝对文件路径，不是 JDBC 客户端文件路径。驱动不提供文件生成/上传，官方 BulkWriter 可用于准备。

文件准备、存储可见性、访问权限和资源限额由部署负责。任务状态与失败原因来自 Milvus，不根据已提交文件数推算完成进度。

sync=true 默认等待；Hint timeout 是客户端毫秒等待期限，WITH timeout 是服务端任务期限字符串。超时、取消、状态 Failed 或后续 HTTP 失败保留已知 Job ID 和最后进度，不撤销、不重复提交任务；创建响应丢失时任务可能存在但没有 ID，需要列表核实。整个导入不承诺事务或精确一次。

注意：主动 Statement.cancel() 或公共 JDBC 超时可能优先返回标准取消/超时异常，而不携带适配器进度；不能把该异常当作“零行已写入”。需要可靠记录导入任务 ID 时使用 sync=false RETURNING JOB_ID，再独立查询。

版本与功能要求见[版本与支持范围](../compatibility.md)。

任务状态见 [SHOW IMPORT](../show/import.md)。
