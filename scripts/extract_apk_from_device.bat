@echo off
REM ===================================================================
REM Script: Extract APK from Android Device/Emulator
REM Purpose: Automatically pull APK from connected device
REM Usage: extract_apk_from_device.bat com.example.eduquizz
REM ===================================================================

setlocal enabledelayedexpansion

REM Check if package name is provided
if "%~1"=="" (
    echo [ERROR] Package name not provided!
    echo Usage: %~nx0 ^<package_name^>
    echo Example: %~nx0 com.example.eduquizz
    exit /b 1
)

set PACKAGE_NAME=%~1
set OUTPUT_FILE=%PACKAGE_NAME%.apk

echo ===================================================================
echo Extracting APK from Device
echo ===================================================================
echo Package: %PACKAGE_NAME%
echo Output: %OUTPUT_FILE%
echo ===================================================================

REM Check if ADB is available
where adb >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] ADB not found! Please install Android SDK Platform Tools.
    exit /b 1
)

REM Check if device is connected
echo [1/4] Checking device connection...
adb devices | findstr /C:"device" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] No device connected! Please connect a device or start emulator.
    exit /b 1
)
echo [OK] Device connected

REM Get package path
echo [2/4] Getting APK path from device...
for /f "tokens=2 delims=:" %%i in ('adb shell pm path %PACKAGE_NAME% 2^>nul') do (
    set APK_PATH=%%i
)

if "!APK_PATH!"=="" (
    echo [ERROR] Package "%PACKAGE_NAME%" not found on device!
    echo Available packages:
    adb shell pm list packages | findstr /i edu
    exit /b 1
)

REM Remove whitespace/newline
set APK_PATH=!APK_PATH: =!

echo [OK] APK found at: !APK_PATH!

REM Pull APK from device
echo [3/4] Pulling APK from device...
adb pull !APK_PATH! %OUTPUT_FILE%

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to pull APK from device!
    exit /b 1
)

echo [OK] APK pulled successfully

REM Verify file
echo [4/4] Verifying APK file...
if exist %OUTPUT_FILE% (
    for %%A in (%OUTPUT_FILE%) do set FILE_SIZE=%%~zA
    echo [OK] APK extracted successfully!
    echo File: %OUTPUT_FILE%
    echo Size: !FILE_SIZE! bytes
) else (
    echo [ERROR] APK file not found after extraction!
    exit /b 1
)

echo ===================================================================
echo SUCCESS! APK extracted to: %cd%\%OUTPUT_FILE%
echo ===================================================================
echo.
echo Next steps:
echo 1. Decompile with JADX: jadx %OUTPUT_FILE% -d output_folder
echo 2. Decompile with APKTool: apktool d %OUTPUT_FILE%
echo ===================================================================

endlocal
