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

package com.crust87.ffmpegexecutor;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FFmpegExecutor {

    /*
    Constants
     */
    public static final String TAG = "FFmpegExecutor";

    /*
    State Enum
     */
    public enum ExecutorState {
        idle,       // Executor do nothing
        running,    // Process working
        error       // has Error on Process
    }

    /*
    Event Listener
     */
    public interface FFmepgExecutorListener {
        void onReadProcessLine(String line);    // on read process line
        void onFinishProcess();                 // on finish process
        void onError(Exception e);              // on executor has error
    }

    /*
    Components
     */
    private Context mContext;
    private Handler mHandler;
    private ArrayList<String> mCommands;
    private ArrayList<ProcessBuilder> mCommendQueue;
    private FFmepgExecutorListener mFFmepgExecutorListener;

    /*
    Attributes
     */
    private String mFFmpegPath;
    private ExecutorState mExecutorState;

    /*
    Working Components
     */
    private Process mCurrentProcess;

    /**
     * Constructor
     * Copy ffmpeg to internal storage and change file permission
     *
     * @param context           the context of application
     * @param ffmpegInputStream the InputStream of ffmpeg binary
     * @throws IOException          Exception for copy binary to internal storage
     * @throws InterruptedException Exception for change permission ffmpeg directory
     */
    public FFmpegExecutor(Context context, InputStream ffmpegInputStream) throws IOException, InterruptedException {
        mContext = context;
        mCommands = new ArrayList<>();
        mCommendQueue = new ArrayList<>();

        File ffmpegDirPath = new File(mContext.getFilesDir().getAbsolutePath() + "/ffmpeg");
        if (!ffmpegDirPath.exists()) {
            ffmpegDirPath.mkdir();
        }

        mHandler = new Handler();
        mFFmpegPath = ffmpegDirPath.getAbsolutePath() + "/ffmpeg";
        mExecutorState = ExecutorState.idle;

        File ffmpeg = new File(mFFmpegPath);
        if (!ffmpeg.exists()) {
            FileMover fileMover = new FileMover(ffmpegInputStream, ffmpeg);
            fileMover.moveIt();
        }

        String[] args = {"/system/bin/chmod", "755", mFFmpegPath};
        Process process = new ProcessBuilder(args).start();
        process.waitFor();
        process.destroy();
    }

    /**
     * Initialization
     * <p/>
     * clear Command and Process Queue
     */
    public void init() {
        mExecutorState = ExecutorState.idle;

        mCommands.clear();
        mCommands.add(mFFmpegPath);

        mCommendQueue.clear();
    }

    /**
     * @param command the string command for FFmpeg
     * @return this
     */
    public FFmpegExecutor putCommand(String command) {
        mCommands.add(command);

        return this;
    }

    /**
     * add FFmpeg commands to Queue
     *
     * @return this
     */
    private FFmpegExecutor addQueue() {
        mCommendQueue.add(new ProcessBuilder(mCommands));

        return this;
    }

    /**
     * clear FFmpeg command line Queue
     *
     * @return this
     */
    private void clearQueue() {
        mCommendQueue.clear();
    }

    /**
     * execute FFmpeg commends with process queue
     *
     * @throws IOException
     */
    public void executeProcessQueue() throws IOException {
        mExecutorState = ExecutorState.running;

        for (ProcessBuilder builder : mCommendQueue) {
            executeProcess(builder);
        }

        mCommendQueue.clear();

        mExecutorState = ExecutorState.idle;
    }

    /**
     * execute FFmpeg commends with process queue Async
     */
    public void executeProcessQueueAsync() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                mExecutorState = ExecutorState.running;
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    for (ProcessBuilder builder : mCommendQueue) {
                        executeProcess(builder);
                    }

                    mCommendQueue.clear();
                } catch (final IOException e) {
                    mExecutorState = ExecutorState.error;

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mFFmepgExecutorListener != null) {
                                mFFmepgExecutorListener.onError(e);
                            }
                        }
                    });
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mExecutorState = ExecutorState.idle;

                if (mFFmepgExecutorListener != null) {
                    mFFmepgExecutorListener.onFinishProcess();
                }
            }

        }.execute();
    }

    /**
     * execute ffmpeg command line process
     *
     * @throws IOException if Process can not start
     */
    public void executeCommand() throws IOException {
        mExecutorState = ExecutorState.running;
        executeProcess(new ProcessBuilder(mCommands));
        mExecutorState = ExecutorState.idle;
    }

    /**
     * execute ffmpeg command line process Async
     */
    public void executeCommandAsync() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                mExecutorState = ExecutorState.running;
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    executeProcess(new ProcessBuilder(mCommands));
                } catch (final IOException e) {
                    mExecutorState = ExecutorState.error;

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mFFmepgExecutorListener != null) {
                                mFFmepgExecutorListener.onError(e);
                            }
                        }
                    });
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mExecutorState = ExecutorState.idle;

                if (mFFmepgExecutorListener != null) {
                    mFFmepgExecutorListener.onFinishProcess();
                }
            }

        }.execute();
    }

    /**
     * Execute FFmpeg commend line process with ProcessBuilder
     *
     * @param builder ProcessBuilder of FFmpeg commend line
     * @throws IOException
     */
    private void executeProcess(ProcessBuilder builder) throws IOException {
        mCurrentProcess = builder.redirectErrorStream(true).start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(mCurrentProcess.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (mFFmepgExecutorListener != null) {
                mFFmepgExecutorListener.onReadProcessLine(line);
            } else {
                Log.v(TAG, line);
            }
        }

        destroy();
    }

    /*
     * I'm not sure it's work
     */
    public void destroy() {
        if (mCurrentProcess != null) {
            mCurrentProcess.destroy();
            mCurrentProcess = null;
        }
    }

    /**
     * @param l listener
     */
    public void setFFmepgExecutorListener(FFmepgExecutorListener l) {
        mFFmepgExecutorListener = l;
    }

    /**
     *
     * @return State of FFmpeg executor state
     */
    public ExecutorState getExecutorState() {
        return mExecutorState;
    }

    /**
     * get System architecture
     *
     * @return System architecture
     */
    public static String getArchitecture() {
        return System.getProperty("os.arch");
    }
}
