# 🎯 Verification Attack Guide - Testing Protected APK
## Kiểm tra hiệu quả của Defense Implementation

> **Mục tiêu:** Tấn công lại APK đã được bảo vệ để verify các defense mechanisms hoạt động đúng

---

## 📊 So sánh: Before vs After Defense

### ⚠️ BEFORE (Unsigned/Debug APK - Không có defense)

| Attack Vector | Difficulty | Result |
|--------------|------------|--------|
| APK Decompilation | ⭐ Very Easy | ✅ Success - Code dễ đọc |
| Find API URLs | ⭐ Very Easy | ✅ Found all URLs plaintext |
| Extract Secrets | ⭐ Very Easy | ✅ Found Google Client ID, Project Number |
| Repackage APK | ⭐ Easy | ✅ App chạy bình thường |
| Run with Debugger | ⭐ Easy | ✅ Debug được |
| Run on Rooted Device | ⭐ Easy | ✅ Chạy bình thường |

### ✅ AFTER (Signed Release APK - Có defense)

| Attack Vector | Difficulty | Expected Result |
|--------------|------------|-----------------|
| APK Decompilation | ⭐⭐⭐⭐ Hard | ⚠️ Success nhưng code obfuscated (a,b,c) |
| Find API URLs | ⭐⭐⭐⭐⭐ Very Hard | ❌ Không tìm thấy plaintext |
| Extract Secrets | ⭐⭐⭐⭐⭐ Very Hard | ❌ All encrypted |
| Repackage APK | ⭐⭐⭐⭐⭐ Very Hard | ❌ App detect và thoát |
| Run with Debugger | ⭐⭐⭐⭐ Hard | ❌ App detect và kill process |
| Run on Rooted Device | ⭐⭐⭐⭐ Hard | ❌ App show warning và thoát |

---

## 🔨 Attack Scenarios & Expected Results

### Scenario 1: Decompile Protected APK

**Bước tấn công:**

```powershell
# 1. Build signed release APK
cd d:\Android\EduQuizzApp_v1
.\gradlew clean
.\gradlew assembleRelease

# 2. Sign APK (nếu chưa auto-sign)
# APK location: app\build\outputs\apk\release\app-release.apk

# 3. Decompile với JADX
cd d:\APK_Analysis
jadx ..\EduQuizzApp_v1\app\build\outputs\apk\release\app-release.apk -d protected_apk
```

**✅ Expected Results:**

**BEFORE Defense:**
```java
// File: MainActivity.java - DỄ ĐỌC
package com.example.eduquizz;

public class MainActivity extends ComponentActivity {
    protected void onCreate(Bundle savedInstanceState) {
        // Code logic rõ ràng
        PlayIntegrityHelper.checkIntegrity(this, ...);
        SignatureUtils.verifyAppSignature(this);
    }
}

// File: AuthModule.java
public class AuthModule {
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // ⚠️ LỘ
}
```

**AFTER Defense (with ProGuard/R8):**
```java
// File: a.java - KHÓ ĐỌC
package a.b.c;

public class d extends e {
    protected void onCreate(Bundle bundle) {
        // Code bị obfuscate, khó hiểu logic
        f.a(this, ...);
        g.b(this);
    }
}

// File: h.java
public class h {
    // BASE_URL KHÔNG CÒN hardcoded
    // Hoặc nằm trong BuildConfig (obfuscated)
}
```

**🎯 Verification Checklist:**

- [ ] Class names đã bị obfuscate (a, b, c thay vì MainActivity, AuthModule)?
- [ ] Method names đã bị obfuscate?
- [ ] Không tìm thấy `"http://10.0.2.2:8080/"` bằng search?
- [ ] Không tìm thấy Google Client ID plaintext?
- [ ] Không tìm thấy Play Integrity Project Number plaintext?
- [ ] Code logic khó hiểu hơn nhiều?

---

### Scenario 2: Search for Sensitive Information

**Bước tấn công:**

