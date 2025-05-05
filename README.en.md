About
------------------------------------

<p align="center">
	dbVisitor database access library, provides Java with more natural access to relational databases.
</p>

<p align="center">
	<a href="https://www.dbvisitor.net">https://www.dbvisitor.net</a>
</p>

<p align="center">
    <a target="_blank" href="https://central.sonatype.com/artifact/net.hasor/dbvisitor">
        <img src="https://img.shields.io/maven-central/v/net.hasor/dbvisitor.svg?label=Maven%20Central" alt="Maven" />
    </a>
    <a target="_blank" href="LICENSE">
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

dbVisitor is built on top of JDBC. If your data source has a JDBC driver, you can easily use it in conjunction with dbVisitor. It enhances the low - level JDBC interfaces by providing a more intuitive API.

## Why use it?

There are already many JDBC-based database access methods and libraries, including several well-known tools. Nevertheless, people continue to seek simpler and more convenient data access methods, which has led to the emergence of more advanced or interesting data access approaches. From EJB to Hibernate, then to MyBatis, SpringJDBC, and other active or once-active technologies like ActiveRecord, QueryWrapper, Row, Chain, JPA, etc. Each new method brings surprises while having its inherent limitations.

The core breakthrough of dbVisitor lies in its seamless integration of access patterns, allowing developers to mix different access paradigms within the same project. This design solves compatibility issues caused by coexisting multiple frameworks in traditional solutions, such as operations with different styles between JdbcTemplate and MyBatis.

his multi-paradigm integrated architecture enables dbVisitor to adapt to scenarios of varying complexity: small projects can quickly enable ActiveRecord mode to improve development efficiency, while medium and large systems can implement complex business logic through dynamic SQL and stored procedure support.

## About dbVisitor

- dbVisitor uses the business-friendly [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) license.
- dbVisitor only has one dependency, [Cobble](https://gitee.com/zycgit/cobble) (Cobble is a toolkit similar to Apache Commons or Guava).
- All dbVisitor modules and their dependencies can be obtained via [Maven Central](https://central.sonatype.com/search?q=dbvisitor).

## JVM Compatibility

dbVisitor can run on all Java 8 or higher versions, and all versions are compiled and built using Java 8.

## Support

- Spring、SpringBoot、Solon、Hasor、Guice
