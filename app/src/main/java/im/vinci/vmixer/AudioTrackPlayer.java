package im.vinci.vmixer;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioTrackPlayer {
    private final static String TAG = AudioTrackPlayer.class.getSimpleName();
    private AudioTrack mAudio;
    private int mMinBuffetSize;
    private int mSampleRate;
    private int mBits;
    private int mChannels;

    //write to file
    private boolean isWriteToFile = true;
    private FileOutputStream mFos;
    private String mMixPath = "/mnt/sdcard/mix/mixer.pcm";

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public AudioTrackPlayer(int sampleRate, int bits, int channels){

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
    }

    public void start(){
        mAudio.play();
        if (isWriteToFile){
            File file = new File(mMixPath);
            if (file.exists()) {
                file.delete();
            }
            try {
                mFos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void play(byte[] buffer, int size){
        int ret = mAudio.write(buffer, 0, size);
        Logger.i(TAG, "play [%d], size[%d]", ret, size);
        if (isWriteToFile && mFos != null){
            try {
                mFos.write(buffer, 0, size);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void stop(){
        Logger.d(TAG, "audio track stop...");
        mAudio.stop();

        if (isWriteToFile){
            if (mFos == null){
                return;
            }
            try {
                mFos.flush();
                mFos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}