package com.ramzmania.tubefy.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class SharedFlowProvider<T> {
    private val sharedJsonContentPrivate = MutableSharedFlow<T?>()
    val sharedJsonContent: SharedFlow<T?> = sharedJsonContentPrivate

    fun emit(value: T?) {
        sharedJsonContentPrivate.tryEmit(value)
    }
}
