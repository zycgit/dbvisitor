<p align="center">
    <b>愿景 “One APIs Access Any DataBase”</b>
    <br>
    一款数据库访问库，提供 Java 对多种不同类型数据库统一访问，它的目标是使用一套 API 访问所有数据库。
</p>

<p align="center">
	<a href="https://www.dbvisitor.net"><b>Website</b></a> •
	<a href="https://www.dbvisitor.net/docs/guides/overview"><b>Documentation</b></a> •
    <a href="https://www.dbvisitor.net/blog"><b>Blog</b></a>
</p>

<p align="center">
    <a target="_blank" href="https://central.sonatype.com/artifact/net.hasor/dbvisitor">
        <img src="https://img.shields.io/maven-central/v/net.hasor/dbvisitor.svg?label=Maven%20Central" alt="Maven" />
    </a>
    <a target="_blank" href="LICENSE.txt">
		<img src="https://img.shields.io/:License-Apache2-blue.svg" alt="Apache 2" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
		<img src="https://img.shields.io/badge/JDK-8-green.svg" alt="jdk-8" />
	</a>
    <a target="_blank" href='https://gitee.com/zycgit/dbvisitor/stargazers'>
		<img src='https://gitee.com/zycgit/dbvisitor/badge/star.svg' alt='gitee star'/>
	</a>
    <a target="_blank" href='https://github.com/zycgit/dbvisitor/stargazers'>
		<img src="https://img.shields.io/github/stars/zycgit/dbvisitor.svg?style=flat&logo=github" alt="github star"/>
	</a>
    <br />
    <a href="https://qm.qq.com/cgi-bin/qm/qr?k=-ha3lrkHrAddrZMINYbmxj4W7ZrrWW2b&jump_from=webapi&authKey=BtyfWIjWF7uhOf/ZPur+pr5p1efOZyjGESLynkPzbJ9IMd/j/T/pR1SDLcJKC972">
        <img border="0" src="https://img.shields.io/badge/QQ%E7%BE%A42-948706820-orange" alt="dbVisitor ORM 交流群1" title="dbVisitor ORM 交流群1"/>
    </a>
    <br />
    [<a target="_blank" href='./README.en.md'>English</a>]
    [<a target="_blank" href='./README.cn.md'>中文</a>]
</p>

---

## 📖 简介 | Introduction

**dbVisitor** 是一款基于 JDBC 标准构建的**统一数据访问基座**，致力于实现 **“One APIs Access Any DataBase”** 的愿景。
借助独特的**双层适配器架构**，它成功打破了 RDBMS 与 NoSQL 之间的编程壁垒，让开发者能够使用**同一套标准 API** 无缝操作 MySQL、MongoDB、Elasticsearch 等各类数据库。
dbVisitor 不主张发明新语法去掩盖差异，而是通过**标准化**与**分层抽象**来科学地**管理差异**，为 Java 应用提供了一个既具备便捷性又保留 JDBC 灵活性的通用数据底座。

<img src="https://www.dbvisitor.net/assets/images/api-levels-78d3da41ab16f3a14b80f564e4a5e18c.jpg" alt="api-levels"/>

dbVisitor 提供了多层次的 API 抽象，允许在 **简单性** 与 **灵活性** 之间自由切换：

- 🛡 **LambdaTemplate**: 屏蔽差异
  - 类型安全的 Lambda 查询构建器，支持链式调用。
  - 单表 CRUD 方法，开箱即用，自动翻译为目标数据库的 SQL 或 DSL。
- 🧱 **Mapper/Interface**: 管理差异
  - 声明式接口编程，支持 `@Query`/XML，将 SQL/DSL 逻辑与代码分离。
  - 提供 BaseMapper 通用方法无需重复开发。
- 🔧 **JdbcTemplate**: 透传差异
  - 标准 JDBC 模板方法，支持原生 SQL 和 Shell 脚本透传。
  - 提供 ConnectionCallback，允许直接访问底层驱动 API。

## ✨ 核心特性 | Features

<img src="https://www.dbvisitor.net/assets/images/one-api3-1b04529fb64b307aff59db56d1b86b08.jpg" alt="features"/>

### ⚙️ 框架特点 (Framework Characteristics)

- **🛡️ 统一基座**: 一套 API 通杀 RDBMS (MySQL, PG...) 与 NoSQL (Mongo, ES...)。
- **🔌 广泛兼容**: 纯 Java (JDK8+)，零依赖，完美适配 Spring/Solon 等主流框架。
- **📦 轻量级**: 极简设计，核心仅依赖 `cobble` 工具包。

### 🔋 基础能力 (Capabilities)

