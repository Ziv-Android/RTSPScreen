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

## 编译aar
./gradlew bundleDebugAar
./gradlew bundleReleaseAar