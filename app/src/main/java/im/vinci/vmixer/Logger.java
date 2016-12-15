package im.vinci.vmixer;

import android.util.Log;

import java.util.Locale;

/**
 * Android Log的包装类,免去一些麻烦
 */
public class Logger {
    private static final String PRE = "&";
    public static boolean sIsDebug = true;

    public static final String space = "----------------------------------------------------------------------------------------------------";
    private static boolean LOGV = true;
    private static boolean LOGD = true;
    private static boolean LOGI = true;
    private static boolean LOGW = true;
    private static boolean LOGE = true;

//    private static boolean LOGV = false;
//    private static boolean LOGD = false;
//    private static boolean LOGI = false;
//    private static boolean LOGW = false;
//    private static boolean LOGE = false;

    public static void v(String tag, String format, Object... args) {
        if (LOGV) {
            Log.v(formartLength(PRE + tag, 28), buildMessage(format, args));
        }
    }

    public static void v(Throwable throwable, String tag, String format, Object... args) {
        if (LOGV) {
            Log.v(formartLength(PRE + tag, 28), buildMessage(format, args), throwable);
        }
    }


    public static void d(String tag, String format, Object... args) {
        if (LOGD) {
            Log.d(formartLength(PRE + tag, 28), buildMessage(format, args));
        }
    }

    public static void d(Throwable throwable, String tag, String format, Object... args) {
        if (LOGD) {
            Log.d(formartLength(PRE + tag, 28), buildMessage(format, args), throwable);
        }
    }

    public static void i(String tag, String format, Object... args) {
        if (LOGI) {
            Log.i(formartLength(PRE + tag, 28), buildMessage(format, args));
        }
    }

    public static void i(Throwable throwable, String tag, String format, Object... args) {
        if (LOGI) {
            Log.i(formartLength(PRE + tag, 28), buildMessage(format, args), throwable);
        }
    }

    public static void w(String tag, String format, Object... args) {
        if (LOGW) {
            Log.w(formartLength(PRE + tag, 28), buildMessage(format, args));
        }
    }

    public static void w(Throwable throwable, String tag, String format, Object... args) {
        if (LOGW) {
            Log.w(formartLength(PRE + tag, 28), buildMessage(format, args), throwable);
        }
    }


    public static void e(String tag, String format, Object... args) {
        if (LOGE) {
            Log.e(formartLength(PRE + tag, 28), buildMessage(format, args));
        }
    }

    public static void e(Throwable throwable, String tag, String format, Object... args) {
        if (LOGE) {
            Log.e(formartLength(PRE + tag, 28), buildMessage(format, args), throwable);
        }
    }

    private static String buildMessage(String format, Object[] args) {
        try {
            String msg = (args == null || args.length == 0) ? format : String.format(Locale.US, format, args);
            if (!sIsDebug) {
                return msg;
            }
            StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();
            String caller = "";
            String callingClass = "";
            String callFile = "";
            int lineNumber = 0;
            for (int i = 2; i < trace.length; i++) {
                Class<?> clazz = trace[i].getClass();
                if (!clazz.equals(Logger.class)) {
                    callingClass = trace[i].getClassName();
                    callingClass = callingClass.substring(callingClass
                            .lastIndexOf('.') + 1);
                    caller = trace[i].getMethodName();
                    callFile = trace[i].getFileName();
                    lineNumber = trace[i].getLineNumber();
                    break;
                }
            }

            String method = String.format(Locale.US, "[%03d] %s.%s(%s:%d)"
                    , Thread.currentThread().getId(), callingClass, caller, callFile, lineNumber);

            return String.format(Locale.US, "%s> %s", formartLength(method, 93), msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "----->ERROR LOG STRING<------";
    }

    private static String formartLength(String src, int len) {
        StringBuilder sb = new StringBuilder();
        if (src.length() >= len) {
            sb.append(src);
        } else {
            sb.append(src);
            sb.append(space.substring(0, len - src.length()));
        }
        return sb.toString();
    }

    public static class XiaoCaiqi {
        long start;

        public XiaoCaiqi() {
            start = System.currentTimeMillis();
        }

        public long end() {
            return System.currentTimeMillis() - start;
        }

    }

}

/*
                     ★
                    ／＼
                   ／  ＼
                  ／i⸛  ＼
                 ／｡ i & ＼
                ／ i &⸛&⸛ ＼
               ／⸮⁂    @⸮ ⸛＼
              ／｡⸛  & &｡ ⸮  ＼
             ／ ⸛  ⸮    ｡⸮  ⸛＼
            ／ ⁂｡⸛@  ｡  @⸛｡ & ＼
           ／⁂ @ @   ⸮ ⸛@     &＼
          ／@ ｡ ｡& ⸮@   ⸛ & ｡   ＼
         ／i ⁂⁂ ⸮⸛i  @⸛ ⁂ ⸛     ＼
        ／  ⸛          ⸮｡｡@｡&&    ＼
       ／   @⸮ ｡   ⸛   ⁂      ⸮    ＼
      ／ &⸮｡i  ⸛｡   ｡  & &     i   i＼
     ／⸛ i      @ i    @ @   i& &i⁂@ ＼
    ／⸛@⸛i ⁂    ⁂ @  i  & ｡ ⸮⸮    &   ＼
   ／｡i⸛   ⸮   ⸛     @& ⸮&⸮i⸛｡⁂  ｡@ ｡⸮⸮＼
  ／         &     i  ⸛&@     ｡ ⸛ ⁂⸛  @ ＼
 ／⁂ ⁂     ⁂ i  &  ⸮⸛⁂ &        @｡ ｡     ＼
 ^^^^^^^^^^^^^^^^^^^|  |^^^^^^^^^^^^^^^^^^^
                    |  |


 */