package com.esenlermotionstar.lightnote;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by hussainlobo on 16.05.2018.
 */

public class CN_Note extends CN_Item {
    public static final String xmlRootTag = "lightnote",
            xmlMainCategoryTitle = "maincategory",
            categoryTag = "category", noteTag = "note", titleAttr = "title", refPathAttr = "refpath";

    public CN_Note() {
        super();
    }

    String referanceFolder;

    void setReferanceFolder(String newPath) {
        referanceFolder = newPath.replace("workingfolder", CN_Item.workingFolder);
        File fil = new File(referanceFolder);
        if (!fil.exists()) fil.mkdirs();
    }

    public String getReferanceFolder() {
        return referanceFolder;
    }

    public static CN_Note createNoteFromElement(Document doc, Element el, CN_Category mainCategory) {
        CN_Note note = new CN_Note();

        note.SetAll(el, mainCategory, false);

        String ElementrefPathStr = "workingfolder/";
        if (el.hasAttribute(refPathAttr)) {
            ElementrefPathStr = (el.getAttribute(refPathAttr));
        } else {
            ElementrefPathStr += note.title.replaceAll("[^a-zA-Z0-9.-]", "_") + "_ref";
            el.setAttribute(refPathAttr, ElementrefPathStr);
        }
        note.setReferanceFolder(ElementrefPathStr);

        //File fi =


        //SADECE ALT ITEMLERİ YÜKLEMEK İÇİN

        return note;
    }

    @Override
    public void RemoveInstant() {
        super.RemoveInstant();
        if (!StringUtils.isEmpty(referanceFolder) && referanceFolder.startsWith(LNtNoteEditor.workingFolder))
            ;
        {
            if (foldersThatNeedToBeRemoved == null)
                foldersThatNeedToBeRemoved = new ArrayList<>();

            String refFolderwithSlash = referanceFolder + "/";
            foldersThatNeedToBeRemoved.add(refFolderwithSlash);
        }
    }

    @Override
    public void CancelInstantRemove() {
        super.CancelInstantRemove();
        String refFolderwithSlash = referanceFolder + "/";
        if (foldersThatNeedToBeRemoved != null && foldersThatNeedToBeRemoved.contains(refFolderwithSlash))
            foldersThatNeedToBeRemoved.remove(refFolderwithSlash);
    }

    public static List<String> foldersThatNeedToBeRemoved;

    public static void removeFoldersNeedToBeRemovedBeforeClose() {
        if (foldersThatNeedToBeRemoved != null)
        {
            for (int i = 0; i < foldersThatNeedToBeRemoved.size(); i++) {
                String refFolderwithSlash = foldersThatNeedToBeRemoved.get(i);
                File dir = (new File(refFolderwithSlash));
                CleanFolder(dir);
                dir.delete();

            }
            foldersThatNeedToBeRemoved.clear();
        }

    }

    static CN_Note createNewNote(Document doc, String title_, @NonNull CN_Category mainCategory, String refPathStr) {
        String title = title_;
        Element el = doc.createElement(noteTag);
        el.setAttribute(titleAttr, title);

        String refPathStrx = refPathStr.replace(CN_Item.workingFolder, "workingfolder");
        el.setAttribute(refPathAttr, refPathStrx);

        CN_Note ct = new CN_Note();
        ct.setReferanceFolder(refPathStr);

        ct.SetAll(el, mainCategory, true);


        return ct;
    }


    public void RemoveWorkFolder() {
        try {
            String refFolderwithSlash = referanceFolder + "/";
            File dir = (new File(refFolderwithSlash));
            CleanFolder(dir);
            dir.delete();
            if (foldersThatNeedToBeRemoved != null && foldersThatNeedToBeRemoved.contains(refFolderwithSlash))
                foldersThatNeedToBeRemoved.remove(refFolderwithSlash);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void CleanFolder(File folder) {
        for (File fl : folder.listFiles()) {
            if (fl.isDirectory()) CleanFolder(fl);
            fl.delete();
        }

    }
}


