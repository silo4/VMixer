package im.vinci.vmixer;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.os.Process.THREAD_PRIORITY_DEFAULT;

/**
 * Created by zhonglz on 2016/11/25.
 */
public class VTrackProcessor {
    private final static String TAG = VTrackProcessor.class.getSimpleName();
    private final static int TRACK_BUFFER_SIZE = 10 * 1024;

    private String mPath = "/mnt/sdcard/mix/dukou.wav";
    private FileInputStream mFileInputStream;
    private boolean isProcessing = false;
    private ByteRingBuffer mTrackBuffer;

    private HandlerThread mThread;
    private Handler mHandler;

    public VTrackProcessor(){
        mThread = new HandlerThread(TAG, THREAD_PRIORITY_DEFAULT);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
    }

    public void start(){
        if (TextUtils.isEmpty(mPath)){
            Logger.e(TAG, "track path is empty");
            return;
        }

        try {
            isProcessing = true;
            mFileInputStream = new FileInputStream(mPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (mTrackBuffer == null){
//                    mTrackBuffer = new ByteRingBuffer(TRACK_BUFFER_SIZE);
//                }else {
//                    mTrackBuffer.clear();
//                }
//                isProcessing = true;
//
//                try {
//                    mFileInputStream = new FileInputStream(mPath);
//                    byte[] readBuf = new byte[512];
//                    int realRead;
//                    while ((realRead = mFileInputStream.read(readBuf)) != -1 && isProcessing){
//                        mTrackBuffer.write(readBuf, 0, realRead);
//                    }
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }finally {
//                    if (mFileInputStream != null){
//                        try {
//                            mFileInputStream.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        mFileInputStream = null;
//                    }
//                }
//
//            }
//        });


    }

    public void stop(){
        isProcessing = false;

        if (mFileInputStream != null) {
            try {
                mFileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFileInputStream = null;
        }
    }

    public void setTrackPath(String path){
        mPath = path;
    }

    public int getData(byte[] outputData, int size){

        if (!isProcessing){
            return 0;
        }

        if (mFileInputStream == null){
            return 0;
        }

//        int ret = mTrackBuffer.read(outputData, 0, size);
        int ret;
        try {
            ret = mFileInputStream.read(outputData, 0, size);

            if (ret == -1){
                Logger.e(TAG, "track file end....");
                stop();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        return ret;
    }


}
