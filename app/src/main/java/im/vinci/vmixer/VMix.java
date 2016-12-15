package im.vinci.vmixer;

import android.util.SparseArray;
import android.util.SparseBooleanArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * Created by zhonglz on 2016/11/25.
 */
public class VMix {
    private final static String TAG = VMix.class.getSimpleName();

    private int mSampleRate;
    private int mBits;
    private int mChannels;
    private int mInputStreamCount;
    private int mMaxInputBufferSize;
    private int mMaxOutputBufferSize;

    private SparseArray<ByteRingBuffer> mInputStreamBuffers;
    private SparseBooleanArray mInputStreamValid;

    private ByteRingBuffer mOutputStreamBuffer;

    public VMix(){

    }

    public int Create(int sampleRate, int bits, int channels, int inputStreamCount, int maxInputBufferSize, int maxOutputBufferSize){

        if (!VMixParams.SampleRateOptions.isValidSampleRate(sampleRate)){
            Logger.e(TAG, "error mSampleRate rate[%d]", sampleRate);
            return VMixError.MIXERROR_SAMPLE_RATE;
        }

        if (!VMixParams.BitsOptions.isValidBits(bits)){
            Logger.e(TAG, "error bits[%d]", bits);
            return VMixError.MIXERROR_BITS;
        }

        if (!VMixParams.ChannelsOptions.isValidChannels(channels)){
            Logger.e(TAG, "error channels[%d]", channels);
            return VMixError.MIXERROR_CHANNELS;
        }

        if (inputStreamCount <= 0 || inputStreamCount > VMixParams.BoundaryValue.MAX_STREAMCOUNT){
            Logger.e(TAG, "error input stream count[%d]", inputStreamCount);
            return VMixError.MIXERROR_INPUTSTREAMCOUNT;
        }

        if (maxInputBufferSize <= 0 || maxInputBufferSize > VMixParams.BoundaryValue.MAX_BUFFERSIZE){
            Logger.e(TAG, "error input buffer size[%d]", maxInputBufferSize);
            return VMixError.MIXERROR_INPUTBUFFERSIZE;
        }

        if (maxOutputBufferSize <= 0 || maxOutputBufferSize > VMixParams.BoundaryValue.MAX_BUFFERSIZE){
            Logger.e(TAG, "error output buffer size[%d]", maxOutputBufferSize);
            return VMixError.MIXERROR_OUTPUTBUFFERSIZE;
        }

        mSampleRate = sampleRate;
        mBits = bits;
        mChannels = channels;
        mInputStreamCount = inputStreamCount;
        mMaxInputBufferSize = maxInputBufferSize;
        mMaxOutputBufferSize = maxOutputBufferSize;

        //input stream buffer
        mInputStreamBuffers = new SparseArray<>();
        mInputStreamValid = new SparseBooleanArray();

        for (int i = 0; i < inputStreamCount; i++){
            ByteRingBuffer inputBuf = new ByteRingBuffer(maxInputBufferSize);
            mInputStreamBuffers.put(i, inputBuf);
            mInputStreamValid.put(i, true);
        }
        //output stream buffer
        mOutputStreamBuffer = new ByteRingBuffer(maxOutputBufferSize);

        return VMixError.MIXERROR_OK;
    }

    public int SetData(int streamNO, byte[] data, int size){
        if (!isValidStream(streamNO)){
            return VMixError.MIXERROR_NOSUCHSTREAM;
        }

        if (size <= 0 || size > VMixParams.BoundaryValue.MAX_BUFFERSIZE){
            Logger.e(TAG, "error input date size[%d]", size);
            return VMixError.MIXERROR_INPUTBUFFERSIZE;
        }

        ByteRingBuffer inputBuf = mInputStreamBuffers.get(streamNO);
        if (inputBuf.getFree() < size){
            Logger.e(TAG, "error buffer is full, streamNO[%d], free[%d], used[%d], shouldWrite[%d]", streamNO, inputBuf.getFree(), inputBuf.getUsed(), size);
            return VMixError.MIXERROR_BUFFERFULL;
        }

        int ret = inputBuf.write(data);

        return ret;
    }

    public int GetData(short[] outputData, int size){

        if (size > VMixParams.BoundaryValue.MAX_BUFFERSIZE || size <= 0){
            return VMixError.MIXERROR_OUTPUTBUFFERSIZE;
        }

        if ((outputData.length * 2) < size){
            return VMixError.MIXERROR_OUTPUTBUFFERSIZE;
        }

        byte[] readBuf = new byte[size];

        int ret = GetData(readBuf, size);

        if (ret < 0){
            return ret;
        }

        short[] outputBuf = ByteBuffer.wrap(readBuf, 0, ret).asShortBuffer().array();

        System.arraycopy(outputBuf, 0, outputData, 0, outputBuf.length);

        return ret;
    }

