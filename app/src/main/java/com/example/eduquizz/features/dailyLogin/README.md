# Daily Login Reward Feature

## Tổng quan
Chức năng đăng nhập 7 ngày liên tiếp với phần thưởng tăng dần:
- Ngày 1: 100 vàng
- Ngày 2: 200 vàng
- Ngày 3: 300 vàng
- Ngày 4: 400 vàng
- Ngày 5: 500 vàng
- Ngày 6: 600 vàng
- Ngày 7: 700 vàng

Sau khi hoàn thành 7 ngày, chu kỳ sẽ reset về ngày 1.

## Cấu trúc Files

```
features/dailyLogin/
├── model/
│   └── DailyLoginReward.kt          # Data models
├── repository/
│   └── DailyLoginRepository.kt      # Firebase repository với security
├── viewmodel/
│   └── DailyLoginViewModel.kt       # ViewModel quản lý logic
└── screens/
    └── DailyLoginScreen.kt          # UI Screen
```

## Tính năng bảo mật

### 1. Server Timestamp Validation
- Sử dụng Firebase Server Timestamp thay vì local time
- Validate client timestamp với server timestamp trước khi claim
- Cho phép chênh lệch tối đa 5 phút (do network delay)

### 2. Chống gian lận đổi thời gian
- Tất cả timestamps được lưu trên Firebase với server timestamp
- Không thể claim reward nếu đã claim trong ngày
- Nếu bỏ lỡ 1 ngày, chu kỳ sẽ reset về ngày 1
- Không thể claim ngày tiếp theo nếu chưa claim ngày hiện tại

### 3. Firebase Security Rules (Cần cấu hình)
```javascript
// Firestore Security Rules
match /daily_login_rewards/{userId} {
  allow read: if request.auth != null && request.auth.uid == userId;
  allow write: if request.auth != null && request.auth.uid == userId 
    && request.resource.data.lastServerTimestamp == request.time;
}
```

## Cách sử dụng

### 1. Navigation
```kotlin
// Trong NavGraph.kt
composable(Routes.DAILY_LOGIN) {
    DailyLoginScreen(
        onBackClick = { navController.navigateUp() }
    )
}

// Navigate từ MainScreen
navController.navigate(Routes.DAILY_LOGIN)
```

### 2. ViewModel
```kotlin
@HiltViewModel
class DailyLoginViewModel @Inject constructor(
    private val repository: DailyLoginRepository,
    private val dataViewModel: DataViewModel
) : ViewModel() {
    // Load data
    fun loadDailyLoginData(userId: String)
    
    // Claim reward
    fun claimTodayReward(userId: String)
}
```

### 3. Repository
```kotlin
@Singleton
class DailyLoginRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Get server timestamp
    suspend fun getServerTimestamp(): Timestamp
    
    // Validate timestamp
    suspend fun validateTimestamp(clientTimestamp: Long): Boolean
    
    // Claim reward
    suspend fun claimDailyReward(
        userId: String,
        clientTimestamp: Long
    ): Result<Pair<Int, Int>>
}
```

## Firebase Structure

### Collection: `daily_login_rewards`
```json
{
  "userId": "user123",
  "currentDay": 3,
  "lastClaimedDay": 3,
  "lastClaimedDate": "2024-01-15T00:00:00Z",
  "lastServerTimestamp": "2024-01-15T00:00:00Z",
  "cycleStartDate": "2024-01-13T00:00:00Z",
  "rewards": [
    {
      "day": 1,
      "goldReward": 100,
      "isClaimed": true,
      "claimedAt": "2024-01-13T00:00:00Z"
    },
    // ... các ngày khác
  ],
  "updatedAt": "2024-01-15T00:00:00Z"
}
```

## UI Features

1. **Progress Indicator**: Hiển thị tiến độ 7 ngày
2. **Reward Cards**: 7 cards hiển thị phần thưởng mỗi ngày
3. **Animation**: Scale animation cho ngày hiện tại
4. **Status Icons**: 
   - ✅ Đã claim
   - 🔒 Chưa mở khóa
   - 🎁 Có thể claim
5. **Success/Error Messages**: Snackbar thông báo

## Testing

### Test Cases
1. ✅ Claim ngày đầu tiên
2. ✅ Claim các ngày tiếp theo
3. ✅ Không thể claim 2 lần trong 1 ngày
4. ✅ Reset về ngày 1 nếu bỏ lỡ
5. ✅ Validate timestamp chống gian lận
6. ✅ Hoàn thành chu kỳ 7 ngày và reset

### Test với thay đổi thời gian
1. Claim ngày 1
2. Đổi thời gian điện thoại sang ngày mai
3. Thử claim → **Phải bị từ chối** (timestamp validation failed)

## Lưu ý

1. **Firebase Rules**: Cần cấu hình Firestore Security Rules để bảo vệ dữ liệu
2. **Network**: Cần kết nối internet để validate server timestamp
3. **User ID**: Sử dụng username từ auth để làm userId
4. **Gold Integration**: Tự động cộng gold vào tài khoản khi claim

