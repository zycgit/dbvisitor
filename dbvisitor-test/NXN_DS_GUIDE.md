# N×N 测试与文档维护指南

N×N 验证 dbVisitor 的同一项 API 能力在不同数据源上的实际行为。分类以核心 API 为骨架，测试代码通过注解直接关联[数据源差异](../dbvisitor-doc/docs/features/overview.md)使用的[兼容性 JSON](../dbvisitor-doc/src/data/capabilities)。不再另行维护 Markdown 能力矩阵。

## 1. 分类与归属

| 测试范围 | 代码位置 | 文档与统计方式 |
| --- | --- | --- |
| 通用 API 能力 | `contract` 定义，`realdb/{env}` 实现 | 关联兼容性 JSON 的能力列，按测试方法统计 |
| JDBC 元信息 | `contract/api/jdbc/metadata` | 保留真实数据源测试，说明放在各驱动文档，不计入 API 差异表 |
| 数据源专有功能 | `realdb/{env}` | 随该数据源回归，用法放在数据源文档，不扩大通用能力的分母 |
| 原生语句综合场景 | `scenario`，由 `realdb` 入口执行 | 保留综合回归，不重复计入通用能力列 |
| 框架单元测试 | `unit` | 不连接数据库，也不重复计入每个数据源 |
| 契约与报告检查 | `nxn/report` | 检查分类、绑定及报告，不作为业务能力 |

SQL、Redis 命令、Mongo DSL、Elasticsearch 请求等均为原生命令。测试关注 API 调用、参数、映射和结果处理，不因命令不是 SQL 而判为不支持。

### 核心 API 与能力文件

| 核心 API | 主要契约目录 | JSON 文件 |
| --- | --- | --- |
| 5.1 编程式 API | `api/jdbc` | `jdbc.json` |
| 5.2 Mapper API | `api/mapper/annotation`、`api/mapper/basemapper`、`api/session` | `mapper.json` |
| 5.3 构造器 API、5.5 Map 查询模式 | `api/lambda`（Map 场景位于 `map_query` 子包） | `builder.json` |
| 5.4 Mapper 文件 | `api/mapper/xml`、`api/session` | `mapper-files.json` |
| 5.6 向量查询、8.9 向量类型处理器 | `api/vector_query` | `vectors.json`（向量操作） |
| 5.7 对象映射 | `feature/mapping`、`feature/naming`、`feature/keygen` | `mapping-keys.json` |
| 6. 参数传递与规则 | `feature/parameter`、`feature/rule` | `parameters.json` |
| 8. 类型处理器 | `feature/type` | `types.json` |
| 9. 结果接收 | `feature/result` | `results.json` |
| 10. 数据库事务 | `feature/transaction` | `transactions.json` |

目录相对于 `src/test/java/net/hasor/dbvisitor/test/contract`；表格 JSON 位于 `dbvisitor-doc/src/data/capabilities/tables`。

兼容性配置分成两个维度：

```text
dbvisitor-doc/src/data/capabilities/
├── sources.json          数据源名称、排列顺序和介绍链接
├── tables/               测试维度：表格分组、能力列及核心 API 链接
│   ├── jdbc.json
│   └── ...
└── datasources/          数据源能力：执行结果及差异说明链接
    ├── mysql.json
    └── ...
```

`contract` 定义要验证的能力，`realdb` 指定实际数据源。文档用这两个维度组合出表格：能力为列，数据源为行。`sources.json` 只管理展示顺序和入口，不重复记录能力结果。

同一场景的测试集中在一个契约类中；相同能力的边界测试可以汇总到同一列。不同 API 入口分别统计，不能用 JdbcTemplate 的通过结果代替构造器 API 的通过结果。拆分或迁移时保留方法、断言、能力编号和各数据源的实际绑定。

## 2. 在测试代码中关联能力列

每个参与兼容性矩阵的测试方法都必须填写 `@Capability.column`，并在旁边用简短注释说明能力归属。格式固定为 **表格 ID / 分组 ID / 能力列 ID**，分别对应表格 JSON 中的三层 `id`。显示标题、翻译或菜单顺序变化不需要修改这个标识。

例如 [JdbcBeanQueryCase](src/test/java/net/hasor/dbvisitor/test/contract/api/jdbc/JdbcBeanQueryCase.java) 中的查询方法对应 `tables/jdbc.json` 中 `queries` 分组的 `queries` 列：

```java
@NxnContract
public abstract class JdbcBeanQueryCase extends JdbcQuerySupport {
    // 能力归属：编程式 API / 查询 / 实体结果。
    @Test
    @Capability(value = CapabilityId.JDBC_QUERY_BEAN,
            column = "jdbc/queries/queries")
    public void jdbcQueryForList_shouldReturnBeans() throws SQLException {
        // 准备数据、执行查询并断言实体结果。
    }
}
```

