package im.vinci.vmixer;

/**
 * Created by zhonglz on 2016/11/25.
 */
public class VMixParams {
    private final static String TAG = VMixParams.class.getSimpleName();

    public static class SampleRateOptions{
        public final static int SAMPLERATE_44100 = 44100;
        public final static int SAMPLERATE_16000 = 16000;
        public final static int SAMPLERATE_8000 = 8000;

        public static boolean isValidSampleRate(int sampleRate){

            boolean isValid = false;

            switch (sampleRate){
                case SAMPLERATE_44100:
                case SAMPLERATE_16000:
                case SAMPLERATE_8000:{
                    isValid = true;
                }
                break;
                default:break;
            }

            return isValid;
        }
    }

    public static class BitsOptions{
        public final static int BITS_16 = 16;
        public final static int BITS_8 = 8;

        public static boolean isValidBits(int bits){

            boolean isValid = false;

            switch (bits){
                case BITS_16:
                case BITS_8:{
                    isValid = true;
                }
                break;
                default:break;
            }

            return isValid;
        }
    }

    public static class ChannelsOptions{
        public final static int CHANNEL_1 = 1;
        public final static int CHANNEL_2 = 2;

        public static boolean isValidChannels(int channels){

            boolean isValid = false;

            switch (channels){
                case CHANNEL_1:
                case CHANNEL_2:{
                    isValid = true;
                }
                break;
                default:break;
            }

            return isValid;
        }
    }

    public static class BoundaryValue{
        public final static int MAX_STREAMCOUNT = 4;
        public final static int MAX_BUFFERSIZE = 20 * 1024;

    }
}
