package com.example.eduquizz.data.local

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataManager @Inject constructor() {
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun getUserName(): String {
        return _userName.value
    }
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userDataManager: UserDataManager
) : ViewModel() {

    val userName: StateFlow<String> = userDataManager.userName

    fun setUserName(name: String) {
        viewModelScope.launch {
            userDataManager.setUserName(name)
        }
    }
}