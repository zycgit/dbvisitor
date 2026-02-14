# DBVisitor 测试与覆盖率报告

生成时间: 2026年02月14日

## 1. 代码规模统计

| 类别 | Java 文件数 | 代码行数 |
|------|:----------:|:-------:|
| 框架代码 (src/main/java) | 567 | 47052 |
| 测试代码 (src/test/java) | 852 | 98486 |
| 示例代码 (dbvisitor-example) | 88 | 2032 |

- **测试与示例代码 / 框架代码比**: **213.63%**

## 2. 组件质量概览

| 组件 | 框架代码 | 测试代码 | 用例总数 | 通过 | 失败 | 行覆盖率 | 分支覆盖率 |
|------|:-------:|:-------:|:-------:|:----:|:----:|:-------:|:--------:|
| dbvisitor (核心框架) | 24070 | 39951 | 2069 | 2069 | 0 | 73.6% | 59.7% |
| dbvisitor-driver (驱动层) | 7446 | 7213 | 997 | 997 | 0 | 85.4% | 70.1% |
| dbvisitor-spring | 1032 | 195 | 2 | 2 | 0 | 33.6% | 23.9% |
| spring-boot-starter | 271 | 0 | 0 | 0 | 0 | 0.0% | 0.0% |
| dbvisitor-hasor | 656 | 258 | 3 | 3 | 0 | 55.5% | 35.2% |
| dbvisitor-guice | 668 | 246 | 3 | 3 | 0 | 72.8% | 52.6% |
| solon-plugin | 412 | 235 | 0 | 0 | 0 | 0.0% | 0.0% |
| jdbc-redis | 4082 | 6462 | 245 | 245 | 0 | 86.7% | 74.3% |
| jdbc-mongo | 3000 | 3308 | 85 | 85 | 0 | 73.8% | 53.0% |
| jdbc-milvus | 3124 | 3209 | 94 | 94 | 0 | 79.6% | 61.0% |
| jdbc-elastic | 2291 | 3774 | 119 | 119 | 0 | 74.9% | 60.4% |
| dbvisitor-test (集成测试) | 0 | 33618 | 0 | 0 | 0 | — | — |
| **合计** | **47052** | **98486** | **3617** | **3617** | **0** | **75.12%** | **60.68%** |

> 覆盖率数据来源: 所有模块 jacoco.exec 合并后按组件分析。无框架代码的模块覆盖率显示"—"。

## 3. 单元测试 & 集成测试结果

| 模块 | 总数 | 通过 | 失败 | 跳过 |
|------|:----:|:----:|:----:|:----:|
| dbvisitor (核心框架) | 2069 | 2069 | 0 | 0 |
| dbvisitor-driver (驱动层) | 997 | 997 | 0 | 0 |
| dbvisitor-spring | 2 | 2 | 0 | 0 |
| dbvisitor-hasor | 3 | 3 | 0 | 0 |
| dbvisitor-guice | 3 | 3 | 0 | 0 |
| jdbc-redis | 245 | 245 | 0 | 0 |
| jdbc-mongo | 85 | 85 | 0 | 0 |
| jdbc-milvus | 94 | 94 | 0 | 0 |
| jdbc-elastic | 119 | 119 | 0 | 0 |
| **合计** | **3617** | **3617** | **0** | **0** |

## 4. 总体覆盖率 (JaCoCo)

> 数据来源: 所有模块 jacoco.exec 合并后的聚合覆盖率

| 指标 | 值 |
|------|:---|
| 行覆盖率 (Line Coverage) | **75.12%** |
| 分支覆盖率 (Branch Coverage) | **60.68%** |
| 覆盖行数 / 总行数 | 18796 / 25020 |
| 覆盖分支 / 总分支 | 7325 / 12071 |

> HTML 详细报告: dbvisitor-test/target/report/jacoco-aggregate/index.html

## 5. 如何生成此报告

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
