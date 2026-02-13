---
id: keytype
sidebar_position: 8
hide_table_of_contents: true
title: 主键生成器
description: 在 dbVisitor 中，主键生成器是一个重要的部件，它决定了如何为数据库表中的记录生成唯一的主键值。
---

# 主键生成器

在 dbVisitor 中，主键生成器是一个重要的部件，它决定了如何为数据库表中的记录生成唯一的主键值。以下是关于主键生成策略的详细说明和配置方法。

## 基于注解 {#anno}

```java title='例1：使用 Auto 映射数据库自增主键'
@Table("admin_users")
public class AdminUsers {
    @Column(primary = true, keyType = KeyType.Auto)
    private Integer id;

    ...
}
```

```java title='例2：使用 id_seq 序列填充主键'
@Table("admin_users")
public class AdminUsers {
    @KeySeq("id_seq")
    @Column(primary = true, keyType = KeyType.Sequence)
    private Integer id;

    ...
}
```

```java title='例3：使用自定义生成器填充主键'
@Table("admin_users")
public class AdminUsers {
    @KeyHolder(MyKeySeqHolder.class)
    @Column(primary = true, keyType = KeyType.Holder)
    private Integer id;

    ...
}
```

:::info[提示]
- primary 和 keyType 两个属性没有强关联，您也可以为非主键列配备生成器。
:::

### keyType 属性可选的配置

| 属性名      | 描述                                                                                                                 |
|----------|--------------------------------------------------------------------------------------------------------------------|
| None     | 不做任何事。                                                                                                             |
| Auto     | 通过 `java.sql.Statement.RETURN_GENERATED_KEYS` 选项接收来自数据库的自增返回数据。                                                    |
| UUID32   | 在插入数据前使用 32 位字符串的 UUID 填充数据，如：`4d68040901d24b70bd10c1c8119001e2`。                                              |
| UUID36   | 在插入数据前使用 36 位字符串的 UUID 填充数据，如：`4d680409-01d2-4b70-bd10-c1c8119001e2`。                                              |
| Sequence | 先从数据库序列中获取最新值，填充到属性后再执行数据库插入。兼容性请参考 [数据库支持性](../../api/differences/about#dialect)。<br/>- 使用该选项需要同时使用 @KeySeq 注解标识序列的名字。 |
| Holder   | 自定义数据生成逻辑，需要实现 `GeneratedKeyHandlerFactory` 接口，并通过 @KeyHolder 注解将其一同声明。                                            |

## 基于 Mapper File {#xml}

```xml title='例1：使用 Auto 映射数据库自增主键'
<entity table="admin_users" type="net.example.dto.AdminUsers">
    <id column="id" property="id" keyType="auto" />
    ...
</entity>
```

```xml title='例2：使用 id_seq 序列填充主键'
<entity table="admin_users" type="net.example.dto.AdminUsers">
    <id column="id" property="id" keyType="Sequence::id_seq" />
    ...
</entity>
```

```xml title='例3：使用自定义生成器填充主键'
<entity table="admin_users" type="net.example.dto.AdminUsers">
    <id column="id" property="id" keyType="net.example.dto.handler.MyKeySeqHolder" />
    ...
</entity>
```

:::info[提示]
&lt;id&gt; 标签 和 &lt;mapping&gt; 标签，都可以配置 keyType 属性。
:::

### keyType 属性可选的配置

| 属性名            | 描述                                                                                      |
|----------------|-----------------------------------------------------------------------------------------|
| (空)            | 不做任何事。                                                                                  |
| auto           | 通过 `java.sql.Statement.RETURN_GENERATED_KEYS` 选项接收来自数据库的自增返回数据。                         |
| uuid32         | 在插入数据前使用 32 位字符串的 UUID 填充数据，如：`4d68040901d24b70bd10c1c8119001e2`。                   |
| uuid36         | 在插入数据前使用 36 位字符串的 UUID 填充数据，如：`4d680409-01d2-4b70-bd10-c1c8119001e2`。                   |
| Sequence::xxxx | 先从名称为 xxxx 的数据库序列中获取最新值，填充到属性后再执行数据库插入。兼容性请参考 [数据库支持性](../../api/differences/about#dialect)。 |
| (类名)           | 自定义数据生成逻辑，填写实现了 `GeneratedKeyHandlerFactory` 接口的完整类名。                                   |

## 自定义生成器

```java title='例1，在数据插入之前生成主键数据'
public class MyKeySeqHolder implements GeneratedKeyHandlerFactory {
    @Override
    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) {
        return new GeneratedKeyHandler() {
            @Override
            public boolean onBefore() {
                return true;
            }

            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) {
                Object keyValue = ...
                
                mapping.getHandler().set(entity, keyValue); // 将生成的数据更新到 Bean 的属性中
                return keyValue;//返回生成的数据
            }
        };
    }
}
```

```java title='例2，在数据插入之后获取主键'
public class MyKeySeqHolder implements GeneratedKeyHandlerFactory {
    @Override
    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) {
        return new GeneratedKeyHandler() {
            @Override
            public boolean onAfter() {
                return true;
            }

            @Override
            public Object afterApply(ResultSet generatedKeys, Object entity, int argsIndex, ColumnMapping mapping) {
                Object keyValue = ...
                
                mapping.getHandler().set(entity, keyValue); // 将生成的数据更新到 Bean 的属性中
                return keyValue;//返回生成的数据
            }
        };
    }
}
```
