---
id: search
sidebar_position: 2
title: 2. Quick Lookup
description: This page skips the API overview and organizes links by usage scenarios for quick lookup.
---

# Quick Lookup

:::info[Note]
This page skips the API overview and organizes links by usage scenarios for quick lookup.
:::

### Insert,Delete,Update
- Use raw [SQL Statements](./core/jdbc/update).
- Use annotations [@Insert](./core/mapper/annotation_insert), [@Delete](./core/mapper/annotation_delete), [@Update](./core/mapper/annotation_update).
- Use the Fluent API to [Insert](./core/lambda/insert), [Delete](./core/lambda/delete), [Update](./core/lambda/update).
- Use the [Common Mapper](./core/mapper/about#base-mapper) interface.
- In a Mapper file, use [&lt;insert&gt;](./core/file/statements#insert) to insert data.
- In a Mapper file, use [&lt;selectKey&gt;](./core/file/statements#selectKey) to handle auto-increment IDs during insert.
- In a Mapper file, use [&lt;update&gt; / &lt;delete&gt;](./core/file/statements#update_delete) to update or delete data.

### Basic Query
- Use SQL statements to [Query Result Set](./core/jdbc/query#list), [Query Object](./core/jdbc/query#one), [Query Key-Value Pairs](./core/jdbc/query#pairs), [Query Value/List](./core/jdbc/query#value), [Stream Query](./core/jdbc/query#stream)
- Execute statements and [Multiple ResultSet](./core/jdbc/multiple_results).
- Define queries on interfaces with [@Query](./core/mapper/annotation_query).
- Use the Fluent API to [Fetch one object](./core/lambda/query#object), [List](./core/lambda/query#list), [Count](./core/lambda/query#count).
- Use the Fluent API for [Group by](./core/lambda/group_by) and [Order by](./core/lambda/order_by).
- In a Mapper file, use [&lt;select&gt;](./core/file/statements#select) to query and [&lt;sql&gt;](./core/file/statements#sql) to define fragments.

### Arguments
- In dynamic SQL, pass parameters via [SQL text substitution](./args/inject) (assess injection risks yourself).
- Pass parameters by [Positional](./args/position) or using [Named positional](./args/position#pos_named).
- Pass parameters by [Named](./args/named) and further access values with [OGNL](./args/named#ognl).
- Use [SqlArgSource](./args/interface#source) or [PreparedStatement](./args/interface#pset) for parameters.
- Use [Rules](./args/rule) to make parameters dynamic in SQL.

### Pagination
- Use [pagination](./core/lambda/query#page) in the Fluent API.
- In Mapper interfaces, add paging params to @Query methods for [pagination](./core/mapper/annotation_query#page).
- Paginate via the [Common Mapper](./core/mapper/about#base-mapper) (includes sorting and null ordering).
- Use Session [queryStatement / pageStatement](./core/mapper/file_statement#page) overloads for pagination.
- After binding Mapper interfaces to XML, use a [Page object](./core/file/paging) for pagination.

### Dynamic SQL
- Rules Manual: [Dynamic SQL Rules](./rules/dynamic_rule), [Nested Rules](./rules/nested_rule), [Result Processing](./rules/result_rule), etc.
- Inject predefined SQL fragments via [MACRO rules](./rules/dynamic_rule#macro) in SQL statements (assess SQL injection security risks yourself).
- Build SQL text via [IFTEXT rules](./rules/dynamic_rule#if) or [`${...}`](./args/inject) syntax in SQL statements (assess SQL injection security risks yourself).
- Enhance SQL statements via [AND](./rules/dynamic_rule#and), [OR](./rules/dynamic_rule#or), [SET](./rules/dynamic_rule#set), [CASE](./rules/dynamic_rule#case) rules.
- Use [IN](./rules/dynamic_rule#in) rule to automatically generate corresponding `(?,?,?,?)` in SQL statements based on collection parameter size.
- Use [IFAND](./rules/dynamic_rule#and), [IFOR](./rules/dynamic_rule#or), [IFSET](./rules/dynamic_rule#set), [IFIN](./rules/dynamic_rule#in) rules to control rule validity via a condition parameter.
- Rules can also process [a segment of SQL](/blog/rule_multiple_conditions) not just a parameter.
- Nested rules: `@{case, ... @{and, ...}}` enable logical processing capabilities in SQL via nesting.
- Use [&lt;if&gt;](./core/file/dynamic_sql#if), [&lt;choose&gt;, &lt;when&gt;, &lt;otherwise&gt;](./core/file/dynamic_sql#choose) tags in Mapper File for conditional judgment.
- Use [&lt;trim&gt;, &lt;where&gt;, &lt;set&gt;](./core/file/dynamic_sql#trim) tags in Mapper File to enhance specific SQL statement generation.
- Use [&lt;foreach&gt;](./core/file/dynamic_sql#foreach) tag in Mapper File to handle loop requirements.

### Object mapping
- Use [@Table and @Column](./core/mapping/table) annotations for mapping.
- Use [camel case](./core/mapping/camel_case) to auto-map properties to columns.
- Handle [case sensitivity and keyword columns](./core/mapping/name_sensitivity).
- Control column participation via [write policy](./core/mapping/write_policy) (insert/update).
- Choose primary key strategies via @Column `keyType` ([Key Generators](./core/mapping/key_generator)).
- In Mapper files, use [&lt;entity&gt;](./core/file/entity_map) to describe mappings in XML.
- In Mapper files, [&lt;resultMap&gt;](./core/file/result_map) is similar to &lt;entity&gt; but only for query result mapping.
- Use [auto-mapping](./core/file/auto_mapping) to simplify configuration.
- With the builder, [Statement Templates](./core/mapping/statement_template) decide generated SQL elements.

### Stored procedures
- Use SQL to [Stored Procedures Call](./core/jdbc/procedure#exec).
- Set parameter mode to [OUT](./core/jdbc/procedure#outp).
- Use mode `cursor` to read [cursor arguments](./core/jdbc/procedure#outcur).
- Use [@Call](./core/mapper/annotation_call) to execute procedures.

### Executing SQL
- [Batches](./core/jdbc/batch) SQL statements.
- Load a [Scripts](./core/jdbc/execute) file.
- Execute any statement with [@Execute](./core/mapper/annotation_execute).
- In Mapper files, use [&lt;execute&gt;](./core/file/statements#execute) for arbitrary SQL.

### Results
- Use [List/Map](./result/for_map) across APIs to collect query results.
- Use [RowMapper](./result/for_mapper) to map each row:
  - [ColumnMapRowMapper](./result/for_mapper#inner) converts rows to Map and returns List/Map.
  - [SingleColumnRowMapper](./result/for_mapper#inner) handles single-column results into a List.
  - [BeanMappingRowMapper](./result/for_mapper#inner) maps rows to beans.
  - [MapMappingRowMapper](./result/for_mapper#inner) maps rows to maps.
- Use [ResultSetExtractor](./result/for_extractor) to customize ResultSet handling.
  - dbVisitor [ResultSetExtractor implementations](./result/for_extractor#inner) used internally.
- Use [RowCallbackHandler](./result/row_callback) to process rows without collecting them.
  - Example: [MySQL streaming large tables](/blog/mysql_stream_read).

### Type Handler
- Specify a type handler via the [TypeHandler option](./args/options#normal) in SQL parameters.
- Use the @Column [TypeHandler option](./core/mapping/type_mapping) for abstract types, enums, JSON serialization.
- dbVisitor provides many handlers; check built-ins first:
  - [Boolean](./types/handlers/bool-handler), [Numeric](./types/handlers/number-handler), [Char/String](./types/handlers/string-handler), [Datetime](./types/handlers/datetime-handler), [Byte[]](./types/handlers/bytes-handler)
- Map enums via [EnumOfValue](./types/enum-handler#ofvalue) or [EnumOfCode](./types/enum-handler#ofcode).
- The [serialization](./types/json-serialization) auto-detects Jackson, Gson, Fastjson, Fastjson2 in that order.
- With JTS on the classpath, dbVisitor can handle [WKB/WKT](./types/gis-handler) geospatial data.
- dbVisitor also supports [InputStream/Reader](./types/stream-handler) and [Array](./types/array-handler) types.

### Redis support
- See the 140+ Redis commands supported by dbVisitor: [Commands](../drivers/redis/commands).
- Learn how dbVisitor handles Redis data types ([String](../features/redis/usage#string), [Hash](../features/redis/usage#hash),
  [List](../features/redis/usage#list), [Set](../features/redis/usage#set), [Sorted Set](../features/redis/usage#sorted_set)).
- Use JdbcTemplate to [Execute commands](../features/redis/usage#exec-command).
- Use @Insert, @Update, @Delete on Mapper interfaces for [Annotation-driven](../features/redis/usage#exec-annotation) Redis operations.
- Configure commands via tags in [Mapper File](../features/redis/usage#exec-file).

### MongoDB support
- See MongoDB commands supported by dbVisitor: [Commands](../drivers/mongo/commands).
- Use JdbcTemplate to [Execute commands](../features/mongo/usage#exec-command).
- Use [Fluent API](../features/mongo/usage#exec-lambda).
- Use [Common Mapper](../features/mongo/usage#exec-mapper).
- On Mapper interfaces, use @Insert, @Update, @Delete for [Annotation-driven](../features/mongo/usage#exec-annotation) operations.
- Configure commands via tags in [Mapper File](../features/mongo/usage#exec-file).

### ElasticSearch support
- See ElasticSearch commands supported by dbVisitor: [Commands](../drivers/elastic/commands).
- Use JdbcTemplate to [Execute commands](../features/elastic/usage#exec-command).
- Use [Fluent API](../features/elastic/usage#exec-lambda).
- Use [Common Mapper](../features/elastic/usage#exec-mapper).
- On Mapper interfaces, use @Insert, @Update, @Delete for [Annotation-driven](../features/elastic/usage#exec-annotation) operations.
- Configure commands via tags in [Mapper File](../features/elastic/usage#exec-file).

### Database transactions
- Spring projects: use [Spring Annotations](./yourproject/with_spring#tran).
- Solon projects: use [Solon Annotations](./yourproject/with_solon#tran).
- Guice and Hasor: use dbVisitor [@Transactional](./transaction/annotation).
- In plain Java programs without framework proxies, create a proxy with `TransactionHelper.support()` and then use [@Transactional](./transaction/annotation).
- To wrap a local code block, use [transaction templates](./transaction/template).
- To control `begin/commit/rollBack` manually, use [programmatic transactions](./transaction/program).

### Framework integrations
- Use [dbvisitor-guice](./yourproject/with_guice) with Google Guice.
- Use [dbvisitor-spring](./yourproject/with_spring) with Spring / Spring Boot.
- Use [dbvisitor-solon](./yourproject/with_solon) with Solon.
- Use [dbvisitor-hasor](./yourproject/with_hasor) with Hasor.
