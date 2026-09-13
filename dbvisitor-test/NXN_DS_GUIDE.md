# dbvisitor-test N×N 数据源接入指南

本文档描述当前 `dbvisitor-test` 测试工程的最终接入方式。后续新增数据源、补能力、减少 skip、解释支持状态时，统一以本文档和 `NXN_DS_CAPABILITY.md` 为准。

`NXN_DS_CAPABILITY.md` 已经承担长期支持性文档职责：它包含能力矩阵、测试类数量、跳过清单、当前结论、下一步改进方向和减少 skip 的操作方式。后续不再维护单独的能力清单文档。

## §1 接入目标

N×N 测试工程用于验证 dbVisitor 在真实数据源上的能力边界。这里的 N×N 不是“所有数据库跑同一批测试”，而是：

- 每个数据源有自己的 profile、连接配置、初始化物料和 realdb 测试实现。
- 每个通用能力由 `contract` 定义契约，由 `realdb/{env}` 绑定到具体数据源。
- 每个数据源只声明和验证自己真实支持的能力。
- 不支持或暂不覆盖的能力必须进入 profile feature gate，并反映到 `NXN_DS_CAPABILITY.md`。
- 专有 DSL 和专有数据结构必须留在对应数据源包内，不能上提到通用 contract。

完成接入后的用户应该能通过 `NXN_DS_CAPABILITY.md` 看清三件事：

1. 这个数据源支持哪些能力。
2. 哪些能力被跳过，以及为什么跳过。
3. 下一步如果要提升覆盖率，应先改哪里。

## §2 当前工程结构

当前测试代码按四个边界组织：

```text
src/test/java/net/hasor/dbvisitor/test/
  nxn/          N×N 运行模型、profile、能力标识、报告生成
  contract/     通用能力契约和通用测试物料
  realdb/       每个真实数据源自己的测试实现和专有物料
```

`nxn` 当前结构：

```text
nxn/
  capability/   @Capability、CapabilityId、FeatureId、SupportStatus
  config/       OneApiDataSourceManager
  env/          DataSourceId、DataSourceProfile、各数据源 Profile、ProfileRegistry
  junit/        AbstractNxnContractTest
  report/       CapabilityMatrixReport、NxnMetadataContractTest、metadata 子包
```

`contract` 当前结构：

```text
contract/
  api/
    adapter/
    jdbc/
    lambda/
    map_query/
    mapper/annotation/
    mapper/basemapper/
    mapper/xml/
    session/
    vector_query/
  feature/
    function/
    keygen/
    mapping/
    naming/
    procedure/
    schema/
    transaction/
    type/
  material/
    dao/
    handler/
    model/
    service/
```

`realdb` 当前结构：

```text
realdb/
  h2/
  mysql/
  pg/
  mssql/
  oracle/
  db2/
  clickhouse/
  redis/
  mongo/
  elastic6/
  elastic7/
  milvus/
```

关系型数据源通常按 `api`、`feature` 分层：

```text
realdb/mysql/api/jdbc/
realdb/mysql/api/lambda/
realdb/mysql/api/mapper/
realdb/mysql/api/session/
realdb/mysql/feature/type/
realdb/mysql/feature/keygen/
realdb/mysql/feature/transaction/
```

非关系型或 adapter 数据源可以保留更贴近自身模型的结构，例如 `realdb/mongo/material`、`realdb/elastic7/material`、`realdb/redis/dto1`。

N×N 报告元数据测试不属于某个业务数据源测试范畴，统一放在：

```text
nxn/report/NxnMetadataContractTest.java
nxn/report/metadata/{Env}NxnMetadataContractTest.java
```

## §3 资源结构

当前资源按连接配置、初始化脚本、通用 Mapper/Mapping、数据源专有物料分层：

```text
src/test/resources/
  jdbc-{env}.properties
  sql/{env}/init.sql
  sql/{env}/sql_template.sql
  mapper/
  mapping/
  session/
  realdb/{env}/
```

规则：

- `jdbc-{env}.properties` 只描述一个数据源的连接参数，不再承载 feature gate。
- `sql/{env}/init.sql` 是该数据源的初始化入口，必须幂等。
- `mapper/`、`mapping/`、`session/` 只放可跨数据源复用的通用物料。
- 数据源专有 XML、DSL、DTO、脚本、向量/索引/集合物料必须放在 `realdb/{env}` 对应目录。
- XML 文件不天然等于通用 Mapper 能力；Mongo、Elasticsearch、Milvus、Redis 等 adapter 的 XML 往往承载专有 DSL，必须留在数据源内部。

## §4 能力模型

每个测试点通过 `@Capability` 绑定稳定能力编号：

