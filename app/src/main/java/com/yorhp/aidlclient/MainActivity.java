package com.yorhp.aidlclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import com.yorhp.interprocesscommunication.IOnUserChangedListener;
import com.yorhp.interprocesscommunication.bean.User;
import com.yorhp.userlibrary.UserManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    /**
     * 服务端信使
     */
    private Messenger serviceMessenger;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 2:
                    String name = msg.getData().getString("name");
                    Toast.makeText(MainActivity.this, name, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UserManager.getInstance().init(this);
        findViewById(R.id.btnAIDL).setOnClickListener(v -> {
            String name=UserManager.getInstance().getUser().getName();
            Toast.makeText(MainActivity.this, name, Toast.LENGTH_SHORT).show();
            UserManager.getInstance().setOnUserChangedListener(userChangedListener);
        });


        Intent intentMessenger = new Intent();
        intentMessenger.setAction("com.yorhp.messenger.name");
        intentMessenger.setPackage("com.yorhp.interprocesscommunication");
        bindService(intentMessenger, mMessengerServiceConnection, BIND_AUTO_CREATE);
        findViewById(R.id.btnMessenger).setOnClickListener(v -> {
            if (serviceMessenger != null) {
                try {
                    Message message = new Message();
                    message.what = 2;
                    Bundle bundle = new Bundle();
                    bundle.putString("name", "Tom");
                    message.setData(bundle);
                    serviceMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * messenger服务连接监听
     */
    private ServiceConnection mMessengerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                //获取到服务端信使
                serviceMessenger = new Messenger(service);
                Message message = new Message();
                //将客户端信使传递到服务端
                message.replyTo = new Messenger(handler);
                ;
                message.what = 1;
                //使用服务端信使发送
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };



    private IOnUserChangedListener userChangedListener=new IOnUserChangedListener.Stub(){
        @Override
        public void onUserChanged(User user) throws RemoteException {
            handler.post(()->{
                Toast.makeText(MainActivity.this,user.getName(),Toast.LENGTH_SHORT).show();
            });
        }
    };

}
