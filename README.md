# Android-FFmpegExecuter
simple ffmpeg command executer for android

Something wrong on this project....sample application module is bigger than library module. i will split app modul.

## Example
```java
mExecuter = new FFmpegExecuter(getApplicationContext());
```

if you want to know ffmpeg log while process running, set this listener 
```java
mExecuter.setOnReadProcessLineListener(new FFmpegExecuter.OnReadProcessLineListener() {
    @Override
    public void onReadProcessLine(String line) {
        // TODO something
    }
});
```

you must call init() before put command<br/>
put some commands
```java
mExecuter = new FFmpegExecuter(getApplicationContext(), ffmpegPath);

mExecuter.init();

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
    .putCommand(Environment.getExternalStorageDirectory().getAbsolutePath() + "/result.mp4");
```

and execute command
```java
mExecuter.executeCommand();
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
