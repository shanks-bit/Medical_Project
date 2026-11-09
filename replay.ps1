param([string]$CatalogPath = "catalog/field_catalog.json")
$ErrorActionPreference = "Stop"
# Run from repository root. Replay reads fixtures and does not call Gemini.
mvn -q -DskipTests package
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
mvn -q -DincludeScope=runtime "-Dmdep.outputFile=target/classpath.txt" dependency:build-classpath
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
$dependencies = Get-Content "target/classpath.txt" -Raw
$classpath = "target/classes;$dependencies"
java -cp $classpath com.example.caseintake.ReplayRunner "--app.catalog-path=$CatalogPath"
exit $LASTEXITCODE
