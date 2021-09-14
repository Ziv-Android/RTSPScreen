package com.zbx.librtsp.utils.aide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.zbx.librtsp.R;
import com.zbx.librtsp.RtspServer;
import com.zbx.librtsp.rtsp.RtspService;
import com.zbx.librtsp.utils.LogUtil;

public class PermissionActivity extends AppCompatActivity {
    private static final String TAG = "PermissionActivity";
    private static final int REQUEST_CODE = 1002;

    private MediaProjectionManager mMediaProjectionManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        LogUtil.d(TAG, "onCreate");

        RtspServer.mServerState = RtspServer.STATE_INIT;

        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);

        LogUtil.d(TAG, "onCreate finish.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                LogUtil.d(TAG, "Permission success.");
                MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
                RtspServer.getInstance().setMediaProjection(mediaProjection);
                startService(new Intent(this, RtspService.class));
                finish();
            } catch (Exception e) {
                LogUtil.e(TAG, "Start screen record service error: " + e.getMessage());
                RtspServer.mServerState = RtspServer.STATE_UNKNOWN;
                e.printStackTrace();
            }
        } else {
            LogUtil.e(TAG, "Permission error: 未获得屏幕读取权限.");
            RtspServer.mServerState = RtspServer.STATE_PERMISSION_DENIED;
        }
    }
}
