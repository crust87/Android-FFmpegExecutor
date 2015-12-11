/*
 * HttpRequestBuilder
 * https://github.com/mabi87/Android-FFmpegExecuter
 *
 * Mabi
 * crust87@gmail.com
 * last modify 2015-05-22
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mabi87.ffmpegexecutersample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.mabi87.videocropview.VideoCropView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends ActionBarActivity {

    // Constants
    private final static String FFMPEG_PATH = "ffmpeg";

    // Layout Components
    private VideoCropView mVideoCropView;
    private ProgressDialog mProgressDialog;

    // Component
    private FFmpegExecuter mExecuter;

    // Attributes
    private String originalPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] libraryAssets = { "ffmpeg" };

        File ffmpegDirPath = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/" + FFMPEG_PATH);
        if(!ffmpegDirPath.exists()) {
            ffmpegDirPath.mkdir();
        }

        for (int i = 0; i < libraryAssets.length; i++) {
            try {
                InputStream ffmpegInputStream = getApplicationContext().getAssets().open(libraryAssets[i]);
                FileMover fm = new FileMover(ffmpegInputStream, ffmpegDirPath.getAbsolutePath() + "/" + libraryAssets[i]);
                fm.moveIt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            String[] args = { "/system/bin/chmod", "755", ffmpegDirPath.getAbsolutePath() + "/ffmpeg" };
            Process process = new ProcessBuilder(args).start();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mExecuter = new FFmpegExecuter(getApplicationContext(), ffmpegDirPath.getAbsolutePath() + "/ffmpeg");

        mVideoCropView = (VideoCropView) findViewById(R.id.cropVideoView);
        mVideoCropView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoCropView.start();
            }
        });

        mExecuter.setOnReadProcessLineListener(new FFmpegExecuter.OnReadProcessLineListener() {
            @Override
            public void onReadProcessLine(String line) {
                Message message = Message.obtain();
                message.obj = line;
                message.setTarget(mMessageHandler);
                message.sendToTarget();
            }
        });
    }

    private Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            String message = (String) msg.obj;
            if(mProgressDialog != null) {
                mProgressDialog.setMessage(message);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            setOriginalVideo(data.getData());
        }
    }

    public void onButtonLoadClick(View v) {
        Intent lIntent = new Intent(Intent.ACTION_PICK);
        lIntent.setType("video/*");
        lIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(lIntent, 1000);
    }

    public void onButtonCropClick(View v) {
        mVideoCropView.pause();

        new AsyncTask<Void, Void, Void>() {

            float scale;
            int viewWidth;
            int viewHeight;
            int width;
            int height;
            int positionX;
            int positionY;
            int videoWidth;
            int videoHeight;
            int rotate;

            @Override
            protected void onPreExecute() {
                mExecuter.init();
                mProgressDialog = ProgressDialog.show(MainActivity.this, null, "execute....", true);

                scale = mVideoCropView.getScale();
                viewWidth = mVideoCropView.getWidth();
                viewHeight = mVideoCropView.getHeight();
                width = (int)(viewWidth * scale);
                height = (int)(viewHeight * scale);
                positionX = (int) mVideoCropView.getRealPositionX();
                positionY = (int) mVideoCropView.getRealPositionY();
                videoWidth = mVideoCropView.getVideoWidth();
                videoHeight = mVideoCropView.getVideoHeight();
                rotate = mVideoCropView.getRotate();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String filter = "";

                    if(rotate == 0) {
                        filter = "crop="+width+":"+height+":"+positionX+":"+positionY+", scale=640:640, setsar=1:1";
                    } else if(rotate == 90) {
                        filter = "crop="+height+":"+width+":"+positionY+":"+positionX +", scale=640:640, setsar=1:1";
                    } else if(rotate == 180) {
                        filter = "crop="+width+":"+height+":"+(videoWidth - positionX - width)+":"+positionY+ ", scale=640:640, setsar=1:1";
                    } else if(rotate == 270) {
                        filter = "crop="+height+":"+width+":"+(videoHeight - positionY - height)+":"+positionX + ", scale=640:640, setsar=1:1";
                    } else {
                        filter = "crop="+width+":"+height+":"+positionX+":"+positionY+", scale=640:640, setsar=1:1";
                    }

                    mExecuter.putCommand("-y")
                        .putCommand("-i")
                        .putCommand(originalPath)
                        .putCommand("-vcodec")
                        .putCommand("libx264")
                        .putCommand("-profile:v")
                        .putCommand("baseline")
                        .putCommand("-level")
                        .putCommand("3.1")
                        .putCommand("-b:v")
                        .putCommand("1000k")
                        .putCommand("-vf")
                        .putCommand(filter)
                        .putCommand("-c:a")
                        .putCommand("copy")
                        .putCommand(Environment.getExternalStorageDirectory().getAbsolutePath() + "/result.mp4")
                        .executeCommand();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }

        }.execute();
    }

    // Initialization original video
    private void setOriginalVideo(Uri uri) {
        originalPath = getRealPathFromURI(uri);

        mVideoCropView.setVideoURI(uri);
        mVideoCropView.seekTo(1);
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getApplicationContext().getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Copy file
    private class FileMover {

        private InputStream mInputStream;
        private String mDestination;

        public FileMover(InputStream inputStream, String destination) {
            mInputStream = inputStream;
            mDestination = destination;
        }

        public void moveIt() throws IOException {

            File destinationFile = new File(mDestination);
            OutputStream destinationOut = new BufferedOutputStream(new FileOutputStream(destinationFile));

            int numRead;
            byte[] buf = new byte[1024];
            while ((numRead = mInputStream.read(buf) ) >= 0) {
                destinationOut.write(buf, 0, numRead);
            }

            destinationOut.flush();
            destinationOut.close();
        }
    }
}
