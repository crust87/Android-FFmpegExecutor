/*
 * HttpRequestBuilder
 * https://github.com/mabi87/Android-FFmpegExecuter
 *
 * Mabi
 * crust87@gmail.com
 * last modify 2015-05-23
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

import android.content.Context;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class FFmpegExecuter {
    // Constants
    private final static String FFMPEG_PATH = "ffmpeg";

    // Components
    private Context mContext;
    private ArrayList<String> mCommands;
    private OnReadProcessLineListener mOnReadProcessLineListener;
    private Process mCurrentProcess;

    /**
     * Constructor
     * Copy ffmpeg to internal storage and change file permission
     *
     * @param context
     * 				the context of application
     */
    public FFmpegExecuter(Context context) {
        mContext = context;
        mCommands = new ArrayList<String>();

        String[] libraryAssets = { "ffmpeg" };

        File ffmpegPath = new File(mContext.getFilesDir().getAbsolutePath() + "/" + FFMPEG_PATH);
        if(!ffmpegPath.exists()) {
            ffmpegPath.mkdir();
        }

        for (int i = 0; i < libraryAssets.length; i++) {
            try {
                InputStream ffmpegInputStream = mContext.getAssets().open(libraryAssets[i]);
                FileMover fm = new FileMover(ffmpegInputStream, ffmpegPath.getAbsolutePath() + "/" + libraryAssets[i]);
                fm.moveIt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            String[] args = { "/system/bin/chmod", "755", ffmpegPath.getAbsolutePath() + "/ffmpeg" };
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
    }

    /**
     * Initialization
     */
    public void init() {
        mCommands.clear();
        mCommands.add(mContext.getFilesDir().toString() + "/" + FFMPEG_PATH + "/ffmpeg");
    }

    /**
     * @param command
     * 				the string command for FFmpeg
     */
    public void putCommand(String command) {
        mCommands.add(command);
    }

    /**
     * @throws IOException
     * 				if Process can not start
     */
    public void executeCommand() throws IOException {
        mCurrentProcess = new ProcessBuilder(mCommands).redirectErrorStream(true).start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(mCurrentProcess.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null)  {
            if(mOnReadProcessLineListener != null) {
                mOnReadProcessLineListener.onReadProcessLine(line);
            } else {
                Log.v("FFmpegExecuter", line);
            }
        }

        destroy();
    }

    /*
     * I'm not sure it's work
     */
    public void destroy() {
        if(mCurrentProcess != null) {
            mCurrentProcess.destroy();
            mCurrentProcess = null;
        }
    }

    /**
     * @param pOnReadProcessLineListener
     * 				the OnReadProcessLineListener for ffmpegLog()
     * 			    if you set this interface, ffmpegLog method will use it when read each line
     */
    public void setOnReadProcessLineListener(OnReadProcessLineListener pOnReadProcessLineListener) {
        mOnReadProcessLineListener = pOnReadProcessLineListener;
    }

    public interface OnReadProcessLineListener {
        public abstract void onReadProcessLine(String line);
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
