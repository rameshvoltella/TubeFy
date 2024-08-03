package com.ramzmania.tubefy.ui


import android.util.Log
import android.widget.Toast
import com.ramzmania.tubefy.core.dataformatter.BasicResponse
import com.ramzmania.tubefy.core.dataformatter.StreamUrlData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubeV3.YoutubeV3Response
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.observe
import com.ramzmania.tubefy.databinding.KkBinding
import com.ramzmania.tubefy.ui.base.BaseBinderActivity
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.AndroidEntryPoint
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.search.SearchInfo

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
        observe(viewModel.formattedList, ::handleFormatListResponse)
        observe(viewModel.streamUrlData, ::HandleStreamUrlResponse)
        observe(viewModel.newPipeSearch, ::HandleNewPipeSearchResponse)
        observe(viewModel.newPipeSearchNext,::HandleNewPipeSearchNextResponse)
        observe(viewModel.youtubeV3Search,::HandleYoutubeV3Response)


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

    fun handleFormatListResponse(formatResponse: Resource<BasicResponse>) {
        when (formatResponse) {
            is Resource.Loading -> {
            }

            is Resource.Success -> {
//                Toast.makeText(applicationContext, ">" + "started done", 1).show()

                Toast.makeText(
                    applicationContext,
                    "WEB SCRAPPING" + formatResponse.data!!.sortedVideoDataList.size,
                    1
                ).show()

                viewModel.getStreamUrl(formatResponse.data!!.sortedVideoDataList[0].videoId)


            }

            is Resource.DataError -> {
            }

            else -> {}
        }
    }

    private fun HandleStreamUrlResponse(resource: Resource<StreamUrlData>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                Toast.makeText(applicationContext, "WEB SCAPPING DATA>" + resource.data!!.streamUrl, 1).show()
                Log.d("url", "" + resource.data!!.streamUrl)
            }

            is Resource.DataError -> {
            }

            else -> {}
        }
    }
    fun HandleNewPipeSearchResponse(resource: Resource<SearchInfo>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                nextPage=resource.data!!.nextPage
                Toast.makeText(applicationContext, "NEWPIPE HOME" + resource.data!!.relatedItems[0].name, 1).show()
//                Log.d("url", "" + resource.data!!.streamUrl)
//                calculateSearchResult(resource.data!!.relatedItems)
            }

            is Resource.DataError -> {
            }

            else -> {}
        }

    }

    private fun calculateSearchResult(relatedItems: MutableList<InfoItem>) {
        for(data in relatedItems) {
            Log.d("onlynames",data.name+"<>"+data.url)
        }
    }
//    private fun calculateSearchResult2(relatedItems: MutableList<InfoItem>) {
//        for(data in relatedItems) {
//            Log.d("onlynames",data.name)
//        }
//    }

    fun HandleNewPipeSearchNextResponse(resource: Resource<ListExtractor.InfoItemsPage<InfoItem>>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                nextPage=resource.data!!.nextPage
//                calculateSearchResult(resource.data.items)
                Toast.makeText(applicationContext, "NEWPIPE NEXT>" + resource.data.items.size, 1).show()
//                Log.d("url", "" + resource.data!!.streamUrl)
            }

            is Resource.DataError -> {
            }

            else -> {}
        }
    }

    fun HandleYoutubeV3Response(resource: Resource<YoutubeV3Response>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                Toast.makeText(applicationContext, "YOUTUBE V3>" + resource.data!!.items.size, 1).show()
//                Log.d("url", "" + resource.data!!.streamUrl)
            }

            is Resource.DataError -> {
            }

            else -> {}
        }
    }

}