定位关系：

| 测试代码 | JSON 位置 |
| --- | --- |
| `column = "jdbc/queries/queries"` | `tables/jdbc.json` → `queries` 分组 → `queries` 列 |
| RealDB 使用 `MySqlProfile`，其 `env()` 为 `mysql` | `datasources/mysql.json` |
| 该测试在 MySQL 上的结果 | `capabilities["jdbc/queries/queries"]` |

同一 API 使用维度或同一能力的边界场景归入同一列。例如普通数组和 NULL 数组都归入 `types/array-handlers/arrays`。每个方法仍要明确填写 `column`，不从类上继承默认值。

位置参数、名称参数及规则不按 API 重复设列。方法注解和 Session 文件调用中的参数测试也绑定 `parameters.json`；API 特有的多行注解拼接、XML 引用和构造器条件行为保留各自分类。通用规则验证条件展开与原生命令执行；SQL 片段规则验证 AND、OR、IN、SET 生成的语法，不把 SQL 语法不适用判为通用规则不支持。

类型处理器按基础、二进制、时间、枚举、JSON、数组、自定义处理器归类。日期精度、空值等是列内测试场景，不再单独设列；按字段名读取也按实际字段类型归类。向量映射与读写归属 `vectors/vectors/vector-mapping`，与检索一起展示在“向量操作”中，不重复统计。

对象映射按字段映射、命名转换、结果列匹配、标识符处理、写入策略、JSON 字段、语句模板和主键策略归类。忽略字段归入字段映射；只读、NULL 和部分字段归入写入策略；各主键策略保留独立测试，统一绑定 `mapping-keys/key-generators/strategies`。实体数组和空字符串往返分别归入类型表的数组类型、基础类型。

编程式 API 按更新、查询、查询键值对、批量化、多结果、存储过程与函数归类。CRUD 中的查询按实际行为归入查询；列名匹配、单值与列表读取保留各自测试。批量错误归入批量化；`call` 收集结果归入多结果，不等同于存储过程支持。过程与函数合并展示，但保留不同调用方式的独立测试。

Mapper API 按方法注解、Mapper 读写、主键策略、分页查询、执行选项、调用构造器、引用文件 Mapper 和 Session 管理归类。滚动结果归入执行选项；默认主键、生成键、selectKey 与复合主键归入主键策略。Session 测试按实际调用分类：BaseMapper CRUD 归入 Mapper 读写，`lambda()` 调用归入调用构造器，不能仅凭测试类名归类。各数据源的状态链接指向 `mapper` 专题中的对应章节。

Mapper 文件按命令执行、执行选项、主键策略、存储过程调用、sql 标签、动态 SQL、映射结果集、分页查询和调用文件 Mapper 归类。Session 直接调用按实际功能归入同一列，不另分一组；文件加载与命名空间查找归入命令执行。主键策略包括手工赋值、生成键回填和 selectKey，保留各自测试。

构造器 API 按写入操作、写入冲突、查询操作、Map 查询模式、分页查询、条件构造器、条件参数、分组和排序归类。新增、更新、删除及影响条数、空条件保护、跨 API 回读归入写入操作；CRUD 中的查询方法归入查询操作。字段选择、计算列、计数和去重归入查询操作；分页迭代归入分页查询；比较、区间、集合、LIKE、NULL、空字符串与条件组归入条件构造器。条件参数保留构造器的自动绑定场景，不与手写命令的位置参数重复计数。

构造器能力的状态链接统一指向各数据源的 `builder` 专题，并落到与列名对应的章节。方言没有实现的操作标为不支持；原生命令片段写错、输入超过数据库限制或字段存储语义不同，不直接判为 API 不支持。测试应使用合法输入并验证实际行为，例如 Oracle 大集合由调用方分批、空字符串按 NULL 查询，SQL Server 拒绝重复排序列后以正确的排序配置查询。不得通过删除边界断言或手工将跳过结果改成通过来提高支持率。

类内方法实际验证不同能力时，应分别归类。例如执行 DELETE 并检查影响数属于命令执行，不能因为随后查询验证而归入映射结果集；多行命令拼接也不归入执行选项。

- 一个方法只有一个主要能力列，不在两列重复计数。
- 一个数据源只能有一个类绑定同一契约；继承方法和对应的重写方法不重复计数。
- 重写测试方法时保留 `@Test`、原 `@Capability` 的 `value` 和 `column`，不能更换场景含义或转移到另一列。仅继承方法时无需重新声明注解。
- `CapabilityId` 标识具体测试场景，`column` 标识用户文档中的汇总能力，二者不是同一个编号。

