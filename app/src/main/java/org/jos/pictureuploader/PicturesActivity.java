package org.jos.pictureuploader;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.jos.pictureuploader.ZippingService.PREFS_NAME;

public class PicturesActivity extends AppCompatActivity {

  private enum PictureTypes {

    TOP (1), EYES (2), CHEST(3);

    PictureTypes(int valueType) {
      this.valueType = valueType;
    }
    private final int valueType;
    public int getValueType() { return valueType; }
  }

  private ImageView topPic, eyesPic, chestPic;
  private Button readyToZip, back;
  private Map<PictureTypes, File> currentNames = new HashMap<>();
  private int[] currentNumberOfPpictures = {0, 0, 0};

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
    back.setVisibility(View.GONE); // hiding this for now
    back.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.i("PUL", "--------------- Files In Private area ---------------------");
        File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] listOfFiles = path.listFiles();
        if (listOfFiles != null) {
          for (File file : listOfFiles) {
            Log.i("PUL", file.getAbsolutePath());
          }
        }
        Log.i("PUL", "-----------------------------------------------------------");
        Log.i("PUL", "--------------- Files In Shared Prefs ---------------------");
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        Set<String> filesToUpload = settings.getStringSet("filesToUpload", Collections.<String>emptySet());
        for (String filePath : filesToUpload) {
          Log.i("PUL", "ZIP: " + filePath);
        }

      }
    });

    readyToZip = (Button) findViewById(R.id.readyToZip);
    readyToZip.setEnabled(false);
    readyToZip.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        readyToZip.setEnabled(false);
        currentNumberOfPpictures[0] = 0;
        currentNumberOfPpictures[1] = 0;
        currentNumberOfPpictures[2] = 0;
        resetAllImageViewers();
        startZippingService(currentNames);
      }
    });
  }

  private void resetAllImageViewers() {
    topPic.setImageResource(android.R.drawable.ic_menu_camera);
    eyesPic.setImageResource(android.R.drawable.ic_menu_camera);
    chestPic.setImageResource(android.R.drawable.ic_menu_camera);
  }

  /**
   * This is a fire and forget call to the Zipping Service. Zipped files will eventually be uploaded
   * in a different service.
   * @param currentNamesMap the File paths to the actual files on disk
   */
  private void startZippingService(Map<PictureTypes, File> currentNamesMap) {
    Intent intent = new Intent(this, ZippingService.class);
    String[] files = { currentNamesMap.get(PicturesActivity.PictureTypes.TOP).getAbsolutePath(),
        currentNamesMap.get(PicturesActivity.PictureTypes.EYES).getAbsolutePath(),
        currentNamesMap.get(PicturesActivity.PictureTypes.CHEST).getAbsolutePath() };
    intent.putExtra(ZippingService.FILES_HASH, files);
    startService(intent);
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
      if (PictureTypes.TOP.getValueType() == requestCode) {
        currentNumberOfPpictures[0] = 1;
        setPic(topPic, PictureTypes.TOP);
      }
      else if (PictureTypes.EYES.getValueType() == requestCode) {
        currentNumberOfPpictures[1] = 1;
        setPic(eyesPic, PictureTypes.EYES);
      }
      else if (PictureTypes.CHEST.getValueType() == requestCode) {
        currentNumberOfPpictures[2] = 1;
        setPic(chestPic, PictureTypes.CHEST);
      }

      if (allPicsAvailable()) readyToZip.setEnabled(true);
    }
    else {
      Toast.makeText(this, "Something went wrong with the camera. " +
          "Pictures are not available", Toast.LENGTH_LONG);
    }
  }

  private boolean allPicsAvailable() {
    for (int picNum : currentNumberOfPpictures) {
      if (picNum == 0) return false;
    }
    return true;
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
