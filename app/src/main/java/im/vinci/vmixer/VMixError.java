package im.vinci.vmixer;

/**
 * Created by zhonglz on 2016/11/25.
 */
public class VMixError {
    private final static String TAG = VMixError.class.getSimpleName();

    public final static int MIXERROR_OK = 0;
    public final static int MIXERROR_FAILED = -1;

    public final static int MIXERROR_SAMPLE_RATE = -2;//采样率错误
    public final static int MIXERROR_BITS = -3;//采样率错误
    public final static int MIXERROR_CHANNELS = -4;//声道数错误
    public final static int MIXERROR_INPUTSTREAMCOUNT = -5;//音轨数错误
    public final static int MIXERROR_INPUTBUFFERSIZE = -6;//输入buffer size错误
    public final static int MIXERROR_OUTPUTBUFFERSIZE = -7;//输出buffer size错误

    public final static int MIXERROR_NOSUCHSTREAM = -8;//无此输入流
    public final static int MIXERROR_BUFFERFULL = -9;//BUFFER已满

}
