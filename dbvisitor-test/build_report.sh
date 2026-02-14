#!/bin/bash
# ============================================================
# DBVisitor 测试与覆盖率报告生成脚本
#
# 功能:
#   1. 执行所有模块的单元测试 + 集成测试
#   2. 聚合 JaCoCo 覆盖率数据 (合并所有模块的 jacoco.exec)
#   3. 统计代码行数 (框架/测试/示例)
#   4. 生成 report.md 汇总报告
#
# 用法:
#   cd dbvisitor/dbvisitor-test && bash build_report.sh
#
# 前置条件:
#   - JDK 8+
#   - Docker (测试数据库已启动)
#   - Maven 3.x (自动检测 mvnw)
#   - cloc (可选, 用于精确代码行数; 无则使用 grep 近似统计)
# ============================================================
set -euo pipefail

# ==================== 初始化 ====================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
REPORT_FILE="$SCRIPT_DIR/report.md"
JACOCO_VERSION="0.8.11"

cd "$PROJECT_ROOT"

# 模块定义 — 格式: "相对路径:显示名称"
MODULES=(
    "dbvisitor:dbvisitor (核心框架)"
    "dbvisitor-driver:dbvisitor-driver (驱动层)"
    "dbvisitor-integration/dbvisitor-spring:dbvisitor-spring"
    "dbvisitor-integration/dbvisitor-spring-starter:spring-boot-starter"
    "dbvisitor-integration/dbvisitor-hasor:dbvisitor-hasor"
    "dbvisitor-integration/dbvisitor-guice:dbvisitor-guice"
    "dbvisitor-integration/dbvisitor-solon-plugin:solon-plugin"
    "dbvisitor-adapter/jdbc-redis:jdbc-redis"
    "dbvisitor-adapter/jdbc-mongo:jdbc-mongo"
    "dbvisitor-adapter/jdbc-milvus:jdbc-milvus"
    "dbvisitor-adapter/jdbc-elastic:jdbc-elastic"
    "dbvisitor-test:dbvisitor-test (集成测试)"
)

# ==================== 工具检测 ====================
echo "▶ 检测工具..."

MVN_CMD="./mvnw"
[ -f "$MVN_CMD" ] || MVN_CMD="mvn"
echo "  Maven: $MVN_CMD"