```powershell
cd protected_apk\sources

# Tìm API URLs
findstr /s /i "http://" *.java
findstr /s /i "10.0.2.2" *.java

# Tìm Google OAuth Client ID
findstr /s /i "1026710210552" *.java
findstr /s /i "apps.googleusercontent.com" *.java

# Tìm Play Integrity Project Number
findstr /s /i "177486006662" *.java
findstr /s /i "PROJECT_NUMBER" *.java

# Tìm Expected Signature
findstr /s /i "EC:0E:5E" *.java
findstr /s /i "EXPECTED_SIGNATURE" *.java
```

**✅ Expected Results:**

**BEFORE Defense:**
```
✅ Found: "http://10.0.2.2:8080/" in 12 files
✅ Found: "1026710210552-v084tbclfnhotnrv4lvf5i0hgppihk2r.apps.googleusercontent.com"
✅ Found: "177486006662"
✅ Found: "EC:0E:5E:7D:C8:F3:B2:9B:F1:4F:CA:E4:D1:E9:01:05:..."
```

**AFTER Defense:**
```
❌ Not found: "10.0.2.2" (moved to BuildConfig)
❌ Not found: "1026710210552" (encrypted)
❌ Not found: "177486006662" (encrypted)
❌ Not found: "EC:0E:5E" (encrypted or in obfuscated constant)
⚠️ Có thể tìm thấy: "BuildConfig.API_BASE_URL" hoặc obfuscated reference "a.b.c.d.e"
```

**🎯 Verification:**
- [ ] Không tìm thấy bất kỳ sensitive string nào bằng plaintext search

---

### Scenario 3: Repackage Attack

**Bước tấn công:**

```powershell
# 1. Decompile APK về smali
cd d:\APK_Analysis
apktool d app-release.apk -o app_modified

# 2. Modify code (e.g., bypass login)
notepad app_modified\smali\...\LoginActivity.smali
# Thay đổi logic: return true thay vì check password

# 3. Repackage APK
apktool b app_modified -o app-repackaged.apk

# 4. Sign với self-signed key
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore my-release-key.jks app-repackaged.apk my-key

# 5. Install modified APK
adb install app-repackaged.apk
```

**✅ Expected Results:**

**BEFORE Defense:**
```
✅ Repackage successful
✅ Installation successful
✅ App runs normally
✅ Modified logic works (bypass login successful)
```

**AFTER Defense:**
```
✅ Repackage successful
✅ Installation successful
❌ App launches but IMMEDIATELY EXITS with message:
   "App đã bị chỉnh sửa!"
   
Reason: Signature Verification failed
- Original signature: EC:0E:5E:7D:C8:F3:...
- Modified APK signature: [Different hash]
- SignatureUtils.verifyAppSignature() returns false
```

**Console Log:**
```
D/SIGNATURE: [actual hash of repackaged APK]
E/Security: Signature mismatch detected
```

**🎯 Verification:**
- [ ] Modified APK installs successfully
- [ ] App launches
- [ ] App shows "App đã bị chỉnh sửa!" toast
- [ ] App calls finish() and exits immediately
- [ ] Cannot bypass signature check

---

### Scenario 4: Debugger Attachment

**Bước tấn công:**

```powershell
# Method 1: Via ADB
adb shell am set-debug-app -w com.example.eduquizz
adb install app-release.apk

# Launch app
adb shell am start -n com.example.eduquizz/.MainActivity

# Method 2: Via Android Studio
# 1. Install APK
# 2. Attach debugger: Run > Attach Debugger to Android Process
# 3. Select com.example.eduquizz
```

**✅ Expected Results:**

**BEFORE Defense:**
```
✅ Debugger attached successfully
✅ Can set breakpoints
✅ Can inspect variables
✅ Can modify runtime values
✅ App runs normally in debug mode
```

**AFTER Defense:**
```
✅ Debugger attachment appears successful
❌ App IMMEDIATELY CRASHES/EXITS
   
Reason: Anti-Debugging detection
- Debug.isDebuggerConnected() returns true
- App calls android.os.Process.killProcess()
- Process terminated instantly
```

**Console Log:**
```
D/MainActivity: onCreate started
D/Debug: Debugger detected!
I/Process: Sending signal. PID: 12345 SIG: 9
```

