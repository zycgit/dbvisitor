---
sidebar_position: 1
title: 映射文件
description: dbVisitor ORM 工具使用 Mapper 文件的好处是便于维护和管理 SQL。
---

# 映射文件

使用 Mapper 文件的好处是便于维护和管理 SQL，这在团队协作时 review sql 代码比起在程序中用代码来拼接要好。

## 文档结构

下面是 dbVisitor Mapper 文件基本结构：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
                        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="...">
    ...
</mapper>
```

`mapper` 标签属性

| 属性名                        | 描述                                                                                          |
|----------------------------|---------------------------------------------------------------------------------------------|
| `namespace`                | 可选，通常是配置一个接口类名，这个接口下的每个方法会对应到 mapper 文件中一个具体的 sql 操作上。                                      |
| `caseInsensitive`          | 可选，在处理映射列名和属性名时是否对大小写不敏感，默认是 `true` 不敏感。对于某些数据库查询结果始终返回大写，利用这个功能可以方便的映射到属性上。                |
| `mapUnderscoreToCamelCase` | 可选，用于决定属性名在映射到列名时，是否按照驼峰命名法转换为下划线命名法，例如：属性名 `createTime` 被转换为 `create_time`。默认是 `false` 不转换 |
| `autoMapping`              | 可选，用于决定是否进行 **自动映射**。默认是 `true` 自动映射。                                                       |
| `useDelimited`             | 可选(v5.3.4+)，用于决定在生成 SQL 语句时，表名/列名 是否强制使用限定符。默认：false 不使用。                                   |

在 `mapper` 根元素下可以使用的顶层 Xml 元素有如下几个：

- **entity** 用于描述一个数据库表和类型的映射，每个类型只能映射一次。
- **resultMap** 用于描述如何从查询结果集中加载数据。
- **sql** 用于定义一小段可以复用的 SQL 片段。
- **insert** 映射 INSERT 语句。
- **update** 映射 UPDATE 语句。
- **delete** 映射 DELETE 语句。
- **select** 映射 SELECT 语句。

## select 标签

`select` 标签是 dbVisitor 中最常用的元素之一。对于简单的情况 `select` 元素非常简单。例如:

```xml
<select id="queryListByAge">
    select * from `test_user` where age = #{age}
</select>
```

`select` 标签属性

| 属性名              | 描述                                                                                                                   |
|------------------|----------------------------------------------------------------------------------------------------------------------|
| `id`             | 必选，用于标识查询命令                                                                                                          |
| `statementType`  | 可选，`STATEMENT`、`PREPARED`、`CALLABLE` 对应了 `Statement`, `PreparedStatement` 或 `CallableStatement` 中的一种。默认值为 `PREPARED` |
| `timeout`        | 可选，当配置的值大于 `0` 时会被设置到 `Statement.setQueryTimeout`，用于表示查询最长等待的超时时间。默认值是 `-1`                                          |
| `resultMap`      | 可选，对于映射配置的引用。select 标签可以使用 `resultMap` 和 `resultType` 其中的一种，不应该同时使用它们。如果没有配置将会按照 `map` 来处理                           |
| `resultType`     | 可选，将返回的预期类型的完全限定类名或别名。注意，在集合的情况下，这应该是集合包含的类型，而不是集合本身的类型，不应该同时使用`resultMap` 和 `resultType`                            |
| `fetchSize`      | 可选，当配置的值大于 `0` 时会被设置到 `Statement.setQueryTimeout`，用于表示查询最长等待的超时时间。默认值是 `-1`                                          |
| `resultSetType`  | 可选，`FORWARD_ONLY`、`SCROLL_INSENSITIVE`、`SCROLL_SENSITIVE` 和 `DEFAULT` 其中的一种。默认值是 `DEFAULT` 相当于未设置                    |
| `multipleResult` | 可选，`FIRST`、`LAST`、`ALL` 用于处理多结果集的情况。它们对应的行为是 `保留第一个结果集`、`保留最后一个结果集`、`全部保留`。默认配置是 `LAST`                              |

## update 和 delete 标签

`update` 和 `delete` 标签用于更新和删除数据

```xml
<update id="updateAge">
    update `test_user` set age = #{age} where id = #{id}
</update>

<delete id="deleteById">
    delete from `test_user` where id = #{id}
