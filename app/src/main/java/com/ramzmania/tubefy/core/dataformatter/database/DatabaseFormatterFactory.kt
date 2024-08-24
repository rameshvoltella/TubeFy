package com.ramzmania.tubefy.core.dataformatter.database

import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import javax.inject.Singleton

@Singleton
class DatabaseFormatterFactory constructor() {

    fun formatActivePlayList():DatabaseFormatter<List<TubeFyCoreTypeData>>
    {
        return DatabaseFormatter<List<TubeFyCoreTypeData>>()
    }
}