# dbVisitor

<p align="center">
    <b>Vision: “One API Access Any DataBase”</b>
    <br>
    A database access library that provides unified access to different types of databases for Java, aiming to use a single API to access all databases.
</p>

<p align="center">
	<a href="https://www.dbvisitor.net/en"><b>Website</b></a> •
	<a href="https://www.dbvisitor.net/en/docs/guides/overview"><b>Documentation</b></a> •
    <a href="https://www.dbvisitor.net/en/blog"><b>Blog</b></a>
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

## 📖 Introduction

**dbVisitor** is a **unified data access foundation** built on the JDBC standard, dedicated to realizing the vision of **“One API Access Any DataBase”**.
With its unique **Dual-Layer Adapter Architecture**, it successfully breaks the programming barrier between RDBMS and NoSQL, allowing developers to seamlessly manipulate various databases such as MySQL, MongoDB, and Elasticsearch using **the same set of standard APIs**.
dbVisitor does not advocate inventing new syntax to conceal differences, but rather scientifically **manages differences** through **standardization** and **layered abstraction**, providing a general-purpose data foundation for Java applications that is both convenient and retains the flexibility of JDBC.

<img src="https://www.dbvisitor.net/assets/images/api-levels-78d3da41ab16f3a14b80f564e4a5e18c.jpg" alt="api-levels"/>

dbVisitor provides multi-level API abstraction, allowing for free switching between **Simplicity** and **Flexibility**:

- 🛡 **LambdaTemplate**: Shield Differences
  - Type-safe Lambda query builder supporting method chaining.
  - Single-table CRUD methods, ready to use out of the box, automatically translated into SQL or DSL for the target database.
- 🧱 **Mapper/Interface**: Manage Differences
  - Declarative interface programming, supporting `@Query`/XML, separating SQL/DSL logic from code.
  - Provides BaseMapper generic methods, eliminating legitimate repetitive development.
- 🔧 **JdbcTemplate**: Pass-Through Differences
  - Standard JDBC template methods, supporting pass-through of native SQL and Shell scripts.
  - Provides ConnectionCallback, allowing direct access to underlying driver APIs.

## ✨ Features

<img src="https://www.dbvisitor.net/assets/images/one-api3-1b04529fb64b307aff59db56d1b86b08.jpg" alt="features"/>

### ⚙️ Framework Characteristics

- **🛡️ Unified Base**: One API conquers both RDBMS (MySQL, PG...) and NoSQL (Mongo, ES...).
- **🔌 Wide Compatibility**: Pure Java (JDK8+), zero dependencies, perfectly adapted to mainstream frameworks like Spring/Solon.
- **📦 Lightweight**: Minimalist design, core capability depends only on the `cobble` toolkit.

### 🔋 Capabilities

