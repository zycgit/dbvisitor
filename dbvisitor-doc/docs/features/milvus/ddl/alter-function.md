---
id: alter-function
sidebar_position: 17
title: ALTER TABLE … FUNCTION
---

:::info[说明]
对应 SDK 方法：`addCollectionFunction`、`alterCollectionFunction`、`dropCollectionFunction`。
:::

### 在线函数管理 {#alter-functions}

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

**版本边界：Milvus 2.6.2 对三个在线 API 均返回 UNIMPLEMENTED。** 网络错误或其他执行失败不构成回滚保证。参见[官方新增函数 API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/addCollectionFunction.md)及[修改函数 API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/alterCollectionFunction.md)。
