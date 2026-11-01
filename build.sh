#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

# Project-specific targets; build and release logic below is shared across repositories.
project_name=dbvisitor
release_tag_prefix=v
local_publish_task=publishToMavenLocal
web_project=
web_command=()
nxn_command=(./dbvisitor-test/runnxn.sh all)
plugin_build=

usage() {
    cat <<'EOF'
Usage:
  ./build.sh package [test] [gradle options...]
  ./build.sh install [test] [gradle options...]
  ./build.sh deploy  [test] [gradle options...]
  ./build.sh release [deploy] [test] [gradle options...]

Commands:
  package    Clean and build packages; tests are skipped unless test is present.
  install    Build packages and publish to the local repository.
  deploy     Build and publish locally, then upload signed packages to Maven Central.
  release    Prepare a release commit and tag, then the next development version.
  test       Enable unit tests; can also be used alone to build and test.
EOF
    if [[ -n "$web_project" || ${#web_command[@]} -gt 0 ]]; then
        echo '  web        Build the project frontend.'
    fi
    if [[ ${#nxn_command[@]} -gt 0 ]]; then
        echo '  nxn        Run the real-datasource matrix; can be combined with test or install.'
    fi
    if [[ "$local_publish_task" == publishToMavenLocal ]]; then
        echo 'Local publication: the standard Maven Local repository.'
    else
        echo 'Local publication: build/maven-repository.'
    fi
    cat <<'EOF'

Deploy properties (Gradle user-home gradle.properties or -Pkey=value):
  maven.central.username
  maven.central.password
  maven.central.signing_key
  maven.central.signing_password
  maven.central.publishing_type=USER_MANAGED

Windows (CMD or PowerShell, JDK 17):
  .\gradlew.bat clean
  .\gradlew.bat build -x test
  .\gradlew.bat build
EOF
    printf '  .\\gradlew.bat %s\n' "$local_publish_task"
    if [[ -n "$plugin_build" ]]; then
        printf '  .\\gradlew.bat --include-build %s :%s:publishToMavenLocal\n' "$plugin_build" "${plugin_build##*/}"
    fi
    if [[ -n "$web_project" ]]; then
        printf '  .\\gradlew.bat :%s:assemble -x test\n' "$web_project"
    elif [[ ${#web_command[@]} -gt 0 ]]; then
        echo '  .\gradlew.bat :meteor-console:processResources'
    fi
}

read_property() {
    local file="$1" key="$2"
    [[ -f "$file" ]] || return 0
    awk -F= -v key="$key" '
        $0 !~ /^[[:space:]]*(#|!|$)/ {
            prop=$1
            gsub(/^[[:space:]]+|[[:space:]]+$/, "", prop)
            if (prop == key) {
                value=substr($0, index($0, "=") + 1)
                gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
            }
        }
        END { print value }
    ' "$file"
}

read_publish_property() {
    local key="$1" value arg
    value="$(read_property gradle.properties "$key")"
    local user_value
    user_value="$(read_property "$gradle_user_home/gradle.properties" "$key")"
    if [[ -n "$user_value" ]]; then
        value="$user_value"
    fi
    for arg in "${gradle_args[@]}"; do
        if [[ "$arg" == "-P${key}="* || "$arg" == "${key}="* ]]; then
            value="${arg#*=}"
        fi
    done
    printf '%s\n' "$value"
}

set_project_version() {
    mkdir -p build
    awk -v version="$1" '/^version=/ { $0="version=" version } { print }' \
        gradle.properties > build/gradle.properties.release
    cat build/gradle.properties.release > gradle.properties
    rm build/gradle.properties.release
}

next_snapshot_of() {
    if [[ "$1" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
        printf '%s.%s.%s-SNAPSHOT\n' "${BASH_REMATCH[1]}" "${BASH_REMATCH[2]}" "$((BASH_REMATCH[3] + 1))"
    else
        printf '%s-SNAPSHOT\n' "$1"
    fi
}

prompt_with_default() {
    local value
    read -r -p "$1 [$2]: " value
    printf '%s\n' "${value:-$2}"
}

prepare_release() {
    # Confirm the release plan before changing the version or Git history.
    if [[ -n "$(git status --porcelain)" ]]; then
        echo 'Release requires a clean git worktree.' >&2
        git status --short >&2
        exit 1
    fi
    local current_version release_version next_version confirm
    current_version="$(read_property gradle.properties version)"
    echo "Current version: ${current_version}"
    release_version="$(prompt_with_default 'Release version' "${current_version%-SNAPSHOT}")"
    next_version="$(prompt_with_default 'Next development version' "$(next_snapshot_of "$release_version")")"
    local release_tag="${release_tag_prefix}${release_version}"
    echo "Release version: ${release_version}; tag: ${release_tag}; next version: ${next_version}"
    echo "Tests: ${run_tests}; NXN: ${run_nxn}; deploy: ${release_deploy}"
    read -r -p 'Continue? [y/N]: ' confirm
    if [[ "$confirm" != y && "$confirm" != Y ]]; then
        echo 'Release cancelled.'
        exit 1
    fi

    # Build with the selected project options; restore the version if verification fails.
    local build_args=(package)
    if [[ "$run_tests" == true ]]; then build_args+=(test); fi
    if [[ "$run_nxn" == true ]]; then build_args+=(nxn); fi
    if [[ "$web_only" == true ]]; then build_args+=(web); fi
    set_project_version "$release_version"
    if ! ./build.sh "${build_args[@]}" "${gradle_args[@]}"; then
        set_project_version "$current_version"
        echo "Release build failed. Version restored to ${current_version}." >&2
        exit 1
    fi
    git add gradle.properties
    git commit -m "Release ${release_version}"
    git tag -a "$release_tag" -m "Release ${release_version}"

    # Finish any requested upload before advancing to the next development version.
    if [[ "$release_deploy" == true ]]; then
        local deploy_args=(deploy)
        if [[ "$web_only" == true ]]; then deploy_args+=(web); fi
        if ! ./build.sh "${deploy_args[@]}" "${gradle_args[@]}"; then
            echo "Deploy failed. Version remains at ${release_version}." >&2
            exit 1
        fi
    fi
    set_project_version "$next_version"
    git add gradle.properties
    git commit -m "Next development version ${next_version}"
}

if [[ $# -eq 0 ]]; then
    usage
    exit 0
fi

mode=package
mode_explicit=false
run_tests=false
run_nxn=false
web_only=false
dry_run=false
release_deploy=false
gradle_args=()
gradle_user_home="${GRADLE_USER_HOME:-$HOME/.gradle}"

# Parse build commands while preserving Gradle option values and their quoting.
while [[ $# -gt 0 ]]; do
    case "$1" in
        help|-h|--help) usage; exit 0 ;;
        package|install) mode="$1"; mode_explicit=true ;;
        deploy)
            if [[ "$mode" == release ]]; then release_deploy=true; else mode=deploy; fi
            mode_explicit=true
            ;;
        release)
            if [[ "$mode" == deploy ]]; then release_deploy=true; fi
            mode=release
            mode_explicit=true
            ;;
        test) run_tests=true ;;
        web) web_only=true ;;
        nxn) run_nxn=true ;;
        --dry-run|-m) dry_run=true; gradle_args+=("$1") ;;
        -p|--project-dir|--include-build|--project-cache-dir|--priority|\
        -g|--gradle-user-home|-x|--exclude-task|-P|--project-prop|-D|--system-prop|\
        --max-workers|--console|--warning-mode|-I|--init-script|--tests)
            if [[ $# -lt 2 ]]; then echo "$1 requires a value." >&2; exit 2; fi
            if [[ "$1" == -g || "$1" == --gradle-user-home ]]; then gradle_user_home="$2"; fi
            gradle_args+=("$1" "$2")
            shift
            ;;
        --gradle-user-home=*) gradle_user_home="${1#*=}"; gradle_args+=("$1") ;;
        --) gradle_args+=("$@"); break ;;
        *) gradle_args+=("$1") ;;
    esac
    shift
done

if [[ "$web_only" == true ]]; then
    if [[ ${#web_command[@]} -gt 0 ]]; then
        if [[ "$mode_explicit" == true || "$run_tests" == true || "$run_nxn" == true ]]; then
            echo 'Use web as a standalone frontend command.' >&2
            exit 2
        fi
        exec "${web_command[@]}" "${gradle_args[@]}"
    elif [[ -z "$web_project" ]]; then
        echo 'This project has no web build command.' >&2
        exit 2
    fi
fi
if [[ "$run_nxn" == true ]]; then
    if [[ ${#nxn_command[@]} -eq 0 ]]; then
        echo 'This project has no NXN command.' >&2
        exit 2
    fi
    if [[ "$run_tests" == false && "$mode_explicit" == false ]]; then
        exec "${nxn_command[@]}" "${gradle_args[@]}"
    fi
fi
if [[ "$mode" == release ]]; then
    if [[ "$dry_run" == true ]]; then
        echo 'Release does not support --dry-run because it creates commits and tags.' >&2
        exit 2
    fi
    prepare_release
    exit 0
fi

# Select this project's build and publication targets.
task_prefix=
if [[ "$web_only" == true ]]; then task_prefix=":${web_project}:"; fi
tasks=("${task_prefix}build")
if [[ "$web_only" == true && "$run_tests" == false ]]; then tasks=("${task_prefix}assemble"); fi
plugin_tasks=()
if [[ "$mode" == install || "$mode" == deploy ]]; then
    tasks+=("${task_prefix}${local_publish_task}")
    if [[ -n "$plugin_build" ]]; then
        plugin_tasks+=(--include-build "$plugin_build" ":${plugin_build##*/}:publishToMavenLocal")
    fi
fi
if [[ "$mode" == deploy ]]; then
    central_dir="$PWD/build/central-bundle/repository"
    central_zip="$PWD/build/central-bundle/central-bundle.zip"
    tasks+=("${task_prefix}publishAllPublicationsToCentralBundleRepository")
    if [[ -n "$plugin_build" ]]; then
        plugin_tasks+=(":${plugin_build##*/}:publishAllPublicationsToCentralBundleRepository")
    fi
    if [[ "$dry_run" == false ]]; then
        version="$(read_property gradle.properties version)"
        if [[ "$version" == *-SNAPSHOT ]]; then
            echo "Maven Central deploy requires a release version, current version is ${version}." >&2
            exit 1
        fi
        central_username="$(read_publish_property maven.central.username)"
        central_password="$(read_publish_property maven.central.password)"
        publishing_type="$(read_publish_property maven.central.publishing_type)"
        publishing_type="${publishing_type:-USER_MANAGED}"
        if [[ "$publishing_type" != AUTOMATIC && "$publishing_type" != USER_MANAGED ]]; then
            echo 'maven.central.publishing_type must be AUTOMATIC or USER_MANAGED.' >&2
            exit 1
        fi
        if [[ -z "$central_username" || -z "$central_password" ]]; then
            echo 'maven.central.username and maven.central.password are required for deploy.' >&2
            exit 1
        fi
        rm -rf -- "$central_dir"
        rm -f -- "$central_zip"
    fi
    gradle_args+=("-PcentralRelease=true" "-PcentralBundleDir=${central_dir}")
fi

# Apply common defaults only when the caller did not supply them.
has_parallel_option=false
has_max_workers_option=false
for arg in "${gradle_args[@]}"; do
    case "$arg" in
        --parallel|--no-parallel) has_parallel_option=true ;;
        --max-workers|--max-workers=*) has_max_workers_option=true ;;
    esac
done
gradle_defaults=()
if [[ "$has_parallel_option" == false ]]; then gradle_defaults+=(--parallel); fi
if [[ "$has_max_workers_option" == false ]]; then gradle_defaults+=(--max-workers 8); fi
build_gradle_args=("${gradle_args[@]}")
if [[ "$run_tests" == false ]]; then build_gradle_args+=(-x test); fi

# Finish cleaning before parallel build tasks can consume their outputs.
./gradlew "${task_prefix}clean" "${gradle_defaults[@]}" "${build_gradle_args[@]}"
./gradlew "${tasks[@]}" "${gradle_defaults[@]}" "${build_gradle_args[@]}"
if [[ ${#plugin_tasks[@]} -gt 0 ]]; then
    ./gradlew "${plugin_tasks[@]}" "${gradle_defaults[@]}" "${build_gradle_args[@]}"
fi
if [[ "$run_nxn" == true ]]; then
    if [[ "$mode" != deploy || "$dry_run" == true ]]; then
        exec "${nxn_command[@]}" "${gradle_args[@]}"
    fi
    "${nxn_command[@]}" "${gradle_args[@]}" &
    nxn_pid=$!
    trap 'kill -TERM "$nxn_pid" 2>/dev/null || true; wait "$nxn_pid" 2>/dev/null || true; exit 130' INT
    trap 'kill -TERM "$nxn_pid" 2>/dev/null || true; wait "$nxn_pid" 2>/dev/null || true; exit 143' TERM
    wait "$nxn_pid"
    trap - INT TERM
fi

if [[ "$mode" == deploy && "$dry_run" == false ]]; then
    # Gradle provides signatures and checksums; the bundle omits repository metadata.
    find "$central_dir" -type f -name 'maven-metadata.xml*' -delete
    jar --create --no-manifest --file "$central_zip" -C "$central_dir" .

    # Keep the upload credential out of process arguments.
    token="$(printf '%s:%s' "$central_username" "$central_password" | base64 | tr -d '\n')"
    upload_url="https://central.sonatype.com/api/v1/publisher/upload"
    upload_url+="?name=${project_name}-${version}&publishingType=${publishing_type}"
    response="$(printf 'header = "Authorization: Bearer %s"\n' "$token" |
        curl --disable --config - --fail --silent --show-error --request POST \
            --connect-timeout 30 --max-time 300 --write-out $'\n%{http_code}' \
            --form "bundle=@${central_zip};type=application/octet-stream" "$upload_url")"
    status="${response##*$'\n'}"
    if [[ "$status" != 2[0-9][0-9] ]]; then
        echo "Maven Central upload failed with HTTP ${status}." >&2
        exit 1
    fi
    echo "Maven Central deployment id: ${response%$'\n'*}"
fi
