package com.esenlermotionstar.lightnote;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class about extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
    void openLink()
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
        startActivity(browserIntent);
    }

    public void esmsTrLink(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://esenlermotionstar.blogspot.com"));
        startActivity(browserIntent);

    }

    public void esmsEnLink(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://en-esenlermotionstar.blogspot.com"));
        startActivity(browserIntent);
    }

    public void constituentparts(View view) {
        Intent itd = new Intent(this,OpenSourceLisences.class);
        itd.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(itd);
    }
}
