package com.yellowforktech.littlefamilytree.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.yellowforktech.littlefamilytree.R;

public class ParentsGuideActivity extends FragmentActivity {
    public static final String OK = "OK";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parents_guide);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ParentsGuideDialog dialog = new ParentsGuideDialog();
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Light_Dialog);
        dialog.show(getSupportFragmentManager(), "Parent's Guide");
    }

    public void dismiss(View view) {
        Intent intent = getIntent();
        intent.putExtra(OK, true);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void launchWebsite(View view) {
        Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse("http://www.littlefamilytree.com"));
        startActivity(intent);
    }

    public void launchKidHeritage(View view) {
        Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse("http://www.myheritagebook.com"));
        startActivity(intent);
    }
}
