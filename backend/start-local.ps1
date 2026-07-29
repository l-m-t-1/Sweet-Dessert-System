$ErrorActionPreference = 'Stop'

$localConfig = Join-Path $PSScriptRoot 'application-local.properties'
if (-not (Test-Path -LiteralPath $localConfig)) {
    throw 'Missing backend/application-local.properties. Configure the local database account first.'
}

Push-Location $PSScriptRoot
try {
    & (Join-Path $PSScriptRoot 'mvnw.cmd') `
        '-Dspring-boot.run.arguments=--spring.config.additional-location=optional:file:./application-local.properties' `
        clean `
        spring-boot:run
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
