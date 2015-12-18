/*
 * Android-FFmpegExecutor
 * https://github.com/crust87/Android-FFmpegExecutor
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

package com.crust87.ffmpegexecutor;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FFmpegExecutor {

    // Components
    private Context mContext;
    private ArrayList<String> mCommands;
    private OnReadProcessLineListener mOnReadProcessLineListener;
    private Process mCurrentProcess;

    // Attributes
    private String mFFmpegPath;

    /**
     * Constructor
     * Copy ffmpeg to internal storage and change file permission
     *
     * @param context
     * 				the context of application
     * @param ffmpegPath
     *              the String of ffmpegPath, this path must be internal storage
     */
    public FFmpegExecutor(Context context, String ffmpegPath) {
        mContext = context;
        mCommands = new ArrayList<>();

        mFFmpegPath = ffmpegPath;
    }

    /**
     * Constructor
     * Copy ffmpeg to internal storage and change file permission
     *
     * @param context
     * 				the context of application
     * @param ffmpegInputStream
     *              the InputStream of ffmpeg binary
     * @throws IOException
     *              Exception for copy binary to internal storage
     * @throws InterruptedException
     *              Exception for change permission ffmpeg directory
     */
    public FFmpegExecutor(Context context, InputStream ffmpegInputStream) throws IOException, InterruptedException {
        mContext = context;
        mCommands = new ArrayList<>();

        File ffmpegDirPath = new File(mContext.getFilesDir().getAbsolutePath() + "/ffmpeg");
        if(!ffmpegDirPath.exists()) {
            ffmpegDirPath.mkdir();
        }

        mFFmpegPath = ffmpegDirPath.getAbsolutePath() + "/ffmpeg";

        File ffmpeg = new File(mFFmpegPath);
        if(!ffmpeg.exists()) {
            FileMover fileMover = new FileMover(ffmpegInputStream, ffmpeg);
            fileMover.moveIt();
        }

        String[] args = { "/system/bin/chmod", "755", mFFmpegPath };
        Process process = new ProcessBuilder(args).start();
        process.waitFor();
        process.destroy();
    }

    /**
     * Initialization
     */
    public void init() {
        mCommands.clear();
        mCommands.add(mFFmpegPath);
    }

    /**
     * @param command
     * 				the string command for FFmpeg
     * @return this
     */
    public FFmpegExecutor putCommand(String command) {
        mCommands.add(command);

        return this;
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
                Log.v("FFmpegExecutor", line);
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
}