### 按属性种类统计

默认按测试方法统计。固定枚举能力可以在表格列中声明 `variants`，例如事务传播的 7 种属性；对应测试用 `@Capability(variants = { "NESTED" })` 标明验证哪种属性。一个综合场景可以关联多种属性，但测试明细仍只保留一条。

每种属性的所有相关测试通过才计入完整支持数量。数据源 JSON 的 `passed/total` 此时表示完整支持属性数/属性总数，`variants` 保存各属性的状态及测试计数。全部属性支持时显示“支持”（事务传播显示“全部”），部分属性完整支持时显示“部分 x/y”。没有完整支持的属性、但存在局部可用场景时，状态为 `limited`，显示“有限”，不展示比例；必须链接到数据源说明，区分框架行为与数据库事务效果。所有场景均不支持时才标为不支持。

表格列中声明的每种属性必须有测试；方法必须标记合法属性，数据源重写方法时须保留相同标记。原始逐方法结果仍保存在 `compatibility.json`，不能用属性计数替代测试明细。

### 设置能力与事务前提

隔离级别列用 `availability: "transaction.template.isolation"` 指定隔离级别设置的契约场景；它验证框架设置及当前 JDBC 连接的实际值。该列只显示支持或不支持，不按数据库接受的隔离级别数量计算比例。其余隔离测试仍参与回归并保留明细；失败仍阻止导出，明确不支持某个级别则在数据源文档中说明。`availability` 必须对应本列唯一的测试场景，支持和不支持都必须有说明链接。

依赖真实事务的调用方式列声明 `requiresTransaction: true`。数据源 profile 未提供 `FeatureId.TRANSACTION` 且测试未证明完整支持时，展示“有限”，并强制链接到数据源的事务支持说明。此规则只调整用户文档的提示，不把跳过场景转为通过，也不修改原始测试计数。

`@NxnContract` 只标记契约及其统计范围，不再配置能力列。JDBC 元信息契约使用 `@NxnContract(scope = NxnContract.Scope.JDBC_METADATA)`；报告基础设施使用 `INFRASTRUCTURE`。这两类方法不填写 `column`。矩阵方法遗漏 `column`、JSON 列没有测试、重复绑定、重写后丢失能力编号或归属都会使检查失败。

## 3. 编写契约与数据源实现

### 通用契约

1. 先选定被测 API 和能力列，再编写契约；不要为了加入矩阵而创建数据库 SQL 语法分类。
2. 用 `@Test`、`@Capability(value = ..., column = "...")` 标识方法，加上能力归属注释，断言该场景实际执行后的结果。
3. 通用 DTO、Mapper、处理器放在 `contract/material`。专有 DSL、表模型和初始化物料留给数据源实现。
4. 辅助步骤不能阻断本来可独立验证的主功能；需要时拆成各自有断言的场景，不能删掉断言来换取通过。

### RealDB 绑定

使用现有契约时，绑定类只需指定 profile。以下是现有 MySQL CRUD 入口：

```java
public class MySqlJdbcCrudTest extends JdbcCrudCase {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
```

`MySqlProfile.env()` 为 `mysql`，对应 `datasources/mysql.json`。不需要再增加数据源注解或为继承的方法编写空壳重写。

数据源实现可以覆盖初始化、清理及命令生成钩子，使用自己的 XML、DTO 或原生命令。重写测试方法时必须保留等价断言；不能用空方法、永久 `@Ignore` 或只调用 SDK 的测试代替 dbVisitor API 验证。

### 明确能力限制

限制由 `DataSourceProfile.support(capabilityId)` 或 feature gate 声明。根据真实原因使用 `UNSUPPORTED_BY_DATABASE`、`UNSUPPORTED_BY_DRIVER`、`UNSUPPORTED_BY_DBVISITOR`；profile 中应说明具体原因，用户需要注意的行为放在该数据源文档中。

测试方法需要检查一个已声明的 feature 时，使用：

```java
requiresNxnFeature(FeatureId.ARRAY);
```

运行器将这些明确限制记为 `unsupported`。普通 `Assume`、`NOT_IMPLEMENTED`、环境不匹配和原因不明的跳过记为未验证，不能更新对应能力列。数据库连接失败、初始化失败和断言失败是测试失败，不是不支持。

整个契约无法绑定时，只允许 profile 对每个方法都声明明确限制；导出明细会保留“未绑定及其限制层”的依据。`SUPPORTED` 声明本身不能产生通过结果，缺少实际绑定或执行记录都会阻止更新。

