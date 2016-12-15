package im.vinci.vmixer;

/**
 * Created by zhonglz on 2016/11/26.
 */
public interface VMixListener {
    void onStart();
    void onStop();
    void onEnable(int streamNO, boolean onOff);
}
