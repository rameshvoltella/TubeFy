package com.ramzmania.tubefy


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Main: ComponentActivity()
{
//    @Inject
//    lateinit var scrapping:YoutubeJsonScrapping

    private val webViewModel: TubeFyViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kk)

//        val scrapping = YoutubeJsonScrapping(webViewModel)

        // Observe the HTML content
        webViewModel.htmlContent.observe(this, Observer { htmlContent ->
            // Do something with the htmlContent

            val response2= parseJson(htmlContent)
            Toast.makeText(applicationContext,"YOOOO"+            response2!!.contents.sectionListRenderer.contents.get(0).itemSectionRenderer!!.contentsBaseRenderer.size
                ,1).show()
        })
        webViewModel.startScrapping()


        // Start fetching the page source
//        scrapping.fetchPageSource("https://www.youtube.com/results?search_query=wwe", webViewModel)
    }
    fun parseJson(jsonString: String): ApiResponse? {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val jsonAdapter = moshi.adapter(ApiResponse::class.java)
        return jsonAdapter.fromJson(jsonString)
    }
}
