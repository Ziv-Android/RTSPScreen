package com.zbx.librtsp.stream.codec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.zbx.librtsp.config.Constant;
import com.zbx.librtsp.stream.h264.H264Data;
import com.zbx.librtsp.stream.h264.H264DataCollector;
import com.zbx.librtsp.utils.LogUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoMediaCodec extends MediaCodecBase {

    private static final String TAG = "VideoMediaCodec";
    private static final int TIMEOUT_USEC = 11000;

    private Surface mSurface;
    private long startTime = 0;

    public byte[] configbyte;

    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private long timeStamp = 0;

    private Context context;

    private void createfile() {
        FileOutputStream outStream;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test1.h264";
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     **/
    public VideoMediaCodec(WindowManager wm, Context context, H264DataCollector mH264Collector) {
        this.mH264Collector = mH264Collector;
        this.context = context;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        // createfile();
        prepare();
    }

    public Surface getSurface() {
        return mSurface;
    }

    public void isRun(boolean isR) {
        this.isRun = isR;
    }

    @Override
    public void prepare() {
        try {
            LogUtil.d(TAG, "MediaCodec prepare.");
            MediaFormat format = MediaFormat.createVideoFormat(Constant.MIME_TYPE, Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, Constant.VIDEO_BITRATE);// ?????????
            format.setInteger(MediaFormat.KEY_FRAME_RATE, Constant.VIDEO_FRAME_RATE);// ??????
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Constant.VIDEO_IFRAME_INTER); // K??????????????? ??????s
            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR); // ????????????

            // -----------------ADD BY XU.WANG ??????????????????,??????????????????--------------------------------------------------------
            format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, Constant.VIDEO_BITRATE / Constant.VIDEO_FRAME_RATE);

//            format.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR); // ????????????
//            format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline); // Profile HIGH???or Baseline
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel31); // Level 3.1
//            }
//            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);

            mEncoder = MediaCodec.createEncoderByType(Constant.MIME_TYPE);
            mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mSurface = mEncoder.createInputSurface();
            timeStamp = System.currentTimeMillis();
            mEncoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() {
        this.isRun = false;
        LogUtil.d(TAG, "execute release.");
        if (mEncoder != null) {
            try {
                mEncoder.stop();
                mEncoder.release();
            } catch (Exception e) {
            } finally {
                mEncoder = null;
            }
        }
        mH264Collector = null;
    }


    /**
     * ??????h264??????
     **/
    public void getBuffer() {
        try {
            while (isRun) {
                if (mEncoder == null)
                    break;
                if (startTime == 0) {
                    startTime = mBufferInfo.presentationTimeUs * 1000;
                }

                if (System.currentTimeMillis() - timeStamp >= 1000) {
                    timeStamp = System.currentTimeMillis();
                    Bundle params = new Bundle();
                    params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 2);
                    mEncoder.setParameters(params);
                }
                int outputBufferIndex  = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
//                LogUtil.d("getBuffer outputBufferIndex " + outputBufferIndex);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat outputFormat = mEncoder.getOutputFormat();
                    byte[] AVCDecoderConfigurationRecord = Packager.H264Packager.generateAVCDecoderConfigurationRecord(outputFormat);
                    int packetLen = Packager.FLVPackager.FLV_VIDEO_TAG_LENGTH +
                            AVCDecoderConfigurationRecord.length;
                    byte[] finalBuff = new byte[packetLen];
                    Packager.FLVPackager.fillFlvVideoTag(finalBuff,
                            0,
                            true,
                            true,
                            AVCDecoderConfigurationRecord.length);
                    System.arraycopy(AVCDecoderConfigurationRecord, 0,
                            finalBuff, Packager.FLVPackager.FLV_VIDEO_TAG_LENGTH, AVCDecoderConfigurationRecord.length);

                    H264Data data = new H264Data(finalBuff, 1, 10);
                    if (mH264Collector != null) {
                        mH264Collector.collect(data);
                    }
                }

                while (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = mEncoder.getOutputBuffer(outputBufferIndex);
//                    MediaFormat bufferFormat = mEncoder.getOutputFormat(outputBufferIndex);

                    byte[] outData = new byte[mBufferInfo.size];
                    outputBuffer.get(outData);
                    if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                        configbyte = new byte[mBufferInfo.size];
                        configbyte = outData;
                    } else if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                        // ????????????????????????
                        byte[] keyframe = new byte[mBufferInfo.size + configbyte.length];
                        System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                        System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                        H264Data data = new H264Data(keyframe, 1, mBufferInfo.presentationTimeUs * 1000);
                        if (mH264Collector != null) {
                            mH264Collector.collect(data);
                        }
                    } else {
                        H264Data data = new H264Data(outData, 2, mBufferInfo.presentationTimeUs * 1000);
                        if (mH264Collector != null) {
                            mH264Collector.collect(data);
                        }
                    }
                    mEncoder.releaseOutputBuffer(outputBufferIndex, true);
                    outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        release();
    }

}
