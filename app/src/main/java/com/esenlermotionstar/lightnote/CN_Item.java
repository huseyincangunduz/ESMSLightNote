package com.esenlermotionstar.lightnote;

import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hussainlobo on 16.05.2018.
 */

public class CN_Item {
    public String title;
    Element XMLElement;

    final String FLAT_INDEX_ATTRIBUTE = "flatindex";
    @Nullable
    public CN_Category ParentCategory;

    public static String workingFolder;

    void setParentCategory(CN_Category category) {
        if (ParentCategory != null && ParentCategory.SubItems.indexOf(this) > -1) {
            ParentCategory.SubItems.remove(this);
        }

        ParentCategory = category;
        if (category != null && category.SubItems.indexOf(this) == -1) {
            category.SubItems.add(this);
        }
    }

    public void setXMLElement(Element XMLElement) {

        this.XMLElement = XMLElement;
        if (XMLElement != null) this.title = XMLElement.getAttribute("title");
    }

    public void SetAll(Element el, @Nullable CN_Category mainCategory, boolean createNew) {

        if (mainCategory != null) {
            this.setParentCategory(mainCategory);
        }
        this.setXMLElement(el);
        if (createNew && ParentCategory != null && ParentCategory.XMLElement != null) {
            ParentCategory.XMLElement.appendChild(XMLElement);
        }
    }

    public void setFlatIndex(int newIndex)
    {
        if (XMLElement != null)
        {
            /*if (!XMLElement.hasAttribute(FLAT_INDEX_ATTRIBUTE))
            {
                Attribute flatIndexAttr = new A
            }*/
            XMLElement.setAttribute(FLAT_INDEX_ATTRIBUTE,String.valueOf(newIndex));
        }
    }
    public int getFlatIndex()
    {


        String flatIndexAttrStr = XMLElement.getAttribute(FLAT_INDEX_ATTRIBUTE);

        if (this.XMLElement != null && flatIndexAttrStr != null && flatIndexAttrStr != "")
        {
            try
            {
                return Integer.valueOf(flatIndexAttrStr);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return -1;
            }
        }
        else
            return -1;
    }
    Element instantElement, instantElementParent;
    public void RemoveInstant()
    {
        if (XMLElement != null) {
            instantElementParent = (Element)XMLElement.getParentNode();
            XMLElement.getParentNode().removeChild(XMLElement);
        }


    }




    public void CancelInstantRemove()
    {
        if (instantElementParent != null && XMLElement != null)
            instantElementParent.appendChild(XMLElement);

    }
    public void RemoveItself() {
        if (ParentCategory != null && ParentCategory.SubItems.indexOf(this) > -1) {
            ParentCategory.SubItems.remove(this);
        }

        if (XMLElement != null && XMLElement.getParentNode() != null) {
            XMLElement.getParentNode().removeChild(XMLElement);
            setXMLElement(null);
        }

    }




}