JAVA_CMD="java"
if [ -n "${JAVA_HOME:-}" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
fi
echo "  Java: $($JAVA_CMD -version 2>&1 | head -1)"

CLOC_CMD=""
if command -v cloc &>/dev/null; then
    CLOC_CMD="cloc"
elif command -v npx &>/dev/null; then
    CLOC_CMD="npx cloc"
fi
echo "  cloc: ${CLOC_CMD:-未安装 (将使用 grep 近似统计)}"
echo ""

# ==================== Step 1: 全量测试 ====================
echo "▶ 执行全量测试 (可能需要数分钟)..."
TEMP_LOG="/tmp/dbvisitor-test-$$.log"

set +e
$MVN_CMD clean test -fn -Dmaven.test.failure.ignore=true -B > "$TEMP_LOG" 2>&1
MVN_EXIT=$?
set -e

WORK_DIR="$SCRIPT_DIR/target/report"
mkdir -p "$WORK_DIR"
# 使用 cat + rm 替代 mv，避免在外部磁盘(如 FAT/ExFAT)上移动文件时出现"权限/属主"错误
cat "$TEMP_LOG" > "$WORK_DIR/mvn.log"
rm -f "$TEMP_LOG"

if [ $MVN_EXIT -eq 0 ]; then
    echo "  ✅ 全量测试完成"
else
    echo "  ⚠️  测试包含错误 (Exit: $MVN_EXIT), 继续生成报告..."
fi

# ==================== Step 2: 代码行数统计 ====================
echo "▶ 统计代码行数..."

count_java_lines() {
    local args=("$@")
    if [ -n "$CLOC_CMD" ]; then
        find . -type f -name "*.java" "${args[@]}" -print0 2>/dev/null | \
            xargs -0 $CLOC_CMD --csv --quiet 2>/dev/null | \
            { grep "Java" || true; } | awk -F, '{sum+=$5} END {print sum+0}'
    else
        # 回退: 统计非空行 (包含注释, 不如 cloc 精确)
        find . -type f -name "*.java" "${args[@]}" -print0 2>/dev/null | \
            xargs -0 grep -c '[^[:space:]]' 2>/dev/null | \
            awk -F: '{sum+=$NF} END {print sum+0}'
    fi
}

count_java_files() {
    find . -type f -name "*.java" "$@" 2>/dev/null | wc -l | tr -d ' '
}

MAIN_ARGS=(-path "*/src/main/java/*" -not -path "./dbvisitor-example*")
MAIN_LINES=$(count_java_lines "${MAIN_ARGS[@]}")
MAIN_FILES=$(count_java_files "${MAIN_ARGS[@]}")
MAIN_LINES=${MAIN_LINES:-0}

TEST_ARGS=(-path "*/src/test/java/*")
TEST_LINES=$(count_java_lines "${TEST_ARGS[@]}")
TEST_FILES=$(count_java_files "${TEST_ARGS[@]}")
TEST_LINES=${TEST_LINES:-0}

DEMO_ARGS=(-path "./dbvisitor-example/*/src/main/java/*")
DEMO_LINES=$(count_java_lines "${DEMO_ARGS[@]}")
DEMO_FILES=$(count_java_files "${DEMO_ARGS[@]}")
DEMO_LINES=${DEMO_LINES:-0}

TOTAL_VERIFY=$((TEST_LINES + DEMO_LINES))
TEST_RATIO="0.00"
if [ "$MAIN_LINES" -gt 0 ]; then
    TEST_RATIO=$(awk "BEGIN {printf \"%.2f\", ($TOTAL_VERIFY / $MAIN_LINES) * 100}")
fi

echo "  框架代码: ${MAIN_FILES} 文件, ${MAIN_LINES} 行"
echo "  测试代码: ${TEST_FILES} 文件, ${TEST_LINES} 行"
echo "  示例代码: ${DEMO_FILES} 文件, ${DEMO_LINES} 行"
echo "  测试/代码比: ${TEST_RATIO}%"

# ==================== Step 3: 解析 Surefire 报告 ====================
echo "▶ 解析测试结果..."

# 解析单个模块的 surefire XML 报告, 输出: 总数 通过 失败 跳过
parse_surefire() {
    local report_dir="$1"
    local t=0 e=0 f=0 s=0

    if [ -d "$report_dir" ]; then
        while IFS= read -r xml; do
            [ -f "$xml" ] || continue
            local ts es fs ss
            ts=$(grep -o 'tests="[0-9]*"' "$xml" 2>/dev/null | head -1 | grep -o '[0-9]*') || true
            es=$(grep -o 'errors="[0-9]*"' "$xml" 2>/dev/null | head -1 | grep -o '[0-9]*') || true
            fs=$(grep -o 'failures="[0-9]*"' "$xml" 2>/dev/null | head -1 | grep -o '[0-9]*') || true
            ss=$(grep -o 'skipped="[0-9]*"' "$xml" 2>/dev/null | head -1 | grep -o '[0-9]*') || true
            t=$((t + ${ts:-0}))
            e=$((e + ${es:-0}))
            f=$((f + ${fs:-0}))
            s=$((s + ${ss:-0}))
        done < <(find "$report_dir" -name "TEST-*.xml" 2>/dev/null)
    fi

    local failed=$((e + f))
    local passed=$((t - failed - s))
    [ $passed -lt 0 ] && passed=0
    echo "$t $passed $failed $s"
}

GTT=0 GTP=0 GTF=0 GTS=0
MODULE_TABLE=""

# 用于存储每个模块的测试结果 (后续组件报告使用)
declare -a MOD_NAMES=()
declare -a MOD_PATHS=()
declare -a MOD_TESTS=()
declare -a MOD_PASS=()
declare -a MOD_FAIL=()
declare -a MOD_SKIP=()

mod_idx=0
for entry in "${MODULES[@]}"; do
    mod_path="${entry%%:*}"
    mod_name="${entry##*:}"

    read -r mt mp mf ms <<< "$(parse_surefire "$mod_path/target/surefire-reports")"

    MOD_NAMES[$mod_idx]="$mod_name"
    MOD_PATHS[$mod_idx]="$mod_path"
    MOD_TESTS[$mod_idx]=$mt
    MOD_PASS[$mod_idx]=$mp
    MOD_FAIL[$mod_idx]=$mf
    MOD_SKIP[$mod_idx]=$ms

    if [ "$mt" -gt 0 ]; then
        MODULE_TABLE="${MODULE_TABLE}| ${mod_name} | ${mt} | ${mp} | ${mf} | ${ms} |
"
    fi

    GTT=$((GTT + mt))
    GTP=$((GTP + mp))
    GTF=$((GTF + mf))
    GTS=$((GTS + ms))
    mod_idx=$((mod_idx + 1))
done

echo "  总测试: ${GTT}, 通过: ${GTP}, 失败: ${GTF}, 跳过: ${GTS}"

# ==================== Step 4: JaCoCo 覆盖率聚合 ====================
echo "▶ 聚合代码覆盖率..."

# 定位 JaCoCo CLI
JACOCO_CLI=""
M2_JAR="$HOME/.m2/repository/org/jacoco/org.jacoco.cli/$JACOCO_VERSION/org.jacoco.cli-$JACOCO_VERSION-nodeps.jar"
LOCAL_JAR="$WORK_DIR/jacococli.jar"

if [ -f "$M2_JAR" ]; then
    JACOCO_CLI="$M2_JAR"
elif [ -f "$LOCAL_JAR" ]; then
    JACOCO_CLI="$LOCAL_JAR"
else
    echo "  下载 JaCoCo CLI v${JACOCO_VERSION}..."
    curl -sL "https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/$JACOCO_VERSION/org.jacoco.cli-$JACOCO_VERSION-nodeps.jar" \
        -o "$LOCAL_JAR"
    JACOCO_CLI="$LOCAL_JAR"
fi

# 收集所有 jacoco.exec
EXEC_FILES=$(find . -name "jacoco.exec" -path "*/target/*" \
    -not -path "*/dbvisitor-example/*" \
    -not -path "*/dbvisitor-doc/*" \
    2>/dev/null || true)

LINE_COVERAGE="N/A"
BRANCH_COVERAGE="N/A"
LINE_COVERED=0
LINE_MISSED=0
LINE_TOTAL=0
BRANCH_COVERED=0
BRANCH_MISSED=0
BRANCH_TOTAL=0

if [ -n "$EXEC_FILES" ]; then
    # 合并所有 exec 文件
    echo "$EXEC_FILES" | xargs $JAVA_CMD -jar "$JACOCO_CLI" merge \
        --destfile "$WORK_DIR/merged.exec" 2>/dev/null

    # -------------------------------------------------------------------------
    # 过滤 Generated 代码 (ANTLR) - 避免污染覆盖率统计
    # 对应 pom.xml 中的 exclusion 配置: Milvus, Redis, Elastic, Mongo
    echo "  过滤 Generated 代码..."
    find . -path "*/target/classes/*" \( \
          -name "MilvusLexer*.class"  -o -name "MilvusParser*.class"  -o -name "MilvusBase*.class"   -o -name "MilvusListener*.class"   -o -name "MilvusVisitor*.class" \
       -o -name "RedisLexer*.class"   -o -name "RedisParser*.class"   -o -name "RedisBase*.class"    -o -name "RedisListener*.class"    -o -name "RedisVisitor*.class" \
       -o -name "ElasticLexer*.class" -o -name "ElasticParser*.class" -o -name "ElasticBase*.class"  -o -name "ElasticListener*.class"  -o -name "ElasticVisitor*.class" \
       -o -name "MongoLexer*.class"   -o -name "MongoParser*.class"   -o -name "MongoBase*.class"    -o -name "MongoListener*.class"    -o -name "MongoVisitor*.class" \
    \) -delete
    # -------------------------------------------------------------------------

    # 收集所有框架代码的 classes + sourcefiles 目录
    CLASS_ARGS=""
    SOURCE_ARGS=""
    for entry in "${MODULES[@]}"; do
        mod_path="${entry%%:*}"
        if [ -d "$mod_path/target/classes" ]; then
            CLASS_ARGS="$CLASS_ARGS --classfiles $mod_path/target/classes"
        fi
        if [ -d "$mod_path/src/main/java" ]; then
            SOURCE_ARGS="$SOURCE_ARGS --sourcefiles $mod_path/src/main/java"
        fi
    done

    if [ -n "$CLASS_ARGS" ]; then
        # 生成 CSV + HTML 聚合报告
        $JAVA_CMD -jar "$JACOCO_CLI" report "$WORK_DIR/merged.exec" \
            $CLASS_ARGS $SOURCE_ARGS \
            --csv "$WORK_DIR/jacoco.csv" \
            --html "$WORK_DIR/jacoco-aggregate" \
            2>/dev/null || true

        if [ -f "$WORK_DIR/jacoco.csv" ]; then
            STATS=$(awk -F, 'NR>1 {lm+=$8; lc+=$9; bm+=$6; bc+=$7} END {printf "%d %d %d %d", lm, lc, bm, bc}' "$WORK_DIR/jacoco.csv")
            LINE_MISSED=$(echo "$STATS" | awk '{print $1}')
            LINE_COVERED=$(echo "$STATS" | awk '{print $2}')
            BRANCH_MISSED=$(echo "$STATS" | awk '{print $3}')
            BRANCH_COVERED=$(echo "$STATS" | awk '{print $4}')

            LINE_TOTAL=$((LINE_MISSED + LINE_COVERED))
            BRANCH_TOTAL=$((BRANCH_MISSED + BRANCH_COVERED))

            if [ "$LINE_TOTAL" -gt 0 ]; then
                LINE_COVERAGE=$(awk "BEGIN {printf \"%.2f\", ($LINE_COVERED / $LINE_TOTAL) * 100}")
            fi
            if [ "$BRANCH_TOTAL" -gt 0 ]; then
                BRANCH_COVERAGE=$(awk "BEGIN {printf \"%.2f\", ($BRANCH_COVERED / $BRANCH_TOTAL) * 100}")
            fi
        fi
    fi
else
    echo "  ⚠️  未找到 jacoco.exec 文件 (覆盖率将显示 N/A)"
fi

echo "  行覆盖率: ${LINE_COVERAGE}%"
echo "  分支覆盖率: ${BRANCH_COVERAGE}%"

# ==================== Step 4b: 组件级别覆盖率 ====================
echo "▶ 计算各组件覆盖率..."

# 用于存储每个模块的代码行数和覆盖率
declare -a MOD_MAIN_FILES=()
declare -a MOD_MAIN_LINES=()
declare -a MOD_TEST_FILES=()
declare -a MOD_TEST_LINES=()
declare -a MOD_LINE_COV=()
declare -a MOD_BRANCH_COV=()
declare -a MOD_LINE_COVERED_ARR=()
declare -a MOD_LINE_TOTAL_ARR=()
declare -a MOD_BRANCH_COVERED_ARR=()
declare -a MOD_BRANCH_TOTAL_ARR=()

# 计算指定目录下 Java 文件的行数
count_lines_in_dir() {
    local dir="$1"
    if [ ! -d "$dir" ]; then
        echo "0"
        return
    fi
    local result=""
    if [ -n "$CLOC_CMD" ]; then
        result=$(find "$dir" -type f -name "*.java" -print0 2>/dev/null | \
            xargs -0 $CLOC_CMD --csv --quiet 2>/dev/null | \
            { grep "Java" || true; } | awk -F, '{sum+=$5} END {print sum+0}')
    else
        result=$(find "$dir" -type f -name "*.java" -print0 2>/dev/null | \
            xargs -0 grep -c '[^[:space:]]' 2>/dev/null | \
            awk -F: '{sum+=$NF} END {print sum+0}' || echo "0")
    fi
    echo "${result:-0}"
}

count_files_in_dir() {
    local dir="$1"
    if [ ! -d "$dir" ]; then
        echo "0"
        return
    fi
    find "$dir" -type f -name "*.java" 2>/dev/null | wc -l | tr -d ' '
}

for i in $(seq 0 $((mod_idx - 1))); do
    mp="${MOD_PATHS[$i]}"

    # 统计代码行数
    MOD_MAIN_FILES[$i]=$(count_files_in_dir "$mp/src/main/java")
    MOD_MAIN_LINES[$i]=$(count_lines_in_dir "$mp/src/main/java")
    MOD_MAIN_LINES[$i]=${MOD_MAIN_LINES[$i]:-0}
    MOD_TEST_FILES[$i]=$(count_files_in_dir "$mp/src/test/java")
    MOD_TEST_LINES[$i]=$(count_lines_in_dir "$mp/src/test/java")
    MOD_TEST_LINES[$i]=${MOD_TEST_LINES[$i]:-0}

    # 计算覆盖率 (仅对有源码的模块)
    MOD_LINE_COV[$i]="—"
    MOD_BRANCH_COV[$i]="—"
    MOD_LINE_COVERED_ARR[$i]=0
    MOD_LINE_TOTAL_ARR[$i]=0
    MOD_BRANCH_COVERED_ARR[$i]=0
    MOD_BRANCH_TOTAL_ARR[$i]=0

    if [ -d "$mp/target/classes" ] && [ -d "$mp/src/main/java" ] && [ -f "$WORK_DIR/merged.exec" ]; then
        # 为当前模块生成单独的 CSV
        mod_csv="$WORK_DIR/jacoco_${i}.csv"
        $JAVA_CMD -jar "$JACOCO_CLI" report "$WORK_DIR/merged.exec" \
            --classfiles "$mp/target/classes" \
            --sourcefiles "$mp/src/main/java" \
            --csv "$mod_csv" \
            2>/dev/null || true

        if [ -f "$mod_csv" ]; then
            mod_stats=$(awk -F, 'NR>1 {lm+=$8; lc+=$9; bm+=$6; bc+=$7} END {printf "%d %d %d %d", lm, lc, bm, bc}' "$mod_csv")
            m_lm=$(echo "$mod_stats" | awk '{print $1}')
            m_lc=$(echo "$mod_stats" | awk '{print $2}')
            m_bm=$(echo "$mod_stats" | awk '{print $3}')
            m_bc=$(echo "$mod_stats" | awk '{print $4}')

            m_lt=$((m_lm + m_lc))
            m_bt=$((m_bm + m_bc))

            MOD_LINE_COVERED_ARR[$i]=$m_lc
            MOD_LINE_TOTAL_ARR[$i]=$m_lt
            MOD_BRANCH_COVERED_ARR[$i]=$m_bc
            MOD_BRANCH_TOTAL_ARR[$i]=$m_bt

            if [ "$m_lt" -gt 0 ]; then
                MOD_LINE_COV[$i]=$(awk "BEGIN {printf \"%.1f%%\", ($m_lc / $m_lt) * 100}")
            fi
            if [ "$m_bt" -gt 0 ]; then
                MOD_BRANCH_COV[$i]=$(awk "BEGIN {printf \"%.1f%%\", ($m_bc / $m_bt) * 100}")
            fi

            rm -f "$mod_csv"
        fi
    fi
done

echo "  ✅ 组件级别统计完成"

# ==================== Step 5: 生成报告 ====================
echo "▶ 生成报告..."

DATE=$(date "+%Y年%m月%d日")

cat > "$REPORT_FILE" <<EOF
# DBVisitor 测试与覆盖率报告

生成时间: ${DATE}

## 1. 代码规模统计

| 类别 | Java 文件数 | 代码行数 |
|------|:----------:|:-------:|
| 框架代码 (src/main/java) | ${MAIN_FILES} | ${MAIN_LINES} |
| 测试代码 (src/test/java) | ${TEST_FILES} | ${TEST_LINES} |
| 示例代码 (dbvisitor-example) | ${DEMO_FILES} | ${DEMO_LINES} |

- **测试与示例代码 / 框架代码比**: **${TEST_RATIO}%**

## 2. 组件质量概览

EOF

# ---------- 动态生成组件质量表格 ----------
{
    echo "| 组件 | 框架代码 | 测试代码 | 用例总数 | 通过 | 失败 | 行覆盖率 | 分支覆盖率 |"
    echo "|------|:-------:|:-------:|:-------:|:----:|:----:|:-------:|:--------:|"

    for i in $(seq 0 $((mod_idx - 1))); do
        mn="${MOD_NAMES[$i]}"
        m_main="${MOD_MAIN_LINES[$i]}"
        m_test="${MOD_TEST_LINES[$i]}"
        m_tt="${MOD_TESTS[$i]}"
        m_tp="${MOD_PASS[$i]}"
        m_tf="${MOD_FAIL[$i]}"
        m_lc="${MOD_LINE_COV[$i]}"
        m_bc="${MOD_BRANCH_COV[$i]}"

        # 跳过无代码且无测试的模块
        if [ "$m_main" -eq 0 ] && [ "$m_test" -eq 0 ] && [ "$m_tt" -eq 0 ]; then
            continue
        fi

        # 为有失败的模块标记
        if [ "$m_tf" -gt 0 ]; then
            fail_mark="**${m_tf}** ⚠️"
        else
            fail_mark="${m_tf}"
        fi

        echo "| ${mn} | ${m_main} | ${m_test} | ${m_tt} | ${m_tp} | ${fail_mark} | ${m_lc} | ${m_bc} |"
    done

    # 合计行
    echo "| **合计** | **${MAIN_LINES}** | **${TEST_LINES}** | **${GTT}** | **${GTP}** | **${GTF}** | **${LINE_COVERAGE}%** | **${BRANCH_COVERAGE}%** |"
} >> "$REPORT_FILE"

cat >> "$REPORT_FILE" <<EOF

> 覆盖率数据来源: 所有模块 jacoco.exec 合并后按组件分析。无框架代码的模块覆盖率显示"—"。

## 3. 单元测试 & 集成测试结果

| 模块 | 总数 | 通过 | 失败 | 跳过 |
|------|:----:|:----:|:----:|:----:|
${MODULE_TABLE}| **合计** | **${GTT}** | **${GTP}** | **${GTF}** | **${GTS}** |

## 4. 总体覆盖率 (JaCoCo)

> 数据来源: 所有模块 jacoco.exec 合并后的聚合覆盖率

| 指标 | 值 |
|------|:---|
| 行覆盖率 (Line Coverage) | **${LINE_COVERAGE}%** |
| 分支覆盖率 (Branch Coverage) | **${BRANCH_COVERAGE}%** |
| 覆盖行数 / 总行数 | ${LINE_COVERED} / ${LINE_TOTAL} |
| 覆盖分支 / 总分支 | ${BRANCH_COVERED} / ${BRANCH_TOTAL} |

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
EOF

echo ""
echo "================================================"
echo "✅ 报告已生成: $REPORT_FILE"
echo "   行覆盖率: ${LINE_COVERAGE}%  |  测试: ${GTT} (通过 ${GTP}, 失败 ${GTF})"
echo "   HTML 报告: $WORK_DIR/jacoco-aggregate/index.html"
echo "================================================"