    int blockCount = 0;
    public int GetData(byte[] outputData, int size){
        Logger.i(TAG, "GetData used[%d], size[%d]", mOutputStreamBuffer.getUsed(), size);
        blockCount ++;
        if (size > VMixParams.BoundaryValue.MAX_BUFFERSIZE || size <= 0){
            return VMixError.MIXERROR_OUTPUTBUFFERSIZE;
        }

        if (outputData.length < size){
            return VMixError.MIXERROR_OUTPUTBUFFERSIZE;
        }

        Mix2(mInputStreamBuffers.get(0), mInputStreamBuffers.get(1));

//        Mix();

        if (mOutputStreamBuffer.getUsed() < size){
            return VMixError.MIXERROR_OUTPUTBUFFERSIZE;
        }

        int ret = mOutputStreamBuffer.read(outputData, 0, size);

        return ret;
    }

    public int SetStreamVolume(int streamNO, int vol){
        return VMixError.MIXERROR_OK;
    }

    public int SetStreamValid(int streamNo, boolean isValid){
        if (mInputStreamValid == null){
            return VMixError.MIXERROR_FAILED;
        }

        if (streamNo < 0 || streamNo >= mInputStreamValid.size()){
            return VMixError.MIXERROR_NOSUCHSTREAM;
        }

        mInputStreamValid.put(streamNo, isValid);

        return VMixError.MIXERROR_OK;
    }



    /**
     * 每次mix，是取出所有valid的stream，比较出最小buffer size，取这个size的长度buffer进行相加并输出
     */
    private void Mix(){
        int minValidSize = VMixParams.BoundaryValue.MAX_BUFFERSIZE;
        SparseArray<ByteRingBuffer> validStreams = new SparseArray<>();
        //取出可用的流
        for (int i = 0; i < mInputStreamCount; ++i){
            if (!isValidStream(i)){
                continue;
            }
            ByteRingBuffer buffer = mInputStreamBuffers.get(i);
            if (buffer == null){
                continue;
            }
            validStreams.put(i, buffer);
            if (minValidSize > buffer.getUsed()){
                minValidSize = buffer.getUsed();
            }
        }

        Logger.i(TAG, "minValidSize[%d], validStream count[%d]", minValidSize, validStreams.size());
        if (minValidSize <= 0){
            return;
        }

        if (validStreams.size() <= 0){
            return;
        }

        //对每个流取出minValidSize的byte[]转为short[],再相加输出到outputStream

        short[] outputBuffer = new short[minValidSize/2 + minValidSize % 2];//初始化为0

        int minSize = 0;
        for (int i = 0; i < validStreams.size(); ++i){
            ByteRingBuffer buffer = validStreams.valueAt(i);
            if (buffer == null){
                continue;
            }

            byte[] readBuf = new byte[minValidSize];
            int readSize = buffer.read(readBuf);
            if (readSize <= 0){
                continue;
            }

//            short[] tempBuffer = ByteBuffer.wrap(readBuf).asShortBuffer().array();
            short[] tempBuffer = new short[minValidSize/2 + minValidSize%2];
            ByteBuffer.wrap(readBuf).asShortBuffer().get(tempBuffer);
            minSize = outputBuffer.length > tempBuffer.length ? tempBuffer.length : outputBuffer.length;

            for (int j = 0; j < minSize; ++j){
                int add1 = Short.valueOf(outputBuffer[j]).intValue();
                int add2 = Short.valueOf(tempBuffer[j]).intValue();
                int sum = (add1 + add2);
//                int sum = 0;
//                if (add1 < 0 && add2 < 0){
//                    sum = add1 + add2 - (int)(add1 * add2 / -(Math.pow(2.0, 15.0) - 1));
//                }else {
//                    sum = add1 + add2 - (int)(add1 * add2 / (Math.pow(2.0, 15.0) - 1));
//                }

                if (sum > Short.MAX_VALUE){
                    sum = Short.MAX_VALUE;
                }

                if (sum < Short.MIN_VALUE){
                    sum = Short.MIN_VALUE;
                }

                //这种地方就不要打日志了
//                Logger.i(TAG, "sum: [%d] + [%d] = [%d]", add1, add2, sum);

                outputBuffer[j] = Integer.valueOf(sum).shortValue();
            }
        }

        if (minSize <= 0){
            return;
        }

        ByteBuffer bb = ByteBuffer.allocate(minSize * 2);
        bb.asShortBuffer().put(outputBuffer);

        if (mOutputStreamBuffer.getFree() < bb.array().length){
            Logger.e(TAG, "no enough size to write output buffer");
            return;
        }

        mOutputStreamBuffer.write(bb.array());

    }

