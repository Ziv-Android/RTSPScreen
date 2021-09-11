package com.ziv.librtsp.stream.codec;

import android.media.MediaCodec;

import com.ziv.librtsp.stream.h264.H264DataCollector;

public abstract class MediaCodecBase {

    protected MediaCodec mEncoder;

    protected boolean isRun = false;

    public abstract void prepare();

    public abstract void release();

    protected H264DataCollector mH264Collector;

}
