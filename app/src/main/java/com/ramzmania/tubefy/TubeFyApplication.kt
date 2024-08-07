package com.ramzmania.tubefy

import android.app.Application
import com.ramzmania.tubefy.core.extractors.newpipeextractor.PipeDownloader
import dagger.hilt.android.HiltAndroidApp
import org.schabi.newpipe.extractor.NewPipe
import javax.inject.Inject

@HiltAndroidApp
class TubeFyApplication:Application() {
    @Inject
    lateinit var pipeDownloader: PipeDownloader
    override fun onCreate() {
        super.onCreate()
        NewPipe.init(pipeDownloader)

//        https://music.youtube.com/podcasts
//        https://music.youtube.com/charts
//        https://music.youtube.com/moods_and_genres
//        https://music.youtube.com/new_releases
    }
}