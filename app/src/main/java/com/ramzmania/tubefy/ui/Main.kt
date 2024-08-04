package com.ramzmania.tubefy.ui


import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.ramzmania.tubefy.core.YoutubeCoreConstant.extractYoutubeVideoId
import com.ramzmania.tubefy.core.dataformatter.YoutubeApiType
import com.ramzmania.tubefy.core.dataformatter.dto.StreamUrlData
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreTypeData
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.observe
import com.ramzmania.tubefy.databinding.KkBinding
import com.ramzmania.tubefy.ui.base.BaseBinderActivity
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.AndroidEntryPoint
import org.schabi.newpipe.extractor.Page

@AndroidEntryPoint
class Main : BaseBinderActivity<KkBinding, TubeFyViewModel>() {
//    @Inject
//    lateinit var scrapping:YoutubeJsonScrapping

//    private val webViewModel: TubeFyViewModel by viewModels()
private var nextPage: Page? = null
//    var nameList:ArrayList<String>?=ArrayList()
    override fun getViewModelClass() = TubeFyViewModel::class.java

    override fun getViewBinding() = KkBinding.inflate(layoutInflater)

    override fun observeViewModel() {
        observe(viewModel.youTubeSearchData, ::handleYoutubeSearchListResponse)
        observe(viewModel.streamUrlData, ::HandleStreamUrlResponse)

    }


    override fun observeActivity() {
//        viewModel.startScrapping("wwe")
        binding.next.setOnClickListener {
            if (Page.isValid(nextPage)) {
                viewModel.searchNewPipeNextPage(nextPage!!)
            }
        }

        binding.YOUtbev3.setOnClickListener {
            viewModel.searchYoutubeV3()

        }
        binding.lasttype.setOnClickListener { viewModel.startWebScrapping("aavesham") }

        binding.newpipehome.setOnClickListener { viewModel.searchNewPipePage() }
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



    private fun HandleStreamUrlResponse(resource: Resource<StreamUrlData>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                Toast.makeText(applicationContext, "WEB SCAPPING DATA>" + resource.data!!.streamUrl, 1).show()
                Log.d("url", "" + resource.data!!.streamUrl)
                openUrlInBrowser( resource.data!!.streamUrl)
            }

            is Resource.DataError -> {
            }

            else -> {}
        }
    }

    private fun calculateSearchResult(relatedItems: MutableList<TubeFyCoreTypeData>) {
        for(data in relatedItems) {
            Log.d("onlynames",data.videoTitle+"<>"+data.videoId)
        }
    }

    fun handleYoutubeSearchListResponse(resource: Resource<TubeFyCoreUniversalData>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                var type="none"
                if(resource.data?.apiType==YoutubeApiType.WEB_SCRAPPER)
                {
                    type="WEB"
                }else  if(resource.data?.apiType==YoutubeApiType.YOUTUBE_V3_API)
                {
                    type="YOUTUBE V3"
                }
                else  if(resource.data?.apiType==YoutubeApiType.NEW_PIPE_API)
                {
                    type="NEWPIPE"
                    nextPage=resource.data.youtubeSortedData.newPipePage
                }
                viewModel.getStreamUrl(resource.data?.youtubeSortedData?.youtubeSortedList?.get(0)?.videoId!!)
                val videoID = extractYoutubeVideoId(
                    resource.data?.youtubeSortedData?.youtubeSortedList?.get(0)?.videoId
                    ?: "")
                Toast.makeText(applicationContext, "$type ---- $videoID"+resource.data?.youtubeSortedData?.youtubeSortedList?.get(0)?.videoId, 1).show()
//                calculateSearchResult(resource.data?.youtubeSortedData?.youtubeSortedList!!.toMutableList())
//                Log.d("url", "" + resource.data!!.streamUrl)
            }

            is Resource.DataError -> {
            }

            else -> {}
        }
    }
    private fun openUrlInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        // Verify that there's at least one app that can handle this intent
//        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
//        }
    }
}







