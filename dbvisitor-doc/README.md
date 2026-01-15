
## build website
npm run build

https://github.com/jean-humann/docs-to-pdf

## release versions and prepare next SNAPSHOT version
    ./mvnw release:clean release:prepare -Prelease -Darguments="-DskipTests=true"

## push to center maven repository (version tag must be RELEASE)
    ./mvnw clean package install deploy -Prelease -Dmaven.test.skip=true

# 本项目提供了一个聚合的单元测试覆盖率报告模块
    ./mvnw clean test -pl dbvisitor-report -am
