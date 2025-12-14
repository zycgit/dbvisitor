---
id: search
sidebar_position: 2
title: 2. 快速查找
description: 本文会略过 API 概述部分并以更加直观的形式按照使用场景对手册做分类检索。
---

# 快速查找

:::info[说明]
本文会略过 API 概述部分并以更加直观的形式按照使用场景对手册做分类检索。
:::

### 增、删、改
- 使用原始 [SQL 语句](./core/jdbc/update)。
- 使用注解 [@Insert](./core/annotation/insert)、[@Delete](./core/annotation/delete)、[@Update](./core/annotation/update)。
- 使用查询构造器 [新增](./core/lambda/insert)、[删除](./core/lambda/delete)、[更新](./core/lambda/update) 数据。
- 使用 [通用 Mapper](./core/mapper/common) 接口。
- 在 Mapper File 中使用 [&lt;insert&gt;](./core/file/sql_element#insert) 标签新增数据。
- 在 Mapper File 中通过 [&lt;selectKey&gt;](./core/file/sql_element#selectKey) 标签在插入数据时处理数据库自增 ID。
- 在 Mapper File 中使用 [&lt;update&gt;、&lt;delete&gt;](./core/file/sql_element#update_delete) 标签更新或删除数据。

### 基础查询
- 使用 SQL 语句 [查询结果集](./core/jdbc/query#list)、[查询对象](./core/jdbc/query#one)、[查询键值对](./core/jdbc/query#pairs)、[查询值/值列表](./core/jdbc/query#value)、[流式查询](./core/jdbc/query#stream)
- 执行语句块并 [接收多个结果集](./core/jdbc/multi)。
- 通过 [@Query](./core/annotation/query) 在接口上定义查询。
- 使用构造器 [查询单个对象](./core/lambda/query#object)、[查询列表](./core/lambda/query#list)、[查询总数](./core/lambda/query#count)。
- 使用构造器进行 [分组查询](./core/lambda/groupby)、[查询排序](./core/lambda/orderby)。
- 在 Mapper File 中使用 [&lt;select&gt;](./core/file/sql_element#select) 标签查询数据、使用 [&lt;sql&gt;](./core/file/sql_element#sql) 标签定义 SQL 片段。

### 参数传递
- 在动态 SQL 中利用 [SQL 注入](./args/inject) 进行参数传递（需要自行评估 SQL 注入安全风险）
- 使用 [位置编号](./args/position) 进行参数传递，还可以通过 [位置编号名称化](./args/position#pos_named) 传递参数。
- 使用 [名称化](./args/named) 方式参数传递，使用 [OGNL](./args/named#ognl) 对名称参数进一步取值。
- 使用 [SqlArgSource 接口](./args/interface#source) 或 [PreparedStatement 接口](./args/interface#pset) 传参。
- 通过 [规则](./args/rule) 在 SQL 语句中实现参数动态化。

### 分页查询
- 查询构造器中使用 [分页查询](./core/lambda/query#page)。
- 在 Mapper 接口中 @Query 注解方法通过增加分页参数实现 [分页查询](./core/annotation/query#page)。
- 在 [通用 Mapper](./core/mapper/common#page) 中进行分页查询（包含排序、Null值排序）
- 利用 Session 对象的 [queryStatement、pageStatement](./core/mapper/file#page) 重载方法进行分页查询。
- Mapper 接口在和 Mapper XML 文件建立关系后通过 [分页对象](./core/file/page) 进行分页查询。

### 动态 SQL
- 在 SQL 语句中通过 [MACRO 规则](./rules/macro_rule#macro) 注入预先定义的 SQL 片段（需要自行评估 SQL 注入安全风险）
- 在 SQL 语句中通过 [IFTEXT 规则](./rules/macro_rule#macro) 或 [`${...}`](./args/inject) 语法实现 SQL 注入（需要自行评估 SQL 注入安全风险）
- 通过 [AND](./rules/dynamic_rule#and)、[OR](./rules/dynamic_rule#or)、[SET](./rules/dynamic_rule#set) 规则增强 SQL 语句。
- 利用 [IN](./rules/dynamic_rule#in) 规则，可以自动根据集合参数的数量为 SQL 语句中生成对应的 `(?,?,?,?)`。
- 利用 [IFAND](./rules/dynamic_rule#and)、[IFOR](./rules/dynamic_rule#or)、[IFSET](./rules/dynamic_rule#set)、[IFIN](./rules/dynamic_rule#in) 规则，允许通过一个条件参数来控制规则是否有效。
- 规则还可以处理 [一段 SQL](/blog/rule_multiple_conditions) 而不仅仅是一个参数。
- 在 Mapper File 中使用 [&lt;if&gt;](./core/file/dynamic#if)、[&lt;choose&gt;、&lt;when&gt;、&lt;otherwise&gt;](./core/file/dynamic#choose) 标签进行条件判断。
- 在 Mapper File 中使用 [&lt;trim&gt;、&lt;where&gt;、&lt;set&gt;](./core/file/dynamic#trim) 标签增强特定 SQL 语句的生成。
- 在 Mapper File 中使用 [&lt;foreach&gt;](./core/file/dynamic#foreach) 标签处理循环需求。

### 对象映射
- 使用 [@Table 和 @Column](./core/mapping/basic) 注解进行对象映射。
- 使用 [驼峰命名法](./core/mapping/camel_case) 将属性自动映射到列上。
- 掌握当遇到 [名称大小写敏感、列名为关键字](./core/mapping/delimited) 时的映射技巧，可以帮您处理一些特殊问题。
- 通过映射时的 [写入策略](./core/mapping/write_policy) 可以控制在新增/修改数据时列是否参与到其中。
- 通过映射时的 [写入策略](./core/mapping/write_policy) 可以控制在新增/修改数据时列是否参与到其中。
- 通过指定 @Column 注解的 keyType 属性可以设定不同的 [主键生成器](./core/mapping/keytype) 策略。
- 在 Mapper File 中使用 [&lt;entity&gt;](./core/file/entity_map) 标签以 XML 方式描述对象映射可以避免代码的侵入。
- 在 Mapper File 中使用 [&lt;resultMap&gt;](./core/file/result_map) 和 &lt;entity&gt; 两个标签很接近，但不同的是它只能处理查询结果的映射。
- 在 Mapper File 中使用利用 [自动映射](./core/file/automapping) 机制可以极大的简化配置。
- 在使用构造器进行数据库操作时 [语句模版](./core/mapping/template) 可以决定生成的 SQL 的语句元素内容。

### 存储过程
- 使用 SQL 语句 [调用存储过程](./core/jdbc/procedure#exec)。
- 通过参数的 mode 选项将存储过程参数设置为 [OUT 类型](./core/jdbc/procedure#outp)。
- 将 mode 设置为 cursor 用来读取存储过程 [游标参数](./core/jdbc/procedure#outcur)。
- 使用 [@Call](./core/annotation/call) 执行存储过程。

### 执行 SQL
- SQL 语句的 [批量化](./core/jdbc/batch)。
- 加载一个 [SQL 脚本](./core/jdbc/execute) 文件。
- 通过 [@Execute](./core/annotation/execute) 注释执行任何类型的语句。
- 在 Mapper File 中使用 [&lt;execute&gt; 标签](./core/file/sql_element#execute) 执行任意的 SQL 语句。

### 结果集
- 在不同的 API 上使用 [List/Map](./result/for_map) 接收查询结果数据。
- 在不同的 API 上使用 [RowMapper](./result/for_mapper) 处理每一行结果集的映射。
  - 通过 [ColumnMapRowMapper](./result/for_mapper#inner) 将行转换为 Map，最终返回 List/Map 结构数据。
  - 通过 [SingleColumnRowMapper](./result/for_mapper#inner) 接收查询结果中只有一列的结果集，并将其转换为 List。
  - 通过 [BeanMappingRowMapper](./result/for_mapper#inner) 基于对象映射处理行数据，并将每一行数据都转换为 Bean。
  - 通过 [MapMappingRowMapper](./result/for_mapper#inner) 基于对象映射处理行数据，并将每一行数据都转换为 Map。
- 在不同的 API 上使用 [ResultSetExtractor](./result/for_extractor) 自定义 ResultSet 结果集的处理。
  - dbVisitor [内置了多种 不同的 ResultSetExtractor 实现](./result/for_extractor#inner) 事实上很多内部逻辑也都用到了它们。
- 在不同的 API 上使用 [RowCallbackHandler](./result/row_callback) 处理查询结果的每一条记录，而非获取它们。
  - 利用 RowCallbackHandler 实现 [MySQL 流式读取超大表](/blog/mysql_stream_read)。

### 类型处理
- 在 SQL 语句的参数中通过 [typeHandler 选项](./args/options#normal) 指定类型处理器。
- 在对象映射中通过 @Column 注解的 [typeHandler 选项](./core/mapping/type) 处理类型，如：抽象类型、枚举类型、JSON 序列化。
- dbVisitor 提供了大量实用的类型处理器，当遇到类型问题可以先看下已有类型处理器是否已经支持。
  - [布尔](./types/handlers/bool-handler)、[数字](./types/handlers/number-handler)、[字符/字符串](./types/handlers/string-handler)、[时间](./types/handlers/datetime-handler)、[字节数组](./types/handlers/bytes-handler)
- 枚举可以通过实现 [EnumOfValue](./types/enum-handler#ofvalue) 或者 [EnumOfCode](./types/enum-handler#ofcode) 接口将数据库中的 数值 或 特定 Code 作为和枚举的映射关系。
- 使用 [序列化处理器](./types/json-serialization) 可以自动识别您依赖中的 Fastjso2、Fastjson、Jackson、Gson 库，并按照这个顺序自动选择它们。
- 在依赖 JTS 后 dbVisitor 可以处理 [WKB 或 WKT](./types/gis-handler) 格式的地理信息数据。
- 对于 [InputStream/Reader 类型](./types/stream-handler) 或 [数组类型](./types/array-handler) dbVisitor 也有一定的支持。

### Redis 支持
- 了解 dbVisitor 对 Redis [支持的 140+ 命令](./drivers/redis/commands)。
- 简单了解 dbVisitor 如何操作 Redis 不同类型的数据（[字符串](./core/redis/redis_type#string)、[哈希](./core/redis/redis_type#hash)、
  [列表](./core/redis/redis_type#list)、[集合](./core/redis/redis_type#set)、[有序集合](./core/redis/redis_type#sorted_set)）
- 使用 JdbcTemplate [执行命令方式](./core/redis/exec_command) 读写 Redis 数据。
- 在 Mapper 接口上使用 @Insert、@Update、@Delete 注解，以 [注解方式](./core/redis/exec_annotation) 操作 Redis 数据。
- 在 [Mapper 文件](./core/redis/exec_file) 中通过标签配置执行命令。

### MongoDB 支持
- 了解 dbVisitor 对 MongoDB [支持的命令](./drivers/mongo/commands)。
- 使用 JdbcTemplate [执行命令方式](./core/mongo/exec_command) 读写 MongoDB 数据。
- 使用 [构造器方式](./core/mongo/exec_lambda) 读写 MongoDB 数据。
- 使用 [通用 Mapper 方式](./core/mongo/exec_mapper) 读写 MongoDB 数据。
- 在 Mapper 接口上使用 @Insert、@Update、@Delete 注解，以 [注解方式](./core/mongo/exec_annotation) 操作 MongoDB 数据。
- 在 [Mapper 文件](./core/mongo/exec_file) 中通过标签配置执行命令。


### 数据库事务
- 当项目是基于 Spring 技术构建时，通过 Spring 的 [事务注解](./yourproject/with_spring#tran) 完成事务控制。
- 当项目是基于 Solon 技术构建时，通过 Solon 的 [事务注解](./yourproject/with_solon#tran) 完成事务控制。
- 对于 Guice 和 Hasor 项目，可以使用 dbVisitor 的 [@Transactional 注解](./transaction/manager/annotation) 进行事务控制。
- 在没有任何字节码增强技术的应用程序中，可以利用 TransactionHelper 工具类将对象进行增强后在通过，[@Transactional 注解](./transaction/manager/annotation) 进行事务控制。
- 也可以通过 [Java Code 方式](./transaction/manager/program)、或者 [模版代码](./transaction/manager/template) 方式进行事务控制。

### 框架整合
- 利用 [dbvisitor-guice](./yourproject/with_guice) 在 Google Guice 中使用 dbVisitor。
- 利用 [dbvisitor-spring](./yourproject/with_spring) 在 Spring、SpringBoot 中使用 dbVisitor。
- 利用 [dbvisitor-solon](./yourproject/with_solon) 在 Solon 中使用 dbVisitor。
- 利用 [dbvisitor-hasor](./yourproject/with_hasor) 在 Hasor 中使用 dbVisitor。
