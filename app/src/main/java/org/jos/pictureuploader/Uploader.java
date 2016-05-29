package org.jos.pictureuploader;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Functionality for uploading pictures. This class will block the thread it is called on, so DO NOT
 * call from the UI thread.
 */
public class Uploader {

  private static final MediaType MEDIA_TYPE_ZIP = MediaType.parse("application/zip");

  private static final OkHttpClient client = new OkHttpClient();

  /**
   * Do not call from the UI thread. This method blocks.
   * @param file the path to the file to be uploaded.
   */
  static String uploadFile(File file, String remoteUrl){
    try {
      return post(file, remoteUrl);
    } catch (IOException e) {
      System.out.println("Yeah, SOMETHING WENT SO WRONG: " + e.getMessage());
      e.printStackTrace(); //TODO (jos) throw this to the JobScheduler?
    }
    return null;
  }

  private static String post(File file, String remoteUrl) throws IOException {

    if (file == null) {
      return null;
    }

    RequestBody requestBody = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE_ZIP, file))
        .build();

    Request request = new Request.Builder()
        .url(remoteUrl)
        .post(requestBody)
        .build();

    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

    System.out.println(response.body().string());
    return response.body().string();

  }
}
