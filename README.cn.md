介绍
------------------------------------

<p align="center">
	dbVisitor 提供了一种统一且简便的方式，可访问多种不同类型的数据库。
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

dbVisitor 是建立在 JDBC 基础之上，它改进了 JDBC 低级接口提供更加自然的 API。主要包含如下三个部分：
- dbvisitor-adapter，是 JDBC 驱动适配器集合，目的是使不具备 JDBC Driver 的数据库可以用过 JDBC 接口访问。支持 [Redis](dbvisitor-adapter/jdbc-redis/README.md)、MongoDB 等非关系型数据库。
- dbvisitor-integration，是 dbVisitor 与主流框架的集成模块。支持 Spring、SpringBoot、Solon、Hasor、Guice 等主流框架。
- dbvisitor，是核心模块，提供统一的数据库访问 API。

## 为什么使用它？

过去关系型数据库占主导时，数据访问手段已趋多样并且涌现了大量基于 JDBC 的成熟工具；但随着非关系型数据库兴起，数据存储形式更加多元访问 API 也变得更加复杂。开发者需要在不同的 API 之间切换，增加了学习成本和使用难度。
而已有的数据访问技术如 Hibernate、MyBatis、Spring JDBC 以及 ActiveRecord、QueryWrapper、Row、Chain、JPA 等主要针对关系型数据库，在面对日益多样的非关系型存储时显现出局限性。

dbVisitor 的核心突破在于访问模式的无缝集成：开发者可以在同一项目中混合使用多种访问范式，并在关系型与非关系型存储之间获得统一体验。
借助 drivers 模块，dbVisitor 已不再局限于通过 JDBC 访问关系型数据库，而是通过适配器支持例如 Redis、MongoDB 等非关系型数据库，
从而解决了传统多框架并存时的兼容性问题（例如 JdbcTemplate 与 MyBatis 的风格差异），并统一不同存储类型的操作接口。

## 使用 dbVisitor

- dbVisitor 使用商业友好的 [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) 许可协议。
- dbVisitor 只有 [Cobble](https://gitee.com/zycgit/cobble) 一个依赖（cobble 是一个类似 Apache Commons 或 Guava 的工具包）
- 所有 dbVisitor 模块以及依赖项都可以通过 [Maven Central](https://central.sonatype.com/search?q=dbvisitor) 获得。

## JVM 兼容性

dbVisitor 可在所有 Java 8 或更高版本上运行，所有版本均使用 Java 8 编译构建。

## 生态支持

- Spring、SpringBoot、Solon、Hasor、Guice
