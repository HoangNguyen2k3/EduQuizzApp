# 🔍 Attack Vector 1: APK Decompilation & Source Code Analysis
## Hướng dẫn thực hành chi tiết từng bước

> **🎯 Mục tiêu:** Học cách decompile APK và phân tích source code để tìm thông tin nhạy cảm trong EduQuizzApp

---

## 📚 Mục lục

1. [Chuẩn bị môi trường](#phần-1-chuẩn-bị-môi-trường)
2. [Lấy APK file](#phần-2-lấy-apk-file)
3. [Phương pháp 1: Decompile với APKTool (Smali)](#phần-3-phương-pháp-1-apktool)
4. [Phương pháp 2: Decompile với JADX (Java)](#phần-4-phương-pháp-2-jadx)
5. [Phân tích và tìm thông tin nhạy cảm](#phần-5-phân-tích-source-code)
6. [Kết quả và đánh giá](#phần-6-kết-quả)

---

## Phần 1: Chuẩn bị môi trường

### A. Cài đặt Java Development Kit (JDK)

**Windows:**
```powershell
# Download JDK 17 từ Oracle hoặc AdoptOpenJDK
# https://adoptium.net/

# Kiểm tra installation
java -version
# Output: java version "17.x.x"
```

**Thiết lập biến môi trường:**
```powershell
# Mở System Environment Variables
# Thêm JAVA_HOME = C:\Program Files\Java\jdk-17
# Thêm vào PATH: %JAVA_HOME%\bin
```

### B. Cài đặt Android Debug Bridge (ADB)

**Option 1: Qua Android Studio**
- Cài Android Studio
- ADB tự động có sẵn tại: `C:\Users\<YourUser>\AppData\Local\Android\Sdk\platform-tools\`

**Option 2: Standalone ADB**
```powershell
# Download từ: https://developer.android.com/studio/releases/platform-tools
# Extract và thêm vào PATH

# Kiểm tra
adb version
# Output: Android Debug Bridge version x.x.x
```

### C. Cài đặt APKTool

**Download:**
```powershell
# Tải từ: https://ibotpeaches.github.io/Apktool/

# Tạo folder: C:\apktool
# Download 2 files:
# 1. apktool.bat
# 2. apktool.jar (đổi tên thành apktool.jar)

# Thêm C:\apktool vào PATH
```

**Kiểm tra:**
```powershell
apktool --version
# Output: 2.x.x
```

### D. Cài đặt JADX

**Download:**
```powershell
# Tải từ: https://github.com/skylot/jadx/releases
# Download: jadx-gui-x.x.x.zip

# Extract vào: C:\jadx
# Thêm C:\jadx\bin vào PATH
```

**Kiểm tra:**
```powershell
# Chạy JADX GUI
C:\jadx\bin\jadx-gui.bat
```

### E. Cài đặt công cụ bổ sung

**1. dex2jar (Optional):**
```powershell
# Download: https://github.com/pxb1988/dex2jar/releases
# Extract vào C:\dex2jar
```

**2. JD-GUI (Optional):**
```powershell
# Download: http://java-decompiler.github.io/
# File .exe có thể chạy trực tiếp
```

---

## Phần 2: Lấy APK file

### Method 1: Build APK từ source code (Recommended)

**Bước 1: Build Release APK**
```powershell
# Di chuyển vào project folder
cd d:\Android\EduQuizzApp_v1

# Build APK
.\gradlew assembleRelease

# Hoặc qua Android Studio:
# Build > Build Bundle(s) / APK(s) > Build APK(s)
```

**Bước 2: Tìm APK**
```powershell
# APK sẽ nằm tại:
cd d:\Android\EduQuizzApp_v1\app\build\outputs\apk\release\

# File: app-release-unsigned.apk hoặc app-release.apk
dir
```

**Bước 3: Copy APK ra folder làm việc**
```powershell
# Tạo working directory
mkdir d:\APK_Analysis
cd d:\APK_Analysis

# Copy APK
copy d:\Android\EduQuizzApp_v1\app\build\outputs\apk\release\app-release.apk eduquizz.apk
```

### Method 2: Lấy APK từ Device/Emulator

**Bước 1: Start emulator hoặc connect device**
```powershell
# Kiểm tra device
adb devices
# Output:
# List of devices attached
# emulator-5554   device
```

**Bước 2: Tìm package name**
```powershell
# List tất cả packages
adb shell pm list packages | findstr eduquizz
# Output: package:com.example.eduquizz
```

**Bước 3: Lấy APK path**
```powershell
adb shell pm path com.example.eduquizz
# Output: package:/data/app/~~xxxxxx==/com.example.eduquizz-xxxxxx==/base.apk
```

**Bước 4: Pull APK**
```powershell
# Pull về máy
adb pull /data/app/~~xxxxxx==/com.example.eduquizz-xxxxxx==/base.apk eduquizz.apk

# Hoặc sử dụng script tự động:
adb shell pm path com.example.eduquizz | findstr /C:"package:" > temp.txt
FOR /F "tokens=2 delims=:" %%i IN (temp.txt) DO adb pull %%i eduquizz.apk
del temp.txt
```

---

## Phần 3: Phương pháp 1 - APKTool

### 🎯 Mục đích: Decompile APK về Smali bytecode

> **Smali** là assembly language cho Dalvik VM (Android). Khó đọc hơn Java nhưng giữ nguyên cấu trúc bytecode.

### Bước 1: Decompile APK

```powershell
cd d:\APK_Analysis

# Decompile
apktool d eduquizz.apk -o eduquizz_smali

# Options:
# -d : decode
# -o : output folder
# -f : force (nếu folder đã tồn tại)
```

**Output:**
```
I: Using Apktool 2.x.x
I: Loading resource table...
I: Decoding AndroidManifest.xml with resources...
I: Loading resource table from file: C:\Users\...\apktool\framework\1.apk
I: Regular manifest package...
I: Decoding file-resources...
I: Decoding values */* XMLs...
I: Baksmaling classes.dex...
I: Baksmaling classes2.dex...
I: Copying assets and libs...
I: Copying unknown files...
I: Copying original files...
```

### Bước 2: Khám phá cấu trúc APK

```powershell
cd eduquizz_smali
dir

# Cấu trúc:
# ├── AndroidManifest.xml       # App manifest
# ├── apktool.yml               # APKTool metadata
# ├── original/                 # Original files (CERT, MANIFEST)
# ├── res/                      # Resources (layouts, drawables, values)
# ├── smali/                    # Smali code (main)
# ├── smali_classes2/           # Additional smali files
# ├── assets/                   # Raw assets
# └── lib/                      # Native libraries (.so)
```

### Bước 3: Xem AndroidManifest.xml

```powershell
notepad AndroidManifest.xml

# Hoặc
type AndroidManifest.xml
```

**Thông tin quan trọng trong Manifest:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.eduquizz"
    android:versionCode="1"
    android:versionName="1.0">
    
    <!-- Permissions - Quyền app yêu cầu -->
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    
    <!-- Activities - Entry points -->
    <application
        android:name="com.example.eduquizz.EduQuizzApp"
        android:allowBackup="true"
        android:debuggable="false"  <!-- ✅ Nên là false cho release -->
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name">
        
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
    </application>
</manifest>
```

**Red flags cần chú ý:**
- `android:debuggable="true"` - Cho phép debug (nguy hiểm)
- `android:allowBackup="true"` - Data có thể backup được
- Permissions quá nhiều không cần thiết

### Bước 4: Khám phá Smali code

```powershell
cd smali\com\example\eduquizz

# Xem cấu trúc packages
tree /F
```

**Ví dụ cấu trúc:**
```
smali\com\example\eduquizz\
├── MainActivity.smali
├── EduQuizzApp.smali
├── security\
│   ├── SignatureUtils.smali
│   ├── PlayIntegrityHelper.smali
│   └── TokenManager.smali
├── network\
│   ├── ApiService.smali
│   ├── RetrofitClient.smali
│   └── JwtAuthInterceptor.smali
└── data\
    └── SecurePreferencesManager.smali
```

### Bước 5: Đọc Smali code (Ví dụ)

```powershell
notepad security\SignatureUtils.smali
```

**Smali code mẫu:**
```smali
.class public Lcom/example/eduquizz/security/SignatureUtils;
.super Ljava/lang/Object;

# Method: verifyAppSignature
.method public static verifyAppSignature(Landroid/content/Context;)Z
    .locals 3
    
    # const-string v0, "EC:0E:5E:..." - Expected signature
    const-string v0, "EC:0E:5E:7D:C8:F3:B2:9B:F1:4F:CA:E4:D1:E9:01:05:4A:23:CD:F2:C5:69:D1:FE:20:E9:9E:C3:40:D4:50:57"
    
    # ... signature verification logic ...
    
    # return v1 (true/false)
    return v1
.end method
```

**Cách đọc Smali:**
- `.method` - Định nghĩa method
- `.locals 3` - 3 local variables (v0, v1, v2)
- `const-string v0, "..."` - Gán string vào v0
- `return v1` - Return giá trị v1

**🔍 Red flag:** Hardcoded expected signature string!

### Bước 6: Xem Resources

```powershell
# Xem strings
notepad res\values\strings.xml

# Xem network security config
notepad res\xml\network_security_config.xml

# Xem layouts
notepad res\layout\activity_main.xml
```

**Ví dụ strings.xml:**
```xml
<resources>
    <string name="app_name">EduQuizz</string>
    <string name="api_base_url">http://10.0.2.2:8080/api/</string>
    <string name="api_key">HARDCODED_API_KEY_123</string>  <!-- ⚠️ Nguy hiểm! -->
</resources>
```

---

## Phần 4: Phương pháp 2 - JADX (Recommended)

### 🎯 Mục đích: Decompile APK về Java source code dễ đọc

### Bước 1: Decompile bằng JADX GUI

```powershell
# Mở JADX GUI
C:\jadx\bin\jadx-gui.bat

# Hoặc nếu đã thêm vào PATH:
jadx-gui
```

**Trong JADX GUI:**
1. File > Open file > Chọn `eduquizz.apk`
2. Đợi decompile (1-2 phút)
3. Source code sẽ hiển thị dạng Java

### Bước 2: Decompile bằng JADX CLI (để save ra file)

```powershell
cd d:\APK_Analysis

# Decompile và save
jadx eduquizz.apk -d eduquizz_java

# Options:
# -d : output directory
# --no-res : không decompile resources (nhanh hơn)
# --show-bad-code : show code có lỗi
```

**Output:**
```
INFO  - loading ...
INFO  - processing ...
INFO  - done
```

### Bước 3: Khám phá Java source code

```powershell
cd eduquizz_java\sources\com\example\eduquizz

# Xem cấu trúc
tree /F

# Output:
# ├── MainActivity.java
# ├── security\
# │   ├── SignatureUtils.java
# │   ├── PlayIntegrityHelper.java
# │   └── TokenManager.java
# ├── network\
# │   ├── ApiService.java
# │   └── RetrofitClient.java
# └── ...
```

### Bước 4: Đọc decompiled Java code

**Mở SignatureUtils.java:**
```powershell
notepad security\SignatureUtils.java
```

**Decompiled Java code mẫu:**
```java
package com.example.eduquizz.security;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.security.MessageDigest;

public class SignatureUtils {
    
    public static boolean verifyAppSignature(Context context) {
        // ⚠️ HARDCODED EXPECTED SIGNATURE
        String expected = "EC:0E:5E:7D:C8:F3:B2:9B:F1:4F:CA:E4:D1:E9:01:05:4A:23:CD:F2:C5:69:D1:FE:20:E9:9E:C3:40:D4:50:57";
        
        try {
            PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            
            // Get signature
            Signature[] signatures = packageInfo.signatures;
            if (signatures == null || signatures.length == 0) {
                return false;
            }
            
            // Calculate SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String hex = bytesToHex(md.digest(signatures[0].toByteArray()));
            
            Log.d("SIGNATURE", hex);
            
            // Compare
            return hex.equals(expected);
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X:", b));
        }
        return sb.toString().replaceFirst(":$", "");
    }
}
```

**💡 Phân tích:**
- ✅ Code rất dễ đọc so với Smali
- ⚠️ Expected signature bị hardcode
- ⚠️ Log.d() có thể leak signature trong debug
- 🎯 Attacker biết cách bypass: Force return true

---

## Phần 5: Phân tích Source Code

### 🎯 Mục tiêu: Tìm thông tin nhạy cảm trong EduQuizzApp

### A. Tìm API Endpoints và Base URLs

**Method 1: Sử dụng grep/findstr**

```powershell
cd eduquizz_java\sources

# Tìm HTTP URLs
findstr /s /i "http://" *.java
findstr /s /i "https://" *.java

# Tìm BASE_URL constants
findstr /s /i "BASE_URL" *.java
findstr /s /i "baseUrl" *.java
```

**Method 2: Sử dụng JADX GUI Search (Recommended)**

1. Mở JADX GUI
2. Nhấn `Ctrl + Shift + F` (Search)
3. Search: `"http://"`
4. Results sẽ show tất cả files chứa HTTP URLs

**Kết quả mong đợi cho EduQuizzApp:**

```java
// File: network/RetrofitClient.java
public class RetrofitClient {
    // ⚠️ EXPOSED API BASE URL
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    
    public static Retrofit getClient() {
        return new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    }
}
```

**🎯 Thông tin thu được:**
- API Base URL: `http://10.0.2.2:8080/api/`
- Biết server đang dùng `10.0.2.2` (emulator localhost)
- Không có HTTPS → có thể MITM dễ dàng

### B. Tìm API Endpoints

```powershell
# Tìm ApiService interface
cd eduquizz_java\sources\com\example\eduquizz\network

notepad ApiService.java
```

**Decompiled ApiService.java:**
```java
package com.example.eduquizz.network;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    
    // ✅ LOGIN ENDPOINT
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    
    // ✅ REGISTER ENDPOINT
    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
    
    // ✅ PROFILE ENDPOINT
    @GET("auth/profile/{id}")
    Call<UserProfile> getProfile(@Path("id") int userId);
    
    // ✅ UPDATE PROFILE
    @PUT("auth/profile/{id}")
    Call<UpdateResponse> updateProfile(
        @Path("id") int userId,
        @Body ProfileUpdateRequest request
    );
    
    // ⚠️ ADMIN ENDPOINT (nếu có)
    @GET("admin/users")
    Call<List<User>> getAllUsers(@Header("Authorization") String token);
}
```

**🎯 Thông tin thu được:**
- Tất cả API endpoints
- Request/Response models
- Authentication method (JWT trong header)
- Có thể test endpoints trực tiếp bằng Postman/curl

### C. Tìm Authentication Logic

```powershell
# Tìm files liên quan JWT
findstr /s /i "jwt" *.java
findstr /s /i "token" *.java
findstr /s /i "Authorization" *.java
```

**Decompiled JwtAuthInterceptor.java:**
```java
package com.example.eduquizz.network;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class JwtAuthInterceptor implements Interceptor {
    
    private TokenManager tokenManager;
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        
        // ✅ Get token from TokenManager
        String token = tokenManager.getToken();
        
        if (token != null) {
            // ✅ Add Authorization header
            Request authenticated = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
            return chain.proceed(authenticated);
        }
        
        return chain.proceed(original);
    }
}
```

**🎯 Thông tin thu được:**
- Token được lưu bởi `TokenManager`
- Format: `Bearer <token>`
- Có thể extract token từ SharedPreferences

### D. Tìm Encryption/Decryption Logic

```powershell
# Tìm encryption code
findstr /s /i "encrypt" *.java
findstr /s /i "decrypt" *.java
findstr /s /i "AES" *.java
findstr /s /i "cipher" *.java
```

**Nếu tìm thấy:**
```java
// Example: CustomEncryption.java
public class CustomEncryption {
    
    // ⚠️ HARDCODED ENCRYPTION KEY
    private static final String SECRET_KEY = "MyHardcodedKey123";
    
    public static String encrypt(String data) {
        // AES encryption logic
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // ...
    }
}
```

**🎯 Red flags:**
- Hardcoded encryption keys
- Weak encryption algorithms (DES instead of AES)
- No IV (Initialization Vector)

### E. Tìm Hardcoded Credentials/Secrets

```powershell
# Search for common secret patterns
findstr /s /i "password" *.java
findstr /s /i "api_key" *.java
findstr /s /i "secret" *.java
findstr /s /i "credential" *.java
```

**Red flags:**
```java
// Bad practice examples
public class Config {
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String API_KEY = "sk_live_1234567890abcdef";
    public static final String DATABASE_PASSWORD = "mydbpass";
    public static final String GOOGLE_MAPS_API_KEY = "AIzaSyXXXXXXXXXXXXXXXXXXX";
}
```

### F. Tìm SharedPreferences Keys

```powershell
# Tìm preference keys
findstr /s /i "SharedPreferences" *.java
findstr /s /i "putString" *.java
findstr /s /i "getString" *.java
```

**Ví dụ:**
```java
// SecurePreferencesManager.java
public class SecurePreferencesManager {
    
    // ⚠️ EXPOSED PREFERENCE KEYS
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_TOKEN = "user_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ID = "user_id";
    
    public void saveToken(String token) {
        preferences.edit()
            .putString(KEY_USER_TOKEN, token)
            .apply();
    }
}
```

**🎯 Attacker có thể:**
1. Biết tên SharedPreferences file: `user_prefs.xml`
2. Biết keys: `user_token`, `user_email`, `user_id`
3. Pull file từ device và đọc

### G. Phân tích Security Implementations

**Tìm security checks:**
```powershell
cd eduquizz_java\sources\com\example\eduquizz\security

dir /b
# Output:
# SignatureUtils.java
# PlayIntegrityHelper.java
# TokenManager.java
```

**Đọc từng file:**

**1. SignatureUtils.java** - Đã phân tích ở trên
- Expected signature hardcoded
- Có thể bypass bằng cách return true

**2. PlayIntegrityHelper.java:**
```java
public class PlayIntegrityHelper {
    
    private static final long PROJECT_NUMBER = 177486006662L;  // ⚠️ Exposed
    
    public static void checkIntegrity(Context context, OnResultCallback callback) {
        IntegrityManager manager = IntegrityManagerFactory.create(context);
        
        // Generate nonce
        byte[] nonce = new byte[32];
        new SecureRandom().nextBytes(nonce);
        
        // Request integrity token
        IntegrityTokenRequest request = IntegrityTokenRequest.builder()
            .setCloudProjectNumber(PROJECT_NUMBER)
            .setNonce(Base64.encodeToString(nonce, Base64.URL_SAFE))
            .build();
        
        manager.requestIntegrityToken(request)
            .addOnSuccessListener(response -> {
                // Verify response
                boolean passed = verifyPayload(response.token());
                callback.onResult(passed, response);
            });
    }
}
```

**🎯 Thông tin thu được:**
- Google Cloud Project Number
- Integrity check logic
- Có thể bypass nếu check bị comment out

---

## Phần 6: Kết quả và Đánh giá

### 📊 Tổng hợp thông tin nhạy cảm tìm được trong EduQuizzApp:

#### 1. **API Information** ✅
```
Base URL: http://10.0.2.2:8080/api/
Endpoints:
  - POST /auth/login
  - POST /auth/register
  - GET  /auth/profile/{id}
  - PUT  /auth/profile/{id}

Authentication: Bearer JWT Token
```

#### 2. **Security Mechanisms** ⚠️
```
- Signature Verification: Có nhưng bị comment out
  → Expected signature: EC:0E:5E:7D:C8:F3:B2:9B:...
  
- Play Integrity API: Có nhưng bị comment out
  → Project Number: 177486006662
  
- Anti-Debugging: Có nhưng bị comment out
  → Debug.isDebuggerConnected()
```

#### 3. **Data Storage** 📁
```
SharedPreferences:
  - File: user_prefs.xml
  - Keys: user_token, user_email, user_id
  - Encryption: EncryptedSharedPreferences (✅ Good)

Database:
  - Type: Room Database
  - Encryption: Không rõ (cần kiểm tra thêm)
```

#### 4. **Network Security** 🌐
```
- HTTPS: Không (đang dùng HTTP for localhost)
- Certificate Pinning: KHÔNG có
- SSL Pinning: KHÔNG có
→ Dễ bị MITM attack
```

#### 5. **Code Obfuscation** 🔒
```
ProGuard/R8: Có bật cho release build
  - minifyEnabled: true
  - shrinkResources: true
  - proguardFiles: proguard-rules.pro
  
⚠️ NHƯNG: JADX vẫn decompile được khá dễ đọc
```

---

### 🎯 Attack Scenarios có thể thực hiện:

#### Scenario 1: **API Endpoint Testing**
```bash
# Sử dụng curl để test endpoints
curl -X POST http://10.0.2.2:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123"}'
```

#### Scenario 2: **Extract JWT Token**
```bash
# Pull SharedPreferences
adb shell "run-as com.example.eduquizz cat /data/data/com.example.eduquizz/shared_prefs/__androidx_security_crypto_encrypted_prefs__.xml"

# Nếu có Frida:
frida -U -f com.example.eduquizz -l extract_token.js
```

#### Scenario 3: **Bypass Security Checks**
```smali
# Modify SignatureUtils.smali
.method public static verifyAppSignature()Z
    const/4 v0, 0x1  # Force return true
    return v0
.end method
```

#### Scenario 4: **Modify APK**
```bash
# Rebuild với logic modified
apktool b eduquizz_smali -o hacked.apk
jarsigner -keystore my.keystore hacked.apk alias
```

---

### 📝 Tạo Attack Report

**Tạo file báo cáo:**
```powershell
notepad attack_findings.txt
```

**Template báo cáo:**
```
===========================================
APK DECOMPILATION ANALYSIS REPORT
===========================================
Target: EduQuizzApp v1.0
Package: com.example.eduquizz
Analysis Date: 2025-12-26
Analyst: [Your Name]

===========================================
1. SUMMARY
===========================================
APK được decompile thành công bằng JADX.
Source code Java dễ đọc mặc dù có ProGuard.
Nhiều thông tin nhạy cảm bị expose.

===========================================
2. CRITICAL FINDINGS
===========================================

[CRITICAL] API Base URL Exposed
  Location: network/RetrofitClient.java
  Details: BASE_URL = "http://10.0.2.2:8080/api/"
  Impact: Attacker biết server endpoint
  
[HIGH] Hardcoded Expected Signature
  Location: security/SignatureUtils.java
  Details: Expected signature string hardcoded
  Impact: Bypass signature verification
  
[HIGH] Security Checks Disabled
  Location: MainActivity.java (line 57-76)
  Details: All security checks commented out
  Impact: No runtime protection
  
[MEDIUM] No SSL Pinning
  Impact: Vulnerable to MITM attacks

[MEDIUM] SharedPreferences Keys Exposed
  Location: data/SecurePreferencesManager.kt
  Impact: Attacker biết cách extract data

===========================================
3. RECOMMENDATIONS
===========================================

1. Bật lại tất cả security checks trong MainActivity
2. Không hardcode sensitive strings
3. Implement SSL Certificate Pinning
4. Thêm Root Detection
5. Sử dụng Native code (NDK) cho logic nhạy cảm
6. Strengthen ProGuard rules
7. Add runtime tampering detection

===========================================
4. PROOF OF CONCEPT
===========================================
[Attach screenshots, code snippets, scripts]

===========================================
```

---

### 🎬 Script tự động hóa phân tích

**Tạo PowerShell script:**
```powershell
# save as: analyze_apk.ps1

param(
    [Parameter(Mandatory=$true)]
    [string]$ApkPath
)

Write-Host "=== APK Analysis Script ===" -ForegroundColor Green

# 1. Decompile với JADX
Write-Host "`n[1] Decompiling với JADX..." -ForegroundColor Yellow
$outputDir = "analysis_output"
jadx $ApkPath -d $outputDir

# 2. Tìm API URLs
Write-Host "`n[2] Finding API URLs..." -ForegroundColor Yellow
Get-ChildItem -Path "$outputDir\sources" -Recurse -Filter "*.java" | 
    Select-String -Pattern "http://" -CaseSensitive:$false |
    Select-Object -Property Filename, LineNumber, Line |
    Format-Table -AutoSize

# 3. Tìm hardcoded secrets
Write-Host "`n[3] Finding hardcoded secrets..." -ForegroundColor Yellow
$patterns = @("password", "api_key", "secret", "token")
foreach ($pattern in $patterns) {
    Write-Host "  Searching for: $pattern" -ForegroundColor Cyan
    Get-ChildItem -Path "$outputDir\sources" -Recurse -Filter "*.java" |
        Select-String -Pattern $pattern -CaseSensitive:$false |
        Select-Object -First 5 -Property Filename, LineNumber
}

# 4. Extract permissions
Write-Host "`n[4] Extracting permissions..." -ForegroundColor Yellow
$manifest = Get-Content "$outputDir\resources\AndroidManifest.xml"
$manifest | Select-String -Pattern "uses-permission"

Write-Host "`n=== Analysis Complete ===" -ForegroundColor Green
Write-Host "Output saved to: $outputDir" -ForegroundColor Cyan
```

**Sử dụng:**
```powershell
.\analyze_apk.ps1 -ApkPath "eduquizz.apk"
```

---

## 🎓 Kết luận

### Kiến thức đã học:

✅ Cách setup môi trường decompile APK  
✅ Sử dụng APKTool để decompile về Smali  
✅ Sử dụng JADX để decompile về Java  
✅ Đọc và phân tích decompiled code  
✅ Tìm thông tin nhạy cảm trong source code  
✅ Tạo attack report chuyên nghiệp  
✅ Tự động hóa quá trình phân tích  

### Demo cho Giám Khảo:

**Timeline (10 phút):**

1. **[2 phút]** Show JADX decompile process
2. **[3 phút]** Navigate qua source code, show API endpoints
3. **[2 phút]** Show security checks bị comment out
4. **[2 phút]** Show hardcoded expected signature
5. **[1 phút]** Summarize findings và recommendations

### Key Messages:

> **"ProGuard obfuscation không đủ để bảo vệ app. Decompiled code vẫn readable."**

> **"Security checks đang bị disabled → App hoàn toàn không có runtime protection."**

> **"API endpoints và logic đều exposed → Dễ dàng reverse engineer."**

---

**Next Steps:** 
- [Attack Vector 2: Bypass Authentication](ATTACK_VECTOR_2_DETAILED_GUIDE.md)
- [Defense Implementation Guide](DEFENSE_IMPLEMENTATION_GUIDE.md)
