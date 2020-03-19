package com.yorhp.userlibrary;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.yorhp.interprocesscommunication.IMyAidlInterface;
import com.yorhp.interprocesscommunication.IOnUserChangedListener;
import com.yorhp.interprocesscommunication.bean.User;

/**
 * @author tyhj
 * @date 2020/3/19
 * @Description: java类作用描述
 */

public class UserManager {

    private IMyAidlInterface iMyAidlInterface;

    private IOnUserChangedListener mListener;



    public User getUser() {
        if (iMyAidlInterface != null) {
            try {
                return iMyAidlInterface.getUserById(0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setOnUserChangedListener(IOnUserChangedListener listener) {
        mListener = listener;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            try {
                iMyAidlInterface.registerListener(new IOnUserChangedListener.Stub() {
                    @Override
                    public void onUserChanged(User user) throws RemoteException {
                        if (mListener != null) {
                            mListener.onUserChanged(user);
                        }
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.i("ServiceConnection", "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public static UserManager getInstance() {
        return Holder.INSTANCE;
    }


    public void init(Context context){
        Intent intent = new Intent();
        intent.setAction("com.yorhp.aild.name");
        intent.setPackage("com.yorhp.interprocesscommunication");
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private UserManager() {

    }


    private static final class Holder {
        private static final UserManager INSTANCE = new UserManager();
    }

}