- **API Layered Abstraction**:
  - [Programmatic API](https://www.dbvisitor.net/docs/guides/api/program_api): Access database programmatically, characterized by powerful flexibility.
  - [Declarative API](https://www.dbvisitor.net/docs/guides/api/declarative_api): Centralize SQL maintenance through interfaces, making the code structure clearer.
  - [Generic Mapper](https://www.dbvisitor.net/docs/guides/api/base_mapper): Generic Mapper makes your program more refined at the data access layer.
  - [Builder API](https://www.dbvisitor.net/docs/guides/api/lambda_api): Construct query conditions via chain calls, avoiding the tediousness of handwritten SQL.
  - [File Mapper](https://www.dbvisitor.net/docs/guides/api/file_mapper): Write SQL or DSL via files, completely separating SQL logic from code.
  - [JDBC Adapter](https://www.dbvisitor.net/docs/guides/drivers/about): Provides standard encapsulation of JDBC, supporting access to various non-relational databases.
- **Object Mapping**:
    - [One API](https://www.dbvisitor.net/docs/guides/core/mapping/about) handles different types of data sources with a smooth learning curve and no need to master complex concepts.
    - Supports intelligent result set mapping, automatically handling [Camel Case Conversion](https://www.dbvisitor.net/docs/guides/core/mapping/camel_case) and property filling.
    - Built-in 6 types of [Key Generators](https://www.dbvisitor.net/docs/guides/core/mapping/keytype); custom ones are also supported.
    - Tips: Relationship mapping is not supported (e.g., One-to-One, One-to-Many, Many-to-One, Many-to-Many).
- **SQL Rules**:
    - [Dynamic SQL Simplification](https://www.dbvisitor.net/docs/guides/rules/about): Introduces `@{...}` rule syntax significantly simplifying dynamic SQL concatenation logic, saying goodbye to cumbersome XML tags.
    - [SQL Enhancement Rules](https://www.dbvisitor.net/docs/guides/rules/dynamic_rule): Built-in rules like `@{and}`, `@{or}`, `@{in}`, automatically determining whether conditions take effect based on parameter null values.
    - [Argument Processing Rules](https://www.dbvisitor.net/docs/guides/rules/args_rule): Supports directives like `@{md5}`, `@{uuid}`, preprocessing arguments before SQL execution.
- **Argument Processing**:
    - Supports [Positional Arguments](https://www.dbvisitor.net/docs/guides/args/position): Use "?" in statements to mark parameters, binding values to the corresponding index (starting from 0).
    - Supports [Named Arguments](https://www.dbvisitor.net/docs/guides/args/named): Use syntaxes like :name, &name, or #{...} in statements to name parameters in SQL.
    - Supports [SQL Injection](https://www.dbvisitor.net/docs/guides/args/inject): Use ${...} syntax to retrieve values of named parameters and inject the result into the SQL statement.
    - Supports [Rule Arguments](https://www.dbvisitor.net/docs/guides/args/rule): Use @{...} syntax to leverage the Rule mechanism to gracefully handle common dynamic SQL scenarios.
    - Supports [Interface Mode](https://www.dbvisitor.net/docs/guides/args/interface): Customize parameter setting via interface implementation to meet special scenarios.
- **TypeHandler**:
  - Flexible type conversion system automatically handling complex mappings.
  - Rich type support covering [Basic Types](https://www.dbvisitor.net/docs/guides/types/handlers/about),
    [JSON](https://www.dbvisitor.net/docs/guides/types/json-serialization),
    [Enum](https://www.dbvisitor.net/docs/guides/types/enum-handler),
    [Array](https://www.dbvisitor.net/docs/guides/types/array-handler),
    [DateTime](https://www.dbvisitor.net/docs/guides/types/handlers/datetime-handler),
    [GIS](https://www.dbvisitor.net/docs/guides/types/gis-handler),
    [Stream](https://www.dbvisitor.net/docs/guides/types/stream-handler),
    [Bytes](https://www.dbvisitor.net/docs/guides/types/handlers/bytes-handler), etc.
- **Receive Results**:
    - Provides multiple ways to process query results on all types of data sources.
    - Common result processing includes [Bean Mapping](https://www.dbvisitor.net/docs/guides/core/mapping/about),
      [RowMapper](https://www.dbvisitor.net/docs/guides/result/for_mapper),
      [RowCallbackHandler](https://www.dbvisitor.net/docs/guides/result/row_callback),
      [ResultSetExtractor](https://www.dbvisitor.net/docs/guides/result/for_extractor).
- **Session/Transaction**:
  - Supports multi-datasource transaction management (non-distributed transactions).
  - Supports transaction control capabilities identical to Spring, including [7 Transaction Propagation Behaviors](https://www.dbvisitor.net/docs/guides/transaction/propagation).
  - Control transactions via [Programmatic](https://www.dbvisitor.net/docs/guides/transaction/manager/program),
    [Annotation](https://www.dbvisitor.net/docs/guides/transaction/manager/annotation),
    and [Template Method](https://www.dbvisitor.net/docs/guides/transaction/manager/template) approaches.
  - Tips: Although dbVisitor unifies transaction call forms, it cannot change the physical characteristics of the underlying database.
- **Advanced Features**:
  - Strong affinity for Map structures, supporting multiple result set formats:
    - Single value/Single column/Single row/Multi-row/Pagination receive modes.
    - Supports returning List\<Map\>, Map\<K,V\>, Set\<V\>, primitive arrays, and other data structures.
  - Unified pagination interface, automatically adapting to dialects like Limit/ROWNUM/Skip.
- **Driver Adapter**:
  - Can transform into an independent JDBC Driver, allowing MyBatis/Hibernate to also operate on NoSQL.
  - Supports connecting to various databases via standard JDBC URLs.
  - Adapted and supports [Redis](https://www.dbvisitor.net/docs/guides/drivers/redis/about),
    [MongoDB](https://www.dbvisitor.net/docs/guides/drivers/mongo/about),
    [ElasticSearch](https://www.dbvisitor.net/docs/guides/drivers/elastic/about), etc.

## 💡 Why dbVisitor?

- **Dual-Layer Adapter Capability**
  - Use dbVisitor as both a database access library and a JDBC Driver. You can use its **JDBC Driver** independently, putting it into a Spring Boot + MyBatis project, instantly giving MyBatis the ability to operate on MongoDB and Elasticsearch.
- **Unified Underlying Architecture**
  - Unlike simple patching, the various API levels in dbVisitor's **Layered Abstraction** **share** the same underlying mechanisms, avoiding the "Frankenstein" effect of multiple frameworks.
- **Independence**
  - Not bound to any ecosystem framework like Spring or any Web container. Built on pure Java (JDK 8+) and JDBC standards. Seamlessly integrates whether it's Spring, SpringBoot, Solon, Hasor, Guice, or a Main method console program.

## 🚀 Usage

### 1. Add Dependency
```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor</artifactId>
    <version>6.4.0</version> <!-- Check Maven Central for the latest version -->
</dependency>
```

### 2. Code Examples

#### Unified CRUD
Code is exactly the same whether operating on MySQL or Elasticsearch:

```java
// Insert Data
template.insert(UserInfo.class)
        .applyEntity(new UserInfo("1001", "dbVisitor"))
        .executeSumResult();

// Query Data (Automatically translated to SQL or DSL)
List<UserInfo> list = template.lambdaQuery(UserInfo.class)
        .eq(UserInfo::getAge, 18)
        .list();
```

#### Complex Query (Mapper Interface)
Define an interface to enjoy a MyBatis-like development experience:

```java
@SimpleMapper
public interface UserMapper extends BaseMapper<UserInfo> {
    
    // Method 1: Pure Java Construction (No XML needed)
    default List<UserInfo> findActiveUsers(int minAge) {
        return this.query()
                   .eq(UserInfo::getStatus, "ENABLE")
                   .gt(UserInfo::getAge, minAge)
                   .list();
    }

    // Method 2: Annotation Binding (SQL / DSL)
    @Query("select * from user_info where age > #{age}")
    List<UserInfo> findByAge(@Param("age") int age);

    // Method 3: XML Mapping (Supports native SQL or DSL)
    // Coexists with UserMapper.xml, logic separation
    List<Map<String, Object>> groupByAge(@Param("minAge") int minAge);
}
```

#### XML Mapping Example (UserMapper.xml)

```xml
<!-- UserMapper.xml -->
<mapper namespace="com.example.UserMapper">
    <select id="groupByAge">
        <!-- Write native SQL (MySQL) -->
        SELECT age, count(*) FROM user_info 
        WHERE age > #{minAge} GROUP BY age
        
        <!-- Or write JSON DSL (Elasticsearch) -->
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

#### Escape Hatch (Native Experience)
When all abstractions fail to meet your needs, you can pierce through the framework:

```java
JdbcTemplate template = ...;
// 1. Native SQL/Shell Pass-Through (Directly execute commands recognized by the database)
// MySQL
template.queryForList("select * from user where id = ?", 1);
// MongoDB (Directly write Mongo Shell)
template.queryForList("db.user.find({_id: ?})", 1);

// 2. Direct Core SDK Access (Unwrap Mechanism)
T resultList = jdbcTemplate.execute((ConnectionCallback<T>) con -> {
    // Unwrap the underlying native driver object (e.g., MongoClient) via standard JDBC Connection
    if (conn.isWrapperFor(MongoClient.class)) {
        MongoClient client = conn.unwrap(MongoClient.class);
        // Call any API of the official Driver...
    }
    return ...;
});
```

## 📚 Resources

- **Website**: [https://www.dbvisitor.net](https://www.dbvisitor.net)
- **Documentation**: [https://www.dbvisitor.net/docs/guides](https://www.dbvisitor.net/docs/guides)
- **Blog**: [https://www.dbvisitor.net/blog](https://www.dbvisitor.net/blog)

## 📄 License

dbVisitor is released under the commercially friendly [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) license.
