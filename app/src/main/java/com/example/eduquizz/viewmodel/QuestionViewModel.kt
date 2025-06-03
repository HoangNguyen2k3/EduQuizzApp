package com.example.eduquizz.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.model.DataOrException
import com.example.eduquizz.model.QuestionItem
import com.example.eduquizz.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
@HiltViewModel
class QuestionViewModel
@Inject constructor(private val repository: QuestionRepository):ViewModel() {
    val data : MutableState<DataOrException<ArrayList<QuestionItem>,Boolean,Exception>>
                    = mutableStateOf(DataOrException(null,true,Exception("")))
    init{
        getAllQuestions()
    }
    private fun getAllQuestions(){
        viewModelScope.launch {
            data.value.loading = true
            data.value = repository.getAllQuestion()

            if(data.value.data.toString().isNotEmpty()){
                data.value.loading = false
            }
        }
    }
    fun getTotalQuestionCount():Int{
        return data.value.data?.toMutableList()?.size?:0
    }
}*/
@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val repository: QuestionRepository
) : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count = _count
    val data: MutableState<DataOrException<ArrayList<QuestionItem>, Boolean, Exception>> =
        mutableStateOf(DataOrException(null, true, null))

    init {
        getAllQuestions()
    }


/*    private fun getAllQuestions() {
        viewModelScope.launch {
            data.value = DataOrException(null, true, null)  // loading = true
            try {
                val result = repository.getAllQuestion()
                data.value = result  // cập nhật data, loading, exception
            } catch (e: Exception) {
                data.value = DataOrException(null, false, e)
            }
        }
    }*/
private fun getAllQuestions() {
    viewModelScope.launch {
        if (data.value.data == null) {  // chỉ lấy khi chưa có data
            data.value = DataOrException(null, true, null)
            try {
                val result = repository.getAllQuestion()
                data.value = result
            } catch (e: Exception) {
                data.value = DataOrException(null, false, e)
            }
        }
    }
}

    fun getTotalQuestionCount(): Int {
        return data.value.data?.size ?: 0
    }
}

