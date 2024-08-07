package com.ramzmania.tubefy.ui.base

import com.ramzmania.tubefy.errors.ErrorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ramzmania.tubefy.events.SingleEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


/**
 * Base class for ViewModels in the application.
 * Provides common functionality such as error handling and permission management.
 */

abstract class BaseViewModel : ViewModel() {
    /**Inject Singleton ErrorManager
     * Use this errorManager to get the Errors
     */
    @Inject
    lateinit var errorManager: ErrorManager

    private val showToastPrivate= MutableLiveData<SingleEvent<Any>>()
    val showToast: LiveData<SingleEvent<Any>>
        get() = showToastPrivate



    fun showToastMessage(errorCode: Int) {
        val error = errorManager.getError(errorCode)
        showToastPrivate.value = SingleEvent(error.description)
    }

    fun showToastStringMessage(msg: String) {
        showToastPrivate.value = SingleEvent(msg)
    }

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted = _permissionsGranted.asStateFlow()

    fun setPermissionsGranted(granted: Boolean) {
        _permissionsGranted.value = granted
    }


}
