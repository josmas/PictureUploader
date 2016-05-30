package org.jos.pictureuploader;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;

public class PictureListActivity extends AppCompatActivity {

  private ListView listView;
  Button uploadNow;
  private ProgressBar progress;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_picture_list);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    final Intent picturesActivityIntent = new Intent(this, PicturesActivity.class);
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(picturesActivityIntent);
      }
    });

    listView = (ListView) findViewById(R.id.list);
    progress = (ProgressBar) findViewById(R.id.progressBarUpload);
    progress.setVisibility(View.GONE);
    uploadNow = (Button) findViewById(R.id.upload_button);
    uploadNow.setVisibility(View.GONE);
    uploadNow.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        uploadFilesNow();
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    deleteLeftOverFiles();
    setAdapter();
  }

  private void setAdapter() {
    File[] listOfFiles = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();
    int arrayLength = 1; // At least one spot for the 'no items scheduled...' bit.
    if (listOfFiles.length > 0) {
      arrayLength = listOfFiles.length;
      uploadNow.setVisibility(View.VISIBLE);
    }
    String[] values = new String[arrayLength];
    values[0] = "No items scheduled for upload.";
    for (int i = 0; i < listOfFiles.length; i++) {
      values[i] = listOfFiles[i].getName();
    }
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_1, android.R.id.text1, values);
    listView.setAdapter(adapter);
  }

  private void deleteLeftOverFiles() {
    //TODO (jos) the following code would benefit from an AsyncTask or Handler.
    // At this stage it is safe to delete any CHEST_EYES_TOP picture hanging around
    File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    File[] listOfFiles = path.listFiles();
    for (File file : listOfFiles) {
      if (file.getName().contains("CHEST") ||
          file.getName().contains("EYE") ||
          file.getName().contains("TOP")) {
        file.delete();
      }
    }
  }

  private void uploadFilesNow() {
    uploadNow.setVisibility(View.GONE);
    progress.setVisibility(View.VISIBLE);
    final int numberOfFiles = 4; //TODO (jos) Read from Shared Prefs
    final int increment = 100 / numberOfFiles;
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i <= numberOfFiles; i++) {
          final int value = i;
          doFakeWork(); // This one has to block
          progress.post(new Runnable() { // Using a handler in the View object
            @Override
            public void run() {
              progress.setProgress(value * increment);
            }
          });
        }
        Log.i("PUL", "Now it's done uploading files");
        resetAdapter();
      }
    };
    new Thread(runnable).start();
  }

  // TODO (jos) DELETE ME!!! This will call the actual uploads for files.
  private void doFakeWork() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void resetAdapter() {
    String[] values = new String[1];
    values[0] = "No items scheduled for upload.";
    final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_1, android.R.id.text1, values);
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        progress.setProgress(0);
        progress.setVisibility(View.GONE);
        listView.setAdapter(adapter);
      }
    });
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
