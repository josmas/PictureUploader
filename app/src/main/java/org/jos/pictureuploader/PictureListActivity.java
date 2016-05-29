package org.jos.pictureuploader;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;

public class PictureListActivity extends AppCompatActivity {

  ListView listView;

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
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
        startActivity(picturesActivityIntent);
      }
    });

    listView = (ListView) findViewById(R.id.list);

    //TODO (jos) the following code would benefit from an AsyncTask and a loading gif.
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
    listOfFiles = path.listFiles(); // With the other files deleted - these are files still to send.
    int arrayLength = (listOfFiles.length > 0) ? listOfFiles.length : 1;
    String[] values = new String[arrayLength];
    values[0] = "No items scheduled for upload.";
    for (int i = 0; i < listOfFiles.length; i++) {
      values[i] = listOfFiles[i].getAbsolutePath();
    }
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_1, android.R.id.text1, values);
    listView.setAdapter(adapter);

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
