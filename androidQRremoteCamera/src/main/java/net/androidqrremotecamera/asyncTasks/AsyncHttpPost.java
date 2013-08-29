package net.androidqrremotecamera.asyncTasks;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 8/29/13.
 */
public class AsyncHttpPost extends AsyncTask<String, String, String> {
    File pic;
    byte[] data;
    HttpResponse response;
    public AsyncHttpPost(File pic, byte[] data) {
        this.pic = pic;
        this.data = data;
    }

    @Override
    protected String doInBackground(String... params) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://192.168.1.111/upload");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mpEntity.addPart("file",new FileBody(pic,"image/jpeg"));

        httpPost.setEntity(mpEntity);
        try{
            response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            if(resEntity!=null){
                resEntity.consumeContent();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
