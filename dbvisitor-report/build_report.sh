#!/bin/bash
set -e

# ==========================================
# DBVisitor 代码统计生成脚本
# 功能: 统计 Main, Test, Demo 代码行数并计算比率
# ==========================================

# 1. 环境初始化
# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# 获取项目根目录 (假设脚本在 dbvisitor-report 下，根目录在上级)
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
REPORT_FILE="$SCRIPT_DIR/report.md"

# 进入项目根目录
cd "$PROJECT_ROOT"

echo "工作目录: $(pwd)"
echo "报告文件: $REPORT_FILE"
echo "------------------------------------------------"

# 2. 工具检测与自动安装
CLOC_CMD=""

if command -v cloc &> /dev/null; then
    CLOC_CMD="cloc"
    echo "✅ 检测到本地已安装 cloc"
elif command -v npx &> /dev/null; then
    CLOC_CMD="npx cloc"
    echo "✅ 检测到 Node.js 环境，将使用 npx cloc"
else
    echo "⚠️ 未检测到 cloc 或 npx，尝试下载 cloc Perl 脚本..."
    CLOC_SCRIPT="$SCRIPT_DIR/cloc.pl"
    if [ ! -f "$CLOC_SCRIPT" ]; then
        # 下载 1.98 版本 (比较稳定)
        curl -L https://github.com/AlDanial/cloc/releases/download/1.98/cloc-1.98.pl -o "$CLOC_SCRIPT"
        chmod +x "$CLOC_SCRIPT"
    fi
    CLOC_CMD="$CLOC_SCRIPT"
    echo "✅ 已下载并使用本地 cloc 脚本: $CLOC_CMD"
fi

# 3. 统计函数
# 参数: 使用 "$@" 接收所有 find 参数
count_java_lines() {
    # 使用 find 查找文件 -> xargs 传给 cloc -> 输出 CSV -> grep Java -> awk 求和第5列(code)
    # 注意: find 使用 -print0 和 xargs -0 以处理文件名可能包含空格的情况
    find . -type f -name "*.java" "$@" -print0 | xargs -0 $CLOC_CMD --csv --quiet 2>/dev/null | grep "Java" | awk -F, '{sum+=$5} END {print sum}'
}

count_java_files() {
    find . -type f -name "*.java" "$@" | wc -l | xargs
}

echo "正在统计正式代码..."
# 排除 dbvisitor-example, dbvisitor-report, target, .xxx 目录
# 核心逻辑: 路径包含 src/main/java 且不包含 dbvisitor-example
ARGS_MAIN=(-path "*/src/main/java/*" -not -path "./dbvisitor-example*" -not -path "./dbvisitor-report*")
MAIN_LINES=$(count_java_lines "${ARGS_MAIN[@]}")
# 如果为空（可能是 awk 没输出），设为 0
MAIN_LINES=${MAIN_LINES:-0}
MAIN_FILES=$(count_java_files "${ARGS_MAIN[@]}")

echo "正在统计测试与示例代码..."
# Test: 路径包含 src/test/java
ARGS_TEST=(-path "*/src/test/java/*")
TEST_LINES=$(count_java_lines "${ARGS_TEST[@]}")
TEST_LINES=${TEST_LINES:-0}
TEST_FILES=$(count_java_files "${ARGS_TEST[@]}")

# Demo: 在 dbvisitor-example 下的 src/main/java
ARGS_DEMO=(-path "./dbvisitor-example/*/src/main/java/*")
DEMO_LINES=$(count_java_lines "${ARGS_DEMO[@]}")
DEMO_LINES=${DEMO_LINES:-0}
DEMO_FILES=$(count_java_files "${ARGS_DEMO[@]}")

# 4. 计算与汇总
TOTAL_OTHER_LINES=$((TEST_LINES + DEMO_LINES))
TOTAL_OTHER_FILES=$((TEST_FILES + DEMO_FILES))

COVERAGE="0.00"
if [ "$MAIN_LINES" -gt 0 ]; then
    COVERAGE=$(awk "BEGIN {printf \"%.2f\", ($TOTAL_OTHER_LINES / $MAIN_LINES) * 100}")
fi

# 5. 单元测试覆盖率 (JaCoCo)
echo "------------------------------------------------"
echo "正在执行单元测试及其代码覆盖率分析 (可能需要几分钟)..."

# 使用 Maven Wrapper
MVN_CMD="./mvnw"
if [ ! -f "$MVN_CMD" ]; then
    MVN_CMD="mvn"
fi

JACOCO_COVERAGE="N/A (未执行或失败)"
TEST_COUNT="0"
TEST_PASSED="0"
TEST_FAILED="0"
TEST_SKIPPED="0"

# 定义日志和报告路径
TARGET_DIR="$SCRIPT_DIR/target"
LOG_FILE="$TARGET_DIR/mvn_test.log"
JACOCO_CSV="$TARGET_DIR/site/jacoco-aggregate/jacoco.csv"

# 1) 执行 clean (避免日志文件在 clean 过程中被删除的问题)
$MVN_CMD clean -pl dbvisitor-report -am -B > /dev/null 2>&1

# 2) 创建 target 目录并执行 test
mkdir -p "$TARGET_DIR"
echo "测试日志将输出到: $LOG_FILE"

# -pl dbvisitor-report -am: 只构建 dbvisitor-report 及其依赖
# test: 执行测试
# 使用 set +e 允许 mvn 执行失败（例如测试失败），以便后续仍能统计失败用例
# -fn (fail-never) 确保尽可能多的模块被执行
set +e
$MVN_CMD test -pl dbvisitor-report -am -B -fn -Dmaven.test.failure.ignore=true > "$LOG_FILE" 2>&1
MVN_EXIT_CODE=$?
set -e

