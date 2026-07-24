param(
  [ValidateSet("auto","android","apple","windows","macos","linux")]
  [string]$Platform = "auto"
)

$ErrorActionPreference = "Stop"

if ($Platform -eq "auto") {
  if ($IsWindows) { $Platform = "windows" }
  elseif ($IsMacOS) { $Platform = "macos" }
  elseif ($IsLinux) { $Platform = "linux" }
  else { throw "Unable to detect this platform." }
}

switch ($Platform) {
  "android" { $door = "android" }
  "apple"   { $door = "apple" }
  "windows" { $door = "desktop" }
  "macos"   { $door = "desktop" }
  "linux"   { $door = "desktop" }
}

Write-Host "JULE Omega Gate"
Write-Host "Platform: $Platform"
Write-Host "Omega origin: (0,0,0)"
Write-Host "Selected door: $door"
Write-Host "Source path: $PSScriptRoot\$door"
