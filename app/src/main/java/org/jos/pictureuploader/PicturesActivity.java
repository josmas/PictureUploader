package org.jos.pictureuploader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class PicturesActivity extends AppCompatActivity {

  static final int REQUEST_TOP_PICTURE_CAPTURE = 1;
  static final int REQUEST_EYES_PICTURE_CAPTURE = 2;
  static final int REQUEST_CHEST_PICTURE_CAPTURE = 3;

  private ImageView topPic;
  private ImageView eyesPic;
  private ImageView chestPic;

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
  }

  private void dispatchTakePictureIntent(int pictureRequested) {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      startActivityForResult(takePictureIntent, pictureRequested);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      Bundle extras = data.getExtras();
      Bitmap imageBitmap = (Bitmap) extras.get("data");
      switch (requestCode) {
        case REQUEST_TOP_PICTURE_CAPTURE:
          topPic.setImageBitmap(imageBitmap);
          break;
        case REQUEST_EYES_PICTURE_CAPTURE:
          eyesPic.setImageBitmap(imageBitmap);
          break;
        case REQUEST_CHEST_PICTURE_CAPTURE:
          chestPic.setImageBitmap(imageBitmap);
          break;
        default:
          Toast.makeText(this, "Something went wrong with the pictures", Toast.LENGTH_LONG);
          break;
        }
    }
    else {
      Toast.makeText(this, "Something went wrong with the camera. " +
          "Pictures are not available", Toast.LENGTH_LONG);
    }

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
