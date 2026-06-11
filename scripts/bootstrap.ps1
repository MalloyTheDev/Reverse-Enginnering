<#
.SYNOPSIS
    bootstrap.ps1 — Phase 0 developer bootstrap for the Ghidra fork (Windows).

.DESCRIPTION
    SAFE BY DESIGN:
      - Does NOT install anything (no choco/winget/scoop/etc.).
      - Does NOT run a full build.
      - Only inspects the environment.
      - Idempotent: safe to run repeatedly.

    Prints repo/version info and the recommended (but NOT executed) build commands.
#>

$ErrorActionPreference = 'Stop'

# --- locate repo root (one level up from this script) -----------------------
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot  = Split-Path -Parent $ScriptDir
Set-Location $RepoRoot

function Write-Hr { Write-Host '------------------------------------------------------------' }

Write-Hr
Write-Host 'Ghidra fork — Phase 0 bootstrap'
Write-Host "Repo root: $RepoRoot"
Write-Hr

# --- repo / version info ----------------------------------------------------
$AppProps = Join-Path 'Ghidra' 'application.properties'
if (Test-Path $AppProps) {
    Write-Host "Found $AppProps:"
    $keys = @(
        'application.name',
        'application.version',
        'application.release.name',
        'application.gradle.min',
        'application.java.min',
        'application.java.compiler',
        'application.python.supported'
    )
    Get-Content $AppProps | ForEach-Object {
        $line = $_
        foreach ($k in $keys) {
            if ($line -like "$k=*") { Write-Host "  $line" }
        }
    }
} else {
    Write-Host "WARNING: $AppProps not found — are you in the repo root?"
}
Write-Hr

# --- Java -------------------------------------------------------------------
if (Get-Command java -ErrorAction SilentlyContinue) {
    Write-Host 'Java (required: JDK 21):'
    (& java -version 2>&1) | ForEach-Object { Write-Host "  $_" }
} else {
    Write-Host 'Java: not found on PATH (JDK 21 required to build).'
}
Write-Hr

# --- Python -----------------------------------------------------------------
$pythonCmd = $null
if (Get-Command python -ErrorAction SilentlyContinue)      { $pythonCmd = 'python' }
elseif (Get-Command python3 -ErrorAction SilentlyContinue) { $pythonCmd = 'python3' }

if ($pythonCmd) {
    Write-Host 'Python (supported: 3.9 - 3.14):'
    (& $pythonCmd --version 2>&1) | ForEach-Object { Write-Host "  $_" }
} else {
    Write-Host 'Python: not found on PATH (3.9 - 3.14 needed for PyGhidra/scripting).'
}
Write-Hr

# --- Gradle wrapper ---------------------------------------------------------
if (Test-Path '.\gradlew.bat') {
    Write-Host 'Found .\gradlew.bat.'
} else {
    Write-Host 'WARNING: .\gradlew.bat not found in repo root.'
}
Write-Hr

# --- recommended commands (printed, NOT executed) ---------------------------
Write-Host 'Recommended next steps (run manually; this script does NOT run them):'
Write-Host ''
Write-Host '  1) Fetch dependencies:'
Write-Host '     .\gradlew.bat -I gradle/support/fetchDependencies.gradle -DhideDownloadProgress -DnoEclipse'
Write-Host ''
Write-Host '  2) Full build (heavy — only on adequate hardware / with approval):'
Write-Host '     .\gradlew.bat buildGhidra --parallel'
Write-Host ''
Write-Host '  3) Inspect available tasks:'
Write-Host '     .\gradlew.bat tasks --all'
Write-Hr
Write-Host 'Bootstrap complete. No packages installed, no build run.'
