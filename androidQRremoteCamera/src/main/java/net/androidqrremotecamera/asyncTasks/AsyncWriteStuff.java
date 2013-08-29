package net.androidqrremotecamera.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;
import java.io.File;
import java.io.FileOutputStream;


import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by Administrator on 8/29/13.
 */
public class AsyncWriteStuff extends AsyncTask<String,Void,String>{
    File file;
    byte[] data;
    String DEBUG_TAG= "WriteStuffDebug";
    private Context context;
    private Handler h;

    public AsyncWriteStuff(File file, byte[] data, Context context) {
        this.file = file;
        this.data = data;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (Exception error) {
            Log.d(DEBUG_TAG, "File not saved: " + error.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        AsyncHttpPost asy = new AsyncHttpPost(file,data);
        asy.execute();
    }
}
