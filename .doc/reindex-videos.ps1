$ErrorActionPreference = "Stop"

param(
  [int]$PageSize = 200
)

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

if (-not (Get-Command gradle -ErrorAction SilentlyContinue)) {
  Write-Host "gradle not found in PATH. Install Gradle or add it to PATH."
  exit 1
}

gradle :mytube-video-service:bootRun --args="--reindex.videos.enabled=true --reindex.videos.page-size=$PageSize"
