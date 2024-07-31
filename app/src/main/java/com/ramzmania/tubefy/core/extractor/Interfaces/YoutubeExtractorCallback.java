package com.ramzmania.tubefy.core.extractor.Interfaces;

import com.ramzmania.tubefy.core.extractor.Models.VideoStream;

import java.util.List;

public interface YoutubeExtractorCallback {

    void onSuccess(List<VideoStream> videoStreamList);

    void onError(String errorMessage);

}
