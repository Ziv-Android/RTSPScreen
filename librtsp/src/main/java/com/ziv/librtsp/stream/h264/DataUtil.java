package com.ziv.librtsp.stream.h264;

import java.util.concurrent.LinkedBlockingDeque;

public class DataUtil {
    private H264Data mH264Data;

    private volatile static DataUtil mDataUtil;
    private DataUtil(){}
    public static DataUtil getInstance(){
        if(mDataUtil == null){
            synchronized (DataUtil.class){
                if(mDataUtil == null){
                    mDataUtil = new DataUtil();
                }
            }
        }
        return mDataUtil;
    }

    public void putData(byte[] buffer, int type,long ts) {
        mH264Data = new H264Data(buffer, type, ts);
    }

    public H264Data getH264Data() {
        return mH264Data;
    }

    public void putData(H264Data data) {
        mH264Data = data;
    }
}
