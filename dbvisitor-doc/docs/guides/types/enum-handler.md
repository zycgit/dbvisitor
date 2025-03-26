---
id: enum-handler
sidebar_position: 4
title: 8.4 枚举类型处理器
description: dbVisitor ORM 工具处理枚举类型处理器。
---

# 枚举类型处理器

dbVisitor 对于枚举类型通常会自动选择 `EnumTypeHandler` 进行处理，一般情况下无需干预。

```java title='如对象映射中存在 userType 枚举' {2}
public class User {
    private UserType userType;

    // getters and setters omitted
}
```

```java title='程序无需特别处理'
// 查询结果
jdbc.queryForList("select * from users", User.class);

// 查询参数
User userInfo= ...
jdbc.queryForList("select * from users where user_type = #{userType}", userInfo);
```

## 显示声明

```java title='在参数传递中'
// 查询参数
User userInfo= ...
jdbc.queryForList("select * from users where user_type = #{userType, typeHandler=net.<省略>.EnumTypeHandler}", userInfo);
```
- EnumTypeHandler 完整名称为：net.hasor.dbvisitor.types.handler.string.EnumTypeHandler

```java title='在对象映射中'
public class User {
    @Column(typeHandler = net.hasor.dbvisitor.types.handler.string.EnumTypeHandler)
    private UserType userType;

    // getters and setters omitted
}
```

## 将数值映射到枚举 {#ofvalue}

若想将数据库中的数字值类型应为 Java 的枚举时候，枚举需要实现 `net.hasor.dbvisitor.types.handler.string.EnumOfValue` 接口以完成数据的转换。

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

## 将Code映射到枚举 {#ofcode}

通常情况下枚举类型的 name 属性将会作为最终数据读写数据库，但在一些特殊环境中若是枚举的 name 并不能直接做映射此时通常需要 [自定义类型处理器](./custom-handler) 来处理。

dbVisitor 允许在不动用类型处理器的情况下通过让枚举实现 `net.hasor.dbvisitor.types.handler.string.EnumOfCode` 接口来负责枚举值的映射，已完成此类需求。

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
