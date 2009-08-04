package com.google.ase.interpreter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {
  private StreamUtils() {
    // Utility class.
  }

  /**
   * Copy input stream to output stream and return the number of bytes copied.
   *
   * @param in
   * @param out
   * @throws IOException
   */
  public static int copyInputStream(InputStream in, OutputStream out) throws IOException {
    int bytesCopied = 0;
    byte[] buffer = new byte[1024];
    int bytesReadToBuffer;
    while ((bytesReadToBuffer = in.read(buffer)) >= 0) {
      out.write(buffer, 0, bytesReadToBuffer);
      bytesCopied += bytesReadToBuffer;
    }
    in.close();
    out.close();
    return bytesCopied;
  }

  /**
   * Copy input stream to file output stream.
   *
   * @param in
   * @param out
   * @throws IOException
   */
  public static int copyInputStream(InputStream in, File destination) throws IOException {
    FileOutputStream out = new FileOutputStream(destination);
    BufferedOutputStream bufferedOut = new BufferedOutputStream(out, 1024 * 8);
    return copyInputStream(in, bufferedOut);
  }
}
