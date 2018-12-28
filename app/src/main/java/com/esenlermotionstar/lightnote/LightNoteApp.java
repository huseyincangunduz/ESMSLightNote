package com.esenlermotionstar.lightnote;

import android.app.Application;

/**
 * Created by hussainlobo on 30.07.2018.
 */

public class LightNoteApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceUtil.overrideFont(getApplicationContext(), "DEFAULT", "fonts/ddinexp.otf");
        TypefaceUtil.overrideFont(getApplicationContext(), "MONOSPACE", "fonts/ddinexp.otf");
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/ddinexp.otf");
        TypefaceUtil.overrideFont(getApplicationContext(), "SANS", "fonts/ddinexp.otf");




    }
}
