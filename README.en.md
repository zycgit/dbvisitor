About
------------------------------------

<p align="center">
    dbVisitor database access library provides a unified Java API for interacting with multiple types of databases, making data reads and writes more natural.
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
- dbvisitor-adapter — JDBC driver adapters intended to allow databases without a JDBC Driver to be accessed via the JDBC interface. supports [Redis](dbvisitor-adapter/jdbc-redis/README.md) and MongoDB.
- dbvisitor-integration — integration modules that connect dbVisitor with mainstream frameworks. Supports Spring, Spring Boot, Solon, Hasor, Guice, etc.
- dbvisitor — the core module that provides a unified database access API.

## Why use it?

When relational databases dominated, data access methods became more varied and many mature JDBC-based tools emerged.
With the rise of non relational databases, storage formats have become more diverse and access APIs more complex.
Developers must switch between different APIs, increasing learning costs and usage difficulty.
Existing data access technologies such as Hibernate, MyBatis, Spring JDBC and approaches like ActiveRecord, QueryWrapper,
Row, Chain, JPA mainly target relational databases and show limitations when facing the growing variety of non relational storage.


The core breakthrough of dbVisitor is the seamless integration of access patterns.
Developers can mix multiple access paradigms within the same project and obtain a unified experience between relational and non-relational storage.
With the drivers module, dbVisitor is no longer limited to accessing relational databases via JDBC; it supports non-relational databases such as Redis and MongoDB through adapters,
thereby resolving compatibility issues that arise when multiple frameworks coexist (for example, stylistic differences between JdbcTemplate and MyBatis) and unifying the operational interfaces across different storage types.

## About dbVisitor

- dbVisitor uses the business-friendly [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) license.
- dbVisitor only has one dependency, [Cobble](https://gitee.com/zycgit/cobble) (Cobble is a toolkit similar to Apache Commons or Guava).
- All dbVisitor modules and their dependencies can be obtained via [Maven Central](https://central.sonatype.com/search?q=dbvisitor).

## JVM Compatibility

dbVisitor can run on all Java 8 or higher versions, and all versions are compiled and built using Java 8.

## Support

- Spring、SpringBoot、Solon、Hasor、Guice
