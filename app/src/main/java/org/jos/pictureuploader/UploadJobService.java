package org.jos.pictureuploader;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.jos.pictureuploader.ZippingService.PREFS_NAME;

/**
 * This service is run from a JobScheduler. When it is run, it will read all files in shared prefs
 * that are ready to be uploaded, and do so one by one (in a different thread). If for any reason
 * the upload fails, it will be tried again when the next job is scheduled. If the upload is
 * successful, the file name is deleted from shared preferences and from the private storage area.
 */
public class UploadJobService extends JobService {
  //TODO (jos) extract this into a settings screen
  private static final String REMOTE_URL = "http://52.35.33.152:8080/submit";

  @Override
  public boolean onStartJob(final JobParameters params) {
    final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    final Set<String> filesToUpload = settings.getStringSet("filesToUpload", new HashSet<String>());

    if (filesToUpload.isEmpty()) return false; //Nothing to do.

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        Set<String> removeList =new HashSet<>();
        for (String file : filesToUpload) {
          File path = new File(file);
          String result = Uploader.uploadFile(path, REMOTE_URL);
          if (result == null) {
            // Try again when the Job is rescheduled
            Log.i("PUL", "Upload did not finish. Will try again in next Job");
          }
          else {
            // Add the file to the remove list, as it has already been uploaded
            removeList.add(file);
            // If this fails and pictures do not get deleted, they will go away when uninstalling anyway
            // because they are in the private space of the app.
            path.delete();
          }
        }

        SharedPreferences.Editor editor = settings.edit();
        filesToUpload.removeAll(removeList);
        editor.putStringSet("filesToUpload", filesToUpload);
        editor.commit(); // It is fine to block until this is written

        jobFinished(params, false);

      }
    });
    t.start();
    return true; // Async execution above
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    // In this particular case, if this is called, the thread in onStartJob can continue in the
    // background. Return false to drop a reschedule operation.
    // The only potential issue is that if the thread above is running, this operation will make it
    // lose the wakelock. If that is the case, the upload will be done again next time the job is on.
    return false;
  }

}
