介绍
------------------------------------

HasorDB 是一款数据库访问框架，其思想和编程方式来自于 SpringJDBC、MyBatis、MyBatisPlus。可以简单理解 HasorDB 是前面三者集合体。
不同于一般整合方式的是 HasorDB 采用了参照原有接口完全重新实现，尽可能的接近原始框架的使用体验。

虽然 HasorDB 是参照已有框架重新实现，但同时也融入了其独特的一些新特性。这使得使用起来更加便捷。


功能特性
------------------------------------

- 熟悉的方式
    - JdbcTemplate 接口方式（高度兼容 Spring JDBC）
    - Mapper 文件方式（高度兼容 MyBatis）
    - LambdaTemplate （高度接近 MyBatis Plus、jOOQ 和 BeetlSQL）
    - @Insert、@Update、@Delete、@Query、@Callable 注解（类似 JPA）

- 事务支持
    - 支持 5 个事务隔离级别、7 个事务传播行为（与 Spring tx 相同）
    - 提供 TransactionTemplate、TransactionManager 接口方式声明式事务控制能力（用法与 Spring 相同）

- 特色优势
    - 支持 分页查询 并且提供多种数据库方言（20+）
    - 支持 INSERT 策略（INTO、UPDATE、IGNORE）
    - 更加丰富的 TypeHandler（MyBatis 40+，HasorDB 60+）
    - Mapper XML 支持多语句、多结果
    - 提供独特的规则机制，让动态 SQL 更加简单
    - 支持 存储过程
    - 支持 JDBC 4.2 和 Java8 中时间类型
    - 支持多数据源


同类工具
------------------------------------

**Hibernate**
诞生于 2001 年由 Gavin King 发布第一个版本。它是 ORM 领域的标志性工具，在此之前 ORM 实践均是通过 EJB 来完成。
Hibernate 的价值在于它终结了由 EJB 所主导的 ORM 使用习惯，并开创了以 轻量化ORM 和 SpringJDBC 的新生态。同时它推动了 EJB3、和 JPA 规范的建立。

- https://hibernate.org/


**SpringJDBC**
从 Spring 框架推出就存在于 Spring 体系之内至今如此。它比 Hibernate 更加轻量和敏捷，它独特的通过编码的方式将 SQL 和程序结合在一起，使用起来十分轻巧。
除此之外 SpringJDBC 是第一个提出了 7 种事务传播行为。

- https://spring.io/


**MyBatis**
是一款非常棒的数据库访问框架，它虽然不具备 Hibernate 强大的 ORM 能力。但别具风格的 Mapper 文件，完美的解决了动态 SQL 编写和管理上的难题。
本质上来讲 MyBatis 是 SpringJDBC 和 Hibernate 之间的一个折中方案。对于研发管理更加友好。

围绕 MyBatis 涌现出了 MyBatisPlus、MyBatis-Spring 等家喻户晓的工具，前者基于 MyBatis 进行了更多扩展的封装、后者整合了 Spring 提供更加友好的开发体验。

- https://blog.mybatis.org/


**ActiveRecord模式**
简称 AR。最早由 Rails 提出，也属于ORM范畴，它巧妙的将单个表看作单一的对象并提供 CURD 的基本功能，这使得数据库的开发变得十分轻松，在一些简单的 MVC 应用中更是得心应手。
虽然是 Ruby 社区的工具，但 ActiveRecord 在 Java 领域也有很多实现。比如 jOOQ，就是其中一种。

jOOQ 最有创造性的地方提供了特殊的 'DSL'，这组 'DSL' 其实是一种语法糖。结合 Java8 Lambda 语法，这种语法糖风格被很多后起之秀传承下来并发扬广大。
通过 jOOQ 的语法糖，可以编码的方式来编写 SQL。在感官上不亚于直接编写 SQL，这对于有数据库兼容需求的应用来说更加友好。

- https://rubyonrails.org/
- https://www.jooq.org/


**MyBatisPlus**
它来自于中国的一款开源工具，它基于 MyBatis 提供的一组更高级的封装和拓展。比如它提供了类似 jOOQ 的语法糖、分页查询。
其中 HasorDB 的 LambdaTemplate 就是参考了它的实现。

- https://baomidou.com/en/


其它有趣的工具
==============

**BeetlSQL**
来自于中国的一名叫 李家智 的程序员开发的数据库访问框架，和 MyBatisPlus 一样它也提供了 Lambda 方式生成和执行 SQL。HasorDB 的 LambdaTemplate 设计也同样参考和移植了它的理念。
较为遗憾的是对于 存储过程 没有像 Spring 和 MyBatis 一样提供直接支持，有这方面需求的需要依托更底层的 Connection 来实现。

这款框架最有趣的地方是 Mapper 文件的编写是基于 markdown 语法为骨干，动态 SQL 则采用 Beetl 模版语言来编写。作者还提供了 SQLTemplateEngine 接口可以方便更换 Mapper 语言引擎。

http://ibeetl.com/


**sqltoy-orm**
来自于中国的一款数据库操作框架，作者在这个框架中倾注了很多心思。与大多数 ORM 框架一样它也提供了很多相似的功能及特色的 Mapper 文件。
这款框架最别具一格的地方是对于动态 SQL 拼接上，巧妙的提供了一种新型语法可以极大的减少类似 MyBatis 中 if,foreach 标签。
受到 sqltoy-orm 灵感的触发 HasorDB 提供了规则机制，并允许用户自定义规则实现从而达成类似 sqltoy-orm 的效果。

另外最有趣的地方是 sqltoy-orm 中融入了很多业务场景的理解和抽象，例如：对结果集的行列转换处理、结果集聚合计算。这是这款框架最具有新意的地方。

https://github.com/sagframe/sagacity-sqltoy

