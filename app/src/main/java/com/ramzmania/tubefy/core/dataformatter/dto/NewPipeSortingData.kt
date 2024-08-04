package com.ramzmania.tubefy.core.dataformatter.dto

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page

data class NewPipeSortingData(val result: MutableList<InfoItem>, val nextPage: Page)