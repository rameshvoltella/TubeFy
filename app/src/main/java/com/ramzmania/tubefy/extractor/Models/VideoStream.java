package com.ramzmania.tubefy.extractor.Models;

public class VideoStream {

  private String qualityLabel;
  private String Url;
  private int iTag;
  private int bitrate;
  private String codec;
  private int fps;


    public VideoStream(String qualityLabel, String url, int iTag, int bitrate, String codec, int fps) {
        this.qualityLabel = qualityLabel;
        Url = url;
        this.iTag = iTag;
        this.bitrate = bitrate;
        this.codec = codec;
        this.fps = fps;
    }

    public VideoStream() {
    }

    public String getQualityLabel() {
        return qualityLabel;
    }

    public void setQualityLabel(String qualityLabel) {
        this.qualityLabel = qualityLabel;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public int getiTag() {
        return iTag;
    }

    public void setiTag(int iTag) {
        this.iTag = iTag;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }
}
