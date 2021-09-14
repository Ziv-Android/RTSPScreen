# RTSP Model封装

## 基本功能介绍
将Android设备屏幕数据通过RTSP服务发送至外部，同一局域网环境下通过支持RTSP客户端播放。

### 使用环境
Android 5.0以上

## 功能API
### 获取服务地址
RtspServer.getInstance().getRtspAddress(context)

### 设置可配置信息
RtspServer.Builder()
.setPort(int) // 可用端口号：1024~5000，可不设置，默认端口号1234
.setResolution(int, int) // 分辨率（width, height）
.setBitRate(int) // 比特率
.setFrameRate(int) // 帧率：15~25
.build()
注：配置信息需要在启动之前完成

### 启动服务
RtspServer.getInstance().start(context)

### 获取服务状态
int state = RtspServer.getInstance().getState()

返回值：
RtspServer.STATE_INIT = 0;
RtspServer.STATE_START = 1;
RtspServer.STATE_RELEASE = -1;
RtspServer.STATE_PERMISSION_DENIED = -2;
RtspServer.STATE_UNKNOWN = -999;

### 停止服务
RtspServer.getInstance().release(context)

## 分辨率与码率之间的关系
https://support.google.com/youtube/answer/2853702?hl=zh-Hans&ref_topic=9257892
240p -> 426*240 -> 300~700Kbps
360p -> 640*360 -> 400~1000Kbps
480p -> 854*480 -> 500~2000Kbps
720p -> 1280*720 -> 1500~4000Kbps
1080p -> 1920*1080 -> 3000~6000Kbps
4K -> 3840*2160 -> 13000~34000Kbps

## 编译aar
./gradlew bundleDebugAar
./gradlew bundleReleaseAar