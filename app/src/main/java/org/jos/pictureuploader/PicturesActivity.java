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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PicturesActivity extends AppCompatActivity {

  static final int REQUEST_TOP_PICTURE_CAPTURE = 1;
  static final int REQUEST_EYES_PICTURE_CAPTURE = 2;
  static final int REQUEST_CHEST_PICTURE_CAPTURE = 3;

  private ImageView topPic;
  private ImageView eyesPic;
  private ImageView chestPic;

  private String currentPhotoPath;

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
        dispatchTakePictureIntent(REQUEST_TOP_PICTURE_CAPTURE);
      }
    });

    eyesPic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dispatchTakePictureIntent(REQUEST_EYES_PICTURE_CAPTURE);
      }
    });

    chestPic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dispatchTakePictureIntent(REQUEST_CHEST_PICTURE_CAPTURE);
      }
    });

    //TODO (jos) listing files for now, just for debugging purposes.
    Button back = (Button) findViewById(R.id.buttonBack);
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
  }


  //TODO (jos) pictureType should be an Enum so we can use it as an int and String
  //TODO (jos) delete these images when they are not needed anymore
  // delete and exist methods here: https://developer.android.com/reference/android/content/Context.html#getExternalFilesDir(java.lang.String)
  private File createImageFile(String pictureType) throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = pictureType + "_JPEG_" + timeStamp + "_";
    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    File image = File.createTempFile(
        imageFileName,  /* prefix */
        ".jpg",         /* suffix */
        storageDir      /* directory */
    );
    currentPhotoPath = image.getAbsolutePath();
    return image;
  }

  private void dispatchTakePictureIntent(int pictureRequested) {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

      //TODO (jos) use an Enum instead of all this madness.
      String picType = "";
      switch (pictureRequested) {
        case REQUEST_TOP_PICTURE_CAPTURE:
          picType = "TOP";
          break;
        case REQUEST_EYES_PICTURE_CAPTURE:
          picType = "EYES";
          break;
        case REQUEST_CHEST_PICTURE_CAPTURE:
          picType = "CHEST";
          break;
        default:
          Toast.makeText(this, "Something went wrong with the pictures", Toast.LENGTH_LONG);
          break;
      }

      File photoFile = null;
      try {
        photoFile = createImageFile(picType);
      } catch (IOException ex) {
        Toast.makeText(this, "Pictures cannot be stored.", Toast.LENGTH_LONG);
      }

      if (photoFile != null) {
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivityForResult(takePictureIntent, pictureRequested);
      }

    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
        case REQUEST_TOP_PICTURE_CAPTURE:
          setPic(topPic);
          break;
        case REQUEST_EYES_PICTURE_CAPTURE:
          setPic(eyesPic);
          break;
        case REQUEST_CHEST_PICTURE_CAPTURE:
          setPic(chestPic);
          break;
        default:
          Toast.makeText(this, "Something went wrong with the pictures", Toast.LENGTH_LONG);
          break;
      }
    } else {
      Toast.makeText(this, "Something went wrong with the camera. " +
          "Pictures are not available", Toast.LENGTH_LONG);
    }
  }

  /**
   * Scaling images automatically to avoid memory errors as explained at:
   * https://developer.android.com/training/camera/photobasics.html#TaskScalePhoto
   * @param mImageView
   */
  private void setPic(ImageView mImageView) {
    int targetW = mImageView.getWidth();
    int targetH = mImageView.getHeight();

    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
    bmOptions.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
    int photoW = bmOptions.outWidth;
    int photoH = bmOptions.outHeight;

    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

    // Decode the image file into a Bitmap sized to fill the View
    bmOptions.inJustDecodeBounds = false;
    bmOptions.inSampleSize = scaleFactor;
    bmOptions.inPurgeable = true;

    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
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
