package com.ocom.hanmafacepay.util;

import android.util.Log;

import com.ocom.faceidentification.utils.TimeUtils;
import com.ocom.hanmafacepay.FacePayApplication;

import java.io.File;

import dou.utils.FileUtil;
import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FileLogUtil {

    public static boolean mSwitch = true;
    public static boolean mWrite = true;
    static long time;
    public static final int V = 1;
    public static final int D = 2;
    public static final int I = 3;
    public static final int W = 4;
    public static final int E = 5;
    private static String mLogPath = FacePayApplication.INSTANCE.getFilesDir().getAbsolutePath() + "/local_log";

    public FileLogUtil() {
    }

    public static void clearCache() {
        FileUtil.writeFile(mLogPath,
                "log0:\n", false);
    }

    public static File getLogFile() {
        return new File(mLogPath);
    }

    public static void time(String tag) {
        if (time == 0L) {
            time = System.currentTimeMillis();
        } else {
            d(tag + " cost: " + (System.currentTimeMillis() - time));
            time = 0L;
        }

    }

    public static void v() {
        v(null);
    }

    public static void d() {
        d(null);
    }

    public static void i() {
        i(null);
    }

    public static void w() {
        w(null);
    }

    public static void e() {
        e(null);
    }

    public static void v(Object message) {
        v(null, message);
    }

    public static void d(Object message) {
        d(null, message);
    }

    public static void i(Object message) {
        i(null, message);
    }

    public static void w(Object message) {
        w(null, message);
    }

    public static void e(Object message) {
        e(null, message);
    }

    public static void v(String tag, Object message) {
        llog(1, tag, message);
    }

    public static void d(String tag, Object message) {
        llog(2, tag, message);
    }

    public static void i(String tag, Object message) {
        llog(3, tag, message);
    }

    public static void w(String tag, Object message) {
        llog(4, tag, message);
    }

    public static void e(String tag, Object message) {
        llog(5, tag, message);
    }

    public static Disposable mFileDisposable;

    public static void llog(int type, String tagStr, Object obj) {
        if (mSwitch) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            int index = 5;
            String className = stackTrace[index].getFileName();
            String methodName = stackTrace[index].getMethodName();
            int lineNumber = stackTrace[index].getLineNumber();
            String tag = tagStr == null ? "DLog" : tagStr;
            methodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[ (").append(className).append(":").append(lineNumber).append(")#").append(methodName).append(" ] ");
            String msg;
            if (obj == null) {
                msg = "Log with null Object";
            } else {
                msg = obj.toString();
            }
            String stamp = TimeUtils.INSTANCE.stampToDate(System.currentTimeMillis());

            if (msg != null) {
                stringBuilder.append(msg);
            }

            String logStr = stringBuilder.toString();
            if (mWrite) {
                if (mFileDisposable != null && !mFileDisposable.isDisposed())
                    mFileDisposable.dispose();
                //超过20MB清除缓存
                mFileDisposable = Maybe.just(mLogPath)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(path -> {
                            File file = new File(path);
                            if (file.exists() && file.length() > 20 * 1024 * 1024) {
                                clearCache();
                            }
                            FileUtil.writeFile(path, stamp + " ----> " + tag + " : " + logStr + "\n", true);

                        }, Throwable::printStackTrace);
            } else {
                switch (type) {
                    case 1:
                        Log.v(tag, logStr);
                        break;
                    case 2:
                        Log.d(tag, logStr);
                        break;
                    case 3:
                        Log.i(tag, logStr);
                        break;
                    case 4:
                        Log.w(tag, logStr);
                        break;
                    case 5:
                        Log.e(tag, logStr);
                }
            }

        }
    }
}
