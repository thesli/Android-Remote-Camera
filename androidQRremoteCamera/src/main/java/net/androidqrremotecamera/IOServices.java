package net.androidqrremotecamera;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.security.MessageDigestSpi;


import io.socket.*;

/**
 * Created by Administrator on 8/29/13.
 */
public class IOServices extends Service {
    SocketIO socket;
    Handler m_handler;
    public Message msg;
    public Messenger msger;

    public String TAG = "IOServicesDEBUG";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        msger = (Messenger) intent.getParcelableExtra("messenger");
        msg = Message.obtain(null,R.string.trythis);
        Toast.makeText(getApplicationContext(),"start!!!!",Toast.LENGTH_SHORT).show();
//        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//        v.vibrate(2000);
        io();
        return START_STICKY;
    }

    private void io() {
        try {
            socket = new SocketIO("http://pella.sytes.net:3030");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        socket.connect(
                new IOCallback() {
                    IOAcknowledge ack = new IOAcknowledge() {
                        @Override
                        public void ack(Object... objects) {
                            if(objects.length>0){
                                Log.d(TAG, "ack triggered : " + objects[0]);
                            }
                        }
                    };


                    @Override
                    public void onDisconnect() {
                        Log.d(TAG,"onDisconnected");
                    }

                    @Override
                    public void onConnect() {
                        Log.d(TAG,"onConnected");
                    }

                    @Override
                    public void onMessage(String s, IOAcknowledge ioAcknowledge) {

                    }

                    @Override
                    public void onMessage(JSONObject jsonObject, IOAcknowledge ioAcknowledge) {

                    }

                    @Override
                    public void on(String s, IOAcknowledge ioAcknowledge, Object... objects) {
                        if(objects.length>0){
                            Log.d(TAG, s + " : " + objects[0]);
                            if(s.equals("takephoto")){
//                                showToast();
                                takePhoto();
                            }
                        }
                    }

                    @Override
                    public void onError(SocketIOException e) {
                        Log.d(TAG,"event");
                    }
                }
        );
    }

    public void showToast(){
        m_handler = new Handler(getApplicationContext().getMainLooper());
        m_handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"fuck you",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void takePhoto(){
        m_handler = new Handler(getApplication().getMainLooper());
        m_handler.post(takePhotoRunnable);
    }

    Runnable takePhotoRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                msger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
