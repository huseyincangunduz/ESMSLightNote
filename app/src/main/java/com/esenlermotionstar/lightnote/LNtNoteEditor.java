package com.esenlermotionstar.lightnote;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;


import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/**
 * Created by hussainlobo on 31.05.2018.
 */

public class LNtNoteEditor {
    static Context ApplicationContext;
    static String noteFileStr;
    static File noteFile;
    static String workingFolder;
    static boolean loaded = false;
    static XMLEditor editor;
    static CN_Category currentCategory;

    public static CN_Category rootCategory;


    public static void Load(Context context) {

        if (loaded == false) {
            ApplicationContext = context;
            workingFolder = context.getApplicationInfo().dataDir + "/notes";
            CN_Item.workingFolder = workingFolder;
            File workFolderFil = new File(workingFolder);
            if (!workFolderFil.exists()) workFolderFil.mkdirs();

            noteFileStr = workingFolder + "/LNtNotes.xml";

            CN_Item.workingFolder = workingFolder;

            noteFile = new File(noteFileStr);

            //noteSubPath = noteFileStr + "/LNtNotes_sub/";
            editor = new XMLEditor(noteFileStr, context);

            editor.read();

            // rootCategory = CN_Category.CreateCategory();

            rootCategory = CNItemCreator.GenerateRoot(
                    editor.getDocument()
            );
            currentCategory = rootCategory;
            loaded = true;
        }
    }


    public abstract static class NoteEditorClosedEvent {
        abstract void OnClosed();
    }

    public static void Close() {

        ProgressDialog di = ProgressDialog.show(ApplicationContext, "", ApplicationContext.getResources().getString(R.string.saving_note_db_message));

        editor.save();
        CN_Note.removeFoldersNeedToBeRemovedBeforeClose();
        loaded = false;
        Toast.makeText(ApplicationContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
        di.dismiss();

    }

    public static void showErrorDialog(int MessageStringID, int TitleStringID, DialogInterface.OnClickListener clickedEvent) {
        (new AlertDialog.Builder(ApplicationContext)).setTitle(TitleStringID)
                .setMessage(MessageStringID)
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).setPositiveButton(R.string.try_again, clickedEvent).show();
    }

    public static void showErrorDialogForAddNote(int MessageStringID) {
        showErrorDialog(MessageStringID, R.string.enter_different_note_name, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AddNewNote();
            }
        });
    }

    public static void showErrorDialogForAddCategory(int MessageStringID) {
        showErrorDialog(MessageStringID, R.string.enter_different_cat_name, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AddNewCategory();
            }
        });
    }

    public static void AddNewCategory() {

        if (!loaded) {
            Toast.makeText(ApplicationContext, "Loaded true değil", Toast.LENGTH_SHORT).show();

        } else {


            AlertDialog.Builder builder = new AlertDialog.Builder(ApplicationContext);
            builder.setTitle(R.string.enter_new_category);

            // Set up the input
            final EditText input = new EditText(ApplicationContext);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        String entered = input.getText().toString();

                        XPathFactory xpfact = XPathFactory.newInstance();
                        XPath xp = xpfact.newXPath();
                        XPathExpression expr = xp.compile("//*[translate(@title, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')=\"" + entered.toLowerCase() + "\"]");

                        NodeList ndlist = (NodeList) (expr.evaluate(editor.getDocument(), XPathConstants.NODESET));

                        if (entered.equalsIgnoreCase(CNItemCreator.xmlMainCategoryTitle)) {
                            showErrorDialogForAddCategory(R.string.cant_be_used_named);
                            /*showErrorDialog(R.string.cant_be_used_named, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AddNewCategory();
                                }
                            });*/
                        } else if (ndlist.getLength() > 0) {
                            showErrorDialogForAddCategory(R.string.same_named_files_message_str);
                        } else
                            AddNewCategory(input.getText().toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showErrorDialogForAddCategory(R.string.error_occured);

                    }

                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }

    public static void AddNewCategory(String title) {
        CN_Category.createNewCategory(editor.getDocument(), title, currentCategory);        /*category.title =title;

        currentCategory.XMLElement.appendChild(el);*/
        //currentCategory.SubItems.add(category);
        if (editorEvents != null) editorEvents.added(currentCategory.SubItems.size() - 1);

        Toast.makeText(ApplicationContext, R.string.new_cat_added_str, Toast.LENGTH_SHORT).show();

    }

    public static CN_Category FindCategory(String title) {
        return rootCategory.getCategoryByTitle(title);
    }



    public static void AddNewNote() {
        if (!loaded) {
            Toast.makeText(ApplicationContext, "Loaded true değil", Toast.LENGTH_SHORT).show();

        } else {


            AlertDialog.Builder builder = new AlertDialog.Builder(ApplicationContext);
            builder.setTitle(R.string.enter_new_note);

            // Set up the input
            final EditText input = new EditText(ApplicationContext);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    /*AddNewNote(input.getText().toString());*/
                    try {
                        String entered = input.getText().toString(), titleDial = ApplicationContext.getString(R.string.enter_different_note_name);

                        XPathFactory xpfact = XPathFactory.newInstance();
                        XPath xp = xpfact.newXPath();
                        XPathExpression expr = xp.compile("//*[translate(@title, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')=\"" + entered.toLowerCase() + "\"]");

                        NodeList ndlist = (NodeList) (expr.evaluate(editor.getDocument(), XPathConstants.NODESET));
                        if (entered.equalsIgnoreCase(CNItemCreator.xmlMainCategoryTitle)) {
                            showErrorDialogForAddNote(R.string.cant_be_used_named);


                        } else if (ndlist.getLength() > 0) {
                            showErrorDialogForAddNote(R.string.same_named_files_message_str);

                        } else{
                            AddNewNote(input.getText().toString());
                        }

                    } catch (Exception ex) {
                        showErrorDialogForAddNote(R.string.error_occured);

                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }

    public static void AddNewNote(String title) {
        String ttx = title.toLowerCase().replaceAll("[^a-zA-Z0-9.-]", "_") + "_ref";
        CN_Note.createNewNote(editor.getDocument(), title, currentCategory, workingFolder + "/" + ttx);        /*category.title =title;

        currentCategory.XMLElement.appendChild(el);*/
        //currentCategory.SubItems.add(category);
        if (editorEvents != null) editorEvents.added(currentCategory.SubItems.size() - 1);
        Toast.makeText(ApplicationContext, R.string.newnoteadded_msg, Toast.LENGTH_SHORT).show();

    }

    public static void goUpFolder() {
        if (currentCategory != rootCategory) {
            currentCategory = currentCategory.ParentCategory;
        } else {

        }
    }

    public static void intoTheFolder(CN_Item itm) {
        if (itm instanceof CN_Category) {
            currentCategory = (CN_Category) itm;
        } else if (itm instanceof CN_Note) /* KOD YAZ */ ;

    }

    abstract static class NoteEditorEvents {
        abstract void added(int index);
    }

    static NoteEditorEvents editorEvents;
}
