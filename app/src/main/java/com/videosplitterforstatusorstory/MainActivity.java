package com.videosplitterforstatusorstory;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;


import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;



public class MainActivity extends AppCompatActivity {
    public static final String selectedVideoPathForIntent = "selectedVideoPath";

    private static final String TAG = "MainActivity";
    private Uri selectedVideoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        Button videoPickerButton = findViewById(R.id.videoPickerButton);
        videoPickerButton.setOnClickListener(v -> {
            if (isStoragePermissionGranted()) {
                Intent videoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(videoPickerIntent);
            }


        });


    }


    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            assert result.getData() != null;
                            selectedVideoPath = result.getData().getData();

                            if (selectedVideoPath == null) {

                                Log.e("MainActivity", "selected video path = null!");
                                finish();
                            } else {
                                Log.d(TAG,selectedVideoPath.toString());
                                Intent sendVideoToTrimmer = new Intent(MainActivity.this, TrimmerActivity.class);
                                sendVideoToTrimmer.putExtra(selectedVideoPathForIntent, selectedVideoPath.toString());
                                startActivity(sendVideoToTrimmer);


                            }
                        }
                    });


    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                askForStoragePermission();
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    private void askForStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
    }


}

