About
------------------------------------

<p align="center">
    dbVisitor provides a unified, natural way to access many different types of databases.
</p>

<p align="center">
	<a href="https://www.dbvisitor.net">https://www.dbvisitor.net</a>
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
    <br />
    <a target="_blank" href='https://gitee.com/zycgit/dbvisitor/stargazers'>
		<img src='https://gitee.com/zycgit/dbvisitor/badge/star.svg' alt='gitee star'/>
	</a>
    <a target="_blank" href='https://github.com/zycgit/dbvisitor/stargazers'>
		<img src="https://img.shields.io/github/stars/zycgit/dbvisitor.svg?style=flat&logo=github" alt="github star"/>
	</a>
    <br />
    [<a target="_blank" href='./README.en.md'>English</a>]
    [<a target="_blank" href='./README.cn.md'>中文</a>]
</p>

dbVisitor is built on top of JDBC and refines JDBC's low-level interfaces to provide a more natural API. It mainly consists of the following three parts:
- dbvisitor — the core module that provides a unified database access API.
- dbvisitor-integration — integration modules that connect dbVisitor with mainstream frameworks. Supports Spring, Spring Boot, Solon, Hasor, Guice, etc.
- dbvisitor-adapter — JDBC driver adapters intended to allow databases without a JDBC Driver to be accessed via the JDBC interface.
  supports [Redis](dbvisitor-adapter/jdbc-redis/README_en.md), [MongoDB](dbvisitor-adapter/jdbc-mongo/README_en.md) and [ElasticSearch](dbvisitor-adapter/jdbc-elastic/README_en.md).

## Why use it?

When relational databases dominated, data access methods diversified and a large number of mature JDBC-based tools emerged;
however, with the rise of non-relational databases, data storage formats have become more varied and access APIs have grown more complex.
Developers need to switch between different APIs, which increases the learning curve and the difficulty of use.
Existing data access technologies such as Hibernate, MyBatis, Spring JDBC, as well as ActiveRecord, QueryWrapper,
Row, Chain, and JPA, are primarily targeted at relational databases and show limitations when confronted with an increasingly diverse set of non-relational stores.

The core breakthrough of dbVisitor lies in the seamless integration of access patterns: developers can mix multiple access paradigms 
within the same project and obtain a unified experience across relational and non-relational storage. With the drivers module, 
dbVisitor is no longer limited to accessing relational databases via JDBC, but supports non-relational databases such as Redis, MongoDB and ElasticSearch through adapters.

In its design, dbVisitor offers an API style similar to JdbcTemplate and MyBatis, enabling developers to avoid adopting a large number of
new concepts and thus reducing the learning curve and usage complexity.

## About dbVisitor

- dbVisitor uses the business-friendly [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) license.
- dbVisitor only has one dependency, [Cobble](https://gitee.com/zycgit/cobble) (Cobble is a toolkit similar to Apache Commons or Guava).
- All dbVisitor modules and their dependencies can be obtained via [Maven Central](https://central.sonatype.com/search?q=dbvisitor).

## JVM Compatibility

dbVisitor can run on all Java 8 or higher versions, and all versions are compiled and built using Java 8.

## Support

- Spring、SpringBoot、Solon、Hasor、Guice
