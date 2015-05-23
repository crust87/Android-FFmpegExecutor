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

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    // Layout Components
    private CropVideoView mCropVideoView;
    private ProgressDialog mProgressDialog;

    // Component
    private FFmpegExecuter mExecuter;

    // Attributes
    private String originalPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mExecuter = new FFmpegExecuter(getApplicationContext());

        mCropVideoView = (CropVideoView) findViewById(R.id.cropVideoView);
        mCropVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mCropVideoView.start();
            }
        });

        mExecuter.setOnReadProcessLineListener(new FFmpegExecuter.OnReadProcessLineListener() {
            @Override
            public void onReadProcessLine(String line) {
                Message msg = Message.obtain();
                msg.obj = line;
                msg.setTarget(mMessageHandler);
                msg.sendToTarget();
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
        mCropVideoView.pause();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                mExecuter.init();
                mProgressDialog = ProgressDialog.show(MainActivity.this, null, "execute....", true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String filter = "";

                    float lScale = mCropVideoView.getScale();
                    int lViewWidth = mCropVideoView.getWidth();
                    int lViewHeight = mCropVideoView.getHeight();
                    int lWidth = (int)(lViewWidth * lScale);
                    int lHeight = (int)(lViewHeight * lScale);
                    int lPositionX = (int) mCropVideoView.getRealPositionX();
                    int lPositionY = (int) mCropVideoView.getRealPositionY();
                    int lVideoWidth = mCropVideoView.getVideoWidth();
                    int lVideoHeight = mCropVideoView.getVideoHeight();
                    int lRotate = mCropVideoView.getRotate();

                    if(lRotate == 0) {
                        filter = "crop="+lWidth+":"+lHeight+":"+lPositionX+":"+lPositionY+", scale=480:640, setsar=1:1";
                    } else if(lRotate == 90) {
                        filter = "crop="+lHeight+":"+lWidth+":"+lPositionY+":"+lPositionX +", scale=640:480, setsar=1:1";
                    } else if(lRotate == 180) {
                        filter = "crop="+lWidth+":"+lHeight+":"+(lVideoWidth - lPositionX - lWidth)+":"+lPositionY+ ", scale=480:640, setsar=1:1";
                    } else if(lRotate == 270) {
                        filter = "crop="+lHeight+":"+lWidth+":"+(lVideoHeight - lPositionY - lHeight)+":"+lPositionX + ", scale=640:480, setsar=1:1";
                    } else {
                        filter = "crop="+lWidth+":"+lHeight+":"+lPositionX+":"+lPositionY+", scale=480:640, setsar=1:1";
                    }

                    mExecuter.putCommand("-y");
                    mExecuter.putCommand("-i");
                    mExecuter.putCommand(originalPath);
                    mExecuter.putCommand("-vcodec");
                    mExecuter.putCommand("libx264");
                    mExecuter.putCommand("-profile:v");
                    mExecuter.putCommand("baseline");
                    mExecuter.putCommand("-level");
                    mExecuter.putCommand("3.1");
                    mExecuter.putCommand("-b:v");
                    mExecuter.putCommand("1000k");
                    mExecuter.putCommand("-vf");
                    mExecuter.putCommand(filter);
                    mExecuter.putCommand("-c:a");
                    mExecuter.putCommand("copy");
                    mExecuter.putCommand(Environment.getExternalStorageDirectory().getAbsolutePath() + "/result.mp4");
                    mExecuter.executeCommand();
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

        mCropVideoView.setVideoURI(uri);
        mCropVideoView.seekTo(1);
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
