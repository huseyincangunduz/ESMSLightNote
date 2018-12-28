package com.esenlermotionstar.lightnote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.apache.commons.lang3.ObjectUtils;

import java.io.File;
import java.io.FileFilter;

import static android.content.ContentValues.TAG;

/**
 * Created by hussainlobo on 21.06.2018.
 */
class ItemImageUpdaterParameter {
    View v;
    String workFolder;
}

public class ItemImageUpdater extends AsyncTask<File, Void, Bitmap> {

    View LNtItemView;

    public ItemImageUpdater(View lntitemView_) {
        LNtItemView = lntitemView_;
    }

    @Override
    protected void onPreExecute() {
        /*if (LNtItemView == null)
            this.cancel(true);*/

        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if(LNtItemView != null)
        {
            ImageView imgview = ((ImageView) LNtItemView.findViewById(R.id.noteThumbnail));

            if (imgview != null)
            {
                if (bitmap != null) {
                    imgview.setImageBitmap(bitmap);
                    Log.i(null, "onPostExecute: bitmap eklendi");
                }
                else
                {
                    imgview.setImageResource(R.drawable.notethumbnail);
                }
            }
        }


    }

    @Override
    protected Bitmap doInBackground(File... workDirs) {
        for (int ik = 0; ik < workDirs.length; ik++)
        {
            File fil = workDirs[ik];
            if (fil.exists() && fil.isDirectory()) {

                FileFilter filtre = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        String fnm = file.getName();
                        Long len = file.length();
                        Log.i(null,"FILE: " + file.getName() +",LENGHT: " + String.valueOf(len) +  " Bytes");
                        return (len <= (200*1024)) && (fnm.endsWith(".png") ||
                                fnm.endsWith((".jpg")) ||
                                fnm.endsWith((".jpeg")) ||
                                fnm.endsWith((".gif")));

                    }
                };
                File[] files = fil.listFiles(filtre);
                if (files.length > 0) {
                    return BitmapFactory.decodeFile(files[0].getAbsolutePath());

                }
            }
        }

        return null;
    }


}
