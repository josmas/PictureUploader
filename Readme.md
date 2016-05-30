# A Data Collection app for a machine vision experiment 

## Intro
This is a _quick and dirty_ app to collect images of food. The same dish or food
item is photographed from three angles, from the top, from around your eyes (as
if you were wearing something like google glass), and from the chest, as if the
camera was in your clothes.

The goal is to get a dataset to train a classifier. The dataset will be made
available under a creative commons license.

## Usage
Fork/Clone and import into Android Studio. Build or install from the IDE.

## Requirements
The app uses Android's [JobScheduler](https://developer.android.com/reference/android/app/job/JobScheduler.html), so it is only available in API Level 21; that is Android 5, Lollipop.

## What does it do?
For now, it:

  - Allows to take three pictures (Top, Chest, and Eyes)
  - Zips up the files and schedules them for upload (Job Scheduler):
    - Only when the phone is idle
    - Only on non metered connectivity
    - A deadline of a maximum of 6 hours
  - The main activity shows all uploads scheduled and allows to upload manually

The uploads are to a server I am running. Only the pictures are uploaded, no personal information is collected on the phone.

The app has only been tested in an LG Nexus 4, and a OnePlus 2.

# Future work:
  - Use evernote's [android-job](https://github.com/evernote/android-job) for API level < 21
  - Fix a few [issues](https://github.com/josmas/PictureUploader/issues).

## Want to help?
Open an issue and use my handle so I get notified.

Jos - May 2o16
