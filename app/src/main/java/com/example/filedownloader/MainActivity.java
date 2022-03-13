package com.example.filedownloader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    boolean isRunning=false;
    boolean mBound=false;
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DownloadService.MessageBinder binder = (DownloadService.MessageBinder) service;
            DownloadService mService = binder.getService();
            binder.setListener(new BoundServiceListener() {

                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void sendProgress(String message,int fileNumber) {
                    // Use this method to update our download progress
                    runOnUiThread(()->{
                        ProgressBar bar=(findViewById(R.id.progressBar));
                        bar.setProgress(bar.getProgress()+20,true);
                        Toast.makeText(mService, message, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void finishedDownloading() {

                    runOnUiThread(()->{
                        Toast.makeText(mService, "Files Downloaded", Toast.LENGTH_SHORT).show();
                        Button btn=(findViewById(R.id.downloadButton));
                        btn.setText(R.string.downloadButtonString);
                        stopCustomService();
                        isRunning=false;
                    });
                }
            });

            mBound = true;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("PDF Download Activity");
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    private ArrayList<URL> fetchUrls(){
        ArrayList<URL> urls=new ArrayList<>();
        ArrayList<EditText> textEdits=new ArrayList<>();
        try {
            EditText url1 =findViewById(R.id.editText1);
            EditText url2 =  findViewById(R.id.editText2);
            EditText url3 =findViewById(R.id.editText3);
            EditText url4 =  findViewById(R.id.editText4);
            EditText url5 = findViewById(R.id.editText5);
            textEdits.add(url1);
            textEdits.add(url2);
            textEdits.add(url3);
            textEdits.add(url4);
            textEdits.add(url5);
            for(EditText eT:textEdits){
                urls.add(new URL(eT.getText().toString()));
            }
        }
        catch(Exception err){
            urls=new ArrayList<>();
            System.out.println("Error ");
            err.printStackTrace();
        }
        return urls;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startDownload(View view){

        Context context = getApplicationContext();

        Button btn=(findViewById(R.id.downloadButton));
        // use this to start and trigger a service
        Intent i= new Intent(context, DownloadService.class);
        if(!isRunning){
            // potentially add data to the intent
            ArrayList<URL> urls=fetchUrls();
            i.putExtra("urls",urls);
            context.startService(i);
            bindService(i, mConnection, Context.BIND_AUTO_CREATE);

            btn.setText(R.string.actionDownloadButtonString);

            ProgressBar bar=(findViewById(R.id.progressBar));
            bar.setProgress(0,true);
            isRunning=true;
        }
        else{
            context.stopService(i);
            btn.setText(R.string.downloadButtonString);
            isRunning=false;
        }
    }

    public void stopCustomService(){
        Context context = getApplicationContext();
        Intent i= new Intent(context, DownloadService.class);
        context.stopService(i);
    }
}

