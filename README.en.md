About
------------------------------------
``Documents are translated using translation software, The original for README.md``

* Project Home: [https://www.dbvisitor.net](https://www.dbvisitor.net)
* [![QQ群:948706820](https://img.shields.io/badge/QQ%E7%BE%A4-948706820-orange)](https://qm.qq.com/cgi-bin/qm/qr?k=Qy3574A4VgI0ph4fqFbZW-w49gnyqu6p&jump_from=webapi)
  [![zyc@byshell.org](https://img.shields.io/badge/Email-zyc%40byshell.org-blue)](mailto:zyc@byshell.org)
  [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.hasor/dbvisitor/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.hasor/dbvisitor)

dbVisitor offers a more natural way for Java to access relational databases.

dbVisitor is built on top of JDBC. If your data source has a JDBC driver, you can easily use it in conjunction with dbVisitor. It enhances the low - level JDBC interfaces by providing a more intuitive API.

dbVisitor is a library that simplifies database access for Java. Although it bears some resemblance to ORM, it has many differences compared to the true concept of ORM.

dbVisitor is straightforward as it focuses solely on database access. Any specific functions with particular business implications fall outside the scope of dbVisitor. Nevertheless, you can still leverage the ingenious design of dbVisitor to meet specific business requirements.

## Why use it?

There are already numerous ways and libraries for database access based on JDBC, and among them, there are many well - known tools. Even so, people are still looking for simpler and more convenient ways to access data, which has led to the emergence of more advanced or interesting data access methods.

From EJB to Hibernate, and then to later ones like MyBatis, Spring JDBC, as well as ActiveRecord, QueryWrapper, Row, Chain, JPA, etc., which were once or still are active. Each new method brings both surprises and its inherent limitations.

dbVisitor doesn't create a new type of access method. Instead, it integrates the widely - popular access methods. Users can choose the appropriate way to access the database rather than being restricted to a single mode.

## About dbVisitor

- dbVisitor uses the business-friendly [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) license.
- dbVisitor only has one dependency, [Cobble](https://gitee.com/zycgit/cobble) (Cobble is a toolkit similar to Apache Commons or Guava).
- All dbVisitor modules and their dependencies can be obtained via [Maven Central](https://central.sonatype.com/search?q=dbvisitor).

## JVM Compatibility

dbVisitor can run on all Java 8 or higher versions, and all versions are compiled and built using Java 8.

## Support

- Spring、SpringBoot、Solon、Hasor、Guice
