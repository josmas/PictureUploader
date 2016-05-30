package org.jos.pictureuploader;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.jos.pictureuploader.UploadJobService.REMOTE_URL;
import static org.jos.pictureuploader.ZippingService.PREFS_NAME;

public class PictureListActivity extends AppCompatActivity {

  private ListView listView;
  private Button uploadNow;
  private ProgressBar progress;
  private TextView uploadNote;

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
    uploadNote = (TextView) findViewById(R.id.upload_note);
    uploadNote.setVisibility(View.GONE);
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
    String[] values;
    File[] listOfFiles = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();
    int arrayLength = 1; // At least one spot for the 'no items scheduled...' bit.
    if (listOfFiles != null && listOfFiles.length > 0) {
      arrayLength = listOfFiles.length;
      uploadNow.setVisibility(View.VISIBLE);
      uploadNote.setVisibility(View.VISIBLE);
      values = new String[arrayLength];
      for (int i = 0; i < listOfFiles.length; i++) {
        values[i] = listOfFiles[i].getName();
      }
    }
    else {
      values = new String[arrayLength];
      values[0] = "No items scheduled for upload.";
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
    if (listOfFiles != null) {
      for (File file : listOfFiles) {
        if (file.getName().contains("CHEST") ||
            file.getName().contains("EYE") ||
            file.getName().contains("TOP")) {
          file.delete();
        }
      }
    }
  }

  private void uploadFilesNow() {
    uploadNow.setVisibility(View.GONE);
    progress.setVisibility(View.VISIBLE);
    File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    final File[] listOfFiles = path.listFiles();

    if (listOfFiles == null || listOfFiles.length == 0) {
      // This should never be the case, because if there are no files, the button is inactive.
      resetAdapter();
      return;
    }

    final int numberOfFiles = listOfFiles.length;
    final int increment = 100 / numberOfFiles;
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < listOfFiles.length; i++) {
          final int times = i;
          File path = listOfFiles[i];
          String result = Uploader.uploadFile(path, REMOTE_URL);
          if (result == null) {
            // Try again when the Job is rescheduled or the button is pressed again
            Log.i("PUL", "Upload did not finish in main activity. Will try again in next Job");
          }
          else {
            // If this fails and pictures do not get deleted, they will go away when uninstalling anyway
            // because they are in the private space of the app.
            path.delete();
            progress.post(new Runnable() { // Using a handler in the View object
              @Override
              public void run() {
                progress.setProgress(times * increment);
              }
            });
          }
        }

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        final Set<String> filesToUpload = new HashSet<>();
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet("filesToUpload", filesToUpload);
        editor.commit(); // It is fine to block until this is written
        JobScheduler js = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        js.cancelAll();

        resetAdapter();
      }
    };
    new Thread(runnable).start();
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
        uploadNote.setVisibility(View.GONE);
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
