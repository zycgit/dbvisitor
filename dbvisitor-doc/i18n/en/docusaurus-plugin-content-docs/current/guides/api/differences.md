---
id: differences
sidebar_position: 6
hide_table_of_contents: true
title: 4.6 Datasource Differences
description: Introduces the differences between relational and non-relational databases in usage.
---

# Datasource Differences

dbVisitor strives to use a unified API to operate all **Relational Databases** and **Non-Relational Databases**. However, in practice, data sources still have individual differences due to their own characteristics.
The way dbVisitor handles differences is mainly reflected in two aspects:
- **API Support**, refers to whether the called API is supported on a certain data source.
- **Database Dialect**, refers to the use of different commands or syntax when operating different databases with the same API when using [Fluent API](./lambda_api).

:::tip[Feature]
If you want to modify these differences, you can participate in the project and contribute your improved code.
:::

## API Support {#api}

dbVisitor's API designed for usage under a unified kernel architecture is mainly divided into 4 types: [Programmatic API](./program_api), [Declarative API](./declarative_api), [Fluent API](./lambda_api), [Mapper File](./file_mapper).

For **Relational Databases** based on SQL language, all APIs provided by dbVisitor can be used. For specific database differences, refer to the description in [Database Dialect](./differences#dialect) in this article.

For **Non-Relational Databases**, you can learn more about the differences through the following guidelines:
- [Redis Datasource Specifics](../core/redis/about)
- [MongoDB Datasource Specifics](../core/mongo/about)
- [ElasticSearch Datasource Specifics](../core/elastic/about)

:::info[JDBC Feature Support]
For non-relational database drivers (Mongo, Elastic), dbVisitor implements the `Statement.RETURN_GENERATED_KEYS` feature.
This means that when using `JdbcTemplate` or `Statement` to execute an insert operation, you can automatically get the generated `_id`.
:::

## Database Dialect {#dialect}

dbVisitor has intelligent dialect inference capabilities. When creating data operation interfaces, it automatically identifies the target database type based on the JDBC URL and configures the best dialect. Therefore, for the databases listed in the table above, you usually **do not need to perform any manual configuration**.

If your application scenario is special (e.g., using an unsupported database, middleware proxying the JDBC URL), you can explicitly specify the dialect through configuration. Specifying the dialect supports **Dialect Alias** (e.g., `mysql`) or **Dialect Fully Qualified Class Name**.

The following table lists the functional support differences of dbVisitor's built-in database dialects for the [Fluent API](./lambda_api).

| Config Key | Database | Pagination | Conflict Strategy | Null Sort Strategy | Sequence |
|---|---|:---:|:---|---|---|
| db2 | DB2 | Supported | Into | | |
| derby | Apache Derby | Supported | Into | | |
| dm | Dameng | Supported | Into, Ignore (Table needs Primary Key) | | |
| h2 | H2 | Supported | Into | | Supported |
| hive | Hive | Supported | Into | | |
| hsql | HSQL | Supported | Into | | |
| impala | Apache Impala | Supported | Into | | |
| informix | IBM Informix | Supported | Into | | |
| kingbase | Kingbase | Supported | Into | | |
| mariadb | MariaDB | Supported | Into | | |
| mysql | MySQL | Supported | Into, Update, Ignore | Supported | |
| oracle | Oracle | Supported | Into, Update, Ignore (Table needs Primary Key) | | |
| postgresql | PostgreSQL | Supported | Into, Update, Ignore | | |
| sqlite | SQLite | Supported | Into | | |
| sqlserver | SQL SERVER | Supported | Into | | |
| xugu | Xugu DB | Supported | Into | | |
| mongo | MongoDB | Supported | Into | | |
| elastic6 | ElasticSearch 6 | Supported | Into | | |
| elastic7 | ElasticSearch 7+ | Supported | Into | | |
