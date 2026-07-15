#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

usage() {
    cat <<'EOF'
Usage:
  ./runnxn.sh all [gradle options...]
  ./runnxn.sh <datasource> [gradle options...]
  ./runnxn.sh --help

Run the dbvisitor-test suite against all or one real datasource.
dbvisitor-test always uses real databases; this script does not provide a mock mode.
With no arguments, this help is printed and no tests are run.

Datasources:
  h2 mysql pg mssql oracle db2 clickhouse redis mongo es6 es7 milvus

Aliases:
  elastic6 -> es6
  elastic7 -> es7

Examples:
  ./runnxn.sh all
  ./runnxn.sh pg
  ./runnxn.sh mysql --rerun-tasks
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

run_datasource() {
    local datasource="$1"
    shift
    ./gradlew :dbvisitor-test:test -Pnxn.env="$datasource" --rerun-tasks "$@"
}

if [[ "$target" == "all" ]]; then
    for datasource in "${datasources[@]}"; do
        run_datasource "$datasource" "$@"
    done
    exit 0
fi

for datasource in "${datasources[@]}"; do
    if [[ "$target" == "$datasource" ]]; then
        run_datasource "$target" "$@"
        exit 0
    fi
done

echo "Unknown datasource: ${target}" >&2
usage >&2
exit 2
