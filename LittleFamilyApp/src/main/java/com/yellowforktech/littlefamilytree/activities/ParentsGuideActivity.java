package com.yellowforktech.littlefamilytree.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.yellowforktech.littlefamilytree.R;

public class ParentsGuideActivity extends LittleFamilyActivity {
    public static final String OK = "OK";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parents_guide);
    }

    public void dismiss(View view) {
        Intent intent = getIntent();
        intent.putExtra(OK, true);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
