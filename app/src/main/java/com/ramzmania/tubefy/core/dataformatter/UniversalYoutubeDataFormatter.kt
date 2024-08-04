package com.ramzmania.tubefy.core.dataformatter

import org.schabi.newpipe.extractor.InfoItem

abstract class UniversalYoutubeDataFormatter<in Input,out FormattedData>{

    protected abstract suspend fun runFormatting(input:Input):FormattedData

    suspend fun run(input: Input):FormattedData{return runFormatting(input)}
}