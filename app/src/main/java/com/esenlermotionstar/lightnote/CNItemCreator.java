package com.esenlermotionstar.lightnote;

import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hussainlobo on 16.05.2018.
 */

public class CNItemCreator {

    public static final String xmlRootTag = "lightnote",
            xmlMainCategoryTitle = "maincategory",
            categoryTag = "category", noteTag ="note"
            ,titleAttr = "title";

    public static CN_Category GenerateRoot(Document doc) {


        Element xmlRoot, xmlMainCategory = null;
        if (doc == null)
        {

        }
        if (doc.getElementsByTagName(xmlRootTag).getLength() == 0)
        {
            xmlRoot = doc.createElement(xmlRootTag);
            doc.appendChild(xmlRoot);

        }
        else
        {
            xmlRoot = (Element) doc.getElementsByTagName(xmlRootTag).item(0);

            NodeList cocuklar = xmlRoot.getChildNodes();
            for (int i = 0; i < cocuklar.getLength(); i++)
            {
                Element cocuk = (Element)(cocuklar.item(i));
                if (cocuk.getTagName().equals(categoryTag)  && cocuk.getAttribute(titleAttr).equals(xmlMainCategoryTitle))
                {
                    xmlMainCategory = cocuk;
                    break;
                }
            }
        }

        if (xmlMainCategory == null)
        {
            xmlMainCategory = doc.createElement(categoryTag);
            xmlMainCategory.setAttribute(titleAttr,xmlMainCategoryTitle);
            xmlRoot.appendChild(xmlMainCategory);
        }
        return CN_Category.createCategoryFromElement(doc,xmlMainCategory,null);
    }

}

