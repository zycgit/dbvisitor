# DBVisitor 测试与覆盖率报告

生成时间: 2026年02月12日

## 1. 代码规模统计

| 类别 | Java 文件数 | 代码行数 |
|------|:----------:|:-------:|
| 框架代码 (src/main/java) | 567 | 46990 |
| 测试代码 (src/test/java) | 834 | 91433 |
| 示例代码 (dbvisitor-example) | 88 | 2032 |

- **测试与示例代码 / 框架代码比**: **198.90%**

## 2. 单元测试 & 集成测试结果

| 模块 | 总数 | 通过 | 失败 | 跳过 |
|------|:----:|:----:|:----:|:----:|
| dbvisitor (核心框架) | 2157 | 2149 | 8 | 0 |
| dbvisitor-driver (驱动层) | 7 | 7 | 0 | 0 |
| dbvisitor-spring | 2 | 2 | 0 | 0 |
| dbvisitor-hasor | 3 | 0 | 3 | 0 |
| dbvisitor-guice | 3 | 3 | 0 | 0 |
| jdbc-redis | 245 | 245 | 0 | 0 |
| jdbc-mongo | 85 | 84 | 0 | 1 |
| jdbc-milvus | 94 | 94 | 0 | 0 |
| jdbc-elastic | 119 | 114 | 5 | 0 |
| dbvisitor-test (集成测试) | 1438 | 1438 | 0 | 0 |
| **合计** | **4153** | **4136** | **16** | **1** |

## 3. 框架代码覆盖率 (JaCoCo)

> 数据来源: 所有模块 jacoco.exec 合并后的聚合覆盖率

| 指标 | 值 |
|------|:---|
| 行覆盖率 (Line Coverage) | **65.22%** |
| 分支覆盖率 (Branch Coverage) | **51.73%** |
| 覆盖行数 / 总行数 | 27668 / 42422 |
| 覆盖分支 / 总分支 | 8475 / 16382 |

> HTML 详细报告: dbvisitor-test/target/report/jacoco-aggregate/index.html

## 4. 如何生成此报告

### 前置条件
- **JDK**: 8+
- **Docker**: 用于运行测试数据库
- **Maven**: 3.x (脚本自动检测 mvnw)
- **cloc**: 可选, 用于精确代码行数统计

### 运行步骤

1. 启动测试数据库 (根据架构选择 x86 或 arm64):

   cd dbvisitor/dbvisitor-test/docker/{arch}
   docker-compose up -d

2. 执行报告生成脚本:

   cd dbvisitor/dbvisitor-test
   bash build_report.sh

3. 查看报告:

   cat report.md
   open target/report/jacoco-aggregate/index.html
