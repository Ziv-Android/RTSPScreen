package com.zbx.rtspscreen


import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

import com.zbx.librtsp.RtspServer
import java.lang.Exception
import android.util.DisplayMetrics
import android.view.*
import android.widget.Button


class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private var mSurfaceView: View? = null
    private var mIpView: TextView? = null
    private var mFpsView: TextView? = null

    private var mCamera: Camera? = null
    private var mParameters: Camera.Parameters? = null
    private var mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
    private var mWidth = 320
    private var mHeight = 480
    private var mOrientation = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        mSurfaceView = findViewById(R.id.virtual_surface_view)

        startCamera()

        mIpView = findViewById(R.id.txt_address)
        mFpsView = findViewById(R.id.txt_fps)

        RtspServer.Builder().setPort(1024).build()
        RtspServer.getInstance()?.start(this)

        mIpView?.text = RtspServer.getInstance()?.getRtspAddress(this)
    }

    override fun onDestroy() {
        RtspServer.getInstance()?.release(this)
        super.onDestroy()
    }

    private fun startCamera() {
        if (mSurfaceView is TextureView) {
            val dm = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(dm)
            mWidth = dm.widthPixels
            mHeight = dm.heightPixels

            Log.d(TAG, "##### init screen width: $mWidth, height: $mHeight")

            val view = mSurfaceView as TextureView
            view.surfaceTextureListener = textureViewSurfaceTextureListener
            // ??????TextureView??????????????????
            view.scaleX = -1F
        } else if (mSurfaceView is SurfaceView) {
            val view = mSurfaceView as SurfaceView
            view.holder?.addCallback(surfaceHolderCallback)
        }
    }

    private val textureViewSurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(
            surfaceTexture: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            startPreview()
        }

        override fun onSurfaceTextureSizeChanged(
            surfaceTexture: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            Log.d(TAG, "onSurfaceTextureSizeChanged: $width $height")
        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            releaseCamera()
            return false
        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
    }

    private val surfaceHolderCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            startPreview()
        }

        override fun surfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            Log.d(TAG, "surfaceChanged: $width $height")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            releaseCamera()
        }
    }

    private fun releaseCamera() {
        mCamera?.let {
            it.stopPreview()
            it.stopFaceDetection()
            it.setPreviewCallback(null)
            it.release()
        }
        mCamera = null
    }

    private fun startPreview() {
        try {
            if (mCamera == null) {
                createCamera()
            }
            if (mSurfaceView is TextureView) {
                val view = mSurfaceView as TextureView
                mCamera?.setPreviewTexture(view.surfaceTexture)
            } else if (mSurfaceView is SurfaceView){
                val view = mSurfaceView as SurfaceView
                mCamera?.setPreviewDisplay(view.holder)
            }
            setCameraDisplayOrientation()
            mCamera?.startPreview()

            startFaceDetect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createCamera() {
        try {
            if (isSupport(mCameraId)) {
                mCamera = Camera.open(mCameraId)
                initParameters(mCamera)

                //preview
                if (null != mCamera) {
                    mCamera!!.setPreviewCallback { data, camera ->
//                        Log.d(TAG, "setPreviewCallback camera: $camera, data: ${data?.size}")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startFaceDetect() {
        mCamera?.startFaceDetection()
        mCamera?.setFaceDetectionListener(object : Camera.FaceDetectionListener {
            override fun onFaceDetection(faces: Array<out Camera.Face>?, camera: Camera?) {
                Log.d("DEBUG", "##### face length: " + faces?.size)
            }
        })
    }

    private fun setCameraDisplayOrientation() {
        val cameraInfo = CameraInfo()
        Camera.getCameraInfo(mCameraId, cameraInfo)
        val rotation = windowManager.defaultDisplay.rotation //????????????
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        Log.d("DEBUG", "##### setCameraDisplayOrientation degrees: $degrees")

        degrees = when (mCameraId) {
            Camera.CameraInfo.CAMERA_FACING_FRONT -> 270
            Camera.CameraInfo.CAMERA_FACING_BACK -> 90
            else -> 0
        }

        var result: Int
        //cameraInfo.orientation ??????????????????
        //cameraInfo.orientation ??????????????????
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360
            result = (360 - result) % 360
        } else {
            result = (cameraInfo.orientation - degrees + 360) % 360
        }

        Log.d(
            "DEBUG", "##### setCameraDisplayOrientation rotation: ${
                rotation
            }, cameraInfo.orientation: ${cameraInfo.orientation}, result: $result"
        )

        mOrientation = result
        //??????????????????
        //??????????????????
        mCamera?.setDisplayOrientation(result)
    }

    private fun initParameters(camera: Camera?) {
        mParameters = camera?.parameters
        mParameters?.let {
            it.previewFormat = ImageFormat.NV21
            it.supportedPreviewFormats
            it.supportedPictureFormats
        }

        setPreviewSize()
        // setPictureSize()
    }

    private fun setPreviewSize() {
        val supportSizes = mParameters?.supportedPreviewSizes
        var biggestSize: Camera.Size? = null
        var fitSize: Camera.Size? = null
        var targetSize: Camera.Size? = null
        var targetSiz2: Camera.Size? = null

        if (null != supportSizes) {
            for (i in 0 until supportSizes.size) {
                val size = supportSizes[i]
                Log.d(
                    "DEBUG",
                    "###### SupportedPreviewSizes: width=" + size.width + ", height=" + size.height
                )
                if (biggestSize == null ||
                    (size.width >= biggestSize.width && size.height >= biggestSize.height)
                ) {
                    biggestSize = size
                }

                if (size.width == mWidth
                    && size.height == mHeight
                ) {
                    fitSize = size
                    //????????????????????????????????????????????????
                } else if (size.width == mWidth
                    || size.height == mHeight
                ) {
                    if (targetSize == null) {
                        targetSize = size
                        //?????????????????????????????? ??????????????????????????????????????????
                    } else if (size.width < mWidth
                        || size.height < mHeight
                    ) {
                        targetSiz2 = size
                    }
                }
            }


            if (fitSize == null) {
                fitSize = targetSize
            }

            if (fitSize == null) {
                fitSize = targetSiz2
            }

            if (fitSize == null) {
                fitSize = biggestSize
            }

            Log.d(
                "DEBUG",
                "##### fitSize width: " + fitSize?.width + ", height: " + fitSize?.height
            )
            mParameters?.setPreviewSize(fitSize?.width ?: mWidth, fitSize?.height ?: mHeight)
        }
    }

    private fun isSupport(cameraId: Int): Boolean {
        val cameraInfo = Camera.CameraInfo()
        for (i in 0 until Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == cameraId) {
                return true
            }
        }
        return false
    }

    @SuppressLint("SetTextI18n")
    fun startOrStop(view: View) {
        when(RtspServer.getInstance().state){
            RtspServer.STATE_UNKNOWN, RtspServer.STATE_RELEASE -> {
                (view as Button).text = "Start"
                RtspServer.getInstance()?.start(this)
            }
            RtspServer.STATE_START -> {
                (view as Button).text = "Stop"
                RtspServer.getInstance().release(this)
            }
        }
    }

}