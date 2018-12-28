package com.esenlermotionstar.lightnote;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by hussainlobo on 17.04.2018.
 */

public class XMLEditor {
    public static void main(String[] args) {


        /*String str = "%s rtrtr %d";
        str = String.format(str, "hüseyin can gündüz",170204051);
        System.out.println(str);
        str = str.replace("hüseyin can", "hussainlobo");
        System.out.println(str);*/
        /*hüseyin can gündüz rtrtr 170204051
        hussainlobo gündüz rtrtr 170204051*/
    }

    DocumentBuilderFactory factory;
    DocumentBuilder builder;
    Document mainDoc;

    String filePath;
    Boolean readed;
    File file;
    Context AppContext;



    public Document getDocument() {
        return mainDoc;
    }

    public XMLEditor(String flpath, @Nullable Context ctx) {
        filePath = flpath;
        file = new File(filePath);
        AppContext = ctx;

    }


    public String DocumentSource() {
        try {
            StringWriter wrt = new StringWriter();
            StreamResult res = new StreamResult(wrt);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource domSrc = new DOMSource(mainDoc);
            transformer.transform(domSrc, res);
            String output = wrt.getBuffer().toString().replaceAll("\n|\r", "");
            return output;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }

    }

    public void read() {
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            if (!file.exists()) {
                mainDoc = builder.newDocument();
                ShowMessage(AppContext.getString(R.string.created_new));
            } else {
                try
                {
                    mainDoc = builder.parse(file);
                }
                catch (Exception ex) {

                }

                if(mainDoc == null)
                {
                    file.delete();
                    mainDoc = builder.newDocument();
                    ShowMessage(AppContext.getString(R.string.created_new));
                }
                else
                {
                    mainDoc.normalize();
                    ShowMessage(AppContext.getString(R.string.readed_note_db));
                }


            }
            readed = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            ShowMessage(ex.getMessage());
        }
    }


    public void ShowMessage(String str) {
        if (AppContext != null) {
            Toast.makeText(AppContext, str, Toast.LENGTH_SHORT).show();
        }

        System.out.println(str);

    }


    public void save() {
        if (!readed) {
            ShowMessage("File hasn't readed (or created) yet");
        } else {
            try {
                //ProgressDialog di = ESMSUtils.showProgressDialog(AppContext,"Saving","Please wait while note database is being updated");
                //Akım Sonucu
                StreamResult res = new StreamResult(new PrintWriter(new FileOutputStream(file, false
                )));

                //Döküman Kaynak Metni
                DOMSource domSrc = new DOMSource(mainDoc);

                //Dönüştürücü
                Transformer transformer = TransformerFactory.newInstance().newTransformer();


                transformer.transform(domSrc, res);
                //di.dismiss();
            } catch (Exception ex) {
                ex.printStackTrace();
                ShowMessage(ex.getMessage());
            }
        }
    }

}
