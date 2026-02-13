---
id: enum-handler
sidebar_position: 4
title: 8.4 枚举类型处理器
description: dbVisitor 枚举类型处理器的使用方式和自定义映射。
---

# 枚举类型处理器

dbVisitor 对于枚举类型会自动选择 `EnumTypeHandler` 进行处理，一般无需干预。

```java title='如对象映射中存在枚举字段' {2}
public class User {
    private UserType userType;

    // getters and setters omitted
}
```

```java title='查询和参数传递无需特别处理'
// 查询结果中枚举字段会自动转换
jdbc.queryForList("select * from users", User.class);

// 枚举参数也会自动处理
User userInfo = ...;
jdbc.queryForList("select * from users where user_type = #{userType}", userInfo);
```

:::info
默认情况下，`EnumTypeHandler` 使用枚举的 `name()` 值与数据库字段进行映射（字符串方式）。
:::

## 显式声明

```java title='在参数传递中显式指定'
jdbc.queryForList(
    "select * from users where user_type = #{userType, typeHandler=net.hasor.dbvisitor.types.handler.string.EnumTypeHandler}",
    userInfo
);
```

```java title='在对象映射中显式指定'
public class User {
    @Column(typeHandler = EnumTypeHandler.class)
    private UserType userType;

    // getters and setters omitted
}
```

- EnumTypeHandler 完整类名：`net.hasor.dbvisitor.types.handler.string.EnumTypeHandler`

## 将数值映射到枚举 {#ofvalue}

若数据库中存储的是数字，需要枚举实现 `EnumOfValue` 接口来完成数值与枚举的转换。

```java
public enum LicenseEnum implements EnumOfValue<LicenseEnum> {
    Private(0),
    AGPLv3(1),
    GPLv3(2);

    private final int type;

    LicenseEnum(int type) {
        this.type = type;
    }

    public int codeValue() {
        return this.type;
    }

    public LicenseEnum valueOfCode(int codeValue) {
        for (LicenseEnum item : LicenseEnum.values()) {
            if (item.codeValue() == codeValue) {
                return item;
            }
        }
        return null;
    }
}
```

- `EnumOfValue` 接口位于 `net.hasor.dbvisitor.types.handler.string` 包

## 将 Code 映射到枚举 {#ofcode}

当枚举的 `name()` 不能直接作为数据库映射值时，可以让枚举实现 `EnumOfCode` 接口来自定义字符串映射逻辑。

```java
public enum LicenseEnum implements EnumOfCode<LicenseEnum> {
    Private("private_license"),
    AGPLv3("agpl_v3"),
    GPLv3("gpl_v3");

    private final String code;

    LicenseEnum(String code) {
        this.code = code;
    }

    public String codeName() {
        return this.code;
    }

    public LicenseEnum valueOfCode(String codeValue) {
        for (LicenseEnum item : LicenseEnum.values()) {
            if (item.codeName().equalsIgnoreCase(codeValue)) {
                return item;
            }
        }
        return null;
    }
}
```

- `EnumOfCode` 接口位于 `net.hasor.dbvisitor.types.handler.string` 包
- 如果枚举已有默认的 `codeName()` 方法（返回 `name()`），可以只重写 `valueOfCode` 方法
