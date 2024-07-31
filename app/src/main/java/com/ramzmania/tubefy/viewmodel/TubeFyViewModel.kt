package com.ramzmania.tubefy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ramzmania.tubefy.data.ContextModule
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TubeFyViewModel @Inject constructor(contextModule: ContextModule):ViewModel() {
    private val _htmlContent = MutableLiveData<String>()
    val htmlContent: LiveData<String> get() = _htmlContent

    fun setHtmlContent(content: String) {
        _htmlContent.value = content
    }


}