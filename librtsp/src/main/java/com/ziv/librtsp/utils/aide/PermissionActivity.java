package com.ziv.librtsp.utils.aide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ziv.librtsp.R;
import com.ziv.librtsp.RtspServer;
import com.ziv.librtsp.config.Constant;
import com.ziv.librtsp.rtsp.RtspService;
import com.ziv.librtsp.utils.LogUtil;

public class PermissionActivity extends AppCompatActivity {
    private static final String TAG = "PermissionActivity";
    private static final int REQUEST_CODE = 1002;

    private MediaProjectionManager mMediaProjectionManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        LogUtil.d(TAG, "onCreate");

        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
        LogUtil.d(TAG, "onCreate finish.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                Constant.mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
                startService(new Intent(this, RtspService.class));
                finish();
            } catch (Exception e) {
                LogUtil.e(TAG, "Start screen record service error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            LogUtil.e(TAG, "Permission error: 未获得屏幕读取权限.");
        }
    }
}
