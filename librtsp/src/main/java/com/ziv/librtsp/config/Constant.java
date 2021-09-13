package com.ziv.librtsp.config;

import android.media.projection.MediaProjection;

public class Constant {
    public static final String MIME_TYPE = "video/avc";

    public static final int VIDEO_WIDTH = 800; //横屏w 和 h互换
    public static final int VIDEO_HEIGHT = 1280;

    public static int VIDEO_BITRATE = 2000 * 1000;
    public static int VIDEO_FRAMERATE = 25;
    public static int VIDEO_IFRAME_INTER = 1;

    public static int DEFAULT_RTSP_PORT = 1234;

    public static MediaProjection mMediaProjection = null;
}
