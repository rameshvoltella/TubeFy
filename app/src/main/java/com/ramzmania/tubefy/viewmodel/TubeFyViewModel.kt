package com.ramzmania.tubefy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class TubeFyViewModel:ViewModel() {
    private val _htmlContent = MutableLiveData<String>()
    val htmlContent: LiveData<String> get() = _htmlContent

    fun setHtmlContent(content: String) {
        _htmlContent.value = content
    }
}