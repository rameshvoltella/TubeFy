package com.ramzmania.tubefy.ui


import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.ramzmania.tubefy.core.YoutubeCoreConstant.extractYoutubeVideoId
import com.ramzmania.tubefy.core.dataformatter.YoutubeApiType
import com.ramzmania.tubefy.core.extractors.yotubewebextractor.YoutubeScrapType
import com.ramzmania.tubefy.data.dto.base.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListCategory
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListData
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
        observe(viewModel.youTubeMusicHomeData, ::HandleYtMusicHomeResponse)
        observe(viewModel.youTubeMusicHomeDefaultData, ::HandleDefaultHomeResponse)
        observe(viewModel.youTubePlayListData, ::HandleDefaultPlayListResponse)
        observe(viewModel.youTubeMusicCategoryData,:: HandleCategoryResposne)



    }




    override fun observeActivity() {
//        viewModel.startScrapping("wwe")
        binding.next.setOnClickListener {
            if (Page.isValid(nextPage)) {
                viewModel.searchNewPipeNextPage(nextPage,mutableListOf<String>("music_songs"),"aavesham")
            }
        }

        binding.YOUtbev3.setOnClickListener {
            viewModel.loadPlayList("https://music.youtube.com/playlist?list=RDCLAK5uy_kmPRjHDECIcuVwnKsx2Ng7fyNgFKWNJFs")

        }
        binding.lasttype.setOnClickListener {
            viewModel.callCatogyplayList()
//            viewModel.loadDefaultHomeData()
//            viewModel.startWebScrapping("https://music.youtube.com/moods_and_genres", YoutubeScrapType.YOUTUBE_MUSIC_CATEGORY)
                    }

        binding.newpipehome.setOnClickListener { viewModel.searchNewPipePage("aavesham",mutableListOf<String>("music_songs")) }
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
    fun HandleYtMusicHomeResponse(resource: Resource<List<HomePageResponse?>>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                Toast.makeText(applicationContext, "WEB SCAPPING DATA>" + resource.data!!.size, 1).show()
                Log.d("url", "" + resource.data[0]!!.contentData!!.size)
//                Log.d("url", "" + resource.data[1]!!.contentData!!.size)

//                openUrlInBrowser( resource.data!!.streamUrl)
            }

            is Resource.DataError -> {
            }

            else -> {}
        }
    }

    fun HandleDefaultHomeResponse(resource: Resource<List<HomePageResponse?>>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                Toast.makeText(applicationContext, "DAT FROM ASSERTS DATA>" + resource.data!!.size, 1).show()

                resource.data.forEach {
                    Log.d("url", "<><><>" + it?.contentData?.get(0)?.playlistId)

                }
//                Log.d("url", "" + resource.data[1]!!.contentData?.get(0)?.playlistId)
//                Log.d("url", "" + resource.data[2]!!.contentData?.get(0)?.playlistId)
//                Log.d("url", "" + resource.data[3]!!.contentData?.get(0)?.playlistId)
//                Log.d("url", "" + resource.data[4]!!.contentData?.get(0)?.playlistId)
//                Log.d("url", "" + resource.data[5]!!.contentData?.get(0)?.playlistId)

//                openUrlInBrowser( resource.data!!.streamUrl)
            }

            is Resource.DataError -> {
            }

            else -> {}
        }
    }
    private fun HandleCategoryResposne(resource: Resource<List<PlayListCategory?>>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                Toast.makeText(applicationContext, "DAT FROM ASSERTS DATA>"+resource.data?.size, 1).show()
                Log.d("pada", "playListCategoryId"+resource.data?.get(1)?.playListCategoryId!!)
                Log.d("pada", "playListBrowserId"+resource.data?.get(1)?.playListBrowserId!!)
                Log.d("pada", "playListName"+resource.data?.get(1)?.playListName!!)


//                resource.data?.playListVideoList?.forEach {
//                    Log.d("url", "<><><>" + it?.videoId)
//
//                }
//                Log.d("url", "" + resource.data[1]!!.contentData?.get(0)?.playlistId)
//                Log.d("url", "" + resource.data[2]!!.contentData?.get(0)?.playlistId)
//                Log.d("url", "" + resource.data[3]!!.contentData?.get(0)?.playlistId)
//                Log.d("url", "" + resource.data[4]!!.contentData?.get(0)?.playlistId)
//                Log.d("url", "" + resource.data[5]!!.contentData?.get(0)?.playlistId)

//                openUrlInBrowser( resource.data!!.streamUrl)
            }

            is Resource.DataError -> {
            }

            else -> {}
        }
    }

    private fun HandleDefaultPlayListResponse(resource: Resource<PlayListData>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                Toast.makeText(applicationContext, "DAT FROM ASSERTS DATA>", 1).show()

                resource.data?.playListVideoList?.forEach {
                    Log.d("url", "<><><>" + it?.videoId)

                }
//                Log.d("url", "" + resource.data[1]!!.contentData?.get(0)?.playlistId)
//                Log.d("url", "" + resource.data[2]!!.contentData?.get(0)?.playlistId)
//                Log.d("url", "" + resource.data[3]!!.contentData?.get(0)?.playlistId)
//                Log.d("url", "" + resource.data[4]!!.contentData?.get(0)?.playlistId)
//                Log.d("url", "" + resource.data[5]!!.contentData?.get(0)?.playlistId)

//                openUrlInBrowser( resource.data!!.streamUrl)
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