</delete>
```

`update` 和 `delete` 标签属性

| 属性名             | 描述                                                                                                                   |
|-----------------|----------------------------------------------------------------------------------------------------------------------|
| `id`            | 必选，用于标识查询命令                                                                                                          |
| `statementType` | 可选，`STATEMENT`、`PREPARED`、`CALLABLE` 对应了 `Statement`, `PreparedStatement` 或 `CallableStatement` 中的一种。默认值为 `PREPARED` |
| `timeout`       | 可选，当配置的值大于 `0` 时会被设置到 `Statement.setQueryTimeout`，用于表示查询最长等待的超时时间。默认值是 `-1`                                          |

## insert 标签

`insert` 标签，用于配置 insert 语句，例如:

```xml
<insert id="insertUser">
    insert into `test_user` (
        `id`, `name`, `age`, `create_time`
    ) values (
        #{id}, #{name}, #{age}, #{createTime}
    )
</insert>
```

`insert` 标签属性

| 属性名                | 描述                                                                                                                   |
|--------------------|----------------------------------------------------------------------------------------------------------------------|
| `id`               | 必选，用于标识查询命令                                                                                                          |
| `statementType`    | 可选，`STATEMENT`、`PREPARED`、`CALLABLE` 对应了 `Statement`, `PreparedStatement` 或 `CallableStatement` 中的一种。默认值为 `PREPARED` |
| `timeout`          | 可选，当配置的值大于 `0` 时会被设置到 `Statement.setQueryTimeout`，用于表示查询最长等待的超时时间。默认值是 `-1`                                          |
| `useGeneratedKeys` | 可选(v5.2.0+)，是否使用自增属性，如果同时配置了 `SelectKey` 标签，那么该配置将会失效。                                                               |
| `keyProperty`      | 可选(v5.2.0+)，回填自增属性值的 Bean 属性名，如果同时配置了 `SelectKey` 标签，那么该配置将会失效。                                                      |
| `parameterType`    | 可选(v5.2.0+)，参数类型，可以用于回填自增属性。如果同时配置了 `SelectKey` 标签 或者 `useGeneratedKeys` 属性，那么该配置将会失效。                               |

## selectKey

对于不支持自增列的数据库，dbVisitor 可以使用 `selectKey` 标签来通过 SQL 方式生成它，比较常见用处是使用数据库的 `sequence`。
例如：下面 Mapper 配置，在执行 `insert` 之前会先使用数据库函数生成一个随机数作为主键。

```xml
<insert id="insertUser">
    <selectKey keyProperty="id" resultType="int" order="BEFORE">
        SELECT CONCAT('1', CEILING(RAND() * 1000 + 1000))
    </selectKey>
    insert into `test_user` (
        `id`, `name`, `age`, `create_time`
    ) values (
        #{id}, #{name}, #{age}, #{createTime}
    )
</insert>
```

`selectKey` 标签有如下属性。

| 属性名             | 描述                                                                                                                                         |
|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `keyProperty`   | 必选，用于将 `selectKey` 执行后的返回值写入到目标的属性名，如果要回写多个属性则，可以使用逗号分割属性名列表。                                                                              |
| `keyColumn`     | 可选，返回结果集中与属性匹配的列名，如果需要选择多个列，可以使用逗号分割属性名列表。列名和属性名的顺序一致。                                                                                     |
| `order`         | 可选，可以设置为 `BEFORE` 或 `AFTER`。如果设置为 `BEFORE` 它会在执行 insert 之前先执行 `selectKey`；如果设置为 `AFTER` 则会在运行完 insert 之后在执行 `selectKey`。后运行一般用于获取自增主键的返回值。 |
| `handler`       | 可选，用于自定义 `selectKey` 执行逻辑。配置一个全类名，该类要求实现了 `net.hasor.dbvisitor.dal.execute.KeySequenceHolderFactory` 接口，并且有一个无参的构造方法。                      |
| `statementType` | 可选，`STATEMENT`、`PREPARED`、`CALLABLE` 对应了 `Statement`, `PreparedStatement` 或 `CallableStatement` 中的一种。默认值为 `PREPARED`                       |
| `timeout`       | 可选，当配置的值大于 `0` 时会被设置到 `Statement.setQueryTimeout`，用于表示查询最长等待的超时时间。默认值是 `-1`                                                                |
| `resultType`    | 可选，将返回的预期类型的完全限定类名或别名。注意，在集合的情况下，这应该是集合包含的类型，而不是集合本身的类型，不应该同时使用`resultMap` 和 `resultType`。                                                 |
| `fetchSize`     | 可选，当配置的值大于 `0` 时会被设置到 `Statement.setQueryTimeout`，用于表示查询最长等待的超时时间。默认值是 `-1`                                                                |
| `resultSetType` | 可选，`FORWARD_ONLY`、`SCROLL_INSENSITIVE`、`SCROLL_SENSITIVE` 和 `DEFAULT` 其中的一种。默认值是 `DEFAULT` 相当于未设置。                                         |

## sql 代码片段

此标签可用于定义一段在其它语句中被包含的重用代码片段。例如定义列名。

```xml
<sql id="testuser_columns">
    name,age,create_time
