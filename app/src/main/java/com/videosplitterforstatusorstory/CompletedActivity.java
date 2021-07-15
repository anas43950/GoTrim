package com.videosplitterforstatusorstory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

public class CompletedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed);
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        String savedVideoPath = getString(R.string.trim_completed_file_save_location);
        SpannableStringBuilder builder = new SpannableStringBuilder(savedVideoPath);
        builder.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, savedVideoPath.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(" ");
        Intent intentThatStartedThisActivity=getIntent();
        builder.append(intentThatStartedThisActivity.getStringExtra("savedVideoPath"));

        TextView mTextView=(TextView) findViewById(R.id.trimCompletedTextView);
        mTextView.setText(builder);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}