```java
@Test
@Capability(CapabilityId.JDBC_CRUD_INSERT)
public void jdbcCrudInsert() {
    ...
}
```

能力状态由 `DataSourceProfile.support(capabilityId)` 判断，主要状态来自 `SupportStatus`：

```text
SUPPORTED
UNSUPPORTED_BY_DATABASE
UNSUPPORTED_BY_DRIVER
UNSUPPORTED_BY_DBVISITOR
NOT_IMPLEMENTED
```

feature gate 由 `FeatureId` 和 `DataSourceProfile.supportsFeature(featureId)` 表达。contract 中遇到数据库真实能力差异时，应优先使用 profile/feature 描述，而不是在测试里复制大量 `if mysql`、`if pg`、`if h2`。

能力编号应稳定表达“能力域 + API 入口 + 场景”，例如：

```text
jdbc.crud.insert
lambda.query.predicate.in
mapper.xml.result-map
mapper.annotation.parameter.bind
basemapper.crud.composite-key
type.json.round-trip
keygen.auto.uuid
transaction.propagation.requires-new
procedure.call.out-param
adapter.mongo.bson.round-trip
adapter.milvus.vector.knn
adapter.redis.hash.crud
```

## §5 Profile 接入

新增数据源必须补齐 `nxn/env` 下的三个位置：

1. 在 `DataSourceId` 增加 env 标识。
2. 增加 `{Env}Profile`，通常继承 `AbstractDataSourceProfile`。
3. 在 `DataSourceProfileRegistry.all()` 注册 profile。

Profile 至少要回答：

- 数据源 env 是什么。
- 当前数据源支持哪些 capability。
- 当前数据源跳过哪些 feature。
- 跳过原因属于数据库、驱动、dbVisitor 还是未实现。
- 标识符、分页、批量、事务、主键、过程/函数、类型系统等差异如何表达。

示意：

```java
public enum DataSourceId {
    NEWDB("newdb");
}
```

```java
public final class NewDbProfile extends AbstractDataSourceProfile {
    public static final NewDbProfile INSTANCE = new NewDbProfile();

    private NewDbProfile() {
        super(DataSourceId.NEWDB);
    }

    @Override
    protected Set<String> unsupportedFeatures() {
        return setOf(FeatureId.PROCEDURE, FeatureId.VECTOR);
    }
}
```

profile 是差异的稳定入口。不要通过 contract 中的数据库名分支隐藏差异。

## §6 Contract 编写规则

`contract` 只放通用能力契约。通用 contract 应满足：

- 类通常是抽象类，并继承 `AbstractNxnContractTest`。
- 测试方法使用 `@Test` + `@Capability`。
- 断言只描述跨数据源成立的 dbVisitor 通用语义。
- 使用 `contract/material` 中的通用 DTO、DAO、TypeHandler、Service。
- 需要差异时调用 `profile()`、`requiresNxnFeature(...)` 或 `AbstractNxnContractTest` 提供的 helper。

contract 不应做：

- 写死某一个数据源的 SQL 或 DSL。
- 连接其它数据源。
- 读取某个数据源配置后改变测试语义。
- 把专有 mapper、专有 XML、专有 DTO 放进通用物料。
- 长期散落数据库名判断。
- 用 `Assume` 隐藏一个本应 `SUPPORTED` 的能力缺口。

如果一个失败场景只有某个数据源支持，或者同名 API 背后的数据库语义不同，应下沉到 `realdb/{env}`，不要强行塞进通用 contract。

## §7 RealDB 绑定规则

`realdb/{env}` 下的类负责把通用 contract 绑定到具体数据源。典型类应该很薄：

```java
package net.hasor.dbvisitor.test.realdb.mysql.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlJdbcCrudTest extends JdbcCrudCase {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
```

RealDB 可以做：

- 选择当前数据源 profile。
- 覆盖数据源专有 setup/cleanup。
- 引入 `realdb/{env}/material` 中的专有 XML、DTO、Mapper、DSL。
- 编写数据源专有能力测试。
- 用 profile/feature 明确表达不支持原因。

RealDB 不应做：

- 复制 contract 中的大量通用断言。
- 连接其它数据源或复用其它数据源结果。
- 把专有 SQL 放回 `contract`。
- 通过空实现、永久 `@Ignore` 或无解释 skip 规避缺口。

## §8 数据源专有能力

专有能力也是 N×N 的一部分，但必须留在数据源自己的包内：

- Mongo：BSON、Mongo Query、集合语义。
- Elasticsearch 6/7：Search DSL、Index DSL、版本 endpoint 差异。
- Milvus：collection、vector、index、load/search。
- Redis：key/hash/list/set/zset 结构。
- ClickHouse：MergeTree、`join_use_nulls`、OLAP mutation 语义、专有函数。

