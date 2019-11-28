package com.ocom.hanmafacepay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.SparseArray;

import com.hanma.faceservice.FaceRecoService;
import com.hanma.faceservice.RealandApplication;
import com.hanma.faceservice.aidl.Face;
import com.hanma.faceservice.aidl.FaceRecoServiceApi;
import com.hanma.faceservice.aidl.FaceRecoServiceManager;

import java.util.List;

public class FaceServiceManager {

    private static FaceServiceManager instans;
    private FaceRecoServiceApi IFaceRecoServiceApi;
    private SparseArray<Runnable> mRunnableList;
    private Context mContext;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            FaceServiceManager.this.IFaceRecoServiceApi = FaceRecoServiceApi.Stub.asInterface(service);
            try {
                String version = FaceServiceManager.this.IFaceRecoServiceApi.getVersionName();
                System.out.print("api version = " + version);
                for (int i = 0; i <mRunnableList.size() ; i++) {
                    Runnable runnable = mRunnableList.get(i);
                    if (runnable != null) {
                        runnable.run();
                    }
                }
                mRunnableList.clear();
            } catch (RemoteException var4) {
                var4.printStackTrace();
            }

        }

        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public FaceRecoServiceApi getIFaceRecoServiceApi() {
        return IFaceRecoServiceApi;
    }

    public static FaceServiceManager getInstance() {
        if (instans == null) {
            instans = new FaceServiceManager();
            instans.mRunnableList = new SparseArray<>();
        }

        return instans;
    }

    public FaceServiceManager() {
    }

    public FaceRecoServiceApi Init(Context context) {
        RealandApplication.getInstance().onCreate(context);
        Intent intent = new Intent(context, FaceRecoService.class);
        context.bindService(intent, this.mServiceConnection, Context.BIND_AUTO_CREATE);
        return this.IFaceRecoServiceApi;
    }

    public void addRunnable(Runnable runnable) {
        if (this.IFaceRecoServiceApi != null) {
            runnable.run();
        } else {
            mRunnableList.put(mRunnableList.size(), runnable);
        }
    }

    public String getVersionName() {
        try {
            return this.IFaceRecoServiceApi.getVersionName();
        } catch (RemoteException var2) {
            var2.printStackTrace();
            return "";
        }
    }

    public int registerUserFeature(String userid, byte[] feature) {
        try {
            return this.IFaceRecoServiceApi.registerUserFeature(userid, feature);
        } catch (RemoteException var4) {
            var4.printStackTrace();
            return -200;
        }
    }

    public byte[] getUserFeature(String useriId) {
        try {
            return this.IFaceRecoServiceApi.getUserFeature(useriId);
        } catch (RemoteException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public int removeUserFeature(String userid) {
        try {
            return this.IFaceRecoServiceApi.removeUserFeature(userid);
        } catch (RemoteException var3) {
            var3.printStackTrace();
            return -200;
        }
    }

    public int removeAllUsers() {
        try {
            return this.IFaceRecoServiceApi.removeAllUsers();
        } catch (RemoteException var2) {
            var2.printStackTrace();
            return -200;
        }
    }

    public int registerUserByImage(String userid, byte[] rgbImg, int Width, int Height) {
        try {
            return this.IFaceRecoServiceApi.registerUserByImage(userid, rgbImg, Width, Height);
        } catch (RemoteException var6) {
            var6.printStackTrace();
            return -200;
        }
    }

    public int recognizeFacesByYuvData(byte[] yuv, int iw, int ih, int isLive, float fraction, List<String> users) {
        try {
            return this.IFaceRecoServiceApi.recognizeFacesByYuvData(yuv, iw, ih, isLive, fraction, users);
        } catch (RemoteException var8) {
            var8.printStackTrace();
            return -200;
        }
    }

    public List<String> getAllUsers() {
        try {
            return this.IFaceRecoServiceApi.getAllUsers();
        } catch (RemoteException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public byte[] getFeatureByRgb(byte[] rgb, int iw, int ih) {
        try {
            return this.IFaceRecoServiceApi.getFeatureByRgb(rgb, iw, ih);
        } catch (RemoteException var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public float compareFeature(byte[] feature1, byte[] feature2) {
        try {
            return this.IFaceRecoServiceApi.compareFeature(feature1, feature2);
        } catch (RemoteException var4) {
            var4.printStackTrace();
            return 0.0F;
        }
    }

    public String compareFeatureAllUsers(byte[] feature, float fraction) {
        try {
            return this.IFaceRecoServiceApi.compareFeatureAllUsers(feature, fraction);
        } catch (RemoteException var4) {
            var4.printStackTrace();
            return "";
        }
    }

    public byte[] checkRgbHasRegisterUser(byte[] rgb, int iw, int ih, List<String> users) {
        try {
            return this.IFaceRecoServiceApi.checkRgbHasRegisterUser(rgb, iw, ih, users);
        } catch (RemoteException var6) {
            var6.printStackTrace();
            return null;
        }
    }

    public Face getFacesDrawByYuvData(byte[] yuv, int Width, int Height) {
        try {
            return this.IFaceRecoServiceApi.getFacesDrawByYuvData(yuv, Width, Height);
        } catch (RemoteException var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public byte[] checkYuvHasRegisterUser(byte[] yuv, int iw, int ih, List<String> users) {
        try {
            return this.IFaceRecoServiceApi.checkHasRegisterUser(yuv, iw, ih, users);
        } catch (RemoteException var6) {
            var6.printStackTrace();
            return null;
        }
    }


}
