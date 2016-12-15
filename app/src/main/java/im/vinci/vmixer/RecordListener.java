package im.vinci.vmixer;

/**
 * Created by zhonglz on 16/10/10.
 */

public interface RecordListener {

    void onSuccess();
    void onFailed(String err);
}
