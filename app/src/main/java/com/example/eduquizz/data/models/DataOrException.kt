package com.example.eduquizz.data.models

class DataOrException<T,Boolean,E:Exception> (
    var data: T? = null,
    var loading: kotlin.Boolean?=null,
    var e:E?=null
)