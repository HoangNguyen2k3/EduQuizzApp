# 🔐 Signed APK vs Unsigned APK - Sự khác biệt khi bị tấn công

## 📚 Mục lục
1. [APK Signing là gì?](#1-apk-signing-là-gì)
2. [Cấu trúc APK: Signed vs Unsigned](#2-cấu-trúc-apk-signed-vs-unsigned)
3. [Khác biệt khi Decompile](#3-khác-biệt-khi-decompile)
4. [Khác biệt khi Repackage](#4-khác-biệt-khi-repackage)
5. [Khác biệt khi Runtime](#5-khác-biệt-khi-runtime)
6. [Attack Scenarios So sánh](#6-attack-scenarios-so-sánh)

---

## 1. APK Signing là gì?

### Definition

**APK Signing** = Quá trình ký số APK file bằng private key để:
- Xác nhận danh tính developer
- Đảm bảo APK không bị chỉnh sửa sau khi được ký
- Cho phép Android verify integrity của APK

### Signing Schemes

Android hỗ trợ 3 signing schemes:

| Scheme | Version | Security Level | Description |
|--------|---------|---------------|-------------|
| **v1 (JAR)** | All | ⭐⭐ Low | Sign từng file riêng lẻ, dễ bypass |
| **v2 (APK)** | Android 7.0+ | ⭐⭐⭐⭐ High | Sign toàn bộ APK, khó modify |
| **v3** | Android 9.0+ | ⭐⭐⭐⭐⭐ Very High | Hỗ trợ key rotation |

**Recommended:** Sử dụng cả v1 + v2 + v3 cho compatibility tốt nhất

---

## 2. Cấu trúc APK: Signed vs Unsigned

### 📦 Unsigned APK Structure

```
app-debug-unsigned.apk
├── AndroidManifest.xml
├── classes.dex
├── classes2.dex
├── res/
│   ├── layout/
│   ├── drawable/
│   └── values/
├── resources.arsc
├── assets/
└── lib/
    ├── arm64-v8a/
    └── armeabi-v7a/
```

**⚠️ Không có:**
- `META-INF/MANIFEST.MF` - File manifest chứa hash của từng file
- `META-INF/CERT.SF` - Signature file
- `META-INF/CERT.RSA` hoặc `CERT.EC` - Certificate + signature

**Status:** Không thể install trên Android device (Android yêu cầu APK phải được sign)

### 🔒 Signed APK Structure

```
app-release.apk
├── AndroidManifest.xml
├── classes.dex
├── classes2.dex
├── res/
├── resources.arsc
├── assets/
├── lib/
└── META-INF/              ⬅️ SIGNATURE FILES
    ├── MANIFEST.MF        ⬅️ SHA-256 hash của mỗi file
    ├── CERT.SF            ⬅️ Signature của MANIFEST.MF
    └── CERT.RSA           ⬅️ Developer's certificate + public key
```

**META-INF/MANIFEST.MF example:**
```
Manifest-Version: 1.0
Created-By: 1.0 (Android)

Name: AndroidManifest.xml
SHA-256-Digest: xQ7B2K8vF3nP9wE5rT6yU8iO0pA1sD2fG3hJ4kL5zX=

Name: classes.dex
SHA-256-Digest: mN4bV5cX6dZ7eY8fW9gX0hY1iZ2jA3kB4lC5mD6nE7=
```

**CERT.RSA contains:**
- Developer's X.509 certificate
- Certificate chain
- Signature của CERT.SF file
- Public key để verify signature

---

## 3. Khác biệt khi Decompile

### Decompiling Process

```powershell
# Decompile bằng JADX
jadx app.apk -d output_folder
```

**Kết quả Decompile:**

| Aspect | Unsigned APK | Signed APK |
|--------|-------------|-----------|
| **Decompile Success** | ✅ Yes | ✅ Yes |
| **Source Code** | ✅ Giống nhau | ✅ Giống nhau |
| **Resources** | ✅ Giống nhau | ✅ Giống nhau |
| **Algorithm Logic** | ✅ Visible | ✅ Visible |
| **META-INF/*** | ❌ Không có | ✅ Có signature files |

**💡 Kết luận:** 
- **Decompile code thì GIỐNG NHAU**
- Signed hay Unsigned **KHÔNG ẢNH HƯỞNG** đến khả năng decompile code
- Chỉ khác ở việc có thêm folder `META-INF/` với signature files

### Extract Signature Information

**Với Signed APK, attacker có thể extract:**

```powershell
# Extract certificate từ signed APK
keytool -printcert -jarfile app-release.apk

# Output:
Signer #1:

Signature:

Owner: CN=YourName, OU=YourCompany, O=YourOrg, L=City, ST=State, C=VN
Issuer: CN=YourName, OU=YourCompany, O=YourOrg, L=City, ST=State, C=VN
Serial number: 1a2b3c4d
Valid from: Mon Jan 01 00:00:00 ICT 2024 until: Fri Dec 27 00:00:00 ICT 2049
Certificate fingerprints:
   SHA1: A1:B2:C3:D4:E5:F6:...
   SHA256: EC:0E:5E:7D:C8:F3:B2:9B:F1:4F:CA:E4:D1:E9:01:05:...
```

**⚠️ Thông tin này rất quan trọng:**
- Attacker biết được **SHA-256 fingerprint** → Có thể so sánh với expected signature trong code
- Biết certificate owner info → Có thể fake certificate với cùng owner name

---

## 4. Khác biệt khi Repackage

### 🎯 Scenario: Modify Code và Repackage APK

**Attack Flow:**

```
1. Decompile APK
2. Modify code (e.g., bypass login)
3. Repackage APK
4. Sign APK (với key của attacker)
5. Install
```

### Unsigned APK

```powershell
# 1. Decompile
apktool d app-debug-unsigned.apk -o app_modified

# 2. Modify code
notepad app_modified\smali\com\example\eduquizz\LoginActivity.smali
# Change: return v0 -> return v1 (bypass login)

# 3. Repackage
apktool b app_modified -o app-repackaged.apk

# 4. Try to install
adb install app-repackaged.apk
```

**❌ Kết quả:**
```
Failure [INSTALL_PARSE_FAILED_NO_CERTIFICATES]
```

**Lý do:** Android **KHÔNG CHO PHÉP** install unsigned APK
- Bắt buộc APK phải có signature
- Attacker **PHẢI SIGN** APK trước khi install

### Signed APK

```powershell
# 1. Decompile
apktool d app-release.apk -o app_modified

# 2. Modify code
# (same as above)

# 3. Repackage (META-INF bị mất)
apktool b app_modified -o app-repackaged.apk

# 4. Sign với attacker's key
keytool -genkey -v -keystore attacker.jks -keyalg RSA -validity 10000
jarsigner -keystore attacker.jks app-repackaged.apk attacker_key

# 5. Install
adb install app-repackaged.apk
```

**✅ Installation thành công!**

**Nhưng...**

#### 🔴 Case 1: APK KHÔNG CÓ Signature Verification Code

```kotlin
// MainActivity.kt - KHÔNG CÓ check

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // No signature verification
    
    setContent { ... }
}
```

**Kết quả:**
- ✅ App install thành công
- ✅ App chạy bình thường
- ✅ Modified code hoạt động (bypass login successful)
- ❌ **ATTACKER THẮNG!**

#### 🟢 Case 2: APK CÓ Signature Verification Code

```kotlin
// MainActivity.kt - CÓ check

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // ✅ SIGNATURE VERIFICATION
    if (!SignatureUtils.verifyAppSignature(this)) {
        Toast.makeText(this, "App đã bị chỉnh sửa!", Toast.LENGTH_LONG).show()
        finish()
        return
    }
    
    setContent { ... }
}
```

**SignatureUtils.kt:**
```kotlin
object SignatureUtils {
    fun verifyAppSignature(context: Context): Boolean {
        // Expected signature của developer (original)
        val expected = "EC:0E:5E:7D:C8:F3:B2:9B:F1:4F:CA:E4:..."
        
        // Get actual signature của installed APK
        val actual = getActualSignature(context)
        
        return actual == expected
    }
}
```

**Kết quả:**
- ✅ App install thành công
- ✅ App launch
- ❌ Signature mismatch detected
- ❌ App shows "App đã bị chỉnh sửa!" 
- ❌ App calls `finish()` and exits
- ✅ **DEFENDER THẮNG!**

**Log:**
```
D/SIGNATURE: Original: EC:0E:5E:7D:C8:F3:B2:9B:...
D/SIGNATURE: Actual:   A1:B2:C3:D4:E5:F6:G7:H8:...  (attacker's signature)
E/Security: Signature verification FAILED
```

---

## 5. Khác biệt khi Runtime

### Unsigned APK

**⚠️ KHÔNG THỂ CHẠY**

```
Không thể install unsigned APK trên Android device
→ Không có runtime scenario
```

### Signed APK - Original Signature

**✅ Expected Behavior:**

```kotlin
onCreate() {
    // Security checks
    SignatureUtils.verifyAppSignature(this)  // ✅ PASS (match)
    PlayIntegrityHelper.checkIntegrity(this) // ✅ PASS (PLAY_RECOGNIZED)
    
    // App continues normally
}
```

### Signed APK - Modified (Attacker's Signature)

**❌ Security Violation Detected:**

```kotlin
onCreate() {
    // Security check 1: Signature
    SignatureUtils.verifyAppSignature(this)  // ❌ FAIL (mismatch)
    → Toast "App đã bị chỉnh sửa!"
    → finish()
    → return
    
    // Security check 2: Play Integrity (nếu chạy được đến đây)
    PlayIntegrityHelper.checkIntegrity(this) 
    → verdict: "UNEVALUATED" hoặc "TAMPERING_DETECTED"
    → ❌ FAIL
    → finish()
}
```

---

## 6. Attack Scenarios So sánh

### Scenario 1: Decompile và Đọc Code

| Step | Unsigned APK | Signed APK | Khác biệt |
|------|-------------|-----------|-----------|
| Decompile | ✅ Success | ✅ Success | ❌ KHÔNG KHÁC |
| Đọc source code | ✅ Success | ✅ Success | ❌ KHÔNG KHÁC |
| Tìm API URLs | ✅ Found | ✅ Found | ❌ KHÔNG KHÁC |
| Hiểu logic | ✅ Success | ✅ Success | ❌ KHÔNG KHÁC |

**💡 Kết luận:** Signing **KHÔNG BẢO VỆ** khỏi decompilation

---

### Scenario 2: Modify Code và Repackage

| Step | Unsigned APK | Signed APK (No Verification) | Signed APK (With Verification) |
|------|-------------|------------------------------|-------------------------------|
| Decompile | ✅ | ✅ | ✅ |
| Modify code | ✅ | ✅ | ✅ |
| Repackage | ✅ | ✅ | ✅ |
| Sign với attacker key | ✅ Required | ✅ Required | ✅ Required |
| Install | ❌ Fails (no cert) | ✅ Success | ✅ Success |
| Run | N/A | ✅ Works! | ❌ Detects & exits |
| Attack success? | ❌ NO | ✅ YES | ❌ NO |

**💡 Kết luận:** 
- Unsigned APK: Không thể install
- Signed (no check): Attacker thắng
- Signed (with check): **Defender thắng** ✅

---

### Scenario 3: Bypass Signature Check

**Attacker's Goal:** Chạy modified APK với signature check

**Attack Options:**

#### Option 1: Remove Signature Check Code

```smali
// Original code in MainActivity.smali
invoke-static {p0}, Lcom/example/eduquizz/security/SignatureUtils;->verifyAppSignature(...)Z
move-result v0
if-nez v0, :cond_0    # if signature fails, go to :cond_0
invoke-virtual {p0}, finish()
return-void
:cond_0  # continue

// ⚠️ Attacker modifies:
# Remove the signature check
# invoke-static {p0}, Lcom/example/eduquizz/security/SignatureUtils;->verifyAppSignature(...)Z
# move-result v0
# if-nez v0, :cond_0
# invoke-virtual {p0}, finish()
# return-void
:cond_0  # continue
```

**Kết quả:**
- ✅ Modified code bypasses check
- ✅ App runs
- **❌ Nhưng:** Signature STILL KHÁC
- **⚠️ Nếu có Play Integrity:** Vẫn bị detect bởi Google

#### Option 2: Patch Expected Signature

```kotlin
// Original SignatureUtils.kt (decompiled to Java)
public class SignatureUtils {
    public static boolean verifyAppSignature(Context context) {
        String expected = "EC:0E:5E:7D:C8:F3:...";  // Original
        String actual = getSignature(context);
        return actual.equals(expected);
    }
}

// ⚠️ Attacker changes expected to their signature
String expected = "A1:B2:C3:D4:E5:F6:...";  // Attacker's signature
```

**Kết quả:**
- ✅ Check passes (vì expected == actual)
- ✅ App runs
- **⚠️ Nhưng:** Attacker phải decompile, modify, và recompile
- **✅ Defense:** Encrypt expected signature → Khó modify hơn

#### Option 3: Hook Runtime

```javascript
// Frida script để hook signature verification
Java.perform(() => {
    const SignatureUtils = Java.use('com.example.eduquizz.security.SignatureUtils');
    
    SignatureUtils.verifyAppSignature.implementation = function(context) {
        console.log('[+] Signature check bypassed');
        return true;  // Always return true
    };
});
```

**Kết quả:**
- ✅ Bypass signature check
- ✅ App runs với modified code
- **✅ Defense:** Anti-debugging + Frida detection

---

## 7. Summary Table: Signed vs Unsigned APK

| Khía cạnh | Unsigned APK | Signed APK (Basic) | Signed APK (với Verification Code) |
|-----------|-------------|-------------------|-----------------------------------|
| **Decompile** | ✅ Dễ | ✅ Dễ | ✅ Dễ |
| **Đọc code** | ✅ Dễ | ✅ Dễ | ✅ Dễ (trừ khi obfuscated) |
| **Modify code** | ✅ Dễ | ✅ Dễ | ✅ Dễ |
| **Repackage** | ✅ Dễ | ✅ Dễ | ✅ Dễ |
| **Install** | ❌ Không thể | ✅ Được (với attacker signature) | ✅ Được (với attacker signature) |
| **Run modified** | ❌ N/A | ✅ Chạy bình thường | ❌ Detect & exit |
| **Attack success** | ❌ NO | ✅ YES | ❌ NO (nếu có thêm Play Integrity) |
| **Defense level** | None | ⭐ Weak | ⭐⭐⭐⭐ Strong |

---

## 8. Kết luận: Tại sao cần Signature Verification?

### ❌ Signing ALONE không đủ

```
APK được sign ≠ APK an toàn khỏi modification
```

**Lý do:**
- Attacker có thể sign lại với key của họ
- Android chỉ verify "APK có được sign không", không verify "đúng người sign không"

### ✅ Signing + Verification Code = Security

```
APK được sign + Code verify signature = Phát hiện modification
```

**Flow:**
1. Developer sign APK với key A → Signature hash X
2. Developer hardcode "expected = X" trong code
3. Attacker modify code → Phải sign lại với key B → Signature hash Y
4. App runtime: Check actual signature Y ≠ expected X → Detect tampering

### 🔒 Best Practice: Multiple Layers

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Layer 1: Signature Verification
    if (!SignatureUtils.verifyAppSignature(this)) {
        finish(); return
    }
    
    // Layer 2: Play Integrity API
    PlayIntegrityHelper.checkIntegrity(this) { passed, _ ->
        if (!passed) { finish() }
    }
    
    // Layer 3: Anti-Debugging
    if (Debug.isDebuggerConnected()) {
        Process.killProcess(Process.myPid())
    }
    
    // Layer 4: Root Detection
    if (RootDetection.isRooted()) {
        finish()
    }
}
```

---

## 9. Quick Reference

### Attacker muốn chạy Modified APK:

**Unsigned APK:**
```
❌ Không thể install → GAME OVER
```

**Signed APK (no verification):**
```
✅ Modify → Sign lại → Install → RUN SUCCESSFULLY
🎯 Attacker WINs
```

**Signed APK (with verification):**
```
✅ Modify → Sign lại → Install → Launch
❌ Signature mismatch → App exits
🎯 Defender WINs (unless attacker bypasses verification code)
```

**Signed APK (with verification + obfuscation + Play Integrity):**
```
✅ Modify → Sign lại → Install → Launch
❌ Multiple defenses:
   - Signature mismatch
   - Play Integrity failed
   - Code obfuscated (hard to bypass)
   - Anti-debugging active
🎯 Defender WINs STRONGLY 💪
```

---

**💡 Takeaway:**

1. **Signing** = Yêu cầu bắt buộc của Android, KHÔNG BẢO VỆ khỏi modification
2. **Signature Verification Code** = Phát hiện modification, BẢO VỆ app
3. **Multiple layers** (Obfuscation + Integrity + Anti-debug) = Security tốt nhất

**Signed APK khác Unsigned APK khi bị tấn công:**
- Khác ở **runtime protection** (nếu có verification code)
- **KHÔNG KHÁC** ở decompilation và static analysis
