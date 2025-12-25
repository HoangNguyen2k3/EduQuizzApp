# 1. Cấu hình làm mờ mạnh
-repackageclasses 'o'
-allowaccessmodification
-overloadaggressively
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,Signature,Exceptions

# 2. Xóa Log
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# 3. KHÔNG giữ lại toàn bộ package security, chỉ giữ lại những gì hệ thống gọi qua Reflection
# (Nếu bạn dùng Hilt, Hilt đã tự có rule nên bạn không cần keep quá nhiều)

# 4. Chỉ giữ lại tên các Data Class để GSON/Retrofit không bị lỗi
# Thay vì giữ toàn bộ package, hãy dùng @Keep annotation trong code Kotlin
# hoặc chỉ keep các class model:
-keepclassmembers class com.example.eduquizz.**.model.** {
    <fields>;
}

# 5. Xóa bỏ các dòng keep Kotlin/Firebase thừa thãi
# (Vì R8 đã tự hiểu các thư viện này rồi)