package org.jos.pictureuploader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PicturesActivity extends AppCompatActivity {

  private enum PictureTypes {

    TOP (1), EYES (2), CHEST(3);

    PictureTypes(int valueType) {
      this.valueType = valueType;
    }
    private final int valueType;
    public int getValueType() { return valueType; }
  }

  private static final int BUFFER_SIZE = 2048;

  private ImageView topPic, eyesPic, chestPic;
  private Button readyToZip, back;

  //TODO (jos) does this one need to be reset in onResume/onPause?
  private Map<PictureTypes, File> currentNames = new HashMap<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pictures);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    topPic = (ImageView) findViewById(R.id.pic_top);
    eyesPic = (ImageView) findViewById(R.id.pic_eyes);
    chestPic = (ImageView) findViewById(R.id.pic_chest);

    topPic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dispatchTakePictureIntent(PictureTypes.TOP);
      }
    });

    eyesPic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dispatchTakePictureIntent(PictureTypes.EYES);
      }
    });

    chestPic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dispatchTakePictureIntent(PictureTypes.CHEST);
      }
    });

    //TODO (jos) listing files for now, just for debugging purposes.
    back = (Button) findViewById(R.id.buttonBack);
    back.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] listOfFiles = path.listFiles();
        for (File file : listOfFiles) {
          Log.i("PUL", file.getName());
          Log.i("PUL", file.getAbsolutePath());
        }
      }
    });

    readyToZip = (Button) findViewById(R.id.readyToZip);
    readyToZip.setEnabled(false);
    readyToZip.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (PictureTypes file : currentNames.keySet()) {
          Log.i("PUL", currentNames.get(file).getAbsolutePath());
        }
        readyToZip.setEnabled(false);
         resetAllImageViewers();
         zipAllFiles(currentNames); // Send them off to the cloud if possible, but in a service
        //TODO (jos) as per method names
        // deleteAllOriginals(path.listFiles()); //Including any extra pictures that might have been taken
        // delete and exist methods here: https://developer.android.com/reference/android/content/Context.html#getExternalFilesDir(java.lang.String)
        // reset currentNames currentNames = new HashMap<>();
      }
    });

  }

  private void resetAllImageViewers() {
    topPic.setImageResource(android.R.drawable.ic_menu_camera);
    eyesPic.setImageResource(android.R.drawable.ic_menu_camera);
    chestPic.setImageResource(android.R.drawable.ic_menu_camera);
  }

  /**
   * Based in http://www.jondev.net/articles/Zipping_Files_with_Android_(Programmatically)
   * @param currentNamesMap TODO (jos) Moving this somewhere else (like a service)
   * @throws IOException
   */
  public void zipAllFiles(Map<PictureTypes, File> currentNamesMap) {

    long startTime = System.nanoTime(); //TODO (jos) After having timed this, it needs a background process
    Log.i("PUL", "STARTING TO ZIP STUFF!");
    String[] files = { currentNamesMap.get(PictureTypes.TOP).getAbsolutePath(),
        currentNamesMap.get(PictureTypes.EYES).getAbsolutePath(),
        currentNamesMap.get(PictureTypes.CHEST).getAbsolutePath() };

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
      Log.e("PUL", "Exceptions during zipping: " + e.getMessage()); //TODO (jos) shall retry later?
    }
    finally {
      try {
        out.close();
      } catch (IOException e) {
        e.printStackTrace(); //TODO (jos) catch this exception
      }
    }

    //TODO (jos) timing, delete!
    long endTime = System.nanoTime();
    long duration = ((endTime - startTime) / 1000000000);
    Log.i("PUL", "The time to zip the pictures in seconds is: " + duration);
  }

  private File createImageFile(PictureTypes pictureType) throws IOException {
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = pictureType + "_JPEG_" + timeStamp + "_";
    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    File image = File.createTempFile(
        imageFileName,  /* prefix */
        ".jpg",         /* suffix */
        storageDir      /* directory */
    );
    currentNames.put(pictureType, image);
    return image;
  }

  private void dispatchTakePictureIntent(PictureTypes pictureRequested) {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      File photoFile = null;
      try {
        photoFile = createImageFile(pictureRequested);
      } catch (IOException ex) {
        Toast.makeText(this, "Pictures cannot be stored.", Toast.LENGTH_LONG);
      }

      if (photoFile != null) {
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivityForResult(takePictureIntent, pictureRequested.getValueType());
      }

    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      if (PictureTypes.TOP.getValueType() == requestCode) setPic(topPic, PictureTypes.TOP);
      else if (PictureTypes.EYES.getValueType() == requestCode) setPic(eyesPic, PictureTypes.EYES);
      else if (PictureTypes.CHEST.getValueType() == requestCode) setPic(chestPic, PictureTypes.CHEST);

      if (currentNames.keySet().size() == 3) readyToZip.setEnabled(true);
    }
    else {
      Toast.makeText(this, "Something went wrong with the camera. " +
          "Pictures are not available", Toast.LENGTH_LONG);
    }
  }

  /**
   * Scaling images automatically to avoid memory errors as explained at:
   * https://developer.android.com/training/camera/photobasics.html#TaskScalePhoto
   * @param mImageView
   */
  private void setPic(ImageView mImageView, PictureTypes pictureType) {
    int targetW = mImageView.getWidth();
    int targetH = mImageView.getHeight();

    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
    bmOptions.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(currentNames.get(pictureType).getAbsolutePath(), bmOptions);
    int photoW = bmOptions.outWidth;
    int photoH = bmOptions.outHeight;

    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

    // Decode the image file into a Bitmap sized to fill the View
    bmOptions.inJustDecodeBounds = false;
    bmOptions.inSampleSize = scaleFactor;
    bmOptions.inPurgeable = true;

    Bitmap bitmap = BitmapFactory.decodeFile(currentNames.get(pictureType).getAbsolutePath(), bmOptions);
    mImageView.setImageBitmap(bitmap);
  }

  @Override
  protected void onPause() {
    //TODO (jos) If all three pictures exist, make sure they are saved before leaving.
    // Should I save the current pictures in the bundle if not all 3 have been taken?
    super.onPause();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_picture_list, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

}
