# Android-FFmpegExecutor
simple ffmpeg command executor for android

## Update
ffmpeg binary has moved to app<br />
ffmpeg binary must be copy into internal storage

## Example

add build.gradle<br />
``` groovy
compile 'com.crust87:ffmpeg-executor:1.0.1'
```

```java
File ffmpegDirPath = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/ffmpeg");
if(!ffmpegDirPath.exists()) {
    ffmpegDirPath.mkdir();
}

try {
    InputStream ffmpegInputStream = getApplicationContext().getAssets().open("ffmpeg");
    FileMover fm = new FileMover(ffmpegInputStream, ffmpegDirPath.getAbsolutePath() + "/ffmpeg");
    fm.moveIt();
} catch (IOException e) {
    e.printStackTrace();
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

mExecutor = new FFmpegExecutor(getApplicationContext(), ffmpegDirPath.getAbsolutePath() + "/ffmpeg");
```

if you want to know ffmpeg log while process running, set this listener 
```java
mExecutor.setOnReadProcessLineListener(new FFmpegExecutor.OnReadProcessLineListener() {
    @Override
    public void onReadProcessLine(String line) {
        // TODO something
    }
});
```

you must call init() before put command<br/>
put some commands
```java
mExecutor = new FFmpegExecutor(getApplicationContext(), ffmpegPath);

mExecutor.init();

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
    .putCommand(Environment.getExternalStorageDirectory().getAbsolutePath() + "/result.mp4");
```

and execute command
```java
mExecutor.executeCommand();
```

## Licence
Copyright 2015 Mabi

Licensed under the Apache License, Version 2.0 (the "License");<br/>
you may not use this work except in compliance with the License.<br/>
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software<br/>
distributed under the License is distributed on an "AS IS" BASIS,<br/>
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.<br/>
See the License for the specific language governing permissions and<br/>
limitations under the License.
