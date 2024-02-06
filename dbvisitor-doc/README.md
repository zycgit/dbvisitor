
## build website
npm run build

https://github.com/jean-humann/docs-to-pdf

## release versions and prepare next SNAPSHOT version
    mvn release:clean release:prepare -Dmaven.test.skip -Prelease

## push to center maven repository (version tag must be RELEASE)
    mvn clean package install deploy -Prelease
