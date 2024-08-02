package com.ramzmania.tubefy.core.newpipeextractor

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.io.IOException

class PipeDownloader(private val client: OkHttpClient) : Downloader() {

    @Throws(IOException::class, ReCaptchaException::class)
    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()
        val requestBody = dataToSend?.let { RequestBody.create(null, it) }
        val requestBuilder = okhttp3.Request.Builder()
            .method(httpMethod, requestBody)
            .url(url)
            .addHeader("User-Agent", USER_AGENT)
        headers.forEach { (headerName, headerValueList) ->
            when {
                headerValueList.size > 1 -> {
                    requestBuilder.removeHeader(headerName)
                    headerValueList.forEach { value ->
                        value?.let { requestBuilder.addHeader(headerName, it) }
                    }
                }
                headerValueList.size == 1 -> requestBuilder.header(headerName, headerValueList[0])
            }
        }

        // Get the response from Youtube
        val response = client.newCall(requestBuilder.build()).execute()
        if (response.code == TOO_MANY_REQUESTS) {
            response.close()
            throw ReCaptchaException("reCaptcha Challenge requested", url)
        }
        val responseBodyToReturn = response.body?.string()
        val latestUrl = response.request.url.toString()
        return Response(
            response.code, response.message, response.headers.toMultimap(),
            responseBodyToReturn, latestUrl
        )
    }

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:68.0) Gecko/20100101 Firefox/68.0"

        private const val TOO_MANY_REQUESTS  = 429

        fun create(client: OkHttpClient): PipeDownloader {
            return PipeDownloader(client)
        }
    }
}