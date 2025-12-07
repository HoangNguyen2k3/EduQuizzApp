# 🚀 HƯỚNG DẪN CHẠY ADMIN PANEL

## ✅ Frontend đã được cấu hình xong!

Các thay đổi đã thực hiện:
- ✅ Uncomment tất cả admin API endpoints trong `ApiService.kt`
- ✅ Tắt mock data: `USE_MOCK_DATA = false` trong `AdminRepository.kt`
- ✅ BASE_URL đã cấu hình: `http://10.0.2.2:8080/` (cho Android Emulator)
- ✅ Logic auto-redirect admin đã có sẵn trong `NavGraph.kt`

---

## 📋 BƯỚC 1: Khởi động Backend

### 1.1 Clone Backend Repository
```bash
git clone https://github.com/cogdanh2k3/spring-boot-backend.git
cd spring-boot-backend
git checkout thong
```

### 1.2 Khởi động PostgreSQL Database
```bash
# Nếu dùng Docker Compose
docker-compose up -d

# Hoặc đảm bảo PostgreSQL đang chạy trên localhost:5332
```

### 1.3 Chạy Spring Boot Backend
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

Backend sẽ chạy tại: **http://localhost:8080**

---

## 📋 BƯỚC 2: Tạo Tài Khoản Admin

### Cách 1: Chạy SQL Script (Khuyên dùng)

1. Mở **pgAdmin** hoặc **DBeaver** kết nối đến database
2. Chạy file `CREATE_ADMIN_USER.sql` trong thư mục project này
3. Script sẽ tạo admin user với thông tin:
   - **Username**: `admin`
   - **Password**: `Admin@123`
   - **Role**: `ADMIN`

### Cách 2: Cập nhật User Hiện Có

```sql
-- Thay 'your_username' bằng username của bạn
UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
```

### Cách 3: Đăng ký User mới rồi update Role

1. Chạy app Android → Đăng ký tài khoản mới
2. Vào database chạy SQL:
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE username = 'tên_user_vừa_đăng_ký';
   ```

---

## 📋 BƯỚC 3: Build và Chạy Android App

### 3.1 Clean và Rebuild
```bash
# Windows PowerShell
.\gradlew clean
.\gradlew build
```

### 3.2 Run trên Emulator hoặc Device

**Cách 1: Android Studio**
- Mở project trong Android Studio
- Chọn device/emulator
- Click "Run" (Shift + F10)

**Cách 2: Command Line**
```bash
.\gradlew installDebug
```

---

## 📋 BƯỚC 4: Đăng Nhập Admin

1. Mở app trên emulator/device
2. Nhập thông tin đăng nhập:
   - **Username**: `admin`
   - **Password**: `Admin@123`
3. Click **Login**

**✨ App sẽ tự động chuyển đến Admin Dashboard!**

---

## 🔍 KIỂM TRA KẾT NỐI

### Verify Backend đang chạy
```bash
# Test dashboard API
curl http://localhost:8080/api/admin/dashboard/admin

# Test check admin API
curl http://localhost:8080/api/auth/check-admin/admin
```

### Verify Database
```sql
-- Kiểm tra admin user
SELECT username, email, role FROM users WHERE role = 'ADMIN';
```

### Check Android Logcat
```
Tag: AuthViewModel
- "Login successful - User: admin, Role: ADMIN, IsAdmin: true"

Tag: NavGraph
- "User isAdmin: true"
- "Admin user detected, navigating to Admin Dashboard"
```

---

## 🛠️ TROUBLESHOOTING

### ❌ App không kết nối được Backend

**Giải pháp:**
1. Kiểm tra backend đang chạy: `curl http://localhost:8080/api/admin/dashboard/admin`
2. Kiểm tra BASE_URL trong code:
   - Emulator: `http://10.0.2.2:8080/`
   - Device thật: `http://192.168.1.XXX:8080/` (thay bằng IP máy tính)
3. Tắt Windows Firewall tạm thời
4. Check Logcat xem có lỗi network không

### ❌ Login thành công nhưng không vào Admin Dashboard

**Nguyên nhân:** User chưa có role ADMIN

**Giải pháp:**
```sql
-- Kiểm tra role hiện tại
SELECT username, role FROM users WHERE username = 'admin';

-- Update role thành ADMIN (phải viết HOA)
UPDATE users SET role = 'ADMIN' WHERE username = 'admin';
```

**Lưu ý:** Role phải là `ADMIN` (viết HOA), không phải `admin` hay `Admin`

### ❌ Backend báo lỗi 403 Forbidden

**Nguyên nhân:** User không có quyền admin

**Giải pháp:** Kiểm tra lại role trong database như trên

### ❌ Database connection error

**Giải pháp:**
1. Kiểm tra PostgreSQL đang chạy
2. Verify connection string trong `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5332/wordsearch_db
   spring.datasource.username=wordsearch_user
   spring.datasource.password=wordsearch_password
   ```

### ❌ Mock data vẫn xuất hiện

**Giải pháp:**
1. Kiểm tra `AdminRepository.kt`:
   ```kotlin
   private const val USE_MOCK_DATA = false  // Phải là false
   ```
2. Clean và rebuild project:
   ```bash
   .\gradlew clean build
   ```

---

## 📊 TÍNH NĂNG ADMIN PANEL

Sau khi đăng nhập thành công, bạn có thể:

### 1. Dashboard
- Xem tổng quan thống kê
- Số lượng questions, contests, users
- Thống kê theo game type

### 2. Question Management
- ✅ Xem danh sách câu hỏi
- ✅ Tạo câu hỏi mới
- ✅ Sửa câu hỏi
- ✅ Xóa câu hỏi
- ✅ Filter theo category, difficulty
- ✅ Bulk import câu hỏi

### 3. Contest Management
- ✅ Xem danh sách contests
- ✅ Tạo contest mới
- ✅ Sửa contest
- ✅ Xóa contest
- ✅ Xem thống kê contest
- ✅ Quản lý leaderboard

---

## 🎯 KẾT LUẬN

Bây giờ bạn có thể:
1. ✅ Khởi động backend Spring Boot
2. ✅ Tạo admin user trong database
3. ✅ Build và chạy Android app
4. ✅ Đăng nhập với tài khoản admin
5. ✅ Tự động vào Admin Dashboard
6. ✅ Quản lý Questions và Contests

**Chúc bạn thành công! 🎉**

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề:
1. Kiểm tra Logcat trong Android Studio
2. Kiểm tra Console của Spring Boot backend
3. Verify database connections
4. Đảm bảo role = 'ADMIN' (viết HOA)
