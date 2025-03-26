介绍
------------------------------------

* Project Home: [https://www.dbvisitor.net](https://www.dbvisitor.net)
* [![QQ群:948706820](https://img.shields.io/badge/QQ%E7%BE%A4-948706820-orange)](https://qm.qq.com/cgi-bin/qm/qr?k=Qy3574A4VgI0ph4fqFbZW-w49gnyqu6p&jump_from=webapi)
  [![zyc@byshell.org](https://img.shields.io/badge/Email-zyc%40byshell.org-blue)](mailto:zyc@byshell.org)
  [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.hasor/dbvisitor/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.hasor/dbvisitor)

dbVisitor 提供 Java 对关系数据库更加自然的访问。

dbVisitor 是建立在 JDBC 基础之上，如果您的数据源有 JDBC 驱动程序，则可以很方便的将其与 dbVisitor 一起使用。
它改进了 JDBC 低级接口提供更加自然的 API。

dbVisitor 是方便 Java 访问数据库的库，虽然有些类似 ORM 但和真正意义上的 ORM 理念比起来还是有很多不同点。

dbVisitor 是简单的，它专注数据库的访问。任何带有具体业务含义的特定功能都不属于 dbVisitor 范畴。尽管如此你仍然可以利用 dbVisitor 巧妙的设计来满足特定业务的需要。

## 为什么使用它？

基于 JDBC 的数据库访问的方式和库已经非常多并且其中不缺乏有很多知名的工具。即便如此人们依然在寻找对数据的访问更加简单便利的方法，这也使得有更多先进或更有意思的数据访问方式出现。

从 EJB 到 Hibernate 再到后来的 MyBatis、SpringJDBC 及曾经或者依然活跃的 ActiveRecord、QueryWrapper、Row、Chain、JPA 等等。
每种新方式都有给人带来惊喜同时也会带来其固有的局限性。

dbVisitor 并没有开创某种新型的访问方式而是将广为流行的访问方式加以整合汇聚，使用者可以选择适合的方式访问数据库而不是单一的模式。

## 使用 dbVisitor

- dbVisitor 使用商业友好的 [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) 许可协议。
- dbVisitor 只有 [Cobble](https://gitee.com/zycgit/cobble) 一个依赖（cobble 是一个类似 Apache Commons 或 Guava 的工具包）
- 所有 dbVisitor 模块以及依赖项都可以通过 [Maven Central](https://central.sonatype.com/search?q=dbvisitor) 获得。

## JVM 兼容性

dbVisitor 可在所有 Java 8 或更高版本上运行，所有版本均使用 Java 8 编译构建。

## 生态支持

- Spring、SpringBoot、Solon、Hasor、Guice
