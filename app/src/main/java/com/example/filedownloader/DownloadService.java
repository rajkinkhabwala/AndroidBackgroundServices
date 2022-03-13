package com.example.filedownloader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DownloadService extends Service {

    private final IBinder mBinder = new MessageBinder();
    BoundServiceListener mListener;
    ArrayList<String> urls=new ArrayList<>();
    public class MessageBinder extends Binder {

        public DownloadService getService() {
            return DownloadService.this;
        }
        public void setListener(BoundServiceListener listener) {
            mListener = listener;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        // The service is being created

    }

    @Override
    @SuppressWarnings("unchecked")
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
        if(intent!=null){
            ArrayList<URL> urls=(ArrayList<URL>) intent.getSerializableExtra("urls");
            this.urls=new ArrayList<>();
            for(int i=0;i<urls.size();i++){
                new Thread(new FileDownloader(this.getBaseContext(),urls.get(i),i,this)).start();
                Toast.makeText(this, "Downloading "+urls.get(i).getPath(), Toast.LENGTH_SHORT).show();
            }
        }
        return Service.START_STICKY;
    }

    @Override
    public  void onDestroy(){
        this.urls=new ArrayList<>();
        Toast.makeText(this, "Download Stopped", Toast.LENGTH_SHORT).show();
    }
    public void sendProgress(String message,int i){
        //Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        System.out.println(message);
        urls.add(message);
        if(mListener!=null){
            mListener.sendProgress(message,i);
        }
        if(urls.size()==5){
            if(mListener!=null){
                mListener.finishedDownloading();
            }
        }

    }
}

class FileDownloader implements Runnable{
    int i;
    public URL url;
    Context context;
    DownloadService ds;
    public FileDownloader(Context context, URL url, int i,DownloadService ds){
        this.url=url;
        this.i=i;
        this.context=context;
        this.ds=ds;
    }
    public HttpURLConnection setConnectionProperties(HttpURLConnection urlConnection) throws Exception{
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoOutput(false);
        urlConnection.addRequestProperty("Content-Type","application/octet-stream");
        urlConnection.addRequestProperty("Accept","*/*");
        urlConnection.addRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (HTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36");
        urlConnection.addRequestProperty("Accept-Encoding","gzip, deflate, br");
        urlConnection.addRequestProperty("Connection","keep-alive");
        urlConnection.addRequestProperty("Host",this.url.getHost());
        urlConnection.setUseCaches(false);
        return urlConnection;
    }
    @Override
    public void run() {

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection=this.setConnectionProperties(urlConnection);
            urlConnection.connect();
            File sdcard = this.context.getFilesDir();
            File new_file=new File(url.getFile());
            File file = new File(sdcard, new_file.getName());
            FileOutputStream fileOutput = new FileOutputStream(file);
            Log.i("responseCode",file.getName()+" : "+urlConnection.getResponseCode());
            InputStream inputStream =urlConnection.getResponseCode()==200? urlConnection.getInputStream():urlConnection.getErrorStream();
            if(urlConnection.getResponseCode()==200) {

                byte[] buffer = new byte[1024];
                int bufferLength;

                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bufferLength);
                }
                fileOutput.close();
                //Log.i("status","Downloaded File "+file.getName());
                ds.sendProgress("Downloaded File "+file.getName(),this.i);
            }
            else{
                //  Log.i("status","Could not downloaded File "+file.getName());
                ds.sendProgress("Could not downloaded File "+file.getName(),this.i);
            }
        } catch (IOException e) {
            //  Log.i("status","Failed to Download File ");
            ds.sendProgress("Failed to Download File ",this.i);

            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(urlConnection!=null)
                urlConnection.disconnect();
        }
    }
}