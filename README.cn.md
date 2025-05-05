介绍
------------------------------------

<p align="center">
	dbVisitor 数据库访问库，提供 Java 对关系数据库更加自然的访问。
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
    <a target="_blank" href='https://gitee.com/opensolon/solon/stargazers'>
		<img src='https://gitee.com/opensolon/solon/badge/star.svg' alt='gitee star'/>
	</a>
    <a target="_blank" href='https://github.com/opensolon/solon/stargazers'>
		<img src="https://img.shields.io/github/stars/opensolon/solon.svg?style=flat&logo=github" alt="github star"/>
	</a>
    <br />
    [<a target="_blank" href='./README.en.md'>English</a>]
    [<a target="_blank" href='./README.cn.md'>中文</a>]
</p>

dbVisitor 是建立在 JDBC 基础之上，如果您的数据源有 JDBC 驱动程序，则可以很方便的将其与 dbVisitor 一起使用。 它改进了 JDBC 低级接口提供更加自然的 API。

## 为什么使用它？

已经有许多基于JDBC的数据库访问方法和库，其中不乏许多知名的工具。尽管如此，人们仍然在寻求更简单、更方便的数据访问方法，这导致了更先进或更有趣的数据访问方法的出现。从EJB到Hibernate，然后到MyBatis、SpringJDBC，以及曾经或仍然活跃的ActiveRecord、QueryWrapper、Row、Chain、JPA等。每一种新方法都给人们带来惊喜，同时也有其固有的局限性。
即便如此人们依然在寻找对数据的访问更加简单便利的方法，这也使得有更多先进或更有意思的数据访问方式出现。

dbVisitor 的核心突破在于访问模式的无缝集成，开发者可在同一项目中混合使用不同的访问范式。这种设计解决了传统方案中多框架并存导致的兼容性问题，例如：JdbcTemplate 和 MyBatis 不同风格操作。

这种多范式集成架构使得 dbVisitor 能够适应不同复杂度场景：小型项目可快速启用 ActiveRecord 模式提升开发效率，中大型系统则可通过动态 SQL 与存储过程支持实现复杂业务逻辑。

## 使用 dbVisitor

- dbVisitor 使用商业友好的 [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) 许可协议。
- dbVisitor 只有 [Cobble](https://gitee.com/zycgit/cobble) 一个依赖（cobble 是一个类似 Apache Commons 或 Guava 的工具包）
- 所有 dbVisitor 模块以及依赖项都可以通过 [Maven Central](https://central.sonatype.com/search?q=dbvisitor) 获得。

## JVM 兼容性

dbVisitor 可在所有 Java 8 或更高版本上运行，所有版本均使用 Java 8 编译构建。

## 生态支持

- Spring、SpringBoot、Solon、Hasor、Guice
