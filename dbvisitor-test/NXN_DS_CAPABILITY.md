## §2 能力矩阵

> ✅ 对应场景全部通过；❌ 当前实现不支持对应场景或全部验证失败；⚠️ x/y 表示 y 个场景中有 x 个通过，其余仍有限制或失败；— 无绑定或未验证，不代表不支持。执行范围和验证基线见 §3。
> 
> 本表关注 dbVisitor 整合能力。SQL、Redis 命令、Mongo DSL、ES 请求等均视为原生命令；使用原生命令完成 CRUD 同样计入 CRUD 覆盖，不要求命令采用 SQL 语法。Milvus 适配器的 SQL 与 SDK 覆盖范围见 [Milvus 支持范围](../dbvisitor-doc/docs/features/milvus/compatibility.md#sdk-coverage)。

| 测试说明 | 测试类 | H2 | MySQL | PG | Oracle | MSSQL | DB2 | CH | Redis | Mongo | ES6 | ES7 | Milvus |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **数据读写与变更能力** | | | | | | | | | | | | | |
| &emsp;基本 CRUD（插入/查询/更新/删除）（纯原生命令） | `JdbcCrudContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;单条增删改的影响数量（JdbcTemplate） | `JdbcMutationCountContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;基本 CRUD（Lambda） | `LambdaCrudContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;Lambda 写入后的 JdbcTemplate 标量与计数回读（组合场景） | `LambdaJdbcReadbackContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ⚠️ 1/3 | ⚠️ 1/3 | ⚠️ 1/3 | ✅ |
| &emsp;默认主键插入（Lambda） | `LambdaDefaultKeyContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;实体列表插入（Lambda） | `LambdaEntityListInsertContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;实体与列表插入（BaseMapper） | `BaseMapperInsertContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;使用主键默认值插入（BaseMapper） | `BaseMapperDefaultKeyContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;写入限制与错误传递（BaseMapper） | `BaseMapperWriteValidationContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ⚠️ 1/3 |
| &emsp;实体查找、加载与空结果（BaseMapper） | `BaseMapperQueryContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;样本与 Map 条件参数（BaseMapper） | `BaseMapperSampleContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;分页及页边界（BaseMapper） | `BaseMapperPaginationContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;部分更新、替换与 upsert（BaseMapper） | `BaseMapperUpdateContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;主键删除与边界（BaseMapper） | `BaseMapperDeleteContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ⚠️ 6/7 |
| &emsp;多步操作与 API 混用（BaseMapper） | `BaseMapperCombinedContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;实体类型、Session 与 Jdbc 访问器（BaseMapper） | `BaseMapperCombinedContractTest.baseMapperAccessors_shouldExposeEntityTypeSessionJdbcAndLambdaApis` / `RedisSessionCoverageContractTest.accessors_shouldExposeEntitySessionAndWorkingJdbc` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;复合主键 CRUD（BaseMapper） | `BaseMapperCompositeKeyContractTest(12条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;基本 CRUD（注解 Mapper） | `AnnotationMapperCrudContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;基本 CRUD（XML Mapper） | `XmlMapperCrudContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;条件删除与影响行数（注解 Mapper） | `AnnotationMapperConditionalDeleteContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;映射 Map 模式 CRUD（Map 模式） | `MappedMapCrudContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;自由 Map 模式 CRUD（Map 模式） | `FreedomMapCrudContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;Map 参数操作（BaseMapper） | `BaseMapperMapOperationContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;批量增删改与逐项结果（JdbcTemplate，可逐条执行） | `JdbcBatchMutationContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;空批次不执行命令（JdbcTemplate） | `JdbcBatchEmptyContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;批量命令错误传递（JdbcTemplate） | `JdbcBatchErrorContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;批量主键冲突错误传递（JdbcTemplate） | `JdbcBatchConflictContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;较大批次的结果完整性（JdbcTemplate） | `JdbcBatchLargeContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;批量增删改（Lambda） | `LambdaBatchMutationContractTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ⚠️ 8/9 |
| &emsp;写入冲突策略（Ignore/Update）（Lambda） | `LambdaDuplicateStrategyContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ⚠️ 2/7 |
| **查询与检索能力** | | | | | | | | | | | | | |
| &emsp;单行与有序多行 Map 结果（纯原生命令） | `JdbcMapQueryContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;实体列表与多字段映射（纯原生命令） | `JdbcBeanQueryContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;单列值的指定类型读取与快捷方法（纯原生命令） | `JdbcScalarQueryContractTest(2条)` / `JdbcCrudScalarReadbackContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;单列值的有序列表读取（纯原生命令） | `JdbcScalarListQueryContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;计数结果的 int/long 读取（纯原生命令） | `JdbcCountQueryContractTest(1条)` / `JdbcCountReadbackContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;前两列转数值键或数值值的 Pairs（纯原生命令） | `JdbcPairsQueryContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;前两列转 Long/Date 键值的 Pairs（纯原生命令） | `JdbcTemporalPairsQueryContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;NULL 与非 NULL 条件（Lambda） | `LambdaNullConditionContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;过滤结果计数（Lambda） | `LambdaCountContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;比较条件与动态开关（Lambda） | `LambdaComparisonContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;区间边界与取反（Lambda） | `LambdaRangeContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ⚠️ 3/4 |
| &emsp;单元素与多元素集合绑定（Lambda） | `LambdaCollectionBindingContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;大集合参数边界（Lambda） | `LambdaLargeCollectionContractTest(1条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;含 NULL 集合的取反语义（Lambda） | `LambdaNullCollectionContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;LIKE 条件（Lambda） | `LambdaLikeContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;字符串属性名条件（Lambda） | `LambdaPropertyConditionContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;逻辑条件组合（Lambda） | `LambdaLogicalConditionContractTest(14条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ⚠️ 9/14 |
| &emsp;字段投影（Lambda） | `LambdaProjectionContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;去重（Lambda） | `LambdaDistinctContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;分组聚合（Lambda） | `LambdaAggregateContractTest(2条)` / `LambdaAggregateResultContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;排序（ASC/DESC/null 排序）（Lambda） | `LambdaSortContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ⚠️ 3/4 | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;页偏移、翻页与无重复遍历（Lambda） | `LambdaPageNavigationContractTest(2条)` / `LambdaPageOffsetContractTest(1条)` / `*LambdaPageNavigationContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;分页读取与总条数、总页数一致性（Lambda） | `LambdaPageResultContractTest(9条)` / `*LambdaPageResultContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ |
| &emsp;PageObject、PageResult 与页边界（XML Mapper） | `XmlMapperPaginationContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;迭代器/流式查询（Lambda） | `LambdaIteratorContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;KNN 向量排序（向量） | `VectorKnnOrderingContractTest(5条)` | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;向量范围过滤（向量） | `VectorRangeFilteringContractTest(3条)` | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;向量 + 标量混合查询（向量） | `VectorCombinedQueryContractTest(2条)` | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | — | — | — | ✅ |
| **参数绑定与结果处理能力** | | | | | | | | | | | | | |
| &emsp;JDBC 值绑定安全、复用、NULL/空串、写入过滤和 fetchSize/maxRows | `JdbcPreparedValueContractTest(7条)` | ✅ | — | — | — | — | — | ✅ | ⚠️ 6/7 | — | — | — | ✅ |
| &emsp;Object 数组位置参数绑定（纯原生命令） | `JdbcPositionalParameterContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;SqlArg 显式 TypeHandler 参数（纯原生命令） | `JdbcExplicitTypeParameterContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;PreparedStatementSetter 写入与查询参数（纯原生命令） | `JdbcStatementSetterParameterContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;命名参数、Map/Bean 与嵌套表达式（纯原生命令） | `JdbcNamedParameterContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;NULL 参数写入与读回（纯原生命令） | `JdbcNullParameterContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ |
| &emsp;可信标识符替换与值参数混用（纯原生命令） | `JdbcTextParameterContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;可信命令片段替换（纯原生命令） | `JdbcFragmentParameterContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;AND/IN/SET 模板规则展开（JdbcTemplate） | `JdbcRuleParameterContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;Array/Bean/Map 参数源混用（JdbcTemplate 组合场景） | `JdbcArgumentSourceParameterContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;按声明顺序绑定位置参数（注解 Mapper） | `AnnotationMapperPositionalParameterContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;命名参数、重复引用与未使用参数（注解 Mapper） | `AnnotationMapperNamedParameterContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;Bean、Map 与嵌套对象参数（注解 Mapper） | `AnnotationMapperStructuredParameterContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;查询中的多个命名参数（注解 Mapper） | `AnnotationMapperQueryParameterContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;NULL 字段参数写入与读回（注解 Mapper） | `AnnotationMapperNullParameterContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ |
| &emsp;引号与反斜杠参数往返（注解 Mapper） | `AnnotationMapperSpecialTextParameterContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;空字符串与 NULL 字段的区分（注解 Mapper） | `AnnotationMapperEmptyTextParameterContractTest(1条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ |
| &emsp;数组与 List 的集合展开规则（注解 Mapper） | `AnnotationMapperCollectionRuleContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;String[] 命令片段拼接（注解 Mapper） | `AnnotationMapperCommandAssemblyContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;按实体字段进行条件过滤（注解 Mapper） | `AnnotationMapperQueryResultContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;原生命令的对象列表与标量返回（注解 Mapper） | `AnnotationMapperQueryResultContractTest.annotationMapperQuery_shouldReturnObjectListAndScalar` / `RedisMapperCoverageContractTest.nativeQuery_shouldReturnObjectListAndScalar` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;Map、标量与列表查询返回（XML Mapper） | `XmlMapperQueryResultContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;逐行对象映射（RowMapper） | `JdbcRowMapperContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;连续 Map、列表与标量读取（JdbcTemplate 组合场景） | `JdbcMixedResultAccessContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;逐行回调（RowCallbackHandler） | `JdbcRowCallbackContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;结果集提取、过滤与 Pairs（ResultSetExtractor） | `JdbcResultExtractorContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;Map 列表、单行与计数（Lambda） | `LambdaResultValueContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;单列标量与列表类型转换（Lambda） | `LambdaScalarResultContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| &emsp;逐行对象映射（Lambda RowMapper） | `LambdaRowMapperContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;逐行回调（Lambda） | `LambdaRowCallbackContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;结果集提取、Pairs、过滤与客户端分组（Lambda） | `LambdaResultExtractorContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;空值与 Map 到对象映射（Lambda） | `LambdaNullResultMappingContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;默认映射、自定义处理器与空结果（注解 Mapper） | `AnnotationMapperResultHandlerContractTest(7条)` / `MilvusMapperResultHandlerContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;可滚动结果集与提取器组合（注解 Mapper） | `AnnotationMapperScrollableResultContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ❌ |
| &emsp;结果处理（XML Mapper） | `XmlMapperResultHandlerContractTest(4条)` / `MilvusXmlResultHandlerContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;实体、Map、标量、列表与空结果映射（注解 Mapper） | `AnnotationMapperResultMappingContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;计算投影的标量与 Map 映射（注解 Mapper） | `AnnotationMapperProjectionResultContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 1/2 | — | — | — | ❌ |
| &emsp;PageObject 列表分页（注解 Mapper） | `AnnotationMapperPaginationResultContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;resultMap 配置与结果映射（XML Mapper） | `XmlMapperResultMapContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 7/8 | — | — | — | ✅ |
| &emsp;空结果集处理（Lambda） | `LambdaEmptyResultContractTest(11条)` | ✅ | ✅ | ✅ | ⚠️ 10/11 | ✅ | ✅ | ✅ | ❌ | — | — | — | ⚠️ 7/11 |
| &emsp;多结果集（纯原生命令） | `JdbcMultipleResultSetContractTest(4条)` | ❌ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ | — | — | — | ✅ |
| **命令执行与 Mapper 配置能力** | | | | | | | | | | | | | |
| &emsp;@Execute 命令执行（注解 Mapper） | `AnnotationMapperCommandExecutionContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;statement 查找、执行与多命名空间（XML Mapper） | `XmlMapperStatementAccessContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;Mapper 注册、代理创建与非法接口校验（Session） | `SessionMapperRegistrationContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;Mapper 方法调用与列表/标量返回（Session） | `SessionMapperInvocationContractTest(2条)` / `MilvusSessionMapperInvocationContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;跨 Mapper 与 JdbcTemplate 数据共享（Session） | `SessionMapperSharingContractTest(2条)` / `MilvusSessionMapperSharingContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;statement 写入与影响条数（Session） | `SessionStatementMutationContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;未匹配写入的影响条数（Session statement） | `SessionStatementEmptyMutationContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ⚠️ 1/2 |
| &emsp;statement 列表、实体与标量结果（Session） | `SessionStatementResultContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;statement 动态 Map 与 Bean 参数（Session） | `SessionStatementParameterContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;statement 分页列表与 PageResult（Session） | `SessionStatementPaginationContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;statement 非法 ID 校验（Session） | `SessionStatementLookupContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;连续 statement 调用与多集合访问（Session） | `SessionStatementCoordinationContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;statement 写入与多行删除（BaseMapper） | `BaseMapperStatementMutationContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;未匹配写入的影响条数（BaseMapper statement） | `BaseMapperStatementEmptyMutationContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ⚠️ 1/2 |
| &emsp;statement 实体与列表结果（BaseMapper） | `BaseMapperStatementResultContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;statement 分页列表（BaseMapper） | `BaseMapperStatementPaginationContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;statement 非法 ID 校验（BaseMapper） | `BaseMapperStatementLookupContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;执行方式、超时与取数配置（注解 Mapper） | `AnnotationMapperExecutionContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ⚠️ 4/5 |
| &emsp;结果集类型配置（注解 Mapper） | `AnnotationMapperResultSetTypeContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;生成键回填与键来源（注解 Mapper） | `AnnotationMapperGeneratedKeysContractTest(3条)` | ⚠️ 2/3 | ⚠️ 2/3 | ⚠️ 2/3 | ⚠️ 2/3 | ✅ | ✅ | ❌ | ⚠️ 1/3 | — | — | — | ⚠️ 1/3 |
| &emsp;selectKey 执行（注解 Mapper） | `AnnotationMapperSelectKeyContractTest(1条)` | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ❌ |
| &emsp;边界测试（注解 Mapper） | `AnnotationMapperEdgeContractTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 4/6 | ⚠️ 3/6 | — | — | — | ⚠️ 4/6 |
| &emsp;原生命令的解析与执行错误传播（注解 Mapper） | `AnnotationMapperEdgeContractTest.annotationMapperSqlErrors_shouldPropagateFromSyntaxTableAndColumnFailures` / `RedisMapperCoverageContractTest.commandErrors_shouldExposeSyntaxTypeAndNumericFailures` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — |
| &emsp;生成键回填与键来源（XML Mapper） | `XmlMapperGeneratedKeysContractTest(4条)` | ⚠️ 3/4 | ⚠️ 3/4 | ⚠️ 3/4 | ⚠️ 3/4 | ✅ | ✅ | ❌ | ⚠️ 1/4 | — | — | — | ⚠️ 2/4 |
| &emsp;selectKey 前置与后置执行（XML Mapper） | `XmlMapperSelectKeyContractTest(2条)` | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ❌ |
| &emsp;显式主键插入（XML Mapper） | `XmlMapperExplicitKeyContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;命令片段组合（`<sql>` / `<include>`）（XML Mapper） | `XmlMapperSqlFragmentContractTest(4条)` / `MilvusXmlTemplateContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;动态模板分支、参数展开与更新字段（XML Mapper） | `XmlMapperDynamicSqlContractTest(8条)` | ✅ | ✅ | ✅ | ⚠️ 7/8 | ✅ | ✅ | ✅ | ⚠️ 5/8 | — | — | — | ✅ |
| &emsp;动态规则引擎（XML Mapper） | `XmlMapperDynamicRuleContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;statement 类型、超时、fetchSize 与前向结果集（XML Mapper） | `XmlMapperStatementAttributeContractTest(5条)` / `MilvusXmlStatementAttributeContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;可滚动结果集选项（XML Mapper） | `XmlMapperScrollableResultContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;外部 XML 引用与 CRUD（RefMapper） | `XmlRefMapperCrudContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;动态条件、集合展开与文本替换（RefMapper） | `XmlRefMapperTemplateContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;Map 与 Bean 入参（RefMapper） | `XmlRefMapperParameterContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;多行 Map 结果映射（RefMapper） | `XmlRefMapperResultMapContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;存储过程调用（XML Mapper） | `XmlMapperCallableContractTest(6条)` | ❌ | ⚠️ 5/6 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | — | — | — | ❌ |
| **值、边界与安全能力** | | | | | | | | | | | | | |
| &emsp;边界条件（空表/null/极值）（Lambda） | `LambdaEdgeContractTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 7/9 | ❌ | — | — | — | ⚠️ 7/9 |
| &emsp;安全值（注入防护）（Lambda） | `LambdaSecurityValueContractTest(10条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ⚠️ 8/10 |
| &emsp;特殊值（超长字符串/特殊字符）（Lambda） | `LambdaSpecialValueContractTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 8/9 | ❌ | — | — | — | ⚠️ 8/9 |
| &emsp;自由 Map 模式标识符安全（Map 模式） | `FreedomMapIdentifierSecurityContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| **类型系统与对象映射能力** | | | | | | | | | | | | | |
| &emsp;按字段名读取 Date（TypeHandler，原生命令） | `NativeNamedFieldTypeContractTest.date_shouldRoundTripByFieldName` | — | — | — | — | — | — | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| &emsp;按字段名读取枚举名称（TypeHandler，原生命令） | `NativeNamedFieldTypeContractTest.enum_shouldRoundTripByFieldName` | — | — | — | — | — | — | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| &emsp;按字段名读取 true/false（TypeHandler，原生命令） | `NativeNamedFieldTypeContractTest.boolean_shouldRoundTripByFieldName` | — | — | — | — | — | — | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| &emsp;整型与浮点数往返（TypeHandler） | `BasicNumericTypeJdbcContractTest(1条)` / `RedisBasicTypeContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;高精度小数往返（TypeHandler） | `BasicDecimalTypeJdbcContractTest(1条)` / `RedisBasicTypeContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;大整数往返（TypeHandler） | `BasicBigIntegerTypeJdbcContractTest(1条)` / `RedisBasicTypeContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ |
| &emsp;布尔值往返（TypeHandler） | `BasicBooleanTypeJdbcContractTest(1条)` / `RedisBasicTypeContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;布尔空值绑定与读取（TypeHandler） | `BasicBooleanNullJdbcContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| &emsp;ASCII 与 Unicode 字符往返（TypeHandler） | `BasicCharacterTypeJdbcContractTest(1条)` / `RedisBasicTypeContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;字符空值绑定与读取（TypeHandler） | `BasicCharacterNullJdbcContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ |
| &emsp;空字符串往返（TypeHandler） | `BasicEmptyStringJdbcContractTest(1条)` / `RedisBasicTypeContractTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;枚举类型映射（TypeHandler） | `EnumTypeJdbcContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 4/5 | — | — | — | ✅ |
| &emsp;二进制类型（TypeHandler） | `BinaryTypeJdbcContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 3/4 | — | — | — | ❌ |
| &emsp;日期值往返（TypeHandler） | `TimeDateJdbcContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;JDBC 时间/时间戳与 LocalTime 转换（TypeHandler） | `TimeClockJdbcContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;LocalDateTime、Instant 与等价时刻转换（TypeHandler） | `TimeInstantJdbcContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;完整日期值的年、月、年月与月日提取（TypeHandler） | `TimePartialJdbcContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ❌ |
| &emsp;时间精度、日历与极值边界（TypeHandler） | `TimeBoundaryJdbcContractTest(3条)` | ⚠️ 2/3 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;儒略日与公元前日期往返（TypeHandler） | `TimeJulianDayJdbcContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;时间字段与映射空值（TypeHandler） | `TimeNullJdbcContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;对象、嵌套值与特殊字符的 JSON 写入序列化（TypeHandler） | `JsonSerializationJdbcContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;单列 JSON 对象转 Map（TypeHandler） | `JsonMapJdbcContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;单列 JSON 数组与 Bean 数组转 List（TypeHandler） | `JsonListJdbcContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;单列 JSON 数组转 Set（TypeHandler） | `JsonSetJdbcContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;注解绑定的 JSON Bean 往返与空字段（TypeHandler） | `JsonAnnotatedBeanJdbcContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;单列 JSON 空值绑定与读取（TypeHandler） | `JsonNullScalarJdbcContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| &emsp;JSON 空字段的行 Map 结果（TypeHandler） | `JsonNullRowJdbcContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ |
| &emsp;数组类型（TypeHandler） | `ArrayTypeJdbcContractTest(7条)` | ✅ | ⚠️ 1/7 | ✅ | ⚠️ 1/7 | ⚠️ 1/7 | ⚠️ 1/7 | ⚠️ 6/7 | ❌ | — | — | — | ✅ |
| &emsp;向量类型映射（TypeHandler） | `VectorTypeMappingContractTest(4条)` | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;字段名、默认列与实体/Map 映射（对象映射） | `AnnotationFieldMappingContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;插入、更新与只读字段策略（对象映射） | `AnnotationWritePolicyContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;忽略字段的读写与注解优先级（对象映射） | `AnnotationIgnoredFieldContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;样本条件中的忽略字段（对象映射） | `AnnotationIgnoredSampleContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| &emsp;仅映射显式注解字段（对象映射） | `AnnotationExplicitMappingContractTest(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;NULL 字段插入与显式更新（对象映射） | `AnnotationNullFieldMappingContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ⚠️ 1/2 | ⚠️ 1/2 | ✅ |
| &emsp;部分实体插入与字段更新（对象映射） | `AnnotationPartialFieldMappingContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;空字符串字段往返（对象映射） | `AnnotationEmptyFieldMappingContractTest(1条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;表定义（对象映射） | `MappingTableDefinitionContractTest(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;表元数据（对象映射） | `AnnotationTableMetadataContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;XML 映射元数据（对象映射） | `XmlMappingMetadataContractTest(13条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;Mapping 注册表（对象映射） | `MappingRegistryContractTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;继承映射（对象映射） | `AnnotationInheritanceContractTest(10条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;实体 JSON 字段的 Map/List/Set 与 specialJavaType 映射（对象映射） | `AnnotationJsonFieldMappingContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;实体原生数组字段与 specialJavaType 映射（对象映射） | `AnnotationArrayFieldMappingContractTest(1条)` | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;TypeHandler 注册（对象映射） | `AnnotationTypeHandlerContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 4/7 | — | — | — | ✅ |
| &emsp;SQL 模板（对象映射） | `AnnotationSqlTemplateContractTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| **数据库方言与元数据能力** | | | | | | | | | | | | | |
| &emsp;手工主键与完整性校验（主键） | `AssignedKeyContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 1/3 | ❌ | — | — | — | ⚠️ 2/3 |
| &emsp;数据库生成主键回填（主键） | `DatabaseGeneratedKeyContractTest(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;客户端 UUID 生成（主键） | `UuidKeyContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;自定义主键处理器（主键） | `CustomKeyHolderContractTest(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 6/7 | ❌ | — | — | — | ⚠️ 6/7 |
| &emsp;序列键配置与执行（主键） | `SequenceKeyContractTest(3条)` | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;方言生成键策略（主键） | `InsertDialectStrategyContractTest(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;字段命名转换与注解覆盖（对象映射） | `NamingConversionContractTest(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;结果列名大小写匹配（对象映射） | `ResultColumnCaseContractTest(3条)` | ⚠️ 1/3 | ✅ | ✅ | ⚠️ 1/3 | ✅ | ⚠️ 1/3 | ✅ | ✅ | — | — | — | ✅ |
| &emsp;大小写敏感标识符（Schema） | `IdentifierCaseContractTest(4条)` | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;分隔符与关键字引用（Schema） | `IdentifierQuotingContractTest(6条)` | ⚠️ 5/6 | ✅ | ✅ | ⚠️ 5/6 | ✅ | ⚠️ 5/6 | ✅ | ❌ | — | — | — | ❌ |
| &emsp;标准物料的 JDBC 表/列元数据（Schema） | `StandardSchemaContractTest(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| **事务与数据库过程能力** | | | | | | | | | | | | | |
| &emsp;提交、回滚与回滚规则（事务） | `TransactionBoundaryContractTest(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;传播与跨代理调用（事务） | `TransactionPropagationContractTest(17条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;嵌套事务与保存点（事务） | `TransactionNestedContractTest(6条)` | ✅ | ✅ | ✅ | ⚠️ 2/6 | ⚠️ 2/6 | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;隔离级别配置与恢复（事务） | `TransactionIsolationContractTest(6条)` | ✅ | ✅ | ✅ | ⚠️ 5/6 | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;JdbcTemplate、Session 与 Lambda 共享事务 | `TransactionApiParticipationContractTest(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;存储过程（过程/函数） | `ProcedureContractTest(8条)` | ❌ | ⚠️ 7/8 | ⚠️ 7/8 | ⚠️ 7/8 | ⚠️ 7/8 | ⚠️ 7/8 | ❌ | ❌ | — | — | — | ❌ |
| &emsp;函数调用（过程/函数） | `FunctionContractTest(7条)` | ⚠️ 6/7 | ⚠️ 4/7 | ✅ | ✅ | ✅ | ✅ | ⚠️ 4/7 | ⚠️ 3/7 | — | — | — | ❌ |
| **Session 生命周期能力** | | | | | | | | | | | | | |
| &emsp;Session 生命周期（Session） | `SessionCoreContractTest(11条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 5/11 | — | — | — | ⚠️ 10/11 |
| &emsp;Configuration 创建 Jdbc 与 Session | `SessionCoreContractTest.configurationFactories_shouldCreateUsableJdbcLambdaAndSession` / `RedisSessionCoverageContractTest.configuration_shouldCreateWorkingNativeJdbcAndSession` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | — |

## §3 如何理解验证范围

- 主表衡量 dbVisitor 的命令执行、参数绑定、结果处理、映射、回调和生命周期等整合行为。原生命令只是测试物料，不将具体命令或 SQL 语法单列为能力；Lambda 生成的条件、排序和分页仍按对应 API 验证。
- CRUD 按插入、读取、修改、删除后的实际数据判断；影响行数、参数绑定和结果映射按各自场景验证。不同数据源采用不同原生命令，不影响同一场景的覆盖认定。
- 本地映射注册、注解与报告检查不访问数据库，不计为真实数据源访问。生成的 profile 报告表示声明的绑定和限制，不能代替 JUnit 执行结果。
- 没有绑定和未运行不是数据库不支持；因物料依赖而跳过，也不能推断主功能不支持。只有等价语义可由原生实现提供时，才调整物料或拆分场景；不以客户端模拟补齐事务、JOIN 等能力。
- JdbcTemplate 批量调用可使用驱动批处理或逐条执行。它与 JDBC addBatch/executeBatch、多语句 getMoreResults()、原子批量写入是不同概念。
- 生成键、游标、空字符串、受影响行数、数组和时间类型等边界按具体驱动/版本判断；保持原断言，不把另一种数据库语义伪装成通过。
- Milvus 服务端 2.6.2、SDK 2.6.22 的向量数据库原生能力、TLS/Cloud 与版本条件统一见 [Milvus 支持范围](../dbvisitor-doc/docs/features/milvus/compatibility.md)。原生命令集成测试通过不等于集群、Cloud、吞吐或完整 SDK 验收。

### 各数据源最近一次完整集合验证

| 数据源 | 验证日期 | 用例数 | 通过 | 跳过 | 失败/错误 |
| --- | --- | ---: | ---: | ---: | ---: |
| h2 | 2026-09-11 | 711 | 668 | 43 | 0 |
| mysql | 2026-09-11 | 704 | 670 | 34 | 0 |
| pg | 2026-09-11 | 705 | 702 | 3 | 0 |
| mssql | 2026-09-11 | 704 | 664 | 40 | 0 |
| oracle | 2026-09-11 | 704 | 654 | 50 | 0 |
| db2 | 2026-09-11 | 704 | 664 | 40 | 0 |
| clickhouse | 2026-09-13 | 729 | 633 | 96 | 0 |
| redis | 2026-09-13 | 307 | 295 | 12 | 0 |
| mongo | 2026-09-11 | 135 | 111 | 0 | 24 |
| es6 | 2026-09-11 | 184 | 146 | 0 | 38 |
| es7 | 2026-09-11 | 192 | 154 | 0 | 38 |
| milvus | 2026-09-11 | 884 | 712 | 163 | 9 |
| 合计 | — | 6663 | 6073 | 481 | 109 |

统计包含各数据源完整 realdb 集合中的原生命令执行用例与本地映射检查，不是实际执行的命令数量。跳过项包括原生/驱动限制和明确的物料前置条件（如结果列名大小写），不能推断对应 API 整体不支持。Milvus 的 6 项明文、TLS/mTLS 正反向连接检查均通过；反向用例先验证正确证书可连接，避免将端口不可达误判为证书校验成功。

能力声明、契约绑定、注解及报告生成属于本地检查，不计入上表。

## §4 运行与维护

在仓库根目录执行：

```bash
./runnxn.sh all
./runnxn.sh milvus
./gradlew :dbvisitor-test:test -Pnxn.env=pg
```

连接配置和测试物料见 [接入规则](NXN_DS_GUIDE.md)。结果在 dbvisitor-test/build/test-results/test 和 build/reports/tests/test 下；切换数据源会覆盖上一组输出，比较完整集合时应按数据源保存报告。runnxn.sh all 在某个数据源失败后停止，不能把前面的通过结果当成全部运行完成。

调整契约时先确认被测 API、必要物料与原生边界，再回归受影响数据源；修改公共契约后运行完整集合，并据实际结果更新 §2。不要只依据 profile 声明打勾。