    private void Mix2(ByteRingBuffer stream1, ByteRingBuffer stream2){

        if (stream1 == null || stream2 == null){
            return;
        }

        //两个都没有数据
        if ((stream1.getUsed() + stream2.getUsed()) == 0){
            return;
        }

        //只有stream1
        if (stream1.getUsed() > 0 && stream2.getUsed() == 0){
            if (stream1.getUsed() <= mOutputStreamBuffer.getFree()){
                byte[] buffer = new byte[stream1.getUsed()];
                stream1.read(buffer);
                if (blockCount % 1500 == 0){
                    if (buffer.length > 40){
                        for (int i = 0; i < 20; ++i){
                            buffer[i*2] = 0;
                            buffer[i*2 + 1] = 0x40;
                        }
                        for (int i = 20; i < 40; ++i){
                            buffer[i*2] = 0;
                            buffer[i*2 + 1] = -0x40;
                        }
                    }
                }

                mOutputStreamBuffer.write(buffer);
            }
            return;
        }
        //只有stream2
        if (stream1.getUsed() == 0 && stream2.getUsed() > 0){
            if (stream2.getUsed() <= mOutputStreamBuffer.getFree()){
                byte[] buffer = new byte[stream2.getUsed()];
                stream2.read(buffer);
                mOutputStreamBuffer.write(buffer);
            }
            return;
        }
        //两个都有数据
        //对每个流取出minValidSize的byte[]转为short[],再相加输出到outputStream
        int minValidSize = stream1.getUsed() > stream2.getUsed() ? stream2.getUsed() : stream1.getUsed();

        byte[] bBuffer1 = new byte[minValidSize];
        byte[] bBuffer2 = new byte[minValidSize];

        int readSize1 = stream1.read(bBuffer1);
        int readSize2 = stream2.read(bBuffer2);

        int sSize = minValidSize/2 + minValidSize%2;
        short[] sBuffer1 = new short[sSize];
        short[] sBuffer2 = new short[sSize];
        short[] outputBuffer = new short[sSize];//初始化为0

        ByteBuffer.wrap(bBuffer1).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sBuffer1);
        ByteBuffer.wrap(bBuffer2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sBuffer2);

        for (int i = 0; i < sSize; ++i){
            //mix1
//            int add1 = Short.valueOf(sBuffer1[i]).intValue();
//            int add2 = Short.valueOf(sBuffer2[i]).intValue();
//            int sum = (add1 + add2);
            //mix2
//            int sum = 0;
//            if (add1 < 0 && add2 < 0){
//                sum = add1 + add2 - (int)(add1 * add2 / -(Math.pow(2.0, 15.0) - 1));
//            }else {
//                sum = add1 + add2 - (int)(add1 * add2 / (Math.pow(2.0, 15.0) - 1));
//            }

            //mix3
            int sum = sBuffer1[i] + sBuffer2[i];

            if (sum > Short.MAX_VALUE){
                sum = Short.MAX_VALUE;
            }

            if (sum < Short.MIN_VALUE){
                sum = Short.MIN_VALUE;
            }

            //这种地方就不要打日志了
//          Logger.i(TAG, "sum: [%d] + [%d] = [%d]", add1, add2, sum);

            outputBuffer[i] = Integer.valueOf(sum).shortValue();
        }

        ByteBuffer bb = ByteBuffer.allocate(sSize * 2);
        bb.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(outputBuffer);

        if (mOutputStreamBuffer.getFree() < bb.array().length){
            Logger.e(TAG, "no enough size to write output buffer");
            return;
        }

        mOutputStreamBuffer.write(bb.array());

    }

    public boolean isValidStream(int streamNO){
        if (mInputStreamValid == null){
            return false;
        }

        return mInputStreamValid.get(streamNO);
    }

    public void reset(){
        //input stream buffer
        blockCount = 0;
        for (int i = 0; i < mInputStreamBuffers.size(); ++i){
            mInputStreamBuffers.valueAt(i).clear();
        }

        for (int i = 0; i < mInputStreamValid.size(); ++i){
            mInputStreamValid.put(i, true);
        }
        //output stream buffer
        mOutputStreamBuffer.clear();
    }

}
