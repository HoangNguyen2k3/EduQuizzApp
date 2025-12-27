# ===================================================================
# Script: APK Analysis Automation
# Purpose: Automatically decompile and analyze APK for security issues
# Usage: .\analyze_apk.ps1 -ApkPath "path\to\app.apk"
# ===================================================================

param(
    [Parameter(Mandatory = $true, HelpMessage = "Path to APK file")]
    [string]$ApkPath,
    
    [Parameter(Mandatory = $false)]
    [string]$OutputDir = "apk_analysis_output",
    
    [Parameter(Mandatory = $false)]
    [switch]$SkipDecompile = $false,
    
    [Parameter(Mandatory = $false)]
    [switch]$GenerateReport = $true
)

# Colors for output
function Write-Header($text) {
    Write-Host "`n=== $text ===" -ForegroundColor Green
}

function Write-Step($text) {
    Write-Host "[*] $text" -ForegroundColor Yellow
}

function Write-Finding($severity, $text) {
    $color = switch ($severity) {
        "CRITICAL" { "Red" }
        "HIGH" { "Magenta" }
        "MEDIUM" { "Yellow" }
        "LOW" { "Cyan" }
        "INFO" { "White" }
    }
    Write-Host "[$severity] $text" -ForegroundColor $color
}

# Check if APK exists
if (-not (Test-Path $ApkPath)) {
    Write-Host "[ERROR] APK file not found: $ApkPath" -ForegroundColor Red
    exit 1
}

$ApkName = [System.IO.Path]::GetFileNameWithoutExtension($ApkPath)
$FullOutputDir = Join-Path $PWD $OutputDir
$SourcesDir = Join-Path $FullOutputDir "sources"
$ResourcesDir = Join-Path $FullOutputDir "resources"
$ReportFile = Join-Path $FullOutputDir "security_report.txt"

Write-Header "APK Security Analysis Tool"
Write-Host "APK File: $ApkPath"
Write-Host "Output Directory: $FullOutputDir"
Write-Host "Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"

