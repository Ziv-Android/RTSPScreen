package com.zbx.librtsp;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;

import com.zbx.librtsp.config.Constant;
import com.zbx.librtsp.rtsp.RtspService;
import com.zbx.librtsp.utils.NetworkUtil;
import com.zbx.librtsp.utils.ToastUtil;
import com.zbx.librtsp.utils.aide.PermissionActivity;

public class RtspServer {
    public static final int STATE_INIT = 0;
    public static final int STATE_START = 1;
    public static final int STATE_RELEASE = -1;
    public static final int STATE_PERMISSION_DENIED = -2;
    public static final int STATE_UNKNOWN = -999;

    public volatile static int mServerState = STATE_UNKNOWN;
    private MediaProjection mMediaProjection = null;

    private RtspServer() {
    }

    public static final class Builder {
        private int width = Constant.VIDEO_WIDTH; //横屏w 和 h互换
        private int height = Constant.VIDEO_HEIGHT;

        private int bitRate = Constant.VIDEO_BITRATE;
        private int frameRate = Constant.VIDEO_FRAME_RATE;

        private int port = Constant.DEFAULT_RTSP_PORT;
        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setResolution(int width, int height) {
            this.width= width;
            this.height= height;
            return this;
        }

        public Builder setBitRate(int bitRate) {
            this.bitRate = bitRate;
            return this;
        }

        public Builder setFrameRate(int frameRate) {
            this.frameRate = frameRate;
            return this;
        }

        public void build(){
            Constant.VIDEO_WIDTH = width;
            Constant.VIDEO_HEIGHT = height;

            Constant.VIDEO_BITRATE = bitRate;
            Constant.VIDEO_FRAME_RATE = frameRate;

            Constant.DEFAULT_RTSP_PORT = port;
        }
    }

    private static final class RTSPLibrary {
        private static final RtspServer INSTANCE = new RtspServer();
    }

    public static RtspServer getInstance() {
        return RTSPLibrary.INSTANCE;
    }

    public void setMediaProjection(MediaProjection mediaProjection) {
        mMediaProjection = mediaProjection;
    }

    public MediaProjection getMediaProjection() {
        return mMediaProjection;
    }

    public String getRtspAddress(Context context) {
        Context applicationContext = context.getApplicationContext();
        StringBuilder ipaddress = new StringBuilder();
        String ip = NetworkUtil.displayIpAddress(applicationContext);
        ipaddress.append("rtsp://").append(ip).append(":").append(Constant.DEFAULT_RTSP_PORT);
        return ipaddress.toString();
    }

    public void start(Context context) {
        try {
            Context applicationContext = context.getApplicationContext();
            Intent intent = new Intent();
            intent.setClass(applicationContext, PermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            applicationContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showLong(context, "PermissionActivity 未找到");
        }
    }

    public int getState() {
        return mServerState;
    }

    public void release(Context context) {
        try {
            Context applicationContext = context.getApplicationContext();
            Intent rtspServerIntent = new Intent(applicationContext, RtspService.class);
            applicationContext.stopService(rtspServerIntent);
            mServerState = STATE_RELEASE;
            mMediaProjection = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
