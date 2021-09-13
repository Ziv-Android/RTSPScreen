package com.zbx.librtsp.stream.video.screen;

import com.zbx.librtsp.stream.h264.H264Data;
import com.zbx.librtsp.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by user111 on 2018/3/14.
 */

public class ScreenInputStream extends InputStream {

    private long ts = 0;
    private ByteBuffer mBuffer = null;

    private static int queuesize = 8 * 1024;
    private LinkedBlockingDeque<H264Data> h264Queue = new LinkedBlockingDeque<>(queuesize);
    private H264Data data = null;

    private long lastTime = 0L;
    private int fps = 0;

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        int min = 0;

        if(mBuffer == null){
            data = h264Queue.poll();
            if(data == null) return 0;
            long currentTime = System.currentTimeMillis();
            if (currentTime > 1000 + lastTime) {
                LogUtil.d("FPS: (OUT) " + fps);
                fps = 0;
                lastTime = currentTime;
            } else {
                fps++;
            }
            ts = data.ts;
            mBuffer = ByteBuffer.wrap(data.data);
            mBuffer.position(0);
        }
        min = length < data.data.length - mBuffer.position() ? length : data.data.length - mBuffer.position();
        mBuffer.get(buffer, offset, min);
        if (mBuffer.position()>=data.data.length) {
            mBuffer = null;
        }
        return min;
    }


    @Override
    public int read() throws IOException {
        return 0;
    }

    public int available() {
        if (mBuffer != null)
            return data.data.length - mBuffer.position();
        else
            return 0;
    }


    public long getLastts(){
        return ts;
    }

    public void putH264Data(H264Data data) {
        if (h264Queue == null){
            h264Queue = new LinkedBlockingDeque<>(queuesize);
        }
        if (h264Queue.size() >= queuesize) {
            h264Queue.poll();
        }
        try {
            h264Queue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
