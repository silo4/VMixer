package im.vinci.vmixer;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestAudioPlayer {
    private final static String TAG = TestAudioPlayer.class.getSimpleName();
    private AudioTrack mAudio;
    private int mMinBuffetSize;
    private int mSampleRate;
    private int mBits;
    private int mChannels;

    private HandlerThread mTestMixThread;
    private Handler mTestMixHandler;

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public TestAudioPlayer(int sampleRate, int bits, int channels){

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

        mMinBuffetSize = AudioTrack.getMinBufferSize(mSampleRate, pcmChannel, pcmBit);

        mAudio = new AudioTrack(AudioManager.STREAM_MUSIC,
                                mSampleRate,
                                pcmChannel,
                                pcmBit,
                                mMinBuffetSize,
                                AudioTrack.MODE_STREAM);

        mTestMixThread = new HandlerThread("TestAudioPlayer");
        mTestMixThread.start();
        mTestMixHandler = new Handler(mTestMixThread.getLooper());
    }


    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void play(){
        Logger.d(TAG, "audio track play begin...");

        mTestMixHandler.post(mPlayRunnable);
    }

    private Runnable mPlayRunnable = new Runnable() {
        @Override
        public void run() {
            FileInputStream fis = null;
            String path = "/mnt/sdcard/mix/mixer.pcm";
            File file = new File(path);
            if (!file.exists()) {
                Logger.e(TAG, "file not exist");
                return;
            }
            try {
                mAudio.play();

                fis = new FileInputStream(file);
                byte[] buffer = new byte[512];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    mAudio.write(buffer, 0, read);
                    Logger.d(TAG, "write to file : %d", read);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void stop(){
        Logger.d(TAG, "audio track stop...");
        mAudio.stop();
    }
}