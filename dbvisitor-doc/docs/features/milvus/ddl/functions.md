---
id: functions
slug: /features/milvus/sql/functions
sidebar_position: 16
title: FUNCTION 定义
---

:::info[说明]
对应 SDK 方法：`createCollection`。
:::

## 语法

```text
FUNCTION function_name USING { BM25 | TEXTEMBEDDING }
    (input_field [, ...]) INTO (output_field [, ...])
    [DESCRIPTION description]
    [WITH (option = value [, ...])];

```

FUNCTION 是 CREATE TABLE 或 ALTER TABLE 中的定义子句，不是可以独立执行的语句。输入、输出字段和允许的数量由函数类型决定。ANALYZE 是独立语句，调用分词器并返回 token 信息，不创建函数、写入实体或执行向量检索。

## BM25 / TextEmbedding schema 函数 {#functions}

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

函数定义支持 `INTO (...) DESCRIPTION '说明' WITH (...)`，`DESCRIPTION` 可使用 `?` 绑定非 NULL 字符串。说明写入 SDK 的独立 `description` 字段，并由 `SHOW CREATE TABLE` 保留；`WITH (description=...)` 仍是函数参数，不是说明。省略说明时使用空字符串。

说明字面量中，单引号写作 `''`，反斜杠写作 `\\`；`\n`、`\r`、`\t` 分别表示换行、回车和制表符。PreparedStatement 绑定值直接传入，不需要这些 SQL 转义；SHOW CREATE 会生成可重新解析的说明字面量。

<span id="alter-functions" />

<span id="analyze" />
