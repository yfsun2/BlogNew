package com.syf.blognew.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.syf.blognew.R;

public class DetailActivity extends AppCompatActivity {

    private TextView blog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        blog=findViewById(R.id.blog);
        blog.setText(getIntent().getStringExtra("blog"));
    }
}