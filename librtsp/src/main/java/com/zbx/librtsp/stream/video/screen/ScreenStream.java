package com.zbx.librtsp.stream.video.screen;

import com.zbx.librtsp.stream.h264.H264Packetizer;
import com.zbx.librtsp.stream.video.VideoStream;

import java.io.IOException;

/**
 * 将经过MediaCodec编码的视频流 封包H.264
 */
public class ScreenStream extends VideoStream {
    public ScreenStream(){
        mPacketizer = new H264Packetizer();
    }

    public synchronized void start() throws IllegalStateException, IOException {
        if (!mStreaming) {
            configure();
            super.start();
        }
    }

    /**
     * Configures the stream. You need to call this before calling {@link #getSessionDescription()} to apply
     * your configuration of the stream.
     */
    public synchronized void configure() throws IllegalStateException, IOException {
        super.configure();

    }

    @Override
    public String getSessionDescription() throws IllegalStateException {
        return "m=video "+ String.valueOf(getDestinationPorts()[0])+" RTP/AVP 96\r\n" +
                "a=rtpmap:96 H264/90000\r\n" +
                "a=fmtp:96 packetization-mode=1;profile-level-id=000042"+";sprop-parameter-sets="+";\r\n";
    }
}