专有测试仍然要声明 `@Capability`，能力编号应体现 adapter 或数据源域，避免和通用能力混淆。支持性文档按职责区分：dbVisitor 整合能力统一回填 `NXN_DS_CAPABILITY.md` 的 §2 主表；适配器对数据库 SDK 的命令、参数和返回语义覆盖维护在该数据源的 `dbvisitor-doc` 用户文档中，不在 N×N 文档另设专有能力表。

## §9 运行方式

单数据源运行：

```bash
./runnxn.sh mysql
./runnxn.sh pg
./runnxn.sh milvus
```

全量顺序运行：

```bash
./runnxn.sh all
```

脚本实际执行：

```bash
./gradlew :dbvisitor-test:test -Pnxn.env={env} --rerun-tasks
```

以上命令在仓库根目录执行。`nxn.env` 必须和 `DataSourceProfile.env()` 匹配。Gradle 按 env 选择 `realdb/{env}` 测试目录，`es6`/`es7` 对应 `elastic6`/`elastic7`；`AbstractNxnContractTest` 还会检查当前 profile。未设置 `nxn.env` 时，`:dbvisitor-test:test` 不执行。`runnxn.sh all` 顺序运行各数据源，需要相应数据库服务均可用。

新增数据源后需要把 env 加入 `runnxn.sh` 的数据源列表；若 env 与目录名不同，还应同步 `dbvisitor-test/build.gradle` 的映射。每次运行会更新该模块同一份测试报告，跨数据源对比前需分别保留结果，不能只看最后一次报告。

## §10 报告和长期文档

后续支持性文档统一使用：

```text
NXN_DS_CAPABILITY.md
```

这份文档应持续包含：

- `§2 能力矩阵`：按能力维度聚合，二级项目按业务场景排序。
- 测试类列：类名后追加测试数量，例如 `JdbcCrudCase(4条)`。共用场景以 `Case` 结尾，数据源实现以 `Test` 结尾。
- 分组顺序与核心 API 目录一致。同一数据源、同一场景的测试集中在一个类中；支持边界不同的场景分别列类、列行，不以减少类数量为目标。
- 二级能力项：使用 `&emsp;` 缩进，区别于一级能力维度。
- 数据源状态：`✅` 全部通过，`⚠️ x/y` 表示 y 个场景中有 x 个通过，`❌` 不支持或全部验证失败，`—` 无绑定或未验证。不能将未验证直接写成不支持。
- `§3 如何理解验证范围`：说明覆盖边界，并列出各数据源最近一次完整集合的验证结果。
- `§4 运行与维护`：提供运行入口和矩阵维护规则。

能力矩阵统一维护在这份文档中，不另建重复清单。

## §11 新数据源接入流程

### 11.1 盘点能力边界

接入前先回答：

1. 该数据源属于关系型、adapter，还是专有向量/搜索/缓存数据源。
2. 哪些通用 contract 可以继承。
3. 哪些能力只能作为数据源专有测试。
4. 哪些 feature 应标记为不支持。
5. 需要哪些标准表、集合、索引、mapper、DTO、TypeHandler。

分类结论必须能解释：一个 case 为什么能进入 `contract`，或为什么必须留在 `realdb/{env}`。

### 11.2 增加环境物料

需要新增或调整：

```text
src/test/resources/jdbc-{env}.properties
src/test/resources/sql/{env}/init.sql
src/test/resources/sql/{env}/sql_template.sql
src/test/resources/realdb/{env}/...
```

非 SQL 数据源如果不使用 `init.sql`，也要通过该数据源自己的 setup hook 完成等价初始化。

### 11.3 增加 profile

需要新增或调整：

```text
DataSourceId
{Env}Profile
DataSourceProfileRegistry
runnxn.sh
```

如果 dbVisitor provider 中没有对应方言或 adapter 能力，需要先补 provider，再接入 N×N。

### 11.4 绑定通用 contract

为该数据源支持的通用能力建立 realdb 薄类。关系型数据源建议保持现有分层：

```text
realdb/{env}/api/jdbc/
realdb/{env}/api/lambda/
realdb/{env}/api/map_query/
realdb/{env}/api/mapper/
realdb/{env}/api/session/
realdb/{env}/api/vector_query/
realdb/{env}/feature/type/
realdb/{env}/feature/keygen/
realdb/{env}/feature/transaction/
realdb/{env}/feature/procedure/
realdb/{env}/feature/function/
realdb/{env}/feature/schema/
realdb/{env}/feature/naming/
realdb/{env}/feature/mapping/
```

不支持的能力不要建空类；应在 profile 中表达不支持，并在矩阵中呈现。