</sql>

<insert id="insertUser">
    insert into `test_user` (
        <include refid="testuser_columns"/>
    ) values (
        #{name}, #{age}, now()
    )
</insert>
```

## 结果集映射

并不是每一个 `select` 都必须要求配置 `resultMap` 默认情况下会使用 Map 来承装返回的数据。

```xml
<select id="queryListByAge">
    select * from `test_user` where age = #{age}
</select>
```

但通常 Map 并不是一个很好的模型设计，应该使用一些有意义的 pojo 充当数据对象。dbVisitor 支持将一个普通的 pojo 映射到一个结果集上。例如下面这个 Bean：

```java title="class com.example.demo.mapper.TestUser"
public class TestUser {
    private Integer id;
    private String  name;
    private Integer age;
    private Date    createTime;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
```

可以使用 `resultType` 属性将查询结果映射到这个 Bean 上。

```xml
<select id="queryById" resultType="com.example.demo.mapper.TestUser">
    select 
        id, name, age, create_time
    from
        test_user
    where
        id = #{id}
</select>
```

使用这种方式，dbVisitor 会在加载配置文件的时候自动创建一个 `resultMap`，根据名称将列自动映射到 pojo 的属性上。如果列名不完全匹配，可以在列名上使用 `as` 子句来匹配映射。例如:

```xml
<select id="queryById" resultType="com.example.demo.mapper.TestUser">
    select 
        id, name, age, create_time as createTime
    from
        test_user
    where
        id = #{id}
</select>
```

直接使用 `resultMap` 的好处是可以更加精细化的控制每一个属性映射，以刚才的映射为例。在不通过 `as` 改变列名的情况下映射这个 pojo：

```xml
<resultMap id="testuser_resultMap" type="com.example.demo.mapper.TestUser">
    <result column="id" property="id"/>
    <result column="name" property="name"/>
    <result column="age" property="age"/>
    <result column="create_time" property="createTime"/>
</resultMap>

<select id="queryById" resultMap="testuser_resultMap">
    select
        id, name, age, create_time
    from
        test_user
    where
        id = #{id}
</select>
```

`resultMap` 标签有如下属性：

| 属性名                        | 描述                                                                                          |
|----------------------------|---------------------------------------------------------------------------------------------|
| `type`                     | 必选，类型全名，用于决定映射到的具体类型。                                                                       |
| `id`                       | 可选，如果为空那么将会以 `type` 属性为替代。主要是用于标识 resultMap。                                                |
| `caseInsensitive`          | 可选，在处理映射列名和属性名时是否对大小写不敏感，默认是 `true` 不敏感。对于某些数据库查询结果始终返回大写，利用这个功能可以方便的映射到属性上。                |
| `mapUnderscoreToCamelCase` | 可选，用于决定属性名在映射到列名时，是否按照驼峰命名法转换为下划线命名法，例如：属性名 `createTime` 被转换为 `create_time`。默认是 `false` 不转换 |
| `autoMapping`              | 可选，用于决定是否进行 **自动映射**。默认是 `true` 自动映射。                                                       |

`result` 标签有如下属性：

| 属性名           | 描述                                                                                 |
|---------------|------------------------------------------------------------------------------------|
| `column`      | 必选，查询结果的列名。                                                                        |
| `property`    | 必选，pojo 的属性名。                                                                      |
| `javaType`    | 可选，通常 dbVisitor 会识别到具体类型，但如果 pojo 的属性是一个抽象类或者接口，则可以配置 `javaType` 来指定具体的实现类。        |
| `jdbcType`    | 可选，对应的 JDBC 类型。dbVisitor 将会遵循 **[Java 和 JDBC 类型关系](../types/java-jdbc.md)** 进行映射   |
| `typeHandler` | 可选，通常 dbVisitor 会根据 **[类型映射](../types/type-handlers.md)** 自动寻找列的读写器。该属性允许自定义属性读写器。 |

## 实体映射

`5.2.0 版开始提供`

实体具有 `resultMap` 的所有功能，同时可以通过表名来映射到一个具体的 表 或 视图。

一个实体会根据实体类型注册到 `DalRegistry` 注册器中，在注册器中实体是全局有效的。

```xml
<entity id="testuser_resultMap" table="test_user" type="com.example.demo.mapper.TestUser">
    <id column="id" property="id"/>
    <mapping column="name" property="name"/>
    <mapping column="age" property="age"/>
    <mapping column="create_time" property="createTime"/>
</entity>

<select id="queryById" resultMap="testuser_resultMap">
    select
        id, name, age, create_time
    from
        test_user
    where
        id = #{id}
</select>
```

`entity` 标签有如下属性：

| 属性名                        | 描述                                                                                                      |
|----------------------------|---------------------------------------------------------------------------------------------------------|
| `type`                     | 必选，类型全名，用于决定映射到的具体类型。                                                                                   |
| `table`                    | 可选，映射到的数据库 table 名字（如果省略 table 配置则实体类必须标有 @Table 注解）                                                    |
| `id`                       | 可选，如果为空那么将会以 `type` 属性为替代。主要是用于标识 resultMap。                                                            |
| `catalog`                  | 可选，一个补充选项，通常在使用通用 `Mapper` 时候用到。它可以决定 映射到的数据库名字。                                                        |
| `schema`                   | 可选，一个补充选项，通常在使用通用 `Mapper` 时候用到。它可以决定 映射到的数据库 schema 名字。                                                |
| `caseInsensitive`          | 可选，在处理映射列名和属性名时是否对大小写不敏感，默认是 `true` 不敏感。对于某些数据库查询结果始终返回大写，利用这个功能可以方便的映射到属性上。                            |
| `mapUnderscoreToCamelCase` | 可选，用于决定属性名在映射到列名时，是否按照驼峰命名法转换为下划线命名法，例如：属性名 `createTime` 被转换为 `create_time`。默认是 `false` 不转换             |
| `autoMapping`              | 可选，用于决定是否进行 **自动映射**。默认是 `true` 自动映射。                                                                   |
| `useDelimited`             | 可选(v5.3.3+)，用于决定在生成 SQL 语句时，表名/列名 是否强制使用限定符。默认：false 不使用。                                               |
| `character-set`            | 可选(v5.3.3+)，用于在生成建表语句时候使用的默认字符集。（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置）                               |
| `collation`                | 可选(v5.3.3+)，用于在生成建表语句时候使用的排序规则。（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置）                                |
| `comment`                  | 可选(v5.3.3+)，用于在生成建表语句时候使用的表备注。（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置）                                 |
| `other`                    | 可选(v5.3.3+)，用于在生成建表语句时候使用的其它建表参数。（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置）                              |
| `ddlAuto`                  | 可选(v5.3.3+)，自动建表方式，可选值范围：`none、create、add、update、create-drop`，（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置） |

`id` 标签 和 `mapping` 标签 有如下属性：

| 属性名                  | 描述                                                                                     |
|----------------------|----------------------------------------------------------------------------------------|
| `column`             | 必选，查询结果的列名。                                                                            |
| `property`           | 必选，pojo 的属性名。                                                                          |
| `javaType`           | 可选，通常 dbVisitor 会识别到具体类型，但如果 pojo 的属性是一个抽象类或者接口，则可以配置 `javaType` 来指定具体的实现类。            |
| `jdbcType`           | 可选，对应的 JDBC 类型。dbVisitor 将会遵循 **[Java 和 JDBC 类型关系](../types/java-jdbc.md)** 进行映射       |
| `typeHandler`        | 可选，通常 dbVisitor 会根据 **[类型映射](../types/type-handlers.md)** 自动寻找列的读写器。该属性允许自定义属性读写器。     |
| `keyType`            | 可选，key 生成策略，当列的属性为 null 的时。采用一种生成算法来生成 key 值。通常做用于 自增。                                 |
| `insert`             | 可选(v5.2.0+)，在使用 `LambdaTemplate` 方式时，是否参与新增。                                           |
| `update`             | 可选(v5.2.0+)，在使用 `LambdaTemplate` 方式时，是否参与更新。                                           |
| `selectTemplate`     | 可选(v5.2.0+)，在使用 `LambdaTemplate` 方式时，用作 select 语句时 column name 的写法，默认是空                |
| `insertTemplate`     | 可选(v5.2.0+)，在使用 `LambdaTemplate` 方式时，用作 insert 语句时 value 的参数写法，默认是 ?                   |
| `setColTemplate`     | 可选(v5.2.0+)，在使用 `LambdaTemplate` 方式时，用作 update 的 set 语句时 column name 的写法，默认是空          |
| `setValueTemplate`   | 可选(v5.2.0+)，在使用 `LambdaTemplate` 方式时，用作 update set 语句时 value 的参数写法，默认是 ?               |
| `whereColTemplate`   | 可选(v5.2.0+)，在使用 `LambdaTemplate` 方式时，用作 update/delete 的 where 语句时 column name 的写法，默认是空 |
| `whereValueTemplate` | 可选(v5.2.0+)，在使用 `LambdaTemplate` 方式时，用作 update/delete 的 where 语句时 value 的参数写法，默认是 ?    |
| `sqlType`            | 可选(v5.3.3+)，在自动建表中使用的列的数据库类型（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置）                  |
| `length`             | 可选(v5.3.3+)，在自动建表中使用的列的数据长度（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置）                   |
| `precision`          | 可选(v5.3.3+)，在自动建表中使用的列的数值精度（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置）                   |
| `scale`              | 可选(v5.3.3+)，在自动建表中使用的列的数值刻度（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置）                   |
| `character-set`      | 可选(v5.3.3+)，在自动建表中使用的列的字符集（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置）                    |
| `collation`          | 可选(v5.3.3+)，在自动建表中使用的列的字符排序规则（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置）                 |
| `default`            | 可选(v5.3.3+)，在自动建表中使用的列的默认值（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置）                    |
| `comment`            | 可选(v5.3.3+)，在自动建表中使用的列的备注（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置）                     |
| `other`              | 可选(v5.3.3+)，在自动建表中使用的列的其它属性信息（目前还不支持自动建表，仅能用于 TableDescription 接口获取配置）                 |

**keyType** 取值范围
- `auto`，不会主动生成自增，但会接收来自数据库的自增
- `uuid32`，使用 32 位字符串的 UUID 填充数据
- `uuid36`，使用 36 位字符串的 UUID 填充数据
- `KeySeq::xxxx`，使用名称为 xxxx 数据库的 sequence。
- `类名`，类需要实现 `net.hasor.dbvisitor.keyholder.KeySeqHolderFactory` 接口来自定义生成策略

## 多结果映射

例如，一个 `select` 配置了两个 查询语句。或者调用的存储过程中执行了两条 查询 SQL。`resultType` 中以逗号作为分割将两个结果分别映射到两个类型上。

:::tip
使用 `resultMap` 同样也可以通过逗号作为分割，映射多个结果。
:::

```xml
<select id="multipleListByAge" multipleResult="ALL"
        resultType="com.example.demo.mapper.TestUserPojo,com.example.demo.mapper.TestUser">
    select id, name, age, create_time as createTime from `test_user` where age = #{age};
    select * from `test_user`;
</select>
```

## 自动映射 

默认 `autoMapping` 是 true，开启自动映射后。无需为每一个属性都添加 result、mapping 标签，它会根据 type 类型自定进行推断。

类型映射遵循 **[对象映射映射](../../category/对象映射)**

```xml title='配置方式(默认 autoMapping 属性为 true 可不配置)'
<resultMap id="xxx" type="xxx.xxx" autoMapping="true"/>
<entity id="xxx" table="..." type="xxx.xxx" autoMapping="true"/>
```

:::caution
注意两点：
- `resultMap`和 `entity` 标签中如果含有 id/result/mapping 其中任何一种标签，那么会采用它们来配置属性。
- 若属性标有 `@Column` 注解，无论 `autoMapping` 属性是任何值都会将其自动映射。
:::

驼峰命名法

通常数据库列的命名使用大写字母和下划线，这与 java 通常遵循驼峰命名约定有一定的差异。
若想使它们之间自定映射需要设置 `mapUnderscoreToCamelCase` 为 `true`

```xml
<resultMap id="xxx" type="xxx.xxx" mapUnderscoreToCamelCase="true"/>
```

## 标签和注解混用

`resultMap`和 `entity` 标签所配置的类型上如果类型上标有 `@Table` 注解。那么需要注意下面几点：

1. `resultMap` 标签会自动忽略 `@Table` 注解上配置的 catalog/schema/table 属性
2. `entity` 标签会使用标签上配置的 catalog/schema/table 属性来覆盖 `@Table` 注解上配置的。
3. `resultMap` 和 `entity` 标签下如果配置了 id/result/mapping 其中任何一种标签，那么无论类型中是否配置了 `@Column` 都将忽略。
4. `mapUnderscoreToCamelCase` 标签无法影响 `@Column` 中配置的列名。
