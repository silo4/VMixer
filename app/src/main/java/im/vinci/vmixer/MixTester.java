package im.vinci.vmixer;


/**
 * Created by zhonglz on 2016/11/25.
 */
public class MixTester {
    private final static String TAG = MixTester.class.getSimpleName();

    private final static int RECORD_STREAM = 0;
    private final static int TRACK_STREAM = 1;
    private final static int AUDIO_BUFFER_SIZE = 512;

    private int mSampleRate = VMixParams.SampleRateOptions.SAMPLERATE_44100;
    private int mChannels = VMixParams.ChannelsOptions.CHANNEL_2;
    private int mBits = VMixParams.BitsOptions.BITS_16;

    private VRecorder mRecorder;
    private AudioTrackPlayer mPlayer;
    private VTrackProcessor mTrackProcessor;
    private VMix mMix;

    private InputThread mInputThread;
    private OutputThread mOutputThread;

    private VMixListener mVixListener;

    public MixTester(){
        init();
    }

    public MixTester(VMixListener listener){
        this.mVixListener = listener;

        init();
    }

    private void init(){
        mRecorder = new VRecorder(mSampleRate, mBits, mChannels);
        mPlayer = new AudioTrackPlayer(mSampleRate, mBits, mChannels);
        mTrackProcessor = new VTrackProcessor();
        mMix = new VMix();
        mMix.Create(mSampleRate, mBits, mChannels, 2, 10 * 1024, 10 * 1024);
    }

    public void start(){
        mInputThread = new InputThread();
        mOutputThread = new OutputThread();

        mRecorder.start();
        mTrackProcessor.start();
//        mInputThread.startInput();
        mOutputThread.startOutput();

        if (mVixListener != null){
            mVixListener.onStart();
        }
    }

    public void stop(){
        mInputThread.stopInput();
        mOutputThread.stopOutput();

        mRecorder.stop();
        mPlayer.stop();
        mTrackProcessor.stop();
        mMix.reset();

        if (mVixListener != null){
            mVixListener.onStop();
        }
        mInputThread = null;
        mOutputThread = null;
    }

    public void setRecorderEnable(boolean isEnable){
        if (mMix != null){
            mMix.SetStreamValid(RECORD_STREAM, isEnable);
        }
    }

    public boolean isRecordEnable(){
        if (mMix == null){
            return false;
        }
        return mMix.isValidStream(RECORD_STREAM);
    }

    public void setTrackEnable(boolean isEnable){
        if (mMix != null){
            mMix.SetStreamValid(TRACK_STREAM, isEnable);
        }
    }

    public boolean isTrackEnable(){
        if (mMix == null){
            return false;
        }
        return mMix.isValidStream(TRACK_STREAM);
    }

    class InputThread extends Thread{
        private boolean isRunning = false;

        public InputThread(){

        }

        public void startInput(){
            isRunning = true;
            start();
        }

        public void stopInput(){
            isRunning = false;
        }

        @Override
        public void run() {
            byte[] recordBuffer = new byte[AUDIO_BUFFER_SIZE];
            byte[] trackBuffer = new byte[AUDIO_BUFFER_SIZE];
            while (isRunning){
                int realRecordSize = mRecorder.getData(recordBuffer, AUDIO_BUFFER_SIZE);
                int realTrackSize = mTrackProcessor.getData(trackBuffer, AUDIO_BUFFER_SIZE);

                mMix.SetData(RECORD_STREAM, recordBuffer, realRecordSize);
                mMix.SetData(TRACK_STREAM, trackBuffer, realTrackSize);
            }
        }
    }

    class OutputThread extends Thread{
        private boolean isRunning = false;
        public OutputThread(){

        }

        public void startOutput(){
            isRunning = true;
            start();
        }

        public void stopOutput(){
            isRunning = false;
        }

        @Override
        public void run() {
            byte[] recordBuffer = new byte[AUDIO_BUFFER_SIZE];
            byte[] trackBuffer = new byte[AUDIO_BUFFER_SIZE];
            byte[] outputbuffer = new byte[AUDIO_BUFFER_SIZE];

            mPlayer.start();
            while (isRunning){

                int realRecordSize = mRecorder.getData(recordBuffer, AUDIO_BUFFER_SIZE);
                int realTrackSize = mTrackProcessor.getData(trackBuffer, AUDIO_BUFFER_SIZE);
                Logger.i(TAG, "get recorder data[%d], track data[%d]", realRecordSize, realTrackSize);
                mMix.SetData(RECORD_STREAM, recordBuffer, realRecordSize);
                mMix.SetData(TRACK_STREAM, trackBuffer, realTrackSize);

                int realSize = mMix.GetData(outputbuffer, AUDIO_BUFFER_SIZE);
                Logger.i(TAG, "get mix data[%d]", realSize);
                if (realSize <= 0){
                    continue;
                }
                mPlayer.play(outputbuffer, realSize);
            }
        }


    }
}
