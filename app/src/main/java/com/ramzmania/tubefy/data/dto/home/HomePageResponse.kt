package com.ramzmania.tubefy.data.dto.home
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.ramzmania.tubefy.data.dto.base.BaseContentData
@Parcelize
data class HomePageResponse(val heading:String,val cellType:CellType,val contentData:List<BaseContentData>?): Parcelable