package com.esenlermotionstar.lightnote;

import android.Manifest;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

import static com.esenlermotionstar.lightnote.ESMSUtils.CompareFileAsSame;

public class textEditorActivity extends AppCompatActivity {
    private static final int LOAD_IMAGE_FROM_GALLERY_REQUEST = 3;
    boolean started = false;
    private static final int IMAGE_SELECTION_ACTIVITYRESULT_REQUEST = 2;
    private TextView mTextMessage;
    WebView webBrowserx;
    File editingFile;
    File WorkFolder;
    File TemporaryFile;
    ProgressDialog LoadingDialog;
    Toolbar toolbar;
    String lastTookPicturefullFileName;
    String mImageFileLocation;
    final String thumbnailPath = "thumbnail";
    final int STORAGE_PERMISSION_FOR_ADD_IMAGE = 3;
    final int STORAGE_PERMISSION_FOR_BROWSE_GALLERY = 4;
    final int STORAGE_PERMISSION_FOR_CAPTURE_PICTURE = 5;
    final int TAKE_PHOTO_REQUEST = 6;

    public void fontsizebtnclicked(View view) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        //builderSingle.setIcon(R.drawable);
        builderSingle.setTitle(R.string.select_size_from_list);
        //builderSingle.setMessage("1 is too small, 7 is too big");
        final ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<Integer>(this, android.R.layout.select_dialog_item);
        arrayAdapter.add(1);
        arrayAdapter.add(2);
        arrayAdapter.add(3);
        arrayAdapter.add(4);
        arrayAdapter.add(5);
        arrayAdapter.add(6);
        arrayAdapter.add(7);
        builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selected = arrayAdapter.getItem(which);
                ExecCmd("fontSize", String.valueOf(selected));
            }
        });
        builderSingle.show();
    }

    public void fontColorBtnClicked(View view) {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle(getString(R.string.foreground_color))

                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                /*.setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {

                    }
                })*/
                .setPositiveButton(R.string.okay, new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        ExecCmd("foreColor", String.format("#%06X", (0xFFFFFF & selectedColor)));

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    public void backColorbtnClicked(View view) {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle(getString(R.string.text_background_color))

                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                /*.setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {

                    }
                })*/
                .setPositiveButton(R.string.okay, new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        ExecCmd("backColor", String.format("#%06X", (0xFFFFFF & selectedColor)));

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    public void subscrptBtnClicked(View view) {
        ExecCmd("subscript", "");
    }

    public void superScriptBtnClicked(View view) {
        ExecCmd("superscript", "");
    }

    public void strikethroughBtnClick(View view) {
        ExecCmd("strikethrough", "");
    }

    public void underlineBtnClick(View view) {
        ExecCmd("underline", "");
    }

    class LNtJsHelper {
        Context ApplicationContext;
        File WorkFolder;

        public LNtJsHelper(Context ctx_, File workFolder_) {
            WorkFolder = workFolder_;
            ApplicationContext = ctx_;
        }

        int indexOfArray(String[] strings, String srcing) {
            for (int i = 0; i < strings.length; i++) {
                if (strings[i].equals(srcing)) {
                    return i;
                }
            }
            return -1;
        }

        @JavascriptInterface
        public void RemoveFiles(String[] safedFiles) {
            File[] getFils = WorkFolder.listFiles();
            for (int i = 0; i < getFils.length; i++) {
                File fl = getFils[i];
                if (fl.exists() && indexOfArray(safedFiles, fl.getName()) == -1) {
                    fl.delete();
                }
            }
        }

        @JavascriptInterface
        public void openImageAtActivity(String startPath) {
            String absolutePath = WorkFolder.getAbsolutePath();
            String filePath = absolutePath + "/" + startPath;
            if ((new File(filePath)).exists()) {
                try {
                    Intent intent = new Intent(ApplicationContext, ImageViewerActivity.class);

                    Log.i(null, absolutePath);
                    intent.putExtra(ImageViewerActivity.IMAGE_PATH_EXTRA, filePath);
                    ApplicationContext.startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();

                }

            }

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LoadingDialog = ProgressDialog.show(textEditorActivity.this, getString(R.string.loading_note),
                getString(R.string.pls_wait_loading_note), true);
        LoadingDialog.show();
        super.onCreate(savedInstanceState);
        //this.setRequestedOrientation(this.getResources().getConfiguration().orientation);


        setContentView(R.layout.activity_text_editor);
        setSupportActionBar(((Toolbar) (findViewById(R.id.text_editor_toolbar))));
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        this.setTitle(getIntent().getStringExtra("title"));

        webBrowserx = (WebView) (findViewById(R.id.webBrowser));

        String folderPath = getIntent().getStringExtra("path");


        if (savedInstanceState != null) {
            Bundle browserStateBackUp = savedInstanceState.getBundle(BROWSER_STATE_BUNDLE_KEY);
            if (browserStateBackUp != null) {
                webBrowserx.restoreState(browserStateBackUp);
                ContinueFromState(folderPath);
            } else {
                PrepareFile(folderPath);
            }
            LoadingDialog.dismiss();

        } else
            PrepareFile(folderPath);

        LNtJsHelper lightNoteHelper = new LNtJsHelper(this, WorkFolder);
        webBrowserx.addJavascriptInterface(lightNoteHelper, "lightNoteHelper");

        /*if (savedInstanceState == null)

        else
            ContinueFromBundle(savedInstanceState);*/

        mTextMessage = (TextView) findViewById(R.id.message);
        //BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        started = true;


        new MaterialShowcaseView.Builder(this).withRectangleShape()
                .setTarget(findViewById(R.id.texteditbtns_hzscrl))

                .setDismissText(getString(R.string.got_it))
                .setContentText(getString(R.string.horizontal_scroll_help_text))
                .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse("swipe_bottom_toolbar_showcase") // provide a unique ID used to ensure it is only shown once
                .show();


    }


    public void italicBtnClick(View view) {
        ExecCmd("italic", "");
    }

    public void centerAligment(View view) {
        ExecCmd("justifyCenter", "");
    }

    public void rightAlignment(View view) {
        ExecCmd("justifyRight", "");
    }

    public void leftAlignment(View view) {
        ExecCmd("justifyLeft", "");
    }

    public void orderedList(View view) {
        ExecCmd("insertOrderedList", "");
    }

    public void unorderedList(View view) {
        ExecCmd("insertUnorderedList", "");
    }

    public void addImage(View view) {
        ShowAddImageMenu(view);
    }


    String data = null;
    ProgressDialog progress;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_texteditor, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_redo) {
            ExecCmd("redo", "");
            return true;
        } else if (item.getItemId() == R.id.action_undo) {
            ExecCmd("undo", "");
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            SaveNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ContinueFromState(String folderPath) {
        editingFile = new File(folderPath + "/note.html");
        TemporaryFile = new File(folderPath + "/note_temp.html");
        WorkFolder = new File(folderPath);
        PrepareBrowser();
    }

    private void PrepareFile(String folderPath) {


        try {
            editingFile = new File(folderPath + "/note.html");
            TemporaryFile = new File(folderPath + "/note_temp.html");
            WorkFolder = new File(folderPath);


            if (!editingFile.exists())
                ESMSUtils.copyInputStreamUsingStream(getAssets().open("originnote.html"),
                        editingFile);

            if (TemporaryFile.exists()) {
                //FileReader read = new FileReader(TemporaryFile);


                webBrowserx.loadUrl(TemporaryFile.toURI().toString());
                setTitle(getTitle() + "*");
                getSupportActionBar().setSubtitle(R.string.didnt_apply_changes);
                Toast tstMsg = Toast.makeText(this, R.string.didnt_apply_changes, Toast.LENGTH_LONG);

                tstMsg.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                tstMsg.show();
            } else {
                //FileReader read = new FileReader(editingFile);


                webBrowserx.loadUrl(editingFile.toURI().toString());
            }


        } catch (Exception ex) {

        }
        PrepareBrowser();

    }

    private void PrepareBrowser() {
        webBrowserx.getSettings().setDomStorageEnabled(true);
        webBrowserx.getSettings().setJavaScriptEnabled(true);
        final String readedHelperScript = ESMSUtils.readFileStringFromAsset(getApplicationContext(), "helperscriptloader.js");

        webBrowserx.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                LoadingDialog.dismiss();
                webBrowserx.evaluateJavascript(
                        "document.getElementById(\"editarea\").contentEditable = true;\n" +
                                "document.getElementById(\"editarea\").focus();\n" +
                                readedHelperScript
                        , new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {

                            }
                        });
            }
        });
        webBrowserx.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("MyApplication", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);
            }
        });
    }


    void ExecCmd(String name, String args) {
        String script = "document.execCommand('" + name + "',false,'" + args + "');";
        webBrowserx.evaluateJavascript(
                script
                , new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        //Toast.makeText(textEditorActivity.this, s, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    boolean exitRequest() {
        webBrowserx.loadUrl("about:blank");
        started = false;
        super.onBackPressed();

        if (TemporaryFile.exists()) {
            boolean bl = TemporaryFile.delete();
            if (TemporaryFile.exists()) {

                try {
                    TemporaryFile.getCanonicalFile().delete();
                } catch (Exception e) {

                }

                if (TemporaryFile.exists()) {
                    getApplicationContext().deleteFile(TemporaryFile.getName());
                }
            }
            Log.d(null, "exitRequest() called" + String.valueOf(bl));
        }

        return true;
    }

    private void SaveTemp() {
        if (started)
            webBrowserx.evaluateJavascript(
                    "function f() {\n" +
                            "return document.getElementsByTagName('html')[0].innerHTML;}\n" +
                            "f();",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {

                            String srcx = StringEscapeUtils.unescapeJava(s);
                            srcx = srcx.substring(1, srcx.length() - 1);
                            Source = "<html>" + srcx + "</html>";
                            //(new AlertDialog.Builder(getApplicationContext())).setMessage(s).show();
                            try {
                                FileWriter wrt = new FileWriter(TemporaryFile, false);
                                wrt.write(Source);
                                wrt.close();

                            } catch (Exception ex) {
                                Toast.makeText(textEditorActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


    }

    private void SaveNote() {

        webBrowserx.evaluateJavascript(
                "function f() " +
                        "{" +
                        "removeUnnecessaryImageFiles();\n" +
                        "return document.getElementsByTagName('html')[0].innerHTML;" +
                        "}" +

                        "f();",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {

                        String srcx = StringEscapeUtils.unescapeJava(s);
                        srcx = srcx.substring(1, srcx.length() - 1);
                        Source = "<html>" + srcx + "</html>";
                        //(new AlertDialog.Builder(getApplicationContext())).setMessage(s).show();
                        try {
                            FileWriter wrt = new FileWriter(editingFile, false);
                            wrt.write(Source);
                            wrt.close();
                            Toast.makeText(textEditorActivity.this, R.string.changes_saved, Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            Toast.makeText(textEditorActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }

    private void SaveNoteAndExit() {

        webBrowserx.evaluateJavascript(
                "function f() " +
                        "{" +
                        "removeAllScripts();\n" +
                        "removeUnnecessaryImageFiles();\n" +
                        "document.getElementById(\"editarea\").contentEditable = false;\n" +
                        "return document.getElementsByTagName('html')[0].innerHTML;" +
                        "}" +

                        "f();",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {

                        String srcx = StringEscapeUtils.unescapeJava(s);
                        srcx = srcx.substring(1, srcx.length() - 1);
                        Source = "<html>" + srcx + "</html>";
                        //(new AlertDialog.Builder(getApplicationContext())).setMessage(s).show();
                        try {
                            FileWriter wrt = new FileWriter(editingFile, false);
                            wrt.write(Source);
                            wrt.close();

                        } catch (Exception ex) {
                            Toast.makeText(textEditorActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        exitRequest();
                    }
                });


    }

    @Override
    public void onBackPressed() {


        AlertDialog.Builder bd = new AlertDialog.Builder(this);
        bd.setTitle(R.string.save_note_question_title).setMessage(R.string.do_u_save_note)
                .setPositiveButton(R.string.yep, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SaveNoteAndExit();

                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        exitRequest();
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //exitRequest = false;
                    }
                }).show();

        //Toast.makeText(this, "yarrağıma çıkarsın", Toast.LENGTH_SHORT).show();
        //if(exitRequest) super.onBackPressed();
        //super.onBackPressed();
    }

    String Source;


    public void boldBtnClick(View view) {
        ExecCmd("bold", "");
    }

    final String BROWSER_STATE_BUNDLE_KEY = "browser_state";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle BrowserState = new Bundle();
        webBrowserx.saveState(BrowserState);
        outState.putBundle(BROWSER_STATE_BUNDLE_KEY, BrowserState);

    }


    boolean controlStoragePerms() {
        return (
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        );
    }

    protected void OpenImageFromFile() {
        if (Build.VERSION.SDK_INT >= 23 && controlStoragePerms()) {

            showPermissionMessage(STORAGE_PERMISSION_FOR_ADD_IMAGE);
        } else {
            ShowImageFileSelector();
        }
    }


    private void BrowsePictureAtGalleryRequest() {
        if (Build.VERSION.SDK_INT >= 23 && controlStoragePerms()) {

            showPermissionMessage(STORAGE_PERMISSION_FOR_BROWSE_GALLERY);
        } else {
            BrowsePictureAtGallery();
        }
    }

    private void BrowsePictureAtGallery() {
        try {
            Intent i = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (i.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(i, LOAD_IMAGE_FROM_GALLERY_REQUEST);
            } else {
                (new AlertDialog.Builder(this)).setTitle(R.string.error_occured)
                        .setMessage(R.string.not_found_application);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            (new AlertDialog.Builder(this)).setTitle(R.string.error_occured)
                    .setMessage(R.string.error_occured);
        }


    }

    protected void TakePhotoAddRequest() {
        if (Build.VERSION.SDK_INT >= 23 && controlStoragePerms()) {

            showPermissionMessage(STORAGE_PERMISSION_FOR_CAPTURE_PICTURE);
        } else {
            TakePhotoAdd();
        }
    }

    void showPermissionMessage(int permissionRequestInt) {
        final int permissionRequestIntFinal = permissionRequestInt;

        (new AlertDialog.Builder(this)).setMessage(R.string.perm_request_for_image_select)
                .setPositiveButton(R.string.continue_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendStoragePermissionRequest(permissionRequestIntFinal);
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).show();
    }

    private void sendStoragePermissionRequest(int permissionRequestIntFinal) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, permissionRequestIntFinal);
    }

    private void ShowImageFileSelector() {

        Intent fileExploreIntent = new Intent(
                FileBrowserActivity.INTENT_ACTION_SELECT_FILE,
                null,
                this,
                FileBrowserActivity.class
        );

        fileExploreIntent.putExtra(FileBrowserActivity.startDirectoryParameter, "/storage/emulated/0");//Here you can add optional start directory parameter, and file browser will start from that directory.
        startActivityForResult(
                fileExploreIntent,
                IMAGE_SELECTION_ACTIVITYRESULT_REQUEST
        );
    }


    @Override
    protected void onPause() {
        SaveTemp();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_SELECTION_ACTIVITYRESULT_REQUEST && resultCode == RESULT_OK) {
            String out = data.getStringExtra(FileBrowserActivity.returnFileParameter);
            //String s = ESMSUtils.getUriRealPath(this,);


            if (out.endsWith(".png") || out.endsWith(".jpg") ||
                    out.endsWith(".jpeg") || out.endsWith(".gif")
            ) {
                BitmapFactory.Options ops = new BitmapFactory.Options();
                ops.inJustDecodeBounds = true;
                String saveLocal = SaveFromFileLOCALURI(out);

                if (saveLocal != null) {
                    AddImageCommandWithThumbnail(WorkFolder.getAbsolutePath(), saveLocal);
                /*String scriptx = "<img src=\"" + saveLocal.substring(1) + "\"/>";
                ExecCmd("insertHTML", scriptx);*/
                } else
                    Log.i(null, "onActivityResult: savelocal null değerdir.");
            } else {
                (new AlertDialog.Builder(this)).setTitle(R.string.isnt_invalid_image)
                        .setMessage(R.string.fileselect_invalid_extns).
                        setNeutralButton(R.string.okay, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                ShowImageFileSelector();

                            }
                        })
                        .show();
            }

        } else if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK) {
            // String mImageFileLocationWithSlash = mImageFileLocation;
            String importedFileNameSlash = SaveFromFileLOCALURI(mImageFileLocation);
            AddImageCommandWithThumbnail(WorkFolder.getAbsolutePath(), importedFileNameSlash);

        } else if (requestCode == LOAD_IMAGE_FROM_GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            //Toast.makeText(this, picturePath, Toast.LENGTH_SHORT).show();
            cursor.close();
            String saveLocal = SaveFromFileLOCALURI(picturePath);
            if (saveLocal != null) {
                AddImageCommandWithThumbnail(WorkFolder.getAbsolutePath(), saveLocal);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_FOR_ADD_IMAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.successful, Toast.LENGTH_SHORT).show();
                ShowImageFileSelector();
            } else {
                Toast.makeText(this, R.string.permission_failed, Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == STORAGE_PERMISSION_FOR_BROWSE_GALLERY) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.successful, Toast.LENGTH_SHORT).show();
                BrowsePictureAtGallery();
            } else {
                Toast.makeText(this, R.string.permission_failed, Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == STORAGE_PERMISSION_FOR_CAPTURE_PICTURE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.successful, Toast.LENGTH_SHORT).show();
                TakePhotoAdd();
            } else {
                Toast.makeText(this, R.string.permission_failed, Toast.LENGTH_SHORT).show();
            }
        }

    }

    String downloadFile(URI url) {
        try {
            String sourceFilename = url.getPath();
            String nm = sourceFilename.substring(sourceFilename.lastIndexOf(File.separatorChar));

            String destinationFilename = WorkFolder.getPath() + nm;

            FileDownloader.downloadUsingStream(url.toString(), destinationFilename);
            return nm;
        } catch (Exception ex) {
            return null;
        }

    }


    String SaveFromFileLOCALURI(String sourceuri) {


        String sourceFilename = sourceuri;
        String nm = sourceFilename.substring(sourceFilename.lastIndexOf(File.separatorChar));

        String destinationFilename = WorkFolder.getPath() + nm;
        File destinationFile = new File(destinationFilename), sourceFile = new File(sourceFilename);

        if (destinationFile.exists()) {
            if (CompareFileAsSame(sourceFile, destinationFile)) {
                Log.i(null, "SaveFromFileLOCALURI: Exist same file. not will be copied");
                return nm;
            } else {
                String nameStart = nm.substring(1, nm.lastIndexOf("."));
                String extension = nm.substring(nm.lastIndexOf("."));
                int sayi = 1;
                String newFileName = nameStart + " (" + String.valueOf(sayi++) + ")" + extension;
                while ((new File(newFileName)).exists()) {
                    newFileName = nameStart + " (" + String.valueOf(sayi++) + ")" + extension;
                }
                nm = "/" + newFileName;
                destinationFilename = WorkFolder.getPath() + nm;
            }
        }

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            if (sourceFile.getAbsolutePath().endsWith(".jpg")) {
                ProgressDialog di = new ProgressDialog(this);
                di.setTitle(R.string.optimizing);
                di.show();
                try {
                    Bitmap bmp = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
                    FileOutputStream strm = new FileOutputStream(destinationFile);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 75, strm);
                    strm.close();
                } catch (Exception ex) {
                    di.dismiss();
                    (new AlertDialog.Builder(this)).setTitle("Maalesef optimizasyon sırasında hata oluştu")
                            .setMessage(ex.getMessage()).show();

                    ex.printStackTrace();
                    FileUtils.copyFile(sourceFile, new File(destinationFilename));
                }
                di.dismiss();
            } else
                FileUtils.copyFile(sourceFile, new File(destinationFilename));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            /*try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }


        return nm;
    }


    URI urix;
    String outFilePath;
    Boolean working = false;

    private void AddImageByURIWithThread(String entered) {
        ProgressDialog dialog = ProgressDialog.show(textEditorActivity.this, getString(R.string.adding_img),
                getString(R.string.plswait_wh_adding_image), true);
        dialog.show();
        final String girilen = entered;
        Thread downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //urix = URI.create(girilen);
                    //outFilePath = downloadFile(urix);
                    outFilePath = FileDownloader.downloadAsImage(girilen, WorkFolder.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        downloadThread.start();
        try {
            downloadThread.join();
                    /*File fil = new File(outFilePath);
                    String sx = fil.toURI().toString();*/

            if (outFilePath != null)
                AddImageCommandWidthSensed(outFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.dismiss();
    }

    private void AddImageCommandWithThumbnail(String WorkFolder, String safeFileNameWithSlash) {
        try {
            //AddImageCommandWidthSensed(WorkFolder, safeFileNameWithSlash);
            Point size = new Point(0, 0);
            final float scale = getResources().getDisplayMetrics().density;
            getWindowManager().getDefaultDisplay().getSize(size);

            Point scaledSize = new Point((int) (size.x / scale), (int) (size.y / scale));
            File thumbnailFile = ESMSUtils.createThumbnailImage(new File(WorkFolder + safeFileNameWithSlash),
                    new File(WorkFolder + "/" + thumbnailPath),
                    scaledSize);


            if (thumbnailFile != null) {

                String scriptx = "<img src=\"" + thumbnailPath + safeFileNameWithSlash + "\" " +
                        "full_src = \"" + safeFileNameWithSlash.substring(1) +
                        "\" " +
                        "style = \"margin-right: 20px;  margin-bottom:20px;\"/><br/>&nbsp;";
                ExecCmd("insertHTML", scriptx);
            } else {
                AddImageCommandWidthSensed(WorkFolder, safeFileNameWithSlash);
               /* String scriptx = "<img src=\"" + safeFileNameWithSlash.substring(1) + "\" " +

                        "style = \"margin-right: 20px; width: 75%; margin-bottom:20px;\"/><br/>&nbsp;";
                ExecCmd("insertHTML", scriptx);*/
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void AddImageCommandWidthSensed(String FullPath) {
        htmlSizeGetter sizeGetter = new textEditorActivity.htmlSizeGetter();
        File imageFile = new File(FullPath);
        sizeGetter.setHeightWidth(imageFile);

        Point size = new Point(0, 0);
        final float scale = getResources().getDisplayMetrics().density;
        getWindowManager().getDefaultDisplay().getSize(size);
        String scriptx;
        String safeFile = FullPath.substring(FullPath.lastIndexOf("/") + 1);
        if (sizeGetter.Width > size.x / scale || sizeGetter.Width > size.y / scale) {
            scriptx = "<img src=\"" + safeFile + "\" " +
                    "style = \"width: 85%;margin-right: 16px;  margin-bottom:16px;\"/><br/>&nbsp;";
        } else {
            scriptx = "<img src=\"" + safeFile + "\" " +
                    "style = \"margin-right: 20px;  margin-bottom:20px;\"/><br/>&nbsp;";
        }
        /*String scriptx = "<img src=\"" + safeFileNameWithSlash.substring(1) + "\" " +
                "style = \"margin-right: 20px;  margin-bottom:20px;\"/>&nbsp;";*/
        ExecCmd("insertHTML", scriptx);
    }

    private void AddImageCommandWidthSensed(String WorkFolder, String safeFileNameWithSlash) {
        htmlSizeGetter sizeGetter = new textEditorActivity.htmlSizeGetter();
        File imageFile = new File(WorkFolder + safeFileNameWithSlash);
        sizeGetter.setHeightWidth(imageFile);

        Point size = new Point(0, 0);
        final float scale = getResources().getDisplayMetrics().density;
        getWindowManager().getDefaultDisplay().getSize(size);
        String scriptx;
        if (sizeGetter.Width > size.x / scale || sizeGetter.Width > size.y / scale) {
            scriptx = "<img src=\"" + safeFileNameWithSlash.substring(1) + "\" " +
                    "style = \"width: 85%;margin-right: 16px;  margin-bottom:16px;\"/><br/>&nbsp;";
        } else {
            scriptx = "<img src=\"" + safeFileNameWithSlash.substring(1) + "\" " +
                    "style = \"margin-right: 20px;  margin-bottom:20px;\"/><br/>&nbsp;";
        }
        /*String scriptx = "<img src=\"" + safeFileNameWithSlash.substring(1) + "\" " +
                "style = \"margin-right: 20px;  margin-bottom:20px;\"/>&nbsp;";*/
        ExecCmd("insertHTML", scriptx);
    }

    void insertImageFromURL() {
        final Thread currentThread = Thread.currentThread();

        ESMSUtils.inputBox(this, getString(R.string.enter_url), new ESMSUtils.onClickedAtInputBox() {
            @Override
            public void OnClicked(String entered) {

                AddImageByURIWithThread(entered);

            }
        });
    }

    class htmlSizeGetter {
        public int Height, Width;

        void setHeightWidth(File fil) {
            BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(fil.getAbsolutePath(), ops);
            Height = ops.outHeight;
            Width = ops.outWidth;
        }
    }

    void ShowAddImageMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_add_image, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_add_from_file) {
                    OpenImageFromFile();
                    //loadItem(LNtNoteEditor.rootCategory);
                } else if (item.getItemId() == R.id.menu_image_url) {


                    insertImageFromURL();
                } else if (item.getItemId() == R.id.menu_add_take_photo) {
                    TakePhotoAddRequest();
                } else if (item.getItemId() == R.id.menu_select_from_gallery) {
                    BrowsePictureAtGalleryRequest();
                }
                return true;
            }
        });
        popup.show();
    }


    File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);
        mImageFileLocation = image.getAbsolutePath();

        return image;

    }

    private void TakePhotoAdd() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        /*String new_name = ESMSUtils.getNowAsString() + ".jpg";
        String filNameWithSlash = "/" + new_name;*/
            //lastTookPicturefullFileName = "Android/data/com.example.package.name/files/Pictures/amk.jpg";

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File FullFile = createImageFile();
                String authority = "com.esenlermotionstar.lightnote.fileprovider";
                Uri uriForPhoto;
                if (Build.VERSION.SDK_INT >= 23) {
                    uriForPhoto = FileProvider.getUriForFile(this, authority, FullFile);
                } else {
                    uriForPhoto = Uri.fromFile(FullFile);
                }

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriForPhoto);

                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST);
            } else {
                (new AlertDialog.Builder(this)).setTitle(R.string.error_occured)
                        .setMessage(R.string.not_found_application);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            (new AlertDialog.Builder(this)).setTitle(R.string.error_occured).setMessage(ex.getLocalizedMessage())
                    .setNeutralButton("Tamam", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
        }

    }
}