### 11.5 增加元数据报告类

新增数据源还要增加一条报告元数据绑定：

```text
src/test/java/net/hasor/dbvisitor/test/nxn/report/metadata/{Env}NxnMetadataContractTest.java
```

并检查 `NxnMetadataContractTest`、`NxnMetaTest`、`CapabilityMatrixReport` 是否能识别该数据源的 metadata 类。这个类用于验证当前数据源的 contract 绑定、能力注解、矩阵报告生成，不放在 `realdb/{env}` 下。

### 11.6 增加专有测试

专有测试放在 `realdb/{env}`，物料放在 `realdb/{env}/material` 或资源目录 `src/test/resources/realdb/{env}`。专有测试也必须使用 `@Capability`。其中的 dbVisitor 整合验证可作为主表对应能力的部分覆盖证据，但不能替代完整通用契约通过；纯数据库 SDK/SQL 覆盖及版本限制写入数据源用户文档。

### 11.7 运行验证

建议顺序：

```bash
./gradlew :dbvisitor-test:test -Pnxn.env=milvus --tests '*MilvusDiagnosticsSqlTest' --rerun-tasks
./runnxn.sh milvus
# 所有数据源服务均就绪后，才运行全量：
./runnxn.sh all
```

按待接入数据源替换示例中的 env 和测试类。验证时必须核对 Gradle 输出、`dbvisitor-test/build/test-results/test/TEST-*.xml`、HTML 报告及矩阵描述，区分通过、跳过和实际未选择的测试。HTML 报告位于 `dbvisitor-test/build/reports/tests/test/index.html`。不要只依赖退出码判断覆盖范围。

默认 Gradle 任务只选择 `realdb` 目录，不执行 `nxn/report/metadata` 下的元数据测试；仅添加 `--tests '*NxnMetadataContractTest'` 不能突破该目录过滤。元数据检查需通过 IDE 单独运行已有的具体元数据测试类（设置 `nxn.env`），或使用明确选择该类的独立测试配置；不能把 SQL 回归成功记为元数据检查通过。

### 11.8 更新矩阵

完成接入或能力变更后，更新 `NXN_DS_CAPABILITY.md`：

- 新增或调整矩阵列。
- 新增测试类后更新类名后的测试数量。
- 新增 skip 后更新跳过清单。
- 能力打开后更新状态、当前结论和下一步方向。
- 保持一级维度是能力聚合，二级项目按业务场景排序。

## §12 减少 skip 的流程

每次减少 skip 只选择一个 feature：

1. 阅读 feature 对应的 contract 断言。
2. 判断目标数据源是否能提供等价语义。
3. 能支持的，将 SQL、DDL、XML mapper、fixture 下沉到 `realdb/{env}` 或 `sql/{env}`。
4. 移除 `{Env}Profile` 中的 feature。
5. 跑目标数据源代表类。
6. 跑目标数据源全量。
7. 更新 `NXN_DS_CAPABILITY.md` 的矩阵、跳过清单和下一步方向。

如果确认是数据库或驱动语义差异，不要为了减少 skip 弱化 contract，应保留 feature gate 并在矩阵中说明原因。

## §13 验收标准

新数据源接入完成时必须满足：

- `DataSourceId`、`{Env}Profile`、`DataSourceProfileRegistry` 已补齐。
- `jdbc-{env}.properties` 和初始化/setup 物料已补齐。
- `runnxn.sh {env}` 可以单独运行。
- `realdb/{env}` 下有清晰的数据源实现结构。
- 支持的通用能力都有 realdb 绑定类。
- 不支持能力进入 profile/feature gate，并有原因。
- 专有能力留在 `realdb/{env}`，不污染 `contract`。
- 每个测试点都有 `@Capability`。
- 元数据报告类位于 `nxn/report/metadata`。
- Gradle 测试 XML、HTML 和 N×N 报告已检查。
- `NXN_DS_CAPABILITY.md` 已更新，并能说明当前支持状态。

## §14 禁止模式

以下做法不应再进入 N×N 测试：

- 新增跨多个数据源的 suite 类。
- 一个测试类连接多个数据源。
- 用 H2 代表 MySQL，或用一个数据源补另一个数据源没跑的场景。
- 只加初始化脚本，不补 profile、contract 绑定和矩阵说明。
- 把数据源专有 DSL 移入 `contract`、`mapper/` 或 `mapping/` 通用目录。
- 按目录名判断测试是否通用，而不读断言和语义。
- 用 `@Ignore`、空实现、永久 Assume 隐藏 `SUPPORTED` 能力缺口。
- 在 realdb 中长期复制大量通用断言。
- 继续维护已经被 `NXN_DS_CAPABILITY.md` 替代的能力清单文档。
