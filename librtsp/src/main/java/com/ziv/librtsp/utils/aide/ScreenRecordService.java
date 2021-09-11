package com.ziv.librtsp.utils.aide;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;

import androidx.appcompat.app.AlertDialog;

import com.ziv.librtsp.RtspServer;
import com.ziv.librtsp.rtsp.RtspService;
import com.ziv.librtsp.stream.h264.DataUtil;
import com.ziv.librtsp.stream.h264.H264Data;
import com.ziv.librtsp.stream.h264.H264DataCollector;
import com.ziv.librtsp.utils.LogUtil;
import com.ziv.librtsp.utils.ToastUtil;

public class ScreenRecordService extends Service implements H264DataCollector {
    private RtspService mRtspServer = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, RtspService.class);
        bindService(intent, mRtspServiceConnection, BIND_AUTO_CREATE);
        MediaProjection mediaProjection = RtspServer.getInstance().getMediaProjection();
        ScreenRecordThread screenRecordThread = new ScreenRecordThread(getApplicationContext(), mediaProjection, this);
        screenRecordThread.start();
    }

    private final ServiceConnection mRtspServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mRtspServer = ((RtspService.LocalBinder) iBinder).getService();
//            mRtspServer.addCallbackListener(mRtspCallbackListener);
            mRtspServer.start();
            LogUtil.d("$TAG onServiceConnected finish.");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // mRtspServer.stop();
        }
    };

    private final RtspService.CallbackListener mRtspCallbackListener = new RtspService.CallbackListener() {
        @Override
        public void onError(RtspService server, Exception e, int error) {
            // We alert the user that the port is already used by another app.
            if (error == RtspService.ERROR_BIND_FAILED) {
                new AlertDialog.Builder(getApplicationContext())
                        .setTitle("端口被占用用")
                        .setMessage("你需要选择另外一个端口")
                        .show();
            }
        }

        @Override
        public void onMessage(RtspService server, int message) {
            if (message == RtspService.MESSAGE_STREAMING_STARTED) {
                ToastUtil.showShort(getApplicationContext(), "用户接入，推流开始");
            } else if (message == RtspService.MESSAGE_STREAMING_STOPPED) {
                ToastUtil.showShort(getApplicationContext(), "推流结束");
            }
        }
    };

    private long lastTime = 0L;
    private int fps = 0;
    @Override
    public void collect(H264Data data) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > 1000 + lastTime) {
            LogUtil.d("FPS: (IN) " + fps);
            fps = 0;
            lastTime = currentTime;
        } else {
            fps++;
        }
        // 数据分发
        DataUtil.getInstance().putData(data);
    }
}
