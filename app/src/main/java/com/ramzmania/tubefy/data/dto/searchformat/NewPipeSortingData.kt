package com.ramzmania.tubefy.data.dto.searchformat

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page

data class NewPipeSortingData(val result: MutableList<InfoItem>, val nextPage: Page)