## 4. 运行测试与更新文档

以下命令在仓库根目录执行。

真实数据源测试会初始化并清理测试表、集合或键，请使用独立测试库。

```bash
# 框架单元测试，包括兼容性导出器的保护测试
./gradlew :dbvisitor-test:test

# 检查测试代码与 JSON 的关联，不连接数据库
./gradlew :dbvisitor-test:checkNxnMapping

# 单数据源 / 全部真实数据源，只运行测试
./runnxn.sh h2
./runnxn.sh all

# 运行完整集合，并更新相应数据源的兼容性结果
./runnxn.sh h2 --update-docs
./runnxn.sh all --update-docs

# 等价的 Gradle 更新入口
./gradlew :dbvisitor-test:updateNxnDocs -Pnxn.env=h2

# 定位问题时可以筛选，但筛选结果不能更新完整矩阵
./gradlew :dbvisitor-test:test -Pnxn.env=redis --tests '*RedisJdbcMapQueryTest'
```

支持的环境标识见 `DataSourceId` 和 `runnxn.sh --help`；`elastic6`、`elastic7` 是脚本对 `es6`、`es7` 的别名。不指定环境只运行 `unit`，不会自动连接全部数据库。

普通测试不会改文档。`--update-docs` 会重新运行测试，并在全部关联方法得到确定结果后只更新 `datasources/{env}.json`，不会改动表格定义和其他数据源。不能用 `-x test` 跳过测试，也不会使用旧的筛选结果。`all` 按数据源依次运行，遇到失败停止，已成功更新的数据源结果保留，不能把它们视为全部完成。

### 结果文件

| 位置（相对于 `dbvisitor-test`） | 内容 |
| --- | --- |
| `build/test-results/nxn/{env}` | JUnit XML，包含该数据源完整测试集合 |
| `build/reports/tests/nxn/{env}` | 可浏览的 HTML 测试报告 |
| `build/nxn/{env}/run.json` | 本次运行标识、是否完成、失败及筛选状态 |
| `build/nxn/{env}/cases` | 实际测试方法的结果、能力编号及限制原因 |
| `build/nxn/{env}/compatibility.json` | 成功导出后的逐列明细，可追溯到契约和执行类 |

不同数据源的报告分别保存；同一数据源再次运行会替换自己的报告。运行标识和测试类指纹用于拒绝混入旧记录。上述文件是构建产物，不提交到源码仓库。

原有 `CapabilityMatrixReport` 输出的是 profile 声明和绑定清单，不是执行结果，不用于更新用户文档。

## 5. JSON 和用户文档怎么维护

### 表格定义

`tables/{table}.json` 只定义分组、列名、顺序及核心 API 用法链接，不存放任何数据源的测试结果。例如 `tables/jdbc.json` 中的一列：

```json
{
  "id": "queries",
  "title": { "zh-cn": "查询", "en": "Queries" },
  "href": "/docs/guides/core/jdbc/query"
}
```

这列位于 `queries` 分组中，完整标识就是 `jdbc/queries/queries`。

### 数据源能力

`datasources/{env}.json` 按完整能力标识保存结果。例如：

```json
{
  "schemaVersion": 1,
  "id": "mysql",
  "capabilities": {
    "jdbc/queries/queries": {
      "status": "supported",
      "passed": 16,
      "total": 16
    }
  }
}
```

以上仅展示一个能力项；完整文件必须包含表格定义中的全部能力列。

测试只更新当前数据源各能力的 `status`、`passed`、`total`。表格定义、数据源顺序和差异链接由文档维护。中英文页面共用这份结果，不需要分别改勾选状态。

| 状态 | 计算方式 |
| --- | --- |
| `supported` | 所有关联测试方法通过 |
| `unsupported` | 所有关联方法均有明确能力限制 |
| `partial` | 一部分通过，其余均为明确限制；展示 `passed/total` |

默认分母按契约方法的并集计算，不是 SQL 条数、断言条数或实现类数量。失败、漏跑、筛选运行、无法确认原因的跳过、旧记录和不完整运行均阻止写入，不会自动改成“不支持”。

结果接收的三列使用 `variants` 按四个 API 入口汇总：`jdbc`（编程式 API）、`mapper`（方法注解）、`builder`（构造器 API）、`mapper-file`（Mapper 文件）。测试方法通过 `@Capability(variants = { "jdbc" })` 标记入口。一个入口的关联测试全部通过才计为支持一次；原始测试条数和结果保留在明细中，不替代 API 入口数量。不要把同一个结果处理测试同时统计到 Mapper 或构造器表中。部分支持链接到数据源自己的结果接收文档，逐一说明四个入口。

