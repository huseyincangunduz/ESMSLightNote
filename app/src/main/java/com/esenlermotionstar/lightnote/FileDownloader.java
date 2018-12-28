package com.esenlermotionstar.lightnote; /**
 * Created by hussainlobo on 19.06.2018.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;

public class FileDownloader {

    public static void main(String[] args) {
        try {
            String urlStr = "https://vignette.wikia.nocookie.net/disney/images/9/9d/Dipper-pines.png/revision/latest?cb=20160307202342";
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            String type = conn.getContentType();
            System.out.println(type);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /*String url = "https://www.journaldev.com/sitemap.xml";

        try {
            downloadUsingNIO(url, "/Users/pankaj/sitemap.xml");

            downloadUsingStream(url, "/Users/pankaj/sitemap_stream.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    public static void downloadUsingStream(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        FileOutputStream fis = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int count = 0;
        while ((count = bis.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, count);
        }
        fis.close();
        bis.close();
    }


    public static String downloadAsImage(String urlStr, String workFolder) {
        try
        {
            Log.i(null, "downloadAsImage: adres: " + urlStr);
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            String type = conn.getContentType();

            if (type.toLowerCase().startsWith("image")) {

                if (!workFolder.endsWith("/")) workFolder += "/";

                String host = url.getHost(),
                        filename = host,
                        extension = ".jpg",
                        filePath = workFolder + filename + extension;

                File file = new File(filePath);
                int deneme = 0;

                while (file.exists())
                {
                    if (deneme == 0)
                    {
                        deneme = ((new File(workFolder)).listFiles().length) + 1;
                    }
                    else
                    {
                        deneme++;
                    }
                    filePath = workFolder + String.format(filename + "({0})",deneme) + extension;
                    file = new File(filePath);

                }
                Log.i(null, "downloadAsImage: dosya şuraya inecek: " + filePath);
                BufferedInputStream bis = new BufferedInputStream(url.openStream());
                Bitmap bmp = BitmapFactory.decodeStream(bis);
                FileOutputStream cikisStream = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 70, cikisStream);
                cikisStream.close();

                return filePath;
            } else
                return null;
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }



    }

    public static void downloadUsingNIO(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

}