- **API 分层抽象**：
  - [编程式 API](https://www.dbvisitor.net/docs/guides/api/program_api)，通过编程方式实现对数据库的访问，最大特点是具有强大的灵活性。
  - [声明式 API](https://www.dbvisitor.net/docs/guides/api/declarative_api)，通过接口可以对 SQL 的维护更加集中，使代码结构变得更加清晰。
  - [通用 Mapper](https://www.dbvisitor.net/docs/guides/api/base_mapper)，通用 Mapper 可以让您的程序在数据访问层变得更加精炼。
  - [构造器 API](https://www.dbvisitor.net/docs/guides/api/lambda_api)，通过链式调用的方式构造查询条件，避免了手写 SQL 的繁琐。
  - [文件 Mapper](https://www.dbvisitor.net/docs/guides/api/file_mapper)，通过文件的方式编写 SQL 或 DSL，使得 SQL 逻辑与代码完全分离。
  - [JDBC 适配器](https://www.dbvisitor.net/docs/guides/drivers/about)，提供对 JDBC 的标准封装，支持各类非关系型数据库的接入。
- **对象映射**：
    - [一套 API](https://www.dbvisitor.net/docs/guides/core/mapping/about) 应对不同类型数据源，学习曲线平稳，无需掌握复杂的概念。
    - 支持智能结果集映射，自动处理 [驼峰转换](https://www.dbvisitor.net/docs/guides/core/mapping/camel_case) 与属性填充。
    - 内置 6 种 [主键生成器](https://www.dbvisitor.net/docs/guides/core/mapping/keytype)，不够还可以自定义
    - Tips：不支持关系映射，如：一对一、一对多、多对一、多对多。
- **SQL Rules**：
    - [动态 SQL 简化](https://www.dbvisitor.net/docs/guides/rules/about)，引入 `@{...}` 规则语法，大幅简化动态 SQL 拼接逻辑，告别繁琐的 XML 标签。
    - [SQL 增强规则](https://www.dbvisitor.net/docs/guides/rules/dynamic_rule)，内置 `@{and}`, `@{or}`, `@{in}` 等规则，自动根据参数空值情况判定条件是否生效。
    - [参数处理规则](https://www.dbvisitor.net/docs/guides/rules/args_rule)，支持 `@{md5}`, `@{uuid}` 等指令，在 SQL 执行前对参数进行预处理。
- **参数处理**：
    - 支持 [位置参数](https://www.dbvisitor.net/docs/guides/args/position)，语句中使用 “?” 标记参数，可以将值绑定到相应索引（从 0 开始）的参数。
    - 支持 [名称参数](https://www.dbvisitor.net/docs/guides/args/named)，语句中使用如 :name、&name 或 #{...} 写法，可以将 SQL 中的参数名称化。
    - 支持 [SQL 注入](https://www.dbvisitor.net/docs/guides/args/inject)，语句中使用 ${...} 写法，可以对已名称化的参数进行取值，并将结果注入到 SQL 语句中。
    - 支持 [规则传参](https://www.dbvisitor.net/docs/guides/args/rule)，语句中通过 @{...} 写法，可以借助 规则 机制，优雅的处理一些常见动态 SQL 场景。
    - 支持 [接口方式](https://www.dbvisitor.net/docs/guides/args/interface)，通过接口实现方式让参数设置更加具有定制化，以满足一些特殊的场景。
- **TypeHandler**：
  - 灵活的类型转换系统，自动处理复杂映射。
  - 丰富的类型支持，涵盖 [基础类型](https://www.dbvisitor.net/docs/guides/types/handlers/about)、
    [JSON](https://www.dbvisitor.net/docs/guides/types/json-serialization)、
    [枚举](https://www.dbvisitor.net/docs/guides/types/enum-handler)、
    [数组](https://www.dbvisitor.net/docs/guides/types/array-handler)、
    [时间类型](https://www.dbvisitor.net/docs/guides/types/handlers/datetime-handler)、
    [地理信息类型](https://www.dbvisitor.net/docs/guides/types/gis-handler)、
    [流类型](https://www.dbvisitor.net/docs/guides/types/stream-handler)、
    [字节类型](https://www.dbvisitor.net/docs/guides/types/handlers/bytes-handler) 等。
- **接收结果**：
    - 在所有类型数据源上提供多种方式处理查询结果。
    - 常见结果处理有 [Bean映射](https://www.dbvisitor.net/docs/guides/core/mapping/about)、
      [RowMapper](https://www.dbvisitor.net/docs/guides/result/for_mapper)、
      [RowCallbackHandler](https://www.dbvisitor.net/docs/guides/result/row_callback)、
      [ResultSetExtractor](https://www.dbvisitor.net/docs/guides/result/for_extractor)
- **Session/Transaction**：
  - 支持多数据源事物管理（非分布式事务）
  - 支持和 Spring 一样的事务控制能力，包括 [7 种事物传播行为](https://www.dbvisitor.net/docs/guides/transaction/propagation)。
  - 支持通过 [编程式](https://www.dbvisitor.net/docs/guides/transaction/program)、
    [注解式](https://www.dbvisitor.net/docs/guides/transaction/annotation)、
    [模版方法](https://www.dbvisitor.net/docs/guides/transaction/template) 几种方式控制事务。
  - Tips：尽管 dbVisitor 统一了事务等调用形式，但它不能改变底层数据库的物理特性。
- **高级特性**：
  - Map 结构亲和力强，支持多种结果集格式：
    - 单值/单列/单行/多行/分页 等多种结果集接收方式。
    - 支持返回 List\<Map\>、Map\<K,V\>、Set\<V\>、基本类型数组 等多种数据结构。
  - 统一分页接口，自动适配 Limit/ROWNUM/Skip 等方言。
- **驱动适配器**：
  - 可化身为独立的 JDBC Driver，让 MyBatis/Hibernate 也能操作 NoSQL。
  - 支持通过标准 JDBC URL 连接各类数据库。
  - 已经适配，支持 [Redis](https://www.dbvisitor.net/docs/guides/drivers/redis/about)、
    [MongoDB](https://www.dbvisitor.net/docs/guides/drivers/mongo/about)、
    [ElasticSearch](https://www.dbvisitor.net/docs/guides/drivers/elastic/about) 等。

## 💡 为何选择 dbVisitor？ | Why dbVisitor

- **双层适配能力**
  - 能力加法，dbVisitor 既是数据库访问库也是 JDBC Driver。你可以单独使用其 **JDBC Driver**，将其放入 Spring Boot + MyBatis 项目中。
    让 MyBatis 立刻具备操作 MongoDB 和 Elasticsearch 的能力。
- **底层架构统一**
  - 不同于简单的拼凑，dbVisitor 在 **API 分层抽象** 中提供的各级 API **共享** 同一套底层机制。
    告别多框架产生的缝合怪效应。
- **独立性**
  - 不绑定任何生态框架 Spring 或任何 Web 容器。基于纯 Java (JDK 8+) 和 JDBC 标准构建。
    无论是 Spring、SpringBoot、Solon、Hasor、Guice 还是 Main 方法控制台程序，都能无缝集成。

## 🚀 使用介绍

### 1. 引入依赖
```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor</artifactId>
    <version>6.4.0</version> <!-- 请检查 Maven Central 获取最新版本 -->
</dependency>
```

### 2. 实战代码

#### 统一 CRUD
无论操作 MySQL 还是 Elasticsearch，代码完全一致：

```java
// 插入数据
template.insert(UserInfo.class)
        .applyEntity(new UserInfo("1001", "dbVisitor"))
        .executeSumResult();

// 查询数据 (自动翻译为 SQL 或 DSL)
List<UserInfo> list = template.lambdaQuery(UserInfo.class)
        .eq(UserInfo::getAge, 18)
        .list();
```

#### 复杂查询 (Mapper 接口)
定义接口，即可享受类似 MyBatis 的开发体验：

```java
@SimpleMapper
public interface UserMapper extends BaseMapper<UserInfo> {
    
    // 方式一：纯 Java 构建 (无需 XML)
    default List<UserInfo> findActiveUsers(int minAge) {
        return this.query()
                   .eq(UserInfo::getStatus, "ENABLE")
                   .gt(UserInfo::getAge, minAge)
                   .list();
    }

    // 方式二：注解绑定 (SQL / DSL)
    @Query("select * from user_info where age > #{age}")
    List<UserInfo> findByAge(@Param("age") int age);

    // 方式三：XML 映射 (支持原生 SQL 或 DSL)
    // 配合 UserMapper.xml 使用，逻辑分离
    List<Map<String, Object>> groupByAge(@Param("minAge") int minAge);
}
```

#### XML 映射示例 (UserMapper.xml)

```xml
<!-- UserMapper.xml -->
<mapper namespace="com.example.UserMapper">
    <select id="groupByAge">
        <!-- 编写原生 SQL (MySQL) -->
        SELECT age, count(*) FROM user_info 
        WHERE age > #{minAge} GROUP BY age
        
        <!-- 或者编写 JSON DSL (Elasticsearch) -->
        <!--
        POST /user_info/_search
        {
            "query": { "range": { "age": { "gt": #{minAge} } } },
            "aggs": { "age_group": { "terms": { "field": "age" } } }
        }
        -->
    </select>
</mapper>
```

#### 逃生舱 (原生体验)
当所有抽象都无法满足需求时，你可以直接穿透框架：

```java
JdbcTemplate template = ...;
// 1. 原生 SQL/Shell 透传 (直接执行数据库能识别的命令)
// MySQL
template.queryForList("select * from user where id = ?", 1);
// MongoDB (直接写 Mongo Shell)
template.queryForList("db.user.find({_id: ?})", 1);

// 2. 底层 SDK 直达 (Unwrap 机制)
T resultList = jdbcTemplate.execute((ConnectionCallback<T>) con -> {
    // 通过标准 JDBC Connection，拆包出底层的原生驱动对象 (如 MongoClient)
    if (conn.isWrapperFor(MongoClient.class)) {
        MongoClient client = conn.unwrap(MongoClient.class);
        // 调用官方 Driver 的任意 API ...
    }
    return ...;
});
```

## 📚 文档与资源 | Resources

- **官方网站**: [https://www.dbvisitor.net](https://www.dbvisitor.net)
- **文档指南**: [https://www.dbvisitor.net/docs/guides](https://www.dbvisitor.net/docs/guides)
- **博客文章**: [https://www.dbvisitor.net/blog](https://www.dbvisitor.net/blog)

## 📄 许可证 | License

dbVisitor 使用商业友好的 [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) 许可协议。
