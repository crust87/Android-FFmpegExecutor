package com.crust87.ffmpegexecutor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mabi on 2015. 12. 18..
 */
public class FileMover {
    private InputStream mInputStream;
    private File mDestination;

    public FileMover(InputStream inputStream, File destination) {
        mInputStream = inputStream;
        mDestination = destination;
    }

    public void moveIt() throws IOException {
        OutputStream destinationOut = new BufferedOutputStream(new FileOutputStream(mDestination));

        int numRead;
        byte[] buf = new byte[1024];
        while ((numRead = mInputStream.read(buf) ) >= 0) {
            destinationOut.write(buf, 0, numRead);
        }

        destinationOut.flush();
        destinationOut.close();
    }
}
