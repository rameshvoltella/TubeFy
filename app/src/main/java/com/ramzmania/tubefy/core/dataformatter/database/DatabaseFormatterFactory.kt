package com.ramzmania.tubefy.core.dataformatter.database

import com.ramzmania.tubefy.data.dto.base.playlist.PlayListData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.database.ActivePlaylist
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.database.FavoritePlaylist
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseFormatterFactory @Inject constructor() {

    // Example method for formatting a list of TubeFyCoreTypeData to a list of ActivePlaylist
    fun formatActivePlayList(): DatabaseFormatter<List<TubeFyCoreTypeData?>, List<ActivePlaylist>,ActivePlaylist> {
        return DatabaseFormatter(ActivePlaylist::class)
    }

    fun formatTubeFyPlayList(): DatabaseFormatter< List<ActivePlaylist>,List<TubeFyCoreTypeData?>,TubeFyCoreTypeData> {
        return DatabaseFormatter(TubeFyCoreTypeData::class)
    }

    fun formatTubeFyCustomPlayList(): DatabaseFormatter< List<CustomPlaylist>,List<TubeFyCoreTypeData?>,TubeFyCoreTypeData> {
        return DatabaseFormatter(TubeFyCoreTypeData::class)
    }

    fun formatTubeFyFavouritePlayList(): DatabaseFormatter< List<FavoritePlaylist>,List<TubeFyCoreTypeData?>,TubeFyCoreTypeData> {
        return DatabaseFormatter(TubeFyCoreTypeData::class)
    }

    // Add more methods for different input/output types if needed
//    fun formatSomeOtherType(): DatabaseFormatter<List<AnotherInputType>, List<AnotherOutputType>> {
//        return DatabaseFormatter()
//    }
}
