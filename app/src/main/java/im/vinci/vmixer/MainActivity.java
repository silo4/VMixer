package im.vinci.vmixer;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private int mState = ControlState.CONTROL_IDLE;

    @Bind(R.id.ivControl)
    ImageView mIVControl;
    @Bind(R.id.btnRecord)
    Button mBtnRecord;
    @Bind(R.id.btnTrack)
    Button mBtnTrack;
    @Bind(R.id.btnTestAudio)
    Button mBtnTestAudio;

    private MixTester mMix;

    private TestAudioPlayer mTestAudioPlayer;
    private boolean isPlayingTestAudio = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mMix = new MixTester(mMixListener);
        mTestAudioPlayer = new TestAudioPlayer(44100, 16, 2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.ivControl})
    void onClickControl(){
        switch (mState){
            case ControlState.CONTROL_IDLE:{
                mState = ControlState.CONTROL_PLAYING;
                startAnim();
                mMix.start();
            }
            break;
            case ControlState.CONTROL_PLAYING:{
                mState = ControlState.CONTROL_IDLE;
                stopAnim();
                mMix.stop();

                mBtnRecord.setText("record running");
                mBtnTrack.setText("track playing");
            }
            break;
            default:break;
        }
    }

    @OnClick({R.id.btnTestAudio})
    void onClickTestAudio(){
        if (!isPlayingTestAudio){
            mTestAudioPlayer.play();
            mBtnTestAudio.setText("Playing Mixer");
            isPlayingTestAudio = true;
        }else {
            mTestAudioPlayer.stop();
            mBtnTestAudio.setText("Play Mixer");
            isPlayingTestAudio = false;
        }

    }

    @OnClick({R.id.btnRecord})
    void onClickRecord(){
        if (mMix == null){
            return;
        }

        if (mMix.isRecordEnable()){
            mMix.setRecorderEnable(false);
            mBtnRecord.setText("record forbidden");
        }else {
            mMix.setRecorderEnable(true);
            mBtnRecord.setText("record running");
        }
    }

    @OnClick({R.id.btnTrack})
    void onClickTrack(){
        if (mMix == null){
            return;
        }

        if (mMix.isTrackEnable()){
            mMix.setTrackEnable(false);
            mBtnTrack.setText("track forbidden");
        }else {
            mMix.setTrackEnable(true);
            mBtnTrack.setText("track playing");
        }
    }

    private void startAnim(){
        Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.control_rotate_anim);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        mIVControl.startAnimation(operatingAnim);
    }

    private void stopAnim(){
        mIVControl.clearAnimation();
    }

    private VMixListener mMixListener = new VMixListener() {
        @Override
        public void onStart() {
            Logger.i(TAG, "onStart");
        }

        @Override
        public void onStop() {
            Logger.i(TAG, "onStop");
        }

        @Override
        public void onEnable(int streamNO, boolean onOff) {
            Logger.i(TAG, "onEnable[%d][%s]", streamNO, onOff);
        }
    };

    public static class ControlState{
        public final static int CONTROL_IDLE = 0;
        public final static int CONTROL_PLAYING = 1;
    }
}
