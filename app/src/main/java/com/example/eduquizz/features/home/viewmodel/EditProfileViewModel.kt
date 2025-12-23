package com.example.eduquizz.features.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.auth.data.repository.SecureAuthRepository
import com.example.eduquizz.features.auth.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    
    // Editable fields
    val fullName: String = "",
    val email: String = "",
    val dateOfBirth: String = "",
    val hometown: String = "",
    val phoneNumber: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    
    // Read-only fields
    val username: String = "",
    val cccd: String = "",
    val cccdIssueDate: String = "",
    val cccdIssuePlace: String = "",
    val userId: Long = 0
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: SecureAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            try {
                // Load from repository
                val savedUser = repository.getSavedUser()
                val profileData = repository.getSavedProfileData()

                _uiState.value = _uiState.value.copy(
                    // Editable
                    fullName = profileData.fullName,
                    email = savedUser?.email ?: "",
                    dateOfBirth = profileData.dateOfBirth,
                    hometown = profileData.hometown,
                    phoneNumber = profileData.phoneNumber,
                    
                    // Read-only
                    username = savedUser?.username ?: "",
                    cccd = profileData.cccd,
                    cccdIssueDate = profileData.cccdIssueDate,
                    cccdIssuePlace = profileData.cccdIssuePlace,
                    userId = savedUser?.id ?: 0
                )
                
                Log.d("EditProfileViewModel", "Loaded profile data: userId=${savedUser?.id}")
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Error loading profile: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Lỗi khi tải thông tin: ${e.message}"
                )
            }
        }
    }

    fun updateFullName(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value)
    }

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun updateDateOfBirth(value: String) {
        _uiState.value = _uiState.value.copy(dateOfBirth = value)
    }

    fun updateHometown(value: String) {
        _uiState.value = _uiState.value.copy(hometown = value)
    }

    fun updatePhoneNumber(value: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = value)
    }

    fun updateNewPassword(value: String) {
        _uiState.value = _uiState.value.copy(newPassword = value)
    }

    fun updateConfirmPassword(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value)
    }

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value

            // Validation
            if (state.fullName.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Vui lòng nhập họ tên")
                return@launch
            }

            if (state.email.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Vui lòng nhập email")
                return@launch
            }

            if (state.dateOfBirth.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Vui lòng nhập ngày sinh")
                return@launch
            }

            // Validate password if changing
            if (state.newPassword.isNotBlank()) {
                if (state.newPassword.length < 6) {
                    _uiState.value = state.copy(errorMessage = "Mật khẩu phải có ít nhất 6 ký tự")
                    return@launch
                }
                
                if (state.newPassword != state.confirmPassword) {
                    _uiState.value = state.copy(errorMessage = "Mật khẩu xác nhận không khớp")
                    return@launch
                }
            }

            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                // Convert date format from dd/MM/yyyy to yyyy-MM-dd for backend
                val dateForBackend = convertDateFormat(state.dateOfBirth)
                
                // Update profile via repository
                val result = repository.updateUserProfile(
                    userId = state.userId,
                    fullName = state.fullName,
                    dateOfBirth = dateForBackend,
                    gender = "", // Keep existing gender, not editable here
                    hometown = state.hometown,
                    phoneNumber = state.phoneNumber,
                    cccd = state.cccd, // Keep existing
                    cccdIssueDate = state.cccdIssueDate, // Keep existing
                    cccdIssuePlace = state.cccdIssuePlace // Keep existing
                )

                when (result) {
                    is AuthResult.Success -> {
                        Log.d("EditProfileViewModel", "Profile updated successfully")
                        
                        // If password was changed, handle it separately
                        // TODO: Implement password change API call if needed
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            successMessage = "Cập nhật thông tin thành công!"
                        )
                    }
                    is AuthResult.Error -> {
                        Log.e("EditProfileViewModel", "Update failed: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Save profile error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi khi lưu thông tin: ${e.message}"
                )
            }
        }
    }

    private fun convertDateFormat(date: String): String {
        return try {
            // Convert from dd/MM/yyyy to yyyy-MM-dd
            val parts = date.split("/")
            if (parts.size == 3) {
                val day = parts[0].padStart(2, '0')
                val month = parts[1].padStart(2, '0')
                val year = parts[2]
                "$year-$month-$day"
            } else {
                date
            }
        } catch (e: Exception) {
            Log.e("EditProfileViewModel", "Date conversion error: ${e.message}")
            date
        }
    }
}
