package org.jos.pictureuploader;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * An {@link IntentService} subclass to Zip a number of files into a compressed file
 */
public class ZippingService extends IntentService {

  private static final int BUFFER_SIZE = 2048;
  public static final String FILES_HASH = "currentNames";

  public ZippingService() {
    super("ZippingService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (intent != null) {
      String[] files = intent.getStringArrayExtra(FILES_HASH);
      zipAllFiles(files);
      deleteAllFiles(files);
      // TODO (jos) Schedule UploadService (to be written) to eventually upload pictures
    }
  }

  /**
   * Based in http://www.jondev.net/articles/Zipping_Files_with_Android_(Programmatically)
   * @param files the files to Zip, as an array of Strings as AbsolutePaths
   */
  public void zipAllFiles(String[] files) {

    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = timeStamp + "_ZIP_";
    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    File zipFile;
    ZipOutputStream out = null;
    BufferedInputStream origin;
    try {
      zipFile = File.createTempFile(imageFileName, ".zip", storageDir);
      out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
      byte data[] = new byte[BUFFER_SIZE];

      for (int i = 0; i < files.length; i++) {
        FileInputStream fi = new FileInputStream(files[i]);
        origin = new BufferedInputStream(fi, BUFFER_SIZE);
        try {
          ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
          out.putNextEntry(entry);
          int count;
          while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
            out.write(data, 0, count);
          }
        }
        finally {
          origin.close();
        }
      }
    }
    catch(Exception e) {
      Log.e("PUL", "Error creating Zip file: " + e.getMessage()); // Not much can be done
    }
    finally {
      try {
        out.close();
      } catch (IOException e) {
        Log.e("PUL", "Error closing the output stream to Zip files: " + e.getMessage());
      }
    }
  }

  private void deleteAllFiles(String[] files) {
    for (String file : files) {
      deleteExternalStoragePrivateFile(file);
    }
  }

  void deleteExternalStoragePrivateFile(String file) {
    // If this fails and pictures do not get deleted, they will go away when uninstalling anyway
    // because they are in the private space of the app. Also some pictures will be left behind if
    // more than one pic for a type is taken in one set.
    File path = new File(file);
    if (path != null) {
      path.delete();
    }
  }
}