# ===================================================================
# Step 1: Decompile APK
# ===================================================================
if (-not $SkipDecompile) {
    Write-Header "Step 1: Decompiling APK with JADX"
    
    # Check if JADX is available
    try {
        $jadxVersion = jadx --version 2>&1
        Write-Step "JADX found: $jadxVersion"
    }
    catch {
        Write-Host "[ERROR] JADX not found! Please install JADX and add to PATH." -ForegroundColor Red
        Write-Host "Download from: https://github.com/skylot/jadx/releases" -ForegroundColor Cyan
        exit 1
    }
    
    # Create output directory
    if (Test-Path $FullOutputDir) {
        Write-Step "Removing existing output directory..."
        Remove-Item -Path $FullOutputDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $FullOutputDir -Force | Out-Null
    
    # Decompile
    Write-Step "Decompiling APK... (this may take a few minutes)"
    $jadxArgs = @($ApkPath, "-d", $FullOutputDir, "--show-bad-code")
    $jadxProcess = Start-Process -FilePath "jadx" -ArgumentList $jadxArgs -NoNewWindow -Wait -PassThru
    
    if ($jadxProcess.ExitCode -ne 0) {
        Write-Host "[ERROR] JADX decompilation failed!" -ForegroundColor Red
        exit 1
    }
    
    Write-Step "Decompilation complete!"
}
else {
    Write-Header "Step 1: Skipping decompilation (using existing output)"
}

# Verify sources directory exists
if (-not (Test-Path $SourcesDir)) {
    Write-Host "[ERROR] Sources directory not found: $SourcesDir" -ForegroundColor Red
    exit 1
}

# ===================================================================
# Step 2: Analyze AndroidManifest.xml
# ===================================================================
Write-Header "Step 2: Analyzing AndroidManifest.xml"

$manifestPath = Join-Path $ResourcesDir "AndroidManifest.xml"
if (Test-Path $manifestPath) {
    [xml]$manifest = Get-Content $manifestPath
    
    # Check debuggable flag
    $debuggable = $manifest.manifest.application.debuggable
    if ($debuggable -eq "true") {
        Write-Finding "CRITICAL" "android:debuggable is set to TRUE (allows debugging)"
    }
    else {
        Write-Finding "INFO" "android:debuggable is FALSE (good)"
    }
    
    # Check backup allowed
    $allowBackup = $manifest.manifest.application.allowBackup
    if ($allowBackup -eq "true") {
        Write-Finding "MEDIUM" "android:allowBackup is TRUE (data can be backed up)"
    }
    
    # List permissions
    $permissions = $manifest.manifest.SelectNodes("//uses-permission")
    Write-Step "Permissions declared: $($permissions.Count)"
    foreach ($perm in $permissions | Select-Object -First 10) {
        $permName = $perm.GetAttribute("android:name")
        Write-Host "  - $permName" -ForegroundColor Gray
    }
}
else {
    Write-Host "[WARNING] AndroidManifest.xml not found" -ForegroundColor Yellow
}

# ===================================================================
# Step 3: Find API URLs and Endpoints
# ===================================================================
Write-Header "Step 3: Finding API URLs and Endpoints"

$patterns = @{
    "http_urls"  = "http://"
    "https_urls" = "https://"
    "base_url"   = "BASE_URL|baseUrl|base_url"
}

foreach ($key in $patterns.Keys) {
    $pattern = $patterns[$key]
    Write-Step "Searching for: $key"
    
    $results = Get-ChildItem -Path $SourcesDir -Recurse -Filter "*.java" -ErrorAction SilentlyContinue |
    Select-String -Pattern $pattern -CaseSensitive:$false |
    Select-Object -First 5
    
    if ($results) {
        foreach ($result in $results) {
            $relativePath = $result.Path.Replace($SourcesDir, "")
            Write-Host "  Line $($result.LineNumber): $relativePath" -ForegroundColor Cyan
            Write-Host "    $($result.Line.Trim())" -ForegroundColor Gray
        }
    }
}

# ===================================================================
# Step 4: Find Hardcoded Secrets
# ===================================================================
Write-Header "Step 4: Searching for Hardcoded Secrets"

$secretPatterns = @{
    "Passwords" = 'password\s*=\s*"[^"]+"'
    "API Keys"  = 'api[_-]?key\s*=\s*"[^"]+"'
    "Tokens"    = 'token\s*=\s*"[^"]+"'
    "Secrets"   = 'secret\s*=\s*"[^"]+"'
}

foreach ($name in $secretPatterns.Keys) {
    $pattern = $secretPatterns[$name]
    Write-Step "Searching for: $name"
    
    $results = Get-ChildItem -Path $SourcesDir -Recurse -Filter "*.java" -ErrorAction SilentlyContinue |
    Select-String -Pattern $pattern -CaseSensitive:$false |
    Select-Object -First 3
    
    if ($results) {
        Write-Finding "HIGH" "Found potential $name!"
        foreach ($result in $results) {
            $relativePath = $result.Path.Replace($SourcesDir, "")
            Write-Host "  $relativePath : Line $($result.LineNumber)" -ForegroundColor Red
            Write-Host "    $($result.Line.Trim())" -ForegroundColor Gray
        }
    }
}

# ===================================================================
# Step 5: Find Security Implementations
# ===================================================================
Write-Header "Step 5: Analyzing Security Implementations"

$securityChecks = @{
    "Root Detection"         = "isRooted|detectRoot|checkRoot"
    "Emulator Detection"     = "isEmulator|detectEmulator"
    "Debugger Detection"     = "isDebuggerConnected|waitingForDebugger"
    "Signature Verification" = "verifySignature|checkSignature"
    "SSL Pinning"            = "CertificatePinner|pinning|PinningTrustManager"
    "Obfuscation"            = "ProGuard|R8|DexGuard"
}

foreach ($checkName in $securityChecks.Keys) {
    $pattern = $securityChecks[$checkName]
    
    $results = Get-ChildItem -Path $SourcesDir -Recurse -Filter "*.java" -ErrorAction SilentlyContinue |
    Select-String -Pattern $pattern -CaseSensitive:$false |
    Select-Object -First 1
    
    if ($results) {
        Write-Finding "INFO" "${checkName}: IMPLEMENTED"
        $relativePath = $results.Path.Replace($SourcesDir, "")
        Write-Host "  Found in: $relativePath" -ForegroundColor Gray
    }
    else {
        Write-Finding "MEDIUM" "${checkName}: NOT FOUND"
    }
}

# ===================================================================
# Step 6: Find Logging Statements
# ===================================================================
Write-Header "Step 6: Finding Debug Logging"

$logPatterns = @("Log\.d", "Log\.v", "Log\.i", "System\.out\.println")

foreach ($pattern in $logPatterns) {
    $results = Get-ChildItem -Path $SourcesDir -Recurse -Filter "*.java" -ErrorAction SilentlyContinue |
    Select-String -Pattern $pattern -CaseSensitive:$false |
    Measure-Object
    
    if ($results.Count -gt 0) {
        Write-Finding "LOW" "Found $($results.Count) instances of $pattern"
    }
}

# ===================================================================
# Step 7: Generate Report
# ===================================================================
if ($GenerateReport) {
    Write-Header "Step 7: Generating Security Report"
    
    $reportContent = @"
===================================================================
APK SECURITY ANALYSIS REPORT
===================================================================
APK File: $ApkPath
Analysis Date: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Output Directory: $FullOutputDir
===================================================================

SUMMARY:
--------
This report contains the security analysis findings from 
automated static analysis of the APK file.

RECOMMENDATIONS:
----------------
1. Review all CRITICAL and HIGH severity findings
2. Remove hardcoded secrets and credentials
3. Implement missing security controls
4. Enable code obfuscation for production builds
5. Disable debug logging in release builds
6. Implement SSL certificate pinning
7. Add runtime integrity checks

===================================================================
For detailed findings, review the console output above.
===================================================================
"@
    
    $reportContent | Out-File -FilePath $ReportFile -Encoding UTF8
    Write-Step "Report saved to: $ReportFile"
}

# ===================================================================
# Summary
# ===================================================================
Write-Header "Analysis Complete"
Write-Host "Output directory: $FullOutputDir" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Review decompiled source code in: $SourcesDir" -ForegroundColor White
Write-Host "2. Open with IDE for detailed analysis" -ForegroundColor White
Write-Host "3. Read security report: $ReportFile" -ForegroundColor White
Write-Host ""
Write-Host "Manual review areas:" -ForegroundColor Yellow
Write-Host "- Authentication logic" -ForegroundColor White
Write-Host "- Encryption implementations" -ForegroundColor White
Write-Host "- Network communication code" -ForegroundColor White
Write-Host "- Data storage mechanisms" -ForegroundColor White
Write-Host ""

# Open output directory
$openDir = Read-Host "Open output directory? (Y/N)"
if ($openDir -eq "Y" -or $openDir -eq "y") {
    Start-Process explorer.exe -ArgumentList $FullOutputDir
}