if [ $MVN_EXIT_CODE -eq 0 ]; then
    echo "✅ 单元测试执行成功。"
else
    echo "⚠️ 单元测试执行包含错误或失败 (Exit Code: $MVN_EXIT_CODE)。将尝试解析部分结果。"
fi

# 无论是否成功，都尝试解析结果（因为使用了 -Dmaven.test.failure.ignore=true，部分结果可能可用）
# A. 解析 JaCoCo (无论测试成功与否，只要有 CSV)
if [ -f "$JACOCO_CSV" ]; then
    # 解析 CSV 获取 LINE 覆盖率
    STATS=$(awk -F, 'NR>1 {missed+=$8; covered+=$9} END {print missed, covered}' "$JACOCO_CSV")
    MISSED=$(echo $STATS | awk '{print $1}')
    COVERED=$(echo $STATS | awk '{print $2}')
    
    TOTAL_JACOCO=$((MISSED + COVERED))
    if [ "$TOTAL_JACOCO" -gt 0 ]; then
        VAL=$(awk "BEGIN {printf \"%.2f\", ($COVERED / $TOTAL_JACOCO) * 100}")
        JACOCO_COVERAGE="${VAL}%"
    else
        JACOCO_COVERAGE="0.00% (无数据)"
    fi
else
     echo "⚠️ 未找到 JaCoCo 报告文件: $JACOCO_CSV (覆盖率将显示 N/A)"
fi

# B. 解析 Maven 日志获取测试数量
if [ -f "$LOG_FILE" ]; then
    # 统计单元测试数量 (从日志中提取)
    # 提取针对每个测试类的统计 (包含 "- in " 的行)，避免重复计算模块汇总
    # 注意: surefire 3.x 通常使用 "-- in"
    REPORT_LINES=$(grep "Tests run: .* -- in " "$LOG_FILE" || true)
    
    if [ -n "$REPORT_LINES" ]; then
        # 提取数据: Tests run: R, Failures: F, Errors: E, Skipped: S
        STATS_SUM=$(echo "$REPORT_LINES" | sed -n 's/.*Tests run: \([0-9]*\), Failures: \([0-9]*\), Errors: \([0-9]*\), Skipped: \([0-9]*\).*/\1 \2 \3 \4/p' | awk '{r+=$1; f+=$2; e+=$3; s+=$4} END {print r, f, e, s}')
        
        TEST_COUNT=$(echo $STATS_SUM | awk '{print $1}')
        FAILURES=$(echo $STATS_SUM | awk '{print $2}')
        ERRORS=$(echo $STATS_SUM | awk '{print $3}')
        TEST_SKIPPED=$(echo $STATS_SUM | awk '{print $4}')
        
        # 默认值处理
        TEST_COUNT=${TEST_COUNT:-0}
        FAILURES=${FAILURES:-0}
        ERRORS=${ERRORS:-0}
        TEST_SKIPPED=${TEST_SKIPPED:-0}
        
        TEST_FAILED=$((FAILURES + ERRORS))
        TEST_PASSED=$((TEST_COUNT - TEST_FAILED - TEST_SKIPPED))
    fi
    echo "📊 单元测试统计: 总数=$TEST_COUNT, 成功=$TEST_PASSED, 失败=$TEST_FAILED, 跳过=$TEST_SKIPPED"
else
    echo "⚠️ 未找到 Maven 日志文件: $LOG_FILE (无法统计测试数量)"
fi

DATE=$(date "+%Y年%m月%d日")

# 6. 生成报告
TEMPLATE_FILE="$SCRIPT_DIR/report.template.md"

if [ ! -f "$TEMPLATE_FILE" ]; then
    echo "❌ 错误: 未找到模版文件 $TEMPLATE_FILE"
    exit 1
fi

echo "正在根据模版生成报告..."
# 使用 sed 进行替换，使用 # 作为分隔符以避免路径中的 / 冲突
sed \
    -e "s#{{DATE}}#$DATE#g" \
    -e "s#{{GENERATOR}}#自动脚本 (\`$0\`)#g" \
    -e "s#{{MAIN_FILES}}#$MAIN_FILES#g" \
    -e "s#{{MAIN_LINES}}#$MAIN_LINES#g" \
    -e "s#{{TOTAL_OTHER_FILES}}#$TOTAL_OTHER_FILES#g" \
    -e "s#{{TOTAL_OTHER_LINES}}#$TOTAL_OTHER_LINES#g" \
    -e "s#{{TEST_FILES}}#$TEST_FILES#g" \
    -e "s#{{DEMO_FILES}}#$DEMO_FILES#g" \
    -e "s#{{TEST_LINES}}#$TEST_LINES#g" \
    -e "s#{{DEMO_LINES}}#$DEMO_LINES#g" \
    -e "s#{{COVERAGE}}#$COVERAGE#g" \
    -e "s#{{JACOCO_COVERAGE}}#$JACOCO_COVERAGE#g" \
    -e "s#{{TEST_COUNT}}#$TEST_COUNT#g" \
    -e "s#{{TEST_PASSED}}#$TEST_PASSED#g" \
    -e "s#{{TEST_FAILED}}#$TEST_FAILED#g" \
    -e "s#{{TEST_SKIPPED}}#$TEST_SKIPPED#g" \
    "$TEMPLATE_FILE" > "$REPORT_FILE"

echo "------------------------------------------------"
echo "✅ 报告生成成功: $REPORT_FILE"
echo "代码行数比率: ${COVERAGE}%"
