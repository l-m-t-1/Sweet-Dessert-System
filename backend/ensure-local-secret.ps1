param(
    [Parameter(Mandatory = $true)]
    [string]$ConfigPath
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath $ConfigPath)) {
    throw "Local configuration file not found: $ConfigPath"
}

$content = [IO.File]::ReadAllText($ConfigPath)
$secretPattern = '(?m)^\s*app\.jwt-secret\s*=\s*(.*)\s*$'
$existing = [regex]::Match($content, $secretPattern)
if ($existing.Success -and $existing.Groups[1].Value.Trim().Length -ge 32) {
    return
}

$bytes = New-Object byte[] 48
$generator = [Security.Cryptography.RandomNumberGenerator]::Create()
try {
    $generator.GetBytes($bytes)
}
finally {
    $generator.Dispose()
}
$secretLine = 'app.jwt-secret=' + [Convert]::ToBase64String($bytes)

if ($existing.Success) {
    $content = [regex]::Replace($content, $secretPattern, $secretLine)
}
else {
    if ($content.Length -gt 0 -and -not $content.EndsWith("`n")) {
        $content += "`r`n"
    }
    $content += $secretLine + "`r`n"
}

$utf8WithoutBom = New-Object Text.UTF8Encoding($false)
[IO.File]::WriteAllText($ConfigPath, $content, $utf8WithoutBom)
