package com.zbx.librtsp.stream.codec;

import android.media.MediaCodec;

import com.zbx.librtsp.stream.h264.H264DataCollector;

public abstract class MediaCodecBase {

    protected MediaCodec mEncoder;

    protected volatile boolean isRun = false;

    public abstract void prepare();

    public abstract void release();

    protected H264DataCollector mH264Collector;

}
