# 🔐 Setup APK Signing Configuration
## Hướng dẫn cấu hình ký APK tự động

## Hiện trạng

✅ **Có keystore file:** `d:\Android\EduQuizzApp_v1\keystore\my-release-key`
❌ **Chưa có signing config** trong `build.gradle.kts`

---

## Option 1: Thêm Signing Config vào build.gradle.kts

### Bước 1: Tạo file keystore.properties

Tạo file `keystore.properties` ở thư mục root project:

```properties
# File: d:\Android\EduQuizzApp_v1\keystore.properties

storeFile=keystore/my-release-key
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=upload
keyPassword=YOUR_KEY_PASSWORD
```

**⚠️ Lưu ý:** 
- Thay `YOUR_KEYSTORE_PASSWORD` và `YOUR_KEY_PASSWORD` bằng password thực tế
- **KHÔNG commit file này lên Git!** (Thêm vào `.gitignore`)

### Bước 2: Update build.gradle.kts

Thêm code sau vào `app/build.gradle.kts`:

```kotlin
import java.util.Properties
import java.io.FileInputStream

// ... existing plugins ...

android {
    namespace = "com.example.eduquizz"
    compileSdk = 35
    
    // ✅ THÊM: Load keystore properties
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    defaultConfig {
        // ... existing config ...
    }
    
    // ✅ THÊM: Signing Configs
    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // ⚠️ Nên đổi thành true cho security
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // ✅ THÊM: Use signing config
            signingConfig = signingConfigs.getByName("release")
        }
        
        debug {
            // Debug builds sử dụng default debug keystore
        }
    }
    
    // ... rest of config ...
}
```

### Bước 3: Update .gitignore

Thêm vào `.gitignore`:

```gitignore
# Keystore files
keystore.properties
*.jks
*.keystore
keystore/
```

---

## Option 2: Ký thủ công sau khi build

Nếu không muốn config tự động, bạn có thể ký manually:

### Build unsigned APK

```powershell
cd d:\Android\EduQuizzApp_v1
.\gradlew assembleRelease
```

APK sẽ ở: `app\build\outputs\apk\release\app-release-unsigned.apk`

### Ký APK thủ công

```powershell
# Align APK trước
zipalign -v -p 4 app-release-unsigned.apk app-release-unsigned-aligned.apk

# Ký APK
apksigner sign --ks keystore\my-release-key `
    --ks-key-alias upload `
    --out app-release-signed.apk `
    app-release-unsigned-aligned.apk

# Verify signature
apksigner verify --verbose app-release-signed.apk
```

---

## Kiểm tra keystore hiện tại

Để xem thông tin keystore:

```powershell
keytool -list -v -keystore d:\Android\EduQuizzApp_v1\keystore\my-release-key
# Nhập password khi được hỏi
```

Output sẽ cho bạn:
- Alias name
- Certificate fingerprint (SHA-256)
- Validity dates
- Owner information

---

## Lấy SHA-256 Signature để update SignatureUtils

Sau khi ký APK, lấy signature hash:

```powershell
# Method 1: Từ keystore
keytool -list -v -keystore keystore\my-release-key -alias upload

# Output:
Certificate fingerprints:
     SHA1: A1:B2:C3:D4:E5:F6:...
     SHA256: EC:0E:5E:7D:C8:F3:B2:9B:F1:4F:CA:E4:D1:E9:01:05:...

# Method 2: Từ signed APK
keytool -printcert -jarfile app-release-signed.apk
```

Copy SHA-256 hash và update trong `SignatureUtils.kt`:

```kotlin
// File: security/SignatureUtils.kt
object SignatureUtils {
    fun verifyAppSignature(context: Context): Boolean {
        // ✅ UPDATE hash này với SHA-256 từ keystore của bạn
        val expected = "EC:0E:5E:7D:C8:F3:B2:9B:F1:4F:CA:E4:D1:E9:01:05:..."
        
        // ... rest of code ...
    }
}
```

---

## Recommendation

**Tôi khuyên bạn dùng Option 1** (Auto signing) vì:
- ✅ Tự động ký mỗi lần build
- ✅ Không quên ký APK
- ✅ Consistent signature
- ✅ Dễ integrate với CI/CD

---

## Bạn muốn tôi làm gì tiếp?

1. ✅ **Tự động update build.gradle.kts** với signing config?
2. ✅ **Tạo keystore.properties template**?
3. ✅ **Update .gitignore**?
4. ✅ **Kiểm tra keystore hiện tại** và lấy SHA-256?

Cho tôi biết nhé!
