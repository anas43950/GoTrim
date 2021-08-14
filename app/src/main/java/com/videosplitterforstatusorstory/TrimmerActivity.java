package com.videosplitterforstatusorstory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import android.text.Editable;

import android.text.TextWatcher;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.arthenica.mobileffmpeg.AsyncFFmpegExecuteTask;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.FFmpegExecution;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.File;
import java.util.concurrent.Executors;


public class TrimmerActivity extends AppCompatActivity {

    private VideoView mVideoView;
    private EditText startFromPositionEditText, durationEditText;
    private int startFromPosition, duration;
    private Button trimVideoButton;
    private final String TAG = "TrimmerActivity";
    private String videoUrl;
    private Uri videoUri;
    private static final String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString();
    private static final String app_folder = root + "/VideoTrimmer/";
    private SeekBar seekBar;
    private Handler mHandler;
    private LinearLayout linearLayout;
    private TextView trimCompletedTextView;
    private CircularProgressIndicator circularProgressIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trimmer);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        }
        //Initializing Views
        mVideoView = findViewById(R.id.videoView);
        trimVideoButton = findViewById(R.id.TrimButton);
        startFromPositionEditText = findViewById(R.id.start_from_edit_text);
        durationEditText = findViewById(R.id.duration_edit_text);
        linearLayout = findViewById(R.id.linearLayout);
        circularProgressIndicator = findViewById(R.id.progressIndicator);


        circularProgressIndicator.setVisibility(View.GONE);
        startFromPositionEditText.setRawInputType(Configuration.KEYBOARD_12KEY);
        durationEditText.setRawInputType(Configuration.KEYBOARD_12KEY);
        seekBar = findViewById(R.id.seekBar);
        ImageButton mImageButton = findViewById(R.id.imageButton);
        mHandler = new Handler(Looper.getMainLooper());
        //Setting startFromPosition text change listener to keep seekbar in sync with edittext value
        startFromPositionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int i = (Integer.parseInt(s.toString())) * 1000;
                    if (i < mVideoView.getDuration()) {
                        mVideoView.seekTo(i);
                        seekBar.setProgress(i);
                    }

                } catch (NumberFormatException e) {
                }

            }
        });


        Intent intentThatStartedThisActivity = getIntent();
        videoUri = Uri.parse(intentThatStartedThisActivity.getStringExtra(MainActivity.selectedVideoPathForIntent));
        try {
            File video_file = FileUtils.getFileFromUri(this, videoUri);
            videoUrl = video_file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }


        mVideoView.setVideoURI(videoUri);

        mVideoView.seekTo(1);
        mVideoView.setOnPreparedListener(l -> {
            updateProgressBar();
        });
        mVideoView.setOnClickListener(l -> {
            if (mImageButton.getVisibility() == View.INVISIBLE) {
                mVideoView.pause();
                mImageButton.setVisibility(View.VISIBLE);
            } else if (mImageButton.getVisibility() == View.VISIBLE) {
                mImageButton.setVisibility(View.INVISIBLE);
            }
        });
        mVideoView.setOnCompletionListener(l -> {
            mVideoView.seekTo(1);
            mVideoView.start();
        });

        //setting seekBarChangeListener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int i = progress / 1000;
                    startFromPositionEditText.setText(String.valueOf(i));
                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(updateTimeTask);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(updateTimeTask);
                mVideoView.seekTo(seekBar.getProgress());


                updateProgressBar();


            }
        });


        trimVideoButton.setOnClickListener(l -> {
            try {

                startFromPosition = Integer.parseInt(startFromPositionEditText.getText().toString());
                duration = Integer.parseInt(durationEditText.getText().toString());
                if (duration < 5 || duration > (mVideoView.getDuration()) / 1000 || String.valueOf(duration).equals("")) {

                    Toast.makeText(this, R.string.min_duration_error, Toast.LENGTH_SHORT).show();
                } else if (startFromPosition < 0 || String.valueOf(startFromPosition).equals("") || startFromPosition > (mVideoView.getDuration()) / 1000) {
                    Toast.makeText(this, R.string.start_from_duration_error, Toast.LENGTH_SHORT).show();
                } else if (duration < 5 || duration > (mVideoView.getDuration()) / 1000 && startFromPosition < 0 || startFromPosition > (mVideoView.getDuration()) / 1000) {
                    Toast.makeText(this, R.string.min_and_start_from_duration_error, Toast.LENGTH_SHORT).show();

                } else {
                    linearLayout.setVisibility(View.GONE);
                    trimVideoButton.setVisibility(View.GONE);

                    executeTrimCommand(startFromPosition, duration);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.empty_values_error, Toast.LENGTH_SHORT).show();
            }
        });
        mImageButton.setOnClickListener(l -> {
            if (!mVideoView.isPlaying()) {
                mVideoView.start();
                mImageButton.setVisibility(View.INVISIBLE);

            } else {
                mVideoView.pause();
                mImageButton.setVisibility(View.VISIBLE);

            }

        });


    }

    //Method to sync seekbar with videoview
    private void updateProgressBar() {
        mHandler.postDelayed(updateTimeTask, 100);
    }

    private final Runnable updateTimeTask = new Runnable() {

        @Override
        public void run() {
            long currentDuration = mVideoView.getCurrentPosition();

            // update progress bar
            seekBar.setMax(mVideoView.getDuration());

            seekBar.setProgress((int) currentDuration);


            mHandler.postDelayed(this, 100);

        }
    };


    private void executeTrimCommand(int startMs, int eachSplitDuration) throws Exception {
        // creating a new file in storage
        final String filePath;
        String filePrefix = "trim_video";
        String fileExtn = "%d.mp4";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // With introduction of scoped storage in Android Q the primitive method gives error
            // So, it is recommended to use the below method to create a video file in storage.
            ContentValues valuesvideos = new ContentValues();
            valuesvideos.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "VideoTrimmer");
            valuesvideos.put(MediaStore.Video.Media.TITLE, filePrefix + System.currentTimeMillis());
            valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, filePrefix + System.currentTimeMillis() + fileExtn);
            valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            valuesvideos.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            valuesvideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
            Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, valuesvideos);

            // get the path of the video file created in the storage.
            File file = FileUtils.getFileFromUri(this, uri);
            filePath = file.getAbsolutePath();

        } else {
            // This else statement will work for devices with Android version lower than 10
            // Here, "app_folder" is the path to your app's root directory in device storage
            File dest = new File(new File(app_folder), filePrefix + fileExtn);
            int fileNo = 0;
            // check if the file name previously exist. Since we don't want
            // to overwrite the video files
            while (dest.exists()) {
                fileNo++;
                dest = new File(new File(app_folder), filePrefix + fileNo + fileExtn);
            }
            // Get the filePath once the file is successfully created.
            filePath = dest.getAbsolutePath();
        }

