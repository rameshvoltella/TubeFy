package com.ramzmania.tubefy


import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.ramzmania.tubefy.core.dataformatter.BasicResponse
import com.ramzmania.tubefy.core.dataformatter.YouTubePageStripDataFormatter
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.observe
import com.ramzmania.tubefy.databinding.KkBinding
import com.ramzmania.tubefy.ui.base.BaseBinderActivity
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Main: BaseBinderActivity<KkBinding,TubeFyViewModel>()
{
//    @Inject
//    lateinit var scrapping:YoutubeJsonScrapping

    private val webViewModel: TubeFyViewModel by viewModels()
    override fun getViewModelClass()= TubeFyViewModel::class.java

    override fun getViewBinding()= KkBinding.inflate(layoutInflater)

    override fun observeViewModel() {
        observe(viewModel.formattedList, ::handleFormatListResponse)

    }



    override fun observeActivity() {
        viewModel.startScrapping("wwe")
    }

/*    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kk)

//        val scrapping = YoutubeJsonScrapping(webViewModel)

        // Observe the HTML content
        webViewModel.htmlContent.observe(this, Observer { htmlContent ->
            // Do something with the htmlContent

            val response2= parseJson(htmlContent)
            Toast.makeText(applicationContext,"YOOOO"+            response2!!.contents.sectionListRenderer.contents.get(0).itemSectionRenderer!!.contentsBaseRenderer.size
                ,1).show()
           var list= YouTubePageStripDataFormatter().getFormattedData(response2)
//            Toast.makeText(applicationContext,"list"+  list!!.size
//                ,1).show()
            for(data in list!!)
            {
                Log.d("sorted list",""+data.videoId)
            }
        })
        webViewModel.startScrapping()


        // Start fetching the page source
//        scrapping.fetchPageSource("https://www.youtube.com/results?search_query=wwe", webViewModel)
    }*/
    fun parseJson(jsonString: String): ApiResponse? {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val jsonAdapter = moshi.adapter(ApiResponse::class.java)
        return jsonAdapter.fromJson(jsonString)
    }
    fun handleFormatListResponse(formatResponse: Resource<BasicResponse>) {
        when(formatResponse)
        {
            is Resource.Loading -> {
            }
            is Resource.Success -> {
                Toast.makeText(applicationContext,">"+"started done",1).show()

                Toast.makeText(applicationContext,">"+formatResponse.data!!.sortedVideoDataList.size,1).show()


            }
            is Resource.DataError -> {
            }
            else -> {}
        }
    }
}


