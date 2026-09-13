## §2 能力矩阵

> ✅ 对应场景全部通过；❌ 当前实现不支持对应场景或全部验证失败；⚠️ x/y 表示 y 个场景中有 x 个通过，其余仍有限制或失败；— 无绑定或未验证，不代表不支持。执行范围和验证基线见 §3。
> 
> 本表关注 dbVisitor 整合能力。SQL、Redis 命令、Mongo DSL、ES 请求等均视为原生命令；使用原生命令完成 CRUD 同样计入 CRUD 覆盖，不要求命令采用 SQL 语法。Milvus 适配器的 SQL 与 SDK 覆盖范围见 [Milvus 支持范围](../dbvisitor-doc/docs/features/milvus/compatibility.md#sdk-coverage)。

| 测试说明 | 测试类 | H2 | MySQL | PG | Oracle | MSSQL | DB2 | CH | Redis | Mongo | ES6 | ES7 | Milvus |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **5.1 编程式 API** | | | | | | | | | | | | | |
| &emsp;基本 CRUD（插入/查询/更新/删除）（纯原生命令） | `JdbcCrudCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;单条增删改的影响数量（JdbcTemplate） | `JdbcMutationCountCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;批量增删改与逐项结果（JdbcTemplate，可逐条执行） | `JdbcBatchMutationCase(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;空批次不执行命令（JdbcTemplate） | `JdbcBatchEmptyCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;批量命令错误传递（JdbcTemplate） | `JdbcBatchErrorCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;较大批次的结果完整性（JdbcTemplate） | `JdbcBatchLargeCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;单行与有序多行 Map 结果（纯原生命令） | `JdbcMapQueryCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;实体列表与多字段映射（纯原生命令） | `JdbcBeanQueryCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;单列值的指定类型读取与快捷方法（纯原生命令） | `JdbcScalarQueryCase(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;单列值的有序列表读取（纯原生命令） | `JdbcScalarListQueryCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;计数结果的 int/long 读取（纯原生命令） | `JdbcCountQueryCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;前两列转数值键或数值值的 Pairs（纯原生命令） | `JdbcPairsQueryCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;前两列转 Long/Date 键值的 Pairs（纯原生命令） | `JdbcTemporalPairsQueryCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;JDBC 值绑定安全、复用、NULL/空串、写入过滤和 fetchSize/maxRows | `JdbcPreparedValueCase(7条)` | ✅ | — | — | — | — | — | ✅ | ⚠️ 6/7 | — | — | — | ✅ |
| &emsp;存储过程（过程/函数） | `ProcedureCase(8条)` | ❌ | ⚠️ 7/8 | ⚠️ 7/8 | ⚠️ 7/8 | ⚠️ 7/8 | ⚠️ 7/8 | ❌ | ❌ | — | — | — | ❌ |
| &emsp;函数调用（过程/函数） | `FunctionCase(7条)` | ⚠️ 6/7 | ⚠️ 4/7 | ✅ | ✅ | ✅ | ✅ | ⚠️ 4/7 | ⚠️ 3/7 | — | — | — | ❌ |
| **5.2 Mapper API** | | | | | | | | | | | | | |
| &emsp;实体与列表插入（BaseMapper） | `BaseMapperInsertCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;写入限制与错误传递（BaseMapper） | `BaseMapperWriteValidationCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ⚠️ 1/3 |
| &emsp;实体查找、加载与空结果（BaseMapper） | `BaseMapperQueryCase(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;样本与 Map 条件参数（BaseMapper） | `BaseMapperSampleCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;分页及页边界（BaseMapper） | `BaseMapperPaginationCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;部分更新、替换与 upsert（BaseMapper） | `BaseMapperUpdateCase(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;多步操作与 API 混用（BaseMapper） | `BaseMapperCombinedCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;实体类型、Session 与 Jdbc 访问器（BaseMapper） | `BaseMapperAccessorsCase(1条)` / `RedisBaseMapperAccessorsTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;基本 CRUD（注解 Mapper） | `AnnotationMapperCrudCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;条件删除与影响行数（注解 Mapper） | `AnnotationMapperConditionalDeleteCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;Map 参数操作（BaseMapper） | `BaseMapperMapOperationCase(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;按声明顺序绑定位置参数（注解 Mapper） | `AnnotationMapperPositionalParameterCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;命名参数、重复引用与未使用参数（注解 Mapper） | `AnnotationMapperNamedParameterCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;Bean、Map 与嵌套对象参数（注解 Mapper） | `AnnotationMapperStructuredParameterCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;查询中的多个命名参数（注解 Mapper） | `AnnotationMapperQueryParameterCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;NULL 字段参数写入与读回（注解 Mapper） | `AnnotationMapperNullParameterCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ |
| &emsp;引号与反斜杠参数往返（注解 Mapper） | `AnnotationMapperSpecialTextParameterCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;空字符串与 NULL 字段的区分（注解 Mapper） | `AnnotationMapperEmptyTextParameterCase(1条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ |
| &emsp;数组与 List 的集合展开规则（注解 Mapper） | `AnnotationMapperCollectionRuleCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;String[] 命令片段拼接（注解 Mapper） | `AnnotationMapperCommandAssemblyCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;原生命令的对象列表与标量返回（注解 Mapper） | `AnnotationMapperQueryResultCase(1条)` / `RedisAnnotationMapperQueryResultTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;默认映射、自定义处理器与空结果（注解 Mapper） | `AnnotationMapperResultHandlerCase(7条)` / `MilvusAnnotationMapperResultHandlerTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;可滚动结果集与提取器组合（注解 Mapper） | `AnnotationMapperScrollableResultCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ❌ |
| &emsp;实体、Map、标量、列表与空结果映射（注解 Mapper） | `AnnotationMapperResultMappingCase(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;计算投影的标量与 Map 映射（注解 Mapper） | `AnnotationMapperProjectionResultCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 1/2 | — | — | — | ❌ |
| &emsp;PageObject 列表分页（注解 Mapper） | `AnnotationMapperPaginationResultCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;@Execute 命令执行（注解 Mapper） | `AnnotationMapperCommandExecutionCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;Mapper 注册、代理创建与非法接口校验（Session） | `SessionMapperRegistrationCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;Mapper 方法调用与列表/标量返回（Session） | `SessionMapperInvocationCase(2条)` / `MilvusSessionMapperInvocationTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;跨 Mapper 与 JdbcTemplate 数据共享（Session） | `SessionMapperSharingCase(2条)` / `MilvusSessionMapperSharingTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;statement 写入与多行删除（BaseMapper） | `BaseMapperStatementMutationCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;未匹配写入的影响条数（BaseMapper statement） | `BaseMapperStatementEmptyMutationCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ⚠️ 1/2 |
| &emsp;statement 实体与列表结果（BaseMapper） | `BaseMapperStatementResultCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;statement 分页列表（BaseMapper） | `BaseMapperStatementPaginationCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;statement 非法 ID 校验（BaseMapper） | `BaseMapperStatementLookupCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;执行方式、超时与取数配置（注解 Mapper） | `AnnotationMapperExecutionCase(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ⚠️ 4/5 |
| &emsp;结果集类型配置（注解 Mapper） | `AnnotationMapperResultSetTypeCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;生成键回填与键来源（注解 Mapper） | `AnnotationMapperGeneratedKeysCase(3条)` | ⚠️ 2/3 | ⚠️ 2/3 | ⚠️ 2/3 | ⚠️ 2/3 | ✅ | ✅ | ❌ | ⚠️ 1/3 | — | — | — | ⚠️ 1/3 |
| &emsp;selectKey 执行（注解 Mapper） | `AnnotationMapperSelectKeyCase(1条)` | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ❌ |
| &emsp;NULL 字段写入与读取（注解 Mapper） | `AnnotationMapperNullValueCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;引号与 Unicode 参数往返（注解 Mapper） | `AnnotationMapperTextValueCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;未匹配写入的影响条数（注解 Mapper） | `AnnotationMapperEmptyMutationCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | — | — | — | ❌ |
| &emsp;目标表删除后的执行错误（注解 Mapper） | `AnnotationMapperMissingResourceCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;主键冲突错误传播（注解 Mapper） | `AnnotationMapperConflictErrorCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;原生命令的解析与执行错误传播（注解 Mapper） | `AnnotationMapperCommandErrorCase(1条)` / `RedisAnnotationMapperCommandErrorTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;Session 生命周期（Session） | `SessionCoreCase(10条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 5/10 | — | — | — | ⚠️ 9/10 |
| &emsp;Configuration 创建 Jdbc 与 Session | `SessionFactoryCase(1条)` / `RedisSessionFactoryTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| **5.3 构造器 API** | | | | | | | | | | | | | |
| &emsp;基本 CRUD（Lambda） | `LambdaCrudCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;Lambda 写入后的 JdbcTemplate 标量与计数回读（组合场景） | `LambdaJdbcReadbackCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ⚠️ 1/3 | ⚠️ 1/3 | ⚠️ 1/3 | ✅ |
| &emsp;实体列表插入（Lambda） | `LambdaEntityListInsertCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;批量增删改（Lambda） | `LambdaBatchMutationCase(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ⚠️ 8/9 |
| &emsp;写入冲突策略（Ignore/Update）（Lambda） | `LambdaDuplicateStrategyCase(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ⚠️ 2/7 |
| &emsp;NULL 与非 NULL 条件（Lambda） | `LambdaNullConditionCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;过滤结果计数（Lambda） | `LambdaCountCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;比较条件与动态开关（Lambda） | `LambdaComparisonCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;区间边界与取反（Lambda） | `LambdaRangeCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ⚠️ 3/4 |
| &emsp;单元素与多元素集合绑定（Lambda） | `LambdaCollectionBindingCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;大集合参数边界（Lambda） | `LambdaLargeCollectionCase(1条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;含 NULL 集合的取反语义（Lambda） | `LambdaNullCollectionCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;LIKE 条件（Lambda） | `LambdaLikeCase(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;字符串属性名条件（Lambda） | `LambdaPropertyConditionCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;逻辑条件组合（Lambda） | `LambdaLogicalConditionCase(14条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ⚠️ 9/14 |
| &emsp;字段投影（Lambda） | `LambdaProjectionCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;去重（Lambda） | `LambdaDistinctCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;分组聚合（Lambda） | `LambdaAggregateCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;排序（ASC/DESC/null 排序）（Lambda） | `LambdaSortCase(4条)` | ✅ | ✅ | ✅ | ✅ | ⚠️ 3/4 | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;页偏移、翻页与无重复遍历（Lambda） | `LambdaPageNavigationCase(3条)` / `*LambdaPageNavigationTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;分页读取与总条数、总页数一致性（Lambda） | `LambdaPageResultCase(9条)` / `*LambdaPageResultTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ |
| &emsp;迭代器/流式查询（Lambda） | `LambdaIteratorCase(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;Map 列表、单行与计数（Lambda） | `LambdaResultValueCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;单列标量与列表类型转换（Lambda） | `LambdaScalarResultCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| &emsp;逐行回调（Lambda） | `LambdaRowCallbackCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;结果集提取、Pairs、过滤与客户端分组（Lambda） | `LambdaResultExtractorCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;空结果集处理（Lambda） | `LambdaEmptyResultCase(11条)` | ✅ | ✅ | ✅ | ⚠️ 10/11 | ✅ | ✅ | ✅ | ❌ | — | — | — | ⚠️ 7/11 |
| &emsp;边界条件（空表/null/极值）（Lambda） | `LambdaEdgeCase(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 7/9 | ❌ | — | — | — | ⚠️ 7/9 |
| &emsp;安全值（注入防护）（Lambda） | `LambdaSecurityValueCase(10条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ⚠️ 8/10 |
| &emsp;特殊值（超长字符串/特殊字符）（Lambda） | `LambdaSpecialValueCase(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 8/9 | ❌ | — | — | — | ⚠️ 8/9 |
| **5.4 Mapper 文件** | | | | | | | | | | | | | |
| &emsp;基本 CRUD（XML Mapper） | `XmlMapperCrudCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;PageObject、PageResult 与页边界（XML Mapper） | `XmlMapperPaginationCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;Map、标量与列表查询返回（XML Mapper） | `XmlMapperQueryResultCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;结果处理（XML Mapper） | `XmlMapperResultHandlerCase(4条)` / `MilvusXmlMapperResultHandlerTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;resultMap 配置与结果映射（XML Mapper） | `XmlMapperResultMapCase(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 7/8 | — | — | — | ✅ |
| &emsp;statement 查找、执行与多命名空间（XML Mapper） | `XmlMapperStatementAccessCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;statement 写入与影响条数（Session） | `SessionStatementMutationCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;未匹配写入的影响条数（Session statement） | `SessionStatementEmptyMutationCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ⚠️ 1/2 |
| &emsp;statement 列表、实体与标量结果（Session） | `SessionStatementResultCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;statement 动态 Map 与 Bean 参数（Session） | `SessionStatementParameterCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;statement 分页列表与 PageResult（Session） | `SessionStatementPaginationCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;statement 非法 ID 校验（Session） | `SessionStatementLookupCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;连续 statement 调用与多集合访问（Session） | `SessionStatementCoordinationCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;生成键回填与键来源（XML Mapper） | `XmlMapperGeneratedKeysCase(4条)` | ⚠️ 3/4 | ⚠️ 3/4 | ⚠️ 3/4 | ⚠️ 3/4 | ✅ | ✅ | ❌ | ⚠️ 1/4 | — | — | — | ⚠️ 2/4 |
| &emsp;selectKey 前置与后置执行（XML Mapper） | `XmlMapperSelectKeyCase(2条)` | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ❌ |
| &emsp;命令片段组合（`<sql>` / `<include>`）（XML Mapper） | `XmlMapperSqlFragmentCase(4条)` / `MilvusXmlMapperSqlFragmentTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;动态模板分支、参数展开与更新字段（XML Mapper） | `XmlMapperDynamicSqlCase(8条)` | ✅ | ✅ | ✅ | ⚠️ 7/8 | ✅ | ✅ | ✅ | ⚠️ 5/8 | — | — | — | ✅ |
| &emsp;动态规则引擎（XML Mapper） | `XmlMapperDynamicRuleCase(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;statement 类型、超时、fetchSize 与前向结果集（XML Mapper） | `XmlMapperStatementAttributeCase(5条)` / `MilvusXmlMapperStatementAttributeTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;可滚动结果集选项（XML Mapper） | `XmlMapperScrollableResultCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;外部 XML 引用与 CRUD（RefMapper） | `XmlRefMapperCrudCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;动态条件、集合展开与文本替换（RefMapper） | `XmlRefMapperTemplateCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;Map 与 Bean 入参（RefMapper） | `XmlRefMapperParameterCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;多行 Map 结果映射（RefMapper） | `XmlRefMapperResultMapCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;存储过程调用（XML Mapper） | `XmlMapperCallableCase(6条)` | ❌ | ⚠️ 5/6 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | — | — | — | ❌ |
| **5.5 Map 查询模式** | | | | | | | | | | | | | |
| &emsp;映射 Map 模式 CRUD（Map 模式） | `MappedMapCrudCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;自由 Map 模式 CRUD（Map 模式） | `FreedomMapCrudCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;自由 Map 模式标识符安全（Map 模式） | `FreedomMapIdentifierSecurityCase(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| **5.6 向量查询** | | | | | | | | | | | | | |
| &emsp;KNN 向量排序（向量） | `VectorKnnOrderingCase(5条)` | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;向量范围过滤（向量） | `VectorRangeFilteringCase(3条)` | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;向量 + 标量混合查询（向量） | `VectorCombinedQueryCase(2条)` | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | — | — | — | ✅ |
| **5.7 对象映射** | | | | | | | | | | | | | |
| &emsp;默认主键插入（Lambda） | `LambdaDefaultKeyCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;使用主键默认值插入（BaseMapper） | `BaseMapperDefaultKeyCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;主键删除与边界（BaseMapper） | `BaseMapperDeleteCase(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ⚠️ 6/7 |
| &emsp;复合主键 CRUD（BaseMapper） | `BaseMapperCompositeKeyCase(12条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;批量主键冲突错误传递（JdbcTemplate） | `JdbcBatchConflictCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;逐行对象映射（RowMapper） | `JdbcRowMapperCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;逐行对象映射（Lambda RowMapper） | `LambdaRowMapperCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;空值与 Map 到对象映射（Lambda） | `LambdaNullResultMappingCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;显式主键插入（XML Mapper） | `XmlMapperExplicitKeyCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;字段名、默认列与实体/Map 映射（对象映射） | `AnnotationFieldMappingCase(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;插入、更新与只读字段策略（对象映射） | `AnnotationWritePolicyCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;忽略字段的读写与注解优先级（对象映射） | `AnnotationIgnoredFieldCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;样本条件中的忽略字段（对象映射） | `AnnotationIgnoredSampleCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| &emsp;仅映射显式注解字段（对象映射） | `AnnotationExplicitMappingCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;NULL 字段插入与显式更新（对象映射） | `AnnotationNullFieldMappingCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ⚠️ 1/2 | ⚠️ 1/2 | ✅ |
| &emsp;部分实体插入与字段更新（对象映射） | `AnnotationPartialFieldMappingCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;空字符串字段往返（对象映射） | `AnnotationEmptyFieldMappingCase(1条)` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;表定义（对象映射） | `MappingTableDefinitionCase(6条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;表元数据（对象映射） | `AnnotationTableMetadataCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;XML 映射元数据（对象映射） | `XmlMappingMetadataCase(13条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;Mapping 注册表（对象映射） | `MappingRegistryCase(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;继承映射（对象映射） | `AnnotationInheritanceCase(10条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;实体 JSON 字段的 Map/List/Set 与 specialJavaType 映射（对象映射） | `AnnotationJsonFieldMappingCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| &emsp;实体原生数组字段与 specialJavaType 映射（对象映射） | `AnnotationArrayFieldMappingCase(1条)` | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;SQL 模板（对象映射） | `AnnotationSqlTemplateCase(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| &emsp;手工主键与完整性校验（主键） | `AssignedKeyCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 1/3 | ❌ | — | — | — | ⚠️ 2/3 |
| &emsp;数据库生成主键回填（主键） | `DatabaseGeneratedKeyCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;客户端 UUID 生成（主键） | `UuidKeyCase(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;自定义主键处理器（主键） | `CustomKeyHolderCase(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 6/7 | ❌ | — | — | — | ⚠️ 6/7 |
| &emsp;序列键配置与执行（主键） | `SequenceKeyCase(3条)` | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;方言生成键策略（主键） | `InsertDialectStrategyCase(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;字段命名转换与注解覆盖（对象映射） | `NamingConversionCase(9条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;结果列名大小写匹配（对象映射） | `ResultColumnCasingCase(3条)` | ⚠️ 1/3 | ✅ | ✅ | ⚠️ 1/3 | ✅ | ⚠️ 1/3 | ✅ | ✅ | — | — | — | ✅ |
| &emsp;大小写敏感标识符（Schema） | `IdentifierCasingCase(4条)` | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;分隔符与关键字引用（Schema） | `IdentifierQuotingCase(6条)` | ⚠️ 5/6 | ✅ | ✅ | ⚠️ 5/6 | ✅ | ⚠️ 5/6 | ✅ | ❌ | — | — | — | ❌ |
| &emsp;标准物料的 JDBC 表/列元数据（Schema） | `StandardSchemaCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ❌ |
| **6. 参数传递** | | | | | | | | | | | | | |
| &emsp;Object 数组位置参数绑定（纯原生命令） | `JdbcPositionalParameterCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;PreparedStatementSetter 写入与查询参数（纯原生命令） | `JdbcStatementSetterParameterCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;命名参数、Map/Bean 与嵌套表达式（纯原生命令） | `JdbcNamedParameterCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;NULL 参数写入与读回（纯原生命令） | `JdbcNullParameterCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ |
| &emsp;可信标识符替换与值参数混用（纯原生命令） | `JdbcTextParameterCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;可信命令片段替换（纯原生命令） | `JdbcFragmentParameterCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;Array/Bean/Map 参数源混用（JdbcTemplate 组合场景） | `JdbcArgumentSourceParameterCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **7. SQL 规则** | | | | | | | | | | | | | |
| &emsp;AND/IN/SET 模板规则展开（JdbcTemplate） | `JdbcRuleParameterCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| **8. 类型处理器** | | | | | | | | | | | | | |
| &emsp;SqlArg 显式 TypeHandler 参数（纯原生命令） | `JdbcExplicitTypeParameterCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;按字段名读取 Date（TypeHandler，原生命令） | `NativeNamedFieldTypeCase.date_shouldRoundTripByFieldName` | — | — | — | — | — | — | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| &emsp;按字段名读取枚举名称（TypeHandler，原生命令） | `NativeNamedFieldTypeCase.enum_shouldRoundTripByFieldName` | — | — | — | — | — | — | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| &emsp;按字段名读取 true/false（TypeHandler，原生命令） | `NativeNamedFieldTypeCase.boolean_shouldRoundTripByFieldName` | — | — | — | — | — | — | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| &emsp;整型与浮点数往返（TypeHandler） | `BasicNumericTypeJdbcCase(1条)` / `RedisBasicNumericTypeJdbcTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;高精度小数往返（TypeHandler） | `BasicDecimalTypeJdbcCase(1条)` / `RedisBasicDecimalTypeJdbcTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ |
| &emsp;大整数往返（TypeHandler） | `BasicBigIntegerTypeJdbcCase(1条)` / `RedisBasicBigIntegerTypeJdbcTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ |
| &emsp;布尔值往返（TypeHandler） | `BasicBooleanTypeJdbcCase(1条)` / `RedisBasicBooleanTypeJdbcTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;布尔空值绑定与读取（TypeHandler） | `BasicBooleanNullJdbcCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| &emsp;ASCII 与 Unicode 字符往返（TypeHandler） | `BasicCharacterTypeJdbcCase(1条)` / `RedisBasicCharacterTypeJdbcTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;字符空值绑定与读取（TypeHandler） | `BasicCharacterNullJdbcCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ |
| &emsp;空字符串往返（TypeHandler） | `BasicEmptyStringJdbcCase(1条)` / `RedisBasicEmptyStringJdbcTest` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;枚举类型映射（TypeHandler） | `EnumTypeJdbcCase(5条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 4/5 | — | — | — | ✅ |
| &emsp;二进制类型（TypeHandler） | `BinaryTypeJdbcCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 3/4 | — | — | — | ❌ |
| &emsp;日期值往返（TypeHandler） | `TimeDateJdbcCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;JDBC 时间/时间戳与 LocalTime 转换（TypeHandler） | `TimeClockJdbcCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;LocalDateTime、Instant 与等价时刻转换（TypeHandler） | `TimeInstantJdbcCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;完整日期值的年、月、年月与月日提取（TypeHandler） | `TimePartialJdbcCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ❌ |
| &emsp;时间精度、日历与极值边界（TypeHandler） | `TimeBoundaryJdbcCase(3条)` | ⚠️ 2/3 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;儒略日与公元前日期往返（TypeHandler） | `TimeJulianDayJdbcCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;时间字段与映射空值（TypeHandler） | `TimeNullJdbcCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;对象、嵌套值与特殊字符的 JSON 写入序列化（TypeHandler） | `JsonSerializationJdbcCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| &emsp;单列 JSON 对象转 Map（TypeHandler） | `JsonMapJdbcCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;单列 JSON 数组与 Bean 数组转 List（TypeHandler） | `JsonListJdbcCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;单列 JSON 数组转 Set（TypeHandler） | `JsonSetJdbcCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;注解绑定的 JSON Bean 往返与空字段（TypeHandler） | `JsonAnnotatedBeanJdbcCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| &emsp;单列 JSON 空值绑定与读取（TypeHandler） | `JsonNullScalarJdbcCase(2条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| &emsp;JSON 空字段的行 Map 结果（TypeHandler） | `JsonNullRowJdbcCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ |
| &emsp;数组类型（TypeHandler） | `ArrayTypeJdbcCase(7条)` | ✅ | ⚠️ 1/7 | ✅ | ⚠️ 1/7 | ⚠️ 1/7 | ⚠️ 1/7 | ⚠️ 6/7 | ❌ | — | — | — | ✅ |
| &emsp;向量类型映射（TypeHandler） | `VectorTypeMappingCase(4条)` | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | — | — | — | ✅ |
| &emsp;TypeHandler 注册（对象映射） | `AnnotationTypeHandlerCase(7条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 4/7 | — | — | — | ✅ |
| **9. 结果接收** | | | | | | | | | | | | | |
| &emsp;连续 Map、列表与标量读取（JdbcTemplate 组合场景） | `JdbcMixedResultAccessCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;逐行回调（RowCallbackHandler） | `JdbcRowCallbackCase(1条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;结果集提取、过滤与 Pairs（ResultSetExtractor） | `JdbcResultExtractorCase(4条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — | — | ✅ |
| &emsp;多结果集（纯原生命令） | `JdbcMultipleResultSetCase(4条)` | ❌ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ | — | — | — | ✅ |
| **10. 数据库事务** | | | | | | | | | | | | | |
| &emsp;提交、回滚与回滚规则（事务） | `TransactionBoundaryCase(8条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;传播与跨代理调用（事务） | `TransactionPropagationCase(17条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;嵌套事务与保存点（事务） | `TransactionNestedCase(6条)` | ✅ | ✅ | ✅ | ⚠️ 2/6 | ⚠️ 2/6 | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;隔离级别配置与恢复（事务） | `TransactionIsolationCase(6条)` | ✅ | ✅ | ✅ | ⚠️ 5/6 | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |
| &emsp;JdbcTemplate、Session 与 Lambda 共享事务 | `TransactionApiParticipationCase(3条)` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | — | — | — | ❌ |

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
