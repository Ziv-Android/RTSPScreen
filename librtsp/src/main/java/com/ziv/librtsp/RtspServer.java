package com.ziv.librtsp;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;

import com.ziv.librtsp.config.Constant;
import com.ziv.librtsp.rtsp.RtspService;
import com.ziv.librtsp.utils.NetworkUtil;
import com.ziv.librtsp.utils.ToastUtil;
import com.ziv.librtsp.utils.aide.PermissionActivity;

public class RtspServer {
    private volatile static boolean mIsRun = false;
    private MediaProjection mMediaProjection = null;

    private RtspServer() {
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

    public void setRtspPort(int port) {
        Constant.DEFAULT_RTSP_PORT = port;
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

    public String getRtspAddress(Context context) {
        Context applicationContext = context.getApplicationContext();
        StringBuilder ipaddress = new StringBuilder();
        String ip = NetworkUtil.displayIpAddress(applicationContext);
        ipaddress.append("rtsp://").append(ip).append(":").append(Constant.DEFAULT_RTSP_PORT);
        return ipaddress.toString();
    }

    public void release(Context context) {
        try {
            Context applicationContext = context.getApplicationContext();
            Intent rtspServerIntent = new Intent(applicationContext, RtspService.class);
            applicationContext.stopService(rtspServerIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