//        String[] complexCommand1 = {"-ss", "" + startMs, "-y", "-i", videoUrl, "-t", "" + eachSplitDuration, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};
        String[] complexCommand = {"-ss", "" + startMs, "-i", videoUrl, "-f", "segment", "-segment_time", "" + eachSplitDuration, "-reset_timestamps", "1", "-vcodec", "copy", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};
        String[] filePathArray = filePath.split(filePrefix);


//        new Thread(() -> {
//
//            FFmpeg.execute(complexCommand);
//            runOnUiThread(() -> {
//                Intent startCompletedActivityIntent = new Intent(TrimmerActivity.this, CompletedActivity.class);
//
//                startCompletedActivityIntent.putExtra("savedVideoPath", filePathArray[0]);
//                startActivity(startCompletedActivityIntent);
//            });
//
//        }).start();
        Runnable executeCommandOnBGThread = new Runnable() {
            @Override
            public void run() {
                FFmpeg.execute(complexCommand);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent startCompletedActivityIntent = new Intent(TrimmerActivity.this, CompletedActivity.class);

                        startCompletedActivityIntent.putExtra("savedVideoPath", filePathArray[0]);
                        startActivity(startCompletedActivityIntent);
                    }
                });

            }
        };

        Executors.newSingleThreadExecutor().execute(executeCommandOnBGThread);


    }


}


