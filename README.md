# Android-FFmpegExecutor
simple ffmpeg command executor for android<br />

ffmpeg binary in this project comes from<br />
https://github.com/WritingMinds/ffmpeg-android-java

## Update
### 1.0.1
ffmpeg binary has moved to app<br />
ffmpeg binary must be copied into internal storage

### 1.1.0
add constructor with InputStream

### 1.1.3
check if ffmpeg file has already copied

## Example

add build.gradle<br />
``` groovy
compile 'com.crust87:ffmpeg-executor:1.1.3'
```

```java
try {
    InputStream ffmpegFileStream = getApplicationContext().getAssets().open("ffmpeg");
    mExecutor = new FFmpegExecutor(getApplicationContext(), ffmpegFileStream);
} catch (IOException e) {
    // TODO something
} catch (InterruptedException e) {
    // TODO something
}
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

## Documents
### Java
| Public Methods | |
|:---|:---|
| void | init()<br />Reset FFmpeg command |
| FFmpegExecuter | putCommand(String command)<br />Add ffmpeg command, It can be Method chaining |
| void | executeCommand()<br />Execute FFmpeg with added command, this method throws IOException |
| void | destroy()<br /> Destroy FFmpeg process, not tested |
| void | setOnReadProcessLineListener(OnReadProcessLineListener pOnReadProcessLineListener)<br />Add listener lesten read line from FFmpeg process |


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
