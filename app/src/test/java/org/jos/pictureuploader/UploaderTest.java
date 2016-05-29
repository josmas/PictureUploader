package org.jos.pictureuploader;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class UploaderTest {
  private static final String REMOTE_URL = "http://52.35.33.152:8080/submit";

  @Test
  public void upload_null_file() throws Exception {
    assertNull(Uploader.uploadFile(null, REMOTE_URL));
  }

  @Test
  public void upload_real_file() throws Exception {
    File realFile = new File(System.getProperty("user.dir") + "/../test_data/20160528_184848_ZIP_-396541634.zip");
    assertNotNull(Uploader.uploadFile(realFile, REMOTE_URL));
  }
}