**Screen Result:**
- App opens briefly
- Black screen or splash screen
- App disappears (killed)
- No error message (silent kill for security)

**🎯 Verification:**
- [ ] Cannot debug app with debugger attached
- [ ] App process kills itself immediately
- [ ] No way to bypass without modifying the anti-debug check itself

---

### Scenario 5: Run on Rooted Device

**Bước tấn công:**

```powershell
# 1. Use a rooted device or emulator with root
# Common rooted emulators: Genymotion, Nox Player
# Or root a real device with Magisk

# 2. Install app
adb install app-release.apk

# 3. Launch app
adb shell am start -n com.example.eduquizz/.MainActivity
```

**✅ Expected Results:**

**BEFORE Defense:**
```
✅ App installs successfully
✅ App runs normally
✅ No detection of root
✅ Full functionality available
```

**AFTER Defense (if Root Detection implemented):**
```
✅ App installs successfully
✅ App launches
⚠️ App shows dialog:
   "This app cannot run on rooted devices for security reasons."
   [Exit button]
❌ App exits when user clicks Exit
   
OR (if you choose silent exit):
❌ App exits immediately without message
```

**Detection Methods that will trigger:**
- `/system/bin/su` exists
- `com.topjohnwu.magisk` package detected
- Build.TAGS contains "test-keys"
- Can execute `su` command

**🎯 Verification:**
- [ ] Root detected correctly
- [ ] App shows warning or exits
- [ ] Cannot use app on rooted device

**⚠️ Note:** Trong code hiện tại của bạn, tôi chưa thấy Root Detection được implement. Bạn cần thêm code từ DEFENSE_IMPLEMENTATION_GUIDE.md nếu muốn test scenario này.

---

### Scenario 6: Play Integrity API Check

**Bước tấn công:**

```powershell
# Attacker không thể bypass Play Integrity dễ dàng
# Nhưng có thể test với các scenario:

# Scenario A: Modified APK
# 1. Repackage APK như Scenario 3
# 2. Install modified APK
# 3. Launch app
```

**✅ Expected Results:**

**Modified APK:**
```
✅ App launches
⚠️ Play Integrity check runs in background
❌ Check FAILS với verdict:
   "appRecognitionVerdict": "UNEVALUATED" hoặc "PLAY_RECOGNIZES_TAMPERING"
   
Result: App shows toast "App có dấu hiệu bị chỉnh sửa!" và exits
```

**On Emulator (không phải production environment):**
```
⚠️ Play Integrity may fail with:
   "deviceRecognitionVerdict": ["MEETS_BASIC_INTEGRITY"]
   (không có "MEETS_DEVICE_INTEGRITY")
   
Note: This is expected on emulator/rooted devices
```

**On Real Device, Official APK:**
```
✅ Play Integrity PASSES
   "appRecognitionVerdict": "PLAY_RECOGNIZED"
   "deviceRecognitionVerdict": ["MEETS_DEVICE_INTEGRITY"]
   
Result: App continues normally
```

**🎯 Verification:**
- [ ] Modified APK fails Play Integrity
- [ ] Official APK on real device passes
- [ ] App exits on integrity failure

---

## 🔍 Complete Verification Attack Plan

### Phase 1: Static Analysis

```powershell
# 1. Build protected APK
cd d:\Android\EduQuizzApp_v1
.\gradlew clean assembleRelease

# 2. Decompile
cd d:\APK_Analysis
jadx app-release.apk -d protected

# 3. Run automated analysis
cd ..\EduQuizzApp_v1\scripts
.\analyze_apk.ps1 -ApkPath "..\app\build\outputs\apk\release\app-release.apk"

# 4. Manual verification
cd d:\APK_Analysis\protected\sources
# Search for sensitive data (should find NOTHING)
findstr /s /i "10.0.2.2" *.java
findstr /s /i "1026710210552" *.java
findstr /s /i "177486006662" *.java
```

