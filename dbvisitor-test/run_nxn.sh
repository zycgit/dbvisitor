#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
TARGET="${1:-all}"

run_env() {
  local env="$1"
  mvn -f "$ROOT/pom.xml" -Pnxn -Dnxn.env="$env" test
}

case "$TARGET" in
  all)
    for env in h2 mysql pg mssql oracle redis mongo es6 es7 milvus; do
      run_env "$env"
    done
    ;;
  h2|mysql|pg|mssql|oracle|redis|mongo|es6|es7|milvus)
    run_env "$TARGET"
    ;;
  elastic6)
    run_env es6
    ;;
  elastic7)
    run_env es7
    ;;
  *)
    echo "Usage: $0 [all|h2|mysql|pg|mssql|oracle|redis|mongo|es6|es7|elastic6|elastic7|milvus]" >&2
    exit 2
    ;;
esac
