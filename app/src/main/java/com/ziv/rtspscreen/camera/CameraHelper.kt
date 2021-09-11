//package com.ziv.rtspscreen.camera
//
//import android.graphics.ImageFormat
//import android.graphics.SurfaceTexture
//import android.hardware.Camera
//import android.util.DisplayMetrics
//import android.util.Log
//import android.view.Surface
//import android.view.SurfaceHolder
//import android.view.SurfaceView
//import android.view.TextureView
//import java.lang.Exception
//
//class CameraHelper(mSurfaceView: View, ) {
//
//    private fun startCamera() {
//        if (mSurfaceView is TextureView) {
//            val dm = DisplayMetrics()
//            windowManager.defaultDisplay.getMetrics(dm)
//            mWidth = dm.widthPixels
//            mHeight = dm.heightPixels
//
//            Log.d(TAG, "##### init screen width: $mWidth, height: $mHeight")
//
//            val view = mSurfaceView as TextureView
//            view.surfaceTextureListener = textureViewSurfaceTextureListener
//            // 只有TextureView支持设置镜像
//            view.scaleX = -1F
//        } else if (mSurfaceView is SurfaceView) {
//            val view = mSurfaceView as SurfaceView
//            view.holder?.addCallback(surfaceHolderCallback)
//        }
//    }
//
//    private val textureViewSurfaceTextureListener = object : TextureView.SurfaceTextureListener {
//        override fun onSurfaceTextureAvailable(
//            surfaceTexture: SurfaceTexture,
//            width: Int,
//            height: Int
//        ) {
//            startPreview()
//        }
//
//        override fun onSurfaceTextureSizeChanged(
//            surfaceTexture: SurfaceTexture,
//            width: Int,
//            height: Int
//        ) {
//            Log.d(TAG, "onSurfaceTextureSizeChanged: $width $height");
//        }
//
//        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
//            releaseCamera()
//            return false
//        }
//
//        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
//    }
//
//    private val surfaceHolderCallback = object : SurfaceHolder.Callback {
//        override fun surfaceCreated(holder: SurfaceHolder) {
//            startPreview()
//        }
//
//        override fun surfaceChanged(
//            holder: SurfaceHolder,
//            format: Int,
//            width: Int,
//            height: Int
//        ) {
//            Log.d(TAG, "surfaceChanged: $width $height");
//        }
//
//        override fun surfaceDestroyed(holder: SurfaceHolder) {
//            releaseCamera()
//        }
//    }
//
//    private fun releaseCamera() {
//        mCamera?.let {
//            it.stopPreview()
//            it.stopFaceDetection()
//            it.setPreviewCallback(null)
//            it.release()
//        }
//        mCamera = null
//    }
//
//    private fun startPreview() {
//        try {
//            if (mCamera == null) {
//                createCamera()
//            }
//            if (mSurfaceView is TextureView) {
//                val view = mSurfaceView as TextureView
//                mCamera?.setPreviewTexture(view.surfaceTexture)
//            } else if (mSurfaceView is SurfaceView){
//                val view = mSurfaceView as SurfaceView
//                mCamera?.setPreviewDisplay(view.holder)
//            }
//            setCameraDisplayOrientation()
//            mCamera?.startPreview()
//
//            startFaceDetect()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun createCamera() {
//        try {
//            if (isSupport(mCameraId)) {
//                mCamera = Camera.open(mCameraId)
//                initParameters(mCamera)
//
//                //preview
//                if (null != mCamera) {
//                    mCamera!!.setPreviewCallback { data, camera ->
////                        Log.d(TAG, "setPreviewCallback camera: $camera, data: ${data?.size}")
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun startFaceDetect() {
//        mCamera?.startFaceDetection()
//        mCamera?.setFaceDetectionListener(object : Camera.FaceDetectionListener {
//            override fun onFaceDetection(faces: Array<out Camera.Face>?, camera: Camera?) {
//                Log.d("DEBUG", "##### face length: " + faces?.size);
//            }
//        })
//    }
//
//    private fun setCameraDisplayOrientation() {
//        val cameraInfo = Camera.CameraInfo()
//        Camera.getCameraInfo(mCameraId, cameraInfo)
//        val rotation = windowManager.defaultDisplay.rotation //自然方向
//        var degrees = 0
//        when (rotation) {
//            Surface.ROTATION_0 -> degrees = 0
//            Surface.ROTATION_90 -> degrees = 90
//            Surface.ROTATION_180 -> degrees = 180
//            Surface.ROTATION_270 -> degrees = 270
//        }
//
//        Log.d("DEBUG", "##### setCameraDisplayOrientation degrees: $degrees")
//
//        degrees = when (mCameraId) {
//            Camera.CameraInfo.CAMERA_FACING_FRONT -> 270
//            Camera.CameraInfo.CAMERA_FACING_BACK -> 90
//            else -> 0
//        }
//
//        var result: Int
//        //cameraInfo.orientation 图像传感方向
//        //cameraInfo.orientation 图像传感方向
//        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            result = (cameraInfo.orientation + degrees) % 360
//            result = (360 - result) % 360
//        } else {
//            result = (cameraInfo.orientation - degrees + 360) % 360
//        }
//
//        Log.d(
//            "DEBUG", "##### setCameraDisplayOrientation rotation: ${
//                rotation
//            }, cameraInfo.orientation: ${cameraInfo.orientation}, result: $result"
//        )
//
//        mOrientation = result
//        //相机预览方向
//        //相机预览方向
//        mCamera!!.setDisplayOrientation(result)
//    }
//
//    private fun initParameters(camera: Camera?) {
//        mParameters = camera?.parameters
//        mParameters?.let {
//            it.setPreviewFormat(ImageFormat.NV21)
//            it.supportedPreviewFormats
//            it.supportedPictureFormats
//        }
//
//        setPreviewSize()
//        // setPictureSize()
//    }
//
//    private fun setPreviewSize() {
//        val supportSizes = mParameters?.getSupportedPreviewSizes();
//        var biggestSize: Camera.Size? = null
//        var fitSize: Camera.Size? = null
//        var targetSize: Camera.Size? = null
//        var targetSiz2: Camera.Size? = null
//
//        if (null != supportSizes) {
//            for (i in 0 until supportSizes.size) {
//                val size = supportSizes[i]
//                Log.d(
//                    "DEBUG",
//                    "###### SupportedPreviewSizes: width=" + size.width + ", height=" + size.height
//                );
//                if (biggestSize == null ||
//                    (size.width >= biggestSize.width && size.height >= biggestSize.height)
//                ) {
//                    biggestSize = size;
//                }
//
//                if (size.width == mWidth
//                    && size.height == mHeight
//                ) {
//                    fitSize = size;
//                    //如果任一宽或者高等于所支持的尺寸
//                } else if (size.width == mWidth
//                    || size.height == mHeight
//                ) {
//                    if (targetSize == null) {
//                        targetSize = size;
//                        //如果上面条件都不成立 如果任一宽高小于所支持的尺寸
//                    } else if (size.width < mWidth
//                        || size.height < mHeight
//                    ) {
//                        targetSiz2 = size;
//                    }
//                }
//            }
//
//
//            if (fitSize == null) {
//                fitSize = targetSize;
//            }
//
//            if (fitSize == null) {
//                fitSize = targetSiz2;
//            }
//
//            if (fitSize == null) {
//                fitSize = biggestSize;
//            }
//
//            Log.d(
//                "DEBUG",
//                "##### fitSize width: " + fitSize?.width + ", height: " + fitSize?.height
//            );
//            mParameters?.setPreviewSize(fitSize?.width ?: mWidth, fitSize?.height ?: mHeight);
//        }
//    }
//
//    private fun isSupport(cameraId: Int): Boolean {
//        val cameraInfo = Camera.CameraInfo()
//        for (i in 0 until Camera.getNumberOfCameras()) {
//            Camera.getCameraInfo(i, cameraInfo)
//            if (cameraInfo.facing == cameraId) {
//                return true
//            }
//        }
//        return false
//    }
//}