package com.ocom.hanmafacepay.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

public class Installation {
    private static String sID = null;
    private static final String INSTALLATION = "INSTALLATION";

    public synchronized static String id(Context context) {
        if (sID == null) {
            File directory = Environment.getExternalStoragePublicDirectory("UUID");
            if (!directory.exists()) {
                boolean mkdir = directory.mkdir();
                if (!mkdir) {
                    Log.e(Installation.TAG, "Mkdir failed!");
                    return "kfjafls";
                }
            }
            File installation = new File(directory, INSTALLATION);
            try {
                if (!installation.exists()) {
                    writeInstallationFile(installation);
                }
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes).substring(0,6);
    }

    private static final String TAG = "Installation";

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        Log.d(TAG, "writeInstallationFile path: " + installation.getAbsolutePath());
        String id = UUID.randomUUID().toString().substring(0, 6);
//        android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        out.write(id.getBytes());
        out.close();
    }
}

