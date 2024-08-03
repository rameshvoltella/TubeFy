package com.ramzmania.tubefy.core.dataformatter

sealed class FormattingResult<out SuccessData,out FailureError> {

    data class SUCCESS<out Data>(val data:Data) :FormattingResult<Data,Nothing>()
    data class FAILURE<out Error>(val error:Error) :FormattingResult<Nothing,Error>()

}