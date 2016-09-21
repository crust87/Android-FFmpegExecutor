/*
 * Android-FFmpegExecutor
 * https://github.com/crust87/Android-FFmpegExecutor
 *
 * Mabi
 * crust87@gmail.com
 * last modify 2016-09-21
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

package com.crust87.ffmpegexecutorsample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.crust87.ffmpegexecutor.FFmpegExecutor;
import com.crust87.ffmpegexecutor.FileMover;
import com.crust87.videocropview.VideoCropView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    // Layout Components
    private VideoCropView mVideoCropView;
    private ProgressDialog mProgressDialog;

    // Component
    private FFmpegExecutor mExecutor;

    // Attributes
    private String originalPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            InputStream ffmpegFileStream = getApplicationContext().getAssets().open("ffmpeg");
            mExecutor = new FFmpegExecutor(getApplicationContext(), ffmpegFileStream);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Fail FFmpeg Setting", Toast.LENGTH_LONG).show();
            finish();
        }

        loadGUI();
        bindEvent();
    }

    private void loadGUI() {
        setContentView(R.layout.activity_main);

        mVideoCropView = (VideoCropView) findViewById(R.id.cropVideoView);
    }

    private void bindEvent() {
        mVideoCropView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoCropView.start();
            }
        });

        mExecutor.setFFmepgExecutorListener(new FFmpegExecutor.FFmepgExecutorListener() {

            @Override
            public void onReadProcessLine(String line) {
                Log.d("TEST", line);

                Message message = Message.obtain();

                message.obj = line;
                message.setTarget(mMessageHandler);
                message.sendToTarget();
            }

            @Override
            public void onFinishProcess() {
                Toast.makeText(getApplicationContext(), "Finish FFmpeg Process", Toast.LENGTH_LONG).show();

                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private Handler mMessageHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String message = (String) msg.obj;

            if(mProgressDialog != null) {
                mProgressDialog.setMessage(message);
            }

            return true;
        }
    });

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
        if(originalPath == null) {
            return;
        }

        mVideoCropView.pause();

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

        mExecutor.init();
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

        mExecutor.putCommand("-y")
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
                .executeCommandAsync();
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
}
