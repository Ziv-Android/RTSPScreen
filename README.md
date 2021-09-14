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

## 接入指南
1. 设置配置信息（可选）

2. 启动服务

3. 获取服务地址（可选）
播放地址为：rtsp://设备IP:端口号

4. 启动支持RTSP协议的客户端/播放器请求

5. 停止服务

## 通过日志排查问题
1. 过滤 Permission ，排除权限问题
`D/PermissionActivity: Permission success.`

2. 过滤 MediaCodec ，排除视频编码服务问题
`E/VideoMediaCodec: MediaCodec prepare.`

3. 过滤 FPS ，排除视频流输入输出问题
```
D/default: FPS: (IN) 27
D/default: FPS: (OUT) 27
```
没有请求接入时，只有IN，且值应为设置帧率附近
有几个请求接入，就有几个OUT输出，请求断开后OUT停止

4. 过滤 RtspServer ，排除服务没有启动问题和请求报文及状态异常问题
`I/RtspServer: RTSP server listening on port 1234`

启动连接后
```
I/RtspServer: Connection from 192.168.16.9
E/RtspServer: OPTIONS rtsp://192.168.17.138:1024
D/RtspServer: RTSP/1.0 200 OK
    Server: RTSP Server
    Cseq: 2
    Content-Length: 0
    Public: DESCRIBE,SETUP,TEARDOWN,PLAY,PAUSE
E/RtspServer: DESCRIBE rtsp://192.168.17.138:1024
D/RtspServer: RTSP/1.0 200 OK
    Server: RTSP Server
    Cseq: 3
    Content-Length: 245
    Content-Base: 192.168.17.138:1024/
    Content-Type: application/sdp

    v=0
    o=- 0 0 IN IP4 192.168.17.138
    s=Unnamed
    i=N/A
    c=IN IP4 192.168.16.9
    t=0 0
    a=recvonly
    m=video 5006 RTP/AVP 96
    a=rtpmap:96 H264/90000
    a=fmtp:96 packetization-mode=1;profile-level-id=000042;sprop-parameter-sets=;
    a=control:trackID=1
E/RtspServer: SETUP 192.168.17.138:1024/trackID=1
D/RtspServer: RTSP/1.0 200 OK
    Server: RTSP Server
    Cseq: 4
    Content-Length: 0
    Transport: RTP/AVP/UDP;unicast;destination=192.168.16.9;client_port=5006-5007;server_port=54158-37864;ssrc=13372d1a;mode=play
    Session: 1185d20035702ca
    Cache-Control: no-cache
E/RtspServer: PLAY 192.168.17.138:1024/
D/RtspServer: RTSP/1.0 200 OK
    Server: RTSP Server
    Cseq: 5
    Content-Length: 0
    RTP-Info: url=rtsp://192.168.17.138:1024/trackID=1;seq=0
    Session: 1185d20035702ca
```
服务断开
```
E/RtspServer: TEARDOWN 192.168.17.138:1024/
D/RtspServer: RTSP/1.0 200 OK
    Server: RTSP Server
    Cseq: 6
    Content-Length: 0
I/RtspServer: Client disconnected
```

VLC播放器异常终止后再次播放错误，需要结束所有VLC相关进程后，重试
```
E/RtspServer: Error parsing CSeq: Attempt to read from field 'java.util.HashMap com.zbx.librtsp.rtsp.RtspService$Request.headers' on a null object reference
D/RtspServer: RTSP/1.0 400 Bad Request
    Server: RTSP Server
    Content-Length: 0
```
