package com.example.narye.signmeup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;


/**
 * Created by NaryE on 3/23/18.
 */

public class Activity2 extends Activity {

    MediaRecorder mRecorder;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    private static LongOperation operation = null;
    private static Polling pollOperation = null;
//    private static GetGif gifOperation = null;
    ProgressDialog dialog;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }


    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioSamplingRate(16000);
        mRecorder.setOutputFile(mFileName);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    boolean mStartRecording = true;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_screen);

        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audioAMAZON.mp4";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        final ImageButton mBtn1 = (ImageButton) findViewById(R.id.imageButton3);
        final ProgressBar prog = (ProgressBar) findViewById(R.id.progressBar);


        mBtn1.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("WrongConstant")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mStartRecording && event.getAction() == MotionEvent.ACTION_DOWN) {
                    //Record
                    prog.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(),
                            "Recording :)", 6000).show();
                    startRecording();
                    //onRecord(mStartRecording);
                    mStartRecording = false;
                } else if (!mStartRecording && event.getAction() == MotionEvent.ACTION_UP) {
                    //Stop recording
                    //Go to some place else
                    mBtn1.setVisibility(View.INVISIBLE);
                    stopRecording();
                    //onRecord(mStartRecording);
                    mStartRecording = true;
                    operation = new LongOperation();
                    operation.execute(mFileName, "http://54.91.200.30/api/translate/");


                }

                return true;
            }
        });
    }

    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            System.out.println("Long Operation");
            String filePath = params[0];
            String Sever_URL = params[1];
            File file = new File(filePath);
            int serverResponseCode = 0;

            HttpURLConnection connection;
            DataOutputStream dataOutputStream;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";


            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;


            String[] parts = filePath.split("/");
            final String fileName = parts[parts.length - 1];

            if (!file.isFile()) {
                dialog.dismiss();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Source File Doesn't Exist");
                    }
                });
                return null;
            } else {
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    URL url = new URL(Sever_URL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);//Allow Inputs
                    connection.setDoOutput(true);//Allow Outputs
                    connection.setUseCaches(false);//Don't use a cached Copy
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    connection.setRequestProperty("uploaded_file", filePath);


                    //creating new dataoutputstream
                    dataOutputStream = new DataOutputStream(connection.getOutputStream());

                    //writing bytes to data outputstream
                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                            + filePath + "\"" + lineEnd);

                    dataOutputStream.writeBytes(lineEnd);

                    //returns no. of bytes present in fileInputStream
                    bytesAvailable = fileInputStream.available();
                    //selecting the buffer size as minimum of available bytes or 1 MB
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    //setting the buffer as byte array of size of bufferSize
                    buffer = new byte[bufferSize];

                    //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                    while (bytesRead > 0) {
                        //write the bytes read from inputstream
                        dataOutputStream.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    serverResponseCode = connection.getResponseCode();
                    String serverResponseMessage = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();


                    //closing the input and output streams
                    fileInputStream.close();
                    dataOutputStream.flush();
                    dataOutputStream.close();

                    return serverResponseMessage;


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Activity2.this, "File Not Found", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Toast.makeText(Activity2.this, "URL error!", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(Activity2.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                return null;
            }
        }

        @Override
        protected void onPostExecute (String result){
            try {
                JSONObject jObj = new JSONObject(result);
                String jobID = jObj.getString("jobID");
                pollOperation = new Polling();
                pollOperation.execute(String.format("http://54.91.200.30/api/isDone/%s", jobID));
                System.out.println(jobID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute () {
        }

        @Override
        protected void onProgressUpdate (Void...values){
        }
    }

    private class Polling extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            System.out.println("Polling");
            String Server_URL = params[0];
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(Server_URL);

                urlConnection = (HttpURLConnection) url
                        .openConnection();

                InputStream in = urlConnection.getInputStream();

                InputStreamReader isw = new InputStreamReader(in);

                return new BufferedReader(isw).readLine();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        ProgressBar prog = (ProgressBar) findViewById(R.id.progressBar);
        @SuppressLint("WrongConstant")
        @Override
        protected void onPostExecute (String result){
            try {
                JSONObject jObj = new JSONObject(result);
                String jobID = jObj.getString("jobID");
                String isdone = jObj.getString("isDone");
                System.out.println("IS DONE: "+isdone);
                if(isdone.equals("true")){
                    //load gif
                    System.out.println("TRUUEUEUEUEUEUE");
//                    gifOperation = new GetGif();
//                    gifOperation.execute(jObj.getString("gifURL"));

                    Glide
                            .with(Activity2.this)
                            .load(jObj.getString("gifURL"))
                            .into(imageView);
                    prog.setVisibility(View.INVISIBLE);


                }
                else if(isdone.equals("false")){
                    Toast.makeText(getApplicationContext(),
                            "Crash", 1000).show();
                }
                else if(isdone.equals("null")){
                    Toast.makeText(getApplicationContext(),
                            "Pending", 1000).show();

                    //poll again
                    Thread.sleep(10000);
                    pollOperation = new Polling();
                    pollOperation.execute(String.format("http://54.91.200.30/api/isDone/%s", jobID));


                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

//    private class GetGif extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected String doInBackground(String... params) {
//            System.out.println("GIF");
//            String Server_URL = params[0];
//            URL url;
//            HttpURLConnection urlConnection = null;
//            try {
//                url = new URL(Server_URL);
//
//                urlConnection = (HttpURLConnection) url
//                        .openConnection();
//
//                InputStream in = urlConnection.getInputStream();
//
//                InputStreamReader isw = new InputStreamReader(in);
//
//                return new BufferedReader(isw).readLine();
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//            }
//            return null;
//        }
//
//        @SuppressLint("WrongConstant")
//        ImageView imageView = (ImageView) findViewById(R.id.imageView);
//        ProgressBar prog = (ProgressBar) findViewById(R.id.progressBar);
//        @SuppressLint("WrongConstant")
//        @Override
//        protected void onPostExecute (String result){
//            try {
//                JSONObject jObj = new JSONObject(result);
//                String jobID = jObj.getString("jobID");
//                String isdone = jObj.getString("isDone");
//
//                if(isdone.equals("true")){
//                    //load gif
////                    Glide.with(Activity2.this)
////                            .load(jObj.getString("gifURL"))
////                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
////                            .into(imageView);
//                    prog.setVisibility(View.INVISIBLE);
//                    Glide.with(Activity2.this).load(jObj.getString("gifURL")).asBitmap().centerCrop().into(new BitmapImageViewTarget(imageView) {
//                        @Override
//                        protected void setResource(Bitmap resource) {
//                            System.out.println("GLIDE1");
//                            RoundedBitmapDrawable circularBitmapDrawable =
//                                    RoundedBitmapDrawableFactory.create(Activity2.this.getResources(), resource);
//                            System.out.println("GLIDE2");
//                            circularBitmapDrawable.setCircular(true);
//                            System.out.println("GLIDE3");
//                            imageView.setImageDrawable(circularBitmapDrawable);
//                        }
//                    });
//
//                }
//                else if(isdone.equals("false")){
//                    Toast.makeText(getApplicationContext(),
//                            "Crash", 1000).show();
//                    System.out.println("GIF FALSEEE");
//                }
//                else if(isdone.equals("null")){
//                    Toast.makeText(getApplicationContext(),
//                            "Pending", 1000).show();
//                    System.out.println("GIF NULLL");
//                    //poll again
//                    Thread.sleep(20000*60);
//                    pollOperation = new Polling();
//                    pollOperation.execute(String.format("http://54.91.200.30/api/isDone/%s", jobID));
//
//
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}