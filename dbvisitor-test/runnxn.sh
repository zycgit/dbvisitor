#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

usage() {
    cat <<'EOF'
Usage:
  ./dbvisitor-test/runnxn.sh all [gradle options...]
  ./dbvisitor-test/runnxn.sh <datasource> [gradle options...]
  ./dbvisitor-test/runnxn.sh <datasource|all> --update-docs [gradle options...]
  ./dbvisitor-test/runnxn.sh --help

Run the dbvisitor-test suite against all or one real datasource.
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
gradle_task=test
gradle_options=()
for option in "$@"; do
    if [[ "$option" == "--update-docs" ]]; then
        gradle_task=updateNxnDocs
    else
        gradle_options+=("$option")
    fi
done

run_datasource() {
    local datasource="$1"
    ./gradlew ":dbvisitor-test:${gradle_task}" -Pnxn.env="$datasource" "${gradle_options[@]}"
}

if [[ "$target" == "all" ]]; then
    for datasource in "${datasources[@]}"; do
        run_datasource "$datasource"
    done
    exit 0
fi

for datasource in "${datasources[@]}"; do
    if [[ "$target" == "$datasource" ]]; then
        run_datasource "$target"
        exit 0
    fi
done

echo "Unknown datasource: ${target}" >&2
usage >&2
exit 2
