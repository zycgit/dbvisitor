## §2 能力矩阵

> 基于 2026-07-05 `run_nxn.sh all` 全量 Docker 实测。✅ 通过 ⚠️ 跳过（feature gate） ❌ 失败/错误 — 未实现（不含该数据源）
> 
> 不纳入 N×N 的数据源专有 DSL（Redis command/JDBC-DSL、MongoDB BSON/command、ES/Milvus 专有 DSL）不在此表。

| 测试说明 | 测试类 | H2 | MySQL | PG | Oracle | MSSQL | DB2 | CH | Redis | Mongo | ES6 | ES7 | Milvus |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **数据读写与变更能力** | | | | | | | | | | | | | |
| &emsp;基本 CRUD（INSERT/UPDATE/DELETE/SELECT）（纯 SQL） | `JdbcCrudContractTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;基本 CRUD（Lambda） | `LambdaCrudContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;CRUD + 条件 + 分页（BaseMapper） | `BaseMapperCrudContractTest(34条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | ✅ | ✅ | ✅ | ✅ |
| &emsp;复合主键 CRUD（BaseMapper） | `BaseMapperCompositeKeyContractTest(12条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;注解 CRUD（@Query/@Insert/etc）（注解 Mapper） | `AnnotationMapperCrudContractTest(7条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;XML CRUD（XML Mapper） | `XmlMapperCrudContractTest(12条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;映射 Map 模式 CRUD（Map 模式） | `MappedMapCrudContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;自由 Map 模式 CRUD（Map 模式） | `FreedomMapCrudContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;Map 参数操作（BaseMapper） | `BaseMapperMapOperationContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;Batch 批量操作（纯 SQL） | `JdbcBatchContractTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | — | — |
| &emsp;批量增删改（Lambda） | `LambdaBatchMutationContractTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;写入冲突策略（Ignore/Update）（Lambda） | `LambdaDuplicateStrategyContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | — | — |
| **查询与检索能力** | | | | | | | | | | | | | |
| &emsp;查询与结果集（纯 SQL） | `JdbcQueryContractTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;条件查询（Lambda） | `LambdaQueryContractTest(7条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | — | ✅ | ✅ | ✅ | ✅ |
| &emsp;谓词条件（IN/NOT IN/BETWEEN）（Lambda） | `LambdaPredicateContractTest(14条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;逻辑条件组合（Lambda） | `LambdaLogicalConditionContractTest(12条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;JOIN 查询（inner/left/right/self/cross）（纯 SQL） | `JdbcJoinQueryContractTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;联表查询（注解 Mapper） | `AnnotationMapperJoinQueryContractTest(7条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | — | — | — | — | — |
| &emsp;联表查询（XML Mapper） | `XmlMapperJoinQueryContractTest(6条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | — | — | — | — | — |
| &emsp;列选择（Lambda） | `LambdaSelectContractTest(6条)` | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | — | — | — | — | — |
| &emsp;排序（ASC/DESC/null 排序）（Lambda） | `LambdaSortContractTest(3条)` | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | — | — | — | — | — |
| &emsp;分页查询（Lambda） | `LambdaPaginationContractTest(11条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;迭代器/流式查询（Lambda） | `LambdaIteratorContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;KNN 向量排序（向量） | `VectorKnnOrderingContractTest(5条)` | ❌ | ❌ | ⚠️ | ❌ | ❌ | ❌ | ❌ | — | — | — | — | ✅ |
| &emsp;向量范围过滤（向量） | `VectorRangeFilteringContractTest(3条)` | ❌ | ❌ | ⚠️ | ❌ | ❌ | ❌ | ❌ | — | — | — | — | ✅ |
| &emsp;向量 + 标量混合查询（向量） | `VectorCombinedQueryContractTest(2条)` | ❌ | ❌ | ⚠️ | ❌ | ❌ | ❌ | ❌ | — | — | — | — | ✅ |
| **参数绑定与结果处理能力** | | | | | | | | | | | | | |
| &emsp;参数绑定（位置/命名/Map）（纯 SQL） | `JdbcParameterContractTest(10条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;参数绑定（注解 Mapper） | `AnnotationMapperParameterBindingContractTest(9条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | — | — | — | — | — |
| &emsp;结果处理器（纯 SQL） | `JdbcResultHandlingContractTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;结果转换（Lambda） | `LambdaResultHandlingContractTest(10条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;结果处理（注解 Mapper） | `AnnotationMapperResultHandlerContractTest(8条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | — | — | — | — | — |
| &emsp;结果处理（XML Mapper） | `XmlMapperResultHandlerContractTest(4条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | — | — | — | — | — |
| &emsp;结果映射（注解 Mapper） | `AnnotationMapperResultMappingContractTest(7条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | — | — | — | — | — |
| &emsp;结果映射（XML Mapper） | `XmlMapperResultMapContractTest(8条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | — | — | — | — | — |
| &emsp;空结果集处理（Lambda） | `LambdaEmptyResultContractTest(11条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;多结果集（纯 SQL） | `JdbcMultipleResultSetContractTest(4条)` | ❌ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | — | — | — | — | — |
| **SQL 与 Mapper 配置能力** | | | | | | | | | | | | | |
| &emsp;Mapper 注册与查找（Session） | `SessionMapperContractTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;Statement 执行（Session） | `SessionStatementContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;语句执行（BaseMapper） | `BaseMapperStatementContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;注解属性（useGeneratedKeys/keyColumn）（注解 Mapper） | `AnnotationMapperAttributeContractTest(10条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | — | — | — | — | — |
| &emsp;边界测试（注解 Mapper） | `AnnotationMapperEdgeContractTest(6条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | — | — | — | — | — |
| &emsp;主键生成与回填（selectKey/useGeneratedKeys）（XML Mapper） | `XmlMapperKeyGenerationContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | — | — |
| &emsp;SQL 片段（`<sql>` / `<include>`）（XML Mapper） | `XmlMapperSqlFragmentContractTest(4条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | — | — | — | — | — |
| &emsp;动态 SQL（`<if>` / `<choose>`）（XML Mapper） | `XmlMapperDynamicSqlContractTest(7条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | — | — | — | — | — |
| &emsp;动态规则引擎（XML Mapper） | `XmlMapperDynamicRuleContractTest(5条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | — | — | — | — | — |
| &emsp;statement 属性（XML Mapper） | `XmlMapperStatementAttributeContractTest(5条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | — | — | — | — | — |
| &emsp;引用外部 Mapper（XML Mapper） | `XmlRefMapperContractTest(5条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | — | — | — | — | — |
| &emsp;存储过程调用（XML Mapper） | `XmlMapperCallableContractTest(6条)` | ❌ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | — | — | — | — | — |
| **值、边界与安全能力** | | | | | | | | | | | | | |
| &emsp;边界条件（空表/null/极值）（Lambda） | `LambdaEdgeContractTest(9条)` | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | — | — | — | — | — |
| &emsp;安全值（注入防护）（Lambda） | `LambdaSecurityValueContractTest(9条)` | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | — | — | — | — | — |
| &emsp;特殊值（超长字符串/特殊字符）（Lambda） | `LambdaSpecialValueContractTest(8条)` | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ⚠️ | — | — | — | — | — |
| &emsp;自由 Map 模式标识符安全（Map 模式） | `FreedomMapIdentifierSecurityContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| **类型系统与对象映射能力** | | | | | | | | | | | | | |
| &emsp;基本类型（int/string/bool/date）（TypeHandler） | `BasicTypeJdbcContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;枚举类型映射（TypeHandler） | `EnumTypeJdbcContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;二进制类型（TypeHandler） | `BinaryTypeJdbcContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | — | — |
| &emsp;时间类型（TypeHandler） | `TimeTypeJdbcContractTest(13条)` | ⚠️ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | — | — |
| &emsp;JSON 序列化/反序列化（TypeHandler） | `JsonTypeJdbcContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;数组类型（TypeHandler） | `ArrayTypeJdbcContractTest(7条)` | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | — | — | — | — | — |
| &emsp;向量类型映射（TypeHandler） | `VectorTypeMappingContractTest(4条)` | ❌ | ❌ | ⚠️ | ❌ | ❌ | ❌ | ❌ | — | — | — | — | ✅ |
| &emsp;映射策略（@Table/@Column）（对象映射） | `AnnotationMappingPolicyContractTest(22条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | — | ✅ | ✅ | ✅ | ✅ |
| &emsp;表定义（对象映射） | `MappingTableDefinitionContractTest(6条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;表元数据（对象映射） | `AnnotationTableMetadataContractTest(4条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;XML 映射元数据（对象映射） | `XmlMappingMetadataContractTest(13条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;Mapping 注册表（对象映射） | `MappingRegistryContractTest(9条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | — | ✅ | ✅ | ✅ | ✅ |
| &emsp;继承映射（对象映射） | `AnnotationInheritanceContractTest(10条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;特殊类型映射（对象映射） | `AnnotationSpecialTypeContractTest(4条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;TypeHandler 注册（对象映射） | `AnnotationTypeHandlerContractTest(7条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;SQL 模板（对象映射） | `AnnotationSqlTemplateContractTest(9条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | — | — | — | — | — |
| **数据库方言与元数据能力** | | | | | | | | | | | | | |
| &emsp;自增键/序列键/UUID 键（主键） | `KeyGenerationContractTest(22条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | — | — |
| &emsp;方言生成键策略（主键） | `InsertDialectStrategyContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;命名策略（驼峰/下划线/分隔符）（Schema） | `NamingMappingContractTest(22条)` | ⚠️ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | — | — | — | — | — |
| &emsp;标准 Schema（schema.table）（Schema） | `StandardSchemaContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| **事务与数据库过程能力** | | | | | | | | | | | | | |
| &emsp;事务提交/回滚/传播/隔离（事务） | `TransactionContractTest(40条)` | ✅ | ✅ | ✅ | ⚠️ | ⚠️ | ✅ | ❌ | — | — | — | — | — |
| &emsp;存储过程（过程/函数） | `ProcedureContractTest(8条)` | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | — | — | — | — | — |
| &emsp;函数调用（过程/函数） | `FunctionContractTest(7条)` | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | — | — | — | — | — |
| **Session 生命周期能力** | | | | | | | | | | | | | |
| &emsp;Session 生命周期（Session） | `SessionCoreContractTest(11条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |

### 备注

- **Oracle** ❌ 批量：Annotation/XML Mapper 主路径、Mapping 元数据等因大量 feature gate 累积导致整体 ❌
- **MSSQL** ❌ 批量：Lambda 排序/选择/安全值因 REPEATED_ORDER_BY_COLUMN 等 gate
- **ClickHouse** ❌ 批量：16 项 OLAP 差异导致（无事务/无约束/无自增/无过程/无序列）
- **H2** ❌ 项：procedure、多结果集、vector、callable 等 12 gate
- **MySQL** ❌ 项：Array、序列、KNN、cursor、table function 等 10 gate
- **PG** ⚠️ 项：仅 procedure-result-set（refcursor 替代）、vector 部分跳过
- **Redis/Mongo/ES/Milvus** — 项：不参与 RDBMS 特有测试（JDBC/Batch/Join/Transaction 等）

## §3 跳过清单总览

当前所有已接入 N×N 的数据源均无 failure/error。后续改进重点不是修复当前失败，而是逐项减少显式 feature gate。

| 数据源 | 跳过数 | 跳过 feature |
| --- | ---: | --- |
| H2 | 55 | `json`, `knn`, `procedure`, `xml-mapper-callable`, `function-call-callback`, `vector`, `delimited-lowercase-standard-table`, `xml-select-key-user-info-sequence`, `postgres-on-conflict`, `time-extreme-date`, `case-sensitive-identifiers`, `multiple-result-sets` |
| MySQL | 33 | `array`, `sequence`, `knn`, `procedure-cursor-result`, `function-record-result`, `function-table-result`, `vector`, `xml-select-key-user-info-sequence`, `postgres-on-conflict` |
| PostgreSQL | 1 | `procedure-result-set` |
| Oracle | 64 | `array`, `json`, `sequence`, `xml-select-key-user-info-sequence`, `knn`, `procedure-result-set`, `vector`, `postgres-on-conflict`, `sql-md5-function`, `generated-keys-numeric`, `keygen-auto-batch-explicit-null`, `delimited-lowercase-standard-table`, `distinct-empty-string`, `large-in-list`, `xml-foreach-batch-insert-values`, `lowercase-standard-result-columns`, `transaction-release-savepoint`, `transaction-repeatable-read`, `multiple-result-sets` |
| MSSQL | 60 | `array`, `json`, `sequence`, `xml-select-key-user-info-sequence`, `knn`, `procedure-cursor-result`, `vector`, `postgres-on-conflict`, `sql-md5-function`, `keygen-auto-batch-explicit-null`, `generated-keys-numeric`, `repeated-order-by-column`, `transaction-release-savepoint`, `case-sensitive-identifiers` |
| DB2 | 50 | `array`, `json`, `knn`, `procedure-cursor-result`, `vector`, `xml-select-key-user-info-sequence`, `postgres-on-conflict`, `bit-cast-null-value`, `sql-md5-function`, `delimited-lowercase-standard-table`, `case-sensitive-identifiers`, `multiple-result-sets` |
| ClickHouse | 144 | `array`, `json`, `binary`, `sequence`, `generated-key-column`, `xml-select-key-user-info-sequence`, `knn`, `procedure`, `xml-mapper-callable`, `function-call-callback`, `function-record-result`, `function-table-result`, `postgres-on-conflict`, `duplicate-key-strategy`, `duplicate-primary-key-rejected`, `batch-duplicate-failure-propagated`, `exact-mutation-affected-rows`, `length-limit-enforced`, `non-null-primary-key-rejected`, `empty-where-mutation`, `transaction`, `join-non-equi-condition`, `left-join-null-values`, `sql-not-in-null-semantics`, `bit-cast-null-value`, `time-extreme-date`, `sql-md5-function`, `keygen-auto-batch-explicit-null`, `generated-keys-numeric`, `transaction-release-savepoint`, `transaction-repeatable-read`, `case-sensitive-identifiers`, `multiple-result-sets` |
| Redis | 0 | 无；但通用 Lambda CRUD 不纳入当前 Redis 目标 |
| Mongo | 0 | 无 |
| ES6 | 0 | 无 |
| ES7 | 0 | NXN 专有测试无 skip；旧配置层仍有 `pagination`, `transaction`, `procedure`, `testTableAnnotation`，后续可评估是否清理或转为 adapter 专有解释 |
| Milvus | 0 | 无；旧配置层仍有 `testTableAnnotation` case skip |

## §4 当前结论

| 数据源 | 当前结论 |
| --- | --- |
| H2 | `635 / 0 fail / 0 error / 55 skip`；剩余主要是多结果集、procedure/XML callable、JSON/KNN/vector、极端时间和大小写标识符差异 |
| MySQL | `635 / 0 fail / 0 error / 33 skip`；剩余为 array/sequence/KNN/vector、cursor/table function 等差异 |
| PostgreSQL | `635 / 0 fail / 0 error / 1 skip`；当前覆盖最完整，仅 `procedure-result-set` 保留 gate，refcursor 场景已覆盖 |
| Oracle | `635 / 0 fail / 0 error / 64 skip`；剩余包括默认 numeric generated keys、direct procedure ResultSet、多结果集、空字符串 NULL、IN 1000 和事务隔离差异 |
| MSSQL | `635 / 0 fail / 0 error / 60 skip`；剩余包括 array/json/sequence、cursor、SQL MD5、numeric generated keys、重复 ORDER BY 同列和事务 savepoint 差异 |
| DB2 | `635 / 0 fail / 0 error / 50 skip`；剩余包括多结果集、array/json/vector、cursor 和 BIT/大小写标识符差异 |
| ClickHouse | `636 / 0 fail / 0 error / 144 skip`；剩余主要是 OLAP 语义差异、事务、约束、mutation 行数、非空复杂类型、callable/procedure 和多结果集 |
| Redis | `25 / 0 fail / 0 error / 0 skip`；作为特殊 adapter，只统计 command、JDBC-style DSL、Mapper 和类型操作 |
| Mongo | `18 / 0 fail / 0 error / 0 skip`；Mongo 专有 command/JDBC-style DSL、BSON、Lambda、BaseMapper、Mapper XML/Annotation 均在数据源专有能力内表达 |
| Elastic6 / Elastic7 | 各 `16 / 0 fail / 0 error / 0 skip`；ES6、ES7 独立保留各自专有 XML/DSL，不上提到通用 contract |
| Milvus | `17 / 0 fail / 0 error / 0 skip`；Milvus 专有 JDBC-style DSL、向量类型、索引/load/search、Mapper XML 均在数据源专有能力内表达 |

## §5 下一步改进方向

优先级按“关系型通用能力应尽量接近”和“先 JDBC 原始 DSL，后 Lambda CRUD”的原则排序。

### P0：先保持不动的差异

以下项目已经被证明是数据库语义或驱动能力差异，短期不建议为了减少 skip 而弱化 contract。

| 项目 | 数据源 | 原因 |
| --- | --- | --- |
| `multiple-result-sets` | H2、Oracle、DB2、ClickHouse | 单个 prepared statement 执行多条分号 SQL 的驱动支持不一致；不能由 dbVisitor 模拟拆 SQL 代替真实能力 |
| `procedure-result-set` | PostgreSQL、Oracle | PostgreSQL 已用 refcursor 覆盖 cursor 场景；Oracle 已用 `SYS_REFCURSOR` 覆盖，direct procedure ResultSet 不等价 |
| `procedure-cursor-result` | MySQL、MSSQL、DB2 | 普通 procedure/result-set 已打开；OUT cursor/refcursor 模型不等价 |
| `distinct-empty-string` | Oracle | Oracle 空字符串即 NULL，是数据库语义 |
| `large-in-list` | Oracle | Oracle `IN` 列表 1000 限制 |
| `transaction-repeatable-read` | Oracle、ClickHouse | 数据库事务隔离模型差异 |
| `transaction` | ClickHouse | ClickHouse 非传统 OLTP 事务模型 |
| `exact-mutation-affected-rows` | ClickHouse | mutation affected rows 语义不同 |

### P1：最值得继续尝试打开的关系型能力

| 方向 | 影响数据源 | 建议动作 |
| --- | --- | --- |
| `sql-md5-function` | Oracle、MSSQL、DB2、ClickHouse | 为各数据源下沉专有 MD5 表达式或替代函数，避免通用 SQL 写死某一方言 |
| `xml-select-key-user-info-sequence` | H2、MySQL、Oracle、MSSQL、DB2、ClickHouse | 逐个阅读 selectKey contract；能等价提供 sequence/default key 查询的，补充 `realdb/{env}` 专有 SQL |
| `generated-keys-numeric` | Oracle、MSSQL、ClickHouse | 拆分“无 keyColumn 默认数字主键回填”与“需要显式 keyColumn”场景 |
| `keygen-auto-batch-explicit-null` | Oracle、MSSQL、ClickHouse | 审核批量插入时显式 NULL 主键与 generated keys 回填是否可以按数据源分支实现 |
| `case-sensitive-identifiers` | H2、MSSQL、DB2、ClickHouse | 逐一确认 quoted/mixed-case 元数据和结果列行为；能通过物料解决的下沉到 realdb |
| `delimited-lowercase-standard-table` | H2、Oracle、DB2 | 检查标准小写表名加引号后的元数据行为，区分产品问题和数据库大小写语义 |

### P2：类型系统能力提升

| 方向 | 影响数据源 | 建议动作 |
| --- | --- | --- |
| `array` | MySQL、Oracle、MSSQL、DB2、ClickHouse | 现有 `type.array.null` 已打开；下一步只评估非空数组序列化/读取是否有真实等价类型或专有承载 |
| `json` | H2、Oracle、MSSQL、DB2、ClickHouse | 现有 `type.json.null` 已打开；后续按 JSON 原生类型、文本承载和函数能力拆分 |
| `binary` | ClickHouse | `type.binary.null` 已打开；非空 byte/blob 保真仍需评估 ClickHouse JDBC 字节承载问题 |
| `bit-cast-null-value` | DB2、ClickHouse | 只影响 `CAST(NULL AS BIT)`/BIT 空值语义；不要误判为普通 boolean 不支持 |
| `time-extreme-date` | H2、ClickHouse | 极端日期边界可单独评估，避免影响普通时间类型通过状态 |

### P3：过程/函数能力补齐

| 方向 | 影响数据源 | 建议动作 |
| --- | --- | --- |
| `procedure` | H2、ClickHouse | H2 Java alias 与当前 OUT/INOUT contract 不等价；ClickHouse JDBC 不支持 prepareCall，优先保留 gate |
| `xml-mapper-callable` | H2、ClickHouse | H2/ClickHouse callable 模型不等价；不要用函数返回值伪装 OUT 参数 |
| `function-call-callback` | H2、ClickHouse | H2 `supportsStoredProcedures()` 不稳定；ClickHouse prepareCall 不支持 |
| `function-record-result` / `function-table-result` | MySQL、ClickHouse | MySQL 函数/过程结果模型与 PostgreSQL/DB2/MSSQL table function 不同；ClickHouse 不走 callable 函数模型 |

### P4：ClickHouse 专项提升

ClickHouse skip 最多，但多数不是 dbVisitor bug，而是 OLAP 数据库语义差异。后续建议按 ClickHouse 专有能力补强，而不是强行对齐关系型 OLTP contract。

| 方向 | 建议 |
| --- | --- |
| 约束/重复键 | `duplicate-key-strategy`、`duplicate-primary-key-rejected`、`batch-duplicate-failure-propagated` 可设计 ClickHouse 专有 contract |
| mutation 行数 | `exact-mutation-affected-rows` 应保留或改为 ClickHouse 专有断言 |
| 空 WHERE mutation | `empty-where-mutation` 应按 ClickHouse mutation 安全边界单独定义 |
| JOIN 语义 | 已有 `join_use_nulls` 专有 DSL，通过后可继续扩展 ClickHouse 专有 JOIN 能力 |
| 事务 | `transaction`、savepoint、repeatable-read 建议保留不支持 |

## §6 使用方式

后续减少 skip 时，建议每次只选择一个 feature：

1. 阅读该 feature 对应的 contract 断言。
2. 判断目标数据源是否能提供等价语义。
3. 若可以支持，SQL、DDL、XML mapper 和 fixture 必须放入 `realdb/{env}`。
4. 移除对应 `DataSourceProfile` 和 `jdbc-*.properties` 中的 skip。
5. 跑目标数据源代表类，再跑目标数据源全量。
6. 更新本文档；新数据源接入流程同步更新 `NXN_DS_GUIDE.md`。
