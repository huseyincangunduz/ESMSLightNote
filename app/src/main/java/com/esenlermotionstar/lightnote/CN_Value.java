package com.esenlermotionstar.lightnote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by hussainlobo on 16.05.2018.
 */


public class CN_Value {
    public enum ValueType{

        Image,Sound_MP3, Sound_WAV
    }


    public static void main(String[] args) {

        try
        {
            String sourcePath = "D:\\test_java\\source.jpg";
            String targetPath = "D:\\test_java\\target.jpg";
            File src = new File(sourcePath);
            File target = new File(targetPath);
            FileInputStream srcOku = new FileInputStream(src);
            int bytesLenght = (int)src.length();
            byte[] bytes = new byte[bytesLenght];
            srcOku.read(bytes,0,bytesLenght);
            srcOku.close();

            FileOutputStream out = new FileOutputStream(target);
            out.write(bytes,0,bytesLenght);
            out.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }


    }


    byte[] bytes;
    String link;
    ValueType type = ValueType.Image;
    CN_Value readValFromFile(String path)
    {
        try
        {
            CN_Value val = new CN_Value();
            File file  = new File(path);
            FileInputStream giris = new FileInputStream(file);

            int length = (int)file.length();
            byte[] bt = new byte[length];
            giris.read(bt,0,length);

            giris.close();
            val.bytes = bt;
            val.link = path;
            return val;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }

    }
}
