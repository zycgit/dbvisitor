#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

usage() {
    cat <<'EOF'
Usage:
  ./dbvisitor-test/runnxn.sh all [--jobs 16] [gradle options...]
  ./dbvisitor-test/runnxn.sh <datasource> [gradle options...]
  ./dbvisitor-test/runnxn.sh <datasource|all> --update-docs [--jobs 16] [gradle options...]
  ./dbvisitor-test/runnxn.sh --help

Run the dbvisitor-test suite against all or one real datasource.
One test JVM, with 16 worker threads by default. Unmarked classes stay exclusive per datasource.
Only @NxnConcurrent classes can overlap; --class-jobs limits them per datasource (default: 16).
All datasources and classes share the same global worker budget; the limits do not multiply.
Live per-datasource progress is printed every 5 seconds; detailed output stays in execution.log.
Use --jobs 1 for fully serial execution, or --class-jobs 1 to keep each datasource serial.
Use --output <directory> for separate comparison reports (relative to dbvisitor-test).
Failures do not stop the other datasources.
This script uses real databases; framework-only tests run separately without nxn.env.
Use --update-docs to update compatibility JSON after each successful full suite.
See dbvisitor-test/NXN_DS_GUIDE.md for test bindings and documentation updates.
With no arguments, this help is printed and no tests are run.

Datasources:
  h2 mysql pg mssql oracle db2 clickhouse redis mongo es6 es7 milvus

Aliases:
  elastic6 -> es6
  elastic7 -> es7

Examples:
  ./dbvisitor-test/runnxn.sh all
  ./dbvisitor-test/runnxn.sh all --jobs 4
  ./dbvisitor-test/runnxn.sh pg
  ./dbvisitor-test/runnxn.sh mysql --rerun-tasks
  ./dbvisitor-test/runnxn.sh milvus
  ./dbvisitor-test/runnxn.sh all --update-docs
EOF
}

if [[ "$#" -eq 0 ]]; then
    usage
    exit 0
fi

target="$1"
shift

case "$target" in
    help|-h|--help)
        usage
        exit 0
        ;;
    elastic6)
        target="es6"
        ;;
    elastic7)
        target="es7"
        ;;
esac

datasources=(h2 mysql pg mssql oracle db2 clickhouse redis mongo es6 es7 milvus)
jobs=16
update_docs=false
dry_run=false
gradle_options=()
test_filters=()
class_options=()
output_options=()
while [[ "$#" -gt 0 ]]; do
    case "$1" in
        --help|-h)
            usage
            exit 0
            ;;
        --dry-run|-m|--version|-v)
            dry_run=true
            gradle_options+=("$1")
            shift
            ;;
        --jobs)
            if [[ "$#" -lt 2 ]]; then
                echo "$1 requires a positive integer." >&2
                exit 2
            fi
            jobs="$2"
            shift 2
            ;;
        --jobs=*)
            jobs="${1#*=}"
            shift
            ;;
        --class-jobs)
            if [[ "$#" -lt 2 || ! "$2" =~ ^[1-9][0-9]*$ ]]; then
                echo '--class-jobs requires a positive integer.' >&2
                exit 2
            fi
            class_options=(--class-jobs "$2")
            shift 2
            ;;
        --class-jobs=*)
            class_jobs="${1#*=}"
            if [[ ! "$class_jobs" =~ ^[1-9][0-9]*$ ]]; then
                echo '--class-jobs requires a positive integer.' >&2
                exit 2
            fi
            class_options=(--class-jobs "$class_jobs")
            shift
            ;;
        --output)
            if [[ "$#" -lt 2 || -z "$2" ]]; then
                echo '--output requires a report directory.' >&2
                exit 2
            fi
            output_options=(--output "$2")
            shift 2
            ;;
        --tests)
            if [[ "$#" -lt 2 || -z "$2" ]]; then
                echo '--tests requires a test pattern.' >&2
                exit 2
            fi
            test_filters+=(--tests "$2")
            shift 2
            ;;
        --tests=*)
            test_filters+=(--tests "${1#*=}")
            shift
            ;;
        --update-docs)
            update_docs=true
            shift
            ;;
        -Pnxn.env=*|-Pmilvus.cloud=*|-x|--exclude-task|--exclude-task=*|-x?*)
            echo "$1 conflicts with the NxN datasource runner." >&2
            exit 2
            ;;
        *)
            gradle_options+=("$1")
            shift
            ;;
    esac
done

if [[ ! "$jobs" =~ ^[1-9][0-9]*$ ]]; then
    echo '--jobs requires a positive integer.' >&2
    exit 2
fi
selected=()
for datasource in "${datasources[@]}"; do
    if [[ "$target" == "all" || "$target" == "$datasource" ]]; then
        selected+=("$datasource")
    fi
done
if [[ "${#selected[@]}" -eq 0 ]]; then
    echo "Unknown datasource: ${target}" >&2
    usage >&2
    exit 2
fi
if [[ "$update_docs" == true && "${#test_filters[@]}" -gt 0 ]]; then
    echo 'Filtered test runs cannot update the compatibility matrix.' >&2
    exit 2
fi

# Keep the lock through compilation and exec. No wrapper process remains around the test JVM.
mkdir -p .gradle
exec 9>.gradle/nxn.lock
if ! flock --nonblock 9; then
    echo 'Another NxN run is already using this checkout. Wait for it to finish.' >&2
    exit 75
fi

# The compilation process never starts NxN tests. Forward interruptions during this short phase.
gradle_pid=
stop_compilation() {
    if [[ -n "$gradle_pid" ]]; then
        kill -TERM "$gradle_pid" 2>/dev/null || true
        wait "$gradle_pid" 2>/dev/null || true
    fi
    exit "$1"
}
trap 'stop_compilation 130' INT
trap 'stop_compilation 143' TERM
./gradlew :dbvisitor-test:prepareNxnRunner "${gradle_options[@]}" 9>&- &
gradle_pid=$!
if wait "$gradle_pid"; then
    gradle_pid=
else
    exit "$?"
fi
if [[ "$dry_run" == true ]]; then
    echo "NxN tests were not started (target=${target}, threads=${jobs})."
    exit 0
fi

runner_options=("$target" --jobs "$jobs" "${class_options[@]}" "${output_options[@]}" "${test_filters[@]}")
if [[ "$update_docs" == true ]]; then
    runner_options+=(--update-docs)
fi
cd dbvisitor-test
java_command=$(<build/nxn-launcher/java.txt)
trap - INT TERM
exec "$java_command" @build/nxn-launcher/arguments.txt "${runner_options[@]}"
