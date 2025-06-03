package com.example.eduquizz.model

import androidx.compose.foundation.text.input.InputTransformation.Companion.applySemantics

class DataOrException<T,Boolean,E:Exception> (
    var data: T? = null,
    var loading: kotlin.Boolean?=null,
    var e:E?=null
)
