package com.ramzmania.tubefy.extractor;

import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.github.kotvertolet.youtubejextractor.JExtractorCallback;
import com.github.kotvertolet.youtubejextractor.YoutubeJExtractor;
import com.github.kotvertolet.youtubejextractor.exception.YoutubeRequestException;
import com.github.kotvertolet.youtubejextractor.models.AdaptiveAudioStream;
import com.github.kotvertolet.youtubejextractor.models.AdaptiveVideoStream;
import com.github.kotvertolet.youtubejextractor.models.newModels.VideoPlayerConfig;
import com.github.kotvertolet.youtubejextractor.models.youtube.playerResponse.MuxedStream;
import com.ramzmania.tubefy.extractor.Models.VideoStream;
import com.ramzmania.tubefy.extractor.StreamExtractor.ExtractorException;
import com.ramzmania.tubefy.extractor.StreamExtractor.model.YTMedia;
import com.ramzmania.tubefy.extractor.StreamExtractor.model.YTSubtitles;
import com.ramzmania.tubefy.extractor.StreamExtractor.model.YoutubeMeta;
import com.ramzmania.tubefy.extractor.Interfaces.YoutubeExtractorCallback;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeExtractor  {

    private ArrayList<VideoStream> videoStreamArrayList;
    private YoutubeExtractorCallback callback;
    private String ExtractorType;

    public YoutubeExtractor() {
        videoStreamArrayList = new ArrayList<>();
    }

    public void Extract(String Url, String ExtractorType, YoutubeExtractorCallback callback) {
      this.callback = callback;
      this.ExtractorType = ExtractorType;

      if (ExtractorType.equalsIgnoreCase("StreamExtractor")) {
          try {
              new com.ramzmania.tubefy.extractor.StreamExtractor.YoutubeStreamExtractor(new com.ramzmania.tubefy.extractor.StreamExtractor.YoutubeStreamExtractor.ExtractorListner() {
                  @Override
                  public void onExtractionGoesWrong(ExtractorException e) {
                      YoutubeExtractorTask youtubeExtractorTask = new YoutubeExtractorTask();
                      youtubeExtractorTask.execute(Url);

                  }

                  @Override
                  public void onExtractionDone(List<YTMedia> adativeStream, List<YTMedia> muxedStream, List<YTSubtitles> subList, YoutubeMeta meta) {
                      Log.e("ADAP", adativeStream.size() + "");
                      Log.e("mux", muxedStream.size() + "");
                      if (adativeStream != null && adativeStream.size() > 0) {

                          int pos = 0;
                          for (YTMedia yTMedia : adativeStream) {
                              boolean isExist = false;

                              if (yTMedia != null && !TextUtils.isEmpty(yTMedia.getMimeType()) && !TextUtils.isEmpty(yTMedia.getUrl()) && !TextUtils.isEmpty(yTMedia.getQualityLabel())) {
                                  if (!isExist && yTMedia.getMimeType().contains("mp4")) {
                                      VideoStream videoStream = new VideoStream(yTMedia.getQualityLabel(), yTMedia.getUrl(), yTMedia.getItag(), yTMedia.getBitrate(), "", yTMedia.getFps());
                                      videoStreamArrayList.add(videoStream);
                                  }

                              }


                          }

                          callback.onSuccess(videoStreamArrayList);


                      } else if (muxedStream != null && muxedStream.size() > 0) {
                          for (YTMedia yTMedia : muxedStream) {
                              if (yTMedia != null && !TextUtils.isEmpty(yTMedia.getMimeType()) && !TextUtils.isEmpty(yTMedia.getUrl()) && !TextUtils.isEmpty(yTMedia.getQualityLabel())) {
                                  if (yTMedia.getMimeType().contains("mp4")) {
                                      VideoStream videoStream = new VideoStream(yTMedia.getQualityLabel(), yTMedia.getUrl(), yTMedia.getItag(), yTMedia.getBitrate(), "", yTMedia.getFps());
                                      videoStreamArrayList.add(videoStream);
                                  }
                              }

                          }

                          callback.onSuccess(videoStreamArrayList);
                      } else {
                          YoutubeExtractorTask youtubeExtractorTask = new YoutubeExtractorTask();
                          youtubeExtractorTask.execute(Url);
                      }

                  }
              }).useDefaultLogin().Extract(Url);
          } catch (Exception unused) {
              YoutubeExtractorTask youtubeExtractorTask = new YoutubeExtractorTask();
              youtubeExtractorTask.execute(Url);

          }
      }
      else  {
          YoutubeExtractorTask youtubeExtractorTask = new YoutubeExtractorTask();
          youtubeExtractorTask.execute(Url);
      }


    }



    public class YoutubeExtractorTask extends AsyncTask<String, String, List<VideoStream>> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<VideoStream> doInBackground(String... strings) {

            if (!TextUtils.isEmpty(strings[0])) {
                

                if (ExtractorType.equalsIgnoreCase("NewPipeExtractor") || ExtractorType.equalsIgnoreCase("StreamExtractor")) {

                    try {
                        
                        NewPipe.init(DownloaderTestImpl.getInstance());
                        
                        YoutubeStreamExtractor streamExtractor = (YoutubeStreamExtractor) YouTube.getStreamExtractor(strings[0]);
                        
                        streamExtractor.fetchPage();
                        
                        for (org.schabi.newpipe.extractor.stream.VideoStream stream : streamExtractor.getVideoStreams()) {
                            
                            VideoStream videoStream = new VideoStream(stream.getResolution(), stream.getUrl(), stream.getItag(), stream.getBitrate(), stream.getCodec(), stream.getFps());
                            videoStreamArrayList.add(videoStream);
                        }

                        if (videoStreamArrayList.size() <= 0) {
                            YoutubeJExtractor youtubeJExtractor = new YoutubeJExtractor();
                            

                            try {
                                String videoId = getYouTubeId(strings[0]);
                                
                                youtubeJExtractor.extract(videoId, new JExtractorCallback() {
                                    List<AdaptiveVideoStream> videoList = null;
                                    List<MuxedStream> videoListMux = null;

                                    @Override
                                    public void onSuccess(VideoPlayerConfig videoData) {
                                        
                                        if (videoData != null) {
                                            videoList = videoData.getStreamingData().getAdaptiveVideoStreams();
                                            videoListMux = videoData.getStreamingData().getMuxedStreams();
                                            if (videoData.getVideoDetails().isLiveContent()) {
                                                videoList.clear();
                                            }

                                            if (videoList == null || videoList.size() < 1) {
                                                for (MuxedStream muxedStream : videoListMux) {
                                                    AdaptiveVideoStream stream = new AdaptiveVideoStream(muxedStream.getMimeType(), "", muxedStream.getBitrate(), muxedStream.getItag(), muxedStream.getUrl(), muxedStream.getAverageBitrate(), 0, 0, "", muxedStream.getQualityLabel(), muxedStream.getProjectionType());
                                                    videoList.add(stream);
                                                }
                                            }

                                            for (AdaptiveVideoStream stream : videoList) {
                                                VideoStream videoStream = new VideoStream(stream.getQualityLabel(), stream.getUrl(), stream.getiTag(), stream.getBitrate(), stream.getCodec(), stream.getFps());
                                                videoStreamArrayList.add(videoStream);
                                            }

                                        }
                                    }

                                    @Override
                                    public void onNetworkException(YoutubeRequestException e) {
                                        
                                        e.printStackTrace();
                                        callback.onError("Something went wrong...");

                                    }

                                    @Override
                                    public void onError(Exception exception) {
                                        
                                        exception.printStackTrace();
                                        callback.onError("Something went Wrong...");

                                    }
                                });

                            } catch (Exception exception) {
                                
                                exception.printStackTrace();
                                callback.onError("Something went Wrong");

                                // Something really bad happened, nothing we can do except just show some error notification to the user
                            }
                        }

                    } catch (ExtractionException | IOException e) {

                        

                        YoutubeJExtractor youtubeJExtractor = new YoutubeJExtractor();

                        try {
                            String videoId = getYouTubeId(strings[0]);
                            youtubeJExtractor.extract(videoId, new JExtractorCallback() {
                                List<AdaptiveVideoStream> videoList = null;
                                List<MuxedStream> videoListMux = null;

                                @Override
                                public void onSuccess(VideoPlayerConfig videoData) {
                                    if (videoData != null) {
                                        videoList = videoData.getStreamingData().getAdaptiveVideoStreams();
                                        videoListMux = videoData.getStreamingData().getMuxedStreams();
                                        if (videoData.getVideoDetails().isLiveContent()) {
                                            videoList.clear();
                                        }

                                        if (videoList == null || videoList.size() < 1) {
                                            for (MuxedStream muxedStream : videoListMux) {
                                                AdaptiveVideoStream stream = new AdaptiveVideoStream(muxedStream.getMimeType(), "", muxedStream.getBitrate(), muxedStream.getItag(), muxedStream.getUrl(), muxedStream.getAverageBitrate(), 0, 0, "", muxedStream.getQualityLabel(), muxedStream.getProjectionType());
                                                videoList.add(stream);
                                            }
                                        }

                                        List<AdaptiveAudioStream> audioStreamsList = videoData.getStreamingData().getAdaptiveAudioStreams();

                                        for (AdaptiveVideoStream stream : videoList) {
                                            VideoStream videoStream = new VideoStream(stream.getQualityLabel(), stream.getUrl(), stream.getiTag(), stream.getBitrate(), stream.getCodec(), stream.getFps());
                                            videoStreamArrayList.add(videoStream);
                                        }

                                    }
                                }

                                @Override
                                public void onNetworkException(YoutubeRequestException e) {
                                    
                                    e.printStackTrace();
                                    callback.onError("Something went Wrong");

                                }

                                @Override
                                public void onError(Exception exception) {
                                    
                                    exception.printStackTrace();
                                    callback.onError("Something went Wrong");

                                }
                            });

                        } catch (Exception exception) {
                            
                            exception.printStackTrace();
                            callback.onError("Something went Wrong");

                            // Something really bad happened, nothing we can do except just show some error notification to the user
                        }
                    }
                }
                else {

                    YoutubeJExtractor youtubeJExtractor = new YoutubeJExtractor();

                    try {
                        String videoId = getYouTubeId(strings[0]);
                        youtubeJExtractor.extract(videoId, new JExtractorCallback() {
                            List<AdaptiveVideoStream> videoList = null;
                            List<MuxedStream> videoListMux = null;

                            @Override
                            public void onSuccess(VideoPlayerConfig videoData) {
                                if (videoData != null) {
                                    videoList = videoData.getStreamingData().getAdaptiveVideoStreams();
                                    videoListMux = videoData.getStreamingData().getMuxedStreams();
                                    if (videoData.getVideoDetails().isLiveContent()) {
                                        videoList.clear();
                                    }
                                    else {
                                        for (AdaptiveVideoStream stream : videoList) {
                                            VideoStream videoStream = new VideoStream(stream.getQualityLabel(), stream.getUrl(), stream.getiTag(), stream.getBitrate(), stream.getCodec(), stream.getFps());
                                            videoStreamArrayList.add(videoStream);
                                        }
                                    }

                                    if (videoList == null || videoList.size() < 1) {
                                        for (MuxedStream muxedStream : videoListMux) {
                                            AdaptiveVideoStream stream = new AdaptiveVideoStream(muxedStream.getMimeType(), "", muxedStream.getBitrate(), muxedStream.getItag(), muxedStream.getUrl(), muxedStream.getAverageBitrate(), 0, 0, "", muxedStream.getQualityLabel(), muxedStream.getProjectionType());
                                            videoList.add(stream);
                                        }

                                        for (AdaptiveVideoStream stream : videoList) {
                                            VideoStream videoStream = new VideoStream(stream.getQualityLabel(), stream.getUrl(), stream.getiTag(), stream.getBitrate(), stream.getCodec(), stream.getFps());
                                            videoStreamArrayList.add(videoStream);
                                        }
                                    }



                                }
                            }

                            @Override
                            public void onNetworkException(YoutubeRequestException e) {

                                try {
                                    NewPipe.init(DownloaderTestImpl.getInstance());
                                    YoutubeStreamExtractor streamExtractor = (YoutubeStreamExtractor) YouTube.getStreamExtractor(strings[0]);
                                    streamExtractor.fetchPage();
                                    for (org.schabi.newpipe.extractor.stream.VideoStream stream : streamExtractor.getVideoStreams()) {
                                        VideoStream videoStream = new VideoStream(stream.getResolution(), stream.getUrl(), stream.getItag(), stream.getBitrate(), stream.getCodec(), stream.getFps());
                                        videoStreamArrayList.add(videoStream);
                                    }

                                } catch (ExtractionException | IOException ex) {
                                    callback.onError("Something went wrong....");
                                }
                            }

                            @Override
                            public void onError(Exception exception) {

                                try {
                                    NewPipe.init(DownloaderTestImpl.getInstance());
                                    YoutubeStreamExtractor streamExtractor = (YoutubeStreamExtractor) YouTube.getStreamExtractor(strings[0]);
                                    streamExtractor.fetchPage();
                                    for (org.schabi.newpipe.extractor.stream.VideoStream stream : streamExtractor.getVideoStreams()) {
                                        VideoStream videoStream = new VideoStream(stream.getResolution(), stream.getUrl(), stream.getItag(), stream.getBitrate(), stream.getCodec(), stream.getFps());
                                        videoStreamArrayList.add(videoStream);
                                    }

                                } catch (ExtractionException | IOException ex) {
                                    callback.onError("Something went wrong....");
                                }

                            }
                        });

                    } catch (Exception exception) {
                        try {
                            NewPipe.init(DownloaderTestImpl.getInstance());
                            YoutubeStreamExtractor streamExtractor = (YoutubeStreamExtractor) YouTube.getStreamExtractor(strings[0]);
                            streamExtractor.fetchPage();
                            for (org.schabi.newpipe.extractor.stream.VideoStream stream : streamExtractor.getVideoStreams()) {
                                VideoStream videoStream = new VideoStream(stream.getResolution(), stream.getUrl(), stream.getItag(), stream.getBitrate(), stream.getCodec(), stream.getFps());
                                videoStreamArrayList.add(videoStream);
                            }

                        } catch (ExtractionException | IOException ex) {
                            callback.onError("Something went wrong....");
                        }

                        // Something really bad happened, nothing we can do except just show some error notification to the user
                    }
                }

            }
            else {
                Log.d("Empty", "Url is Empty");
                callback.onError("Url is Empty");
            }

            return videoStreamArrayList;

        }

        @Override
        protected void onPostExecute(List<VideoStream> videoStreams) {
            
            super.onPostExecute(videoStreams);
            callback.onSuccess(videoStreams);


        }
    }

    private static String getYouTubeId(String youTubeUrl) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "error";
        }
    }

}
