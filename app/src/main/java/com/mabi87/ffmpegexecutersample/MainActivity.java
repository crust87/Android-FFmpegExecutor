package com.mabi87.ffmpegexecutersample;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    // Layout Components
    private CropVideoView mCropVideoView;

    // Component
    private FFmpegExecuter mExecuter;

    // Attributes
    private String originalPath;
    private int mRotate;

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
    }

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

        mExecuter.init();

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

            if(mRotate == 0) {
                filter = "crop="+lWidth+":"+lHeight+":"+lPositionX+":"+lPositionY+", scale=480:640, setsar=1:1";
            } else if(mRotate == 90) {
                filter = "crop="+lHeight+":"+lWidth+":"+lPositionY+":"+lPositionX +", scale=640:480, setsar=1:1";
            } else if(mRotate == 180) {
                filter = "crop="+lWidth+":"+lHeight+":"+(lVideoWidth - lPositionX - lWidth)+":"+lPositionY+ ", scale=480:640, setsar=1:1";
            } else if(mRotate == 270) {
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

    }

    // Initialization original video
    private void setOriginalVideo(Uri uri) {
        originalPath = getRealPathFromURI(uri);

        mCropVideoView.setVideoURI(uri);
        mCropVideoView.seekTo(1);

        MediaMetadataRetriever retriever = new  MediaMetadataRetriever();
        retriever.setDataSource(originalPath);

        // create thumbnail bitmap
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

            try {
                mRotate = Integer.parseInt(rotation);
            } catch(NumberFormatException e) {
                mRotate = 0;
            }
        }

        retriever.release();
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getApplicationContext().getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
