package com.ramzmania.tubefy.errors

import com.ramzmania.tubefy.errors.mapper.ErrorMapper
import javax.inject.Inject

/**
 * This ERROR manager class
 */

class ErrorManager @Inject constructor(private val errorMapper: ErrorMapper) : ErrorUseCase {
    override fun getError(errorCode: Int): Error {
        return Error(code = errorCode, description = errorMapper.errorsMap.getValue(errorCode))
    }
}
