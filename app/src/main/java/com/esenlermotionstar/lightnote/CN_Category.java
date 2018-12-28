package com.esenlermotionstar.lightnote;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompatSideChannelService;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by hussainlobo on 16.05.2018.
 */

public class CN_Category extends CN_Item {
    public CN_Category() {
        super();
        SubItems = new ArrayList<>();
    }


    public ArrayList<CN_Item> SubItems;


    public static final String xmlRootTag = "lightnote",
            xmlMainCategoryTitle = "maincategory",
            categoryTag = "category", noteTag = "note", titleAttr = "title";


    static CN_Category createCategoryFromElement(Document doc, Element el, CN_Category mainCategory) {
        CN_Category ct = new CN_Category();

        ct.SetAll(el, mainCategory, false);

        //SADECE ALT ITEMLERİ YÜKLEMEK İÇİN
        NodeList XmlSubitems = el.getChildNodes();

        Map<CN_Item, Integer> SpecialIndexes = new HashMap<>();

        for (int i = 0; i < XmlSubitems.getLength(); i++) {
            Element elx = (Element) XmlSubitems.item(i);
            if (elx.getTagName().equals(noteTag)) {
                CN_Note note = CN_Note.createNoteFromElement(doc, elx, ct);
                if (note.getFlatIndex() != -1) {
                    SpecialIndexes.put(note, note.getFlatIndex());
                }
            } else if (elx.getTagName().equals(categoryTag)) {
                CN_Category category = createCategoryFromElement(doc, elx, ct);
                if (category.getFlatIndex() != -1) {
                    SpecialIndexes.put(category, category.getFlatIndex());
                }
            }

        }

        Set<CN_Item> keys = SpecialIndexes.keySet();
        for (CN_Item itm : keys) {

            int newIndex = SpecialIndexes.get(itm);
            if (newIndex < ct.SubItems.size()) {

                ct.SubItems.remove(itm);
                ct.SubItems.add(newIndex, itm);
            }
            else
            {
                ct.setFlatIndex(-1);
            }

        }


        return ct;
    }

    static CN_Category createNewCategory(Document doc, String title, @NonNull CN_Category mainCategory) {
        Element el = doc.createElement(categoryTag);
        el.setAttribute(titleAttr, title);

        CN_Category ct = new CN_Category();
        ct.SetAll(el, mainCategory, true);


        return ct;
    }

    public CN_Category getCategoryByTitle(String title) {
        int maxSubItems = 0;
        CN_Category found = null;
        if (this.SubItems != null)
            maxSubItems = this.SubItems.size();

        if (this.title.equals(title)) {
            return this;
        } else if (maxSubItems > 0) {

            for (int i = 0; i < maxSubItems; i++) {
                CN_Item subitem = this.SubItems.get(i);
                if (subitem instanceof CN_Category) {
                    found = ((CN_Category) subitem).getCategoryByTitle(title);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }

        return null;
    }

    public void removeSubItems()
    {
        CN_Item[] SubItemsArray = this.SubItems.toArray(new CN_Item[SubItems.size()]);
        for (CN_Item itm : SubItemsArray)
        {
            itm.RemoveItself();
            if (itm instanceof  CN_Note)
                ((CN_Note)itm).RemoveWorkFolder();
        }
    }

    @Override
    public void RemoveItself() {
        super.RemoveItself();
        removeSubItems();
    }


}
