# DBVisitor 项目代码行数统计报告
日期: {{DATE}}
统计方式: {{GENERATOR}}

## 1. 正式代码 (Main)
范围: dbvisitor 所有模块的 `src/main/java` (排除 dbvisitor-example)
- Java 文件数: **{{MAIN_FILES}}**
- Java 代码行数: <font color="red">**{{MAIN_LINES}}**</font>

## 2. 测试与 Demo 工程
范围: 所有 `src/test/java` 以及 `dbvisitor-example` 下的所有 Java 代码
- Java 文件数: **{{TOTAL_OTHER_FILES}}** (测试: {{TEST_FILES}}, Demo Main: {{DEMO_FILES}})
- Java 代码行数: <font color="red">**{{TOTAL_OTHER_LINES}}**</font> (测试: {{TEST_LINES}}, Demo Main: {{DEMO_LINES}})

## 3. 代码覆盖率统计
> 注意：此处的覆盖率指“测试与示例代码行数”与“正式代码行数”的比率（静态指标）。

- 正式代码行数: {{MAIN_LINES}}
- 测试/Demo代码行数: {{TOTAL_OTHER_LINES}}
- **代码行数比率 (Test/Code Ratio): <font color="red">{{COVERAGE}}%</font>**

## 4. 单元测试覆盖率 (JaCoCo)
> 数据来源: Maven JaCoCo Aggregate Report (Line Coverage)

- **Line Coverage**: <font color="red">**{{JACOCO_COVERAGE}}**</font>
- **Test Cases**: **{{TEST_COUNT}}** (Passed: <font color="green">**{{TEST_PASSED}}**</font>, Failed: <font color="red">**{{TEST_FAILED}}**</font>, Skipped: {{TEST_SKIPPED}})

(详细报告位置: `dbvisitor/dbvisitor-report/target/site/jacoco-aggregate/index.html`)

## 5. 如何生成此报告

### 运行环境
- **操作系统**: macOS / Linux
- **Java**: JDK 8+ (必需)
- **Docker**: (必需, 用于运行测试数据库)
- **Maven**: 3.x (脚本自动检测 mvnw)
- **工具**: 
    - `cloc`: 代码统计工具 (脚本会自动检测本地安装，或使用 npx，或自动下载 perl 脚本)
    - Shell 基础工具: `find`, `awk`, `grep`, `sed`

### 准备工作
1. 确保已安装 Java 和 Docker 环境。
2. 启动测试数据库:
   - 进入目录: `dbvisitor/dbvisitor-test`
   - 根据当前机器架构 (x86/arm64) 选择对应目录
   - 运行: `docker-compose up -d`
3. 确保网络正常（用于下载 Maven 依赖）。

### 生成步骤
1. 在项目根目录下打开终端。
2. 运行生成脚本:
   ```bash
   ./dbvisitor/dbvisitor-report/build_report.sh
   ```
   *(注意: 脚本会执行完整的单元测试，首次运行可能需要较长时间)*
3. 脚本执行完成后，查看 `dbvisitor/dbvisitor-report/report.md`。
