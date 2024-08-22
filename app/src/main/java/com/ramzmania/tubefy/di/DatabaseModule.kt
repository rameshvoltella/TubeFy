package com.ramzmania.tubefy.di
import android.content.Context
import androidx.room.Room
import com.ramzmania.tubefy.database.PlaylistDao
import com.ramzmania.tubefy.database.TubefyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TubefyDatabase {
        return Room.databaseBuilder(
            context,
            TubefyDatabase::class.java,
            "playlist_database"
        ).build()
    }

    @Provides
    fun providePlaylistDao(database: TubefyDatabase): PlaylistDao {
        return database.playlistDao()
    }
}
