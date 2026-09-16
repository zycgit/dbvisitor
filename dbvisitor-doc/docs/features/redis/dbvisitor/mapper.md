---
id: mapper
slug: /features/redis/mapper
sidebar_position: 20
title: Mapper API
---

## 方法注解 {#annotations}

`@Query`、`@Insert`、`@Update`、`@Delete` 和 `@Execute` 可以直接写 Redis 命令，不要求写成 SQL。

```java
@SimpleMapper
public interface ValueMapper {
    @Insert("SET #{key} #{value}")
    int put(@Param("key") String key, @Param("value") String value);

    @Query("GET #{key}")
    String get(@Param("key") String key);
}
```

无效命令会抛出异常；GET 不存在的键返回 null，SET 同名键覆盖旧值，二者不是“表不存在”或“主键冲突”错误。用法见[查询操作](query.mdx)与[数据写入](write.mdx)。

## Mapper 读写 {#operations}

Redis 没有 BaseMapper 的自动 CRUD 方言，不能根据实体调用 `insert`、`selectById`、`update` 等方法生成 Redis 命令。包括样本查询、Map 参数写入在内，都应改用上面的方法注解或文件 Mapper。

这不影响把 Redis 返回的数据映射为对象，见[商品缓存](../scenarios/product-cache.md)。

## 主键策略 {#keys}

键名由应用提供；业务编号可在写入前用 `selectKey` 执行 INCR 取得，也可接收 Lua 脚本返回的编号。Redis 没有 JDBC 生成键和数据库复合主键，不能直接套用自增列配置。两种编号用法见[键与编号](generated-keys.mdx)。

## 分页查询 {#pagination}

没有自动 SQL 分页：不能让 Page 参数自动把 Redis 命令改写为 LIMIT / OFFSET。使用 SCAN 游标遍历键，或使用 LRANGE / ZRANGE 读取范围；具体用法见[分页查询](pagination.mdx)。

## 执行选项 {#options}

使用默认或 `FORWARD_ONLY` 结果集，不支持 `SCROLL_INSENSITIVE`、`SCROLL_SENSITIVE`。需要重复读取时重新执行命令，或先接收到 List。

结果处理接口与执行选项是不同能力，见[结果接收](result-handling.md)。

## 调用构造器 {#builder}

可以获取构造器对象，但 Redis 没有执行构造器 CRUD 的方言。不要通过 `mapper.lambda()` 或 `session.lambda()` 自动生成读写命令；改用方法注解或 `session.jdbc()` 执行原生命令。

## 引用文件 Mapper {#file-mapper}

可以通过 `@RefMapper`，或按 statement ID 执行文件中的 Redis 命令；查询和写入都可用。即使不能使用 BaseMapper 自动 CRUD，它的文件语句调用也不受此限制。

文件中写 Redis 命令，不写 SQL。示例见[数据写入](write.mdx#base-mapper)。

## Session 管理 {#session}

Session 可以创建原生命令 Mapper、加载文件 Mapper，并通过 `jdbc()` 访问 Redis。它们可以读取彼此写入的键；无需 BaseMapper 自动 CRUD 支持。

不支持构造器或自动 CRUD，不等于 Session 不可用。这里的共享访问也不表示 Redis 写入具备 JDBC 事务回滚能力，事务边界见[事务支持](transactions.md)。
