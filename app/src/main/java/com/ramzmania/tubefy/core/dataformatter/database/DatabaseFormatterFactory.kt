package com.ramzmania.tubefy.core.dataformatter.database

import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseFormatterFactory  @Inject constructor(){

    fun createForFormatActivePlayList():DatabaseFormatter<List<TubeFyCoreTypeData>>
    {
        return DatabaseFormatter<List<TubeFyCoreTypeData>>()
    }
}