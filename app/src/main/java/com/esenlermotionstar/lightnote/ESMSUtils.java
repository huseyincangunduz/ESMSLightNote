package com.esenlermotionstar.lightnote;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import static java.sql.DriverManager.println;

/**
 * Created by hussainlobo on 18.06.2018.
 */

public class ESMSUtils {

    public static void main(String[]args){
        String s1, s2;
        System.out.println("\n İlk Dosya yolu");
        s1 = (new Scanner(System.in)).nextLine();
        System.out.println("\n 2. Dosya yolu");
        s2 = (new Scanner(System.in)).nextLine();
        System.out.println(CompareFileAsSame(new File(s1),new File(s2)));
    }


    public static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public static void copyInputStreamUsingStream(InputStream inpStream, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = inpStream;
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public static boolean AcceptFile(File sel, String... Extensions) {
        Boolean valid = false;
        for (int i = 0; i < Extensions.length; i++) {
            valid = sel.getName().endsWith(Extensions[i]);
            if (valid) break;
        }
        return valid;
    }

    public static String makeTitleUsuableAtFiles(String s) {
        return s.toLowerCase().replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public static String getNowAsString() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }


    public interface onClickedAtInputBox {
        void OnClicked(String entered);
    }

    public static void inputBox(Context ApplicationContext, String message, final onClickedAtInputBox onOK) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ApplicationContext);
        builder.setTitle(message);

        // Set up the input
        final EditText input = new EditText(ApplicationContext);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onOK.OnClicked(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static boolean CompareFileAsSame(File f1, File f2) {

        try {
            return FileUtils.contentEquals(f1,f2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }


    public static ProgressDialog showProgressDialog(Context ctx, String title, String message) {

        return ProgressDialog.show(ctx, title, message, true);
    }

    static File createThumbnailImage(File imageSrc, File exportFolder, Point scaledSize) {
        try {

            if (imageSrc.exists() && imageSrc.length() > 500 * 1024) {
                String safeFile = imageSrc.getName();

                Bitmap bmp = BitmapFactory.decodeFile(imageSrc.getAbsolutePath());

                if (bmp.getWidth() > scaledSize.x) {

                    if (!exportFolder.exists()) exportFolder.mkdirs();
                    int Width = bmp.getWidth();
                    float Scale = (float) (Width / (scaledSize.x * (0.9)));

                    bmp = Bitmap.createScaledBitmap(bmp, (int) (Width / Scale), (int) (bmp.getHeight() / Scale), false);

                    String absPath = exportFolder.getAbsolutePath();
                    if (!absPath.endsWith("/")) absPath = absPath + "/";

                    String scaledBitmapFile = absPath + safeFile;
                    File f = new File(scaledBitmapFile);
                    if (!f.exists()) f.createNewFile();

                    FileOutputStream fos = new FileOutputStream(scaledBitmapFile);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                    fos.close();
                    return new File(scaledBitmapFile);
                }
            }




        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;

    }
   /* static void overrideFonts(final Context context, final View v) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof TextView) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "nimbussans.otf"));
            }
        } catch (Exception e) {
        }
    }*/


    public static String readFileStringFromAsset(Context context, String filePath) {
        try {
            InputStream strm = context.getAssets().open(filePath);

            InputStream inputStream = strm;
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader
                    (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }
            return textBuilder.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }

    }


}
