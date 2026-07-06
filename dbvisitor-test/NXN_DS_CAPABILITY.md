## §2 能力矩阵

> 基于 2026-07-06 按 `run_nxn.sh all` 数据源顺序逐个真实运行。每个数据源执行 `mvn -Pnxn -Dnxn.env={env} test`，Surefire XML 已归档到 `target/nxn-runs/{env}/surefire-reports`。✅ 通过 ⚠️ 跳过（feature gate） ❌ 失败/错误 — 未实现（不含该数据源）
> 
> 不纳入 N×N 的数据源专有 DSL（Redis command/JDBC-DSL、MongoDB BSON/command、ES/Milvus 专有 DSL）不在此表。

| 测试说明 | 测试类 | H2 | MySQL | PG | Oracle | MSSQL | DB2 | CH | Redis | Mongo | ES6 | ES7 | Milvus |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **数据读写与变更能力** | | | | | | | | | | | | | |
| &emsp;基本 CRUD（INSERT/UPDATE/DELETE/SELECT）（纯 SQL） | `JdbcCrudTest(6条)` | ⚠️ | ⚠️ | ✅ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | — | — | — | — | — |
| &emsp;基本 CRUD（Lambda） | `LambdaCrudTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;CRUD + 条件 + 分页（BaseMapper） | `BaseMapperCrudContractTest(34条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | ✅ | ✅ | ✅ | ✅ |
| &emsp;复合主键 CRUD（BaseMapper） | `BaseMapperCompositeKeyContractTest(12条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;注解 CRUD（@Query/@Insert/etc）（注解 Mapper） | `AnnotationMapperCrudContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;XML CRUD（XML Mapper） | `XmlMapperCrudContractTest(12条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;映射 Map 模式 CRUD（Map 模式） | `MappedMapCrudTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;自由 Map 模式 CRUD（Map 模式） | `FreedomMapCrudTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;Map 参数操作（BaseMapper） | `BaseMapperMapOperationContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;Batch 批量操作（纯 SQL） | `JdbcBatchTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;批量增删改（Lambda） | `LambdaBatchMutationContractTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;写入冲突策略（Ignore/Update）（Lambda） | `LambdaDuplicateStrategyContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| **查询与检索能力** | | | | | | | | | | | | | |
| &emsp;查询与结果集（纯 SQL） | `JdbcQueryTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;条件查询（Lambda） | `LambdaQueryTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | ✅ | ✅ | ✅ | ✅ |
| &emsp;谓词条件（IN/NOT IN/BETWEEN）（Lambda） | `LambdaPredicateContractTest(14条)` | ✅ | ✅ | ✅ | ⚠️ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;逻辑条件组合（Lambda） | `LambdaLogicalConditionContractTest(12条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;JOIN 查询（inner/left/right/self/cross）（纯 SQL） | `JdbcJoinQueryContractTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;联表查询（注解 Mapper） | `AnnotationMapperJoinQueryContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;联表查询（XML Mapper） | `XmlMapperJoinQueryContractTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;列选择（Lambda） | `LambdaSelectContractTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;排序（ASC/DESC/null 排序）（Lambda） | `LambdaSortTest(3条)` | ✅ | ✅ | ✅ | ✅ | ⚠️ | ✅ | ✅ | — | — | — | — | — |
| &emsp;分页查询（Lambda） | `LambdaPaginationContractTest(11条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;迭代器/流式查询（Lambda） | `LambdaIteratorContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;KNN 向量排序（向量） | `VectorKnnOrderingContractTest(5条)` | ⚠️ | ⚠️ | ✅ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | — | — | — | — | ✅ |
| &emsp;向量范围过滤（向量） | `VectorRangeFilteringContractTest(3条)` | ⚠️ | ⚠️ | ✅ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | — | — | — | — | ✅ |
| &emsp;向量 + 标量混合查询（向量） | `VectorCombinedQueryContractTest(2条)` | ⚠️ | ⚠️ | ✅ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | — | — | — | — | ✅ |
| **参数绑定与结果处理能力** | | | | | | | | | | | | | |
| &emsp;参数绑定（位置/命名/Map）（纯 SQL） | `JdbcParameterContractTest(10条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;参数绑定（注解 Mapper） | `AnnotationMapperParameterBindingContractTest(9条)` | ✅ | ✅ | ✅ | ⚠️ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;结果处理器（纯 SQL） | `JdbcResultHandlingContractTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;结果转换（Lambda） | `LambdaResultHandlingContractTest(10条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;结果处理（注解 Mapper） | `AnnotationMapperResultHandlerContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;结果处理（XML Mapper） | `XmlMapperResultHandlerContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;结果映射（注解 Mapper） | `AnnotationMapperResultMappingContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;结果映射（XML Mapper） | `XmlMapperResultMapContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;空结果集处理（Lambda） | `LambdaEmptyResultContractTest(11条)` | ✅ | ✅ | ✅ | ⚠️ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;多结果集（纯 SQL） | `JdbcMultipleResultSetContractTest(4条)` | ⚠️ | ✅ | ✅ | ⚠️ | ✅ | ⚠️ | ⚠️ | — | — | — | — | — |
| **SQL 与 Mapper 配置能力** | | | | | | | | | | | | | |
| &emsp;Mapper 注册与查找（Session） | `SessionMapperContractTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;Statement 执行（Session） | `SessionStatementContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;语句执行（BaseMapper） | `BaseMapperStatementContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;注解属性（useGeneratedKeys/keyColumn）（注解 Mapper） | `AnnotationMapperAttributeContractTest(10条)` | ⚠️ | ⚠️ | ⚠️ | ⚠️ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;边界测试（注解 Mapper） | `AnnotationMapperEdgeContractTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;主键生成与回填（selectKey/useGeneratedKeys）（XML Mapper） | `XmlMapperKeyGenerationContractTest(7条)` | ⚠️ | ⚠️ | ⚠️ | ⚠️ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;SQL 片段（`<sql>` / `<include>`）（XML Mapper） | `XmlMapperSqlFragmentContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;动态 SQL（`<if>` / `<choose>`）（XML Mapper） | `XmlMapperDynamicSqlContractTest(7条)` | ✅ | ✅ | ✅ | ⚠️ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;动态规则引擎（XML Mapper） | `XmlMapperDynamicRuleContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;statement 属性（XML Mapper） | `XmlMapperStatementAttributeContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;引用外部 Mapper（XML Mapper） | `XmlRefMapperTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;存储过程调用（XML Mapper） | `XmlMapperCallableContractTest(6条)` | ⚠️ | ⚠️ | ✅ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | — | — | — | — | — |
| **值、边界与安全能力** | | | | | | | | | | | | | |
| &emsp;边界条件（空表/null/极值）（Lambda） | `LambdaEdgeTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;安全值（注入防护）（Lambda） | `LambdaSecurityValueContractTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;特殊值（超长字符串/特殊字符）（Lambda） | `LambdaSpecialValueContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;自由 Map 模式标识符安全（Map 模式） | `FreedomMapIdentifierSecurityContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| **类型系统与对象映射能力** | | | | | | | | | | | | | |
| &emsp;基本类型（int/string/bool/date）（TypeHandler） | `BasicTypeJdbcContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | ⚠️ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;枚举类型映射（TypeHandler） | `EnumTypeJdbcContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;二进制类型（TypeHandler） | `BinaryTypeJdbcContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;时间类型（TypeHandler） | `TimeTypeJdbcContractTest(13条)` | ⚠️ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;JSON 序列化/反序列化（TypeHandler） | `JsonTypeJdbcContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;数组类型（TypeHandler） | `ArrayTypeJdbcContractTest(7条)` | ✅ | ⚠️ | ✅ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | — | — | — | — | — |
| &emsp;向量类型映射（TypeHandler） | `VectorTypeMappingContractTest(4条)` | ⚠️ | ⚠️ | ✅ | ⚠️ | ⚠️ | ⚠️ | ✅ | — | — | — | — | ✅ |
| &emsp;映射策略（@Table/@Column）（对象映射） | `AnnotationMappingPolicyContractTest(22条)` | ✅ | ✅ | ✅ | ⚠️ | ✅ | ✅ | ✅ | — | ✅ | ✅ | ✅ | ✅ |
| &emsp;表定义（对象映射） | `MappingTableDefinitionContractTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;表元数据（对象映射） | `AnnotationTableMetadataContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;XML 映射元数据（对象映射） | `XmlMappingMetadataContractTest(13条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;Mapping 注册表（对象映射） | `MappingRegistryContractTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | ✅ | ✅ | ✅ | ✅ |
| &emsp;继承映射（对象映射） | `AnnotationInheritanceContractTest(10条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;特殊类型映射（对象映射） | `AnnotationSpecialTypeContractTest(4条)` | ✅ | ⚠️ | ✅ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | — | — | — | — | — |
| &emsp;TypeHandler 注册（对象映射） | `AnnotationTypeHandlerContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;SQL 模板（对象映射） | `AnnotationSqlTemplateContractTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| **数据库方言与元数据能力** | | | | | | | | | | | | | |
| &emsp;自增键/序列键/UUID 键（主键） | `KeyGenerationContractTest(22条)` | ✅ | ⚠️ | ✅ | ⚠️ | ⚠️ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;方言生成键策略（主键） | `InsertDialectStrategyContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| &emsp;命名策略（驼峰/下划线/分隔符）（Schema） | `NamingMappingContractTest(22条)` | ⚠️ | ✅ | ✅ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | — | — | — | — | — |
| &emsp;标准 Schema（schema.table）（Schema） | `StandardSchemaTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |
| **事务与数据库过程能力** | | | | | | | | | | | | | |
| &emsp;事务提交/回滚/传播/隔离（事务） | `TransactionContractTest(40条)` | ✅ | ✅ | ✅ | ⚠️ | ⚠️ | ✅ | ⚠️ | — | — | — | — | — |
| &emsp;存储过程（过程/函数） | `ProcedureContractTest(8条)` | ⚠️ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | — | — | — | — | — |
| &emsp;函数调用（过程/函数） | `FunctionContractTest(7条)` | ⚠️ | ⚠️ | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | — | — | — | — |
| **Session 生命周期能力** | | | | | | | | | | | | | |
| &emsp;Session 生命周期（Session） | `SessionCoreTest(11条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — | — |

### 备注

- **本轮所有 7 个关系型数据源均已达到 0 failure / 0 error**，剩余 ⚠️ 均为 feature gate 跳过。
- **H2** ⚠️ 项：generated-key-result-set、postgres-on-conflict、procedure、多结果集、vector、callable、time-extreme-date 等 gate。
- **MySQL** ⚠️ 项：array/sequence/KNN/vector、cursor/table function、generated-key-result-set、postgres-on-conflict 等 gate。
- **PG** ⚠️ 项：仅 procedure-result-set、generated-key-result-set 等 gate。
- **Oracle** ⚠️ 项：array/sequence、procedure-result-set、vector、多结果集、空字符串 NULL、IN 1000、事务隔离、generated-key-result-set 等 gate。
- **MSSQL** ⚠️ 项：array/sequence、cursor、vector、repeated-order-by-column、事务 savepoint 等 gate。
- **DB2** ⚠️ 项：多结果集、array/vector、cursor、BIT/大小写标识符等 gate。
- **ClickHouse** ⚠️ 项：OLAP 事务、约束、mutation、binary/time、callable/procedure、多结果集等 gate。
- **Redis/Mongo/ES/Milvus** — 项：不参与 RDBMS 特有测试（JDBC/Batch/Join/Transaction 等）。

## §3 跳过清单总览

本节的跳过数来自本轮 Surefire XML 的 `skipped` 汇总。跳过判定统一由 `DataSourceProfile` 的 `supportsFeature()` / `support()` 控制（`requiresNxnFeature` + `@Capability` 规则），`jdbc-{db}.properties` 只保留连接参数，不再承载 feature gate。

| 数据源 | 跳过数 | 跳过 feature |
| --- | ---: | --- |
| H2 | 45 | `case-sensitive-identifiers`, `delimited-lowercase-standard-table`, `function-call-callback`, `generated-key-result-set`, `knn`, `multiple-result-sets`, `postgres-on-conflict`, `procedure`, `time-extreme-date`, `vector`, `xml-mapper-callable` |
| MySQL | 35 | `array`, `function-record-result`, `function-table-result`, `generated-key-result-set`, `knn`, `postgres-on-conflict`, `procedure-cursor-result`, `sequence`, `vector`, `xml-select-key-user-info-sequence` |
| PostgreSQL | 3 | `generated-key-result-set`, `procedure-result-set` |
| Oracle | 51 | `array`, `delimited-lowercase-standard-table`, `distinct-empty-string`, `generated-key-result-set`, `knn`, `large-in-list`, `lowercase-standard-result-columns`, `multiple-result-sets`, `postgres-on-conflict`, `procedure-result-set`, `sequence`, `transaction-release-savepoint`, `transaction-repeatable-read`, `vector`, `xml-foreach-batch-insert-values`, `xml-mapper-callable` |
| MSSQL | 44 | `array`, `case-sensitive-identifiers`, `knn`, `postgres-on-conflict`, `procedure-cursor-result`, `repeated-order-by-column`, `sequence`, `transaction-release-savepoint`, `vector`, `xml-mapper-callable` |
| DB2 | 42 | `array`, `bit-cast-null-value`, `case-sensitive-identifiers`, `delimited-lowercase-standard-table`, `knn`, `multiple-result-sets`, `postgres-on-conflict`, `procedure-cursor-result`, `vector`, `xml-mapper-callable` |
| ClickHouse | 136 | `array`, `batch-duplicate-failure-propagated`, `binary`, `bit-cast-null-value`, `case-sensitive-identifiers`, `duplicate-key-strategy`, `duplicate-primary-key-rejected`, `empty-where-mutation`, `exact-mutation-affected-rows`, `function-call-callback`, `function-record-result`, `function-table-result`, `generated-key-column`, `generated-key-result-set`, `generated-keys-numeric`, `join-non-equi-condition`, `keygen-auto-batch-explicit-null`, `knn`, `left-join-null-values`, `length-limit-enforced`, `multiple-result-sets`, `non-null-primary-key-rejected`, `postgres-on-conflict`, `procedure`, `sequence`, `sql-not-in-null-semantics`, `time-extreme-date`, `transaction`, `transaction-release-savepoint`, `transaction-repeatable-read`, `xml-mapper-callable`, `xml-select-key-user-info-sequence` |
| Redis | 0 | 无；但通用 Lambda CRUD 不纳入当前 Redis 目标 |
| Mongo | 0 | 无 |
| ES6 | 0 | 无 |
| ES7 | 0 | NXN 专有测试无 skip；旧配置层仍有 `pagination`, `transaction`, `procedure`, `testTableAnnotation`，后续可评估是否清理或转为 adapter 专有解释 |
| Milvus | 0 | 无；旧配置层仍有 `testTableAnnotation` case skip |

## §4 当前结论

本轮修复了上一轮的全部 failure/error：通过为 H2/Oracle/MSSQL/DB2 创建 per-DB 注解 Mapper 重写（`@SelectKeySql` 专有 SQL、`insertWithGeneratedKeyNoKeyColumn` 加 `keyColumn` 或 `OUTPUT INSERTED`）、将 `GENERATED_KEY_RESULT_SET`/`REPEATED_ORDER_BY_COLUMN`/`BINARY`/`TIME_EXTREME_DATE` 等真实限制补入对应 profile、同步 properties 与 profile、移除不必要的 JSON/GENERATED_KEYS_NUMERIC/KEYGEN_AUTO_BATCH_EXPLICIT_NULL/XML_SELECT_KEY gate，实现了 7 个关系型数据源全部 0 failure / 0 error。

| 数据源 | 当前结论 |
| --- | --- |
| H2 | `642 / 0 fail / 0 error / 45 skip`；generated-key-result-set、postgres-on-conflict、procedure、多结果集、vector、callable、time-extreme-date 等 gate |
| MySQL | `642 / 0 fail / 0 error / 35 skip`；array/sequence/KNN/vector、cursor/table function、generated-key-result-set、postgres-on-conflict 等 gate |
| PostgreSQL | `642 / 0 fail / 0 error / 3 skip`；当前覆盖最完整，`procedure-result-set` 与 `generated-key-result-set` 保留 gate |
| Oracle | `642 / 0 fail / 0 error / 51 skip`；array/sequence、procedure-result-set、vector、多结果集、空字符串 NULL、IN 1000、事务隔离、generated-key-result-set 等 gate |
| MSSQL | `642 / 0 fail / 0 error / 44 skip`；array/sequence、cursor、vector、repeated-order-by-column、事务 savepoint 等 gate |
| DB2 | `642 / 0 fail / 0 error / 42 skip`；多结果集、array/vector、cursor、BIT/大小写标识符等 gate |
| ClickHouse | `643 / 0 fail / 0 error / 136 skip`；OLAP 事务、约束、mutation、binary/time、callable/procedure、多结果集等 gate |
| Redis | `21 / 0 fail / 0 error / 0 skip`；作为特殊 adapter，只统计 command、JDBC-style DSL、Mapper 和类型操作 |
| Mongo | `18 / 0 fail / 0 error / 0 skip`；Mongo 专有 command/JDBC-style DSL、BSON、Lambda、BaseMapper、Mapper XML/Annotation 均在数据源专有能力内表达 |
| Elastic6 / Elastic7 | 各 `16 / 0 fail / 0 error / 0 skip`；ES6、ES7 独立保留各自专有 XML/DSL，不上提到通用 contract |
| Milvus | `17 / 0 fail / 0 error / 0 skip`；Milvus 专有 JDBC-style DSL、向量类型、索引/load/search、Mapper XML 均在数据源专有能力内表达 |

### 本轮修复明细

| 修复项 | 影响数据源 | 修复方式 |
| --- | --- | --- |
| `GENERATED_KEY_RESULT_SET` gate 缺失 | H2、Oracle | H2Profile、OracleProfile 新增 `FeatureId.GENERATED_KEY_RESULT_SET`（H2/Oracle 不支持 ResultSet 方式回填主键） |
| Annotation selectKey 使用 PG 专有语法 | H2、Oracle、MSSQL、DB2 | 新建 per-DB `*AnnotationAttributesMapper`，重写 `@SelectKeySql` 为各库专有序列 SQL |
| Oracle `getGeneratedKeys` 返回 ROWID | Oracle | 重写 `insertWithGeneratedKeyNoKeyColumn` 加 `keyColumn="id"` |
| MSSQL `getGeneratedKeys` 无 keyColumn 不可靠 | MSSQL | 重写 `insertWithGeneratedKeyNoKeyColumn` 用 `OUTPUT INSERTED.id` + `generatedKeySource=resultSet` |
| DB2 `generatedKeySource=resultSet` 注解 Mapper 缺 FINAL TABLE | DB2 | 重写 `insertWithGeneratedKeyResultSet` 用 `SELECT id FROM FINAL TABLE(INSERT ...)` |
| `REPEATED_ORDER_BY_COLUMN` gate 缺失 | MSSQL | MsSqlProfile 新增 `FeatureId.REPEATED_ORDER_BY_COLUMN`（MSSQL 不允许 ORDER BY 同列重复） |
| `BINARY` gate 缺失 | ClickHouse | ClickHouseProfile 新增 `FeatureId.BINARY`（ClickHouse JDBC 不支持非空 binary round-trip） |
| `TIME_EXTREME_DATE` gate 缺失 | ClickHouse | ClickHouseProfile 新增 `FeatureId.TIME_EXTREME_DATE`（ClickHouse 不支持极端日期） |
| `JSON` gate 多余 | H2、Oracle、MSSQL、DB2、ClickHouse | JsonTypeHandler 是纯 Java 序列化，所有 DB 支持 VARCHAR 即可；从 profile 和 properties 移除 |
| `GENERATED_KEYS_NUMERIC` gate 多余 | Oracle、MSSQL | LambdaTemplate 走方言策略已支持；从 profile 和 properties 移除 |
| `KEYGEN_AUTO_BATCH_EXPLICIT_NULL` gate 多余 | Oracle、MSSQL | 方言策略已支持；从 profile 和 properties 移除 |
| `XML_SELECT_KEY_USER_INFO_SEQUENCE` gate 多余 | H2、Oracle、MSSQL、DB2 | 已有 per-DB XML 和注解 Mapper 实现；从 profile 和 properties 移除 |
| properties 与 profile 不同步 | 全部 | 所有 7 个 `jdbc-{db}.properties` 的 `test.skip.features` 重新生成为与 profile 完全一致 |

## §5 下一步改进方向

优先级按"关系型通用能力应尽量接近"和"先 JDBC 原始 DSL，后 Lambda CRUD"的原则排序。

### P0：先保持不动的差异

以下项目已经被证明是数据库语义或驱动能力差异，短期不建议为了减少 skip 而弱化 contract。

| 项目 | 数据源 | 原因 |
| --- | --- | --- |
| `generated-key-result-set` | H2、MySQL、PostgreSQL、Oracle、ClickHouse | 只有 DB2（`FINAL TABLE`）和 MSSQL（`OUTPUT INSERTED`）支持 ResultSet 方式回填主键；其余 DB 的 JDBC `getGeneratedKeys()` 不支持 `generatedKeySource=resultSet` |
| `multiple-result-sets` | H2、Oracle、DB2、ClickHouse | 单个 prepared statement 执行多条分号 SQL 的驱动支持不一致；不能由 dbVisitor 模拟拆 SQL 代替真实能力 |
| `procedure-result-set` | PostgreSQL、Oracle | PostgreSQL 已用 refcursor 覆盖 cursor 场景；Oracle 已用 `SYS_REFCURSOR` 覆盖，direct procedure ResultSet 不等价 |
| `procedure-cursor-result` | MySQL、MSSQL、DB2 | 普通 procedure/result-set 已打开；OUT cursor/refcursor 模型不等价 |
| `distinct-empty-string` | Oracle | Oracle 空字符串即 NULL，是数据库语义 |
| `large-in-list` | Oracle | Oracle `IN` 列表 1000 限制 |
| `transaction-repeatable-read` | Oracle、ClickHouse | 数据库事务隔离模型差异 |
| `transaction-release-savepoint` | Oracle、MSSQL、ClickHouse | JDBC 驱动不支持 releaseSavepoint |
| `transaction` | ClickHouse | ClickHouse 非传统 OLTP 事务模型 |
| `exact-mutation-affected-rows` | ClickHouse | mutation affected rows 语义不同 |
| `repeated-order-by-column` | MSSQL | MSSQL 不允许 ORDER BY 同列重复 |
| `binary` | ClickHouse | ClickHouse JDBC 不支持非空 binary round-trip |
| `time-extreme-date` | H2、ClickHouse | 极端日期边界差异 |
| `bit-cast-null-value` | DB2、ClickHouse | DB2 无 BIT 类型；ClickHouse 不支持 `CAST(NULL AS BIT)` |

### P1：可继续评估的关系型能力

| 方向 | 影响数据源 | 建议动作 |
| --- | --- | --- |
| `case-sensitive-identifiers` | H2、MSSQL、DB2、ClickHouse | 逐一确认 quoted/mixed-case 元数据和结果列行为；能通过物料解决的下沉到 realdb |
| `delimited-lowercase-standard-table` | H2、Oracle、DB2 | 检查标准小写表名加引号后的元数据行为，区分产品问题和数据库大小写语义 |

### P2：类型系统能力提升

| 方向 | 影响数据源 | 建议动作 |
| --- | --- | --- |
| `array` | MySQL、Oracle、MSSQL、DB2、ClickHouse | 评估非空数组序列化/读取是否有真实等价类型或专有承载 |
| `bit-cast-null-value` | DB2、ClickHouse | 只影响 `CAST(NULL AS BIT)`/BIT 空值语义；不要误判为普通 boolean 不支持 |

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
4. 移除对应 `DataSourceProfile` 中的 feature（`jdbc-{db}.properties` 不再承载 feature gate）。
5. 跑目标数据源代表类，再跑目标数据源全量。
6. 更新本文档；新数据源接入流程同步更新 `NXN_DS_GUIDE.md`。
