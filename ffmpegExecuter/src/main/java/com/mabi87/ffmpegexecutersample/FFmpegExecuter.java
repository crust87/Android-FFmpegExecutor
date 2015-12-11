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
    public FFmpegExecuter(Context context, String ffmpegPath) {
        mContext = context;
        mCommands = new ArrayList<>();

        mFFmpegPath = ffmpegPath;
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
    public FFmpegExecuter putCommand(String command) {
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
}
