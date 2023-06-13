---
sidebar_position: 2
title: 枚举类型处理器
description: dbVisitor ORM 工具处理枚举类型映射。
---

# 枚举类型处理器

## 基于枚举的 name

枚举类型的映射无需指定特殊的 `TypeHandler` 在 dbVisitor 中它会自动被处理，具体映射规则为：
- 数据库中字段类型必须为 `字符串`，而字段值内容是映射到枚举的枚举的 `name` 上

```java {3}
@Table(mapUnderscoreToCamelCase = true)
public class TestUser {
    private UserType userType;

    // getters and setters omitted
}
```

## 将数值映射到枚举

将数值类型映射成为枚举值，需要枚举类型实现 `net.hasor.dbvisitor.types.EnumOfValue` 接口。

```java
public enum LicenseEnum implements EnumOfValue<LicenseEnum> {
    Private(0),
    AGPLv3(1),
    GPLv3(2),;

    private final int type;

    LicenseEnum(int type) {
        this.type = type;
    }

    public int codeValue() {
        return this.type;
    }

    public LicenseEnum valueOfCode(int codeValue) {
        for (LicenseEnum item : LicenseEnum.values()) {
            if (item.getType() == codeValue) {
                return item;
            }
        }
        return null;
    }
}
```

## 将Code映射到枚举

`Code` 是一个字符串值，区别于枚举的 `name` 属性，它可以由用户自定义规则。从而不再担心枚举元素的变更。

将 Code 类型映射成为枚举值，需要枚举类型实现 `net.hasor.dbvisitor.types.EnumOfCode` 接口。

```java
public enum LicenseEnum implements EnumOfCode<LicenseEnum> {
    Private("Private"),
    AGPLv3("AGPLv3"),
    GPLv3("GPLv3"),;

    private final String type;

    LicenseEnum(String type) {
        this.type = type;
    }

    public String codeName() {
        return this.type;
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