package com.ramzmania.tubefy.data.dto.base.searchformat

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page

data class NewPipeSortingData<T>(val result: MutableList<T>, val nextPage: Page?)