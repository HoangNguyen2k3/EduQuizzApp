package com.example.eduquizz.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.auth.data.repository.AuthResult
import com.example.eduquizz.features.auth.data.repository.SecureAuthRepository
import com.example.eduquizz.features.auth.model.UserProfileData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val profileData: UserProfileData = UserProfileData(),
    val isProfileSaved: Boolean = false
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: SecureAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    // Không tự động load saved profile data trong init để tránh hiển thị dữ liệu cũ
    // khi người dùng đăng ký tài khoản mới

    /**
     * Load saved profile data manually when needed (e.g., for editing existing profile)
     */
    fun loadSavedProfile() {
        val savedProfile = repository.getSavedProfileData()
        _uiState.value = _uiState.value.copy(profileData = savedProfile)
    }

    fun updateFullName(value: String) {
        _uiState.value = _uiState.value.copy(
            profileData = _uiState.value.profileData.copy(fullName = value)
        )
    }

    fun updateDateOfBirth(value: String) {
        _uiState.value = _uiState.value.copy(
            profileData = _uiState.value.profileData.copy(dateOfBirth = value)
        )
    }

    fun updateGender(value: String) {
        _uiState.value = _uiState.value.copy(
            profileData = _uiState.value.profileData.copy(gender = value)
        )
    }

    fun updateHometown(value: String) {
        _uiState.value = _uiState.value.copy(
            profileData = _uiState.value.profileData.copy(hometown = value)
        )
    }

    fun updatePhoneNumber(value: String) {
        _uiState.value = _uiState.value.copy(
            profileData = _uiState.value.profileData.copy(phoneNumber = value)
        )
    }

    fun updateCccd(value: String) {
        _uiState.value = _uiState.value.copy(
            profileData = _uiState.value.profileData.copy(cccd = value)
        )
    }

    fun updateCccdIssueDate(value: String) {
        _uiState.value = _uiState.value.copy(
            profileData = _uiState.value.profileData.copy(cccdIssueDate = value)
        )
    }

    fun updateCccdIssuePlace(value: String) {
        _uiState.value = _uiState.value.copy(
            profileData = _uiState.value.profileData.copy(cccdIssuePlace = value)
        )
    }

    fun saveProfile(userId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            val profile = _uiState.value.profileData

            // Validate required fields
            if (profile.fullName.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Vui lòng nhập tên đầy đủ"
                )
                return@launch
            }

            if (profile.dateOfBirth.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Vui lòng chọn ngày sinh"
                )
                return@launch
            }

            if (profile.gender.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Vui lòng chọn giới tính"
                )
                return@launch
            }

            if (profile.hometown.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Vui lòng nhập quê quán"
                )
                return@launch
            }

            // Validate sensitive fields - now required
            if (profile.phoneNumber.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Vui lòng nhập số điện thoại"
                )
                return@launch
            }

            if (profile.cccd.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Vui lòng nhập số CCCD"
                )
                return@launch
            }

            if (profile.cccdIssueDate.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Vui lòng chọn ngày cấp CCCD"
                )
                return@launch
            }

            if (profile.cccdIssuePlace.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Vui lòng nhập nơi cấp CCCD"
                )
                return@launch
            }


            // Save profile
            val result = repository.updateUserProfile(
                userId = userId,
                fullName = profile.fullName,
                dateOfBirth = profile.dateOfBirth,
                gender = profile.gender,
                hometown = profile.hometown,
                phoneNumber = profile.phoneNumber,
                cccd = profile.cccd,
                cccdIssueDate = profile.cccdIssueDate,
                cccdIssuePlace = profile.cccdIssuePlace
            )

            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Cập nhật thông tin thành công!",
                        isProfileSaved = true
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    /**
     * Xóa tất cả dữ liệu profile
     * Được gọi khi người dùng đăng ký tài khoản mới
     */
    fun clearProfileData() {
        repository.clearSavedProfileData()
        _uiState.value = _uiState.value.copy(
            profileData = UserProfileData()
        )
    }
}
