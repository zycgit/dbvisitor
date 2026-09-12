---
id: json-field
sidebar_position: 6.5
title: JSON 字段映射
description: 将 Java 对象、Map 或 List 映射到数据库的一个 JSON 字段。
---

# JSON 字段映射

JSON 字段映射用于将一个 Java 对象、`Map` 或 `List` 整体保存到数据库的一个字段，并在读取实体时还原。它不会把对象内部属性拆成多个列，也不会创建一对一或一对多关系。

## 选择配置方式

| Java 属性类型 | 推荐配置 |
| --- | --- |
| 可以修改源码的业务类 | 在业务类上使用 `@BindTypeHandler(JsonTypeHandler.class)` |
| `Map`、`List` 等无法添加类型注解的类型 | 在实体属性的 `@Column` 上配置 `typeHandler` |
| 只需要原始 JSON 文本 | 属性使用 `String`，不配置 JSON 处理器 |

业务类的存储约定通常会在多个属性或查询入口中复用，因此默认推荐使用类型级 `@BindTypeHandler`。属性级 `typeHandler` 用于无法在类型上添加注解的场景。

## 业务对象映射

下面将 `UserExtInfo` 整体保存到 `user_details.more_info` 字段。建表语句以 MySQL 为例：

```sql
CREATE TABLE user_details (
    id INTEGER PRIMARY KEY,
    more_info VARCHAR(2000)
);
```

在业务对象类型上声明 JSON 处理器。这个类不是数据库表实体，不需要 `@Table` 或 `@Column`：

```java
import net.hasor.dbvisitor.types.BindTypeHandler;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@BindTypeHandler(JsonTypeHandler.class)
public class UserExtInfo {
    private String city;
    private String theme;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
```

外层实体只需把 `moreInfo` 映射到 `more_info` 列，不再重复指定 `typeHandler`：

```java
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("user_details")
public class UserDetails {
    @Column(primary = true)
    private Integer id;

    @Column("more_info")
    private UserExtInfo moreInfo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserExtInfo getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(UserExtInfo moreInfo) {
        this.moreInfo = moreInfo;
    }
}
```

## 写入和读取

下面的 `lambda` 是已连接数据库的 [LambdaTemplate](../lambda/about.md)：

```java
UserExtInfo info = new UserExtInfo();
info.setCity("Hangzhou");
info.setTheme("dark");

UserDetails user = new UserDetails();
user.setId(1);
user.setMoreInfo(info);
lambda.insert(UserDetails.class).applyEntity(user).executeSumResult();

UserDetails loaded = lambda.query(UserDetails.class)
        .eq(UserDetails::getId, 1)
        .queryForObject();

String city = loaded.getMoreInfo().getCity();
```

写入后，`more_info` 保存类似 `{"city":"Hangzhou","theme":"dark"}` 的 JSON；查询后，`moreInfo` 被还原为 `UserExtInfo`，`city` 的值为 `"Hangzhou"`。JSON 属性的排列顺序不影响含义。

## Map 和 List 属性

`Map`、`List` 无法直接添加 `@BindTypeHandler`，应在实体属性上配置处理器。使用接口类型作为属性时，可以通过 `specialJavaType` 指定反序列化使用的具体类型：

```java
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@Column(value = "preferences",
        typeHandler = JsonTypeHandler.class,
        specialJavaType = LinkedHashMap.class)
private Map<String, Object> preferences;

@Column(value = "tags",
        typeHandler = JsonTypeHandler.class,
        specialJavaType = LinkedList.class)
private List<String> tags;
```

## 使用边界

- `@Table` 只用于外层实体；JSON 对象内部属性不会自动变成数据库列。
- 写入或更新 JSON 属性时处理整个字段值，不会自动合并某个内部属性的修改。
- 修改内存对象不会自动写回数据库，仍需显式执行更新。
- Lambda 条件不会自动变成 JSON 路径查询。需要按内部属性筛选时，应使用数据库支持的 JSON 查询语法或独立列。
- 文本列长度必须容纳序列化结果；`VARCHAR(2000)` 中的 `2000` 只是示例长度，不是触发映射的条件。

JSON 库、处理器实现和 SQL 参数配置见 [8.5 JSON 序列化处理器](../../types/json-serialization.md)。
