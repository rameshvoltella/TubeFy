package com.ramzmania.tubefy.extractor.Interfaces;

import com.ramzmania.tubefy.extractor.Models.VideoStream;

import java.util.List;

public interface YoutubeExtractorCallback {

    void onSuccess(List<VideoStream> videoStreamList);

    void onError(String errorMessage);

}
