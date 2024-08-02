package com.ramzmania.tubefy.errors


interface ErrorUseCase {
    fun getError(errorCode: Int): Error
}