部分支持必须在该数据源的能力结果对象中配置 `href`，指向说明具体限制的页面或章节，而不是笼统介绍页。中英文页面可以共用固定锚点：

```json
{
  "href": "/docs/features/mongo/write#insert-conflict"
}
```

导出器保留链接，不会自动撰写说明或创建锚点。发布前构建中英文文档检查链接。

结果接收页面只显示“支持／部分／不支持”，不展示比例；JSON 中的入口计数和测试明细仍完整保留，不因页面展示方式而删减。

构造器 API 页面同样不展示测试通过率；合并列的部分支持链接必须覆盖该列实际受限的用法。JSON 保留逐方法汇总计数，不能因列变少而删减边界场景。

### 增加或调整能力

1. 已有能力的边界用例直接归入现有 `column`；同一能力不因参数形态、NULL、批次大小或调用入口不同而重复增加列。新增独立能力时，才在 `tables` 中增加列。
2. 新列配置稳定 `id`、中英文标题、核心 API 链接；在每个 `datasources/{env}.json` 中增加相同完整标识的结果对象。尚未验证时用空对象 `{}` 暂存，不能预填“不支持”；补全测试并完成全量导出后再提交或发布。
3. 添加契约和 realdb 实现，运行 `checkNxnMapping`。单纯修改标题不改 ID；调整归属时同步修改注解。
4. 更新受影响数据源的差异说明及其章节链接。通用用法留在核心 API，数据库或驱动的特殊行为才放在数据源章节。
5. 运行受影响数据源；新增通用场景或改变共同实现后运行全部数据源并导出结果。
6. 检查 JSON diff 和逐列执行明细，再构建文档。不要删除旧场景、复制其他数据源结论或手工改状态来绕过失败。

归并列时，同步修改所有方法坐标、中文归属注释、表格定义和每个数据源的能力键。保留原测试方法与断言；按合并后方法的并集重新统计，不能平均原来的通过率。若形成新的部分支持项，应补充该数据源的具体说明链接。

## 6. 接入新数据源

1. 确认服务端、JDBC 驱动版本及可用环境；嵌入式数据库使用真实引擎，不用 H2 兼容模式代替。
2. 在 `nxn/env` 增加 `DataSourceId`、profile，并注册到 `DataSourceProfileRegistry`。参考已有 profile 声明限制，不把环境缺失列为不支持。
3. 添加 `jdbc-{env}.properties`、幂等的 `sql/{env}/init.sql` 及专有物料。连接配置不承载 feature gate。
4. 在 `realdb/{env}` 绑定通用契约，补充专有回归；不能跨数据源继承执行入口。
5. 同步 `build.gradle` 的环境选择、`runnxn.sh`、`sources.json` 以及报告基础设施的环境入口；新增 `datasources/{env}.json`，包含所有表格能力标识。不必修改已有表格定义或其他数据源文件。
6. 完整运行该数据源并导出结果；发现框架缺陷时修复并回归相关数据源，不为提高支持率模拟数据库本不支持的事务、关联或约束。

资源约定：通用 Mapper、映射和 Session 物料分别放在 `mapper`、`mapping`、`session`；专有 XML、DSL、DTO、索引及向量物料放在 `realdb/{env}`。容器配置位于 `docker`。

## 7. 待接入数据源

以下为接入顺序，不代表已经通过验证；按上一节的步骤接入，不预先写入支持结论。

| 顺序 | 数据源或驱动组合 | 重点 |
| --- | --- | --- |
| 优先 | SQLite、HSQLDB、Derby | 真实嵌入式引擎的类型、分页、写入、主键与事务 |
| 优先 | MariaDB、达梦、Elasticsearch 8 | 独立服务端与驱动验证，不能复用兼容产品的通过结论 |
| 后续 | 人大金仓、虚谷、Informix | 主键、序列、类型、分页和过程调用 |
| 后续 | Hive、Impala | 先覆盖原生命令及结果处理，再按表模型验证写入与事务 |
| 后续 | SQL Server + jTDS | 作为不同驱动组合验证，不增加数据库类别 |
| 先评估 | OceanBase、GBase、Sybase、EDB、Phoenix、Presto、Kylin、PolarDB、ODPS | 核对 URL、方言选择、产品版本及兼容模式 |
| 先评估 | TiDB、Greenplum、StarRocks、Doris、SAP HANA | 核对识别入口和适配范围，首页图标不是测试依据 |

OceanBase 的 Oracle 兼容模式还需核对 `JdbcHelper` 的 URL 分支优先级。不同兼容模式分别验证；`MOCK` 不作为真实数据源。
