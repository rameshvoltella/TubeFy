package com.ramzmania.tubefy

import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.ramzmania.tubefy.core.yotubesearch.scrapping.YoutubeJsonScrapping
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Main: ComponentActivity()
{
    private val webViewModel: TubeFyViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kk)

        val scrapping = YoutubeJsonScrapping(webViewModel)

        // Observe the HTML content
        webViewModel.htmlContent.observe(this, Observer { htmlContent ->
            // Do something with the htmlContent
            println(htmlContent)
            Toast.makeText(applicationContext,"YOOOO"+htmlContent,1).show()
        })

        // Start fetching the page source
        scrapping.fetchPageSource("https://www.youtube.com/results?search_query=wwe", this)
    }
}
