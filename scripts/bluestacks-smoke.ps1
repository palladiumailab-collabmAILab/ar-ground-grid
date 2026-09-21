param(
    [string]$ApkPath = "app\build\outputs\apk\debug\app-debug.apk",
    [string]$AdbPath = "",
    [string]$Serial = ""
)

$ErrorActionPreference = "Stop"

$PackageName = "com.palladiumailab.argroundgrid"
$ActivityName = "$PackageName/.MainActivity"
$CameraPermission = "android.permission.CAMERA"

function Resolve-AdbPath {
    if ($AdbPath) {
        return (Resolve-Path $AdbPath).Path
    }

    $adbCommand = Get-Command adb -ErrorAction SilentlyContinue
    if ($adbCommand) {
        return $adbCommand.Source
    }

    $candidates = @(
        "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
        "$env:ProgramFiles\BlueStacks_nxt\HD-Adb.exe"
    )

    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    throw "ADBが見つかりません。BlueStacks 5の Settings > Advanced > Android Debug Bridge を有効にし、必要なら -AdbPath で adb.exe / HD-Adb.exe を指定してください。"
}

function Invoke-Adb {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Arguments)
    & $script:ResolvedAdb -s $script:ResolvedSerial @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "ADB command failed: $($Arguments -join ' ')"
    }
}

function Dump-Ui {
    Invoke-Adb shell uiautomator dump /sdcard/ar-ground-grid-window.xml | Out-Null
    return (Invoke-Adb shell cat /sdcard/ar-ground-grid-window.xml | Out-String)
}

$ResolvedAdb = Resolve-AdbPath
$ResolvedApk = (Resolve-Path $ApkPath).Path

$deviceLines = & $ResolvedAdb devices
if ($LASTEXITCODE -ne 0) {
    throw "ADB devicesの取得に失敗しました: $ResolvedAdb"
}

$devices = @(
    $deviceLines |
        Select-String -Pattern '^([^\s]+)\s+device$' |
        ForEach-Object { $_.Matches[0].Groups[1].Value }
)

if ($Serial) {
    if ($devices -notcontains $Serial) {
        throw "指定したdevice '$Serial' が見つかりません。接続中: $($devices -join ', ')"
    }
    $ResolvedSerial = $Serial
} elseif ($devices.Count -eq 1) {
    $ResolvedSerial = $devices[0]
} elseif ($devices.Count -eq 0) {
    throw "ADB deviceが見つかりません。BlueStacksを起動し、Settings > Advanced > Android Debug BridgeをONにしてください。"
} else {
    throw "複数deviceが接続されています。-Serial を指定してください: $($devices -join ', ')"
}

Write-Host "ADB: $ResolvedAdb"
Write-Host "Device: $ResolvedSerial"
Write-Host "APK: $ResolvedApk"

Write-Host "[1/3] Install debug APK"
Invoke-Adb install -r $ResolvedApk | Out-Host

Write-Host "[2/3] Verify camera-permission denied UI"
& $ResolvedAdb -s $ResolvedSerial shell pm revoke $PackageName $CameraPermission 2>$null | Out-Null
Invoke-Adb shell am force-stop $PackageName | Out-Null
Invoke-Adb shell am start -n $ActivityName | Out-Null
Start-Sleep -Seconds 2

# ArGridScreen requests CAMERA immediately. Back dismisses the system dialog so the app's
# own denied-state UI can be inspected deterministically.
Invoke-Adb shell input keyevent 4 | Out-Null
Start-Sleep -Seconds 1
$permissionUi = Dump-Ui

if ($permissionUi -notmatch 'AR表示にはカメラ権限が必要です' -or $permissionUi -notmatch 'カメラを許可') {
    throw "カメラ権限拒否UIを確認できませんでした。uiautomator dumpに期待テキストがありません。"
}

Write-Host "PASS: permission-denied UI"

Write-Host "[3/3] Grant camera and verify app survives AR startup/failure"
Invoke-Adb shell pm grant $PackageName $CameraPermission | Out-Null
Invoke-Adb shell am force-stop $PackageName | Out-Null
Invoke-Adb shell am start -n $ActivityName | Out-Null
Start-Sleep -Seconds 5

$pid = (Invoke-Adb shell pidof $PackageName | Out-String).Trim()
if (-not $pid) {
    throw "CAMERA付与後にアプリprocessが生存していません。"
}

$arUi = Dump-Ui
$expectedStates = @(
    '床を探しています',
    '床をタップしてグリッドを配置',
    '追跡待機中',
    'ARを開始できません'
)

$matched = $false
foreach ($state in $expectedStates) {
    if ($arUi -match [regex]::Escape($state)) {
        $matched = $true
        break
    }
}

if (-not $matched) {
    throw "CAMERA付与後の期待UI状態を確認できませんでした。"
}

Write-Host "PASS: app process alive after camera grant"
Write-Host "PASS: AR startup or graceful AR initialization failure is visible"
Write-Host "BlueStacks smoke test completed. ARCoreの平面検出・実寸精度は実機評価 #7 で確認してください。"
