---
sidebar_position: 5
title: 执行 Mapper
description: 通过 dbVisitor ORM 工具的 DalSession 类执行 Mapper 文件中定义的 SQL。
---

# 执行 Mapper

## DalSession 方式

Mapper 文件在定义完成之后，需要加载它然后就可以通过 `DalSession` 接口调用它了。例如下面映射：

```xml title="配置文件：/mapper/mapper_1/TestUserMapper.xml"
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
                        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper>
    <select id="queryListByAge">
        select * from `test_user` where age = #{age}
    </select>
</mapper>
```

加载这个映射并执行它。

```java
DalRegistry dalRegistry = new DalRegistry();
dalRegistry.loadMapper("/mapper/mapper_1/TestUserMapper.xml");

DataSource dataSource = DsUtils.dsMySql();
DalSession dalSession = new DalSession(dataSource, dalRegistry);

Map<String, Object> ages = new HashMap<>();
ages.put("age", 26);
List<Object> result = dalSession.queryStatement("queryListByAge", ages);
```

对于 `insert`、`update`、`delete` 三组标签需要使用如下方式来执行，这样才能正确返回受影响行数
```java
int result = dalSession.executeStatement("insertUser", ages);
```

对于 `mapper` 标签已经配置了 `namespace` 的 Mapper SQL还需要指明 `namespace`。例如：

```xml title="配置文件：/mapper/mapper_1/TestUserMapper.xml" {4}
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
                        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="net.example.mapper">
    <select id="queryListByAge">
        select * from `test_user` where age = #{age}
    </select>
</mapper>
```

```java {1}
int result = dalSession.executeStatement("net.example.mapper.queryListByAge", ages);
```

## Mapper 接口化方式

一般应用开发的时候都会有一个 `DAO` 层，而 DAO 通常是以一个接口形态展现出来。在 MyBatisPlus 中会被成为 `Mapper` 接口。
dbVisitor 支持定义一个接口，然后将这个接口的方法映射到 Mapper 文件的具体执行命令上。例如：

```xml title='Mapper 配置文件'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="com.example.demo.File3Mapper">
    <select id="queryListByAge">
        select * from `test_user` where age = #{age}
    </select>
</mapper>
```

```java title="接口定义：com.example.demo.File3Mapper"
public interface File3Mapper {
    public List<Map<String, Object>> queryListByAge(@Param("age") int age);
}
```

最后加载并获取这个接口，就可以当成普通方法调用了。

```java {2,7,9}
DalRegistry dalRegistry = new DalRegistry();
dalRegistry.loadMapper("...");

DataSource dataSource = DsUtils.dsMySql();
DalSession dalSession = new DalSession(dataSource, dalRegistry);

File3Mapper mapper = dalSession.createMapper(File3Mapper.class);

List<Map<String, Object>> result = mapper.queryListByAge(26);
```

当你有大量 Mapper 文件需要和接口进行绑定的时 `dalRegistry` 的注册将会变得比较麻烦。
因此可以改造上面例子使用下面方式即可省略 `dalRegistry.loadMapper("...")`。

```java {1}
@RefMapper("/mapper/mapper_1/TestUserMapper3.xml")
public interface File3Mapper {
    public List<Map<String, Object>> queryListByAge(@Param("age") int age);
}
```

如果 `Mapper` 配置了 `resultMap` 或 `resultType` 那么返回值可以使用对应的类型。例如：

```xml title="/mapper/mapper_1/UserMapper.xml"
<select id="listByAge" resultType="com.example.demo.mapper.TestUser">
    select * from `test_user` where age = #{age};
</select>
```

直接返回一个对象列表

```java {1}
@RefMapper("/mapper/mapper_1/UserMapper.xml")
public interface UserMapper {
    public List<TestUser> listByAge(@Param("age") int age);
}
```

## 通用 Mapper 接口

通用 Mapper 接口类型为 `BaseMapper<T>`，它是对 `LambdaTemplate` 的进一步封装。
与大多数数据库访问框架一样 `BaseMapper` 存在的意义在于避免编写大量相同或类似的接口。

创建 BaseMapper 需要借助 `DalSession` 类型，并且要确定一个 DTO 对象。

```java {4}
DataSource dataSource = DsUtils.dsMySql();

DalSession dalSession = new DalSession(dataSource);
BaseMapper<TestUser> baseMapper = dalSession.createBaseMapper(TestUser.class);
```

:::tip
DTO 对象可以使用 **[对象映射](../objects/class-as-table.md)** 来修饰它，也可以参考 **[映射文件](./dal-mapper.md)** 配置一个实体。
:::

`BaseMapper` 接口提供 `insert`、`delete`、`update`、`query` 四个方法，进行的操作。

每个方法返回的操作接口具体使用请参考 **[CRUD章节](../crud/basic.md)**