**Expected Output from analyze_apk.ps1:**
```
[INFO] Code Obfuscation: ENABLED ✅
[INFO] Class names obfuscated: a, b, c, d...
[MEDIUM] http:// references: 0 (down from 12) ✅
[INFO] Debug logging: REMOVED ✅
[INFO] Signature Verification: IMPLEMENTED ✅
[INFO] Play Integrity API: IMPLEMENTED ✅
[INFO] Debugger Detection: IMPLEMENTED ✅
```

### Phase 2: Dynamic Analysis - Runtime Testing

```powershell
# Test 1: Normal Device
adb install app-release.apk
adb shell am start -n com.example.eduquizz/.MainActivity
# Expected: ✅ App runs normally

# Test 2: Modified APK
apktool d app-release.apk -o modified
apktool b modified -o app-modified.apk
jarsigner -keystore debug.keystore app-modified.apk androiddebugkey
adb install -r app-modified.apk
adb shell am start -n com.example.eduquizz/.MainActivity
# Expected: ❌ App shows "App đã bị chỉnh sửa!" và exits

# Test 3: Debugger
adb shell am set-debug-app -w com.example.eduquizz
adb shell am start -n com.example.eduquizz/.MainActivity
# Expected: ❌ App kills itself immediately

# Test 4: Monitor logs
adb logcat -s MainActivity:* Integrity:* Security:* SIGNATURE:*
# Watch for security-related logs
```

### Phase 3: Verification Report

**Create a checklist:**

```markdown
# Security Verification Report

## Static Analysis Results
- [ ] Code is obfuscated (class names: a,b,c)
- [ ] No plaintext API URLs found
- [ ] No plaintext secrets found (Google Client ID, Project Number)
- [ ] No plaintext expected signature
- [ ] Debug logs removed
- [ ] ProGuard mapping.txt generated

## Dynamic Analysis Results
- [ ] Normal signed APK runs successfully
- [ ] Modified APK detected and app exits
- [ ] Debugger attachment triggers app kill
- [ ] Play Integrity check active
- [ ] Appropriate error messages shown

## Attack Resistance
- [ ] Decompilation: Code hard to understand ✅
- [ ] Repackaging: Detected and blocked ✅
- [ ] Debugging: Detected and blocked ✅
- [ ] Root: (If implemented) Detected ✅

## Overall Security Posture
Before: ⭐ (Very Weak)
After: ⭐⭐⭐⭐ (Strong)

Improvement: +400% security
```

---

## 📝 Summary: Attack Results Before vs After

| Attack | Before | After | Defense That Blocks It |
|--------|--------|-------|----------------------|
| **Decompile APK** | Easy to read code | Obfuscated code (very hard) | ProGuard/R8 |
| **Find API URLs** | Found all 12 instances | Not found (BuildConfig) | BuildConfig + Encryption |
| **Extract Secrets** | All visible | All encrypted | String Encryption |
| **Repackage & Run** | Works perfectly | Detects & exits | Signature Verification |
| **Debug APK** | Can debug freely | App kills itself | Anti-Debugging |
| **Root Access** | No check | Detects & exits | Root Detection |
| **Modify Code Logic** | Works after repackage | Blocked by signature | Signature + Integrity |

---

## 🎯 Next Steps After Verification

1. **If defenses work:** 
   - Document results
   - Keep mapping.txt safe for crash reports
   - Monitor Play Console for tampering attempts

2. **If defenses fail:**
   - Check ProGuard rules
   - Verify signature verification logic
   - Test on different devices
   - Review implementation

3. **Continuous Improvement:**
   - Add more obfuscation layers
   - Implement native code protection
   - Add server-side verification
   - Monitor attack attempts

---

**⚠️ Important Notes:**

- Complete security is impossible - goal is to make attacks very difficult
- Determined attackers can still bypass (with significant effort)
- Multiple layers of defense = better protection
- Regular updates and monitoring essential
- Consider professional security audit for production apps

**🎓 Learning Outcome:**

Sau verification attack, bạn sẽ thấy rõ sự khác biệt:
- **Before:** Script kiddie có thể tấn công trong 30 phút
- **After:** Cần security expert + nhiều giờ/ngày để tấn công thành công
