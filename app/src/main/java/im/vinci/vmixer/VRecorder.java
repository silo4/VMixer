package im.vinci.vmixer;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.os.Process.THREAD_PRIORITY_DEFAULT;

/**
 * Created by zhonglz on 16/4/12.
 */
public class VRecorder {

    private final String TAG = VRecorder.class.getSimpleName();

    private AudioRecord mRecord;
    private boolean isRecording;
    private RecordListener listener;

    int mSampleRate;//采样率
    int mChannels;//声道
    int mBits;//位数

    int bufferSize;

    HandlerThread mThread;
    Handler mHandler;

    public VRecorder(int sampleRate, int bits, int channels){

        mSampleRate = sampleRate;
        mBits = bits;
        mChannels = channels;

        int pcmBit;
        if (mBits == 16){
            pcmBit = AudioFormat.ENCODING_PCM_16BIT;
        }else if(mBits == 8){
            pcmBit = AudioFormat.ENCODING_PCM_8BIT;
        }else {
            pcmBit = AudioFormat.ENCODING_PCM_16BIT;
        }

        int pcmChannel;
        if (mChannels == 1){
            pcmChannel = AudioFormat.CHANNEL_OUT_MONO;
        }else if(mChannels == 2){
            pcmChannel = AudioFormat.CHANNEL_OUT_STEREO;
        }else {
            pcmChannel = AudioFormat.CHANNEL_OUT_STEREO;
        }

        bufferSize = AudioRecord.getMinBufferSize(mSampleRate, pcmChannel, pcmBit);
        mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                mSampleRate,
                pcmChannel,
                pcmBit,
                bufferSize);

        mThread = new HandlerThread(TAG, THREAD_PRIORITY_DEFAULT);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
    }

    public void start(){
        if (mRecord == null){
            Logger.e(TAG, "record is null");
            return;
        }

        isRecording = true;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mRecord.startRecording();
            }
        });
    }

    public void stop(){
        if (mRecord == null){
            return;
        }
        isRecording = false;
        mRecord.stop();
    }

    public int getData(byte[] outputData, int size){

        if (!isRecording){
            return 0;
        }

        if (mRecord == null){
            return 0;
        }

        int ret = mRecord.read(outputData, 0, size);

        return ret;
    }

    private void finishRecord(boolean succeed, String err){
        if (listener == null){
            return;
        }

        if (succeed){
            listener.onSuccess();
        }else {
            listener.onFailed(err);
        }
    }

}
