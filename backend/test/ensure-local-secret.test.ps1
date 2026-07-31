$ErrorActionPreference = 'Stop'

$helper = Join-Path (Split-Path $PSScriptRoot -Parent) 'ensure-local-secret.ps1'
$tempConfig = Join-Path ([IO.Path]::GetTempPath()) ("dessert-local-{0}.properties" -f [guid]::NewGuid())

try {
    [IO.File]::WriteAllText($tempConfig, "spring.datasource.username=test`r`n")

    & $helper -ConfigPath $tempConfig
    $first = [IO.File]::ReadAllText($tempConfig)
    $match = [regex]::Match($first, '(?m)^app\.jwt-secret=(.+)$')
    if (-not $match.Success -or $match.Groups[1].Value.Trim().Length -lt 32) {
        throw 'A secure local JWT secret was not generated.'
    }

    & $helper -ConfigPath $tempConfig
    $second = [IO.File]::ReadAllText($tempConfig)
    if ($second -ne $first) {
        throw 'Existing local JWT secret must be preserved.'
    }

    'PASS: local JWT secret is generated once and persisted.'
}
finally {
    Remove-Item -LiteralPath $tempConfig -Force -ErrorAction SilentlyContinue
}
