package com.ziv.librtsp.stream.codec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import com.ziv.librtsp.config.Constant;
import com.ziv.librtsp.stream.h264.H264Data;
import com.ziv.librtsp.stream.h264.H264DataCollector;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoMediaCodec extends MediaCodecBase {

    private final static String TAG = "VideoMediaCodec";
    private Surface mSurface;
    private long startTime = 0;
    private int TIMEOUT_USEC = 11000;
    public byte[] configbyte;
    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test1.h264";
    private BufferedOutputStream outputStream;
    FileOutputStream outStream;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private long timeStamp = 0;

    private Context context;
    private void createfile(){
        File file = new File(path);
        if(file.exists()){
            file.delete();
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     * **/
    public VideoMediaCodec(WindowManager wm, Context context, H264DataCollector mH264Collector){
        this.mH264Collector = mH264Collector;
        this.context = context;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        //createfile();
        prepare();
    }

    public Surface getSurface(){
        return mSurface;
    }

    public void isRun(boolean isR){
        this.isRun = isR;
    }


    @Override
    public void prepare(){
        try{

            MediaFormat format = MediaFormat.createVideoFormat(Constant.MIME_TYPE, Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, Constant.VIDEO_BITRATE);// 比特率
            format.setInteger(MediaFormat.KEY_FRAME_RATE, Constant.VIDEO_FRAMERATE);// 帧率
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Constant.VIDEO_IFRAME_INTER); // K帧间隔
            // -----------------ADD BY XU.WANG 当画面静止时,重复最后一帧--------------------------------------------------------
            format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, Constant.VIDEO_BITRATE / Constant.VIDEO_FRAMERATE);
            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR); // 动态帧率
            format.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR); // 固定帧率
            format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh); // Profile HIGH　
            format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel31); // Level 3.1
            mEncoder = MediaCodec.createEncoderByType(Constant.MIME_TYPE);
            mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mSurface = mEncoder.createInputSurface();
            timeStamp = System.currentTimeMillis();
            mEncoder.start();
        }catch (IOException e){

        }
    }

    @Override
    public void release() {
        this.isRun = false;
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }

    }


    /**
     * 获取h264数据
     * **/
    public void getBuffer(){
        try {
            while(isRun){
                if(mEncoder == null)
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

                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
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
                    if (mH264Collector != null){
                        mH264Collector.collect(data);
                    }
                }

                while (outputBufferIndex >= 0){
                    ByteBuffer outputBuffer = mEncoder.getOutputBuffer(outputBufferIndex);

//                    MediaFormat bufferFormat = mEncoder.getOutputFormat(outputBufferIndex);

                    byte[] outData = new byte[mBufferInfo.size];
                    outputBuffer.get(outData);
                    if(mBufferInfo.flags == 2){
                        configbyte = new byte[mBufferInfo.size];
                        configbyte = outData;
                    }else if(mBufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME){
                        byte[] keyframe = new byte[mBufferInfo.size + configbyte.length];
                        System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                        System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                        H264Data data = new H264Data(keyframe, 1, mBufferInfo.presentationTimeUs*1000);
                        if (mH264Collector != null){
                            mH264Collector.collect(data);
                        }
                    }else{
                        H264Data data = new H264Data(outData, 2, mBufferInfo.presentationTimeUs*1000);
                        if (mH264Collector != null){
                            mH264Collector.collect(data);
                        }
                    }
                    mEncoder.releaseOutputBuffer(outputBufferIndex, true);
                    outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        try {
            if (mEncoder != null) {
                mEncoder.stop();
                mEncoder.release();
            }
            mEncoder = null;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
